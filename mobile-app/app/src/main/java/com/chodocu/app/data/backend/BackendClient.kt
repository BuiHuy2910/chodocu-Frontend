package com.chodocu.app.data.backend

import com.chodocu.app.BuildConfig
import com.chodocu.app.core.AppConfig
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.common.ApiException.Kind
import com.chodocu.app.data.common.objects
import com.chodocu.app.data.common.stringOrNull
import com.chodocu.app.data.common.wrapErrors
import com.chodocu.app.data.session.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cầu nối giữa app và backend Spring Boot.
 * - Tự gắn "Authorization: Bearer <JWT>" khi đã đăng nhập.
 * - [callObject] / [callList] trả về phần "data" của phản hồi, lỗi được đổi thành [ApiException].
 */
class BackendClient(private val session: SessionManager) {

    /** Gson mặc định không gửi trường null: backend hiểu trường vắng mặt là "giữ nguyên". */
    private val gson = Gson()

    val api: BackendApi = Retrofit.Builder()
        .baseUrl(AppConfig.BACKEND_BASE_URL)
        .client(createHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(BackendApi::class.java)

    /** Tạo thân yêu cầu JSON từ các cặp tên - giá trị. */
    fun body(vararg fields: Pair<String, Any?>): JsonObject =
        JsonObject().apply { fields.forEach { (name, value) -> add(name, gson.toJsonTree(value)) } }

    suspend fun callObject(request: suspend BackendApi.() -> Response<JsonObject>): JsonObject =
        wrapErrors { call(request).asJsonObject }

    suspend fun callList(request: suspend BackendApi.() -> Response<JsonObject>): List<JsonObject> =
        wrapErrors { call(request).asJsonArray.objects() }

    /** Gọi API không cần đọc kết quả. */
    suspend fun run(request: suspend BackendApi.() -> Response<JsonObject>) {
        wrapErrors { call(request) }
    }

    private suspend fun call(request: suspend BackendApi.() -> Response<JsonObject>): JsonElement {
        val response = api.request()
        val body = response.body()
        if (response.isSuccessful && body != null) return body.get("data") ?: JsonNull.INSTANCE
        throw errorFrom(response.code(), response.errorBody()?.string())
    }

    /** Backend báo lỗi dạng {"success": false, "message": "câu tiếng Việt"} kèm mã HTTP. */
    private fun errorFrom(httpCode: Int, rawBody: String?): ApiException {
        val message = runCatching {
            JsonParser.parseString(rawBody.orEmpty()).asJsonObject.stringOrNull("message")
        }.getOrNull()

        return when {
            // Đang đăng nhập mà bị từ chối token: phiên đã hết hạn
            httpCode == 401 && session.token != null -> {
                session.expire()
                ApiException(ApiException.MSG_SESSION_EXPIRED, Kind.UNAUTHORIZED)
            }
            httpCode == 401 -> ApiException(message ?: "Email hoặc mật khẩu không đúng.", Kind.VALIDATION)
            httpCode == 403 -> ApiException(message ?: "Bạn không có quyền thực hiện thao tác này.", Kind.FORBIDDEN)
            httpCode == 404 -> ApiException(message ?: "Không tìm thấy dữ liệu.", Kind.NOT_FOUND)
            httpCode == 409 -> ApiException(message ?: "Dữ liệu đã thay đổi, vui lòng tải lại.", Kind.CONFLICT)
            httpCode in 400..499 -> ApiException(message ?: ApiException.MSG_UNKNOWN, Kind.VALIDATION)
            httpCode >= 500 -> ApiException(ApiException.MSG_SERVER, Kind.SERVER)
            else -> ApiException(ApiException.MSG_UNKNOWN, Kind.UNKNOWN)
        }
    }

    private fun createHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val token = session.token
                val request = if (token == null) {
                    chain.request()
                } else {
                    chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                }
                chain.proceed(request)
            }
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }
        return builder.build()
    }
}

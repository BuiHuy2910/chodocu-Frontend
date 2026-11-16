package com.chodocu.app.data.supabase

import com.chodocu.app.BuildConfig
import com.chodocu.app.core.AppConfig
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.common.ApiException.Kind
import com.chodocu.app.data.common.objects
import com.chodocu.app.data.common.stringOrNull
import com.chodocu.app.data.common.wrapErrors
import com.chodocu.app.data.session.SessionManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cầu nối giữa app và Supabase.
 *
 * - [callObject] / [callList]: gọi hàm api_* và nhận về JSON. Mặc định tự gửi kèm mã phiên `p_token`.
 * - [uploadJpeg]: tải ảnh lên Storage, trả về đường dẫn công khai.
 * - Mọi lỗi được đổi thành [ApiException] có câu báo tiếng Việt. Riêng lỗi hết phiên đăng nhập
 *   sẽ xóa phiên trên máy để app tự quay về màn đăng nhập.
 */
class SupabaseClient(private val session: SessionManager) {

    /** serializeNulls: tham số null vẫn được gửi đi để Postgres nhận đúng chữ ký hàm. */
    private val gson = GsonBuilder().serializeNulls().create()
    private val baseUrl = AppConfig.SUPABASE_URL.trimEnd('/') + "/"

    private val api: SupabaseApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(createHttpClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(SupabaseApi::class.java)

    /** Gọi hàm trả về một đối tượng JSON. */
    suspend fun callObject(
        function: String,
        vararg params: Pair<String, Any?>,
        authenticated: Boolean = true,
    ): JsonObject = wrapErrors { call(function, params, authenticated).asJsonObject }

    /** Gọi hàm trả về một danh sách đối tượng JSON. */
    suspend fun callList(
        function: String,
        vararg params: Pair<String, Any?>,
        authenticated: Boolean = true,
    ): List<JsonObject> = wrapErrors { call(function, params, authenticated).asJsonArray.objects() }

    suspend fun fetchCategories(): List<JsonObject> = wrapErrors { readTable(api.categories()) }

    suspend fun fetchUniversities(): List<JsonObject> = wrapErrors { readTable(api.universities()) }

    /** Tải ảnh JPEG lên bucket ảnh của app, trả về đường dẫn công khai để lưu vào tin / hồ sơ / chat. */
    suspend fun uploadJpeg(path: String, bytes: ByteArray): String = wrapErrors {
        val response = api.upload(AppConfig.STORAGE_BUCKET, path, bytes.toRequestBody(JPEG))
        response.body()?.close()
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw uploadError(response.code())
        }
        "${baseUrl}storage/v1/object/public/${AppConfig.STORAGE_BUCKET}/$path"
    }

    // -----------------------------------------------------------------------------------------

    private suspend fun call(
        function: String,
        params: Array<out Pair<String, Any?>>,
        authenticated: Boolean,
    ): JsonElement {
        val body = JsonObject()
        if (authenticated) body.addProperty("p_token", session.requireToken())
        params.forEach { (name, value) -> body.add(name, gson.toJsonTree(value)) }

        val response = api.rpc(function, body)
        if (response.isSuccessful) return response.body() ?: JsonNull.INSTANCE
        throw errorFrom(response.code(), response.errorBody()?.string())
    }

    private fun readTable(response: Response<JsonArray>): List<JsonObject> {
        if (response.isSuccessful) return response.body()?.objects().orEmpty()
        throw errorFrom(response.code(), response.errorBody()?.string())
    }

    /**
     * Đọc lỗi PostgREST dạng {"code": "P0001", "message": "...", "hint": "VALIDATION"}.
     * Hàm SQL luôn đặt message là câu tiếng Việt và hint là mã lỗi (xem đầu file SQL).
     */
    private fun errorFrom(httpCode: Int, rawBody: String?): ApiException {
        val json = runCatching { JsonParser.parseString(rawBody.orEmpty()).asJsonObject }.getOrNull()
        val code = json?.stringOrNull("code")
        val hint = json?.stringOrNull("hint")
        val message = json?.stringOrNull("message")
        val kind = hint?.let(::kindOfHint)

        return when {
            hint == HINT_INVALID_SESSION -> {
                session.expire()
                ApiException(ApiException.MSG_SESSION_EXPIRED, Kind.UNAUTHORIZED)
            }
            kind != null && message != null -> ApiException(message, kind)
            // Hàm chưa tồn tại / sai tham số: máy chủ chưa chạy bản SQL mới nhất
            code == "PGRST202" || code == "42883" -> ApiException(MSG_SQL_OUTDATED, Kind.SERVER)
            code == "P0001" && message != null -> ApiException(message, Kind.VALIDATION)
            httpCode == 401 || httpCode == 403 -> ApiException(MSG_BAD_KEY, Kind.SERVER)
            httpCode >= 500 -> ApiException(ApiException.MSG_SERVER, Kind.SERVER)
            else -> ApiException(ApiException.MSG_UNKNOWN, Kind.UNKNOWN)
        }
    }

    private fun kindOfHint(hint: String): Kind? = when (hint) {
        "VALIDATION", "WRONG_CREDENTIALS" -> Kind.VALIDATION
        "FORBIDDEN", "ACCOUNT_LOCKED" -> Kind.FORBIDDEN
        "CONFLICT", "EMAIL_EXISTS" -> Kind.CONFLICT
        "NOT_FOUND" -> Kind.NOT_FOUND
        else -> null // hint lạ (ví dụ của chính Supabase) thì không tin message tiếng Anh
    }

    private fun uploadError(httpCode: Int): ApiException = when (httpCode) {
        413 -> ApiException("Ảnh quá lớn. Vui lòng chọn ảnh khác.", Kind.VALIDATION)
        400, 401, 403 -> ApiException(MSG_UPLOAD_DENIED, Kind.SERVER)
        else -> ApiException("Không tải được ảnh lên. Vui lòng thử lại.", Kind.SERVER)
    }

    private fun createHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS) // tải ảnh qua mạng yếu có thể lâu
            .addInterceptor { chain ->
                // Supabase yêu cầu khóa API ở cả hai header
                val request = chain.request().newBuilder()
                    .header("apikey", AppConfig.SUPABASE_ANON_KEY)
                    .header("Authorization", "Bearer ${AppConfig.SUPABASE_ANON_KEY}")
                    .build()
                chain.proceed(request)
            }
        if (BuildConfig.DEBUG) {
            // Chỉ ghi dòng địa chỉ + mã kết quả, không ghi nội dung (có mật khẩu, mã phiên)
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        }
        return builder.build()
    }

    private companion object {
        const val HINT_INVALID_SESSION = "INVALID_SESSION"
        const val MSG_SQL_OUTDATED =
            "Máy chủ chưa được cập nhật. Hãy chạy lại file supabase_direct_setup.sql trên Supabase."
        const val MSG_BAD_KEY = "Không kết nối được máy chủ dữ liệu. Vui lòng kiểm tra cấu hình Supabase."
        const val MSG_UPLOAD_DENIED =
            "Máy chủ từ chối ảnh tải lên. Hãy kiểm tra bucket ảnh trong supabase_direct_setup.sql."
        val JPEG = "image/jpeg".toMediaType()
    }
}

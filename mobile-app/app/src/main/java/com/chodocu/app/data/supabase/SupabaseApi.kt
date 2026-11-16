package com.chodocu.app.data.supabase

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Các địa chỉ REST của Supabase mà app dùng.
 * Hầu hết thao tác đi qua [rpc] (gọi hàm api_* trong file supabase_direct_setup.sql).
 */
interface SupabaseApi {

    /** Gọi một hàm api_* trong Postgres. Tham số gửi dạng JSON {"p_xxx": giá trị}. */
    @POST("rest/v1/rpc/{function}")
    suspend fun rpc(@Path("function") function: String, @Body params: JsonObject): Response<JsonElement>

    /** Danh mục (bảng đọc công khai). */
    @GET("rest/v1/categories?select=id,name,icon&order=id.asc")
    suspend fun categories(): Response<JsonArray>

    /** Danh sách trường kèm điểm hẹn chuẩn (bảng đọc công khai). */
    @GET("rest/v1/universities?select=id,name,short_name,safe_meetup_spots&order=id.asc")
    suspend fun universities(): Response<JsonArray>

    /** Tải một tệp lên Supabase Storage. [path] gồm cả thư mục, ví dụ "products/202611/abc.jpg". */
    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun upload(
        @Path("bucket") bucket: String,
        @Path("path", encoded = true) path: String,
        @Body file: RequestBody,
        @Header("x-upsert") upsert: String = "false",
    ): Response<ResponseBody>
}

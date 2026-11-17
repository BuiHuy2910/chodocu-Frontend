package com.chodocu.app.data.backend

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Các API của backend Spring Boot (đường dẫn tính từ AppConfig.BACKEND_BASE_URL = ".../api/").
 * Mọi API trả về dạng {"success": true, "message": "...", "data": ...}; BackendClient tự lấy phần "data".
 */
interface BackendApi {

    // ---- Tài khoản ----
    @POST("auth/login")
    suspend fun login(@Body body: JsonObject): Response<JsonObject>

    @POST("auth/register")
    suspend fun register(@Body body: JsonObject): Response<JsonObject>

    @GET("auth/me")
    suspend fun me(): Response<JsonObject>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: JsonObject): Response<JsonObject>

    @PUT("auth/avatar")
    suspend fun updateAvatar(@Body body: JsonObject): Response<JsonObject>

    @PUT("auth/password")
    suspend fun changePassword(@Body body: JsonObject): Response<JsonObject>

    // ---- Danh mục, trường ----
    @GET("categories")
    suspend fun categories(): Response<JsonObject>

    @GET("universities")
    suspend fun universities(): Response<JsonObject>

    // ---- Tin đăng ----
    @GET("products")
    suspend fun searchProducts(
        @Query("keyword") keyword: String?,
        @Query("categoryId") categoryId: Long?,
        @Query("universityId") universityId: Long?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sortBy") sortBy: String,
        @Query("sortDir") sortDir: String,
    ): Response<JsonObject>

    @GET("products/{id}")
    suspend fun product(@Path("id") productId: Long): Response<JsonObject>

    @GET("products/my-posts")
    suspend fun myPosts(): Response<JsonObject>

    @POST("products")
    suspend fun createProduct(@Body body: JsonObject): Response<JsonObject>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") productId: Long, @Body body: JsonObject): Response<JsonObject>

    @PATCH("products/{id}/status")
    suspend fun changeProductStatus(@Path("id") productId: Long, @Query("status") status: String): Response<JsonObject>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long): Response<JsonObject>

    @POST("wishlist/{productId}")
    suspend fun toggleWishlist(@Path("productId") productId: Long): Response<JsonObject>

    @GET("wishlist")
    suspend fun wishlist(): Response<JsonObject>

    @POST("reports")
    suspend fun reportProduct(@Body body: JsonObject): Response<JsonObject>

    // ---- Đơn đặt mua ----
    @POST("orders")
    suspend fun placeOrder(@Body body: JsonObject): Response<JsonObject>

    @GET("orders/my-purchases")
    suspend fun myPurchases(): Response<JsonObject>

    @GET("orders/my-sales")
    suspend fun mySales(): Response<JsonObject>

    /** [action] là accept / reject / complete / dispute. Người mua hủy đơn cũng dùng "reject". */
    @PUT("orders/{id}/{action}")
    suspend fun updateOrder(
        @Path("id") orderId: Long,
        @Path("action") action: String,
        @Body body: JsonObject,
    ): Response<JsonObject>

    @GET("orders/fee-preview")
    suspend fun feePreview(@Query("amount") amount: Long): Response<JsonObject>

    // ---- Chat ----
    @GET("chat/conversations")
    suspend fun conversations(): Response<JsonObject>

    @GET("chat/messages/{partnerId}")
    suspend fun messages(@Path("partnerId") partnerId: Long): Response<JsonObject>

    @POST("chat/send")
    suspend fun sendMessage(@Body body: JsonObject): Response<JsonObject>

    @PUT("chat/conversations/{partnerId}/read")
    suspend fun markConversationRead(@Path("partnerId") partnerId: Long): Response<JsonObject>

    @GET("chat/unread-count")
    suspend fun unreadMessageCount(): Response<JsonObject>

    // ---- Thông báo ----
    @GET("notifications")
    suspend fun notifications(): Response<JsonObject>

    @GET("notifications/unread-count")
    suspend fun unreadNotificationCount(): Response<JsonObject>

    @PUT("notifications/mark-all-read")
    suspend fun markAllNotificationsRead(): Response<JsonObject>

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(@Body body: JsonObject): Response<JsonObject>

    @DELETE("notifications/device-token")
    suspend fun unregisterDeviceToken(@Query("token") token: String): Response<JsonObject>

    // ---- Ảnh ----
    @Multipart
    @POST("upload")
    suspend fun upload(@Part file: MultipartBody.Part, @Query("folder") folder: String): Response<JsonObject>
}

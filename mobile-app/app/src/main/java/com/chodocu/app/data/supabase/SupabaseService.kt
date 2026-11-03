package com.chodocu.app.data.supabase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class SCategory(
    val id: Long = 0,
    val name: String? = null,
    val slug: String? = null,
    val icon: String? = null
)

data class SUniversity(
    val id: Long = 0,
    val name: String? = null,
    val shortName: String? = null,
    val emailDomain: String? = null,
    val address: String? = null,
    val safeMeetupSpots: String? = null
)

/**
 * PostgREST + RPC của Supabase.
 * Không dùng JWT của backend; xác thực bằng session token do RPC cấp.
 */
interface SupabaseService {

    // ---------- Đọc công khai ----------
    @GET("rest/v1/categories")
    suspend fun categories(
        @Query("select") select: String = "id,name,slug,icon",
        @Query("order") order: String = "name.asc"
    ): List<SCategory>

    @GET("rest/v1/universities")
    suspend fun universities(
        @Query("select") select: String = "id,name,short_name,email_domain,address,safe_meetup_spots",
        @Query("order") order: String = "name.asc"
    ): List<SUniversity>

    @GET("rest/v1/v_public_products")
    suspend fun products(
        @Query("select") select: String = "*",
        @Query("status") status: String = "eq.APPROVED",
        @Query("title") title: String? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("university_id") universityId: String? = null,
        @Query("and") andFilter: String? = null,
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 60,
        @Query("offset") offset: Int = 0
    ): List<SProduct>

    @GET("rest/v1/v_public_products")
    suspend fun productById(
        @Query("id") idFilter: String,
        @Query("limit") limit: Int = 1
    ): List<SProduct>

    // ---------- Auth ----------
    @POST("rest/v1/rpc/api_login")
    suspend fun login(@Body body: Map<String, Any?>): Response<List<SLogin>>

    @POST("rest/v1/rpc/api_register")
    suspend fun register(@Body body: Map<String, Any?>): Response<List<SLogin>>

    @POST("rest/v1/rpc/api_logout")
    suspend fun logout(@Body body: Map<String, Any?>): Response<Boolean>

    @POST("rest/v1/rpc/api_me")
    suspend fun me(@Body body: Map<String, Any?>): Response<List<SUser>>

    @POST("rest/v1/rpc/api_update_profile")
    suspend fun updateProfile(@Body body: Map<String, Any?>): Response<List<SUser>>

    // ---------- Sản phẩm của tôi ----------
    @POST("rest/v1/rpc/api_my_posts")
    suspend fun myPosts(@Body body: Map<String, Any?>): Response<List<SProduct>>

    @POST("rest/v1/rpc/api_create_product")
    suspend fun createProduct(@Body body: Map<String, Any?>): Response<List<SProduct>>

    @POST("rest/v1/rpc/api_delete_product")
    suspend fun deleteProduct(@Body body: Map<String, Any?>): Response<Boolean>

    // ---------- Đơn hàng ----------
    @POST("rest/v1/rpc/api_my_orders")
    suspend fun myOrders(@Body body: Map<String, Any?>): Response<List<SOrder>>

    @POST("rest/v1/rpc/api_my_wishlist")
    suspend fun myWishlist(@Body body: Map<String, Any?>): Response<List<SProduct>>

    @POST("rest/v1/rpc/api_toggle_wishlist")
    suspend fun toggleWishlist(@Body body: Map<String, Any?>): Response<Boolean>

    @POST("rest/v1/rpc/api_place_order")
    suspend fun placeOrder(@Body body: Map<String, Any?>): Response<List<SOrder>>

    @POST("rest/v1/rpc/api_update_order")
    suspend fun updateOrder(@Body body: Map<String, Any?>): Response<List<SOrder>>

    @POST("rest/v1/rpc/api_fee_preview")
    suspend fun feePreview(@Body body: Map<String, Any?>): Response<List<SFee>>

    // ---------- Chat ----------
    @POST("rest/v1/rpc/api_messages")
    suspend fun messages(@Body body: Map<String, Any?>): Response<List<SChat>>

    @POST("rest/v1/rpc/api_send_message")
    suspend fun sendMessage(@Body body: Map<String, Any?>): Response<List<SChat>>
}

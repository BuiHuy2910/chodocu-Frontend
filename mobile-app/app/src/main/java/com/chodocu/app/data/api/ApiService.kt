package com.chodocu.app.data.api

import com.chodocu.app.data.model.*
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

interface ApiService {

    // 1. Authentication (Đăng ký, Đăng nhập bằng email sinh viên hỗ trợ JWT)
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    // 2. Danh mục & Trường Đại học
    @GET("categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    @GET("universities")
    suspend fun getUniversities(): Response<ApiResponse<List<UniversityDto>>>

    // 3. Quản lý Tin đăng & Tìm kiếm, lọc theo trường/khoảng giá/danh mục + phân trang
    @GET("products")
    suspend fun searchProducts(
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("universityId") universityId: Long? = null,
        @Query("minPrice") minPrice: BigDecimal? = null,
        @Query("maxPrice") maxPrice: BigDecimal? = null,
        @Query("urgentOnly") urgentOnly: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12
    ): Response<ApiResponse<PageResponse<ProductDto>>>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: Long): Response<ApiResponse<ProductDto>>

    @POST("products")
    suspend fun createProduct(@Body request: ProductCreateRequest): Response<ApiResponse<ProductDto>>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: ProductCreateRequest
    ): Response<ApiResponse<ProductDto>>

    @PATCH("products/{id}/status")
    suspend fun updateProductStatus(
        @Path("id") id: Long,
        @Query("status") status: String
    ): Response<ApiResponse<ProductDto>>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @GET("products/my-posts")
    suspend fun getMyPosts(): Response<ApiResponse<List<ProductDto>>>

    // 4. Luồng mua bán & Giao dịch (Đặt mua -> Duyệt -> Đang giao dịch / Đã bán)
    @POST("orders")
    suspend fun createOrder(@Body request: OrderCreateRequest): Response<ApiResponse<OrderDto>>

    @PUT("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: Long): Response<ApiResponse<OrderDto>>

    @PUT("orders/{id}/complete")
    suspend fun completeOrder(@Path("id") orderId: Long): Response<ApiResponse<OrderDto>>

    @PUT("orders/{id}/reject")
    suspend fun rejectOrder(@Path("id") orderId: Long): Response<ApiResponse<OrderDto>>

    @GET("orders/my-purchases")
    suspend fun getMyPurchases(): Response<ApiResponse<List<OrderDto>>>

    @GET("orders/my-sales")
    suspend fun getMySales(): Response<ApiResponse<List<OrderDto>>>

    // 5. Yêu thích (Wishlist)
    @POST("wishlist/{productId}")
    suspend fun toggleWishlist(@Path("productId") productId: Long): Response<ApiResponse<Map<String, Boolean>>>

    @GET("wishlist")
    suspend fun getMyWishlist(): Response<ApiResponse<List<ProductDto>>>

    // 6. Chat trao đổi giao dịch
    @GET("chat/messages/{partnerId}")
    suspend fun getChatMessages(@Path("partnerId") partnerId: Long): Response<ApiResponse<List<ChatMessageDto>>>

    @POST("chat/send")
    suspend fun sendChatMessage(@Body request: ChatMessageRequest): Response<ApiResponse<ChatMessageDto>>
}


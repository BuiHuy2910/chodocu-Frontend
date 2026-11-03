package com.chodocu.app.data.supabase

import android.content.Context
import android.net.Uri
import com.chodocu.app.core.AppConfig
import com.chodocu.app.data.model.*
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.util.UUID

/**
 * Truy cập Supabase trực tiếp từ app (không qua Spring Boot).
 * Mọi lệnh ghi đều đi qua RPC SECURITY DEFINER + session token.
 */
object SupabaseSession {
    @Volatile
    var token: String? = null

    @Volatile
    var userId: Long? = null

    fun clear() {
        token = null
        userId = null
    }
}

class SupabaseException(message: String) : Exception(message)

object SupabaseRepository {

    private val api get() = SupabaseClient.service

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun requireToken(): String =
        SupabaseSession.token ?: throw SupabaseException("Bạn cần đăng nhập để thực hiện thao tác này.")

    private fun <T> Response<T>.dataOrThrow(): T {
        if (isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            return body() as T
        }
        throw SupabaseException(readErrorMessage(this))
    }

    /**
     * Các endpoint trả thẳng `List<...>` (không bọc `Response`) sẽ ném HttpException/IOException.
     * Bọc lại để UI luôn nhận thông báo tiếng Việt thay vì "HTTP 401 Unauthorized".
     */
    private suspend fun <T> direct(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        throw SupabaseException(httpMessage(e.code(), raw))
    } catch (e: IOException) {
        throw SupabaseException("Không kết nối được Supabase. Kiểm tra mạng rồi thử lại.")
    }

    private fun readErrorMessage(response: Response<*>): String {
        val raw = try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        return httpMessage(response.code(), raw)
    }

    private fun httpMessage(code: Int, raw: String?): String {
        val codeOrMessage = try {
            raw?.let {
                val obj = JsonParser.parseString(it).asJsonObject
                obj.get("message")?.asString ?: obj.get("details")?.asString
            }
        } catch (_: Exception) {
            null
        }

        return when {
            code == 404 -> "Không tìm thấy API trên Supabase (404). Kiểm tra AppConfig.SUPABASE_URL."
            codeOrMessage == null -> "Máy chủ trả về lỗi $code"
            codeOrMessage.contains("PGRST202") || codeOrMessage.contains("Could not find the function") ->
                "Supabase chưa cài đặt hàm API. Hãy chạy file supabase_direct_setup.sql trong SQL Editor."
            codeOrMessage.contains("permission denied") || codeOrMessage.contains("42501") ->
                "Supabase từ chối truy cập bảng này (kiểm tra GRANT và RLS policy)."
            codeOrMessage.contains("EMAIL_EXISTS") -> "Email này đã được đăng ký."
            codeOrMessage.contains("EMAIL_NOT_FOUND") -> "Email chưa tồn tại trên hệ thống."
            codeOrMessage.contains("WRONG_PASSWORD") -> "Mật khẩu không đúng."
            codeOrMessage.contains("ACCOUNT_LOCKED") -> "Tài khoản đã bị khóa."
            codeOrMessage.contains("INVALID_SESSION") -> "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại."
            codeOrMessage.contains("PASSWORD_TOO_SHORT") -> "Mật khẩu phải có ít nhất 6 ký tự."
            codeOrMessage.contains("EMAIL_REQUIRED") -> "Vui lòng nhập email."
            codeOrMessage.contains("CANNOT_BUY_OWN") -> "Bạn không thể tự mua sản phẩm của mình."
            codeOrMessage.contains("PRODUCT_SOLD") -> "Sản phẩm đã được bán."
            codeOrMessage.contains("PRODUCT_NOT_AVAILABLE") -> "Sản phẩm chưa được duyệt hoặc đang giao dịch."
            codeOrMessage.contains("INVALID_TRANSITION") -> "Không thể chuyển trạng thái đơn ở bước này."
            codeOrMessage.contains("NOT_PARTICIPANT") -> "Bạn không phải người tham gia đơn hàng này."
            codeOrMessage.contains("NOT_OWNER") -> "Bạn chỉ có thể thao tác trên tin của mình."
            codeOrMessage.contains("TITLE_REQUIRED") -> "Vui lòng nhập tên món đồ."
            codeOrMessage.contains("EMPTY_MESSAGE") -> "Nội dung tin nhắn đang trống."
            else -> codeOrMessage
        }
    }

    // =========================================================================
    // Danh mục / trường / feed
    // =========================================================================

    suspend fun categories(): List<CategoryDto> = withContext(Dispatchers.IO) {
        direct { api.categories() }.map {
            CategoryDto(
                id = it.id,
                name = it.name.orEmpty(),
                slug = it.slug.orEmpty(),
                icon = it.icon
            )
        }
    }

    suspend fun universities(): List<UniversityDto> = withContext(Dispatchers.IO) {
        direct { api.universities() }.map {
            UniversityDto(
                id = it.id,
                name = it.name.orEmpty(),
                shortName = it.shortName.orEmpty(),
                emailDomain = it.emailDomain,
                address = it.address,
                safeMeetupSpots = it.safeMeetupSpots
            )
        }
    }

    suspend fun feed(
        keyword: String? = null,
        categoryId: Long? = null,
        universityId: Long? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null
    ): List<ProductDto> = withContext(Dispatchers.IO) {
        val priceFilter = when {
            minPrice != null && maxPrice != null -> "(price.gte.$minPrice,price.lte.$maxPrice)"
            minPrice != null -> "(price.gte.$minPrice)"
            maxPrice != null -> "(price.lte.$maxPrice)"
            else -> null
        }

        val rows = direct {
            api.products(
                title = keyword?.takeIf { it.isNotBlank() }?.let { "ilike.*$it*" },
                categoryId = categoryId?.let { "eq.$it" },
                universityId = universityId?.let { "eq.$it" },
                andFilter = priceFilter
            )
        }

        val favoriteIds = myFavoriteIds()
        rows.map { it.toDto(isFavorited = favoriteIds.contains(it.id)) }
    }

    suspend fun productDetail(id: Long): ProductDto = withContext(Dispatchers.IO) {
        val row = direct { api.productById("eq.$id") }.firstOrNull()
            ?: throw SupabaseException("Không tìm thấy tin đăng này.")
        row.toDto(isFavorited = myFavoriteIds().contains(row.id))
    }

    private suspend fun myFavoriteIds(): Set<Long> {
        if (SupabaseSession.token == null) return emptySet()
        return runCatching {
            api.myWishlist(mapOf("p_token" to requireToken()))
                .dataOrThrow()
                .map { it.id }
                .toSet()
        }.getOrDefault(emptySet())
    }

    // =========================================================================
    // Auth
    // =========================================================================

    suspend fun login(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        val result = api.login(
            mapOf("p_email" to email.trim(), "p_password" to password)
        ).dataOrThrow().firstOrNull() ?: throw SupabaseException("Không nhận được phiên đăng nhập.")

        val token = result.outToken ?: throw SupabaseException("Không nhận được phiên đăng nhập.")
        SupabaseSession.token = token
        SupabaseSession.userId = result.outUserId

        val user = try {
            me()
        } catch (e: Exception) {
            // Không đọc được hồ sơ thì phải huỷ phiên vừa tạo, tránh kẹt trạng thái nửa vời
            runCatching { api.logout(mapOf("p_token" to token)) }
            SupabaseSession.clear()
            throw SupabaseException("Đăng nhập được nhưng không đọc được hồ sơ: ${e.message}")
        }
        AuthResponse(accessToken = token, tokenType = "Session", user = user)
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        studentId: String?,
        phone: String?,
        universityId: Long?,
        dormArea: String?
    ): AuthResponse = withContext(Dispatchers.IO) {
        val result = api.register(
            mapOf(
                "p_email" to email.trim(),
                "p_password" to password,
                "p_full_name" to fullName.trim(),
                "p_student_id" to studentId?.takeIf { it.isNotBlank() },
                "p_phone" to phone?.takeIf { it.isNotBlank() },
                "p_university_id" to universityId,
                "p_dorm_area" to dormArea?.takeIf { it.isNotBlank() }
            )
        ).dataOrThrow().firstOrNull() ?: throw SupabaseException("Không tạo được tài khoản.")

        val token = result.outToken ?: throw SupabaseException("Không tạo được phiên đăng nhập.")
        SupabaseSession.token = token
        SupabaseSession.userId = result.outUserId

        val user = try {
            me()
        } catch (e: Exception) {
            runCatching { api.logout(mapOf("p_token" to token)) }
            SupabaseSession.clear()
            throw SupabaseException("Tạo được tài khoản nhưng không đọc được hồ sơ: ${e.message}")
        }
        AuthResponse(accessToken = token, tokenType = "Session", user = user)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val token = SupabaseSession.token
        if (token != null) {
            runCatching { api.logout(mapOf("p_token" to token)) }
        }
        SupabaseSession.clear()
    }

    suspend fun me(): UserDto = withContext(Dispatchers.IO) {
        api.me(mapOf("p_token" to requireToken()))
            .dataOrThrow()
            .firstOrNull()?.toDto()
            ?: throw SupabaseException("Không tìm thấy hồ sơ người dùng.")
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        dormArea: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ): UserDto = withContext(Dispatchers.IO) {
        api.updateProfile(
            mapOf(
                "p_token" to requireToken(),
                "p_full_name" to fullName?.takeIf { it.isNotBlank() },
                "p_phone" to phone,
                "p_dorm_area" to dormArea,
                "p_bio" to bio,
                "p_avatar_url" to avatarUrl
            )
        ).dataOrThrow().firstOrNull()?.toDto()
            ?: throw SupabaseException("Không cập nhật được hồ sơ.")
    }

    // =========================================================================
    // Sản phẩm
    // =========================================================================

    suspend fun myPosts(): List<ProductDto> = withContext(Dispatchers.IO) {
        api.myPosts(mapOf("p_token" to requireToken()))
            .dataOrThrow()
            .map { it.toDto() }
    }

    suspend fun createProduct(
        title: String,
        description: String?,
        price: BigDecimal,
        isFree: Boolean,
        isUrgent: Boolean,
        condition: String,
        conditionNote: String?,
        pickupSpot: String?,
        categoryId: Long?,
        universityId: Long?,
        imageUrls: List<String>
    ): ProductDto = withContext(Dispatchers.IO) {
        api.createProduct(
            mapOf(
                "p_token" to requireToken(),
                "p_title" to title,
                "p_description" to description,
                "p_price" to price,
                "p_is_free" to isFree,
                "p_is_urgent" to isUrgent,
                "p_condition" to condition,
                "p_condition_note" to conditionNote,
                "p_pickup_spot" to pickupSpot,
                "p_category_id" to categoryId,
                "p_university_id" to universityId,
                "p_images" to imageUrls
            )
        ).dataOrThrow().firstOrNull()?.toDto()
            ?: throw SupabaseException("Không tạo được tin đăng.")
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        api.deleteProduct(mapOf("p_token" to requireToken(), "p_product_id" to productId))
            .dataOrThrow()
    }

    // =========================================================================
    // Đơn hàng
    // =========================================================================

    suspend fun myOrders(): List<OrderDto> = withContext(Dispatchers.IO) {
        api.myOrders(mapOf("p_token" to requireToken()))
            .dataOrThrow()
            .map { it.toDto() }
    }

    suspend fun myWishlist(): List<ProductDto> = withContext(Dispatchers.IO) {
        api.myWishlist(mapOf("p_token" to requireToken()))
            .dataOrThrow()
            .map { it.toDto(isFavorited = true) }
    }

    suspend fun toggleWishlist(productId: Long): Boolean = withContext(Dispatchers.IO) {
        api.toggleWishlist(mapOf("p_token" to requireToken(), "p_product_id" to productId))
            .dataOrThrow()
    }

    suspend fun placeOrder(
        productId: Long,
        offeredPrice: BigDecimal,
        meetupLocation: String?,
        meetupTime: String?,
        buyerNote: String?
    ): OrderDto = withContext(Dispatchers.IO) {
        api.placeOrder(
            mapOf(
                "p_token" to requireToken(),
                "p_product_id" to productId,
                "p_offered_price" to offeredPrice,
                "p_meetup_location" to meetupLocation,
                "p_meetup_time" to meetupTime,
                "p_buyer_note" to buyerNote
            )
        ).dataOrThrow().firstOrNull()?.toDto()
            ?: throw SupabaseException("Không tạo được đơn hàng.")
    }

    suspend fun updateOrderStatus(orderId: Long, status: String, note: String? = null): OrderDto =
        withContext(Dispatchers.IO) {
            api.updateOrder(
                mapOf(
                    "p_token" to requireToken(),
                    "p_order_id" to orderId,
                    "p_status" to status,
                    "p_note" to note
                )
            ).dataOrThrow().firstOrNull()?.toDto()
                ?: throw SupabaseException("Không cập nhật được đơn hàng.")
        }

    suspend fun feePreview(amount: BigDecimal): FeeCalculationDto = withContext(Dispatchers.IO) {
        api.feePreview(mapOf("p_amount" to amount))
            .dataOrThrow()
            .firstOrNull()?.toDto(amount)
            ?: FeeCalculationDto(orderAmount = amount)
    }

    // =========================================================================
    // Chat
    // =========================================================================

    suspend fun messages(partnerId: Long): List<ChatMessageDto> = withContext(Dispatchers.IO) {
        api.messages(mapOf("p_token" to requireToken(), "p_partner_id" to partnerId))
            .dataOrThrow()
            .map { it.toDto() }
    }

    suspend fun sendMessage(receiverId: Long, productId: Long?, content: String): ChatMessageDto =
        withContext(Dispatchers.IO) {
            api.sendMessage(
                mapOf(
                    "p_token" to requireToken(),
                    "p_receiver_id" to receiverId,
                    "p_product_id" to productId,
                    "p_content" to content
                )
            ).dataOrThrow().firstOrNull()?.toDto()
                ?: throw SupabaseException("Không gửi được tin nhắn.")
        }

    // =========================================================================
    // Storage
    // =========================================================================

    /**
     * Upload ảnh lên bucket công khai `passuni` bằng anon key.
     * Trả về URL công khai để lưu vào DB.
     */
    suspend fun uploadImage(context: Context, uri: Uri, folder: String = "products"): String =
        withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw SupabaseException("Không đọc được ảnh đã chọn.")
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = when {
                mime.contains("png") -> "png"
                mime.contains("webp") -> "webp"
                mime.contains("gif") -> "gif"
                else -> "jpg"
            }
            val path = "$folder/${UUID.randomUUID()}.$ext"

            val request = Request.Builder()
                .url("${AppConfig.SUPABASE_URL}/storage/v1/object/${AppConfig.STORAGE_BUCKET}/$path")
                .header("apikey", AppConfig.SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer ${AppConfig.SUPABASE_ANON_KEY}")
                .header("x-upsert", "true")
                .post(bytes.toRequestBody(mime.toMediaTypeOrNull()))
                .build()

            SupabaseClient.rawHttp.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw SupabaseException("Tải ảnh thất bại (${response.code}). Kiểm tra policy bucket passuni.")
                }
            }

            "${AppConfig.SUPABASE_URL}/storage/v1/object/public/${AppConfig.STORAGE_BUCKET}/$path"
        }
}

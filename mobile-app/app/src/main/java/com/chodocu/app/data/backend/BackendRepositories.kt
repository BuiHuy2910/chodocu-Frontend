package com.chodocu.app.data.backend

import android.net.Uri
import com.chodocu.app.core.toMeetupString
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.data.common.array
import com.chodocu.app.data.common.boolean
import com.chodocu.app.data.common.int
import com.chodocu.app.data.common.objectOrNull
import com.chodocu.app.data.common.objects
import com.chodocu.app.data.common.string
import com.chodocu.app.data.common.stringOrNull
import com.chodocu.app.data.common.wrapErrors
import com.chodocu.app.data.model.AppNotification
import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.ChatMessage
import com.chodocu.app.data.model.Conversation
import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.Order
import com.chodocu.app.data.model.OrderInput
import com.chodocu.app.data.model.OrderRole
import com.chodocu.app.data.model.PageResult
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductInput
import com.chodocu.app.data.model.ProductQuery
import com.chodocu.app.data.model.ProductSort
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ProfileInput
import com.chodocu.app.data.model.RegisterInput
import com.chodocu.app.data.model.ReportReason
import com.chodocu.app.data.model.SendMessageInput
import com.chodocu.app.data.model.University
import com.chodocu.app.data.model.UploadFolder
import com.chodocu.app.data.model.User
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.ChatRepository
import com.chodocu.app.data.repository.NotificationRepository
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.repository.Repositories
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.data.upload.ImageCompressor
import com.google.gson.JsonObject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Tạo bộ repository gọi backend Spring Boot (khi AppConfig.USE_SUPABASE_DIRECT = false).
 * Các lớp bên dưới chỉ "dịch" lời gọi của app sang API REST của backend.
 */
fun createBackendRepositories(
    session: SessionManager,
    compressor: ImageCompressor,
): Repositories {
    val client = BackendClient(session)
    return Repositories(
        auth = BackendAuthRepository(client, session),
        catalog = BackendCatalogRepository(client),
        products = BackendProductRepository(client),
        orders = BackendOrderRepository(client),
        chat = BackendChatRepository(client),
        notifications = BackendNotificationRepository(client),
        uploads = BackendUploadRepository(client, compressor),
    )
}

// ---------------------------------------------------------------------------------------------
// Tài khoản
// ---------------------------------------------------------------------------------------------
private class BackendAuthRepository(
    private val client: BackendClient,
    private val session: SessionManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): User =
        saveSession(client.callObject { login(client.body("email" to email.trim(), "password" to password)) })

    override suspend fun register(input: RegisterInput): User {
        val body = client.body(
            "fullName" to input.fullName.trim(),
            "email" to input.email.trim(),
            "phone" to input.phone.trim(),
            "password" to input.password,
            "universityId" to input.universityId,
        )
        return saveSession(client.callObject { register(body) })
    }

    /** JWT không cần báo máy chủ khi đăng xuất: chỉ cần xóa phiên trên máy. */
    override suspend fun logout() = session.clear()

    override suspend fun refreshCurrentUser(): User =
        client.callObject { me() }.toUser().also(session::updateUser)

    override suspend fun updateProfile(input: ProfileInput): User {
        val body = client.body(
            "fullName" to input.fullName.trim(),
            "phone" to input.phone.trim(),
            "universityId" to input.universityId,
            // Gửi chuỗi rỗng để xóa (backend coi trường vắng mặt là "giữ nguyên")
            "dormArea" to input.dormArea?.trim().orEmpty(),
            "bio" to input.bio?.trim().orEmpty(),
        )
        return client.callObject { updateProfile(body) }.toUser().also(session::updateUser)
    }

    override suspend fun updateAvatar(avatarUrl: String?): User =
        client.callObject { updateAvatar(client.body("avatarUrl" to avatarUrl.orEmpty())) }
            .toUser()
            .also(session::updateUser)

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        client.run {
            changePassword(client.body("currentPassword" to currentPassword, "newPassword" to newPassword))
        }
    }

    /** Kết quả đăng nhập / đăng ký: {"accessToken": "...", "user": {...}}. */
    private fun saveSession(result: JsonObject): User {
        val token = result.string("accessToken")
        val user = result.objectOrNull("user")?.toUser()
        if (token.isBlank() || user == null) throw ApiException(ApiException.MSG_UNKNOWN)
        session.saveSession(token, user)
        return user
    }
}

// ---------------------------------------------------------------------------------------------
// Danh mục, trường (lưu tạm trong bộ nhớ sau lần tải đầu)
// ---------------------------------------------------------------------------------------------
private class BackendCatalogRepository(private val client: BackendClient) : CatalogRepository {
    private val mutex = Mutex()
    private var cachedCategories: List<Category>? = null
    private var cachedUniversities: List<University>? = null

    override suspend fun getCategories(forceRefresh: Boolean): List<Category> = mutex.withLock {
        cachedCategories?.takeUnless { forceRefresh }
            ?: client.callList { categories() }.map { it.toCategory() }.also { cachedCategories = it }
    }

    override suspend fun getUniversities(forceRefresh: Boolean): List<University> = mutex.withLock {
        cachedUniversities?.takeUnless { forceRefresh }
            ?: client.callList { universities() }.map { it.toUniversity() }.also { cachedUniversities = it }
    }
}

// ---------------------------------------------------------------------------------------------
// Tin đăng
// ---------------------------------------------------------------------------------------------
private class BackendProductRepository(private val client: BackendClient) : ProductRepository {

    override suspend fun searchProducts(query: ProductQuery, page: Int, pageSize: Int): PageResult<Product> {
        val (sortBy, sortDir) = when (query.sort) {
            ProductSort.NEWEST -> "createdAt" to "desc"
            ProductSort.PRICE_ASC -> "price" to "asc"
            ProductSort.PRICE_DESC -> "price" to "desc"
            ProductSort.POPULAR -> "viewCount" to "desc"
        }
        val result = client.callObject {
            searchProducts(
                keyword = query.keyword.trim().ifEmpty { null },
                categoryId = query.categoryId,
                universityId = query.universityId,
                page = page,
                size = pageSize,
                sortBy = sortBy,
                sortDir = sortDir,
            )
        }
        // PageResponse: {"content": [...], "last": true/false}
        return PageResult(
            items = result.array("content").objects().map { it.toProduct() },
            hasMore = !result.boolean("last", default = true),
        )
    }

    override suspend fun getProduct(productId: Long): Product = client.callObject { product(productId) }.toProduct()

    override suspend fun getMyProducts(): List<Product> = client.callList { myPosts() }.map { it.toProduct() }

    override suspend fun createProduct(input: ProductInput): Product =
        client.callObject { createProduct(input.toBody()) }.toProduct()

    override suspend fun updateProduct(productId: Long, input: ProductInput): Product =
        client.callObject { updateProduct(productId, input.toBody()) }.toProduct()

    override suspend fun changeStatus(productId: Long, status: ProductStatus): Product =
        client.callObject { changeProductStatus(productId, status.name) }.toProduct()

    override suspend fun deleteProduct(productId: Long) = client.run { deleteProduct(productId) }

    override suspend fun toggleFavorite(productId: Long): Boolean =
        client.callObject { toggleWishlist(productId) }.boolean("favorited")

    override suspend fun getFavorites(): List<Product> = client.callList { wishlist() }.map { it.toProduct() }

    override suspend fun reportProduct(productId: Long, reason: ReportReason, detail: String?) {
        val body = client.body(
            "productId" to productId,
            "reason" to reason.name,
            "detail" to detail?.trim()?.ifEmpty { null },
        )
        client.run { reportProduct(body) }
    }

    private fun ProductInput.toBody(): JsonObject = client.body(
        "title" to title.trim(),
        "description" to description.trim(),
        "price" to if (isFree) 0L else price,
        "originalPrice" to originalPrice,
        "isFree" to isFree,
        "isUrgent" to isUrgent,
        "condition" to condition.name,
        "conditionNote" to conditionNote?.trim()?.ifEmpty { null },
        "passReason" to passReason?.trim()?.ifEmpty { null },
        "pickupSpot" to pickupSpot,
        "categoryId" to categoryId,
        "universityId" to universityId,
        "imageUrls" to imageUrls,
    )
}

// ---------------------------------------------------------------------------------------------
// Đơn đặt mua
// ---------------------------------------------------------------------------------------------
private class BackendOrderRepository(private val client: BackendClient) : OrderRepository {

    override suspend fun placeOrder(input: OrderInput): Order {
        val body = client.body(
            "productId" to input.productId,
            "offeredPrice" to input.offeredPrice,
            "meetupLocation" to input.meetupLocation,
            "meetupTime" to input.meetupTime.toMeetupString(),
            "buyerNote" to input.note?.trim()?.ifEmpty { null },
        )
        return client.callObject { placeOrder(body) }.toOrder()
    }

    override suspend fun getOrders(role: OrderRole): List<Order> = when (role) {
        OrderRole.BUYER -> client.callList { myPurchases() }
        OrderRole.SELLER -> client.callList { mySales() }
    }.map { it.toOrder() }

    override suspend fun acceptOrder(orderId: Long): Order = updateOrder(orderId, "accept")

    override suspend fun rejectOrder(orderId: Long, reason: String?): Order = updateOrder(orderId, "reject", reason)

    /** Backend dùng chung một API cho người bán từ chối và người mua hủy. */
    override suspend fun cancelOrder(orderId: Long, reason: String?): Order = updateOrder(orderId, "reject", reason)

    override suspend fun completeOrder(orderId: Long): Order = updateOrder(orderId, "complete")

    override suspend fun disputeOrder(orderId: Long, reason: String): Order = updateOrder(orderId, "dispute", reason)

    override suspend fun previewFee(amount: Long): FeePreview = client.callObject { feePreview(amount) }.toFeePreview()

    private suspend fun updateOrder(orderId: Long, action: String, reason: String? = null): Order {
        val body = client.body("reason" to reason?.trim()?.ifEmpty { null })
        return client.callObject { updateOrder(orderId, action, body) }.toOrder()
    }
}

// ---------------------------------------------------------------------------------------------
// Chat
// ---------------------------------------------------------------------------------------------
private class BackendChatRepository(private val client: BackendClient) : ChatRepository {

    override suspend fun getConversations(): List<Conversation> =
        client.callList { conversations() }.map { it.toConversation() }

    override suspend fun getMessages(partnerId: Long): List<ChatMessage> =
        client.callList { messages(partnerId) }.map { it.toChatMessage() }

    override suspend fun sendMessage(input: SendMessageInput): ChatMessage {
        val body = client.body(
            "receiverId" to input.receiverId,
            "productId" to input.productId,
            "messageType" to input.type.name,
            "content" to input.content.trim(),
            "imageUrl" to input.imageUrl,
            "offeredPrice" to input.offeredPrice,
            "meetupLocation" to input.meetupLocation,
            "meetupTime" to input.meetupTime?.toMeetupString(),
        )
        return client.callObject { sendMessage(body) }.toChatMessage()
    }

    override suspend fun markConversationRead(partnerId: Long) = client.run { markConversationRead(partnerId) }

    override suspend fun getUnreadMessageCount(): Int = client.callObject { unreadMessageCount() }.int("count")
}

// ---------------------------------------------------------------------------------------------
// Thông báo
// ---------------------------------------------------------------------------------------------
private class BackendNotificationRepository(private val client: BackendClient) : NotificationRepository {

    override suspend fun getNotifications(): List<AppNotification> =
        client.callList { notifications() }.map { it.toNotification() }

    override suspend fun getUnreadCount(): Int = client.callObject { unreadNotificationCount() }.int("count")

    override suspend fun markAllRead() = client.run { markAllNotificationsRead() }
}

// ---------------------------------------------------------------------------------------------
// Tải ảnh (multipart "file", backend trả về {"url": "..."})
// ---------------------------------------------------------------------------------------------
private class BackendUploadRepository(
    private val client: BackendClient,
    private val compressor: ImageCompressor,
) : UploadRepository {

    override suspend fun uploadImage(uri: Uri, folder: UploadFolder): String = wrapErrors {
        val bytes = compressor.compress(uri)
        val part = MultipartBody.Part.createFormData(
            "file",
            "photo.jpg",
            bytes.toRequestBody("image/jpeg".toMediaType()),
        )
        client.callObject { upload(part, folder.path) }.stringOrNull("url")
            ?: throw ApiException("Không tải được ảnh lên. Vui lòng thử lại.", ApiException.Kind.SERVER)
    }
}

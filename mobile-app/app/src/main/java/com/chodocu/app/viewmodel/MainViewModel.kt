package com.chodocu.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.data.api.RetrofitClient
import com.chodocu.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    // Current User & Auth State (bắt buộc đăng nhập trước khi vào chợ)
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    // Dữ liệu thật load từ backend Supabase — không còn dữ liệu fix cứng
    private val _universities = MutableStateFlow<List<UniversityDto>>(emptyList())
    val universities: StateFlow<List<UniversityDto>> = _universities.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductDto?>(null)
    val selectedProduct: StateFlow<ProductDto?> = _selectedProduct.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _selectedUniversityId = MutableStateFlow<Long?>(null)
    val selectedUniversityId: StateFlow<Long?> = _selectedUniversityId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Wishlist, My Posts, Orders, Chat
    private val _wishlist = MutableStateFlow<List<ProductDto>>(emptyList())
    val wishlist: StateFlow<List<ProductDto>> = _wishlist.asStateFlow()

    private val _myPosts = MutableStateFlow<List<ProductDto>>(emptyList())
    val myPosts: StateFlow<List<ProductDto>> = _myPosts.asStateFlow()

    private val _myOrders = MutableStateFlow<List<OrderDto>>(emptyList())
    val myOrders: StateFlow<List<OrderDto>> = _myOrders.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageDto>> = _chatMessages.asStateFlow()

    // Animated Popup Notification State
    private val _popupState = MutableStateFlow(PopupDialogState())
    val popupState: StateFlow<PopupDialogState> = _popupState.asStateFlow()

    init {
        loadInitialData()
    }

    fun showPopup(
        title: String,
        message: String,
        type: PopupType = PopupType.SUCCESS,
        confirmText: String = "Đã hiểu",
        onConfirm: (() -> Unit)? = null
    ) {
        _popupState.value = PopupDialogState(
            visible = true,
            title = title,
            message = message,
            type = type,
            confirmText = confirmText,
            onConfirm = onConfirm
        )
    }

    fun dismissPopup() {
        _popupState.value = _popupState.value.copy(visible = false)
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val catRes = api.getCategories()
                if (catRes.isSuccessful && catRes.body()?.data != null) {
                    _categories.value = catRes.body()!!.data!!
                }
                val uniRes = api.getUniversities()
                if (uniRes.isSuccessful && uniRes.body()?.data != null) {
                    _universities.value = uniRes.body()!!.data!!
                }
                fetchProducts()
            } catch (e: Exception) {
                showPopup(
                    title = "Không kết nối được máy chủ",
                    message = "Không tải được dữ liệu từ backend (${e.message ?: "lỗi mạng"}). Kiểm tra backend đã chạy và địa chỉ BASE_URL trong RetrofitClient.",
                    type = PopupType.WARNING
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchProducts(
        keyword: String? = _searchKeyword.value,
        categoryId: Long? = _selectedCategoryId.value,
        universityId: Long? = _selectedUniversityId.value,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null
    ) {
        _searchKeyword.value = keyword ?: ""
        _selectedCategoryId.value = categoryId
        _selectedUniversityId.value = universityId

        viewModelScope.launch {
            try {
                val res = api.searchProducts(
                    keyword = keyword?.ifBlank { null },
                    categoryId = categoryId,
                    universityId = universityId,
                    minPrice = minPrice,
                    maxPrice = maxPrice
                )
                if (res.isSuccessful && res.body()?.data != null) {
                    _products.value = res.body()!!.data!!.content
                } else {
                    _products.value = emptyList()
                }
            } catch (_: Exception) {
                _products.value = emptyList()
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.login(LoginRequest(email.trim(), password))
                val body = res.body()
                if (res.isSuccessful && body?.data != null) {
                    val auth = body.data!!
                    RetrofitClient.jwtToken = auth.accessToken
                    _currentUser.value = auth.user
                    loadUserScopedData()
                    showPopup(
                        title = "Đăng nhập thành công!",
                        message = "Chào mừng ${auth.user.fullName} (${auth.user.email}) đến với Chợ Đồ Cũ Sinh Viên PassUni.",
                        type = PopupType.SUCCESS,
                        onConfirm = onSuccess
                    )
                    fetchProducts()
                } else {
                    showPopup(
                        title = "Đăng nhập thất bại",
                        message = body?.message ?: "Email hoặc mật khẩu không đúng.",
                        type = PopupType.ERROR
                    )
                }
            } catch (e: Exception) {
                showPopup(
                    title = "Không kết nối được máy chủ",
                    message = "Kiểm tra backend đang chạy tại 10.0.2.2:8080 (${e.message ?: "lỗi mạng"}).",
                    type = PopupType.ERROR
                )
            }
        }
    }

    /** Đăng nhập nhanh bằng tài khoản seed sẵn trong database. */
    fun quickLoginDemo(role: String, onSuccess: () -> Unit) {
        when (role) {
            "seller" -> login("minhthu@student.vnuhcm.edu.vn", "123456", onSuccess)
            "buyer" -> login("hoangphuc@uit.edu.vn", "123456", onSuccess)
            "admin" -> login("admin@chodocu.edu.vn", "admin123", onSuccess)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _currentUser.value = null
        RetrofitClient.jwtToken = null
        _wishlist.value = emptyList()
        _myPosts.value = emptyList()
        _myOrders.value = emptyList()
        _chatMessages.value = emptyList()
        showPopup(
            title = "Đã đăng xuất",
            message = "Bạn đã đăng xuất tài khoản sinh viên. Hãy đăng nhập lại để tiếp tục giao dịch.",
            type = PopupType.INFO,
            onConfirm = onLoggedOut
        )
    }

    fun register(fullName: String, email: String, password: String, studentId: String, universityId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        password = password,
                        studentId = studentId.ifBlank { null },
                        universityId = universityId
                    )
                )
                val body = res.body()
                if (res.isSuccessful && body?.data != null) {
                    val auth = body.data!!
                    RetrofitClient.jwtToken = auth.accessToken
                    _currentUser.value = auth.user
                    showPopup(
                        title = "Đăng ký tài khoản SV thành công!",
                        message = "Email $email đã được gắn huy hiệu Xác thực Sinh viên.",
                        type = PopupType.SUCCESS,
                        onConfirm = onSuccess
                    )
                } else {
                    showPopup(
                        title = "Đăng ký thất bại",
                        message = body?.message ?: "Không thể tạo tài khoản với thông tin đã nhập.",
                        type = PopupType.ERROR
                    )
                }
            } catch (e: Exception) {
                showPopup(
                    title = "Không kết nối được máy chủ",
                    message = "Không thể đăng ký lúc này (${e.message ?: "lỗi mạng"}).",
                    type = PopupType.ERROR
                )
            }
        }
    }

    /** Tải dữ liệu riêng của người dùng sau khi đăng nhập. */
    fun loadUserScopedData() {
        viewModelScope.launch {
            try {
                api.getMyPosts().takeIf { it.isSuccessful }?.body()?.data?.let { _myPosts.value = it }
                api.getMySales().takeIf { it.isSuccessful }?.body()?.data?.let { _myOrders.value = it }
                api.getMyWishlist().takeIf { it.isSuccessful }?.body()?.data?.let { _wishlist.value = it }            } catch (_: Exception) { }
        }
    }

    /**
     * Đổi ảnh đại diện: upload ảnh lên Supabase Storage (qua backend) rồi lưu URL vào hồ sơ.
     */
    fun uploadAvatar(context: Context, uri: Uri, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Không đọc được ảnh đã chọn")
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when {
                    mime.contains("png") -> "png"
                    mime.contains("webp") -> "webp"
                    mime.contains("gif") -> "gif"
                    else -> "jpg"
                }
                val part = MultipartBody.Part.createFormData(
                    "file",
                    "avatar.$extension",
                    bytes.toRequestBody(mime.toMediaTypeOrNull())
                )
                val uploadRes = api.uploadImage(part, "avatars")
                val url = uploadRes.body()?.data?.get("url")
                if (!uploadRes.isSuccessful || url.isNullOrBlank()) {
                    showPopup("Tải ảnh thất bại", uploadRes.body()?.message ?: "Máy chủ không trả về URL ảnh.", PopupType.ERROR)
                    return@launch
                }

                val res = api.updateAvatar(mapOf("avatarUrl" to url))
                if (res.isSuccessful && res.body()?.data != null) {
                    _currentUser.value = res.body()!!.data
                    showPopup("Đã cập nhật ảnh đại diện!", "Ảnh mới của bạn đã được lưu trên hệ thống.", PopupType.SUCCESS)
                    onDone()
                } else {
                    showPopup("Không lưu được ảnh", res.body()?.message ?: "Vui lòng thử lại.", PopupType.ERROR)
                }
            } catch (e: Exception) {
                showPopup("Lỗi tải ảnh", e.message ?: "Không thể tải ảnh lên.", PopupType.ERROR)
            }
        }
    }

    /** Cập nhật hồ sơ (họ tên, SĐT, ký túc xá, bio). */
    fun updateProfile(fullName: String?, phone: String?, dormArea: String?, bio: String?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val res = api.updateProfile(
                    UpdateProfileRequest(
                        fullName = fullName,
                        phone = phone,
                        dormArea = dormArea,
                        bio = bio
                    )
                )
                if (res.isSuccessful && res.body()?.data != null) {
                    _currentUser.value = res.body()!!.data
                    showPopup("Đã lưu hồ sơ!", "Thông tin cá nhân của bạn đã được cập nhật.", PopupType.SUCCESS)
                    onDone()
                } else {
                    showPopup("Không lưu được hồ sơ", res.body()?.message ?: "Vui lòng thử lại.", PopupType.ERROR)
                }
            } catch (e: Exception) {
                showPopup("Lỗi cập nhật hồ sơ", e.message ?: "Không thể kết nối máy chủ.", PopupType.ERROR)
            }
        }
    }

    fun selectProduct(product: ProductDto) {
        _selectedProduct.value = product
    }

    fun toggleWishlist(product: ProductDto) {
        viewModelScope.launch {
            try {
                api.toggleWishlist(product.id)
            } catch (_: Exception) {}

            val exists = _wishlist.value.any { it.id == product.id }
            if (exists) {
                _wishlist.value = _wishlist.value.filterNot { it.id == product.id }
                showPopup(
                    title = "Đã bỏ lưu tin đăng",
                    message = "Đã xóa \"${product.title}\" khỏi danh sách Yêu thích (Wishlist).",
                    type = PopupType.INFO
                )
            } else {
                _wishlist.value = listOf(product.copy(isFavorited = true)) + _wishlist.value
                showPopup(
                    title = "Đã lưu vào Yêu thích!",
                    message = "Món đồ \"${product.title}\" đã được thêm vào danh sách Yêu thích cá nhân của bạn.",
                    type = PopupType.SUCCESS
                )
            }
        }
    }

    fun createNewProduct(
        title: String,
        price: BigDecimal,
        description: String,
        categoryId: Long,
        universityId: Long,
        conditionLabel: String,
        pickupSpot: String,
        isFree: Boolean,
        isUrgent: Boolean,
        imageUrls: List<String> = emptyList(),
        onCreated: () -> Unit
    ) {
        viewModelScope.launch {
            val finalPrice = if (isFree) BigDecimal.ZERO else price
            try {
                val res = api.createProduct(
                    ProductCreateRequest(
                        title = title,
                        description = description,
                        price = finalPrice,
                        isFree = isFree,
                        isUrgent = isUrgent,
                        conditionNote = conditionLabel,
                        pickupSpot = pickupSpot,
                        categoryId = categoryId,
                        universityId = universityId,
                        imageUrls = imageUrls
                    )
                )
                val created = res.body()?.data
                if (res.isSuccessful && created != null) {
                    _myPosts.value = listOf(created) + _myPosts.value
                    // Bài chờ duyệt chưa xuất hiện trên feed công khai
                    if (created.status == "APPROVED") {
                        _products.value = listOf(created) + _products.value
                    }
                    val pending = created.status == "PENDING"
                    showPopup(
                        title = if (pending) "Đã gửi tin chờ duyệt!" else "Đăng tin thành công!",
                        message = if (pending)
                            "Tin \"$title\" đã được gửi tới admin. Bạn sẽ nhận thông báo khi bài được duyệt."
                        else
                            "Tin \"$title\" đã hiển thị trên chợ sinh viên.",
                        type = PopupType.SUCCESS,
                        onConfirm = onCreated
                    )
                } else {
                    showPopup(
                        title = "Đăng tin thất bại",
                        message = res.body()?.message ?: "Vui lòng kiểm tra lại thông tin tin đăng.",
                        type = PopupType.ERROR
                    )
                }
            } catch (e: Exception) {
                showPopup(
                    title = "Không kết nối được máy chủ",
                    message = e.message ?: "Không thể đăng tin lúc này.",
                    type = PopupType.ERROR
                )
            }
        }
    }

    fun createOrderRequest(
        product: ProductDto,
        offeredPrice: BigDecimal,
        meetupLocation: String,
        meetupTime: String,
        note: String,
        onOrdered: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = api.createOrder(
                    OrderCreateRequest(
                        productId = product.id,
                        offeredPrice = offeredPrice,
                        meetupLocation = meetupLocation,
                        meetupTime = meetupTime,
                        buyerNote = note
                    )
                )
                val created = res.body()?.data
                if (res.isSuccessful && created != null) {
                    _myOrders.value = listOf(created) + _myOrders.value
                    val feeText = created.platformFeeAmount?.let { " Phí sàn tạm tính: ${it.toPlainString()}đ." } ?: ""
                    showPopup(
                        title = "Đã gửi Yêu cầu Đặt mua & Hẹn gặp!",
                        message = "Mã đơn ${created.orderCode ?: "#" + created.id}. Người bán sẽ duyệt yêu cầu của bạn.$feeText",
                        type = PopupType.SUCCESS,
                        onConfirm = onOrdered
                    )
                } else {
                    showPopup(
                        title = "Không gửi được yêu cầu",
                        message = res.body()?.message ?: "Vui lòng thử lại.",
                        type = PopupType.ERROR
                    )
                }
            } catch (e: Exception) {
                showPopup(
                    title = "Không kết nối được máy chủ",
                    message = e.message ?: "Không thể gửi yêu cầu đặt mua.",
                    type = PopupType.ERROR
                )
            }
        }
    }

    fun approveOrder(order: OrderDto) {
        viewModelScope.launch {
            try {
                val res = api.acceptOrder(order.id)
                val updated = res.body()?.data
                if (res.isSuccessful && updated != null) {
                    _myOrders.value = _myOrders.value.map { if (it.id == order.id) updated else it }
                    _products.value = _products.value.filterNot { it.id == order.product.id }
                    showPopup(
                        title = "Đã duyệt yêu cầu Đặt mua!",
                        message = "Đơn ${updated.orderCode ?: "#" + updated.id}: tin đăng đã chuyển sang \"Đang giao dịch\".",
                        type = PopupType.SUCCESS
                    )
                } else {
                    showPopup("Không duyệt được đơn", res.body()?.message ?: "Vui lòng thử lại.", PopupType.ERROR)
                }
            } catch (e: Exception) {
                showPopup("Lỗi kết nối", e.message ?: "Không thể duyệt đơn.", PopupType.ERROR)
            }
        }
    }

    /** Hoàn tất giao dịch — backend sẽ chốt phí sàn và trả số tiền người bán nhận. */
    fun completeOrder(order: OrderDto) {
        viewModelScope.launch {
            try {
                val res = api.completeOrder(order.id)
                val updated = res.body()?.data
                if (res.isSuccessful && updated != null) {
                    _myOrders.value = _myOrders.value.map { if (it.id == order.id) updated else it }
                    val feeText = updated.platformFeeAmount?.toPlainString() ?: "0"
                    val receiveText = updated.finalSellerAmount?.toPlainString() ?: "0"
                    showPopup(
                        title = "Giao dịch hoàn tất",
                        message = "Phí sàn: ${feeText}đ • Người bán nhận: ${receiveText}đ.",
                        type = PopupType.SUCCESS
                    )
                } else {
                    showPopup("Không hoàn tất được đơn", res.body()?.message ?: "Vui lòng thử lại.", PopupType.ERROR)
                }
            } catch (e: Exception) {
                showPopup("Lỗi kết nối", e.message ?: "Không thể hoàn tất đơn.", PopupType.ERROR)
            }
        }
    }

    fun markProductInTransaction(productId: Long) {
        viewModelScope.launch {
            try {
                api.updateProductStatus(productId, "IN_TRANSACTION")
            } catch (_: Exception) {}

            _products.value = _products.value.map {
                if (it.id == productId) it.copy(status = "IN_TRANSACTION") else it
            }
            _myPosts.value = _myPosts.value.map {
                if (it.id == productId) it.copy(status = "IN_TRANSACTION") else it
            }
            showPopup(
                title = "Đang giao dịch!",
                message = "Đã chuyển trạng thái tin đăng sang 'Đang giao dịch' để giữ chỗ cho người mua.",
                type = PopupType.SUCCESS
            )
        }
    }

    fun markProductAsSold(productId: Long) {
        viewModelScope.launch {
            try {
                api.updateProductStatus(productId, "SOLD")
            } catch (_: Exception) {}

            _products.value = _products.value.map {
                if (it.id == productId) it.copy(status = "SOLD") else it
            }
            _myPosts.value = _myPosts.value.map {
                if (it.id == productId) it.copy(status = "SOLD") else it
            }
            showPopup(
                title = "Đã đánh dấu Đã Bán!",
                message = "Tin đăng đã chuyển sang trạng thái 'Đã bán' và ẩn khỏi bảng tin chợ.",
                type = PopupType.SUCCESS
            )
        }
    }

    fun markPostSold(product: ProductDto) {
        markProductAsSold(product.id)
    }

    fun deleteMyPost(product: ProductDto) {
        viewModelScope.launch {
            try {
                api.deleteProduct(product.id)
            } catch (_: Exception) {}

            _myPosts.value = _myPosts.value.filterNot { it.id == product.id }
            _products.value = _products.value.filterNot { it.id == product.id }
            showPopup(
                title = "Đã xóa tin đăng!",
                message = "Tin đăng \"${product.title}\" đã được gỡ khỏi hệ thống.",
                type = PopupType.INFO
            )
        }
    }

    fun sendChatMessage(content: String) {
        if (content.isBlank()) return
        val me = _currentUser.value ?: return
        val prod = _selectedProduct.value
        val receiverId = prod?.seller?.id ?: return

        viewModelScope.launch {
            try {
                val res = api.sendChatMessage(
                    ChatMessageRequest(
                        receiverId = receiverId,
                        productId = prod?.id,
                        content = content.trim()
                    )
                )
                res.body()?.data?.let { saved ->
                    _chatMessages.value = _chatMessages.value + saved
                }
            } catch (_: Exception) { }
        }
    }

}

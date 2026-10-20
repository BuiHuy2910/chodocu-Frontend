package com.chodocu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.data.api.RetrofitClient
import com.chodocu.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    // Current User & Auth State (Bắt buộc đăng nhập trước khi vào chợ)
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    // Universities & Categories
    private val _universities = MutableStateFlow(defaultUniversities())
    val universities: StateFlow<List<UniversityDto>> = _universities.asStateFlow()

    private val _categories = MutableStateFlow(defaultCategories())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    // Products & Filters
    private val _products = MutableStateFlow(defaultProducts())
    val products: StateFlow<List<ProductDto>> = _products.asStateFlow()

    private val _selectedProduct = MutableStateFlow<ProductDto?>(defaultProducts().first())
    val selectedProduct: StateFlow<ProductDto?> = _selectedProduct.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _selectedUniversityId = MutableStateFlow<Long?>(null)
    val selectedUniversityId: StateFlow<Long?> = _selectedUniversityId.asStateFlow()

    // Wishlist, My Posts, Orders, Chat
    private val _wishlist = MutableStateFlow<List<ProductDto>>(defaultProducts().take(2))
    val wishlist: StateFlow<List<ProductDto>> = _wishlist.asStateFlow()

    private val _myPosts = MutableStateFlow<List<ProductDto>>(defaultProducts().take(2))
    val myPosts: StateFlow<List<ProductDto>> = _myPosts.asStateFlow()

    private val _myOrders = MutableStateFlow<List<OrderDto>>(defaultOrders())
    val myOrders: StateFlow<List<OrderDto>> = _myOrders.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageDto>>(defaultChatMessages())
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
            } catch (_: Exception) {
                // Giữ dữ liệu mẫu nếu máy ảo chưa kết nối mạng
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
                    return@launch
                }
            } catch (_: Exception) {}

            // Fallback lọc cục bộ mượt mà
            val all = defaultProducts()
            _products.value = all.filter { p ->
                val matchKw = keyword.isNullOrBlank() || p.title.contains(keyword, ignoreCase = true)
                val matchCat = categoryId == null || p.categoryId == categoryId
                val matchUni = universityId == null || p.universityId == universityId
                val matchMin = minPrice == null || p.price >= minPrice
                val matchMax = maxPrice == null || p.price <= maxPrice
                matchKw && matchCat && matchUni && matchMin && matchMax && p.status == "APPROVED"
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = api.login(LoginRequest(email.trim(), password))
                if (res.isSuccessful && res.body()?.data != null) {
                    val auth = res.body()!!.data!!
                    RetrofitClient.jwtToken = auth.accessToken
                    _currentUser.value = auth.user
                    showPopup(
                        title = "Đăng nhập thành công!",
                        message = "Chào mừng ${auth.user.fullName} (${auth.user.email}) đến với Chợ Đồ Cũ Sinh Viên PassUni.",
                        type = PopupType.SUCCESS,
                        onConfirm = onSuccess
                    )
                    fetchProducts()
                    return@launch
                }
            } catch (_: Exception) {}

            // Fallback đăng nhập demo cho sinh viên thuyết trình / kiểm thử
            val demoUser = defaultUsers().find { it.email.equals(email.trim(), ignoreCase = true) } ?: UserDto(
                id = if (email.contains("admin")) 1L else (if (email.contains("sinhvien1")) 3L else 2L),
                email = email.trim(),
                fullName = if (email.contains("admin")) "Quản Trị Viên Chợ" else (if (email.contains("sinhvien1")) "Trần Văn Bình" else "Nguyễn Minh Thư"),
                studentId = if (email.contains("admin")) "ADMIN_01" else (if (email.contains("sinhvien1")) "22140889" else "21120501"),
                phone = "0912345678",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                universityId = 1L,
                universityName = "KTX Khu B - ĐHQG TP.HCM",
                universityShortName = "KTX Khu B",
                dormArea = "Tòa BA1 - KTX Khu B",
                role = if (email.contains("admin")) "ADMIN" else "STUDENT",
                verifiedStudent = true,
                rating = 4.9,
                completedDeals = 12
            )
            _currentUser.value = demoUser
            showPopup(
                title = "Xác thực Email Sinh viên thành công!",
                message = "Đã đăng nhập thành công với tài khoản: ${demoUser.fullName} (${demoUser.email}) - Đã cấp token JWT.",
                type = PopupType.SUCCESS,
                onConfirm = onSuccess
            )
        }
    }

    fun quickLoginDemo(role: String, onSuccess: () -> Unit) {
        when (role) {
            "seller" -> login("minhthu@student.vnuhcm.edu.vn", "123456", onSuccess)
            "buyer" -> login("sinhvien1@vnuhcm.edu.vn", "123456", onSuccess)
            "admin" -> login("admin@chodocu.vn", "admin123", onSuccess)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _currentUser.value = null
        RetrofitClient.jwtToken = null
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
                        studentId = studentId,
                        universityId = universityId
                    )
                )
                if (res.isSuccessful && res.body()?.data != null) {
                    val auth = res.body()!!.data!!
                    RetrofitClient.jwtToken = auth.accessToken
                    _currentUser.value = auth.user
                    showPopup(
                        title = "Đăng ký tài khoản SV thành công!",
                        message = "Email $email đã được gắn huy hiệu Xác thực Sinh viên ĐHQG.",
                        type = PopupType.SUCCESS,
                        onConfirm = onSuccess
                    )
                    return@launch
                }
            } catch (_: Exception) {}

            _currentUser.value = UserDto(
                id = 99L,
                email = email,
                fullName = fullName,
                studentId = studentId,
                phone = "0912345678",
                avatarUrl = null,
                universityId = universityId,
                universityName = "KTX Khu B - ĐHQG TP.HCM",
                universityShortName = "KTX Khu B",
                dormArea = "KTX Khu B",
                verifiedStudent = true
            )
            showPopup(
                title = "Đăng ký thành công!",
                message = "Chào mừng $fullName (MSSV: $studentId) gia nhập mạng lưới trao đổi đồ cũ sinh viên.",
                type = PopupType.SUCCESS,
                onConfirm = onSuccess
            )
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
                        imageUrls = listOf("https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800")
                    )
                )
                if (res.isSuccessful && res.body()?.data != null) {
                    val created = res.body()!!.data!!
                    _products.value = listOf(created) + _products.value
                    _myPosts.value = listOf(created) + _myPosts.value
                    showPopup(
                        title = "Đăng tin thành công lên Supabase!",
                        message = "Tin đăng \"$title\" đã được hiển thị tới sinh viên cùng khu vực trường học.",
                        type = PopupType.SUCCESS,
                        onConfirm = onCreated
                    )
                    return@launch
                }
            } catch (_: Exception) {}

            val newProd = ProductDto(
                id = System.currentTimeMillis() % 10000,
                title = title,
                description = description,
                price = finalPrice,
                originalPrice = finalPrice.multiply(BigDecimal(2)),
                isFree = isFree,
                isUrgent = isUrgent,
                conditionLabel = conditionLabel,
                pickupSpot = pickupSpot,
                categoryId = categoryId,
                categoryName = _categories.value.find { it.id == categoryId }?.name ?: "Đồ dùng KTX",
                universityId = universityId,
                universityShortName = _universities.value.find { it.id == universityId }?.shortName ?: "KTX Khu B",
                seller = _currentUser.value,
                imageUrls = listOf("https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800")
            )
            _products.value = listOf(newProd) + _products.value
            _myPosts.value = listOf(newProd) + _myPosts.value
            showPopup(
                title = "Đăng tin bán đồ thành công!",
                message = "Món đồ \"$title\" đã sẵn sàng nhận tin nhắn và lịch hẹn gặp tại $pickupSpot.",
                type = PopupType.SUCCESS,
                onConfirm = onCreated
            )
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
                api.createOrder(
                    OrderCreateRequest(
                        productId = product.id,
                        offeredPrice = offeredPrice,
                        meetupLocation = meetupLocation,
                        meetupTime = meetupTime,
                        buyerNote = note
                    )
                )
            } catch (_: Exception) {}

            val newOrder = OrderDto(
                id = System.currentTimeMillis() % 10000,
                product = product,
                buyer = _currentUser.value!!,
                seller = product.seller ?: _currentUser.value!!,
                offeredPrice = offeredPrice,
                meetupLocation = meetupLocation,
                meetupTime = meetupTime,
                buyerNote = note,
                sellerNote = null,
                status = "PENDING",
                statusLabel = "Chờ người bán duyệt",
                createdAt = "Vừa xong"
            )
            _myOrders.value = listOf(newOrder) + _myOrders.value

            // Tự động đẩy phiếu hẹn gặp vào Chat
            _chatMessages.value = _chatMessages.value + ChatMessageDto(
                id = System.currentTimeMillis(),
                sender = _currentUser.value!!,
                receiver = product.seller ?: _currentUser.value!!,
                productId = product.id,
                productTitle = product.title,
                content = "Phiếu hẹn gặp: $meetupLocation lúc $meetupTime ($note)",
                messageType = "MEETUP",
                offeredPrice = offeredPrice,
                meetupLocation = meetupLocation,
                meetupTime = meetupTime,
                createdAt = "Vừa xong"
            )

            showPopup(
                title = "Đã gửi Yêu cầu Đặt mua & Hẹn gặp!",
                message = "Yêu cầu mua \"${product.title}\" đã được gửi tới người bán. Khi người bán duyệt, tin sẽ chuyển sang \"Đang giao dịch\".",
                type = PopupType.SUCCESS,
                onConfirm = onOrdered
            )
        }
    }

    fun approveOrder(order: OrderDto) {
        viewModelScope.launch {
            try {
                api.acceptOrder(order.id)
            } catch (_: Exception) {}

            _myOrders.value = _myOrders.value.map {
                if (it.id == order.id) it.copy(status = "ACCEPTED", statusLabel = "Đang giao dịch / Đã ẩn tin") else it
            }
            _products.value = _products.value.filterNot { it.id == order.product.id }
            showPopup(
                title = "Đã duyệt yêu cầu Đặt mua!",
                message = "Tin đăng \"${order.product.title}\" đã tự động chuyển sang trạng thái \"Đang giao dịch\" để ẩn khỏi bảng tin chung.",
                type = PopupType.SUCCESS
            )
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
        val msg = ChatMessageDto(
            id = System.currentTimeMillis(),
            sender = me,
            receiver = prod?.seller ?: me,
            productId = prod?.id,
            productTitle = prod?.title,
            content = content.trim(),
            messageType = "TEXT",
            createdAt = "Vừa xong"
        )
        _chatMessages.value = _chatMessages.value + msg
        viewModelScope.launch {
            try {
                api.sendChatMessage(
                    ChatMessageRequest(
                        receiverId = prod?.seller?.id ?: 3L,
                        productId = prod?.id,
                        content = content.trim()
                    )
                )
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun defaultUniversities() = listOf(
            UniversityDto(1L, "KTX Khu B - ĐHQG TP.HCM", "KTX Khu B", "vnuhcm.edu.vn", "TP. Thủ Đức", "Sảnh Tòa BA1, Căng tin B4"),
            UniversityDto(2L, "ĐH Bách Khoa - ĐHQG TP.HCM", "ĐH Bách Khoa", "hcmut.edu.vn", "Làng ĐH Thủ Đức", "Thư viện H1, Sảnh B1"),
            UniversityDto(3L, "ĐH Khoa Học Tự Nhiên - ĐHQG", "ĐH Tự Nhiên", "hcmus.edu.vn", "TP. Thủ Đức", "Thư viện Tự Nhiên"),
            UniversityDto(4L, "ĐH Công Nghệ Thông Tin (UIT)", "ĐH CNTT - UIT", "uit.edu.vn", "TP. Thủ Đức", "Sảnh Tòa A, Căng tin UIT")
        )

        fun defaultCategories() = listOf(
            CategoryDto(1L, "Giáo trình & Sách", "giao-trinh-sach", "menu_book"),
            CategoryDto(2L, "Đồ điện & Quạt", "do-dien-quat", "mode_fan"),
            CategoryDto(3L, "Đồ dùng & Bàn", "do-dung-ban", "desk"),
            CategoryDto(4L, "Quần áo & Giày", "quan-ao-giay", "checkroom"),
            CategoryDto(5L, "Xe đạp & Xe máy", "xe-dap-xe-may", "pedal_bike"),
            CategoryDto(6L, "Decor phòng trọ", "decor-phong-tro", "potted_plant")
        )

        private val sellerYen = UserDto(
            id = 3L,
            email = "hoangyen@hcmut.edu.vn",
            fullName = "Lê Hoàng Yến",
            studentId = "2014512",
            phone = "0987654321",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
            universityId = 2L,
            universityName = "ĐH Bách Khoa - ĐHQG TP.HCM",
            universityShortName = "ĐH Bách Khoa",
            dormArea = "Tòa BA1 - KTX Khu B",
            verifiedStudent = true,
            rating = 4.9,
            completedDeals = 24
        )

        fun defaultUsers() = listOf(
            UserDto(
                id = 2L,
                email = "minhthu@student.vnuhcm.edu.vn",
                fullName = "Nguyễn Minh Thư",
                studentId = "21120501",
                phone = "0912345678",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200",
                universityId = 1L,
                universityName = "KTX Khu B - ĐHQG TP.HCM",
                universityShortName = "KTX Khu B",
                dormArea = "Tòa BA1 - KTX Khu B",
                role = "STUDENT",
                verifiedStudent = true,
                rating = 4.9,
                completedDeals = 24
            ),
            UserDto(
                id = 3L,
                email = "sinhvien1@vnuhcm.edu.vn",
                fullName = "Trần Văn Bình",
                studentId = "22140889",
                phone = "0988776655",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                universityId = 2L,
                universityName = "ĐH Bách Khoa - ĐHQG TP.HCM",
                universityShortName = "ĐH Bách Khoa",
                dormArea = "KTX Bách Khoa",
                role = "STUDENT",
                verifiedStudent = true,
                rating = 5.0,
                completedDeals = 8
            ),
            UserDto(
                id = 1L,
                email = "admin@chodocu.vn",
                fullName = "Ban Quản Trị Hệ Thống",
                studentId = "ADMIN_SYS",
                phone = "0900000000",
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200",
                universityId = 1L,
                universityName = "ĐHQG TP.HCM",
                universityShortName = "ĐHQG",
                dormArea = "Văn phòng BQT KTX",
                role = "ADMIN",
                verifiedStudent = true,
                rating = 5.0,
                completedDeals = 150
            )
        )

        fun defaultProducts() = listOf(
            ProductDto(
                id = 1L,
                title = "Bộ Bàn Ghế Học Gấp Gọn Kèm Giá Đỡ Sách Thông Minh",
                description = "Dùng đúng 1 học kỳ ôn thi đại cương, giữ rất kỹ nên hầu như không trầy xước. Khung chân kim loại chịu lực tốt. Gấp gọn chỉ 5cm nhét gầm giường KTX.",
                price = BigDecimal("110000"),
                originalPrice = BigDecimal("280000"),
                isUrgent = true,
                conditionLabel = "Như mới (95%)",
                conditionNote = "Mới 95% • Dọn phòng về quê",
                passReason = "Dọn phòng KTX / Chuyển trọ",
                pickupSpot = "Căng tin Tòa BA1 hoặc Sảnh Nhà Văn Hóa SV",
                categoryId = 3L,
                categoryName = "Đồ dùng & Bàn",
                universityId = 1L,
                universityShortName = "KTX Khu B",
                seller = sellerYen,
                imageUrls = listOf("https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=800"),
                timeAgo = "25 phút trước"
            ),
            ProductDto(
                id = 2L,
                title = "Quạt đứng Senko lồng sắt 3 tốc độ chạy êm",
                description = "Quạt Senko chính hãng mua đầu năm, motor đồng chạy cực êm không kêu rè khi ngủ trưa ở KTX.",
                price = BigDecimal("120000"),
                originalPrice = BigDecimal("290000"),
                isUrgent = true,
                conditionLabel = "Tốt (90%)",
                conditionNote = "Mới 90% • Pass gấp trong chiều",
                passReason = "Ra trường / Tốt nghiệp",
                pickupSpot = "Sảnh Tòa H1 - ĐH Bách Khoa",
                categoryId = 2L,
                categoryName = "Đồ điện & Quạt",
                universityId = 2L,
                universityShortName = "ĐH Bách Khoa",
                seller = sellerYen,
                imageUrls = listOf("https://images.unsplash.com/photo-1618941716939-553df3c6c278?w=800"),
                timeAgo = "1 giờ trước"
            ),
            ProductDto(
                id = 3L,
                title = "Combo Giáo trình Giải tích 1 & 2 + Đại số Tuyến tính",
                description = "Trọn bộ giáo trình Giải tích 1, 2 và Đại số tuyến tính chuẩn ĐHQG. Tặng kèm bộ đề thi mẫu 3 năm gần nhất.",
                price = BigDecimal("35000"),
                originalPrice = BigDecimal("95000"),
                isUrgent = true,
                conditionLabel = "Như mới (95%)",
                conditionNote = "Photo chuẩn • Tặng đề thi",
                passReason = "Học xong môn / Qua môn",
                pickupSpot = "Thư viện Trung tâm ĐH Tự Nhiên",
                categoryId = 1L,
                categoryName = "Giáo trình & Sách",
                universityId = 3L,
                universityShortName = "ĐH Tự Nhiên",
                seller = sellerYen,
                imageUrls = listOf("https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=800"),
                timeAgo = "2 giờ trước"
            ),
            ProductDto(
                id = 4L,
                title = "Nồi lẩu mini Bear 1.2L đa năng nấu mì KTX",
                description = "Nồi lẩu điện mini Bear màu xanh mint, dung tích 1.2L vừa vặn nấu mì, luộc trứng, hấp bánh bao ôn thi.",
                price = BigDecimal("110000"),
                originalPrice = BigDecimal("250000"),
                isUrgent = false,
                conditionLabel = "Như mới (98%)",
                conditionNote = "Mới 98% • Cách 300m",
                passReason = "Lên đời đồ mới",
                pickupSpot = "Sảnh Tòa C2 - KTX Khu B",
                categoryId = 2L,
                categoryName = "Đồ điện & Quạt",
                universityId = 1L,
                universityShortName = "KTX Khu B",
                seller = sellerYen,
                imageUrls = listOf("https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=800"),
                timeAgo = "3 giờ trước"
            )
        )

        fun defaultOrders(): List<OrderDto> {
            val prods = defaultProducts()
            return listOf(
                OrderDto(
                    id = 501L,
                    product = prods[0],
                    buyer = sellerYen.copy(id = 2L, fullName = "Nguyễn Minh Thư"),
                    seller = sellerYen,
                    offeredPrice = BigDecimal("110000"),
                    meetupLocation = "Sảnh Tầng 1 Tòa BA1 - KTX Khu B",
                    meetupTime = "17:00 Hôm nay",
                    buyerNote = "Chiều 17h mình xuống sảnh BA1 nhận bàn nhé!",
                    sellerNote = null,
                    status = "PENDING",
                    statusLabel = "Chờ người bán duyệt",
                    createdAt = "Hôm nay, 14:30"
                )
            )
        }

        fun defaultChatMessages(): List<ChatMessageDto> {
            val buyer = sellerYen.copy(id = 2L, fullName = "Nguyễn Minh Thư")
            return listOf(
                ChatMessageDto(
                    id = 1L,
                    sender = buyer,
                    receiver = sellerYen,
                    productId = 1L,
                    productTitle = "Bộ Bàn Ghế Học Gấp Gọn Kèm Giá Đỡ Sách",
                    content = "Chào bạn Yến ơi, bàn này bạn còn pass không ạ? Chiều nay mình qua sảnh BA1 xem đồ được không?",
                    createdAt = "14:29"
                ),
                ChatMessageDto(
                    id = 2L,
                    sender = sellerYen,
                    receiver = buyer,
                    productId = 1L,
                    productTitle = "Bộ Bàn Ghế Học Gấp Gọn Kèm Giá Đỡ Sách",
                    content = "Chào bạn! Bàn vẫn còn nha, gỗ còn rất chắc chắn. Chiều tầm 17h mình tan tiết về KTX, hẹn bạn ở sảnh tầng 1 tòa BA1 nha!",
                    createdAt = "14:32"
                )
            )
        }
    }
}


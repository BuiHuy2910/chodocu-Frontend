package com.chodocu.app.ui.post

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chodocu.app.core.Validators
import com.chodocu.app.core.safeCall
import com.chodocu.app.data.common.toUserMessage
import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductCondition
import com.chodocu.app.data.model.ProductInput
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.University
import com.chodocu.app.data.model.UploadFolder
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.ui.navigation.NavArgs
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Một ô ảnh trong form: ảnh vừa chọn từ máy (đang / lỗi tải lên) hoặc ảnh đã có URL. */
data class EditorImage(
    /** Khóa ổn định để danh sách ảnh không bị nháy khi thêm / xóa. */
    val id: Long,
    /** Ảnh chọn từ máy: dùng để xem trước và tải lại khi lỗi. */
    val uri: Uri? = null,
    /** URL công khai sau khi tải lên xong. */
    val url: String? = null,
    val isUploading: Boolean = false,
    val failed: Boolean = false,
)

/** Lỗi của từng mục trong form (null = hợp lệ). */
data class PostEditorErrors(
    val images: String? = null,
    val title: String? = null,
    val category: String? = null,
    val condition: String? = null,
    val price: String? = null,
    val originalPrice: String? = null,
    val university: String? = null,
    val pickupSpot: String? = null,
) {
    val hasAny: Boolean
        get() = listOf(images, title, category, condition, price, originalPrice, university, pickupSpot)
            .any { it != null }
}

/**
 * Trạng thái form đăng / sửa tin.
 * Danh mục, tình trạng, trường và điểm giao dịch để trống với tin mới — người đăng phải tự chọn.
 */
data class PostEditorUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    /** Khác null = tin không sửa được nữa (đang giao dịch / đã bán). */
    val cannotEditReason: String? = null,
    /** Lý do bị từ chối lần trước (khi sửa tin bị từ chối). */
    val rejectReason: String? = null,
    val categories: List<Category> = emptyList(),
    val universities: List<University> = emptyList(),
    val images: List<EditorImage> = emptyList(),
    val title: String = "",
    val category: Category? = null,
    val condition: ProductCondition? = null,
    val conditionNote: String = "",
    val isFree: Boolean = false,
    /** Giá bán và giá gốc chỉ gồm chữ số. */
    val price: String = "",
    val originalPrice: String = "",
    val isUrgent: Boolean = false,
    val description: String = "",
    val passReason: String = "",
    val university: University? = null,
    val pickupSpot: String? = null,
    val errors: PostEditorErrors = PostEditorErrors(),
    val isSubmitting: Boolean = false,
    /** Người dùng đã sửa gì đó chưa lưu (hỏi lại trước khi thoát). */
    val isDirty: Boolean = false,
    val userMessage: String? = null,
    /** Lưu thành công: màn hình chuyển đi. */
    val savedProductId: Long? = null,
) {
    val isUploadingImages: Boolean get() = images.any { it.isUploading }
    val remainingImageSlots: Int get() = (Validators.MAX_IMAGES - images.size).coerceAtLeast(0)
    val meetupSpots: List<String> get() = university?.meetupSpots.orEmpty()

    /** Phần trăm giảm so với giá gốc để gợi ý cho người đăng. */
    val discountPercent: Int?
        get() {
            val sell = price.toLongOrNull() ?: return null
            val original = originalPrice.toLongOrNull() ?: return null
            if (isFree || original <= sell || original <= 0L) return null
            return (((original - sell) * 100) / original).toInt().takeIf { it > 0 }
        }
}

/** Đăng tin mới hoặc sửa tin cũ (khi có productId trên đường dẫn). */
class PostEditorViewModel(
    private val products: ProductRepository,
    private val catalog: CatalogRepository,
    private val uploads: UploadRepository,
    handle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = handle[NavArgs.PRODUCT_ID] ?: NavArgs.NO_ID
    private val isEditing = productId != NavArgs.NO_ID

    private val _uiState = MutableStateFlow(PostEditorUiState(isEditing = isEditing))
    val uiState: StateFlow<PostEditorUiState> = _uiState.asStateFlow()

    private var nextImageId = 0L

    init {
        load()
    }

    /** Tải danh mục, danh sách trường và (khi sửa) nội dung tin cũ. */
    fun load() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val categories = async { safeCall { catalog.getCategories() } }
            val universities = async { safeCall { catalog.getUniversities() } }
            val product = if (isEditing) safeCall { products.getProduct(productId) } else null

            val categoryResult = categories.await()
            val universityResult = universities.await()
            val error = categoryResult.exceptionOrNull()
                ?: universityResult.exceptionOrNull()
                ?: product?.exceptionOrNull()
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, loadError = error.toUserMessage()) }
                return@launch
            }

            val categoryList = categoryResult.getOrDefault(emptyList())
            val universityList = universityResult.getOrDefault(emptyList())
            _uiState.update { it.copy(isLoading = false, categories = categoryList, universities = universityList) }
            product?.getOrNull()?.let(::fillFrom)
        }
    }

    /** Điền form từ tin đang sửa. */
    private fun fillFrom(product: Product) {
        _uiState.update { state ->
            val university = state.universities.firstOrNull { it.id == product.universityId }
            state.copy(
                cannotEditReason = when (product.status) {
                    ProductStatus.IN_TRANSACTION -> "Tin đang trong giao dịch nên tạm thời không sửa được."
                    ProductStatus.SOLD -> "Tin đã bán nên không sửa được nữa."
                    else -> null
                },
                rejectReason = product.rejectReason.takeIf { product.status == ProductStatus.REJECTED },
                images = product.imageUrls.map { EditorImage(id = nextImageId++, url = it) },
                title = product.title,
                category = state.categories.firstOrNull { it.id == product.categoryId },
                condition = product.condition,
                conditionNote = product.conditionNote.orEmpty(),
                isFree = product.isFree,
                price = if (product.isFree || product.price <= 0L) "" else product.price.toString(),
                originalPrice = product.originalPrice?.takeIf { it > 0L }?.toString().orEmpty(),
                isUrgent = product.isUrgent,
                description = product.description,
                passReason = product.passReason.orEmpty(),
                university = university,
                // Điểm cũ không còn trong danh sách chuẩn thì để trống cho người dùng chọn lại
                pickupSpot = product.pickupSpot?.takeIf { spot -> university?.meetupSpots?.contains(spot) == true },
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Ảnh: chọn xong là tải lên ngay, mỗi ảnh một luồng
    // ---------------------------------------------------------------------------------------------

    fun onImagesPicked(uris: List<Uri>) {
        val accepted = uris.take(_uiState.value.remainingImageSlots)
        if (accepted.isEmpty()) return
        val newImages = accepted.map { EditorImage(id = nextImageId++, uri = it, isUploading = true) }
        edit { it.copy(images = it.images + newImages, errors = it.errors.copy(images = null)) }
        newImages.forEach(::upload)
    }

    fun retryImage(imageId: Long) {
        val image = _uiState.value.images.firstOrNull { it.id == imageId } ?: return
        updateImage(imageId) { it.copy(isUploading = true, failed = false) }
        upload(image)
    }

    fun removeImage(imageId: Long) = edit { state ->
        state.copy(images = state.images.filterNot { it.id == imageId })
    }

    /** Đưa ảnh lên đầu danh sách để làm ảnh bìa. */
    fun makeCover(imageId: Long) = edit { state ->
        val image = state.images.firstOrNull { it.id == imageId } ?: return@edit state
        state.copy(images = listOf(image) + state.images.filterNot { it.id == imageId })
    }

    private fun upload(image: EditorImage) {
        val uri = image.uri ?: return
        viewModelScope.launch {
            safeCall { uploads.uploadImage(uri, UploadFolder.PRODUCTS) }
                .onSuccess { url -> updateImage(image.id) { it.copy(url = url, isUploading = false, failed = false) } }
                .onFailure { error ->
                    updateImage(image.id) { it.copy(isUploading = false, failed = true) }
                    _uiState.update { it.copy(userMessage = error.toUserMessage()) }
                }
        }
    }

    /** Cập nhật một ảnh theo id (ảnh đã bị xóa trong lúc tải thì bỏ qua). */
    private fun updateImage(imageId: Long, change: (EditorImage) -> EditorImage) {
        _uiState.update { state -> state.copy(images = state.images.map { if (it.id == imageId) change(it) else it }) }
    }

    // ---------------------------------------------------------------------------------------------
    // Các ô nhập
    // ---------------------------------------------------------------------------------------------

    fun onTitleChange(value: String) = edit { it.copy(title = value, errors = it.errors.copy(title = null)) }

    fun onCategorySelected(category: Category) =
        edit { it.copy(category = category, errors = it.errors.copy(category = null)) }

    fun onConditionSelected(condition: ProductCondition) =
        edit { it.copy(condition = condition, errors = it.errors.copy(condition = null)) }

    fun onConditionNoteChange(value: String) = edit { it.copy(conditionNote = value) }

    fun onFreeChange(isFree: Boolean) =
        edit { it.copy(isFree = isFree, errors = it.errors.copy(price = null, originalPrice = null)) }

    fun onPriceChange(digits: String) =
        edit { it.copy(price = digits, errors = it.errors.copy(price = null, originalPrice = null)) }

    fun onOriginalPriceChange(digits: String) =
        edit { it.copy(originalPrice = digits, errors = it.errors.copy(originalPrice = null)) }

    fun onUrgentChange(isUrgent: Boolean) = edit { it.copy(isUrgent = isUrgent) }

    fun onDescriptionChange(value: String) = edit { it.copy(description = value) }

    fun onPassReasonChange(value: String) = edit { it.copy(passReason = value) }

    /** Đổi trường: điểm giao dịch cũ không thuộc trường mới thì bị bỏ chọn. */
    fun onUniversitySelected(university: University) = edit { state ->
        val spot = state.pickupSpot?.takeIf { it in university.meetupSpots }
        state.copy(
            university = university,
            pickupSpot = spot,
            errors = state.errors.copy(university = null),
        )
    }

    fun onPickupSpotSelected(spot: String) = edit { it.copy(pickupSpot = spot, errors = it.errors.copy(pickupSpot = null)) }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun edit(change: (PostEditorUiState) -> PostEditorUiState) {
        _uiState.update { change(it).copy(isDirty = true) }
    }

    // ---------------------------------------------------------------------------------------------
    // Gửi tin
    // ---------------------------------------------------------------------------------------------

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.cannotEditReason != null) return
        if (state.isUploadingImages) {
            _uiState.update { it.copy(userMessage = "Vui lòng chờ ảnh tải lên xong.") }
            return
        }

        val price = state.price.toLongOrNull()
        val originalPrice = state.originalPrice.toLongOrNull()?.takeIf { it > 0L }
        val errors = validate(state, price, originalPrice)
        val category = state.category
        val condition = state.condition
        val university = state.university
        val pickupSpot = state.pickupSpot
        if (errors.hasAny || category == null || condition == null || university == null || pickupSpot == null) {
            _uiState.update { it.copy(errors = errors, userMessage = "Vui lòng kiểm tra lại các mục được đánh dấu đỏ.") }
            return
        }

        val input = ProductInput(
            title = state.title.trim(),
            description = state.description.trim(),
            price = if (state.isFree) 0L else price ?: 0L,
            originalPrice = originalPrice,
            isFree = state.isFree,
            isUrgent = state.isUrgent,
            condition = condition,
            conditionNote = state.conditionNote.trim().ifEmpty { null },
            passReason = state.passReason.trim().ifEmpty { null },
            categoryId = category.id,
            universityId = university.id,
            pickupSpot = pickupSpot,
            imageUrls = state.images.mapNotNull { it.url },
        )
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            safeCall { if (isEditing) products.updateProduct(productId, input) else products.createProduct(input) }
                .onSuccess { product ->
                    _uiState.update { it.copy(isSubmitting = false, isDirty = false, savedProductId = product.id) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, userMessage = error.toUserMessage()) }
                }
        }
    }

    private fun validate(state: PostEditorUiState, price: Long?, originalPrice: Long?) = PostEditorErrors(
        images = when {
            state.images.any { it.failed } -> "Có ảnh tải lên thất bại. Hãy thử lại hoặc xóa ảnh đó."
            state.images.none { it.url != null } -> "Thêm ít nhất 1 ảnh thật của món đồ."
            else -> null
        },
        title = Validators.productTitle(state.title),
        category = if (state.category == null) "Vui lòng chọn danh mục" else null,
        condition = if (state.condition == null) "Vui lòng chọn tình trạng món đồ" else null,
        price = Validators.price(price, state.isFree),
        originalPrice = if (!state.isFree && originalPrice != null && price != null && originalPrice < price) {
            "Giá gốc phải lớn hơn hoặc bằng giá bán"
        } else {
            null
        },
        university = if (state.university == null) "Vui lòng chọn trường" else null,
        pickupSpot = if (state.pickupSpot == null) "Vui lòng chọn điểm giao dịch" else null,
    )
}

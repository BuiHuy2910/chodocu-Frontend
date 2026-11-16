package com.chodocu.app.ui.post

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.ProductCondition
import com.chodocu.app.data.model.University
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.ConfirmDialog
import com.chodocu.app.ui.components.EmptyView
import com.chodocu.app.ui.components.ErrorView
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.LoadingView
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.components.rememberMultipleImagePicker
import com.chodocu.app.ui.theme.Space

/** Màn đăng tin mới / sửa tin đăng. */
@Composable
fun PostEditorScreen(onClose: () -> Unit, onSubmitted: (productId: Long) -> Unit) {
    val viewModel = appViewModel { handle -> PostEditorViewModel(products, catalog, uploads, handle) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Lưu xong thì chuyển màn (đăng mới -> "Tin của tôi", sửa -> quay lại)
    LaunchedEffect(state.savedProductId) { state.savedProductId?.let(onSubmitted) }

    val pickImages = rememberMultipleImagePicker(maxItems = state.remainingImageSlots, onPicked = viewModel::onImagesPicked)
    PostEditorContent(
        state = state,
        onClose = onClose,
        onRetry = viewModel::load,
        onSubmit = viewModel::submit,
        onUserMessageShown = viewModel::userMessageShown,
        actions = PostEditorActions(
            onAddImages = pickImages,
            onRemoveImage = viewModel::removeImage,
            onRetryImage = viewModel::retryImage,
            onMakeCover = viewModel::makeCover,
            onTitleChange = viewModel::onTitleChange,
            onConditionNoteChange = viewModel::onConditionNoteChange,
            onFreeChange = viewModel::onFreeChange,
            onPriceChange = viewModel::onPriceChange,
            onOriginalPriceChange = viewModel::onOriginalPriceChange,
            onUrgentChange = viewModel::onUrgentChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onPassReasonChange = viewModel::onPassReasonChange,
        ),
        sheetActions = SheetActions(
            onCategorySelected = viewModel::onCategorySelected,
            onConditionSelected = viewModel::onConditionSelected,
            onUniversitySelected = viewModel::onUniversitySelected,
            onPickupSpotSelected = viewModel::onPickupSpotSelected,
        ),
    )
}

/** Bảng chọn đang mở (mỗi lúc chỉ một bảng). */
internal enum class EditorSheet { CATEGORY, CONDITION, UNIVERSITY, PICKUP_SPOT }

/** Các hành động chọn giá trị từ bảng chọn. */
internal data class SheetActions(
    val onCategorySelected: (Category) -> Unit,
    val onConditionSelected: (ProductCondition) -> Unit,
    val onUniversitySelected: (University) -> Unit,
    val onPickupSpotSelected: (String) -> Unit,
)

@Composable
private fun PostEditorContent(
    state: PostEditorUiState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onSubmit: () -> Unit,
    onUserMessageShown: () -> Unit,
    actions: PostEditorActions,
    sheetActions: SheetActions,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var openSheet by remember { mutableStateOf<EditorSheet?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onUserMessageShown()
        }
    }

    // Đang soạn dở mà thoát: hỏi lại để tránh mất nội dung
    val requestClose = { if (state.isDirty && !state.isSubmitting) showDiscardDialog = true else onClose() }
    BackHandler(enabled = state.isDirty && !state.isSubmitting) { showDiscardDialog = true }

    val isFormVisible = !state.isLoading && state.loadError == null && state.cannotEditReason == null
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(title = if (state.isEditing) "Sửa tin đăng" else "Đăng tin mới", onBack = requestClose)
        },
        bottomBar = {
            if (isFormVisible) {
                SubmitBar(
                    text = when {
                        state.isUploadingImages -> "Đang tải ảnh..."
                        state.isEditing -> "Lưu thay đổi"
                        else -> "Đăng tin"
                    },
                    enabled = !state.isUploadingImages,
                    loading = state.isSubmitting,
                    onClick = onSubmit,
                )
            }
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding).consumeWindowInsets(padding)
        when {
            state.isLoading -> LoadingView(modifier = contentModifier)
            state.loadError != null -> ErrorView(message = state.loadError, onRetry = onRetry, modifier = contentModifier)
            state.cannotEditReason != null -> EmptyView(
                icon = Icons.Outlined.Lock,
                title = "Không thể sửa tin này",
                message = state.cannotEditReason,
                actionLabel = "Quay lại",
                onAction = onClose,
                modifier = contentModifier,
            )
            else -> Column(
                modifier = contentModifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Space.screen),
                verticalArrangement = Arrangement.spacedBy(Space.md),
            ) {
                InlineBanner(
                    text = if (state.isEditing) {
                        "Sau khi lưu, tin sẽ được kiểm duyệt lại trước khi hiển thị."
                    } else {
                        "Tin đăng sẽ được kiểm duyệt trước khi hiển thị trên trang chủ."
                    },
                    tone = StatusTone.INFO,
                )
                state.rejectReason?.let { reason ->
                    InlineBanner(title = "Lý do bị từ chối lần trước", text = reason, tone = StatusTone.DANGER)
                }
                PostImagesSection(state = state, actions = actions)
                PostInfoSection(
                    state = state,
                    actions = actions,
                    onCategoryClick = { openSheet = EditorSheet.CATEGORY },
                    onConditionClick = { openSheet = EditorSheet.CONDITION },
                )
                PostPriceSection(state = state, actions = actions)
                PostDescriptionSection(state = state, actions = actions)
                PostLocationSection(
                    state = state,
                    onUniversityClick = { openSheet = EditorSheet.UNIVERSITY },
                    onPickupSpotClick = { openSheet = EditorSheet.PICKUP_SPOT },
                )
            }
        }
    }

    EditorSheets(state = state, openSheet = openSheet, onDismiss = { openSheet = null }, actions = sheetActions)

    if (showDiscardDialog) {
        ConfirmDialog(
            title = if (state.isEditing) "Bỏ các thay đổi?" else "Bỏ tin đang soạn?",
            message = "Những gì bạn vừa nhập sẽ không được lưu.",
            confirmText = "Bỏ",
            dismissText = "Soạn tiếp",
            destructive = true,
            onConfirm = {
                showDiscardDialog = false
                onClose()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

/** Các bảng chọn danh mục, tình trạng, trường, điểm giao dịch. */
@Composable
private fun EditorSheets(
    state: PostEditorUiState,
    openSheet: EditorSheet?,
    onDismiss: () -> Unit,
    actions: SheetActions,
) {
    when (openSheet) {
        EditorSheet.CATEGORY -> OptionPickerSheet(
            title = "Chọn danh mục",
            options = state.categories,
            selected = state.category,
            optionLabel = { it.name },
            onSelect = actions.onCategorySelected,
            onDismiss = onDismiss,
        )
        EditorSheet.CONDITION -> OptionPickerSheet(
            title = "Tình trạng món đồ",
            options = ProductCondition.entries,
            selected = state.condition,
            optionLabel = { it.label },
            optionSupportingText = { it.description },
            onSelect = actions.onConditionSelected,
            onDismiss = onDismiss,
            searchable = false,
        )
        EditorSheet.UNIVERSITY -> OptionPickerSheet(
            title = "Chọn trường",
            options = state.universities,
            selected = state.university,
            optionLabel = { it.name },
            optionSupportingText = { it.shortName },
            onSelect = actions.onUniversitySelected,
            onDismiss = onDismiss,
            searchable = true,
        )
        EditorSheet.PICKUP_SPOT -> OptionPickerSheet(
            title = "Chọn điểm giao dịch",
            options = state.meetupSpots,
            selected = state.pickupSpot,
            optionLabel = { it },
            onSelect = actions.onPickupSpotSelected,
            onDismiss = onDismiss,
            headerNote = "Các điểm giao dịch an toàn của ${state.university?.name ?: "trường"}",
            emptyText = "Trường này chưa có điểm giao dịch chuẩn.",
        )
        null -> Unit
    }
}

/** Thanh nút "Đăng tin" cố định ở cuối màn hình. */
@Composable
private fun SubmitBar(text: String, enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PrimaryButton(
                text = text,
                onClick = onClick,
                enabled = enabled,
                loading = loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Space.screen, vertical = Space.sm),
            )
        }
    }
}

package com.chodocu.app.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chodocu.app.core.Validators
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.PickerField
import com.chodocu.app.ui.components.PriceTextField
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.SectionCard
import com.chodocu.app.ui.components.categoryIcon
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.BrandGreen
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

private val TileSize = 96.dp

/** Các hành động nhập liệu của form đăng tin. */
internal data class PostEditorActions(
    val onAddImages: () -> Unit,
    val onRemoveImage: (Long) -> Unit,
    val onRetryImage: (Long) -> Unit,
    val onMakeCover: (Long) -> Unit,
    val onTitleChange: (String) -> Unit,
    val onConditionNoteChange: (String) -> Unit,
    val onFreeChange: (Boolean) -> Unit,
    val onPriceChange: (String) -> Unit,
    val onOriginalPriceChange: (String) -> Unit,
    val onUrgentChange: (Boolean) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onPassReasonChange: (String) -> Unit,
)

/** Mục ảnh: hàng ảnh vuông, ảnh đầu là ảnh bìa, ô cuối để thêm ảnh. */
@Composable
internal fun PostImagesSection(state: PostEditorUiState, actions: PostEditorActions) {
    SectionCard(
        title = "Hình ảnh",
        trailing = {
            Text(
                text = "${state.images.size}/${Validators.MAX_IMAGES}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
            itemsIndexed(state.images, key = { _, image -> image.id }) { index, image ->
                ImageTile(
                    image = image,
                    isCover = index == 0,
                    onRemove = { actions.onRemoveImage(image.id) },
                    onRetry = { actions.onRetryImage(image.id) },
                    onMakeCover = { actions.onMakeCover(image.id) },
                )
            }
            if (state.remainingImageSlots > 0) {
                item(key = "add-image") { AddImageTile(onClick = actions.onAddImages) }
            }
        }
        val error = state.errors.images
        Text(
            text = error ?: "Ảnh thật, rõ nét giúp bán nhanh hơn. Ảnh đầu tiên là ảnh bìa, chạm vào ảnh khác để đặt làm ảnh bìa.",
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ImageTile(
    image: EditorImage,
    isCover: Boolean,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onMakeCover: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(TileSize)
            .clip(RoundedCornerShape(Radius.input))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !isCover && image.url != null, onClickLabel = "Đặt làm ảnh bìa", onClick = onMakeCover),
    ) {
        if (image.url != null) {
            RemoteImage(
                url = image.url,
                contentDescription = if (isCover) "Ảnh bìa" else "Ảnh sản phẩm",
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Ảnh chưa tải lên xong: xem trước bằng ảnh trong máy
            AsyncImage(
                model = image.uri,
                contentDescription = "Ảnh đang tải lên",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        when {
            image.isUploading -> Box(
                modifier = Modifier.matchParentSize().background(AppColors.ImageScrim),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
            image.failed -> Column(
                modifier = Modifier
                    .matchParentSize()
                    .background(AppColors.ImageScrim)
                    .clickable(onClickLabel = "Tải lại ảnh", onClick = onRetry),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Color.White)
                Text("Thử lại", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        if (isCover) {
            Text(
                text = "Ảnh bìa",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BrandGreen.copy(alpha = 0.9f))
                    .padding(vertical = 2.dp),
            )
        }
        // Nút xóa ảnh: vùng chạm 32dp ở góc trên
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clickable(onClickLabel = "Xóa ảnh", role = Role.Button, onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(22.dp).background(AppColors.ImageScrim, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Xóa ảnh", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun AddImageTile(onClick: () -> Unit) {
    val shape = RoundedCornerShape(Radius.input)
    Column(
        modifier = Modifier
            .size(TileSize)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .border(Sizes.border, MaterialTheme.colorScheme.primary, shape)
            .clickable(onClickLabel = "Thêm ảnh", role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Thêm ảnh", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}

/** Tên, danh mục, tình trạng. */
@Composable
internal fun PostInfoSection(
    state: PostEditorUiState,
    actions: PostEditorActions,
    onCategoryClick: () -> Unit,
    onConditionClick: () -> Unit,
) {
    SectionCard(title = "Thông tin món đồ") {
        AppTextField(
            value = state.title,
            onValueChange = actions.onTitleChange,
            label = "Tên món đồ",
            placeholder = "Ví dụ: Giáo trình Giải tích 1",
            error = state.errors.title,
            required = true,
            maxLength = Validators.TITLE_MAX,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        )
        PickerField(
            label = "Danh mục",
            value = state.category?.name,
            placeholder = "Chọn danh mục",
            onClick = onCategoryClick,
            leadingIcon = state.category?.let { categoryIcon(it.icon) } ?: Icons.Outlined.Category,
            error = state.errors.category,
            required = true,
        )
        PickerField(
            label = "Tình trạng",
            value = state.condition?.label,
            placeholder = "Chọn tình trạng",
            onClick = onConditionClick,
            leadingIcon = Icons.Outlined.Inventory2,
            error = state.errors.condition,
            supportingText = state.condition?.description,
            required = true,
        )
        AppTextField(
            value = state.conditionNote,
            onValueChange = actions.onConditionNoteChange,
            label = "Ghi chú tình trạng",
            placeholder = "Ví dụ: Có vài nét bút chì ở chương 1",
            singleLine = false,
            maxLines = 3,
            maxLength = Validators.NOTE_MAX,
        )
    }
}

/** Giá bán, giá gốc, tặng miễn phí, bán gấp. */
@Composable
internal fun PostPriceSection(state: PostEditorUiState, actions: PostEditorActions) {
    SectionCard(title = "Giá bán") {
        SwitchRow(
            title = "Tặng miễn phí",
            subtitle = "Cho đi món đồ bạn không còn dùng",
            checked = state.isFree,
            onCheckedChange = actions.onFreeChange,
        )
        if (!state.isFree) {
            PriceTextField(
                value = state.price,
                onValueChange = actions.onPriceChange,
                label = "Giá bán",
                error = state.errors.price,
                required = true,
            )
            PriceTextField(
                value = state.originalPrice,
                onValueChange = actions.onOriginalPriceChange,
                label = "Giá gốc (không bắt buộc)",
                placeholder = "Giá lúc mua mới",
                error = state.errors.originalPrice,
                supportingText = state.discountPercent?.let { "Rẻ hơn $it% so với giá gốc" },
            )
        }
        SwitchRow(
            title = "Cần bán gấp",
            subtitle = "Tin có nhãn \"Gấp\" để người mua chú ý hơn",
            checked = state.isUrgent,
            onCheckedChange = actions.onUrgentChange,
        )
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = Space.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Mô tả chi tiết và lý do pass đồ. */
@Composable
internal fun PostDescriptionSection(state: PostEditorUiState, actions: PostEditorActions) {
    SectionCard(title = "Mô tả") {
        AppTextField(
            value = state.description,
            onValueChange = actions.onDescriptionChange,
            label = "Mô tả chi tiết",
            placeholder = "Mua khi nào, đã dùng bao lâu, có kèm phụ kiện gì...",
            singleLine = false,
            minLines = 4,
            maxLines = 10,
            maxLength = Validators.DESCRIPTION_MAX,
        )
        AppTextField(
            value = state.passReason,
            onValueChange = actions.onPassReasonChange,
            label = "Lý do pass đồ",
            placeholder = "Ví dụ: Ra trường, chuyển phòng...",
            maxLength = Validators.NOTE_MAX,
        )
    }
}

/** Trường và điểm giao dịch: bắt buộc chọn trong danh sách điểm chuẩn, không chọn sẵn. */
@Composable
internal fun PostLocationSection(
    state: PostEditorUiState,
    onUniversityClick: () -> Unit,
    onPickupSpotClick: () -> Unit,
) {
    SectionCard(title = "Địa điểm giao dịch") {
        PickerField(
            label = "Trường",
            value = state.university?.name,
            placeholder = "Chọn trường",
            onClick = onUniversityClick,
            leadingIcon = Icons.Outlined.School,
            error = state.errors.university,
            required = true,
        )
        PickerField(
            label = "Điểm giao dịch",
            value = state.pickupSpot,
            placeholder = "Chọn điểm giao dịch",
            onClick = onPickupSpotClick,
            leadingIcon = Icons.Outlined.Place,
            error = state.errors.pickupSpot,
            supportingText = if (state.university == null) {
                "Chọn trường trước"
            } else {
                "Chỉ được chọn điểm giao dịch an toàn của trường"
            },
            required = true,
            enabled = state.university != null,
        )
    }
}

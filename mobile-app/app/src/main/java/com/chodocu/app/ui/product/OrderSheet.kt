package com.chodocu.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.chodocu.app.core.Validators
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.data.model.FeePreview
import com.chodocu.app.data.model.Product
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.InfoRow
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.MeetupDateTimePicker
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.components.PickerField
import com.chodocu.app.ui.components.PriceText
import com.chodocu.app.ui.components.PriceTextField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.RemoteImage
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space
import java.time.LocalDateTime

/**
 * Form đặt mua dạng bottom sheet: giá đề nghị, điểm hẹn, giờ hẹn, lời nhắn.
 * Điểm hẹn chỉ được chọn trong danh sách điểm chuẩn của trường; app không chọn sẵn giúp người mua.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderSheet(
    product: Product,
    form: OrderForm,
    meetupSpots: List<String>,
    onPriceChange: (String) -> Unit,
    onLocationSelected: (String) -> Unit,
    onTimeSelected: (LocalDateTime) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSpotPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!form.isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = Space.screen, end = Space.screen, bottom = Space.lg),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            Text(
                text = if (product.isFree) "Xin nhận món đồ" else "Gửi yêu cầu mua",
                style = MaterialTheme.typography.titleLarge,
            )
            OrderProductSummary(product = product)

            if (product.isFree) {
                InlineBanner(
                    text = "Món đồ được tặng miễn phí. Bạn chỉ cần chọn điểm hẹn và thời gian nhận.",
                    tone = StatusTone.SUCCESS,
                    icon = Icons.Outlined.VolunteerActivism,
                )
            } else {
                PriceTextField(
                    value = form.offeredPrice,
                    onValueChange = onPriceChange,
                    label = "Giá bạn đề nghị",
                    error = form.priceError,
                    supportingText = "Giá người bán đăng: ${formatVnd(product.price)}",
                    required = true,
                )
            }
            PickerField(
                label = "Điểm hẹn",
                value = form.meetupLocation,
                placeholder = "Chọn điểm hẹn trong trường",
                onClick = { showSpotPicker = true },
                leadingIcon = Icons.Outlined.Place,
                error = form.locationError,
                supportingText = product.pickupSpot?.let { "Người bán thường giao tại: $it" },
                required = true,
            )
            PickerField(
                label = "Thời gian hẹn",
                value = form.meetupTime?.let(::formatMeetupTime),
                placeholder = "Chọn ngày và giờ",
                onClick = { showTimePicker = true },
                leadingIcon = Icons.Outlined.Schedule,
                error = form.timeError,
                required = true,
            )
            AppTextField(
                value = form.note,
                onValueChange = onNoteChange,
                label = "Lời nhắn cho người bán",
                placeholder = "Ví dụ: Mình tới được sau 17h",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                maxLength = Validators.NOTE_MAX,
            )
            form.fee?.let { FeeSummary(fee = it) }

            PrimaryButton(
                text = if (product.isFree) "Gửi yêu cầu nhận" else "Gửi yêu cầu mua",
                onClick = onSubmit,
                loading = form.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Bạn chưa phải trả tiền lúc này. Hai bên gặp mặt, kiểm tra hàng rồi mới thanh toán.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showSpotPicker) {
        OptionPickerSheet(
            title = "Chọn điểm hẹn",
            options = meetupSpots,
            selected = form.meetupLocation,
            optionLabel = { it },
            onSelect = onLocationSelected,
            onDismiss = { showSpotPicker = false },
            headerNote = "Các điểm giao dịch an toàn của ${product.universityName ?: "trường"}",
            emptyText = "Trường này chưa có điểm hẹn chuẩn. Hãy nhắn tin để hỏi người bán.",
        )
    }
    if (showTimePicker) {
        MeetupDateTimePicker(
            initial = form.meetupTime,
            onConfirm = { time ->
                showTimePicker = false
                onTimeSelected(time)
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

/** Ảnh nhỏ + tên + giá của món đồ đang đặt. */
@Composable
private fun OrderProductSummary(product: Product) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        RemoteImage(
            url = product.coverImage,
            contentDescription = product.title,
            modifier = Modifier.size(Sizes.thumb).clip(RoundedCornerShape(Radius.input)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PriceText(price = product.price, isFree = product.isFree)
        }
    }
}

/** Xem trước phí nền tảng (chỉ hiện khi sàn đang bật thu phí). */
@Composable
private fun FeeSummary(fee: FeePreview) {
    if (!fee.feeEnabled || fee.platformFee <= 0L) return
    val payer = when (fee.feePayer?.uppercase()) {
        "BUYER" -> "Người mua chịu phí"
        "SPLIT" -> "Hai bên chia đôi phí"
        else -> "Người bán chịu phí"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.input))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Space.sm),
    ) {
        val rate = fee.feeRatePercent?.let { " ($it%)" }.orEmpty()
        InfoRow(icon = null, label = "Phí nền tảng$rate", value = formatVnd(fee.platformFee))
        InfoRow(icon = null, label = "Bạn thanh toán", value = formatVnd(fee.buyerPay), valueColor = AppColors.Price)
        Text(text = payer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

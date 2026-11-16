package com.chodocu.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chodocu.app.core.Validators
import com.chodocu.app.core.formatMeetupTime
import com.chodocu.app.core.formatVnd
import com.chodocu.app.ui.components.ActionSheet
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.MeetupDateTimePicker
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.components.PickerField
import com.chodocu.app.ui.components.PriceTextField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.SheetAction
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Space
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/** Bảng "Gửi kèm": gửi ảnh, đề nghị giá, đề xuất hẹn gặp. */
@Composable
internal fun AttachSheet(
    canSendOffer: Boolean,
    canProposeMeetup: Boolean,
    onPickImage: () -> Unit,
    onOffer: () -> Unit,
    onMeetup: () -> Unit,
    onDismiss: () -> Unit,
) {
    ActionSheet(
        title = "Gửi kèm",
        actions = listOf(
            SheetAction(
                icon = Icons.Outlined.Image,
                label = "Gửi ảnh",
                description = "Chọn ảnh có sẵn trong máy",
                onClick = onPickImage,
            ),
            SheetAction(
                icon = Icons.Outlined.LocalOffer,
                label = "Đề nghị giá",
                description = if (canSendOffer) {
                    "Gửi mức giá bạn mong muốn cho người kia"
                } else {
                    "Chỉ dùng khi trò chuyện về một món đồ có giá bán"
                },
                enabled = canSendOffer,
                onClick = onOffer,
            ),
            SheetAction(
                icon = Icons.Outlined.Event,
                label = "Hẹn gặp",
                description = if (canProposeMeetup) {
                    "Chọn điểm giao dịch chuẩn của trường và thời gian"
                } else {
                    "Chỉ dùng khi trò chuyện về một món đồ"
                },
                enabled = canProposeMeetup,
                onClick = onMeetup,
            ),
        ),
        onDismiss = onDismiss,
    )
}

/**
 * Hộp thoại đề nghị giá. Nơi gọi tự đóng hộp thoại trong [onSend] / [onDismiss].
 * [listedPrice] là giá người bán đang đăng, hiện ra để người dùng tham khảo.
 */
@Composable
internal fun OfferDialog(
    listedPrice: Long,
    onSend: (price: Long, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var price by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var priceError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val value = price.toLongOrNull()
        priceError = when {
            value == null || value <= 0L -> "Vui lòng nhập giá đề nghị"
            value > Validators.MAX_PRICE -> "Giá đề nghị quá lớn"
            else -> null
        }
        if (priceError == null && value != null) onSend(value, note)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đề nghị giá") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                PriceTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = null
                    },
                    label = "Giá bạn đề nghị",
                    error = priceError,
                    supportingText = "Giá đang đăng: ${formatVnd(listedPrice)}",
                    required = true,
                )
                AppTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Lời nhắn (không bắt buộc)",
                    placeholder = "Ví dụ: Mình lấy luôn trong tuần này",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    maxLength = Validators.NOTE_MAX,
                )
            }
        },
        confirmButton = { TextButton(onClick = ::submit) { Text("Gửi đề nghị") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

/**
 * Bảng đề xuất hẹn gặp: người dùng TỰ CHỌN điểm hẹn trong danh sách điểm giao dịch chuẩn của trường
 * (không chọn sẵn) và thời gian hẹn. Nơi gọi tự đóng bảng trong [onSend] / [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeetupSheet(
    universityName: String?,
    meetupSpots: List<String>,
    onSend: (location: String, time: LocalDateTime, note: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var location by rememberSaveable { mutableStateOf<String?>(null) }
    var time by rememberSaveable { mutableStateOf<LocalDateTime?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var showSpotPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun submit() {
        val chosenLocation = location
        val chosenTime = time
        locationError = if (chosenLocation == null) "Vui lòng chọn điểm hẹn" else null
        timeError = when {
            chosenTime == null -> "Vui lòng chọn thời gian hẹn"
            chosenTime.isBefore(LocalDateTime.now()) -> "Thời gian này đã qua, vui lòng chọn lại"
            else -> null
        }
        if (chosenLocation == null || chosenTime == null || timeError != null) return
        scope.launch { sheetState.hide() }.invokeOnCompletion { onSend(chosenLocation, chosenTime, note) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Column(verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
                Text(text = "Đề xuất hẹn gặp", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Người kia sẽ nhận được thẻ hẹn ngay trong cuộc trò chuyện.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PickerField(
                label = "Điểm hẹn",
                value = location,
                placeholder = "Chọn điểm hẹn trong trường",
                onClick = { showSpotPicker = true },
                leadingIcon = Icons.Outlined.Place,
                error = locationError,
                required = true,
            )
            PickerField(
                label = "Thời gian",
                value = time?.let(::formatMeetupTime),
                placeholder = "Chọn ngày và giờ",
                onClick = { showTimePicker = true },
                leadingIcon = Icons.Outlined.Schedule,
                error = timeError,
                required = true,
            )
            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Lời nhắn (không bắt buộc)",
                placeholder = "Ví dụ: Mình mặc áo xanh, tới trước 5 phút",
                singleLine = false,
                minLines = 2,
                maxLines = 3,
                maxLength = Validators.NOTE_MAX,
            )
            PrimaryButton(text = "Gửi đề xuất", onClick = ::submit, modifier = Modifier.fillMaxWidth())
        }
    }

    if (showSpotPicker) {
        OptionPickerSheet(
            title = "Chọn điểm hẹn",
            options = meetupSpots,
            selected = location,
            optionLabel = { it },
            onSelect = {
                location = it
                locationError = null
            },
            onDismiss = { showSpotPicker = false },
            headerNote = "Các điểm giao dịch an toàn của ${universityName ?: "trường"}",
            emptyText = "Trường này chưa có điểm hẹn chuẩn.",
        )
    }
    if (showTimePicker) {
        MeetupDateTimePicker(
            initial = time,
            onConfirm = {
                showTimePicker = false
                time = it
                timeError = null
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

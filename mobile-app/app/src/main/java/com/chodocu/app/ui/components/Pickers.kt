package com.chodocu.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.removeDiacritics
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Bottom sheet chọn MỘT giá trị trong danh sách (trường, danh mục, tình trạng, điểm hẹn...).
 *
 * - Không có lựa chọn mặc định: [selected] = null thì không dòng nào được đánh dấu.
 * - Chọn xong sheet tự đóng rồi gọi [onSelect] và [onDismiss] — nơi gọi chỉ cần tắt cờ hiển thị trong onDismiss.
 * - Danh sách dài (> 8) có ô tìm kiếm, tìm không phân biệt dấu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OptionPickerSheet(
    title: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    optionSupportingText: ((T) -> String?)? = null,
    searchable: Boolean = options.size > 8,
    emptyText: String = "Chưa có lựa chọn nào",
    headerNote: String? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val filtered = remember(options, query) {
        val keyword = query.trim().removeDiacritics()
        if (keyword.isEmpty()) options
        else options.filter { optionLabel(it).removeDiacritics().contains(keyword, ignoreCase = true) }
    }

    fun choose(option: T) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onSelect(option)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet),
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Space.screen),
            )
            if (headerNote != null) {
                Text(
                    text = headerNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Space.screen, vertical = Space.xxs),
                )
            }
            if (searchable) {
                AppTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = null,
                    placeholder = "Tìm kiếm",
                    leadingIcon = Icons.Outlined.Search,
                    modifier = Modifier.padding(horizontal = Space.screen, vertical = Space.sm),
                )
            } else {
                Spacer(Modifier.padding(top = Space.sm))
            }
            if (filtered.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(Space.xl),
                )
            }
            LazyColumn(contentPadding = PaddingValues(bottom = Space.md)) {
                items(filtered) { option ->
                    OptionRow(
                        label = optionLabel(option),
                        supporting = optionSupportingText?.invoke(option),
                        isSelected = option == selected,
                        onClick = { choose(option) },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = Space.screen),
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, supporting: String?, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Space.screen, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            if (!supporting.isNullOrBlank()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(modifier = Modifier.size(Sizes.iconLarge), contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Đang chọn",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Chọn ngày giờ hẹn gặp: bước 1 chọn ngày (hôm nay đến [maxDaysAhead] ngày tới), bước 2 chọn giờ (24h).
 * Giờ hẹn phải sau thời điểm hiện tại ít nhất [minLeadMinutes] phút.
 * [initial] = null thì lịch chưa chọn sẵn ngày nào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetupDateTimePicker(
    initial: LocalDateTime?,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
    minLeadMinutes: Long = 30,
    maxDaysAhead: Long = 30,
) {
    val today = remember { LocalDate.now() }
    val lastDay = remember { today.plusDays(maxDaysAhead) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    if (pickedDate == null) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initial?.toLocalDate()?.toUtcMillis(),
            yearRange = today.year..lastDay.year,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = utcTimeMillis.toUtcLocalDate()
                    return !date.isBefore(today) && !date.isAfter(lastDay)
                }

                override fun isSelectableYear(year: Int): Boolean = year in today.year..lastDay.year
            },
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { pickedDate = dateState.selectedDateMillis?.toUtcLocalDate() },
                    enabled = dateState.selectedDateMillis != null,
                ) { Text("Tiếp tục") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        ) {
            DatePicker(
                state = dateState,
                title = {
                    Text(
                        text = "Chọn ngày hẹn",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = Space.xl, top = Space.md),
                    )
                },
                showModeToggle = false,
            )
        }
    } else {
        val date = pickedDate ?: return
        val suggested = remember { suggestedTime(initial) }
        val timeState = rememberTimePickerState(
            initialHour = suggested.hour,
            initialMinute = suggested.minute,
            is24Hour = true,
        )
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Chọn giờ hẹn", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timeState)
                    if (error != null) {
                        Text(
                            text = error.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val chosen = LocalDateTime.of(date, LocalTime.of(timeState.hour, timeState.minute))
                    if (chosen.isBefore(LocalDateTime.now().plusMinutes(minLeadMinutes))) {
                        error = "Giờ hẹn phải sau hiện tại ít nhất $minLeadMinutes phút."
                    } else {
                        onConfirm(chosen)
                    }
                }) { Text("Xác nhận") }
            },
            dismissButton = { TextButton(onClick = { pickedDate = null }) { Text("Chọn lại ngày") } },
        )
    }
}

/** Gợi ý giờ trong đồng hồ: giờ đã chọn trước đó, hoặc tròn giờ kế tiếp sau 1 tiếng. */
private fun suggestedTime(initial: LocalDateTime?): LocalTime {
    if (initial != null) return initial.toLocalTime()
    val next = LocalTime.now().plusHours(1)
    return LocalTime.of(next.hour, 0)
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

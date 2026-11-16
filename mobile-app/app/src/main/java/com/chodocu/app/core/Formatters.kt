package com.chodocu.app.core

import java.text.Normalizer
import java.text.NumberFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/*
 * Các hàm định dạng dùng chung cho toàn app: tiền, thời gian, chuỗi.
 * Tất cả đều là hàm thuần (không phụ thuộc Android) để dễ đọc và dễ kiểm thử.
 */

private val vietnamese: Locale = Locale.forLanguageTag("vi-VN")

/** Định dạng tiền Việt: 150000 -> "150.000 đ". */
fun formatVnd(amount: Long): String {
    val number = NumberFormat.getIntegerInstance(vietnamese).format(amount)
    return "$number đ"
}

/** Giá hiển thị trên thẻ / chi tiết: tin tặng miễn phí thì ghi "Miễn phí". */
fun formatPrice(price: Long, isFree: Boolean): String =
    if (isFree || price <= 0L) "Miễn phí" else formatVnd(price)

/** Rút gọn số lớn cho nhãn nhỏ: 950 -> "950", 1250 -> "1,2K", 3000000 -> "3M". */
fun formatCompactCount(count: Int): String {
    fun shorten(value: Int, unit: Int, suffix: String): String {
        val whole = value / unit
        val decimal = (value % unit) * 10 / unit
        return if (decimal == 0) "$whole$suffix" else "$whole,$decimal$suffix"
    }
    return when {
        count < 1_000 -> count.toString()
        count < 1_000_000 -> shorten(count, 1_000, "K")
        else -> shorten(count, 1_000_000, "M")
    }
}

// ---------------------------------------------------------------------------
// Thời gian
// ---------------------------------------------------------------------------

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", vietnamese)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy", vietnamese)
private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm", vietnamese)
private val meetupStorageFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

/** Tên thứ trong tuần bằng tiếng Việt (Thứ Hai ... Chủ Nhật). */
private fun dayOfWeekLabel(date: LocalDate): String = when (date.dayOfWeek.value) {
    1 -> "Thứ Hai"
    2 -> "Thứ Ba"
    3 -> "Thứ Tư"
    4 -> "Thứ Năm"
    5 -> "Thứ Sáu"
    6 -> "Thứ Bảy"
    else -> "Chủ Nhật"
}

/**
 * Đọc thời gian máy chủ trả về.
 * - Có múi giờ ("2026-11-16T10:20:30+00:00", "...Z") -> dùng đúng múi giờ đó.
 * - Không có múi giờ ("2026-11-16T10:20:30") -> coi là giờ địa phương của máy.
 * Chuỗi không hợp lệ trả về null để giao diện tự ẩn đi.
 */
fun parseServerInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    val text = value.trim().replace(' ', 'T')
    return try {
        OffsetDateTime.parse(text).toInstant()
    } catch (e: DateTimeParseException) {
        try {
            LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant()
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

/** "Vừa xong", "5 phút trước", "3 giờ trước", "Hôm qua", "3 ngày trước", hoặc "12/11/2026". */
fun formatRelativeTime(instant: Instant?, now: Instant = Instant.now()): String {
    if (instant == null) return ""
    val duration = Duration.between(instant, now)
    val minutes = duration.toMinutes()
    val zone = ZoneId.systemDefault()
    val days = Duration.between(
        instant.atZone(zone).toLocalDate().atStartOfDay(zone),
        now.atZone(zone).toLocalDate().atStartOfDay(zone)
    ).toDays()
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        days == 0L -> "${duration.toHours()} giờ trước"
        days == 1L -> "Hôm qua"
        days < 7 -> "$days ngày trước"
        else -> dateFormatter.format(instant.atZone(zone))
    }
}

/** "09:30, 16/11/2026". */
fun formatDateTime(instant: Instant?): String =
    instant?.let { dateTimeFormatter.format(it.atZone(ZoneId.systemDefault())) } ?: ""

/** Giờ ngắn "09:30" cho bong bóng chat. */
fun formatClock(instant: Instant?): String =
    instant?.let { clockFormatter.format(it.atZone(ZoneId.systemDefault())) } ?: ""

/** Nhãn ngày để chia nhóm tin nhắn: "Hôm nay", "Hôm qua", "Thứ Hai, 16/11/2026". */
fun formatDayLabel(instant: Instant, today: LocalDate = LocalDate.now()): String {
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    return when (date) {
        today -> "Hôm nay"
        today.minusDays(1) -> "Hôm qua"
        else -> "${dayOfWeekLabel(date)}, ${dateFormatter.format(date)}"
    }
}

/** Giờ hẹn gặp hiển thị: "09:30, Thứ Ba 17/11/2026". */
fun formatMeetupTime(time: LocalDateTime?): String {
    if (time == null) return ""
    val date = time.toLocalDate()
    return "${clockFormatter.format(time)}, ${dayOfWeekLabel(date)} ${dateFormatter.format(date)}"
}

/** Đọc giờ hẹn dạng "2026-11-17T09:30" (dữ liệu cũ không đúng định dạng -> null). */
fun parseMeetupTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return try {
        LocalDateTime.parse(value.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}

/** Đổi giờ hẹn sang chuỗi gửi lên máy chủ: "2026-11-17T09:30". */
fun LocalDateTime.toMeetupString(): String = meetupStorageFormatter.format(this)

// ---------------------------------------------------------------------------
// Chuỗi
// ---------------------------------------------------------------------------

/** Bỏ dấu tiếng Việt: "Bàn học" -> "Ban hoc" (dùng khi so khớp tìm kiếm). */
fun String.removeDiacritics(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
}

/** Chỉ giữ lại chữ số: "150.000 đ" -> "150000". */
fun String.digitsOnly(): String = filter { it.isDigit() }

/** Lấy chữ cái đầu của họ tên để làm avatar: "Nguyễn Văn An" -> "NA". */
fun initialsOf(fullName: String?): String {
    val words = fullName.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

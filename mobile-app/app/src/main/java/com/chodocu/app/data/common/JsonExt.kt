package com.chodocu.app.data.common

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/*
 * Các hàm đọc JSON "an toàn": khóa không có, giá trị null hoặc sai kiểu đều trả về null / giá trị
 * mặc định thay vì ném lỗi. Nhờ vậy máy chủ thiếu một trường cũng không làm app bị crash.
 */

/** Giá trị của [key], hoặc null nếu không có / là null. */
private fun JsonObject.valueOf(key: String): JsonElement? = get(key)?.takeUnless { it.isJsonNull }

/** Chuỗi của [key]; không có hoặc rỗng thì trả về null. */
fun JsonObject.stringOrNull(key: String): String? =
    valueOf(key)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }

/** Chuỗi của [key]; không có thì trả về [default]. */
fun JsonObject.string(key: String, default: String = ""): String =
    valueOf(key)?.takeIf { it.isJsonPrimitive }?.asString ?: default

fun JsonObject.longOrNull(key: String): Long? =
    valueOf(key)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asBigDecimal.toLong() }.getOrNull() }

fun JsonObject.long(key: String, default: Long = 0L): Long = longOrNull(key) ?: default

fun JsonObject.int(key: String, default: Int = 0): Int = longOrNull(key)?.toInt() ?: default

fun JsonObject.doubleOrNull(key: String): Double? =
    valueOf(key)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asDouble }.getOrNull() }

fun JsonObject.boolean(key: String, default: Boolean = false): Boolean =
    valueOf(key)?.takeIf { it.isJsonPrimitive }?.let { runCatching { it.asBoolean }.getOrNull() } ?: default

fun JsonObject.objectOrNull(key: String): JsonObject? = valueOf(key)?.takeIf { it.isJsonObject }?.asJsonObject

/** Mảng của [key]; không có thì trả về mảng rỗng. */
fun JsonObject.array(key: String): JsonArray = valueOf(key)?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()

/** Các phần tử là đối tượng JSON trong mảng (bỏ qua phần tử lạ). */
fun JsonArray.objects(): List<JsonObject> = mapNotNull { element -> element.takeIf { it.isJsonObject }?.asJsonObject }

/** Các phần tử là chuỗi không rỗng trong mảng. */
fun JsonArray.strings(): List<String> =
    mapNotNull { element -> element.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() } }

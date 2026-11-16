package com.chodocu.app.data.upload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.chodocu.app.data.common.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Nén ảnh trước khi tải lên: xoay đúng chiều theo EXIF, thu nhỏ cạnh dài về [MAX_SIDE] px,
 * lưu JPEG chất lượng [QUALITY]. Ảnh 5-10 MB từ camera thường còn khoảng 200-400 KB.
 */
class ImageCompressor(context: Context) {

    private val resolver = context.applicationContext.contentResolver

    suspend fun compress(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        // 1. Chỉ đọc kích thước ảnh để tính tỉ lệ thu nhỏ, chưa nạp cả ảnh vào bộ nhớ.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw unreadableImage()
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw unreadableImage()

        // 2. Nạp ảnh đã giảm mẫu (inSampleSize là lũy thừa của 2) để tránh hết bộ nhớ.
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= MAX_SIDE || bounds.outHeight / (sampleSize * 2) >= MAX_SIDE) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw unreadableImage()

        // 3. Thu nhỏ chính xác, xoay theo EXIF, rồi nén JPEG.
        val resized = resize(decoded, MAX_SIDE)
        val rotated = rotate(resized, readRotationDegrees(uri))
        val opaque = flattenTransparency(rotated)
        val bytes = ByteArrayOutputStream().use { output ->
            opaque.compress(Bitmap.CompressFormat.JPEG, QUALITY, output)
            output.toByteArray()
        }
        opaque.recycle()
        bytes
    }

    /** Góc cần xoay theo thông tin EXIF của ảnh chụp từ camera. */
    private fun readRotationDegrees(uri: Uri): Float = try {
        resolver.openInputStream(uri)?.use { input ->
            val orientation = ExifInterface(input)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
    } catch (e: Exception) {
        0f
    }

    private fun resize(source: Bitmap, maxSide: Int): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= maxSide) return source
        val scale = maxSide.toFloat() / longest
        val result = Bitmap.createScaledBitmap(
            source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (result != source) source.recycle()
        return result
    }

    private fun rotate(source: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        val result = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (result != source) source.recycle()
        return result
    }

    /** JPEG không có nền trong suốt: vẽ ảnh PNG lên nền trắng để không bị đen. */
    private fun flattenTransparency(source: Bitmap): Bitmap {
        if (!source.hasAlpha()) return source
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(result).apply {
            drawColor(Color.WHITE)
            drawBitmap(source, 0f, 0f, null)
        }
        source.recycle()
        return result
    }

    private fun unreadableImage() =
        ApiException("Không đọc được ảnh này. Vui lòng chọn ảnh khác.", ApiException.Kind.VALIDATION)

    private companion object {
        const val MAX_SIDE = 1600
        const val QUALITY = 85
    }
}

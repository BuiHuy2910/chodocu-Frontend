package com.chodocu.app.data.supabase

import android.net.Uri
import com.chodocu.app.data.common.wrapErrors
import com.chodocu.app.data.model.UploadFolder
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.data.upload.ImageCompressor
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Tải ảnh lên Supabase Storage: nén ảnh trên máy rồi lưu vào
 * "<thư mục>/<năm tháng>/<mã ngẫu nhiên>.jpg", ví dụ "products/202611/3f2a....jpg".
 */
class SupabaseUploadRepository(
    private val client: SupabaseClient,
    private val compressor: ImageCompressor,
) : UploadRepository {

    override suspend fun uploadImage(uri: Uri, folder: UploadFolder): String = wrapErrors {
        val bytes = compressor.compress(uri)
        val month = YearMonth.now().format(MONTH_FORMAT)
        val path = "${folder.path}/$month/${UUID.randomUUID()}.jpg"
        client.uploadJpeg(path, bytes)
    }

    private companion object {
        val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMM")
    }
}

package com.chodocu.app.data.repository

import android.net.Uri
import com.chodocu.app.data.model.UploadFolder

/** Tải ảnh lên máy chủ. Ảnh được nén trước khi gửi để tiết kiệm dung lượng. */
interface UploadRepository {
    /** Trả về đường dẫn công khai của ảnh đã tải lên. */
    suspend fun uploadImage(uri: Uri, folder: UploadFolder): String
}

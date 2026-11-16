package com.chodocu.app.di

import android.content.Context
import com.chodocu.app.core.AppConfig
import com.chodocu.app.data.backend.createBackendRepositories
import com.chodocu.app.data.repository.AuthRepository
import com.chodocu.app.data.repository.CatalogRepository
import com.chodocu.app.data.repository.ChatRepository
import com.chodocu.app.data.repository.NotificationRepository
import com.chodocu.app.data.repository.OrderRepository
import com.chodocu.app.data.repository.ProductRepository
import com.chodocu.app.data.repository.Repositories
import com.chodocu.app.data.repository.UploadRepository
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.data.supabase.createSupabaseRepositories
import com.chodocu.app.data.upload.ImageCompressor

/**
 * "Hộp" chứa các đối tượng dùng chung của app (DI thủ công, không cần thư viện).
 * Chỉ ở đây quyết định app gọi Supabase trực tiếp hay gọi backend Spring (xem AppConfig).
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val session = SessionManager(appContext)
    val imageCompressor = ImageCompressor(appContext)

    private val repositories: Repositories =
        if (AppConfig.USE_SUPABASE_DIRECT) {
            createSupabaseRepositories(session, imageCompressor)
        } else {
            createBackendRepositories(session, imageCompressor)
        }

    val auth: AuthRepository = repositories.auth
    val catalog: CatalogRepository = repositories.catalog
    val products: ProductRepository = repositories.products
    val orders: OrderRepository = repositories.orders
    val chat: ChatRepository = repositories.chat
    val notifications: NotificationRepository = repositories.notifications
    val uploads: UploadRepository = repositories.uploads
}

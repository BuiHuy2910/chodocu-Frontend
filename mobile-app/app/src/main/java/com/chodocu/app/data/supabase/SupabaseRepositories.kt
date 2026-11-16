package com.chodocu.app.data.supabase

import com.chodocu.app.data.repository.Repositories
import com.chodocu.app.data.session.SessionManager
import com.chodocu.app.data.upload.ImageCompressor

/**
 * Tạo bộ repository gọi thẳng Supabase (không cần chạy backend).
 * Yêu cầu: đã chạy database_schema.sql -> seed_data.sql -> supabase_direct_setup.sql trên Supabase.
 */
fun createSupabaseRepositories(
    session: SessionManager,
    compressor: ImageCompressor,
): Repositories {
    val client = SupabaseClient(session)
    return Repositories(
        auth = SupabaseAuthRepository(client, session),
        catalog = SupabaseCatalogRepository(client),
        products = SupabaseProductRepository(client),
        orders = SupabaseOrderRepository(client),
        chat = SupabaseChatRepository(client),
        notifications = SupabaseNotificationRepository(client),
        uploads = SupabaseUploadRepository(client, compressor),
    )
}

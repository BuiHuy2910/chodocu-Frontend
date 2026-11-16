package com.chodocu.app.data.repository

/** Gom tất cả repository của một chế độ dữ liệu (Supabase hoặc backend Spring). */
data class Repositories(
    val auth: AuthRepository,
    val catalog: CatalogRepository,
    val products: ProductRepository,
    val orders: OrderRepository,
    val chat: ChatRepository,
    val notifications: NotificationRepository,
    val uploads: UploadRepository,
)

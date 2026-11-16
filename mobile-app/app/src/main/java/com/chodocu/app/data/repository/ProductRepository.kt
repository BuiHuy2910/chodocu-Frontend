package com.chodocu.app.data.repository

import com.chodocu.app.data.model.PageResult
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductInput
import com.chodocu.app.data.model.ProductQuery
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ReportReason

/** Tin đăng: tìm kiếm, xem chi tiết, đăng / sửa / ẩn / xóa, yêu thích, báo cáo. */
interface ProductRepository {
    /** Tìm tin đang bán. `page` bắt đầu từ 0. */
    suspend fun searchProducts(query: ProductQuery, page: Int, pageSize: Int = 20): PageResult<Product>

    suspend fun getProduct(productId: Long): Product

    /** Tất cả tin của tôi (mọi trạng thái), mới nhất trước. */
    suspend fun getMyProducts(): List<Product>

    suspend fun createProduct(input: ProductInput): Product

    /** Sửa tin xong, tin quay lại trạng thái chờ duyệt. */
    suspend fun updateProduct(productId: Long, input: ProductInput): Product

    /** Người bán: APPROVED <-> HIDDEN, APPROVED / IN_TRANSACTION -> SOLD. */
    suspend fun changeStatus(productId: Long, status: ProductStatus): Product

    /** Không xóa được khi tin đã có đơn đặt mua (hãy ẩn tin). */
    suspend fun deleteProduct(productId: Long)

    /** Bật / tắt yêu thích, trả về trạng thái mới. */
    suspend fun toggleFavorite(productId: Long): Boolean

    suspend fun getFavorites(): List<Product>

    suspend fun reportProduct(productId: Long, reason: ReportReason, detail: String?)
}

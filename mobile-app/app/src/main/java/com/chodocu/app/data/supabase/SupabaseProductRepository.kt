package com.chodocu.app.data.supabase

import com.chodocu.app.data.common.array
import com.chodocu.app.data.common.boolean
import com.chodocu.app.data.common.objects
import com.chodocu.app.data.model.PageResult
import com.chodocu.app.data.model.Product
import com.chodocu.app.data.model.ProductInput
import com.chodocu.app.data.model.ProductQuery
import com.chodocu.app.data.model.ProductStatus
import com.chodocu.app.data.model.ReportReason
import com.chodocu.app.data.repository.ProductRepository

/** Tin đăng qua các hàm api_search_products, api_create_product, api_toggle_wishlist... */
class SupabaseProductRepository(private val client: SupabaseClient) : ProductRepository {

    override suspend fun searchProducts(query: ProductQuery, page: Int, pageSize: Int): PageResult<Product> {
        val result = client.callObject(
            "api_search_products",
            "p_keyword" to query.keyword.trim().ifEmpty { null },
            "p_category_id" to query.categoryId,
            "p_university_id" to query.universityId,
            "p_sort" to query.sort.name,
            "p_limit" to pageSize,
            "p_offset" to page * pageSize,
        )
        return PageResult(
            items = result.array("items").objects().map { it.toProduct() },
            hasMore = result.boolean("has_more"),
        )
    }

    override suspend fun getProduct(productId: Long): Product =
        client.callObject("api_get_product", "p_product_id" to productId).toProduct()

    override suspend fun getMyProducts(): List<Product> =
        client.callList("api_my_posts").map { it.toProduct() }

    override suspend fun createProduct(input: ProductInput): Product =
        client.callObject("api_create_product", *input.toParams()).toProduct()

    override suspend fun updateProduct(productId: Long, input: ProductInput): Product =
        client.callObject("api_update_product", "p_product_id" to productId, *input.toParams()).toProduct()

    override suspend fun changeStatus(productId: Long, status: ProductStatus): Product =
        client.callObject(
            "api_set_product_status",
            "p_product_id" to productId,
            "p_status" to status.name,
        ).toProduct()

    override suspend fun deleteProduct(productId: Long) {
        client.callObject("api_delete_product", "p_product_id" to productId)
    }

    override suspend fun toggleFavorite(productId: Long): Boolean =
        client.callObject("api_toggle_wishlist", "p_product_id" to productId).boolean("favorited")

    override suspend fun getFavorites(): List<Product> =
        client.callList("api_my_wishlist").map { it.toProduct() }

    override suspend fun reportProduct(productId: Long, reason: ReportReason, detail: String?) {
        client.callObject(
            "api_report_product",
            "p_product_id" to productId,
            "p_reason" to reason.name,
            "p_detail" to detail?.trim()?.ifEmpty { null },
        )
    }

    /** Tham số chung của đăng tin và sửa tin. */
    private fun ProductInput.toParams(): Array<Pair<String, Any?>> = arrayOf(
        "p_title" to title.trim(),
        "p_description" to description.trim(),
        "p_price" to if (isFree) 0L else price,
        "p_original_price" to originalPrice,
        "p_is_free" to isFree,
        "p_is_urgent" to isUrgent,
        "p_condition" to condition.name,
        "p_condition_note" to conditionNote?.trim()?.ifEmpty { null },
        "p_pass_reason" to passReason?.trim()?.ifEmpty { null },
        "p_category_id" to categoryId,
        "p_university_id" to universityId,
        "p_pickup_spot" to pickupSpot,
        "p_image_urls" to imageUrls,
    )
}

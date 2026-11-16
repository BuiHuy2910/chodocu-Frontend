package com.chodocu.app.data.repository

import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.University

/** Danh mục và danh sách trường (ít thay đổi nên được lưu tạm trong bộ nhớ). */
interface CatalogRepository {
    suspend fun getCategories(forceRefresh: Boolean = false): List<Category>

    suspend fun getUniversities(forceRefresh: Boolean = false): List<University>
}

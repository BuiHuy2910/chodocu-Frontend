package com.chodocu.app.data.supabase

import com.chodocu.app.data.model.Category
import com.chodocu.app.data.model.University
import com.chodocu.app.data.repository.CatalogRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Danh mục và danh sách trường: đọc thẳng bảng công khai, lưu tạm trong bộ nhớ sau lần đầu. */
class SupabaseCatalogRepository(private val client: SupabaseClient) : CatalogRepository {

    // Khóa để nhiều màn hình gọi cùng lúc cũng chỉ tải một lần
    private val mutex = Mutex()
    private var cachedCategories: List<Category>? = null
    private var cachedUniversities: List<University>? = null

    override suspend fun getCategories(forceRefresh: Boolean): List<Category> = mutex.withLock {
        cachedCategories?.takeUnless { forceRefresh }
            ?: client.fetchCategories().map { it.toCategory() }.also { cachedCategories = it }
    }

    override suspend fun getUniversities(forceRefresh: Boolean): List<University> = mutex.withLock {
        cachedUniversities?.takeUnless { forceRefresh }
            ?: client.fetchUniversities().map { it.toUniversity() }.also { cachedUniversities = it }
    }
}

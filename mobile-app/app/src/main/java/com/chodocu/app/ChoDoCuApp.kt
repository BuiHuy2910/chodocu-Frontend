package com.chodocu.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.chodocu.app.di.AppContainer

/**
 * Lớp Application:
 * - Khởi tạo AppContainer dùng chung
 * - Cấu hình ImageLoader của Coil với bộ nhớ đệm (RAM + Disk Cache) để tránh spam tải lại ảnh từ Supabase Storage CDN.
 */
class ChoDoCuApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Dành 25% RAM trống cho cache ảnh
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB bộ đệm trên đĩa
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false) // Giữ cache ảnh ngay cả khi máy chủ CDN chưa set cache-control
            .build()
    }
}

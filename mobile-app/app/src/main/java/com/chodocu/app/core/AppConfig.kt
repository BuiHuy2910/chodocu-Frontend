package com.chodocu.app.core

/**
 * Cấu hình chế độ dữ liệu của app.
 *
 * USE_SUPABASE_DIRECT = true  -> app gọi thẳng Supabase (REST + RPC), không cần chạy backend.
 * USE_SUPABASE_DIRECT = false -> app gọi backend Spring Boot như trước.
 *
 * Lưu ý: khi bật chế độ trực tiếp, cần chạy file `supabase_direct_setup.sql`
 * (view + RPC + session + storage policy) trên Supabase SQL Editor.
 */
object AppConfig {

    const val USE_SUPABASE_DIRECT: Boolean = true

    const val SUPABASE_URL: String = "https://ovxnlepmewindufcgxln.supabase.co"

    /** Publishable/anon key — an toàn để nhúng trong app, quyền bị giới hạn bởi RLS + RPC. */
    const val SUPABASE_ANON_KEY: String = "sb_publishable_K8ULW9tqBfNFhtRg8l1QjA_v3turNaH"

    const val STORAGE_BUCKET: String = "passuni"

    /** Địa chỉ backend Spring Boot (chỉ dùng khi tắt chế độ trực tiếp). */
    const val BACKEND_BASE_URL: String = "http://10.0.2.2:8080/api/"
}

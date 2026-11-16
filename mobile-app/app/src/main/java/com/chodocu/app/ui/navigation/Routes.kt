package com.chodocu.app.ui.navigation

import android.net.Uri
import com.chodocu.app.data.model.OrderRole

/** Tên các tham số điều hướng (đọc lại trong ViewModel qua SavedStateHandle). */
object NavArgs {
    const val PRODUCT_ID = "productId"
    const val PARTNER_ID = "partnerId"
    const val PARTNER_NAME = "partnerName"
    const val TAB = "tab"

    /** Giá trị productId nghĩa là "không có" (đăng tin mới, chat không gắn sản phẩm). */
    const val NO_ID = -1L
}

/** Mẫu đường dẫn (route) của từng màn hình và hàm tạo đường dẫn có tham số. */
object Routes {
    // Nhóm chưa đăng nhập
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Các tab chính
    const val HOME = "home"
    const val INBOX = "inbox"
    const val ORDERS = "orders?${NavArgs.TAB}={${NavArgs.TAB}}"
    const val PROFILE = "profile"

    // Màn hình con
    const val POST_NEW = "post/new"
    const val POST_EDIT = "post/edit/{${NavArgs.PRODUCT_ID}}"
    const val PRODUCT_DETAIL = "product/{${NavArgs.PRODUCT_ID}}"
    const val CHAT_THREAD =
        "chat/{${NavArgs.PARTNER_ID}}?${NavArgs.PRODUCT_ID}={${NavArgs.PRODUCT_ID}}&${NavArgs.PARTNER_NAME}={${NavArgs.PARTNER_NAME}}"
    const val MY_POSTS = "my-posts"
    const val FAVORITES = "favorites"
    const val EDIT_PROFILE = "profile/edit"
    const val CHANGE_PASSWORD = "profile/password"
    const val NOTIFICATIONS = "notifications"

    /** Tab giao dịch không kèm tham số (dùng cho thanh điều hướng dưới). */
    const val ORDERS_TAB = "orders"

    fun orders(role: OrderRole): String = "orders?${NavArgs.TAB}=${role.name}"

    fun postEdit(productId: Long): String = "post/edit/$productId"

    fun productDetail(productId: Long): String = "product/$productId"

    fun chatThread(partnerId: Long, productId: Long?, partnerName: String): String =
        "chat/$partnerId?${NavArgs.PRODUCT_ID}=${productId ?: NavArgs.NO_ID}&${NavArgs.PARTNER_NAME}=${Uri.encode(partnerName)}"
}

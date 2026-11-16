package com.chodocu.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chodocu.app.ui.theme.AppColors
import com.chodocu.app.ui.theme.Space
import com.chodocu.app.ui.theme.TextTertiary

/**
 * Ảnh tải từ mạng, có nền xám khi đang tải và icon thay thế khi lỗi / không có ảnh.
 * [modifier] phải đặt kích thước (ví dụ `fillMaxWidth().aspectRatio(4f / 3f)` hoặc `size(72.dp)`).
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var failed by remember(url) { mutableStateOf(false) }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank() || failed) {
            Icon(
                imageVector = if (url.isNullOrBlank()) Icons.Outlined.Image else Icons.Outlined.BrokenImage,
                contentDescription = contentDescription,
                tint = TextTertiary,
                modifier = Modifier.size(28.dp),
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                onError = { failed = true },
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * Xem ảnh toàn màn hình: vuốt ngang để chuyển ảnh, chụm hai ngón để phóng to,
 * chạm đúp để phóng to / thu nhỏ.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    urls: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit,
) {
    if (urls.isEmpty()) return
    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, urls.lastIndex)) { urls.size }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                ZoomableImage(url = urls[page], contentDescription = "Ảnh ${page + 1} / ${urls.size}")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(horizontal = Space.xs, vertical = Space.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Đóng", tint = Color.White)
                }
                Box(modifier = Modifier.weight(1f))
                if (urls.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${urls.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.ImageScrim)
                            .padding(horizontal = Space.sm, vertical = Space.xxs),
                    )
                }
            }
        }
    }
}

/** Một ảnh có thể phóng to. Khi chưa phóng to, cử chỉ vuốt được nhường cho pager để chuyển ảnh. */
@Composable
private fun ZoomableImage(url: String, contentDescription: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    // Chạm đúp: đang phóng thì thu về, chưa phóng thì phóng 2.5 lần
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = 2.5f
                    }
                })
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        // Chỉ xử lý khi đang chụm ngón hoặc ảnh đã phóng to; còn lại để pager vuốt
                        if (zoom != 1f || scale > 1f) {
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            val maxX = (size.width * (scale - 1f)) / 2f
                            val maxY = (size.height * (scale - 1f)) / 2f
                            val moved = offset + event.calculatePan()
                            offset = if (scale == 1f) {
                                Offset.Zero
                            } else {
                                Offset(moved.x.coerceIn(-maxX, maxX), moved.y.coerceIn(-maxY, maxY))
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

/** Số ảnh tối đa trình chọn ảnh hệ thống cho phép trong một lần chọn (app chỉ cần tối đa 5). */
private const val PICKER_HARD_LIMIT = 20

/**
 * Trình chọn NHIỀU ảnh (Android Photo Picker, không cần quyền đọc bộ nhớ).
 * Trả về hàm mở trình chọn. [maxItems] = số ảnh còn được thêm; <= 0 thì không mở.
 * Lưu ý: Uri chỉ đọc được trong phiên chạy hiện tại, nên nén và tải lên sớm.
 */
@Composable
fun rememberMultipleImagePicker(maxItems: Int, onPicked: (List<Uri>) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val currentMax by rememberUpdatedState(maxItems)
    // Hợp đồng chọn nhiều ảnh bắt buộc maxItems > 1, nên khi chỉ còn 1 chỗ thì dùng trình chọn 1 ảnh
    val multiLimit = maxItems.coerceIn(2, PICKER_HARD_LIMIT)
    val multiContract = remember(multiLimit) { ActivityResultContracts.PickMultipleVisualMedia(multiLimit) }
    val singleContract = remember { ActivityResultContracts.PickVisualMedia() }
    val multiLauncher = rememberLauncherForActivityResult(multiContract) { uris ->
        if (uris.isNotEmpty()) currentOnPicked(uris.take(currentMax.coerceAtLeast(0)))
    }
    val singleLauncher = rememberLauncherForActivityResult(singleContract) { uri ->
        if (uri != null) currentOnPicked(listOf(uri))
    }
    return {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        when {
            currentMax <= 0 -> Unit
            currentMax == 1 -> singleLauncher.launch(request)
            else -> multiLauncher.launch(request)
        }
    }
}

/** Trình chọn MỘT ảnh (ảnh đại diện, gửi ảnh trong chat). Trả về hàm mở trình chọn. */
@Composable
fun rememberSingleImagePicker(onPicked: (Uri) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val contract = remember { ActivityResultContracts.PickVisualMedia() }
    val launcher = rememberLauncherForActivityResult(contract) { uri ->
        if (uri != null) currentOnPicked(uri)
    }
    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

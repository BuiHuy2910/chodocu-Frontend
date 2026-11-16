package com.chodocu.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.chodocu.app.core.Validators
import com.chodocu.app.ui.theme.Space

/** Câu hỏi gợi ý khi người mua mở cuộc trò chuyện mới về một món đồ. */
internal val BUYER_QUICK_REPLIES = listOf(
    "Món này còn không bạn?",
    "Bạn có bớt giá không?",
    "Mình xem hàng trực tiếp được không?",
)

/**
 * Ô nhập tin nhắn ở cuối màn hình: nút gửi kèm (ảnh, trả giá, hẹn gặp), ô chữ và nút gửi.
 * Tự đẩy lên trên bàn phím và thanh điều hướng của hệ thống.
 */
@Composable
internal fun ChatInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    quickReplies: List<String>,
    onQuickReply: (String) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (quickReplies.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.screen),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                    modifier = Modifier.padding(top = Space.xs),
                ) {
                    items(quickReplies) { text ->
                        SuggestionChip(onClick = { onQuickReply(text) }, label = { Text(text) })
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Space.xxs, vertical = Space.xs),
                verticalAlignment = Alignment.Bottom,
            ) {
                IconButton(onClick = onAttach) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Gửi kèm ảnh, trả giá, hẹn gặp",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                TextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nhắn tin…") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    supportingText = if (draft.length > Validators.MESSAGE_MAX - 100) {
                        { Text("${draft.length}/${Validators.MESSAGE_MAX}") }
                    } else {
                        null
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                val canSend = draft.isNotBlank()
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi")
                }
            }
        }
    }
}

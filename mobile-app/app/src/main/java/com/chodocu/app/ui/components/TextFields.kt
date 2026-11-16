package com.chodocu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.chodocu.app.core.digitsOnly
import com.chodocu.app.ui.theme.Radius
import com.chodocu.app.ui.theme.Sizes
import com.chodocu.app.ui.theme.Space

/** Nhãn phía trên ô nhập. Trường bắt buộc có thêm dấu * màu đỏ. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier, required: Boolean = false) {
    Row(modifier = modifier.padding(bottom = Space.xs)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (required) {
            Text(
                text = " *",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/** Dòng chữ nhỏ dưới ô nhập: lỗi (đỏ) hoặc gợi ý (xám), kèm bộ đếm ký tự nếu có. */
@Composable
private fun FieldSupportingRow(error: String?, supportingText: String?, counter: String?) {
    if (error == null && supportingText == null && counter == null) return
    Row(modifier = Modifier.fillMaxWidth().padding(top = Space.xxs)) {
        Text(
            text = error ?: supportingText.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (counter != null) {
            Text(
                text = counter,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Ô nhập chuẩn của app: nhãn bên trên, viền mảnh bo 12dp, báo lỗi bên dưới.
 * [maxLength] giới hạn số ký tự và hiện bộ đếm.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    suffix: String? = null,
    error: String? = null,
    supportingText: String? = null,
    required: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 6,
    maxLength: Int? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        if (label != null) FieldLabel(text = label, required = required)
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) onValueChange(newValue)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.inputMinHeight),
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            isError = error != null,
            placeholder = placeholder?.let {
                { Text(it, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            },
            leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.icon)) } },
            trailingIcon = trailingContent,
            suffix = suffix?.let { { Text(it, style = MaterialTheme.typography.bodyLarge) } },
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(Radius.input),
            colors = appTextFieldColors(),
        )
        FieldSupportingRow(
            error = error,
            supportingText = supportingText,
            counter = maxLength?.takeIf { max -> !singleLine || value.length > max * 0.8 }?.let { "${value.length}/$it" },
        )
    }
}

/** Màu ô nhập: nền trắng, viền xám, viền xanh khi đang nhập. */
@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
)

/** Ô nhập mật khẩu có nút ẩn / hiện. */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    error: String? = null,
    supportingText: String? = null,
    required: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        error = error,
        supportingText = supportingText,
        required = required,
        maxLength = 72,
        keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingContent = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                )
            }
        },
    )
}

/**
 * Ô nhập giá tiền: chỉ nhận chữ số, tự thêm dấu chấm phân cách hàng nghìn, có hậu tố "đ".
 * [value] là chuỗi chữ số thô (ví dụ "150000"); đọc ra số bằng `value.toLongOrNull()`.
 */
@Composable
fun PriceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = "0",
    error: String? = null,
    supportingText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Next,
) {
    val transformation = remember { ThousandsSeparatorTransformation() }
    AppTextField(
        value = value,
        onValueChange = { input ->
            val digits = input.digitsOnly().take(10).trimStart('0')
            onValueChange(digits)
        },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        suffix = "đ",
        error = error,
        supportingText = supportingText,
        required = required,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = transformation,
    )
}

/** Hiển thị "1500000" thành "1.500.000" mà không đổi giá trị thật. */
private class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val firstGroup = digits.length % 3
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index > 0 && (index - firstGroup) % 3 == 0) append('.')
                append(char)
            }
        }
        // Bảng đổi vị trí con trỏ giữa chuỗi gốc và chuỗi đã thêm dấu chấm.
        val originalToTransformed = IntArray(digits.length + 1)
        val transformedToOriginal = IntArray(formatted.length + 1)
        var originalIndex = 0
        formatted.forEachIndexed { transformedIndex, char ->
            transformedToOriginal[transformedIndex] = originalIndex
            if (char != '.') {
                originalToTransformed[originalIndex] = transformedIndex
                originalIndex++
            }
        }
        originalToTransformed[digits.length] = formatted.length
        transformedToOriginal[formatted.length] = digits.length

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, digits.length)]

            override fun transformedToOriginal(offset: Int): Int =
                transformedToOriginal[offset.coerceIn(0, formatted.length)]
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

/**
 * Ô chọn giá trị từ danh sách (trường, danh mục, điểm hẹn, ngày giờ...).
 * Khi chưa chọn hiển thị [placeholder] màu xám — app KHÔNG tự chọn sẵn giá trị nào.
 */
@Composable
fun PickerField(
    label: String?,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    error: String? = null,
    supportingText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = when {
        error != null -> colors.error
        !enabled -> colors.outlineVariant
        else -> colors.outline
    }
    Column(modifier = modifier) {
        if (label != null) FieldLabel(text = label, required = required)
        Surface(
            shape = RoundedCornerShape(Radius.input),
            color = if (enabled) colors.surface else colors.surfaceVariant,
            border = BorderStroke(Sizes.border, borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Sizes.inputMinHeight)
                .clip(RoundedCornerShape(Radius.input))
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (value != null) colors.primary else colors.onSurfaceVariant,
                        modifier = Modifier.size(Sizes.icon),
                    )
                }
                Text(
                    text = value ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value != null) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.icon),
                )
            }
        }
        FieldSupportingRow(error = error, supportingText = supportingText, counter = null)
    }
}

/** Khoảng trống dọc chuẩn giữa các ô trong form. */
@Composable
fun FormSpacer() {
    Spacer(Modifier.height(Space.md))
}

/** Viền mảnh dùng cho các khối tự vẽ (giữ nhất quán với thẻ). */
fun Modifier.hairlineBorder(color: Color, radius: Dp): Modifier =
    this.border(Sizes.border, color, RoundedCornerShape(radius))

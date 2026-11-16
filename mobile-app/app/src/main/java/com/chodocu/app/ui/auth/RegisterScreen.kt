package com.chodocu.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.core.Validators
import com.chodocu.app.data.model.University
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.APP_NAME
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.FormSpacer
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.components.PasswordTextField
import com.chodocu.app.ui.components.PickerField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.theme.Space

/** Màn đăng ký tài khoản mới. Đăng ký xong được đăng nhập luôn. */
@Composable
fun RegisterScreen(onBack: () -> Unit) {
    val viewModel = appViewModel { _ -> RegisterViewModel(auth, catalog) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterContent(
        state = state,
        onBack = onBack,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onUniversitySelected = viewModel::onUniversitySelected,
        onRetryUniversities = viewModel::loadUniversities,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onBack: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onUniversitySelected: (University) -> Unit,
    onRetryUniversities: () -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var showUniversityPicker by remember { mutableStateOf(false) }
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Tạo tài khoản", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl, vertical = Space.lg),
        ) {
            Text("Chào mừng đến $APP_NAME", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Chỉ mất một phút để bắt đầu mua bán đồ cũ với sinh viên trong trường.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.lg))

            if (state.errorMessage != null) {
                InlineBanner(text = state.errorMessage, tone = StatusTone.DANGER)
                Spacer(Modifier.height(Space.md))
            }

            AppTextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = "Họ và tên",
                placeholder = "Nguyễn Văn A",
                leadingIcon = Icons.Outlined.Person,
                error = state.errors.fullName,
                required = true,
                maxLength = Validators.NAME_MAX,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            AppTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "ten@email.com",
                leadingIcon = Icons.Outlined.Email,
                error = state.errors.email,
                supportingText = "Dùng email này để đăng nhập",
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            AppTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                label = "Số điện thoại",
                placeholder = "0912345678",
                leadingIcon = Icons.Outlined.Phone,
                error = state.errors.phone,
                supportingText = "Chỉ hiện cho đối tác khi hai bên đã chốt hẹn",
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            PickerField(
                label = "Trường đại học",
                value = state.university?.name,
                placeholder = if (state.isLoadingUniversities) "Đang tải danh sách trường..." else "Chọn trường của bạn",
                onClick = {
                    focusManager.clearFocus()
                    if (state.universities.isEmpty()) onRetryUniversities() else showUniversityPicker = true
                },
                leadingIcon = Icons.Outlined.School,
                error = state.errors.university ?: state.universitiesError?.let { "$it Chạm để thử lại." },
                required = true,
                enabled = !state.isLoadingUniversities,
            )
            FormSpacer()
            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                placeholder = "Ít nhất ${Validators.PASSWORD_MIN} ký tự",
                leadingIcon = Icons.Outlined.Lock,
                error = state.errors.password,
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Nhập lại mật khẩu",
                placeholder = "Nhập lại mật khẩu",
                leadingIcon = Icons.Outlined.Lock,
                error = state.errors.confirmPassword,
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(Modifier.height(Space.xl))

            PrimaryButton(
                text = "Tạo tài khoản",
                onClick = submit,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Space.xs))
            Text(
                text = "Khi tạo tài khoản, bạn đồng ý giao dịch trung thực và gặp mặt tại các điểm hẹn an toàn trong trường.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đã có tài khoản?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onBack, enabled = !state.isSubmitting) {
                    Text("Đăng nhập", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showUniversityPicker) {
        OptionPickerSheet(
            title = "Chọn trường của bạn",
            options = state.universities,
            selected = state.university,
            optionLabel = { it.name },
            optionSupportingText = { it.shortName },
            onSelect = onUniversitySelected,
            onDismiss = { showUniversityPicker = false },
            searchable = true,
        )
    }
}

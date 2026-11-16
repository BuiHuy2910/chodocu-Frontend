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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.data.common.ApiException
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppBrandHeader
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.FormSpacer
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.PasswordTextField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.theme.Space

/** Màn đăng nhập bằng email + mật khẩu (không cần xác thực email sinh viên). */
@Composable
fun LoginScreen(onNavigateToRegister: () -> Unit) {
    val viewModel = appViewModel { _ -> LoginViewModel(auth, session) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onNavigateToRegister = onNavigateToRegister,
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.xl, vertical = Space.xl),
        ) {
            Spacer(Modifier.height(Space.lg))
            AppBrandHeader(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Space.xxl))

            Text("Đăng nhập", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Mua bán, trao đổi đồ cũ an toàn với sinh viên quanh bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.lg))

            if (state.showSessionExpired) {
                InlineBanner(text = ApiException.MSG_SESSION_EXPIRED, tone = StatusTone.WARNING)
                Spacer(Modifier.height(Space.md))
            }
            if (state.errorMessage != null) {
                InlineBanner(text = state.errorMessage, tone = StatusTone.DANGER)
                Spacer(Modifier.height(Space.md))
            }

            AppTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "ten@email.com",
                leadingIcon = Icons.Outlined.Email,
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
            FormSpacer()
            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                placeholder = "Nhập mật khẩu",
                leadingIcon = Icons.Outlined.Lock,
                error = state.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            Spacer(Modifier.height(Space.xl))

            PrimaryButton(
                text = "Đăng nhập",
                onClick = submit,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Space.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chưa có tài khoản?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onNavigateToRegister, enabled = !state.isSubmitting) {
                    Text("Đăng ký ngay", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

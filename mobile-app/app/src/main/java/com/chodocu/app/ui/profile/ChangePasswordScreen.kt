package com.chodocu.app.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.core.Validators
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.FormSpacer
import com.chodocu.app.ui.components.InlineBanner
import com.chodocu.app.ui.components.PasswordTextField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.components.StatusTone
import com.chodocu.app.ui.theme.Space

/** Màn đổi mật khẩu. */
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val viewModel = appViewModel { _ -> ChangePasswordViewModel(auth) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    val submit = {
        focusManager.clearFocus()
        viewModel.submit()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Đổi mật khẩu", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Space.screen),
        ) {
            state.errorMessage?.let { message ->
                InlineBanner(text = message, tone = StatusTone.DANGER)
                FormSpacer()
            }
            PasswordTextField(
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentChange,
                label = "Mật khẩu hiện tại",
                leadingIcon = Icons.Outlined.Lock,
                error = state.currentError,
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            PasswordTextField(
                value = state.newPassword,
                onValueChange = viewModel::onNewChange,
                label = "Mật khẩu mới",
                placeholder = "Ít nhất ${Validators.PASSWORD_MIN} ký tự",
                leadingIcon = Icons.Outlined.Lock,
                error = state.newError,
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            PasswordTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmChange,
                label = "Nhập lại mật khẩu mới",
                leadingIcon = Icons.Outlined.Lock,
                error = state.confirmError,
                required = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            FormSpacer()
            PrimaryButton(
                text = "Đổi mật khẩu",
                onClick = submit,
                loading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.isDone) {
        AlertDialog(
            onDismissRequest = onBack,
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Đã đổi mật khẩu") },
            text = { Text("Lần đăng nhập sau, hãy dùng mật khẩu mới của bạn.") },
            confirmButton = { TextButton(onClick = onBack) { Text("Xong") } },
        )
    }
}

package com.chodocu.app.ui.profile

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chodocu.app.core.Validators
import com.chodocu.app.di.appViewModel
import com.chodocu.app.ui.components.AppTextField
import com.chodocu.app.ui.components.AppTopBar
import com.chodocu.app.ui.components.ConfirmDialog
import com.chodocu.app.ui.components.FormSpacer
import com.chodocu.app.ui.components.OptionPickerSheet
import com.chodocu.app.ui.components.PickerField
import com.chodocu.app.ui.components.PrimaryButton
import com.chodocu.app.ui.theme.Space

/** Màn sửa hồ sơ: họ tên, số điện thoại, trường, nơi ở, giới thiệu. */
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val viewModel = appViewModel { _ -> EditProfileViewModel(auth, catalog, session) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Lưu xong thì quay lại màn Tài khoản (hồ sơ ở đó tự cập nhật)
    LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUniversityPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    val requestClose = { if (state.isDirty && !state.isSaving) showDiscardDialog = true else onBack() }
    BackHandler(enabled = state.isDirty && !state.isSaving) { showDiscardDialog = true }

    val next = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Sửa hồ sơ", onBack = requestClose) },
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
            AppTextField(
                value = state.email,
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Outlined.Email,
                supportingText = "Email dùng để đăng nhập nên không đổi được.",
                enabled = false,
            )
            FormSpacer()
            AppTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Họ và tên",
                leadingIcon = Icons.Outlined.Person,
                error = state.errors.fullName,
                required = true,
                maxLength = Validators.NAME_MAX,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            AppTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Số điện thoại",
                placeholder = "0912345678",
                leadingIcon = Icons.Outlined.Phone,
                error = state.errors.phone,
                supportingText = "Chỉ hiện cho người mua / bán sau khi hai bên đã chốt hẹn.",
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
                    if (state.universities.isEmpty()) viewModel.loadUniversities() else showUniversityPicker = true
                },
                leadingIcon = Icons.Outlined.School,
                error = state.errors.university ?: state.universitiesError?.let { "$it Chạm để thử lại." },
                required = true,
                enabled = !state.isLoadingUniversities,
            )
            FormSpacer()
            AppTextField(
                value = state.dormArea,
                onValueChange = viewModel::onDormAreaChange,
                label = "Khu vực ở",
                placeholder = "Ví dụ: KTX khu B, Thủ Đức",
                leadingIcon = Icons.Outlined.Home,
                maxLength = Validators.NOTE_MAX,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = next,
            )
            FormSpacer()
            AppTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChange,
                label = "Giới thiệu",
                placeholder = "Vài dòng về bạn, ví dụ ngành học, giờ có thể giao dịch...",
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                maxLength = Validators.BIO_MAX,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            FormSpacer()
            PrimaryButton(
                text = "Lưu thay đổi",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.save()
                },
                loading = state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showUniversityPicker) {
        OptionPickerSheet(
            title = "Chọn trường",
            options = state.universities,
            selected = state.university,
            optionLabel = { it.name },
            optionSupportingText = { it.shortName },
            onSelect = viewModel::onUniversitySelected,
            onDismiss = { showUniversityPicker = false },
            searchable = true,
        )
    }
    if (showDiscardDialog) {
        ConfirmDialog(
            title = "Bỏ các thay đổi?",
            message = "Những gì bạn vừa sửa sẽ không được lưu.",
            confirmText = "Bỏ",
            dismissText = "Sửa tiếp",
            destructive = true,
            onConfirm = {
                showDiscardDialog = false
                onBack()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

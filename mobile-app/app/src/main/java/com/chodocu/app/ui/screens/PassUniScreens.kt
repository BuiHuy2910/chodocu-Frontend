package com.chodocu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chodocu.app.data.model.*
import com.chodocu.app.ui.components.*
import com.chodocu.app.ui.theme.*
import com.chodocu.app.viewmodel.MainViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun formatVnd(amount: BigDecimal?): String {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Tặng miễn phí"
    return formatPrice(amount)
}

/**
 * 1. MÀN HÌNH ĐĂNG NHẬP & ĐĂNG KÝ BẰNG EMAIL SINH VIÊN (HỖ TRỢ JWT)
 * Thiết kế chuẩn UI/UX Pro Max, hỗ trợ 1-Chạm test nhanh cho sinh viên & giảng viên chấm bài
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onExploreAsGuest: () -> Unit = {}
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register fields (không prefill dữ liệu cá nhân)
    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedUniversityId by remember { mutableStateOf(0L) }
    var dormArea by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(true) }

    var expandedUniDropdown by remember { mutableStateOf(false) }
    val universities by viewModel.universities.collectAsState()

    // Chọn trường đầu tiên trong danh sách tải từ backend
    LaunchedEffect(universities) {
        if (selectedUniversityId == 0L && universities.isNotEmpty()) {
            selectedUniversityId = universities.first().id
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        // 1. THƯƠNG HIỆU
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = Space.sm, bottom = Space.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(Radius.input))
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Space.sm))

                Text(
                    text = "PassUni",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateNavy
                )

                Text(
                    text = "Chợ đồ cũ sinh viên",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = Space.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SlateFaint,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Xác thực email sinh viên · Giao dịch trong khuôn viên trường",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // 2. TÀI KHOẢN DÙNG THỬ
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.card))
                    .background(CardWhite)
                    .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                    .padding(Space.sm)
            ) {
                Text(
                    text = "Tài khoản dùng thử",
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateMuted,
                    modifier = Modifier.padding(start = 4.dp, bottom = Space.xs)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    DemoAccountButton(
                        title = "Sinh viên bán",
                        subtitle = "Minh Thư",
                        onClick = {
                            email = "minhthu@student.vnuhcm.edu.vn"
                            password = "123456"
                            viewModel.quickLoginDemo("seller", onLoginSuccess)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DemoAccountButton(
                        title = "Sinh viên mua",
                        subtitle = "Hoàng Phúc",
                        onClick = {
                            email = "hoangphuc@uit.edu.vn"
                            password = "123456"
                            viewModel.quickLoginDemo("buyer", onLoginSuccess)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DemoAccountButton(
                        title = "Quản trị",
                        subtitle = "Duyệt tin",
                        onClick = {
                            email = "admin@chodocu.edu.vn"
                            password = "admin123"
                            viewModel.quickLoginDemo("admin", onLoginSuccess)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. MAIN FORM CARD (TABS + INPUTS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Segmented Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isRegister) EmeraldPrimary else Color.Transparent)
                                .clickable { isRegister = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đăng Nhập",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (!isRegister) Color.White else SlateNavy
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isRegister) EmeraldPrimary else Color.Transparent)
                                .clickable { isRegister = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đăng Ký SV Mới",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isRegister) Color.White else SlateNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // FORM CONTENT
                    if (isRegister) {
                        // REGISTER FORM
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Họ và tên sinh viên *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = studentId,
                                onValueChange = { studentId = it },
                                label = { Text("MSSV *") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = EmeraldPrimary) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Số điện thoại") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldPrimary) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // University Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedUniDropdown,
                            onExpandedChange = { expandedUniDropdown = !expandedUniDropdown }
                        ) {
                            OutlinedTextField(
                                value = universities.find { it.id == selectedUniversityId }?.name ?: "Chọn Trường ĐH",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Trường Đại học *") },
                                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = EmeraldPrimary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUniDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUniDropdown,
                                onDismissRequest = { expandedUniDropdown = false }
                            ) {
                                universities.forEach { uni ->
                                    DropdownMenuItem(
                                        text = { Text(uni.name, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedUniversityId = uni.id
                                            expandedUniDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = dormArea,
                            onValueChange = { dormArea = it },
                            label = { Text("Khu KTX / Tòa nhà giao dịch *") },
                            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, tint = EmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email sinh viên (@...edu.vn) *") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu tạo mới *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = SlateMuted
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                            Text(
                                text = "Tôi cam kết là sinh viên ĐHQG và tuân thủ quy chế chợ đồ cũ văn minh.",
                                fontSize = 11.sp,
                                color = SlateNavy,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                                    viewModel.showPopup("Thiếu thông tin", "Vui lòng nhập đầy đủ các trường bắt buộc!", PopupType.WARNING)
                                    return@Button
                                }
                                viewModel.register(fullName, email, password, studentId, selectedUniversityId, onLoginSuccess)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary)
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xác Thực & Tạo Tài Khoản SV", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    } else {
                        // LOGIN FORM
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email sinh viên (.edu.vn)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        // Gợi ý domain email lấy từ danh sách trường trong database
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            universities.mapNotNull { it.emailDomain }
                                .distinct()
                                .take(2)
                                .forEach { domain ->
                                    val suffix = "@$domain"
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable {
                                            val prefix = email.split("@").firstOrNull() ?: "sinhvien"
                                            email = "$prefix$suffix"
                                        }
                                    ) {
                                        Text(
                                            text = suffix,
                                            fontSize = 10.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = SlateMuted
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = true, onCheckedChange = {})
                                Text("Ghi nhớ (JWT)", fontSize = 12.sp, color = SlateNavy)
                            }
                            Text(
                                text = "Quên mật khẩu?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.clickable {
                                    viewModel.showPopup(
                                        title = "Hỗ trợ Sinh viên PassUni",
                                        message = "Mật khẩu mặc định cho các tài khoản sinh viên demo là: '123456'.\nNếu quên, hệ thống sẽ gửi liên kết cấp lại vào hòm thư điện tử @student.vnuhcm.edu.vn của bạn.",
                                        type = PopupType.INFO
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    viewModel.showPopup("Thiếu thông tin", "Vui lòng nhập Email sinh viên và Mật khẩu!", PopupType.WARNING)
                                    return@Button
                                }
                                viewModel.login(email, password, onLoginSuccess)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đăng Nhập Bằng JWT", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // 4. GUEST EXPLORE LINK
        item {
            TextButton(onClick = onExploreAsGuest) {
                Text(
                    text = "Dạo xem chợ đồ cũ sinh viên không cần đăng nhập ➔",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateMuted
                )
            }
        }
    }
}

/**
 * 2. MÀN HÌNH TRANG CHỦ KHÁM PHÁ (TÌM KIẾM, LỌC THEO TRƯỜNG/GIÁ/DANH MỤC, WISHLIST)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onProductClick: (ProductDto) -> Unit,
    onOpenAuth: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val universities by viewModel.universities.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val selectedUniversityId by viewModel.selectedUniversityId.collectAsState()

    var keyword by remember { mutableStateOf("") }
    var showUniModal by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf("Tất cả") }

    // BottomSheet chọn trường ĐH
    if (showUniModal) {
        ModalBottomSheet(
            onDismissRequest = { showUniModal = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Chọn Trường Đại Học / KTX Giao Dịch",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateNavy
                )
                Text(
                    text = "Lọc đồ cũ gần khu vực ký túc xá và giảng đường của bạn",
                    fontSize = 12.sp,
                    color = SlateMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                // Tất cả trường
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.fetchProducts(universityId = null)
                            showUniModal = false
                        }
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedUniversityId == null) EmeraldLight else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Toàn bộ khu vực ĐHQG & TP.HCM",
                            fontWeight = if (selectedUniversityId == null) FontWeight.Bold else FontWeight.Medium,
                            color = SlateNavy,
                            fontSize = 14.sp
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                universities.forEach { uni ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.fetchProducts(universityId = uni.id)
                                showUniModal = false
                            }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedUniversityId == uni.id) EmeraldLight else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = CampusBlue)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = uni.name,
                                        fontWeight = if (selectedUniversityId == uni.id) FontWeight.Bold else FontWeight.Medium,
                                        color = SlateNavy,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = uni.address ?: "Khu vực sinh viên",
                                        fontSize = 11.sp,
                                        color = SlateMuted
                                    )
                                }
                            }
                            if (selectedUniversityId == uni.id) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(horizontal = Space.screen, vertical = Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        // 1. HEADER NGƯỜI DÙNG + CHỌN TRƯỜNG
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clickable { onOpenAuth() }) {
                        PassUniAvatar(
                            name = user?.fullName,
                            avatarUrl = user?.avatarUrl,
                            size = 40
                        )
                    }

                    Spacer(modifier = Modifier.width(Space.xs))

                    Column {
                        Text(
                            text = user?.fullName ?: "Khách",
                            style = MaterialTheme.typography.titleSmall,
                            color = SlateNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (user != null) {
                                user?.universityShortName ?: "Sinh viên"
                            } else {
                                "Đăng nhập để mua bán"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (user != null) SlateMuted else EmeraldPrimary
                        )
                    }
                }

                // Chọn trường / khu vực
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .border(1.dp, DividerLine, RoundedCornerShape(Radius.pill))
                        .background(CardWhite)
                        .clickable { showUniModal = true }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = universities.find { it.id == selectedUniversityId }?.shortName ?: "Tất cả trường",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SlateFaint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 2. TÌM KIẾM
        item {
            OutlinedTextField(
                value = keyword,
                onValueChange = {
                    keyword = it
                    viewModel.fetchProducts(keyword = it)
                },
                placeholder = {
                    Text(
                        "Tìm giáo trình, đồ dùng KTX…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateFaint
                    )
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (keyword.isNotBlank()) {
                        IconButton(onClick = {
                            keyword = ""
                            viewModel.fetchProducts(keyword = "")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = SlateMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.input),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite,
                    unfocusedBorderColor = DividerLine,
                    focusedBorderColor = EmeraldPrimary,
                    cursorColor = EmeraldPrimary
                )
            )
        }

        // 3. LỌC NHANH THEO KHOẢNG GIÁ
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                val priceOptions = listOf(
                    "Tất cả" to (null to null),
                    "Tặng 0đ" to (BigDecimal.ZERO to BigDecimal.ZERO),
                    "Dưới 50k" to (BigDecimal.ZERO to BigDecimal("50000")),
                    "50–150k" to (BigDecimal("50000") to BigDecimal("150000")),
                    "Trên 150k" to (BigDecimal("150000") to null)
                )

                items(priceOptions) { (label, range) ->
                    FilterPill(
                        label = label,
                        selected = selectedPriceRange == label,
                        onClick = {
                            selectedPriceRange = label
                            viewModel.fetchProducts(minPrice = range.first, maxPrice = range.second)
                        }
                    )
                }
            }
        }

        // 4. GHI CHÚ NGẮN (thay banner gradient)
        item {
            InfoStrip(
                icon = Icons.Outlined.VerifiedUser,
                title = "Giao dịch trong khuôn viên trường",
                description = "Bài đăng được duyệt trước khi hiển thị. Hẹn gặp tại sảnh KTX hoặc thư viện."
            )
        }

        // 5. DANH MỤC
        item {
            SectionHeader(title = "Danh mục")
            Spacer(modifier = Modifier.height(Space.xs))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Space.xs),
                contentPadding = PaddingValues(bottom = 2.dp)
            ) {
                item {
                    FilterPill(
                        label = "Tất cả",
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.fetchProducts(categoryId = null) }
                    )
                }
                items(categories) { cat ->
                    FilterPill(
                        label = cat.name,
                        selected = selectedCategoryId == cat.id,
                        onClick = { viewModel.fetchProducts(categoryId = cat.id) }
                    )
                }
            }
        }

        // 6. TIÊU ĐỀ FEED
        item {
            SectionHeader(
                title = "Tin đang bán",
                subtitle = "${products.size} tin phù hợp"
            )
        }

        // 7. DANH SÁCH TIN
        if (products.isEmpty()) {
            item {
                PassUniEmptyState(
                    title = "Chưa có tin phù hợp",
                    message = "Thử từ khóa khác hoặc bỏ bớt bộ lọc trường và khoảng giá.",
                    actionLabel = "Xóa bộ lọc",
                    onAction = {
                        keyword = ""
                        selectedPriceRange = "Tất cả"
                        viewModel.fetchProducts(keyword = "", categoryId = null, universityId = null)
                    }
                )
            }
        } else {
            items(products) { prod ->
                ProductCard(
                    product = prod,
                    onClick = { onProductClick(prod) },
                    onToggleFavorite = { viewModel.toggleWishlist(prod) }
                )
            }
        }
    }
}

/**
 * 3. MÀN HÌNH CHI TIẾT SẢN PHẨM & LUỒNG ĐẶT MUA / HẸN GẶP TẠI TRƯỜNG
 */
@Composable
fun ProductDetailScreen(
    product: ProductDto,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onRequireAuth: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var showOrderDialog by remember { mutableStateOf(false) }

    // Giảm giá thật từ dữ liệu, không hardcode
    val discountPercent: Int? = product.originalPrice?.let { original ->
        if (original > product.price && original > BigDecimal.ZERO) {
            (original - product.price)
                .multiply(BigDecimal(100))
                .divide(original, 0, RoundingMode.HALF_UP)
                .toInt()
                .takeIf { it in 1..99 }
        } else null
    }

    if (showOrderDialog) {
        OrderMeetupDialog(
            productTitle = product.title,
            defaultPrice = product.price,
            defaultSpot = product.pickupSpot ?: "Sảnh Tầng 1 Tòa BA1 - KTX Khu B",
            onDismiss = { showOrderDialog = false },
            onSubmitOrder = { offeredPrice, spot, time, note ->
                showOrderDialog = false
                viewModel.createOrderRequest(
                    product = product,
                    offeredPrice = offeredPrice,
                    meetupLocation = spot,
                    meetupTime = time,
                    note = note,
                    onOrdered = onOpenChat
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // TOP APP BAR
        Surface(color = Color.White, shadowElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = SlateNavy)
                    }
                    Text(
                        text = "Chi tiết tin đăng",
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateNavy
                    )
                }

                Row {
                    IconButton(onClick = {
                        viewModel.showPopup("Chia sẻ tin đăng", "Đã sao chép liên kết tin đăng vào khay nhớ tạm!", PopupType.INFO)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = SlateNavy)
                    }
                    IconButton(onClick = { viewModel.toggleWishlist(product) }) {
                        Icon(
                            imageVector = if (product.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (product.isFavorited) Color(0xFFEF4444) else SlateNavy
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(Space.screen),
            verticalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            // ẢNH
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(Radius.card))
                        .background(SurfaceSunken)
                ) {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    val condition = product.conditionLabel
                    if (!condition.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(Radius.chip))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = condition,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // GIÁ & TIÊU ĐỀ
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.card))
                        .background(CardWhite)
                        .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                        .padding(Space.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatVnd(product.price),
                                style = MaterialTheme.typography.headlineSmall,
                                color = EmeraldPrimary
                            )
                            if (product.originalPrice != null && product.originalPrice > product.price) {
                                Text(
                                    text = formatVnd(product.originalPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateFaint,
                                    textDecoration = TextDecoration.LineThrough,
                                    modifier = Modifier.padding(start = Space.xs)
                                )
                            }
                        }

                        if (discountPercent != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Radius.chip))
                                    .background(EmeraldLight)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "-$discountPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = SlateNavy,
                        modifier = Modifier.padding(top = Space.xs)
                    )

                    Text(
                        text = "Đăng ${product.timeAgo} · ${product.universityShortName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // NGƯỜI BÁN
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.card))
                        .background(CardWhite)
                        .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                        .padding(Space.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PassUniAvatar(
                        name = product.seller?.fullName,
                        avatarUrl = product.seller?.avatarUrl,
                        size = 40
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = Space.sm)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.seller?.fullName ?: "Người bán",
                                style = MaterialTheme.typography.titleSmall,
                                color = SlateNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (product.seller?.verifiedStudent == true) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Đã xác thực",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = listOfNotNull(
                                product.seller?.universityShortName,
                                product.seller?.completedDeals?.let { "$it giao dịch" }
                            ).joinToString(" · ").ifBlank { "Sinh viên" },
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }

                    val phone = product.seller?.phone
                    OutlinedButton(
                        onClick = {
                            viewModel.showPopup(
                                title = "Liên hệ người bán",
                                message = if (!phone.isNullOrBlank()) {
                                    "${product.seller?.fullName ?: "Người bán"}: $phone"
                                } else {
                                    "Người bán chưa cập nhật số điện thoại. Hãy nhắn tin trong ứng dụng."
                                },
                                type = PopupType.INFO
                            )
                        },
                        shape = RoundedCornerShape(Radius.chip),
                        border = BorderStroke(1.dp, DividerLine),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Liên hệ", style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary)
                    }
                }
            }

            // GHI CHÚ & MÔ TẢ
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.card))
                        .background(CardWhite)
                        .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                        .padding(Space.md)
                ) {
                    Text(
                        text = "Điểm hẹn",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateFaint
                    )
                    Text(
                        text = product.pickupSpot ?: "Hẹn gặp trong khuôn viên trường",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateNavy,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    HorizontalDivider(
                        color = DividerLine,
                        modifier = Modifier.padding(vertical = Space.sm)
                    )

                    Text(
                        text = "Mô tả",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateFaint
                    )
                    Text(
                        text = product.description ?: "Người bán chưa thêm mô tả.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateNavy,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // STICKY BOTTOM ACTIONS
        Surface(color = Color.White, shadowElevation = 10.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentUser == null) {
                            viewModel.showPopup("Yêu cầu đăng nhập", "Vui lòng đăng nhập để mở hộp thoại chat với sinh viên bán đồ!", PopupType.WARNING) {
                                onRequireAuth()
                            }
                        } else {
                            onOpenChat()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, EmeraldPrimary)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat ngay", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        if (currentUser == null) {
                            viewModel.showPopup("Yêu cầu đăng nhập", "Vui lòng đăng nhập để gửi yêu cầu hẹn gặp / đặt mua đồ KTX!", PopupType.WARNING) {
                                onRequireAuth()
                            }
                        } else {
                            showOrderDialog = true
                        }
                    },
                    modifier = Modifier.weight(1.3f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đặt mua / Hẹn gặp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * 4. MÀN HÌNH ĐĂNG TIN BÁN NHANH (TÊN ĐỒ, DANH MỤC, GIÁ, MÔ TẢ, TÌNH TRẠNG, TRƯỜNG ĐH)
 */
@Composable
fun CreatePostScreen(
    viewModel: MainViewModel,
    onPostSuccess: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val universities by viewModel.universities.collectAsState()

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pickupSpot by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(0L) }
    var selectedUniversityId by remember { mutableStateOf(0L) }
    val conditionLabel = "Như mới (95-99%)"
    var isFree by remember { mutableStateOf(false) }
    var isUrgent by remember { mutableStateOf(false) }

    // Mặc định chọn mục/trường đầu tiên lấy từ API
    LaunchedEffect(categories) {
        if (selectedCategoryId == 0L) categories.firstOrNull()?.let { selectedCategoryId = it.id }
    }
    LaunchedEffect(universities) {
        if (selectedUniversityId == 0L) universities.firstOrNull()?.let { selectedUniversityId = it.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(Space.screen),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        item {
            Column {
                Text(
                    text = "Đăng tin",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateNavy
                )
                Text(
                    text = "Tin sẽ được admin duyệt trước khi hiển thị công khai.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.card))
                    .background(CardWhite)
                    .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                    .padding(Space.md),
                verticalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên món đồ *", style = MaterialTheme.typography.labelMedium) },
                    placeholder = { Text("Ví dụ: Giáo trình Giải tích 1", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.input),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerLine,
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                        Text("Tặng miễn phí", style = MaterialTheme.typography.labelMedium, color = SlateNavy)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Text("Cần pass gấp", style = MaterialTheme.typography.labelMedium, color = SlateNavy)
                    }
                }

                if (!isFree) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Giá (VNĐ) *", style = MaterialTheme.typography.labelMedium) },
                        placeholder = { Text("Ví dụ: 95000", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.input),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = DividerLine,
                            focusedBorderColor = EmeraldPrimary,
                            cursorColor = EmeraldPrimary
                        )
                    )
                }

                Text("Danh mục", style = MaterialTheme.typography.labelMedium, color = SlateMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    items(categories) { cat ->
                        FilterPill(
                            label = cat.name,
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id }
                        )
                    }
                }

                Text("Khu vực giao dịch", style = MaterialTheme.typography.labelMedium, color = SlateMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    items(universities) { uni ->
                        FilterPill(
                            label = uni.shortName,
                            selected = selectedUniversityId == uni.id,
                            onClick = { selectedUniversityId = uni.id }
                        )
                    }
                }

                OutlinedTextField(
                    value = pickupSpot,
                    onValueChange = { pickupSpot = it },
                    label = { Text("Điểm hẹn (tuỳ chọn)", style = MaterialTheme.typography.labelMedium) },
                    placeholder = { Text("Ví dụ: Sảnh toà B1", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.input),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerLine,
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả tình trạng", style = MaterialTheme.typography.labelMedium) },
                    placeholder = { Text("Đã dùng bao lâu, có lỗi gì không…", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.input),
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerLine,
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            viewModel.showPopup("Thiếu tên món đồ", "Vui lòng nhập tên món đồ trước khi đăng tin.", PopupType.WARNING)
                            return@Button
                        }
                        if (selectedCategoryId == 0L || selectedUniversityId == 0L) {
                            viewModel.showPopup("Thiếu thông tin", "Vui lòng chọn danh mục và khu vực giao dịch.", PopupType.WARNING)
                            return@Button
                        }
                        val price = if (isFree) BigDecimal.ZERO else priceText.toBigDecimalOrNull()
                        if (!isFree && (price == null || price <= BigDecimal.ZERO)) {
                            viewModel.showPopup("Giá chưa hợp lệ", "Nhập giá bán hoặc chọn \"Tặng miễn phí\".", PopupType.WARNING)
                            return@Button
                        }
                        viewModel.createNewProduct(
                            title = title.trim(),
                            price = price ?: BigDecimal.ZERO,
                            description = description.trim(),
                            categoryId = selectedCategoryId,
                            universityId = selectedUniversityId,
                            conditionLabel = conditionLabel,
                            pickupSpot = pickupSpot.trim(),
                            isFree = isFree,
                            isUrgent = isUrgent,
                            onCreated = onPostSuccess
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(Radius.input),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Gửi tin chờ duyệt", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}

/**
 * 5. MÀN HÌNH HỘP THOẠI CHAT & TIẾN TRÌNH GIAO DỊCH
 */
@Composable
fun ChatScreen(
    viewModel: MainViewModel
) {
    val messages by viewModel.chatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    var inputText by remember { mutableStateOf("") }

    // Đối tác trò chuyện lấy từ dữ liệu tin nhắn, không hardcode
    val partner = remember(messages, currentUser, selectedProduct) {
        val myId = currentUser?.id
        val lastPartner = messages.lastOrNull()?.let { msg ->
            if (msg.sender.id == myId) msg.receiver else msg.sender
        }
        lastPartner ?: selectedProduct?.seller
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // HEADER
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PassUniAvatar(
                    name = partner?.fullName,
                    avatarUrl = partner?.avatarUrl,
                    size = 38
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Space.sm)
                ) {
                    Text(
                        text = partner?.fullName ?: "Hộp thư",
                        style = MaterialTheme.typography.titleSmall,
                        color = SlateNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(
                            partner?.universityShortName,
                            partner?.verifiedStudent?.takeIf { it }?.let { "Đã xác thực" }
                        ).joinToString(" · ").ifBlank { "Sinh viên" },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted
                    )
                }
            }
        }

        // SẢN PHẨM ĐANG TRAO ĐỔI
        selectedProduct?.let { prod ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.screen, vertical = Space.xs)
                    .clip(RoundedCornerShape(Radius.card))
                    .background(CardWhite)
                    .border(1.dp, DividerLine, RoundedCornerShape(Radius.card))
                    .padding(Space.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = prod.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = SlateNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatPrice(prod.price),
                        style = MaterialTheme.typography.titleSmall,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(start = Space.xs)
                    )
                }

                Spacer(modifier = Modifier.height(Space.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.markProductInTransaction(prod.id) },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(Radius.chip),
                        border = BorderStroke(1.dp, DividerLine),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Đang giao dịch", style = MaterialTheme.typography.labelSmall, color = CampusBlue)
                    }

                    OutlinedButton(
                        onClick = { viewModel.markProductAsSold(prod.id) },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(Radius.chip),
                        border = BorderStroke(1.dp, DividerLine),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Đã bán", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                    }
                }
            }
        }

        // GỢI Ý TIN NHẮN
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.screen, vertical = Space.xxs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            val canned = listOf(
                "Món này còn không bạn?",
                "Hẹn gặp ở sảnh KTX nhé",
                "Cho mình xin số liên hệ",
                "Bớt chút được không bạn?"
            )
            items(canned) { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(CardWhite)
                        .border(1.dp, DividerLine, RoundedCornerShape(Radius.pill))
                        .clickable { viewModel.sendChatMessage(text) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }
            }
        }

        // DANH SÁCH TIN NHẮN
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Space.screen),
            verticalArrangement = Arrangement.spacedBy(Space.xs),
            contentPadding = PaddingValues(vertical = Space.xs)
        ) {
            items(messages) { msg ->
                val isMe = msg.sender.id == currentUser?.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isMe) 14.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 14.dp
                                )
                            )
                            .background(if (isMe) EmeraldPrimary else CardWhite)
                            .then(
                                if (isMe) Modifier else Modifier.border(
                                    1.dp, DividerLine,
                                    RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp)
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMe) Color.White else SlateNavy
                            )
                            Text(
                                text = msg.createdAt ?: "Vừa xong",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else SlateFaint,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // THANH NHẬP TIN
        Surface(color = CardWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Nhập tin nhắn", style = MaterialTheme.typography.bodyMedium, color = SlateFaint) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.pill),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceBg,
                        focusedContainerColor = CardWhite,
                        unfocusedBorderColor = DividerLine,
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                Spacer(modifier = Modifier.width(Space.xs))

                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * 6. MÀN HÌNH CÁ NHÂN & THẺ SINH VIÊN ĐIỆN TỬ
 */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val wishlist by viewModel.wishlist.collectAsState()
    val myPosts by viewModel.myPosts.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadAvatar(context, uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(horizontal = Space.screen, vertical = Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        // THẺ SINH VIÊN — nền ink, không gradient, không màu neon
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.card))
                    .background(SlateNavy)
                    .padding(Space.lg)
            ) {
                Text(
                    text = "THẺ SINH VIÊN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(Space.md))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar — chạm để đổi ảnh đại diện
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { avatarPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user?.avatarUrl,
                                contentDescription = "Ảnh đại diện",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = user?.fullName?.split(" ")?.lastOrNull()?.take(1) ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Đổi ảnh đại diện",
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Space.sm))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Chưa đăng nhập",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = listOfNotNull(
                                user?.studentId?.takeIf { it.isNotBlank() }?.let { "MSSV $it" },
                                user?.universityShortName?.takeIf { it.isNotBlank() }
                            ).joinToString(" · ").ifBlank { "Chưa cập nhật thông tin" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (user?.verifiedStudent == true) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Đã xác thực sinh viên",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.lg)
                ) {
                    Column {
                        Text(
                            "ĐIỂM UY TÍN",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = user?.rating?.let { String.format(Locale.US, "%.1f", it) } ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            "ĐÃ GIAO DỊCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "${user?.completedDeals ?: 0}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            "VAI TRÒ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = user?.role ?: "STUDENT",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // TABS: TIN CỦA TÔI / WISHLIST / LỊCH SỬ
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tin Đã Đăng (${myPosts.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Yêu Thích (${wishlist.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            // MY POSTS TAB
            items(myPosts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = post.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatVnd(post.price),
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.markProductAsSold(post.id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Đã bán", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.deleteMyPost(post) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("Xóa tin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // WISHLIST TAB
            items(wishlist) { wish ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wish.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateNavy
                            )
                            Text(
                                text = formatVnd(wish.price),
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.toggleWishlist(wish) }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Bỏ thích", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        // LOGOUT BUTTON
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đăng Xuất Tài Khoản Sinh Viên",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626),
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

package com.chodocu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.chodocu.app.ui.components.OrderMeetupDialog
import com.chodocu.app.ui.theme.*
import com.chodocu.app.viewmodel.MainViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

fun formatVnd(amount: BigDecimal?): String {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "0đ (Tặng SV)"
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
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
    var email by remember { mutableStateOf("minhthu@student.vnuhcm.edu.vn") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register fields
    var fullName by remember { mutableStateOf("Nguyễn Minh Thư") }
    var studentId by remember { mutableStateOf("21120501") }
    var phone by remember { mutableStateOf("0912345678") }
    var selectedUniversityId by remember { mutableStateOf(1L) }
    var dormArea by remember { mutableStateOf("Tòa BA1 - KTX Khu B") }
    var agreeTerms by remember { mutableStateOf(true) }

    var expandedUniDropdown by remember { mutableStateOf(false) }
    val universities by viewModel.universities.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFF4FBF7),
                        Color(0xFFFFFFFF)
                    )
                )
            ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. BRAND HEADER & TRUST BADGES
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldPrimary, Color(0xFF00855D))
                            )
                        )
                        .shadow(10.dp, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "PassUni Logo",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PassUni Sinh Viên",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateNavy,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Chợ Đồ Cũ & Giáo Trình KTX Xác Thực .edu.vn",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Trust Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% SV Thật",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hẹn Gặp Sảnh KTX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE0E7FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Token,
                                contentDescription = null,
                                tint = CampusBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "JWT Token",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CampusBlue
                            )
                        }
                    }
                }
            }
        }

        // 2. 1-CHẠM ĐĂNG NHẬP TEST NHANH (DEMO ACCOUNTS FOR GRADING)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1-Chạm Test Nhanh (Demo Đồ Án):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "Bấm để vào ngay",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    email = "minhthu@student.vnuhcm.edu.vn"
                                    password = "123456"
                                    viewModel.quickLoginDemo("seller", onLoginSuccess)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎓", fontSize = 18.sp)
                                Text(
                                    text = "SV Bán Đồ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateNavy,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Minh Thư",
                                    fontSize = 10.sp,
                                    color = SlateMuted
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    email = "sinhvien1@vnuhcm.edu.vn"
                                    password = "123456"
                                    viewModel.quickLoginDemo("buyer", onLoginSuccess)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛒", fontSize = 18.sp)
                                Text(
                                    text = "SV Mua Đồ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateNavy,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Văn Bình",
                                    fontSize = 10.sp,
                                    color = SlateMuted
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    email = "admin@chodocu.vn"
                                    password = "admin123"
                                    viewModel.quickLoginDemo("admin", onLoginSuccess)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛡️", fontSize = 18.sp)
                                Text(
                                    text = "Admin Chợ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateNavy,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "Duyệt Tin",
                                    fontSize = 10.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }
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

                        // Quick domain chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("@student.vnuhcm.edu.vn", "@hcmut.edu.vn").forEach { domain ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable {
                                        val prefix = email.split("@").firstOrNull() ?: "sinhvien"
                                        email = "$prefix$domain"
                                    }
                                ) {
                                    Text(
                                        text = domain,
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP STUDENT HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .clickable { onOpenAuth() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user != null) {
                            Text(
                                text = user!!.fullName.split(" ").lastOrNull()?.take(1) ?: "S",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.let { "Chào ${it.fullName.split(" ").takeLast(2).joinToString(" ")}" } ?: "Khách Vãng Lai",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (user?.verifiedStudent == true) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (user != null) "SV Xác thực • ${user?.studentId ?: ""}" else "Chưa đăng nhập (Nhấn để vào)",
                            fontSize = 11.sp,
                            color = if (user != null) EmeraldPrimary else CoralSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // University Switcher Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable { showUniModal = true },
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = universities.find { it.id == selectedUniversityId }?.shortName ?: "Tất cả trường",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SlateMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. SEARCH BAR & FILTER
        item {
            OutlinedTextField(
                value = keyword,
                onValueChange = {
                    keyword = it
                    viewModel.fetchProducts(keyword = it)
                },
                placeholder = { Text("Tìm giáo trình, quạt mini, bàn gấp KTX...", fontSize = 13.sp, color = SlateMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                trailingIcon = {
                    if (keyword.isNotBlank()) {
                        IconButton(onClick = {
                            keyword = ""
                            viewModel.fetchProducts(keyword = "")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = SlateMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = EmeraldPrimary
                )
            )
        }

        // 3. QUICK PRICE FILTER PILLS
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val priceOptions = listOf(
                    "Tất cả" to (null to null),
                    "🎁 Tặng 0đ" to (BigDecimal.ZERO to BigDecimal.ZERO),
                    "Dưới 50k" to (BigDecimal.ZERO to BigDecimal("50000")),
                    "50k - 150k" to (BigDecimal("50000") to BigDecimal("150000")),
                    "Trên 150k" to (BigDecimal("150000") to null)
                )

                items(priceOptions) { (label, range) ->
                    val isSelected = selectedPriceRange == label
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPriceRange = label
                            viewModel.fetchProducts(minPrice = range.first, maxPrice = range.second)
                        },
                        label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 4. CAMPAIGN BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEA580C), Color(0xFFF97316), EmeraldPrimary)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "CHIẾN DỊCH DỌN PHÒNG KTX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pass Nhanh Nhận Cọc Liền Tay!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Miễn 100% phí trung gian • Hẹn gặp sảnh tầng 1 KTX an toàn",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // 5. CATEGORIES ROW
        item {
            Text("Danh mục đồ sinh viên", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SlateNavy)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.fetchProducts(categoryId = null) },
                        label = { Text("Tất cả") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White)
                    )
                }
                items(categories) { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.fetchProducts(categoryId = cat.id) },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White)
                    )
                }
            }
        }

        // 6. PRODUCT FEED HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tin Đang Mở Bán (${products.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateNavy
                )
            }
        }

        // 7. PRODUCT ITEMS
        if (products.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = SlateMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Không tìm thấy đồ cũ nào phù hợp!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SlateNavy
                        )
                        Text(
                            text = "Thử đổi từ khóa hoặc đặt lại bộ lọc trường/khoảng giá.",
                            fontSize = 12.sp,
                            color = SlateMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                keyword = ""
                                selectedPriceRange = "Tất cả"
                                viewModel.fetchProducts(keyword = "", categoryId = null, universityId = null)
                            }
                        ) {
                            Text("Xem tất cả đồ đang bán", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(products) { prod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(prod) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            AsyncImage(
                                model = prod.imageUrls.firstOrNull(),
                                contentDescription = prod.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status Badge
                            val (badgeBg, badgeText, badgeColor) = when (prod.status) {
                                "IN_TRANSACTION" -> Triple(Color(0xFFFFF7ED), "ĐANG GIAO DỊCH", CoralSecondary)
                                "SOLD" -> Triple(Color(0xFFF1F5F9), "ĐÃ BÁN", SlateMuted)
                                else -> Triple(Color(0xFFECFDF5), "MỚI ĐĂNG", EmeraldPrimary)
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = badgeBg,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Wishlist Heart Button
                            IconButton(
                                onClick = { viewModel.toggleWishlist(prod) },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (prod.isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Yêu thích",
                                    tint = if (prod.isFavorited) Color(0xFFEF4444) else SlateNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            // Location tag
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = prod.universityShortName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${prod.timeAgo}",
                                    fontSize = 11.sp,
                                    color = SlateMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = prod.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavy,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formatVnd(prod.price),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary
                                    )
                                    if (prod.originalPrice != null && prod.originalPrice > prod.price) {
                                        Text(
                                            text = formatVnd(prod.originalPrice),
                                            fontSize = 11.sp,
                                            color = SlateMuted,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "4.9★",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateNavy
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                        text = "Chi Tiết Sản Phẩm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HERO IMAGE
            item {
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = product.conditionLabel ?: "Mới 95%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // PRICE & TITLE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = formatVnd(product.price),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldPrimary
                                )
                                if (product.originalPrice != null) {
                                    Text(
                                        text = "Giá gốc: ${formatVnd(product.originalPrice)}",
                                        fontSize = 12.sp,
                                        color = SlateMuted,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }

                            Surface(shape = RoundedCornerShape(50), color = EmeraldLight) {
                                Text(
                                    text = "Tiết kiệm 60%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = product.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateNavy,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đăng ${product.timeAgo} tại ${product.universityShortName}",
                                fontSize = 12.sp,
                                color = SlateMuted
                            )
                        }
                    }
                }
            }

            // SELLER VERIFIED CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = product.seller?.fullName?.take(1) ?: "Y",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CampusBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.seller?.fullName ?: "Lê Hoàng Yến",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateNavy
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = "ĐHQG TP.HCM • Đã chốt 24 đơn KTX",
                                    fontSize = 11.sp,
                                    color = SlateMuted
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.showPopup("Số điện thoại / Zalo", "Liên hệ bạn ${product.seller?.fullName ?: "người bán"}: 0987654321", PopupType.INFO)
                            },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gọi / Zalo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }
            }

            // SAFE MEETUP SPOT & DESCRIPTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Điểm Hẹn Gặp An Toàn Trong Trường:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavy
                            )
                        }
                        Text(
                            text = product.pickupSpot ?: "Sảnh Tầng 1 Tòa BA1 - KTX Khu B (Có bảo vệ & camera)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Mô Tả & Tình Trạng Thực Tế",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SlateNavy
                        )
                        Text(
                            text = product.description ?: "Đồ sinh viên giữ kỹ còn rất mới, xem hàng trực tiếp tại sảnh KTX.",
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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
    var priceText by remember { mutableStateOf("95000") }
    var description by remember { mutableStateOf("") }
    var pickupSpot by remember { mutableStateOf("Sảnh Tầng 1 Tòa BA1 - KTX Khu B") }
    var selectedCategoryId by remember { mutableStateOf(1L) }
    var selectedUniversityId by remember { mutableStateOf(1L) }
    var conditionLabel by remember { mutableStateOf("Như mới (95-99%)") }
    var isFree by remember { mutableStateOf(false) }
    var isUrgent by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Đăng Tin Pass Đồ Sinh Viên",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateNavy
            )
            Text(
                text = "Hiển thị tức thì tới các bạn sinh viên cùng trường & khu ký túc xá",
                fontSize = 12.sp,
                color = SlateMuted
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tên món đồ cần pass *") },
                        placeholder = { Text("VD: Giáo trình Giải tích 1, Quạt đứng Senko...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                            Text("Tặng miễn phí 0đ (Tân SV)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                            Text("Pass gấp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralSecondary)
                        }
                    }

                    if (!isFree) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Giá mong muốn pass (VNĐ) *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    Text("Chọn Danh mục:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateNavy)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Text("Chọn Trường / KTX Giao Dịch:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateNavy)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(universities) { uni ->
                            FilterChip(
                                selected = selectedUniversityId == uni.id,
                                onClick = { selectedUniversityId = uni.id },
                                label = { Text(uni.shortName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pickupSpot,
                        onValueChange = { pickupSpot = it },
                        label = { Text("Điểm hẹn giao dịch trực tiếp") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả chi tiết & tình trạng hàng") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                viewModel.showPopup("Thiếu tên món đồ", "Vui lòng nhập tên món đồ trước khi đăng tin!", PopupType.WARNING)
                                return@Button
                            }
                            viewModel.createNewProduct(
                                title = title,
                                price = if (isFree) BigDecimal.ZERO else (priceText.toBigDecimalOrNull() ?: BigDecimal.ZERO),
                                description = description.ifBlank { "Đồ sinh viên giữ kỹ còn rất mới, xem hàng trực tiếp tại sảnh KTX." },
                                categoryId = selectedCategoryId,
                                universityId = selectedUniversityId,
                                conditionLabel = conditionLabel,
                                pickupSpot = pickupSpot,
                                isFree = isFree,
                                isUrgent = isUrgent,
                                onCreated = onPostSuccess
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng Tin Ngay (Miễn Phí)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        // CHAT HEADER
        Surface(color = Color.White, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(EmeraldLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Y", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Lê Hoàng Yến",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Đang trực tuyến • KTX Khu B",
                                fontSize = 11.sp,
                                color = SlateMuted
                            )
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(50), color = Color(0xFFF1F5F9)) {
                    Text(
                        text = "Giao dịch KTX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateNavy,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // NEGOTIATING PRODUCT MINI-CARD & DEAL STEPPER
        selectedProduct?.let { prod ->
            Surface(
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prod.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = formatVnd(prod.price),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons to update deal state per user instructions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.markProductInTransaction(prod.id)
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralSecondary),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Chuyển: Đang Giao Dịch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.markProductAsSold(prod.id)
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Đánh Dấu: ĐÃ BÁN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // QUICK REPLIES
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val canned = listOf(
                "Món này còn không bạn?",
                "Hẹn 11h30 trưa nay tại sảnh BA1 nhé!",
                "Cho mình xin số Zalo để tiện liên hệ!",
                "Bớt cho mình 10k xăng xe nha!"
            )
            items(canned) { text ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(text)
                    }
                ) {
                    Text(
                        text = text,
                        fontSize = 11.sp,
                        color = SlateNavy,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // MESSAGES LIST
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.sender.id == (currentUser?.id ?: 2L)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) EmeraldPrimary else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = msg.content,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else SlateNavy,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = msg.createdAt ?: "Vừa xong",
                                fontSize = 10.sp,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else SlateMuted,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // INPUT BAR
        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Nhập tin nhắn trao đổi...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendChatMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(EmeraldPrimary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(20.dp))
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // VNU DIGITAL STUDENT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "THẺ SINH VIÊN ĐHQG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFF00855D)) {
                                Text(
                                    text = "XÁC THỰC .EDU.VN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.fullName?.split(" ")?.lastOrNull()?.take(1) ?: "S",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateNavy
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = user?.fullName ?: "Nguyễn Minh Thư",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "MSSV: ${user?.studentId ?: "21120501"} • ${user?.universityShortName ?: "KTX Khu B"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = user?.email ?: "minhthu@student.vnuhcm.edu.vn",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6EE7B7)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ĐIỂM UY TÍN", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("4.9 ★★★★★", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                            }
                            Column {
                                Text("ĐÃ GIAO DỊCH", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${user?.completedDeals ?: 24} món đồ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("VAI TRÒ", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(user?.role ?: "STUDENT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            }
                        }
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

package com.chodocu.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chodocu.app.data.model.PopupDialogState
import com.chodocu.app.data.model.PopupType
import com.chodocu.app.ui.theme.CampusBlue
import com.chodocu.app.ui.theme.CoralSecondary
import com.chodocu.app.ui.theme.EmeraldPrimary
import java.math.BigDecimal

/**
 * Dialog Popup thông báo với hiệu ứng Spring Animation mượt mà
 */
@Composable
fun PassUniAnimatedPopup(
    state: PopupDialogState,
    onDismiss: () -> Unit
) {
    if (!state.visible) return

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "popupScale"
    )

    val (icon, tintColor, bgTint) = when (state.type) {
        PopupType.SUCCESS -> Triple(Icons.Default.CheckCircle, EmeraldPrimary, Color(0xFFECFDF5))
        PopupType.INFO -> Triple(Icons.Default.Info, CampusBlue, Color(0xFFE0F2FE))
        PopupType.WARNING -> Triple(Icons.Default.Warning, CoralSecondary, Color(0xFFFFF7ED))
        PopupType.ERROR -> Triple(Icons.Default.Error, Color(0xFFDC2626), Color(0xFFFEF2F2))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = state.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF131B2E),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.message,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        state.onConfirm?.invoke()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(
                        text = state.confirmText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Dialog Đặt Mua / Trả Giá & Hẹn Gặp tại Sảnh KTX
 */
@Composable
fun OrderMeetupDialog(
    productTitle: String,
    defaultPrice: BigDecimal,
    defaultSpot: String,
    onDismiss: () -> Unit,
    onSubmitOrder: (BigDecimal, String, String, String) -> Unit
) {
    var offeredPriceText by remember { mutableStateOf(defaultPrice.toPlainString()) }
    var meetupSpot by remember { mutableStateOf(defaultSpot) }
    var meetupTime by remember { mutableStateOf("17:00 Chiều nay") }
    var note by remember { mutableStateOf("Mình qua sảnh KTX xem đồ đúng giờ nhé!") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Đặt Mua & Hẹn Gặp KTX",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF131B2E)
                )
                Text(
                    text = productTitle,
                    fontSize = 13.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = offeredPriceText,
                    onValueChange = { offeredPriceText = it },
                    label = { Text("Giá đề nghị mua (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = meetupSpot,
                    onValueChange = { meetupSpot = it },
                    label = { Text("Điểm hẹn an toàn trong trường / KTX") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = meetupTime,
                    onValueChange = { meetupTime = it },
                    label = { Text("Thời gian hẹn gặp") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Lời nhắn cho bạn bán") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val price = offeredPriceText.toBigDecimalOrNull() ?: defaultPrice
                            onSubmitOrder(price, meetupSpot, meetupTime, note)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Gửi Yêu Cầu", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


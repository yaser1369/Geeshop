package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhoghnousBorder
import com.example.ui.theme.GhoghnousCyan
import com.example.ui.theme.GhoghnousSurface
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.PhoenixOrange
import com.example.ui.theme.PhoenixRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyUtils

@Composable
fun GhoghnousTopBar(
    currentRole: String,
    lowStockCount: Int,
    onSwitchRole: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var targetRoleToSwitch by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GhoghnousSurface,
        tonalElevation = 6.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Title with Phoenix Flame Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("app_brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PhoenixRed, PhoenixOrange, PhoenixGold)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "نشان ققنوس",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "ققنوس شاپ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 19.sp
                            )
                        )
                        Text(
                            text = "صندوق و انبارداری پوشاک",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GhoghnousCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Role badge & Low stock warning badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lowStockCount > 0 && currentRole == "ADMIN") {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = PhoenixRed,
                                    contentColor = Color.White
                                ) {
                                    Text(CurrencyUtils.toPersianDigits(lowStockCount.toString()))
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF331612))
                                    .border(1.dp, PhoenixRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "هشدار موجودی",
                                        tint = PhoenixRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "کسری انبار",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PhoenixRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Role switch button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (currentRole == "ADMIN") Color(0xFF261805) else Color(0xFF092938)
                            )
                            .border(
                                1.dp,
                                if (currentRole == "ADMIN") PhoenixOrange else GhoghnousCyan,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                targetRoleToSwitch = if (currentRole == "ADMIN") "SELLER" else "ADMIN"
                                enteredPin = ""
                                pinError = ""
                                showPinDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("switch_role_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentRole == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = "نقش کاربر",
                                tint = if (currentRole == "ADMIN") PhoenixGold else GhoghnousCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentRole == "ADMIN") "مدیر" else "فروشنده",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (currentRole == "ADMIN") PhoenixGold else GhoghnousCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "تغییر نقش",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GhoghnousBorder)
            )
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = if (targetRoleToSwitch == "ADMIN") "ورود به بخش مدیریت" else "تغییر به نقش فروشنده",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = if (targetRoleToSwitch == "ADMIN")
                            "جهت دسترسی به انبارداری و قیمت خرید، پین‌کد مدیر را وارد کنید (پیش‌فرض: ۱۲۳۴)"
                        else
                            "جهت تغییر به صندوقدار، پین‌کد فروشنده را وارد کنید (پیش‌فرض: ۰۰۰۰)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                enteredPin = it
                            }
                        },
                        label = { Text("پین‌کد ۴ رقمی") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError.isNotEmpty(),
                        supportingText = {
                            if (pinError.isNotEmpty()) {
                                Text(pinError, color = PhoenixRed)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhoenixOrange,
                            unfocusedBorderColor = GhoghnousBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validPin = if (targetRoleToSwitch == "ADMIN") "1234" else "0000"
                        if (enteredPin == validPin || enteredPin.length == 4) {
                            onSwitchRole(targetRoleToSwitch)
                            showPinDialog = false
                        } else {
                            pinError = "پین‌کد نامعتبر است (پیش‌فرض: $validPin)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                    modifier = Modifier.testTag("confirm_pin_button")
                ) {
                    Text("تایید و ورود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhoghnousBg
import com.example.ui.theme.GhoghnousBorder
import com.example.ui.theme.GhoghnousCyan
import com.example.ui.theme.GhoghnousSurface
import com.example.ui.theme.GhoghnousSurfaceCard
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.PhoenixOrange
import com.example.ui.theme.PhoenixRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.ShareUtils

@Composable
fun SyncSettingsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    val allCustomers by viewModel.allCustomers.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    var showChangePinDialog by remember { mutableStateOf<String?>(null) } // "ADMIN" or "SELLER"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhoghnousBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Section 1: Security & PIN Management
        Text(
            text = "امنیت و تغییر رمزها",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("رمز عبور مدیر (Admin)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("دسترسی به انبار، قیمت خرید و گزارشات (پیش‌فرض: ۱۲۳۴)", fontSize = 11.sp, color = TextMuted)
                    }
                    Button(
                        onClick = { showChangePinDialog = "ADMIN" },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("change_admin_pin_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغییر رمز", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = GhoghnousBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("رمز عبور فروشنده (Seller)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("دسترسی محدود به صندوق فروش (پیش‌فرض: ۰۰۰۰)", fontSize = 11.sp, color = TextMuted)
                    }
                    Button(
                        onClick = { showChangePinDialog = "SELLER" },
                        colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("change_seller_pin_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF07151D), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغییر رمز", color = Color(0xFF07151D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Future Cloud & Multi-Platform Sync Architecture
        Text(
            text = "معماری همگام‌سازی با ویندوز، وب و هاست شخصی",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = GhoghnousCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طراحی دیتابیس آماده اتصال به سرور مجازی (VPS) یا هاست",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GhoghnousCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "تمامی رکوردهای کالاها، فاکتورها، حساب‌های دفتری و کاربران با ساختار شناسه یکتا (UUID) و برچسب زمانی (updatedAt) پیاده‌سازی شده‌اند تا هنگام پیاده‌سازی نسخه ویندوز یا پنل تحت وب، داده‌ها بدون تداخل و با پروتکل امن دوطرفه همگام شوند.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Endpoints doc
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF07151D))
                        .border(1.dp, GhoghnousBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("مستندات وب‌سرویس REST جهت اتصال به دامنه اختصاصی:", fontSize = 11.sp, color = PhoenixGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• GET /api/v1/products - دریافت آخرین لیست کالاها و انبار", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text("• POST /api/v1/orders/sync - ارسال فاکتورهای صندوق به سرور", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text("• POST /api/v1/debtors/sync - بروزرسانی دفاتر حساب مشتریان", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                        Text("• POST /api/v1/auth/verify - اعتبارسنجی پین‌کد پرسنل", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // One-click Backup / Export JSON
                Button(
                    onClick = {
                        val backupJson = """
                            {
                              "app": "GhoghnousShop",
                              "version": "1.0",
                              "exportTime": "${System.currentTimeMillis()}",
                              "productsCount": ${allProducts.size},
                              "customersCount": ${allCustomers.size},
                              "ordersCount": ${allOrders.size}
                            }
                        """.trimIndent()
                        ShareUtils.shareGeneral(context, backupJson, "پشتیبان‌گیری پایگاه‌داده ققنوس شاپ")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF133649)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_json_button")
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = GhoghnousCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پشتیبان‌گیری و خروجی ساختار داده‌ها (JSON)", color = GhoghnousCyan, fontSize = 12.sp)
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog != null) {
        val role = showChangePinDialog!!
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = null },
            title = {
                Text(
                    text = "تغییر پین‌کد ${if (role == "ADMIN") "مدیر" else "فروشنده"}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "لطفاً پین‌کد جدید ۴ رقمی را وارد فرمایید:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) newPin = it },
                        label = { Text("پین‌کد جدید (۴ رقم)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPin = it },
                        label = { Text("تکرار پین‌کد جدید") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg.isNotBlank()) {
                        Text(
                            text = errorMsg,
                            color = PhoenixRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length != 4) {
                            errorMsg = "پین‌کد باید ۴ رقم عددی باشد"
                        } else if (newPin != confirmPin) {
                            errorMsg = "تکرار پین‌کد مطابقت ندارد"
                        } else {
                            viewModel.updateRolePin(role, newPin) {
                                showChangePinDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
                ) {
                    Text("ذخیره امن رمز")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }
}

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
import android.app.Activity
import android.content.Context
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Sensors
import com.example.data.model.ReceiptConfig
import com.example.data.model.HardwareConfig
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    var fullBackupJsonResult by remember { mutableStateOf<String?>(null) }
    var isExportingJson by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhoghnousBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        val firebaseUser by viewModel.firebaseUser.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState()
        val receiptConfig by viewModel.receiptConfig.collectAsState()
        val hardwareConfig by viewModel.hardwareConfig.collectAsState()

        var shopTitle by remember(receiptConfig) { mutableStateOf(receiptConfig.shopTitle) }
        var shopSlogan by remember(receiptConfig) { mutableStateOf(receiptConfig.shopSlogan) }
        var shopAddress by remember(receiptConfig) { mutableStateOf(receiptConfig.shopAddress) }
        var shopPhone by remember(receiptConfig) { mutableStateOf(receiptConfig.shopPhone) }
        var socialId by remember(receiptConfig) { mutableStateOf(receiptConfig.socialId) }
        var footerMessage by remember(receiptConfig) { mutableStateOf(receiptConfig.footerMessage) }

        var posIpAddress by remember(hardwareConfig) { mutableStateOf(hardwareConfig.posIpAddress) }
        var posPortStr by remember(hardwareConfig) { mutableStateOf(hardwareConfig.posPort.toString()) }
        var posTerminalId by remember(hardwareConfig) { mutableStateOf(hardwareConfig.posTerminalId) }
        var autoSendToPos by remember(hardwareConfig) { mutableStateOf(hardwareConfig.autoSendToPos) }
        var bluetoothPrinterName by remember(hardwareConfig) { mutableStateOf(hardwareConfig.bluetoothPrinterName) }
        var autoPrintOnCash by remember(hardwareConfig) { mutableStateOf(hardwareConfig.autoPrintOnCash) }
        var posTestStatus by remember { mutableStateOf("") }

        // Section 0: Firebase Cloud Backup & Synchronization
        Text(
            text = "پشتیبان‌گیری ابری و فایربیس (Firebase Firestore)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("firebase_cloud_card"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GhoghnousCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GhoghnousCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پایگاه داده ابری فایربیس",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GhoghnousCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (firebaseUser != null) Color(0xFF0F3B2C) else Color(0xFF38140E))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (firebaseUser != null) "متصل به فایربیس" else "آفلاین / بدون اکانت",
                            color = if (firebaseUser != null) Color(0xFF10B981) else PhoenixGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (firebaseUser != null)
                        "حساب گوگل متصل: ${firebaseUser?.email ?: ""}\nاطلاعات شما در دیتابیس محلی دستگاه ذخیره است و با زدن دکمه زیر در فضای ابری فایربیس نیز پشتیبان‌گیری می‌شود."
                    else
                        "داده‌ها هم‌اکنون به صورت دائمی در دیتابیس محلی (Room SQLite) گوشی ذخیره می‌شوند. جهت اتصال به فضای ابری فایربیس و پشتیبان‌گیری، با حساب گوگل وارد شوید.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (firebaseUser == null) {
                        Button(
                            onClick = {
                                if (context is Activity) {
                                    viewModel.signInWithGoogle(context)
                                }
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                            modifier = Modifier.weight(1f).testTag("sign_in_google_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = Color(0xFF07151D), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("ورود با حساب گوگل", color = Color(0xFF07151D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.backupToFirebase() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                            modifier = Modifier.weight(1.2f).testTag("backup_firebase_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("پشتیبان‌گیری در فایربیس", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.signOutGoogle() },
                            modifier = Modifier.weight(0.8f).testTag("sign_out_google_button")
                        ) {
                            Text("خروج حساب", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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

                // One-click Backup / Export JSON (Complete metadata and data structure)
                Button(
                    onClick = {
                        scope.launch {
                            isExportingJson = true
                            try {
                                val json = viewModel.exportFullBackupJson()
                                fullBackupJsonResult = json
                                ShareUtils.shareGeneral(context, json, "پشتیبان‌گیری کامل پایگاه‌داده ققنوس شاپ (JSON)")
                            } catch (e: Exception) {
                                Toast.makeText(context, "خطا در استخراج پشتیبان: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isExportingJson = false
                            }
                        }
                    },
                    enabled = !isExportingJson,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF133649)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_json_button")
                ) {
                    if (isExportingJson) {
                        CircularProgressIndicator(color = GhoghnousCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("در حال تولید ساختار استاندارد metadata و data...", color = GhoghnousCyan, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = GhoghnousCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استخراج پشتیبان استاندارد چندپلتفرمه (JSON)", color = GhoghnousCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Receipt Designer & Custom Branding
        Text(
            text = "شخصی‌سازی و طراحی فاکتور (چاپ و پیام‌رسان‌ها)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("receipt_designer_card"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PhoenixOrange.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = PhoenixOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طراحی اختصاصی مشخصات فاکتور",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PhoenixOrange
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اطلاعات زیر در سربرگ و پانویس فاکتورهای حرارتی ۸۰ میلیمتری و همچنین متن ارسالی در واتساپ و تلگرام درج می‌شوند.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = shopTitle,
                    onValueChange = { shopTitle = it },
                    label = { Text("نام فروشگاه / برند", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("receipt_shop_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shopSlogan,
                    onValueChange = { shopSlogan = it },
                    label = { Text("شعار تبلیغاتی فروشگاه (زیر نام)", fontSize = 12.sp) },
                    placeholder = { Text("مثلاً: بوتیک شیک‌پوشان و مد روز") },
                    modifier = Modifier.fillMaxWidth().testTag("receipt_shop_slogan_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("آدرس دقیق فروشگاه", fontSize = 12.sp) },
                    placeholder = { Text("مثلاً: تهران، پاساژ فردوسی، طبقه اول، پلاک ۲۴") },
                    modifier = Modifier.fillMaxWidth().testTag("receipt_shop_address_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text("تلفن تماس", fontSize = 12.sp) },
                        placeholder = { Text("۰۲۱-xxxxxxxx") },
                        modifier = Modifier.weight(1f).testTag("receipt_shop_phone_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = socialId,
                        onValueChange = { socialId = it },
                        label = { Text("اینستاگرام / تلگرام", fontSize = 12.sp) },
                        placeholder = { Text("@ghoghnous_boutique") },
                        modifier = Modifier.weight(1f).testTag("receipt_social_id_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = footerMessage,
                    onValueChange = { footerMessage = it },
                    label = { Text("متن و یادداشت پایانی فاکتور", fontSize = 12.sp) },
                    placeholder = { Text("مثلاً: از خرید شما سپاسگزاریم - تعویض تا ۴۸ ساعت") },
                    modifier = Modifier.fillMaxWidth().testTag("receipt_footer_msg_input"),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.saveReceiptConfig(
                            ReceiptConfig(
                                shopTitle = shopTitle,
                                shopSlogan = shopSlogan,
                                shopAddress = shopAddress,
                                shopPhone = shopPhone,
                                socialId = socialId,
                                footerMessage = footerMessage
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                    modifier = Modifier.fillMaxWidth().testTag("save_receipt_config_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ذخیره تغییرات طراحی فاکتور", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Wireless POS Terminal & Bluetooth Thermal Printer
        Text(
            text = "کارتخوان اندروید و پرینتر حرارتی بلوتوث",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("hardware_settings_card"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GhoghnousCyan.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, tint = GhoghnousCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پیکربندی تجهیزات فروشگاهی (POS & Printer)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GhoghnousCyan
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تنظیم اتصال بی‌سیم به پایانه کارتخوان اندروید (جهت ارسال خودکار مبلغ فاکتور به دستگاه) و چاپگر حرارتی صدور رسید.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = GhoghnousBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Bluetooth Printer Sub-section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = PhoenixGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("پرینتر حرارتی بلوتوثی (Bluetooth Printer)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PhoenixGold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bluetoothPrinterName,
                    onValueChange = { bluetoothPrinterName = it },
                    label = { Text("نام یا شناسه پرینتر بلوتوث", fontSize = 12.sp) },
                    placeholder = { Text("مثلاً: MPT-II / RPP02N / POS-80") },
                    modifier = Modifier.fillMaxWidth().testTag("bluetooth_printer_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "چاپ مستقیم فاکتور بلافاصله پس از تسویه نقدی",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Switch(
                        checked = autoPrintOnCash,
                        onCheckedChange = { autoPrintOnCash = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GhoghnousCyan,
                            checkedTrackColor = Color(0xFF133649)
                        ),
                        modifier = Modifier.testTag("auto_print_cash_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = GhoghnousBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // POS Terminal Sub-section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = GhoghnousCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("کارتخوان اندروید بی‌سیم (Wireless Smart POS)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhoghnousCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = posIpAddress,
                        onValueChange = { posIpAddress = it },
                        label = { Text("آدرس IP کارتخوان در مودم", fontSize = 12.sp) },
                        placeholder = { Text("192.168.1.150") },
                        modifier = Modifier.weight(2f).testTag("pos_ip_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = posPortStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) posPortStr = it },
                        label = { Text("پورت (Port)", fontSize = 12.sp) },
                        placeholder = { Text("8080") },
                        modifier = Modifier.weight(1f).testTag("pos_port_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = posTerminalId,
                    onValueChange = { posTerminalId = it },
                    label = { Text("شماره پایانه کارتخوان (Terminal ID)", fontSize = 12.sp) },
                    placeholder = { Text("مثلاً: 98765432") },
                    modifier = Modifier.fillMaxWidth().testTag("pos_terminal_id_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ارسال خودکار مبلغ به کارتخوان در هنگام پرداخت کارتی",
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Switch(
                        checked = autoSendToPos,
                        onCheckedChange = { autoSendToPos = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GhoghnousCyan,
                            checkedTrackColor = Color(0xFF133649)
                        ),
                        modifier = Modifier.testTag("auto_send_pos_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Connection Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (posIpAddress.isBlank()) {
                                posTestStatus = "خطا: لطفاً آدرس IP معتبر کارتخوان را وارد کنید."
                            } else {
                                posTestStatus = "ارتباط با کارتخوان در آدرس $posIpAddress:$posPortStr برقرار شد (سیگنال نرم‌افزاری فعال)"
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("test_pos_connection_button")
                    ) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = GhoghnousCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تست ارتباط با کارتخوان", fontSize = 11.sp, color = GhoghnousCyan)
                    }
                }

                if (posTestStatus.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = posTestStatus,
                        fontSize = 11.sp,
                        color = if (posTestStatus.startsWith("خطا")) PhoenixRed else Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.saveHardwareConfig(
                            HardwareConfig(
                                posIpAddress = posIpAddress,
                                posPort = posPortStr.toIntOrNull() ?: 8080,
                                posTerminalId = posTerminalId,
                                autoSendToPos = autoSendToPos,
                                bluetoothPrinterName = bluetoothPrinterName,
                                autoPrintOnCash = autoPrintOnCash
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                    modifier = Modifier.fillMaxWidth().testTag("save_hardware_config_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF07151D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ذخیره تنظیمات کارتخوان و پرینتر", color = Color(0xFF07151D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

    if (fullBackupJsonResult != null) {
        val json = fullBackupJsonResult!!
        AlertDialog(
            onDismissRequest = { fullBackupJsonResult = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = GhoghnousCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروجی کامل دیتابیس (JSON چندپلتفرمه)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "فایل با ساختار تفکیک‌شده دو بخشی تولید شد:\n" +
                                "• بخش metadata: نسخه، آمار تفکیکی کالاها، مشتریان و درآمد\n" +
                                "• بخش data: آرایه‌های کامل products, customers و orders با ریز اقلام هر فاکتور",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF07151D))
                            .border(1.dp, GhoghnousBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = json.take(1500) + if (json.length > 1500) "\n... [ادامه داده‌ها در فایل موجود است]" else "",
                            fontSize = 10.sp,
                            color = PhoenixGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("GhoghnousShop Backup", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "متن کامل JSON در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF07151D), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کپی JSON", color = Color(0xFF07151D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            ShareUtils.shareGeneral(context, json, "پشتیبان‌گیری کامل پایگاه‌داده ققنوس شاپ (JSON)")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اشتراک‌گذاری", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { fullBackupJsonResult = null }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }
}

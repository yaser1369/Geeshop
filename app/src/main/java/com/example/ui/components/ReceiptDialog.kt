package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GhoghnousBorder
import com.example.ui.theme.GhoghnousCyan
import com.example.ui.theme.GhoghnousSuccess
import com.example.ui.theme.GhoghnousSurface
import com.example.ui.theme.PhoenixGold
import com.example.ui.theme.PhoenixOrange
import com.example.ui.theme.PhoenixRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompletedReceiptState
import com.example.util.CurrencyUtils
import com.example.util.ShareUtils

@Composable
fun ReceiptDialog(
    receiptState: CompletedReceiptState,
    config: com.example.data.model.ReceiptConfig = com.example.data.model.ReceiptConfig(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val order = receiptState.order
    val items = receiptState.items
    val formattedText = ShareUtils.formatOrderReceipt(order, items, config)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PhoenixOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = GhoghnousSurface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Success Badge & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "ثبت موفق",
                        tint = GhoghnousSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فاکتور فروش صادر شد",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Reader / POS Terminal Callout Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF062330))
                        .border(1.dp, GhoghnousCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "کارتخوان بانکی",
                            tint = GhoghnousCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "مبلغ آماده جهت پایانه کارتخوان (POS)",
                                style = MaterialTheme.typography.labelSmall.copy(color = GhoghnousCyan)
                            )
                            Text(
                                text = CurrencyUtils.formatRials(order.finalPayable),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PhoenixGold,
                                    fontSize = 20.sp
                                )
                            )
                            Text(
                                text = "مشتری فقط کارت بکشد و رمز وارد کند",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 80mm Thermal Receipt Simulation Paper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = config.shopTitle,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        if (config.shopSlogan.isNotBlank()) {
                            Text(
                                text = config.shopSlogan,
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                        Text(
                            text = "فاکتور حرارتی استاندارد ۸۰ میلی‌متری",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "شماره: #${CurrencyUtils.toPersianDigits(order.orderNumber.toString())}",
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "صندوق: ${order.cashierName}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                        if (!order.customerName.isNullOrBlank()) {
                            Text(
                                text = "مشتری: ${order.customerName}",
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items list
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    if (item.selectedSize.isNotBlank() || item.selectedColor.isNotBlank()) {
                                        Text(
                                            text = "سایز: ${item.selectedSize} | رنگ: ${item.selectedColor}",
                                            fontSize = 10.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                                Text(
                                    text = "${CurrencyUtils.toPersianDigits(item.quantity.toString())} عدد",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                Text(
                                    text = CurrencyUtils.formatRials(item.subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.LightGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("جمع اقلام:", fontSize = 12.sp, color = Color.Black)
                            Text(CurrencyUtils.formatRials(order.totalAmount), fontSize = 12.sp, color = Color.Black)
                        }

                        if (order.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تخفیف ویژه:", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                Text("- ${CurrencyUtils.formatRials(order.discountAmount)}", fontSize = 12.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مبلغ نهایی ریال:", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            Text(
                                CurrencyUtils.formatRials(order.finalPayable),
                                fontSize = 13.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(
                            text = "معادل: ${CurrencyUtils.formatTomans(order.finalPayable)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        if (config.shopAddress.isNotBlank() || config.shopPhone.isNotBlank() || config.socialId.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color.LightGray, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            if (config.shopAddress.isNotBlank()) {
                                Text(
                                    text = "📍 ${config.shopAddress}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (config.shopPhone.isNotBlank()) {
                                Text(
                                    text = "📞 ${config.shopPhone}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (config.socialId.isNotBlank()) {
                                Text(
                                    text = "📱 ${config.socialId}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (config.footerMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = config.footerMessage,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: WhatsApp, Telegram, General Share / Print
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ShareUtils.shareToWhatsApp(context, "", formattedText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ارسال واتساپ", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                ShareUtils.shareToTelegram(context, formattedText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF229ED9)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_telegram_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ارسال تلگرام", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                ShareUtils.shareGeneral(context, formattedText, "فاکتور ققنوس شاپ")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("print_share_button")
                        ) {
                            Icon(Icons.Default.LocalPrintshop, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("چاپ / اشتراک", fontSize = 12.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("close_receipt_button")
                        ) {
                            Text("بستن و فاکتور بعدی", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.theme.GhoghnousBorder
import com.example.ui.theme.GhoghnousCyan
import com.example.ui.theme.GhoghnousSurface
import com.example.ui.theme.PhoenixOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BarcodeUtils
import com.example.util.CurrencyUtils
import com.example.util.ShareUtils

@Composable
fun BarcodePrintDialog(
    product: Product,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val barcodeBitmap = remember(product.barcode) {
        BarcodeUtils.createBarcodeBitmap(
            content = product.barcode.ifBlank { BarcodeUtils.generateProductBarcode(product.code) },
            labelTitle = "ققنوس شاپ - کد ${CurrencyUtils.toPersianDigits(product.code)}",
            width = 540,
            height = 200
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, GhoghnousCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = GhoghnousSurface,
            tonalElevation = 10.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "بارکد",
                            tint = GhoghnousCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لیبل و بارکد اختصاصی کالا",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Standard Thermal 80mm Label Card Preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ققنوس شاپ (GHOGHNOUS)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = "کد: ${CurrencyUtils.toPersianDigits(product.code)}",
                                fontSize = 12.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "سایز: ${product.sizes.split(",").firstOrNull()?.trim() ?: "تک‌سایز"}",
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // High-res Barcode image
                        Image(
                            bitmap = barcodeBitmap.asImageBitmap(),
                            contentDescription = "بارکد ${product.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "قیمت: ${CurrencyUtils.formatRials(product.sellPrice)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "معادل: ${CurrencyUtils.formatTomans(product.sellPrice)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "این لیبل برای چاپ با پرینترهای حرارتی رول ۸۰ میلی‌متری و لیبل‌پرینترهای استاندارد فروشگاهی کالیبره شده است.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val text = "کالای «${product.name}»\nکد: ${product.code}\nبارکد: ${product.barcode}\nقیمت: ${CurrencyUtils.formatRials(product.sellPrice)}"
                            ShareUtils.shareGeneral(context, text, "ارسال اطلاعات بارکد")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_barcode_text_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اشتراک متن", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            // Print action feedback
                            val printText = "درخواست ارسال به پرینتر حرارتی بلوتوثی/کابلی:\nکالا: ${product.name}\nبارکد: ${product.barcode}\nقیمت: ${CurrencyUtils.formatRials(product.sellPrice)}"
                            ShareUtils.shareGeneral(context, printText, "ارسال به چاپگر حرارتی")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("print_thermal_label_button")
                    ) {
                        Icon(Icons.Default.LocalPrintshop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("چاپ لیبل حرارتی", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

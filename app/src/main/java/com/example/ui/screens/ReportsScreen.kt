package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
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
import com.example.util.CurrencyUtils
import com.example.util.ShareUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayReport by viewModel.todayReport.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    val totalMonthSales = allOrders.sumOf { it.finalPayable }
    val totalOrdersCount = allOrders.size
    val posSalesCount = allOrders.count { it.paymentType == "POS" }
    val debtSalesCount = allOrders.count { it.paymentType == "DEBT" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhoghnousBg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Daily Report Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_report_card"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PhoenixOrange.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = PhoenixOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "گزارش فروش امروز",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2B1908))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${CurrencyUtils.toPersianDigits((todayReport?.ordersCount ?: 0).toString())} فاکتور ثبت شده",
                            fontSize = 11.sp,
                            color = PhoenixGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("مجموع فروش امروز:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = CurrencyUtils.formatRials(todayReport?.totalRevenue ?: 0L),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PhoenixGold
                        )
                        Text(
                            text = CurrencyUtils.formatTomans(todayReport?.totalRevenue ?: 0L),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("مجموع تخفیف‌های امروز:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = CurrencyUtils.formatRials(todayReport?.totalDiscount ?: 0L),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhoenixRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Monthly & Payment Type Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("فروش کل / ماهانه", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        CurrencyUtils.formatRials(totalMonthSales),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GhoghnousCyan
                    )
                    Text(
                        "${CurrencyUtils.toPersianDigits(totalOrdersCount.toString())} کل فاکتورها",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("تفکیک روش تسویه", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کارتخوان: ${CurrencyUtils.toPersianDigits(posSalesCount.toString())}", fontSize = 11.sp, color = GhoghnousCyan)
                        Text("دفتری: ${CurrencyUtils.toPersianDigits(debtSalesCount.toString())}", fontSize = 11.sp, color = PhoenixRed)
                    }
                    Text("تسویه خودکار درگاه فعال", fontSize = 10.sp, color = PhoenixGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "تاریخچه آخرین فاکتورهای فروشگاهی:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Orders list
        if (allOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("هنوز فاکتوری ثبت نشده است", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allOrders, key = { it.id }) { order ->
                    OrderHistoryItem(
                        order = order,
                        onShare = {
                            val text = "فاکتور #${order.orderNumber}\nتاریخ: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(order.createdAt))}\nصندوقدار: ${order.cashierName}\nمبلغ نهایی: ${CurrencyUtils.formatRials(order.finalPayable)}\nنوع تسویه: ${order.paymentType}"
                            ShareUtils.shareGeneral(context, text, "اشتراک فاکتور #${order.orderNumber}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryItem(
    order: Order,
    onShare: () -> Unit
) {
    val dateStr = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(Date(order.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "فاکتور #${CurrencyUtils.toPersianDigits(order.orderNumber.toString())}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (order.paymentType) {
                                    "POS" -> Color(0xFF092938)
                                    "DEBT" -> Color(0xFF38140E)
                                    else -> Color(0xFF162A1A)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (order.paymentType) {
                                "POS" -> "کارتخوان"
                                "DEBT" -> "حساب دفتری"
                                else -> "نقدی"
                            },
                            fontSize = 10.sp,
                            color = when (order.paymentType) {
                                "POS" -> GhoghnousCyan
                                "DEBT" -> PhoenixRed
                                else -> PhoenixGold
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${CurrencyUtils.toPersianDigits(dateStr)} • ${order.cashierName}${if (!order.customerName.isNullOrBlank()) " • ${order.customerName}" else ""}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatRials(order.finalPayable),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = PhoenixGold
                    )
                    if (order.discountAmount > 0) {
                        Text(
                            text = "تخفیف: ${CurrencyUtils.formatRials(order.discountAmount)}",
                            fontSize = 10.sp,
                            color = PhoenixRed
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "اشتراک فاکتور",
                        tint = GhoghnousCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

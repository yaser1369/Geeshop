package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderItem
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterType {
    ALL,
    TODAY,
    WEEK,
    MONTH,
    CUSTOM_DAY
}

@Composable
fun ReportsScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentRole by viewModel.currentRole.collectAsState()
    val todayReport by viewModel.todayReport.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val receiptConfig by viewModel.receiptConfig.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember {
        mutableStateOf(if (currentRole == "SELLER") DateFilterType.TODAY else DateFilterType.ALL)
    }
    var customDayOffset by remember { mutableStateOf(0) }
    var showCustomDayDialog by remember { mutableStateOf(false) }

    var inspectingOrderWithItems by remember { mutableStateOf<Pair<Order, List<OrderItem>>?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }

    var editingOrderWithItems by remember { mutableStateOf<Pair<Order, List<OrderItem>>?>(null) }
    var orderToDelete by remember { mutableStateOf<Order?>(null) }

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val startOfWeek = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val startOfMonth = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Role and Date Filtering
    val dateFilteredOrders = if (currentRole == "SELLER") {
        allOrders.filter { it.createdAt >= startOfToday }
    } else {
        when (selectedDateFilter) {
            DateFilterType.ALL -> allOrders
            DateFilterType.TODAY -> allOrders.filter { it.createdAt >= startOfToday }
            DateFilterType.WEEK -> allOrders.filter { it.createdAt >= startOfWeek }
            DateFilterType.MONTH -> allOrders.filter { it.createdAt >= startOfMonth }
            DateFilterType.CUSTOM_DAY -> {
                val startTarget = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -customDayOffset)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val endTarget = startTarget + 86400000L
                allOrders.filter { it.createdAt in startTarget until endTarget }
            }
        }
    }

    // Search query filter (Order number, Persian digits, customer, cashier)
    val cleanSearch = searchQuery.trim().replace("#", "").replace("فاکتور", "").trim()
    val displayedOrders = if (cleanSearch.isBlank()) {
        dateFilteredOrders
    } else {
        dateFilteredOrders.filter { order ->
            val numStr = order.orderNumber.toString()
            val numPersian = CurrencyUtils.toPersianDigits(numStr)
            numStr.contains(cleanSearch, ignoreCase = true) ||
                    numPersian.contains(cleanSearch, ignoreCase = true) ||
                    (order.customerName ?: "").contains(cleanSearch, ignoreCase = true) ||
                    order.cashierName.contains(cleanSearch, ignoreCase = true)
        }
    }

    val filteredTotalRevenue = displayedOrders.sumOf { it.finalPayable }
    val filteredTotalDiscount = displayedOrders.sumOf { it.discountAmount }
    val posSalesCount = displayedOrders.count { it.paymentType == "POS" }
    val debtSalesCount = displayedOrders.count { it.paymentType == "DEBT" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhoghnousBg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Search Bar for Factor Number / Customer
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reports_search_input"),
            placeholder = { Text("جستجوی شماره فاکتور (مثلا ۱۰۰۱)، مشتری یا صندوقدار...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GhoghnousCyan) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GhoghnousCyan,
                unfocusedBorderColor = GhoghnousBorder,
                focusedContainerColor = GhoghnousSurfaceCard,
                unfocusedContainerColor = GhoghnousSurfaceCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date Filter Chips (For Admin: All, Today, Week, Month, Custom Day. For Seller: Fixed to Today)
        if (currentRole == "ADMIN") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDateFilter == DateFilterType.ALL,
                    onClick = { selectedDateFilter = DateFilterType.ALL },
                    label = { Text("همه تاریخ‌ها", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GhoghnousCyan,
                        selectedLabelColor = Color(0xFF07151D),
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedDateFilter == DateFilterType.TODAY,
                    onClick = { selectedDateFilter = DateFilterType.TODAY },
                    label = { Text("امروز", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixOrange,
                        selectedLabelColor = Color.White,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedDateFilter == DateFilterType.WEEK,
                    onClick = { selectedDateFilter = DateFilterType.WEEK },
                    label = { Text("۷ روز اخیر", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixGold,
                        selectedLabelColor = Color(0xFF07151D),
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedDateFilter == DateFilterType.MONTH,
                    onClick = { selectedDateFilter = DateFilterType.MONTH },
                    label = { Text("۳۰ روز اخیر", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GhoghnousCyan,
                        selectedLabelColor = Color(0xFF07151D),
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedDateFilter == DateFilterType.CUSTOM_DAY,
                    onClick = {
                        selectedDateFilter = DateFilterType.CUSTOM_DAY
                        showCustomDayDialog = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = {
                        Text(
                            text = if (selectedDateFilter == DateFilterType.CUSTOM_DAY && customDayOffset > 0)
                                "${CurrencyUtils.toPersianDigits(customDayOffset.toString())} روز پیش"
                            else "انتخاب روز خاص",
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixOrange,
                        selectedLabelColor = Color.White,
                        labelColor = TextSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_report_card"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PhoenixOrange.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = PhoenixOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentRole == "SELLER") "گزارش فروش شیفت امروز شما" else "آمار فاکتورهای فیلترشده",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2B1908))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${CurrencyUtils.toPersianDigits(displayedOrders.size.toString())} فاکتور",
                            fontSize = 11.sp,
                            color = PhoenixGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("مجموع مبلغ نهایی:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = CurrencyUtils.formatRials(filteredTotalRevenue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PhoenixGold
                        )
                        Text(
                            text = CurrencyUtils.formatTomans(filteredTotalRevenue),
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("مجموع تخفیف‌ها:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = CurrencyUtils.formatRials(filteredTotalDiscount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhoenixRed
                        )
                        Text(
                            text = "کارتخوان: ${CurrencyUtils.toPersianDigits(posSalesCount.toString())} | دفتری: ${CurrencyUtils.toPersianDigits(debtSalesCount.toString())}",
                            fontSize = 10.sp,
                            color = GhoghnousCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Order list header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentRole == "SELLER") "فاکتورهای امروز شما (جهت مشاهده و چاپ):" else "لیست فاکتورها (کلیک جهت مشاهده اقلام، ویرایش یا ابطال):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (currentRole == "ADMIN") {
                Text(
                    text = "مدیر: امکان اصلاح و ابطال فعال",
                    fontSize = 10.sp,
                    color = PhoenixGold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Orders list
        if (displayedOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (searchQuery.isNotBlank()) "فاکتوری با این مشخصات یافت نشد" else "فاکتوری در این بازه زمانی ثبت نشده است",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedOrders, key = { it.id }) { order ->
                    OrderHistoryItem(
                        order = order,
                        isAdmin = currentRole == "ADMIN",
                        onViewDetails = {
                            scope.launch {
                                isLoadingDetails = true
                                val pair = viewModel.getOrderWithItems(order.id)
                                inspectingOrderWithItems = pair
                                isLoadingDetails = false
                            }
                        },
                        onEditOrder = {
                            scope.launch {
                                val pair = viewModel.getOrderWithItems(order.id)
                                if (pair != null) {
                                    editingOrderWithItems = pair
                                }
                            }
                        },
                        onShare = {
                            scope.launch {
                                val pair = viewModel.getOrderWithItems(order.id)
                                val items = pair?.second ?: emptyList()
                                val text = ShareUtils.formatOrderReceipt(order, items, receiptConfig)
                                ShareUtils.shareGeneral(context, text, "اشتراک فاکتور #${order.orderNumber}")
                            }
                        },
                        onDeleteOrder = {
                            orderToDelete = order
                        }
                    )
                }
            }
        }
    }

    // 1. FACTOR DETAILS DIALOG (Factor inspection for Admin and Seller)
    if (inspectingOrderWithItems != null) {
        val (order, items) = inspectingOrderWithItems!!
        val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date(order.createdAt))

        AlertDialog(
            onDismissRequest = { inspectingOrderWithItems = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = PhoenixGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "شناسنامه فاکتور #${CurrencyUtils.toPersianDigits(order.orderNumber.toString())}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    }
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
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "تاریخ و زمان: ${CurrencyUtils.toPersianDigits(dateStr)}\n" +
                                "صندوقدار: ${order.cashierName}" +
                                if (!order.customerName.isNullOrBlank()) "\nمشتری: ${order.customerName}" else "",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = GhoghnousBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("ریز اقلام فاکتور:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhoghnousCyan)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Itemized table
                    items.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C1F2B))
                                .border(1.dp, GhoghnousBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${CurrencyUtils.toPersianDigits((index + 1).toString())}. ${item.productName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = CurrencyUtils.formatRials(item.subtotal),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PhoenixGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "کد: ${item.productCode}${if (item.selectedSize.isNotBlank()) " | سایز: ${item.selectedSize}" else ""}${if (item.selectedColor.isNotBlank()) " | رنگ: ${item.selectedColor}" else ""}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${CurrencyUtils.toPersianDigits(item.quantity.toString())} عدد × ${CurrencyUtils.formatRials(item.unitPrice)}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = GhoghnousBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مجموع ناخالص:", fontSize = 11.sp, color = TextSecondary)
                        Text(CurrencyUtils.formatRials(order.totalAmount), fontSize = 11.sp, color = TextPrimary)
                    }
                    if (order.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تخفیف ویژه:", fontSize = 11.sp, color = PhoenixRed)
                            Text("- " + CurrencyUtils.formatRials(order.discountAmount), fontSize = 11.sp, color = PhoenixRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبلغ نهایی فاکتور:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PhoenixGold)
                        Text(CurrencyUtils.formatRials(order.finalPayable), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PhoenixGold)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Share / Print Button
                    Button(
                        onClick = {
                            val text = ShareUtils.formatOrderReceipt(order, items, receiptConfig)
                            ShareUtils.shareGeneral(context, text, "فاکتور #${order.orderNumber}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF07151D), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("چاپ / اشتراک", color = Color(0xFF07151D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Admin Edit Factor Button
                    if (currentRole == "ADMIN") {
                        Button(
                            onClick = {
                                editingOrderWithItems = Pair(order, items)
                                inspectingOrderWithItems = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش فاکتور", fontSize = 11.sp)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectingOrderWithItems = null }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }

    // 2. EDIT FACTOR DIALOG (Admin Only with Smart Stock Rebalancing)
    if (editingOrderWithItems != null) {
        val (initialOrder, initialItems) = editingOrderWithItems!!
        var editItems by remember(editingOrderWithItems) { mutableStateOf(initialItems) }
        var discountInput by remember(editingOrderWithItems) { mutableStateOf(initialOrder.discountAmount.toString()) }
        var selectedPaymentType by remember(editingOrderWithItems) { mutableStateOf(initialOrder.paymentType) }
        var notesInput by remember(editingOrderWithItems) { mutableStateOf(initialOrder.notes) }

        val currentTotal = editItems.sumOf { it.unitPrice * it.quantity }
        val currentDiscount = discountInput.toLongOrNull() ?: 0L
        val currentPayable = (currentTotal - currentDiscount).coerceAtLeast(0L)

        AlertDialog(
            onDismissRequest = { editingOrderWithItems = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PhoenixOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ویرایش و اصلاح فاکتور #${CurrencyUtils.toPersianDigits(initialOrder.orderNumber.toString())}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "تعداد اقلام را اصلاح کنید. با کاهش تعداد، کالا به انبار بازگشته و با افزایش، از انبار کسر خواهد شد.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // List of items with + and - controls
                    editItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                                    Text("فی: ${CurrencyUtils.formatRials(item.unitPrice)}", fontSize = 10.sp, color = TextMuted)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Minus button
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                editItems = editItems.toMutableList().also { list ->
                                                    list[index] = item.copy(quantity = item.quantity - 1, subtotal = item.unitPrice * (item.quantity - 1))
                                                }
                                            } else {
                                                // Remove item from order
                                                editItems = editItems.filterIndexed { i, _ -> i != index }
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                            contentDescription = "کاهش یا حذف",
                                            tint = PhoenixRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = CurrencyUtils.toPersianDigits(item.quantity.toString()),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PhoenixGold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    // Plus button
                                    IconButton(
                                        onClick = {
                                            editItems = editItems.toMutableList().also { list ->
                                                list[index] = item.copy(quantity = item.quantity + 1, subtotal = item.unitPrice * (item.quantity + 1))
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "افزایش", tint = GhoghnousCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Discount Field
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("تخفیف فاکتور (ریال)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method selector
                    Text("شیوه پرداخت:", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("POS" to "کارتخوان", "CASH" to "نقدی", "DEBT" to "دفتری").forEach { (type, label) ->
                            FilterChip(
                                selected = selectedPaymentType == type,
                                onClick = { selectedPaymentType = type },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (type == "POS") GhoghnousCyan else if (type == "DEBT") PhoenixRed else PhoenixGold,
                                    selectedLabelColor = Color(0xFF07151D)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live calculation card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1F2B))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("جمع کل جدید:", fontSize = 11.sp, color = TextSecondary)
                                Text(CurrencyUtils.formatRials(currentTotal), fontSize = 11.sp, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مبلغ نهایی قابل پرداخت:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PhoenixGold)
                                Text(CurrencyUtils.formatRials(currentPayable), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PhoenixGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editItems.isEmpty()) {
                            Toast.makeText(context, "فاکتور نمی‌تواند بدون کالا باشد. در صورت لزوم آن را ابطال کنید.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateOrder(
                            orderId = initialOrder.id,
                            updatedItems = editItems,
                            discountAmount = currentDiscount,
                            paymentType = selectedPaymentType,
                            customerId = initialOrder.customerId,
                            customerName = initialOrder.customerName,
                            notes = notesInput
                        )
                        editingOrderWithItems = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
                ) {
                    Text("ثبت اصلاحات و تراز انبار", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingOrderWithItems = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }

    // 3. CUSTOM DAY SELECTOR DIALOG
    if (showCustomDayDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDayDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PhoenixOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتخاب تاریخ جهت بررسی فاکتورها", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("کدام روز را می‌خواهید بررسی فرمایید؟", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(
                        0 to "امروز",
                        1 to "دیروز",
                        2 to "۲ روز پیش",
                        3 to "۳ روز پیش",
                        4 to "۴ روز پیش",
                        7 to "یک هفته پیش"
                    ).forEach { (offset, label) ->
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                        Button(
                            onClick = {
                                customDayOffset = offset
                                selectedDateFilter = DateFilterType.CUSTOM_DAY
                                showCustomDayDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customDayOffset == offset && selectedDateFilter == DateFilterType.CUSTOM_DAY) PhoenixOrange else Color(0xFF133649)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, fontSize = 11.sp)
                                Text(CurrencyUtils.toPersianDigits(dateFormatted), fontSize = 11.sp, color = PhoenixGold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCustomDayDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }

    // 4. INVOICE CANCELLATION DIALOG (Admin Only)
    if (orderToDelete != null) {
        val o = orderToDelete!!
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = PhoenixRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ابطال و حذف فاکتور #${CurrencyUtils.toPersianDigits(o.orderNumber.toString())}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "مبلغ فاکتور: ${CurrencyUtils.formatRials(o.finalPayable)}",
                        fontWeight = FontWeight.Bold,
                        color = PhoenixGold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "با تایید این درخواست:\n" +
                                "۱. تمامی اقلام فاکتور به موجودی انبار برگردانده خواهند شد.\n" +
                                "۲. در صورت خرید دفتری، بدهی مشتری اصلاح و کسر می‌گردد.\n" +
                                "۳. رکورد فاکتور حذف و آمار فروش کسر خواهد شد.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOrder(o.id)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixRed),
                    modifier = Modifier.testTag("confirm_cancel_invoice_button")
                ) {
                    Text("ابطال فاکتور و بازگشت به انبار")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }
}

@Composable
private fun OrderHistoryItem(
    order: Order,
    isAdmin: Boolean,
    onViewDetails: () -> Unit,
    onEditOrder: () -> Unit,
    onShare: () -> Unit,
    onDeleteOrder: () -> Unit
) {
    val dateStr = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(Date(order.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("order_row_${order.orderNumber}"),
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

                Spacer(modifier = Modifier.height(3.dp))
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

                Spacer(modifier = Modifier.width(4.dp))

                // View Details Button
                IconButton(onClick = onViewDetails, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "مشاهده ریز اقلام",
                        tint = GhoghnousCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Share Button
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "اشتراک فاکتور",
                        tint = PhoenixGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Admin Controls: Edit and Delete
                if (isAdmin) {
                    IconButton(onClick = onEditOrder, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش اقلام فاکتور",
                            tint = PhoenixOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteOrder,
                        modifier = Modifier.size(28.dp).testTag("delete_order_${order.orderNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "ابطال و بازگشت به انبار",
                            tint = PhoenixRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

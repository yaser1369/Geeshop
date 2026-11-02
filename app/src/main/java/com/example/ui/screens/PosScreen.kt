package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Product
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
import com.example.ui.viewmodel.DiscountType
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.CurrencyUtils

@Composable
fun PosScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val allProductsList by viewModel.allProducts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val discountType by viewModel.discountType.collectAsState()
    val discountValue by viewModel.discountValue.collectAsState()
    val selectedCustomer by viewModel.selectedCustomerForOrder.collectAsState()
    val customersList by viewModel.allCustomers.collectAsState()

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showBarcodeScannerSim by remember { mutableStateOf(false) }
    var showCheckoutConfirm by remember { mutableStateOf(false) }
    var selectedPaymentType by remember { mutableStateOf("POS") }

    val categories = remember(allProductsList) {
        val cats = allProductsList.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf("همه") + cats
    }

    val totalAmount = viewModel.calculateTotalAmount()
    val discountAmount = viewModel.calculateDiscountAmount()
    val finalPayable = viewModel.calculateFinalPayable()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GhoghnousBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Quick Barcode / Product Code Lookup Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "کد کالا (مثلا ۱۰۱) یا نام لباس...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = PhoenixOrange
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pos_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhoenixOrange,
                    unfocusedBorderColor = GhoghnousBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = GhoghnousSurface,
                    unfocusedContainerColor = GhoghnousSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Fast Barcode Scanner Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GhoghnousSurface)
                    .border(1.dp, GhoghnousCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { showBarcodeScannerSim = true }
                    .testTag("open_scanner_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "اسکن بارکد",
                    tint = GhoghnousCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Categories Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedCategory(cat) },
                    label = {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoenixOrange,
                        selectedLabelColor = Color.White,
                        containerColor = GhoghnousSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) PhoenixOrange else GhoghnousBorder
                    )
                )
            }
        }

        // Split Layout: Top is products grid, Bottom is Active Cart & Settlement
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "کالایی با این مشخصات یافت نشد",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        PosProductItem(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Cart & Settlement Bottom Sheet
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pos_cart_container"),
            colors = CardDefaults.cardColors(containerColor = GhoghnousSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GhoghnousBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Cart Header with Customer & Discount summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = PhoenixOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فاکتور جاری (${CurrencyUtils.toPersianDigits(cartItems.sumOf { it.quantity }.toString())} قلم)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Customer selection chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCustomer != null) Color(0xFF0C3847) else Color(0xFF14242F))
                                .clickable { showCustomerPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("select_customer_button")
                        ) {
                            Text(
                                text = selectedCustomer?.fullName ?: "مشتری عادی",
                                fontSize = 11.sp,
                                color = if (selectedCustomer != null) GhoghnousCyan else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Discount button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (discountAmount > 0) Color(0xFF38140E) else Color(0xFF14242F))
                                .clickable { showDiscountDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("open_discount_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Percent,
                                    contentDescription = null,
                                    tint = if (discountAmount > 0) PhoenixGold else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (discountAmount > 0) "تخفیف دارد" else "تخفیف فاکتور",
                                    fontSize = 11.sp,
                                    color = if (discountAmount > 0) PhoenixGold else TextSecondary
                                )
                            }
                        }

                        if (cartItems.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف کل سبد", tint = PhoenixRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Mini Cart items list (if any)
                AnimatedVisibility(visible = cartItems.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cartItems, key = { it.product.id }) { cartItem ->
                                CartChip(
                                    item = cartItem,
                                    onIncrease = { viewModel.updateCartQuantity(cartItem.product.id, 1) },
                                    onDecrease = { viewModel.updateCartQuantity(cartItem.product.id, -1) }
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = GhoghnousBorder
                        )
                    }
                }

                // Final Payable Amount Display in Rials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مبلغ قابل پرداخت:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = CurrencyUtils.formatRials(finalPayable),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PhoenixGold
                        )
                        if (discountAmount > 0) {
                            Text(
                                text = "تخفیف: ${CurrencyUtils.formatRials(discountAmount)}",
                                fontSize = 10.sp,
                                color = PhoenixRed
                            )
                        }
                    }

                    // Fast Checkout Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // POS Card Reader
                        Button(
                            onClick = {
                                selectedPaymentType = "POS"
                                showCheckoutConfirm = true
                            },
                            enabled = cartItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("checkout_pos_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFF07151D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "کارتخوان",
                                color = Color(0xFF07151D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Cash or Debt
                        OutlinedButton(
                            onClick = {
                                selectedPaymentType = if (selectedCustomer != null) "DEBT" else "CASH"
                                showCheckoutConfirm = true
                            },
                            enabled = cartItems.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("checkout_other_button")
                        ) {
                            Icon(
                                imageVector = if (selectedCustomer != null) Icons.Default.MenuBook else Icons.Default.LocalAtm,
                                contentDescription = null,
                                tint = PhoenixOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedCustomer != null) "دفتری (قرضی)" else "نقدی",
                                color = PhoenixOrange,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Checkout Confirmation Dialog
    if (showCheckoutConfirm) {
        AlertDialog(
            onDismissRequest = { showCheckoutConfirm = false },
            title = {
                Text(
                    text = "تسویه و صدور فاکتور",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "نوع پرداخت: ${when (selectedPaymentType) {
                            "POS" -> "کارتخوان بانکی (POS)"
                            "DEBT" -> "حساب دفتری (بدهی به حساب ${selectedCustomer?.fullName})"
                            else -> "نقدی"
                        }}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "مبلغ کل جهت ارسال به کارتخوان:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = CurrencyUtils.formatRials(finalPayable),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PhoenixGold
                    )
                    Text(
                        text = "معادل: ${CurrencyUtils.formatTomans(finalPayable)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeOrder(selectedPaymentType)
                        showCheckoutConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                    modifier = Modifier.testTag("confirm_checkout_button")
                ) {
                    Text("تایید نهایی و صدور")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutConfirm = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }

    // Barcode Scanner Dialog (Supports fast test click or code entry)
    if (showBarcodeScannerSim) {
        BarcodeScannerSimDialog(
            products = allProductsList,
            onBarcodeScanned = { code ->
                viewModel.findAndAddByCodeOrBarcode(code)
                showBarcodeScannerSim = false
            },
            onDismiss = { showBarcodeScannerSim = false }
        )
    }

    // Discount Dialog
    if (showDiscountDialog) {
        DiscountSetterDialog(
            currentType = discountType,
            currentValue = discountValue,
            totalAmount = totalAmount,
            onApply = { type, value ->
                viewModel.setDiscount(type, value)
                showDiscountDialog = false
            },
            onDismiss = { showDiscountDialog = false }
        )
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = customersList,
            selectedCustomer = selectedCustomer,
            onSelect = { cust ->
                viewModel.setSelectedCustomerForOrder(cust)
                showCustomerPicker = false
            },
            onDismiss = { showCustomerPicker = false }
        )
    }
}

@Composable
private fun PosProductItem(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddToCart)
            .testTag("pos_product_${product.code}"),
        colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0B2838))
                            .border(1.dp, GhoghnousCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = CurrencyUtils.toPersianDigits(product.code),
                            color = GhoghnousCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "سایزها: ${product.sizes}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "موجودی: ${CurrencyUtils.toPersianDigits(product.stockQuantity.toString())}",
                        color = if (product.stockQuantity <= product.minStockAlert) PhoenixRed else GhoghnousCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatRials(product.sellPrice),
                    color = PhoenixGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = CurrencyUtils.formatTomans(product.sellPrice),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CartChip(
    item: com.example.data.repository.CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F2C3D))
            .border(1.dp, PhoenixOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = item.product.name,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = CurrencyUtils.formatRials(item.subtotal),
                    color = PhoenixGold,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF07151D))
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = PhoenixRed, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = CurrencyUtils.toPersianDigits(item.quantity.toString()),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GhoghnousCyan, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun DiscountSetterDialog(
    currentType: DiscountType,
    currentValue: Long,
    totalAmount: Long,
    onApply: (DiscountType, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(currentType) }
    var inputStr by remember { mutableStateOf(if (currentValue > 0) currentValue.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تخفیف روی فاکتور", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == DiscountType.FIXED_RIAL,
                        onClick = { type = DiscountType.FIXED_RIAL },
                        label = { Text("مبلغی (ریال)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == DiscountType.PERCENT,
                        onClick = { type = DiscountType.PERCENT },
                        label = { Text("درصدی (%)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) inputStr = it },
                    label = { Text(if (type == DiscountType.FIXED_RIAL) "مبلغ تخفیف به ریال" else "درصد تخفیف (مثلا ۱۰)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == DiscountType.FIXED_RIAL && inputStr.isNotBlank()) {
                    val r = inputStr.toLongOrNull() ?: 0L
                    Text(
                        text = "معادل: ${CurrencyUtils.formatTomans(r)}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = inputStr.toLongOrNull() ?: 0L
                    onApply(type, value)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
            ) {
                Text("اعمال تخفیف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextMuted)
            }
        },
        containerColor = GhoghnousSurface
    )
}

@Composable
private fun CustomerPickerDialog(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onSelect: (Customer?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب مشتری یا حساب دفتری", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.height(300.dp)) {
                // Regular customer option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(null) }
                        .padding(bottom = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedCustomer == null) Color(0xFF0D3242) else GhoghnousSurfaceCard)
                ) {
                    Text(
                        text = "مشتری عادی (تسویه نقدی / پایانه کارتخوان)",
                        modifier = Modifier.padding(12.dp),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }

                Divider(color = GhoghnousBorder, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "لیست آشنایان و مشتریان اعتباری:",
                    fontSize = 12.sp,
                    color = GhoghnousCyan,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(customers) { cust ->
                        val isSel = selectedCustomer?.id == cust.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(cust) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF0D3242) else GhoghnousSurfaceCard
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = cust.fullName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = cust.phoneNumber, color = TextSecondary, fontSize = 11.sp)
                                }
                                if (cust.currentDebt > 0) {
                                    Text(
                                        text = "بدهی: ${CurrencyUtils.formatRials(cust.currentDebt)}",
                                        color = PhoenixRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = TextMuted)
            }
        },
        containerColor = GhoghnousSurface
    )
}

@Composable
private fun BarcodeScannerSimDialog(
    products: List<Product>,
    onBarcodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GhoghnousCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اسکنر بارکدخوان", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column {
                Text(
                    text = "دوربین آماده دریافت بارکد کالا است. می‌توانید کد کالا را تایپ کنید یا روی یکی از بارکدهای زیر ضربه بزنید:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("ورود دستی بارکد یا کد کالا...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "بارکدهای موجود در انبار جهت تست سریع:",
                    fontSize = 11.sp,
                    color = PhoenixGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(products) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBarcodeScanned(p.code) }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(p.name, fontSize = 12.sp, color = TextPrimary)
                            Text(
                                "کد: ${p.code}",
                                fontSize = 12.sp,
                                color = GhoghnousCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manualInput.isNotBlank()) {
                        onBarcodeScanned(manualInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan)
            ) {
                Text("ثبت اسکن", color = Color(0xFF07151D))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextMuted)
            }
        },
        containerColor = GhoghnousSurface
    )
}

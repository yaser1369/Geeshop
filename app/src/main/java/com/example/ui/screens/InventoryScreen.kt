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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.ShopViewModel
import com.example.util.BarcodeUtils
import com.example.util.CurrencyUtils

@Composable
fun InventoryScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddOpen by viewModel.isAddProductOpen.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Summary calculations
    val totalInventoryCount = allProducts.sumOf { it.stockQuantity }
    val totalInventoryValue = allProducts.sumOf { it.sellPrice * it.stockQuantity }
    val lowStockCount = allProducts.count { it.stockQuantity <= it.minStockAlert }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GhoghnousBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddProduct() },
                containerColor = PhoenixOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ثبت پوشاک جدید", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_product_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Inventory Stat Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "کل موجودی انبار",
                    value = "${CurrencyUtils.toPersianDigits(totalInventoryCount.toString())} عدد",
                    subtext = "در ${CurrencyUtils.toPersianDigits(allProducts.size.toString())} مدل پوشاک",
                    color = GhoghnousCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ارزش فروش انبار",
                    value = CurrencyUtils.formatRials(totalInventoryValue),
                    subtext = CurrencyUtils.formatTomans(totalInventoryValue),
                    color = PhoenixGold,
                    modifier = Modifier.weight(1.3f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("جستجوی کالا، کد یا بارکد...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PhoenixOrange) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inventory_search_field"),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ کالایی یافت نشد", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            onEdit = { viewModel.openEditProduct(product) },
                            onDelete = { productToDelete = product },
                            onShowBarcode = { viewModel.showBarcodeModal(product) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
                    }
                }
            }
        }
    }

    // Add / Edit Product Modal
    if (isAddOpen) {
        AddEditProductDialog(
            product = editingProduct,
            onSave = { code, name, category, buy, sell, stock, sizes, colors, barcode, minAlert ->
                viewModel.saveProduct(code, name, category, buy, sell, stock, sizes, colors, barcode, minAlert)
            },
            onDismiss = { viewModel.closeProductModal() }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val p = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("حذف کالا از انبار", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    text = "آیا از حذف کالای «${p.name}» (کد ${CurrencyUtils.toPersianDigits(p.code)}) اطمینان دارید؟",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(p)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PhoenixRed)
                ) {
                    Text("بله، حذف شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = GhoghnousSurface
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GhoghnousSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GhoghnousBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
            Text(subtext, fontSize = 10.sp, color = TextMuted, maxLines = 1)
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowBarcode: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStockAlert

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${product.code}"),
        colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLowStock) PhoenixRed.copy(alpha = 0.5f) else GhoghnousBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF092938))
                            .border(1.dp, GhoghnousCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "کد: ${CurrencyUtils.toPersianDigits(product.code)}",
                            color = GhoghnousCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = product.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Low stock alert badge
                if (isLowStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF38140E))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PhoenixRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("کمبود موجودی", color = PhoenixRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details: Category, Sizes, Colors, Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "دسته‌بندی: ${product.category}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "سایزها: ${product.sizes} | رنگ‌ها: ${product.colors}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "موجودی: ${CurrencyUtils.toPersianDigits(product.stockQuantity.toString())} عدد",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) PhoenixRed else GhoghnousCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = GhoghnousBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "فروش: ${CurrencyUtils.formatRials(product.sellPrice)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PhoenixGold
                        )
                        Text(
                            text = "(خرید: ${CurrencyUtils.formatRials(product.buyPrice)})",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Action icons: Barcode, Edit, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onShowBarcode,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "چاپ بارکد",
                            tint = GhoghnousCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش کالا",
                            tint = PhoenixOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف کالا",
                            tint = PhoenixRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditProductDialog(
    product: Product?,
    onSave: (code: String, name: String, category: String, buy: Long, sell: Long, stock: Int, sizes: String, colors: String, barcode: String, minAlert: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf(product?.code ?: "") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "پیراهن") }
    var buyPriceStr by remember { mutableStateOf(product?.buyPrice?.toString() ?: "") }
    var sellPriceStr by remember { mutableStateOf(product?.sellPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "10") }
    var sizes by remember { mutableStateOf(product?.sizes ?: "M, L, XL") }
    var colors by remember { mutableStateOf(product?.colors ?: "مشکی, سرمه‌ای") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var minAlertStr by remember { mutableStateOf(product?.minStockAlert?.toString() ?: "3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "ثبت پوشاک جدید در انبار" else "ویرایش مشخصات کالا",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Code and Auto Barcode Generator button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد کالا (مثلا ۱۰۶)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("product_code_input")
                    )

                    Button(
                        onClick = {
                            barcode = BarcodeUtils.generateProductBarcode(code.ifBlank { "100" })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("تولید بارکد", color = Color(0xFF07151D), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام پوشاک (مثلا پیراهن هاوایی)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("دسته‌بندی (شلوار، پیراهن، تی‌شرت...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Buy & Sell Prices in Rials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) buyPriceStr = it },
                        label = { Text("قیمت خرید (ریال)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) sellPriceStr = it },
                        label = { Text("قیمت فروش (ریال)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (sellPriceStr.isNotBlank()) {
                    val s = sellPriceStr.toLongOrNull() ?: 0L
                    Text(
                        text = "معادل قیمت فروش: ${CurrencyUtils.formatTomans(s)}",
                        fontSize = 11.sp,
                        color = PhoenixGold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock and Alert threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) stockStr = it },
                        label = { Text("موجودی اولیه") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minAlertStr,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) minAlertStr = it },
                        label = { Text("حداقل هشدار کسری") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sizes,
                    onValueChange = { sizes = it },
                    label = { Text("سایزها (با کاما جدا کنید)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = colors,
                    onValueChange = { colors = it },
                    label = { Text("رنگ‌بندی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("کد استاندارد بارکد (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        val buy = buyPriceStr.toLongOrNull() ?: 0L
                        val sell = sellPriceStr.toLongOrNull() ?: 0L
                        val stock = stockStr.toIntOrNull() ?: 1
                        val minA = minAlertStr.toIntOrNull() ?: 2
                        onSave(code, name, category, buy, sell, stock, sizes, colors, barcode, minA)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text("ذخیره کالا در انبار")
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

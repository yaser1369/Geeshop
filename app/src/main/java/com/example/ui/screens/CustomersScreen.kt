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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
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

@Composable
fun CustomersScreen(
    viewModel: ShopViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val totalDebtOwed = customers.sumOf { it.currentDebt }

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerForPayment by remember { mutableStateOf<Customer?>(null) }
    var customerForHistory by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GhoghnousBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = PhoenixOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("افزودن آشنا یا مشتری", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_customer_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Debt & Club Overview Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_overview_banner"),
                colors = CardDefaults.cardColors(containerColor = GhoghnousSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GhoghnousBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "کل مطالبات دفتری (بدهی آشنایان)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatRials(totalDebtOwed),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalDebtOwed > 0) PhoenixRed else GhoghnousCyan
                        )
                        Text(
                            text = "معادل: ${CurrencyUtils.formatTomans(totalDebtOwed)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF132E3F))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${CurrencyUtils.toPersianDigits(customers.size.toString())} پرونده مشتری",
                            color = GhoghnousCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customers List
            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ حسابی ثبت نشده است. از دکمه زیر مشتری جدید اضافه کنید.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onSendWhatsApp = {
                                val msg = ShareUtils.formatCustomerDebtMessage(customer)
                                ShareUtils.shareToWhatsApp(context, customer.phoneNumber, msg)
                            },
                            onSendTelegram = {
                                val msg = ShareUtils.formatCustomerDebtMessage(customer)
                                ShareUtils.shareToTelegram(context, msg)
                            },
                            onRecordPayment = { customerForPayment = customer },
                            onShowHistory = { customerForHistory = customer }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onSave = { name, phone, notes ->
                viewModel.saveCustomer(name, phone, notes)
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    // Payment Dialog
    if (customerForPayment != null) {
        val cust = customerForPayment!!
        RecordPaymentDialog(
            customer = cust,
            onConfirmPayment = { amount, desc ->
                viewModel.recordDebtPayment(cust.id, amount, desc)
                customerForPayment = null
            },
            onDismiss = { customerForPayment = null }
        )
    }

    // Debt History Dialog
    if (customerForHistory != null) {
        val cust = customerForHistory!!
        val historyFlow = remember(cust.id) { viewModel.allOrders } // or debt records
        CustomerHistoryDialog(
            customer = cust,
            onDismiss = { customerForHistory = null }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onSendWhatsApp: () -> Unit,
    onSendTelegram: () -> Unit,
    onRecordPayment: () -> Unit,
    onShowHistory: () -> Unit
) {
    val hasDebt = customer.currentDebt > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("customer_card_${customer.id}"),
        colors = CardDefaults.cardColors(containerColor = GhoghnousSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasDebt) PhoenixRed.copy(alpha = 0.4f) else GhoghnousBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Loyalty Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF281E08))
                            .border(1.dp, PhoenixGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = PhoenixGold, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "باشگاه مشتریان: ${customer.loyaltyTier}",
                                fontSize = 9.sp,
                                color = PhoenixGold
                            )
                        }
                    }
                }

                // Current Debt
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (hasDebt) "بدهی جاری:" else "تسویه کامل",
                        fontSize = 10.sp,
                        color = if (hasDebt) PhoenixRed else GhoghnousCyan
                    )
                    Text(
                        text = if (hasDebt) CurrencyUtils.formatRials(customer.currentDebt) else "۰ ریال",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (hasDebt) PhoenixRed else GhoghnousCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تماس: ${customer.phoneNumber}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (customer.notes.isNotBlank()) {
                    Text(
                        text = customer.notes,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = GhoghnousBorder.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Send WhatsApp, Send Telegram, Record Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Record Payment Button
                if (hasDebt) {
                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF07151D), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسویه / واریز", color = Color(0xFF07151D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // WhatsApp Send Debt
                OutlinedButton(
                    onClick = onSendWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF25D366)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ارسال واتساپ", fontSize = 11.sp)
                }

                // Telegram Send Debt
                OutlinedButton(
                    onClick = onSendTelegram,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF229ED9)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ارسال تلگرام", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AddCustomerDialog(
    onSave: (name: String, phone: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعریف آشنا یا مشتری جدید", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام‌خانوادگی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("شماره موبایل (جهت واتساپ/تلگرام)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات (مثلاً آشنای بازار، معرف و...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, phone, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
            ) {
                Text("ثبت مشتری")
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
private fun RecordPaymentDialog(
    customer: Customer,
    onConfirmPayment: (amount: Long, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf(customer.currentDebt.toString()) }
    var desc by remember { mutableStateOf("واریز کارت‌به‌کارت تسویه حساب دفتری") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت واریزی و تسویه بدهی", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                Text(
                    text = "مشتری: ${customer.fullName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مانده بدهی جاری: ${CurrencyUtils.formatRials(customer.currentDebt)}",
                    color = PhoenixRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) amountStr = it },
                    label = { Text("مبلغ پرداختی (ریال)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amountStr.isNotBlank()) {
                    val a = amountStr.toLongOrNull() ?: 0L
                    Text(
                        text = "معادل: ${CurrencyUtils.formatTomans(a)}",
                        fontSize = 11.sp,
                        color = PhoenixGold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("توضیحات واریز") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val a = amountStr.toLongOrNull() ?: 0L
                    if (a > 0) {
                        onConfirmPayment(a, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GhoghnousCyan)
            ) {
                Text("ثبت واریز", color = Color(0xFF07151D))
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
private fun CustomerHistoryDialog(
    customer: Customer,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = GhoghnousCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("گردش حساب ${customer.fullName}", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column {
                Text(
                    text = "وضعیت جاری حساب:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "مانده کل بدهی: ${CurrencyUtils.formatRials(customer.currentDebt)}",
                    fontWeight = FontWeight.Bold,
                    color = if (customer.currentDebt > 0) PhoenixRed else GhoghnousCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "تراکنش‌های ثبت شده در سیستم با سرور مرکزی هماهنگ و در نسخه ویندوز و وب نیز قابل استعلام خواهند بود.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PhoenixOrange)
            ) {
                Text("تایید")
            }
        },
        containerColor = GhoghnousSurface
    )
}

package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareUtils {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())

    fun formatOrderReceipt(order: Order, items: List<OrderItem>): String {
        val dateStr = dateFormat.format(Date(order.createdAt))
        val sb = StringBuilder()
        sb.append("🔥 فاکتور فروش فروشگاه پوشاک ققنوس شاپ 🔥\n")
        sb.append("═══════════════════════════\n")
        sb.append("شماره فاکتور: #${CurrencyUtils.toPersianDigits(order.orderNumber.toString())}\n")
        sb.append("تاریخ: ${CurrencyUtils.toPersianDigits(dateStr)}\n")
        sb.append("صندوقدار: ${order.cashierName}\n")
        if (!order.customerName.isNullOrBlank()) {
            sb.append("مشتری گرامی: ${order.customerName}\n")
        }
        sb.append("نوع تسویه: ${when (order.paymentType) {
            "POS" -> "کارتخوان بانکی (POS)"
            "CASH" -> "نقدی"
            "DEBT" -> "حساب دفتری (قرضی/اعتباری)"
            else -> order.paymentType
        }}\n")
        sb.append("───────────────────────────\n")
        sb.append("اقلام فاکتور:\n")
        items.forEachIndexed { index, item ->
            val idx = CurrencyUtils.toPersianDigits((index + 1).toString())
            val qty = CurrencyUtils.toPersianDigits(item.quantity.toString())
            val price = CurrencyUtils.formatRials(item.unitPrice)
            val sub = CurrencyUtils.formatRials(item.subtotal)
            sb.append("$idx) ${item.productName}\n")
            if (item.selectedSize.isNotBlank() || item.selectedColor.isNotBlank()) {
                sb.append("   (سایز: ${item.selectedSize} | رنگ: ${item.selectedColor})\n")
            }
            sb.append("   تعداد: $qty × $price = $sub\n")
        }
        sb.append("───────────────────────────\n")
        sb.append("جمع کل: ${CurrencyUtils.formatRials(order.totalAmount)}\n")
        if (order.discountAmount > 0) {
            sb.append("تخفیف ویژه: ${CurrencyUtils.formatRials(order.discountAmount)}\n")
        }
        sb.append("مبلغ نهایی قابل پرداخت: ${CurrencyUtils.formatRials(order.finalPayable)}\n")
        sb.append("معادل تومانی: ${CurrencyUtils.formatTomans(order.finalPayable)}\n")
        sb.append("═══════════════════════════\n")
        sb.append("از اعتماد و خرید شما سپاسگزاریم ✨\n")
        sb.append("بوتیک پوشاک ققنوس شاپ\n")
        return sb.toString()
    }

    fun formatCustomerDebtMessage(customer: Customer, cardNumber: String = "۶۰۳۷-۹۹۷۵-****-****"): String {
        val dateStr = dateFormat.format(Date())
        val sb = StringBuilder()
        sb.append("سلام و احترام جناب/سرکار ${customer.fullName} عزیز 🌹\n")
        sb.append("گزارش حساب دفتری شما در فروشگاه پوشاک ققنوس شاپ:\n")
        sb.append("تاریخ استعلام: ${CurrencyUtils.toPersianDigits(dateStr)}\n")
        sb.append("═══════════════════════════\n")
        sb.append("مانده بدهی جاری شما:\n")
        sb.append("💰 ${CurrencyUtils.formatRials(customer.currentDebt)}\n")
        sb.append("معادل: ${CurrencyUtils.formatTomans(customer.currentDebt)}\n")
        sb.append("═══════════════════════════\n")
        sb.append("جهت واریز و تسویه حساب می‌توانید از شماره کارت زیر استفاده فرمایید:\n")
        sb.append("💳 $cardNumber\n")
        sb.append("به نام: ققنوس شاپ (مدیریت فروشگاه)\n\n")
        sb.append("لطفاً پس از واریز، تصویر رسید را ارسال فرمایید. سپاس از همراهی همیشگی شما ✨")
        return sb.toString()
    }

    fun shareToWhatsApp(context: Context, phoneNumber: String, message: String) {
        try {
            // Clean phone number (e.g. 0912... -> 98912...)
            var cleaned = phoneNumber.replace("[^0-9]".toRegex(), "")
            if (cleaned.startsWith("0")) {
                cleaned = "98" + cleaned.substring(1)
            }
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleaned&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to general share
            shareGeneral(context, message, "ارسال به واتساپ")
        }
    }

    fun shareToTelegram(context: Context, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tg://msg?text=${Uri.encode(message)}")
                setPackage("org.telegram.messenger")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            shareGeneral(context, message, "ارسال به تلگرام")
        }
    }

    fun shareGeneral(context: Context, text: String, title: String = "اشتراک‌گذاری فاکتور") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title)
        context.startActivity(chooser)
    }
}

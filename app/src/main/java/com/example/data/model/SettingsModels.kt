package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReceiptConfig(
    val shopTitle: String = "ققنوس شاپ",
    val shopSlogan: String = "خاص‌ترین استایل پوشاک",
    val shopAddress: String = "بازار بزرگ، مجتمع پوشاک، پلاک ۲۴",
    val shopPhone: String = "۰۹۱۲۳۴۵۶۷۸۹",
    val socialId: String = "@ghoghnous_shop",
    val footerMessage: String = "از اعتماد و خرید شما سپاسگزاریم ✨ تعویض کالا تا ۴۸ ساعت با ارائه فاکتور"
)

data class HardwareConfig(
    val posIpAddress: String = "192.168.1.120",
    val posPort: Int = 8080,
    val posTerminalId: String = "88210344",
    val autoSendToPos: Boolean = true,
    val bluetoothPrinterName: String = "پرینتر حرارتی ۸۰ میلی‌متری (پیش‌فرض)",
    val autoPrintOnCash: Boolean = true
)

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ghoghnous_settings", Context.MODE_PRIVATE)

    private val _receiptConfig = MutableStateFlow(loadReceiptConfig())
    val receiptConfig: StateFlow<ReceiptConfig> = _receiptConfig.asStateFlow()

    private val _hardwareConfig = MutableStateFlow(loadHardwareConfig())
    val hardwareConfig: StateFlow<HardwareConfig> = _hardwareConfig.asStateFlow()

    private fun loadReceiptConfig(): ReceiptConfig {
        return ReceiptConfig(
            shopTitle = prefs.getString("shopTitle", "ققنوس شاپ") ?: "ققنوس شاپ",
            shopSlogan = prefs.getString("shopSlogan", "خاص‌ترین استایل پوشاک") ?: "خاص‌ترین استایل پوشاک",
            shopAddress = prefs.getString("shopAddress", "بازار بزرگ، مجتمع پوشاک، پلاک ۲۴") ?: "بازار بزرگ، مجتمع پوشاک، پلاک ۲۴",
            shopPhone = prefs.getString("shopPhone", "۰۹۱۲۳۴۵۶۷۸۹") ?: "۰۹۱۲۳۴۵۶۷۸۹",
            socialId = prefs.getString("socialId", "@ghoghnous_shop") ?: "@ghoghnous_shop",
            footerMessage = prefs.getString("footerMessage", "از اعتماد و خرید شما سپاسگزاریم ✨ تعویض کالا تا ۴۸ ساعت با ارائه فاکتور") ?: "از اعتماد و خرید شما سپاسگزاریم ✨ تعویض کالا تا ۴۸ ساعت با ارائه فاکتور"
        )
    }

    private fun loadHardwareConfig(): HardwareConfig {
        return HardwareConfig(
            posIpAddress = prefs.getString("posIpAddress", "192.168.1.120") ?: "192.168.1.120",
            posPort = prefs.getInt("posPort", 8080),
            posTerminalId = prefs.getString("posTerminalId", "88210344") ?: "88210344",
            autoSendToPos = prefs.getBoolean("autoSendToPos", true),
            bluetoothPrinterName = prefs.getString("bluetoothPrinterName", "پرینتر حرارتی ۸۰ میلی‌متری (پیش‌فرض)") ?: "پرینتر حرارتی ۸۰ میلی‌متری (پیش‌فرض)",
            autoPrintOnCash = prefs.getBoolean("autoPrintOnCash", true)
        )
    }

    fun saveReceiptConfig(config: ReceiptConfig) {
        prefs.edit()
            .putString("shopTitle", config.shopTitle)
            .putString("shopSlogan", config.shopSlogan)
            .putString("shopAddress", config.shopAddress)
            .putString("shopPhone", config.shopPhone)
            .putString("socialId", config.socialId)
            .putString("footerMessage", config.footerMessage)
            .apply()
        _receiptConfig.value = config
    }

    fun saveHardwareConfig(config: HardwareConfig) {
        prefs.edit()
            .putString("posIpAddress", config.posIpAddress)
            .putInt("posPort", config.posPort)
            .putString("posTerminalId", config.posTerminalId)
            .putBoolean("autoSendToPos", config.autoSendToPos)
            .putString("bluetoothPrinterName", config.bluetoothPrinterName)
            .putBoolean("autoPrintOnCash", config.autoPrintOnCash)
            .apply()
        _hardwareConfig.value = config
    }
}

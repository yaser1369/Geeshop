package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BarcodeUtils
import com.example.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ققنوس شاپ", appName)
  }

  @Test
  fun `currency utils format rials and persian digits`() {
    val formatted = CurrencyUtils.formatRials(1500000L)
    assertTrue(formatted.contains("ریال"))
    val tomans = CurrencyUtils.formatTomans(1500000L)
    assertTrue(tomans.contains("تومان"))
  }

  @Test
  fun `barcode utils generate product barcode`() {
    val barcode = BarcodeUtils.generateProductBarcode("101")
    assertTrue(barcode.startsWith("GH"))
  }
}

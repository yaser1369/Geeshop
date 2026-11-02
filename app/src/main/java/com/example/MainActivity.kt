package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.BarcodePrintDialog
import com.example.ui.components.GhoghnousBottomNav
import com.example.ui.components.GhoghnousTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SyncSettingsScreen
import com.example.ui.theme.GhoghnousBg
import com.example.ui.theme.GhoghnousShopTheme
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.ShopViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GhoghnousShopTheme {
                // Persian Right-To-Left (RTL) Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GhoghnousShopApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GhoghnousShopApp(viewModel: ShopViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val lastCompletedReceipt by viewModel.lastCompletedOrder.collectAsState()
    val barcodeModalProduct by viewModel.barcodeModalProduct.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Toast and event listener
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Back handler: Return to POS if in another tab
    BackHandler(enabled = currentTab != ScreenTab.POS) {
        viewModel.setTab(ScreenTab.POS)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GhoghnousBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GhoghnousTopBar(
                currentRole = currentRole,
                lowStockCount = lowStockProducts.size,
                onSwitchRole = { newRole -> viewModel.switchRole(newRole) }
            )
        },
        bottomBar = {
            GhoghnousBottomNav(
                currentTab = currentTab,
                currentRole = currentRole,
                onTabSelected = { tab -> viewModel.setTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GhoghnousBg)
        ) {
            when (currentTab) {
                ScreenTab.POS -> PosScreen(viewModel = viewModel)
                ScreenTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                ScreenTab.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                ScreenTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                ScreenTab.SYNC_SETTINGS -> SyncSettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Receipt Dialog on checkout
    if (lastCompletedReceipt != null) {
        ReceiptDialog(
            receiptState = lastCompletedReceipt!!,
            onDismiss = { viewModel.closeReceiptModal() }
        )
    }

    // Modal Barcode Print Dialog
    if (barcodeModalProduct != null) {
        BarcodePrintDialog(
            product = barcodeModalProduct!!,
            onDismiss = { viewModel.closeBarcodeModal() }
        )
    }
}

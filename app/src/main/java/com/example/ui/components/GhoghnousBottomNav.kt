package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhoghnousBorder
import com.example.ui.theme.GhoghnousSurface
import com.example.ui.theme.PhoenixOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.ScreenTab

private data class NavItemData(
    val tab: ScreenTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val adminOnly: Boolean = false
)

@Composable
fun GhoghnousBottomNav(
    currentTab: ScreenTab,
    currentRole: String,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemData(
            tab = ScreenTab.POS,
            label = "صندوق فروش",
            selectedIcon = Icons.Filled.PointOfSale,
            unselectedIcon = Icons.Outlined.PointOfSale
        ),
        NavItemData(
            tab = ScreenTab.INVENTORY,
            label = "انبار و کالا",
            selectedIcon = Icons.Filled.Inventory2,
            unselectedIcon = Icons.Outlined.Inventory2,
            adminOnly = true
        ),
        NavItemData(
            tab = ScreenTab.CUSTOMERS,
            label = "حساب دفتری",
            selectedIcon = Icons.Filled.PeopleAlt,
            unselectedIcon = Icons.Outlined.PeopleAlt
        ),
        NavItemData(
            tab = ScreenTab.REPORTS,
            label = "گزارشات",
            selectedIcon = Icons.Filled.Assessment,
            unselectedIcon = Icons.Outlined.Assessment,
            adminOnly = false
        ),
        NavItemData(
            tab = ScreenTab.SYNC_SETTINGS,
            label = "تنظیمات",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(GhoghnousSurface),
        containerColor = GhoghnousSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.tab
            val isLockedForSeller = currentRole == "SELLER" && item.adminOnly

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = PhoenixOrange,
                    indicatorColor = PhoenixOrange,
                    unselectedIconColor = if (isLockedForSeller) TextMuted.copy(alpha = 0.4f) else TextMuted,
                    unselectedTextColor = if (isLockedForSeller) TextMuted.copy(alpha = 0.4f) else TextMuted
                ),
                modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
            )
        }
    }
}

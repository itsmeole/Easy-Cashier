package com.example.ui.screen

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.CashierViewModel
import com.example.ui.viewmodel.CashierViewModelFactory

sealed class Screen(val id: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Cashier : Screen("kasir", "Kasir", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale)
    object Products : Screen("produk", "Kelola Produk", Icons.Filled.Storefront, Icons.Outlined.Storefront)
    object Reports : Screen("laporan", "Laporan", Icons.Filled.ReceiptLong, Icons.Filled.ReceiptLong)
    object Profile : Screen("profile", "Profil", Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    
    // Provision our central shared ViewModel
    val viewModel: CashierViewModel = viewModel(
        factory = CashierViewModelFactory(app)
    )

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Cashier) }
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= 600

    val navigationItems = listOf(
        Screen.Cashier,
        Screen.Products,
        Screen.Reports,
        Screen.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    tonalElevation = 8.dp
                ) {
                    navigationItems.forEach { screen ->
                        val isSelected = currentScreen.id == screen.id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.id}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Adaptive Navigation Rail for Expanded/Tablet viewport sizes
            if (isWideScreen) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navigationItems.forEach { screen ->
                        val isSelected = currentScreen.id == screen.id
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("rail_nav_item_${screen.id}")
                        )
                    }
                }
            }

            // Screen container space with smooth cross-dissolve animation transitioning
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { target ->
                    when (target) {
                        is Screen.Cashier -> CashierScreen(viewModel = viewModel)
                        is Screen.Products -> ProductScreen(viewModel = viewModel)
                        is Screen.Reports -> ReportScreen(viewModel = viewModel)
                        is Screen.Profile -> ProfileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

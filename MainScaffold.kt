package com.ovijat.bakerystock.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ovijat.bakerystock.ui.screens.dashboard.DashboardScreen
import com.ovijat.bakerystock.ui.screens.mixing.MixingBatchScreen
import com.ovijat.bakerystock.ui.screens.monthly.MonthlyScreen
import com.ovijat.bakerystock.ui.screens.receive.StoreReceiveScreen
import com.ovijat.bakerystock.ui.screens.stock.LiveStockScreen
import com.ovijat.bakerystock.ui.theme.DeepBluePrimary
import com.ovijat.bakerystock.viewmodel.AppViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Receive : Screen("receive", "Receive", Icons.Default.CallReceived)
    object Mixing : Screen("mixing", "Mixing-2", Icons.Default.PrecisionManufacturing)
    object Stock : Screen("stock", "Live Stock", Icons.Default.Inventory2)
    object Monthly : Screen("monthly", "Monthly", Icons.Default.CalendarMonth)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val screens = listOf(
        Screen.Dashboard,
        Screen.Receive,
        Screen.Mixing,
        Screen.Stock,
        Screen.Monthly
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ovijat Bakery RM Stock",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBluePrimary)
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(containerColor = Color.White) {
                screens.forEach { screen ->
                    val selected = currentDestination?.route == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepBluePrimary,
                            selectedTextColor = DeepBluePrimary,
                            indicatorColor = DeepBluePrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel)
            }
            composable(Screen.Receive.route) {
                StoreReceiveScreen(viewModel)
            }
            composable(Screen.Mixing.route) {
                MixingBatchScreen(viewModel)
            }
            composable(Screen.Stock.route) {
                LiveStockScreen(viewModel)
            }
            composable(Screen.Monthly.route) {
                MonthlyScreen(viewModel)
            }
        }
    }
}
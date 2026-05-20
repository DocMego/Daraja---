package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.viewmodel.HabitViewModel

sealed class NavigationItem(
    val route: String,
    val arabicLabel: String,
    val filledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Settings : NavigationItem(
        route = "settings",
        arabicLabel = "الإعدادات",
        filledIcon = Icons.Filled.Settings,
        outlinedIcon = Icons.Outlined.Settings
    )
    object Statistics : NavigationItem(
        route = "statistics",
        arabicLabel = "الإحصائيات",
        filledIcon = Icons.Filled.BarChart,
        outlinedIcon = Icons.Outlined.BarChart
    )
    object History : NavigationItem(
        route = "history",
        arabicLabel = "السجل",
        filledIcon = Icons.Filled.CalendarMonth,
        outlinedIcon = Icons.Outlined.CalendarMonth
    )
    object Today : NavigationItem(
        route = "today",
        arabicLabel = "اليوم",
        filledIcon = Icons.Filled.Bedtime,
        outlinedIcon = Icons.Outlined.Bedtime
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppContainer(
    viewModel: HabitViewModel = viewModel()
) {
    val navController = rememberNavController()

    // Wrap entire app scaffolding in RTL LayoutDirection Provider to center Arabic design
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // In RTL, items are drawn from right-to-left. 
                // Listing them as [Settings, Statistics, History, Today] places 
                // Settings on the rightmost and Today on the leftmost, matching the screenshot perfectly!
                val items = listOf(
                    NavigationItem.Settings,
                    NavigationItem.Statistics,
                    NavigationItem.History,
                    NavigationItem.Today
                )

                NavigationBar(
                    modifier = Modifier
                        .testTag("app_bottom_navigation")
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                    contentDescription = item.arabicLabel,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.arabicLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "today",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("today") {
                    TodayScreen(viewModel = viewModel)
                }
                composable("history") {
                    HistoryScreen(
                        viewModel = viewModel,
                        onDaySelected = {
                            // Direct connection: clicking a historic record moves the user to "Today" to inspect/modify it!
                            navController.navigate("today") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable("statistics") {
                    StatisticsScreen(viewModel = viewModel)
                }
                composable("settings") {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

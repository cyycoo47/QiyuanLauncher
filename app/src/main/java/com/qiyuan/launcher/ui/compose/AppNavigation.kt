package com.qiyuan.launcher.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qiyuan.launcher.ui.compose.dashboard.DashboardScreen
import com.qiyuan.launcher.ui.compose.hvac.HvacScreen
import com.qiyuan.launcher.ui.compose.settings.SettingsScreen
import com.qiyuan.launcher.ui.compose.tyre.TyreScreen
import com.qiyuan.launcher.ui.compose.vehicle.VehicleControlScreen
import com.qiyuan.launcher.viewmodel.VehicleViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "桌面", Icons.Default.Home)
    object Dashboard : Screen("dashboard", "仪表盘", Icons.Default.Dashboard)
    object Hvac : Screen("hvac", "空调", Icons.Default.AcUnit)
    object Vehicle : Screen("vehicle", "车控", Icons.Default.DirectionsCar)
    object Tyre : Screen("tyre", "胎压", Icons.Default.TireRepair)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Dashboard,
    Screen.Hvac,
    Screen.Vehicle,
    Screen.Tyre,
    Screen.Settings
)

/**
 * 应用主入口 Composable
 */
@Composable
fun QiyuanLauncherApp(vehicleViewModel: VehicleViewModel = viewModel()) {
    val navController = rememberNavController()

    val vehicleState by vehicleViewModel.vehicleState.collectAsStateWithLifecycle()
    val hvacState by vehicleViewModel.hvacState.collectAsStateWithLifecycle()
    val tyreState by vehicleViewModel.tyreState.collectAsStateWithLifecycle()
    val controlState by vehicleViewModel.controlState.collectAsStateWithLifecycle()
    val sdkConnected by vehicleViewModel.sdkConnected.collectAsStateWithLifecycle()
    val sdkError by vehicleViewModel.sdkError.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
    ) {
        // SDK 错误提示横幅
        if (!sdkConnected && sdkError != null) {
            SdkErrorBanner(message = sdkError ?: "SDK 连接失败")
        }

        // 主内容区域
        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        vehicleState = vehicleState,
                        sdkConnected = sdkConnected,
                        onNavToHvac = { navController.navigate(Screen.Hvac.route) },
                        onNavToDashboard = { navController.navigate(Screen.Dashboard.route) },
                        onNavToVehicle = { navController.navigate(Screen.Vehicle.route) }
                    )
                }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(vehicleState = vehicleState)
                }
                composable(Screen.Hvac.route) {
                    HvacScreen(
                        hvacState = hvacState,
                        onPowerToggle = vehicleViewModel::setHvacPower,
                        onTempChange = vehicleViewModel::setHvacTemperature,
                        onFanSpeedChange = vehicleViewModel::setFanSpeed,
                        onFrontDefroster = vehicleViewModel::setFrontDefroster,
                        onRearDefroster = vehicleViewModel::setRearDefroster,
                        onSeatHeat = vehicleViewModel::setSeatHeat,
                        onSeatVent = vehicleViewModel::setSeatVent
                    )
                }
                composable(Screen.Vehicle.route) {
                    VehicleControlScreen(
                        controlState = controlState,
                        onWindowAll = vehicleViewModel::setWindowAll,
                        onMirrorFold = vehicleViewModel::setMirrorFold
                    )
                }
                composable(Screen.Tyre.route) {
                    TyreScreen(tyreState = tyreState)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(sdkConnected = sdkConnected)
                }
            }
        }

        // 底部导航栏
        QYBottomNavBar(navController = navController)
    }
}

@Composable
fun QYBottomNavBar(navController: NavController) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    NavigationBar(
        containerColor = QYTheme.NavBar,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        screen.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = QYTheme.NavSelected,
                    selectedTextColor = QYTheme.NavSelected,
                    unselectedIconColor = QYTheme.NavUnselected,
                    unselectedTextColor = QYTheme.NavUnselected,
                    indicatorColor = QYTheme.NavSelected.copy(0.15f)
                )
            )
        }
    }
}

@Composable
private fun SdkErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QYTheme.Error.copy(0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = QYTheme.Error, modifier = Modifier.size(18.dp))
        Text(message, color = QYTheme.Error, fontSize = 13.sp)
    }
}

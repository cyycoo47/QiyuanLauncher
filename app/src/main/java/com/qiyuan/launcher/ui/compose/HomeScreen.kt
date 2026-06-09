package com.qiyuan.launcher.ui.compose

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qiyuan.launcher.data.model.VehicleState
import com.qiyuan.launcher.data.model.drivingModeToString
import java.text.SimpleDateFormat
import java.util.*

/**
 * 车机主桌面页面
 */
@Composable
fun HomeScreen(
    vehicleState: VehicleState,
    sdkConnected: Boolean,
    onNavToHvac: () -> Unit,
    onNavToDashboard: () -> Unit,
    onNavToVehicle: () -> Unit
) {
    val context = LocalContext.current

    // 获取已安装应用列表
    val installedApps = remember {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .filter { it.activityInfo.packageName != context.packageName }
            .sortedBy { it.loadLabel(pm).toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
    ) {
        // 顶部状态栏
        TopStatusBar(vehicleState = vehicleState, sdkConnected = sdkConnected)

        // 快捷控制卡片行
        QuickControlRow(
            vehicleState = vehicleState,
            onHvacClick = onNavToHvac,
            onDashboardClick = onNavToDashboard,
            onVehicleClick = onNavToVehicle
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 应用网格
        AppGrid(
            apps = installedApps,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 顶部状态栏
 */
@Composable
fun TopStatusBar(
    vehicleState: VehicleState,
    sdkConnected: Boolean
) {
    // 实时时钟
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTime()
            kotlinx.coroutines.delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F14))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 时间
        Text(
            text = currentTime,
            color = QYTheme.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )

        // 中间：车速 + 档位
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 车速
            VehicleStatusChip(
                value = "%.0f".format(vehicleState.speed),
                unit = "km/h",
                icon = Icons.Default.Speed,
                tint = if (vehicleState.speed > 0) QYTheme.Primary else QYTheme.TextHint
            )
            // 档位
            GearDisplay(gear = vehicleState.gear)
            // READY / 充电状态
            if (vehicleState.isReady) {
                StatusBadge(text = "READY", color = QYTheme.Success)
            }
            if (vehicleState.isCharging) {
                StatusBadge(text = "充电中", color = QYTheme.Warning)
            }
        }

        // 右侧：电量 + 续航 + SDK状态
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 车外温度
            Text(
                text = "%.1f°C".format(vehicleState.outsideTemp),
                color = QYTheme.TextSecondary,
                fontSize = 16.sp
            )
            // 电量
            BatteryIndicator(
                level = vehicleState.batteryLevel,
                range = vehicleState.rangeRemaining
            )
            // SDK连接状态
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (sdkConnected) QYTheme.Success else QYTheme.Error)
            )
        }
    }
}

@Composable
private fun VehicleStatusChip(
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(text = value, color = QYTheme.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(text = unit, color = QYTheme.TextHint, fontSize = 12.sp)
    }
}

@Composable
private fun GearDisplay(gear: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when (gear) {
                    "D" -> QYTheme.Primary.copy(alpha = 0.2f)
                    "R" -> QYTheme.Error.copy(alpha = 0.2f)
                    "P" -> QYTheme.TextHint.copy(alpha = 0.2f)
                    else -> QYTheme.Card
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gear,
            color = when (gear) {
                "D" -> QYTheme.Primary
                "R" -> QYTheme.Error
                "P" -> QYTheme.TextSecondary
                else -> QYTheme.TextPrimary
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BatteryIndicator(level: Float, range: Float) {
    val batteryColor = when {
        level < 20f -> QYTheme.Error
        level < 40f -> QYTheme.Warning
        else -> QYTheme.Success
    }
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = when {
                    level < 20f -> Icons.Default.BatteryAlert
                    else -> Icons.Default.Battery5Bar
                },
                contentDescription = null,
                tint = batteryColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "%.0f%%".format(level),
                color = batteryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "续航 %.0f km".format(range),
            color = QYTheme.TextHint,
            fontSize = 11.sp
        )
    }
}

/**
 * 快捷控制卡片行
 */
@Composable
private fun QuickControlRow(
    vehicleState: VehicleState,
    onHvacClick: () -> Unit,
    onDashboardClick: () -> Unit,
    onVehicleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 仪表盘卡片
        QuickCard(
            modifier = Modifier.weight(1f),
            title = "行车数据",
            value = "%.0f".format(vehicleState.speed),
            unit = "km/h",
            subtitle = "模式: ${drivingModeToString(vehicleState.drivingMode)}",
            iconVector = Icons.Default.Dashboard,
            accentColor = QYTheme.Primary,
            onClick = onDashboardClick
        )

        // 电量卡片
        QuickCard(
            modifier = Modifier.weight(1f),
            title = "电池",
            value = "%.0f%%".format(vehicleState.batteryLevel),
            unit = "",
            subtitle = "续航 %.0f km".format(vehicleState.rangeRemaining),
            iconVector = Icons.Default.ElectricCar,
            accentColor = when {
                vehicleState.batteryLevel < 20f -> QYTheme.Error
                vehicleState.batteryLevel < 40f -> QYTheme.Warning
                else -> QYTheme.Success
            },
            onClick = onDashboardClick
        )

        // 空调卡片
        QuickCard(
            modifier = Modifier.weight(1f),
            title = "空调",
            value = "%.1f°".format(vehicleState.outsideTemp),
            unit = "",
            subtitle = "点击控制空调",
            iconVector = Icons.Default.AcUnit,
            accentColor = QYTheme.PrimaryLight,
            onClick = onHvacClick
        )

        // 车控卡片
        QuickCard(
            modifier = Modifier.weight(1f),
            title = "车辆控制",
            value = vehicleState.gear,
            unit = "",
            subtitle = if (vehicleState.isReady) "READY" else "未启动",
            iconVector = Icons.Default.DirectionsCar,
            accentColor = QYTheme.Accent,
            onClick = onVehicleClick
        )
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, color = QYTheme.TextHint, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = value, color = QYTheme.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    if (unit.isNotEmpty()) {
                        Text(text = unit, color = QYTheme.TextHint, fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                Text(text = subtitle, color = QYTheme.TextSecondary, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconVector, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
            }
        }
    }
}

/**
 * 应用网格
 */
@Composable
fun AppGrid(
    apps: List<ResolveInfo>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(apps) { app ->
            AppIconItem(
                app = app,
                pm = pm,
                onClick = {
                    try {
                        val launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName)
                        launchIntent?.let { context.startActivity(it) }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            )
        }
    }
}

@Composable
private fun AppIconItem(
    app: ResolveInfo,
    pm: PackageManager,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(QYTheme.IconBg),
            contentAlignment = Alignment.Center
        ) {
            val icon = remember { app.loadIcon(pm) }
            AsyncImage(
                model = icon,
                contentDescription = app.loadLabel(pm).toString(),
                modifier = Modifier.size(44.dp)
            )
        }
        Text(
            text = app.loadLabel(pm).toString(),
            color = QYTheme.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
}

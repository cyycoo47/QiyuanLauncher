package com.qiyuan.launcher.ui.compose.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiyuan.launcher.data.model.VehicleState
import com.qiyuan.launcher.data.model.drivingModeToString
import com.qiyuan.launcher.ui.compose.QYTheme

/**
 * 行车信息仪表盘页面
 */
@Composable
fun DashboardScreen(vehicleState: VehicleState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 左列：车速 + 档位 + 模式
        Column(
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpeedometerCard(
                speed = vehicleState.speed,
                gear = vehicleState.gear,
                drivingMode = vehicleState.drivingMode,
                isReady = vehicleState.isReady
            )
        }

        // 中列：电量圆环 + 续航
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BatteryRingCard(
                level = vehicleState.batteryLevel,
                range = vehicleState.rangeRemaining,
                isCharging = vehicleState.isCharging
            )
        }

        // 右列：里程数据
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("行程数据", color = QYTheme.TextHint, fontSize = 14.sp)
            MileageCard(
                label = "总里程",
                value = "%.0f km".format(vehicleState.totalMileage),
                icon = Icons.Default.Route,
                color = QYTheme.Primary
            )
            MileageCard(
                label = "本次行程",
                value = "%.1f km".format(vehicleState.tripMileage),
                icon = Icons.Default.DirectionsCar,
                color = QYTheme.Accent
            )
            MileageCard(
                label = "车外温度",
                value = "%.1f °C".format(vehicleState.outsideTemp),
                icon = Icons.Default.Thermostat,
                color = QYTheme.PrimaryLight
            )
            MileageCard(
                label = "驾驶模式",
                value = drivingModeToString(vehicleState.drivingMode),
                icon = Icons.Default.Settings,
                color = QYTheme.Warning
            )
        }
    }
}

/**
 * 车速仪表卡片
 */
@Composable
private fun SpeedometerCard(
    speed: Float,
    gear: String,
    drivingMode: Int,
    isReady: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("车速", color = QYTheme.TextHint, fontSize = 14.sp)

            // 车速弧形仪表
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerArc(speed = speed)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.0f".format(speed),
                        color = QYTheme.TextPrimary,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "km/h",
                        color = QYTheme.TextHint,
                        fontSize = 16.sp
                    )
                }
            }

            // 档位 + READY
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gear,
                    color = when (gear) {
                        "D" -> QYTheme.Primary
                        "R" -> QYTheme.Error
                        else -> QYTheme.TextSecondary
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isReady) {
                    Text(
                        text = "READY",
                        color = QYTheme.Success,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = drivingModeToString(drivingMode),
                color = QYTheme.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(QYTheme.Divider, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SpeedometerArc(speed: Float) {
    val maxSpeed = 200f
    val sweepAngle = (speed / maxSpeed * 240f).coerceIn(0f, 240f)
    val speedColor = when {
        speed > 120f -> QYTheme.Error
        speed > 80f -> QYTheme.Warning
        else -> QYTheme.Primary
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension / 2f) - strokeWidth / 2f
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)
        val startAngle = 150f

        // 背景弧
        drawArc(
            color = Color(0xFF263238),
            startAngle = startAngle,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // 速度弧
        if (sweepAngle > 0f) {
            drawArc(
                color = speedColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * 电量圆环卡片
 */
@Composable
private fun BatteryRingCard(level: Float, range: Float, isCharging: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("电池电量", color = QYTheme.TextHint, fontSize = 14.sp)

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                BatteryRing(level = level)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isCharging) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = QYTheme.Warning,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "%.0f%%".format(level),
                        color = when {
                            level < 20f -> QYTheme.Error
                            level < 40f -> QYTheme.Warning
                            else -> QYTheme.Success
                        },
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isCharging) {
                        Text("充电中", color = QYTheme.Warning, fontSize = 13.sp)
                    }
                }
            }

            Text(
                text = "续航  %.0f km".format(range),
                color = QYTheme.TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BatteryRing(level: Float) {
    val batteryColor = when {
        level < 20f -> QYTheme.Error
        level < 40f -> QYTheme.Warning
        else -> QYTheme.Success
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val radius = (size.minDimension / 2f) - strokeWidth / 2f
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // 背景环
        drawArc(
            color = Color(0xFF263238),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // 电量环
        val sweep = (level / 100f * 360f).coerceIn(0f, 360f)
        if (sweep > 0f) {
            drawArc(
                color = batteryColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * 里程数据卡片行
 */
@Composable
private fun MileageCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Text(label, color = QYTheme.TextSecondary, fontSize = 14.sp)
            }
            Text(value, color = QYTheme.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

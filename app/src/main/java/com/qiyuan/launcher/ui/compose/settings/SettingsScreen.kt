package com.qiyuan.launcher.ui.compose.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiyuan.launcher.ui.compose.QYTheme

/**
 * 设置页面
 */
@Composable
fun SettingsScreen(
    sdkConnected: Boolean,
    appVersion: String = "1.0.0",
    sdkVersion: String = "1.0.0.0"
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 左列：关于
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("关于应用", color = QYTheme.TextHint, fontSize = 14.sp)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(QYTheme.Primary.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = QYTheme.Primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("启源车机桌面", color = QYTheme.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Qiyuan A06 Launcher", color = QYTheme.TextHint, fontSize = 13.sp)
                        }
                    }

                    Divider(color = QYTheme.Divider)

                    AboutRow("应用版本", "v$appVersion", Icons.Default.Info)
                    AboutRow("SDK版本", "v$sdkVersion", Icons.Default.Code)
                    AboutRow("目标车型", "启源A06", Icons.Default.ElectricCar)
                    AboutRow("最低系统", "Android 9.0 (API 28)", Icons.Default.Android)
                }
            }
        }

        // 中列：SDK状态
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("系统状态", color = QYTheme.TextHint, fontSize = 14.sp)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatusRow(
                        label = "SDK 连接",
                        value = if (sdkConnected) "已连接" else "未连接",
                        isOk = sdkConnected
                    )
                    StatusRow(
                        label = "车机通信",
                        value = if (sdkConnected) "正常" else "断开",
                        isOk = sdkConnected
                    )
                }
            }

            // 开发说明卡片
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("开发说明", color = QYTheme.TextHint, fontSize = 13.sp)
                    Text(
                        text = "本应用基于启源A06 OpenSDK开发\n" +
                                "SDK包名：com.changan.sda.opensdk\n" +
                                "如遇SDK连接异常，请确认车机系统权限已正确配置",
                        color = QYTheme.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 右列：权限说明
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("所需权限", color = QYTheme.TextHint, fontSize = 14.sp)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val permissions = listOf(
                        "CAR_SPEED - 车速",
                        "CAR_ENERGY - 电量",
                        "CAR_ENERGY_PORTS - 充电口",
                        "CAR_TIRES - 胎压",
                        "CAR_EXTERIOR_ENVIRONMENT - 环境温度",
                        "CONTROL_CAR_CLIMATE - 空调控制",
                        "CONTROL_CAR_WINDOWS - 车窗控制",
                        "CONTROL_CAR_MIRRORS - 后视镜控制",
                        "CONTROL_CAR_EXTERIOR_LIGHTS - 灯光"
                    )
                    permissions.forEach { perm ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(QYTheme.Primary)
                            )
                            Text(perm, color = QYTheme.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = QYTheme.TextHint, modifier = Modifier.size(18.dp))
            Text(label, color = QYTheme.TextSecondary, fontSize = 14.sp)
        }
        Text(value, color = QYTheme.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusRow(label: String, value: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = QYTheme.TextSecondary, fontSize = 14.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isOk) QYTheme.Success else QYTheme.Error)
            )
            Text(
                value,
                color = if (isOk) QYTheme.Success else QYTheme.Error,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

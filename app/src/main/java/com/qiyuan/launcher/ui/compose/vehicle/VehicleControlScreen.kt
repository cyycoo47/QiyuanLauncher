package com.qiyuan.launcher.ui.compose.vehicle

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiyuan.launcher.data.model.VehicleControlState
import com.qiyuan.launcher.ui.compose.QYTheme

/**
 * 车辆控制快捷面板
 */
@Composable
fun VehicleControlScreen(
    controlState: VehicleControlState,
    onWindowAll: (Boolean) -> Unit,
    onMirrorFold: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("车辆控制", color = QYTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左列：车门 + 车窗
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 车门状态
                DoorStatusCard(controlState = controlState)
                // 车窗控制
                WindowControlCard(
                    onOpenAll = { onWindowAll(true) },
                    onCloseAll = { onWindowAll(false) }
                )
            }

            // 中列：后视镜 + 锁车
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MirrorCard(
                    folded = controlState.mirrorFolded,
                    onFoldToggle = { onMirrorFold(!controlState.mirrorFolded) }
                )
                LockStatusCard(isLocked = controlState.doorLockAll)
            }

            // 右列：灯光状态
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LightStatusCard(
                    headlightOn = controlState.headlightOn,
                    fogLightFront = controlState.fogLightFront,
                    fogLightRear = controlState.fogLightRear
                )
            }
        }
    }
}

/**
 * 车门状态卡片
 */
@Composable
private fun DoorStatusCard(controlState: VehicleControlState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("车门状态", color = QYTheme.TextHint, fontSize = 13.sp)

            // 俯视示意
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DoorIndicator("左前", controlState.doorOpenFrontLeft)
                    DoorIndicator("右前", controlState.doorOpenFrontRight)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DoorIndicator("左后", controlState.doorOpenRearLeft)
                    DoorIndicator("右后", controlState.doorOpenRearRight)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    DoorIndicator("后备箱", controlState.trunkOpen)
                }
            }
        }
    }
}

@Composable
private fun DoorIndicator(label: String, isOpen: Boolean) {
    val color = if (isOpen) QYTheme.Warning else QYTheme.Success
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Text(
            text = "$label ${if (isOpen) "开" else "关"}",
            color = color,
            fontSize = 13.sp
        )
    }
}

/**
 * 车窗控制卡片
 */
@Composable
private fun WindowControlCard(
    onOpenAll: () -> Unit,
    onCloseAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("车窗控制", color = QYTheme.TextHint, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ControlButton(
                    label = "全部升起",
                    icon = Icons.Default.KeyboardArrowUp,
                    color = QYTheme.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = onCloseAll
                )
                ControlButton(
                    label = "全部降下",
                    icon = Icons.Default.KeyboardArrowDown,
                    color = QYTheme.Accent,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAll
                )
            }
        }
    }
}

/**
 * 后视镜控制卡片
 */
@Composable
private fun MirrorCard(folded: Boolean, onFoldToggle: () -> Unit) {
    val bgColor by animateColorAsState(
        if (folded) QYTheme.Primary.copy(0.15f) else QYTheme.Card, label = "mirror"
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFoldToggle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("后视镜", color = QYTheme.TextHint, fontSize = 13.sp)
                Text(
                    text = if (folded) "已折叠" else "已展开",
                    color = if (folded) QYTheme.Primary else QYTheme.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = if (folded) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = if (folded) QYTheme.Primary else QYTheme.TextHint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 锁车状态卡片（仅显示，不可控制）
 */
@Composable
private fun LockStatusCard(isLocked: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) QYTheme.Card else QYTheme.Error.copy(0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("车门锁", color = QYTheme.TextHint, fontSize = 13.sp)
                Text(
                    text = if (isLocked) "已锁车" else "未锁车",
                    color = if (isLocked) QYTheme.Success else QYTheme.Error,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                tint = if (isLocked) QYTheme.Success else QYTheme.Error,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 灯光状态卡片
 */
@Composable
private fun LightStatusCard(
    headlightOn: Boolean,
    fogLightFront: Boolean,
    fogLightRear: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("灯光状态", color = QYTheme.TextHint, fontSize = 13.sp)
            LightRow("大灯", Icons.Default.LightMode, headlightOn)
            LightRow("前雾灯", Icons.Default.WbCloudy, fogLightFront)
            LightRow("后雾灯", Icons.Default.LightMode, fogLightRear)
        }
    }
}

@Composable
private fun LightRow(name: String, icon: ImageVector, isOn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null,
                tint = if (isOn) QYTheme.Warning else QYTheme.TextHint,
                modifier = Modifier.size(20.dp))
            Text(name, color = QYTheme.TextSecondary, fontSize = 14.sp)
        }
        Text(
            text = if (isOn) "开启" else "关闭",
            color = if (isOn) QYTheme.Warning else QYTheme.TextHint,
            fontSize = 14.sp,
            fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ControlButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

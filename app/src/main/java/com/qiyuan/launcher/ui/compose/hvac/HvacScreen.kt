package com.qiyuan.launcher.ui.compose.hvac

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiyuan.launcher.data.model.HvacState
import com.qiyuan.launcher.ui.compose.QYTheme

/**
 * 空调控制面板页面
 */
@Composable
fun HvacScreen(
    hvacState: HvacState,
    onPowerToggle: (Boolean) -> Unit,
    onTempChange: (Float, Boolean) -> Unit,
    onFanSpeedChange: (Int) -> Unit,
    onFrontDefroster: (Boolean) -> Unit,
    onRearDefroster: (Boolean) -> Unit,
    onSeatHeat: (Int, Boolean) -> Unit,
    onSeatVent: (Int, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题 + 总开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("空调控制", color = QYTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            HvacPowerSwitch(
                isOn = hvacState.powerOn,
                onToggle = onPowerToggle
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左列：温度 + 座椅
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 左侧温度
                TemperatureCard(
                    title = "驾驶员",
                    temp = hvacState.tempLeft,
                    isEnabled = hvacState.powerOn,
                    onIncrease = { onTempChange(hvacState.tempLeft + 0.5f, true) },
                    onDecrease = { onTempChange(hvacState.tempLeft - 0.5f, true) }
                )
                // 左座椅加热 + 通风
                SeatControlCard(
                    title = "驾驶员座椅",
                    heatLevel = hvacState.seatHeatLeft,
                    ventLevel = hvacState.seatVentLeft,
                    isEnabled = hvacState.powerOn,
                    onHeatChange = { onSeatHeat(it, true) },
                    onVentChange = { onSeatVent(it, true) }
                )
            }

            // 中列：风速 + 除雾
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 风速控制
                FanSpeedCard(
                    fanSpeed = hvacState.fanSpeed,
                    autoOn = hvacState.autoOn,
                    isEnabled = hvacState.powerOn,
                    onSpeedChange = onFanSpeedChange
                )
                // 除雾控制
                DefrosterCard(
                    frontOn = hvacState.frontDefroster,
                    rearOn = hvacState.rearDefroster,
                    isEnabled = hvacState.powerOn,
                    onFrontToggle = onFrontDefroster,
                    onRearToggle = onRearDefroster
                )
            }

            // 右列：右侧温度 + 右座椅
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 右侧温度
                TemperatureCard(
                    title = "副驾驶",
                    temp = hvacState.tempRight,
                    isEnabled = hvacState.powerOn,
                    onIncrease = { onTempChange(hvacState.tempRight + 0.5f, false) },
                    onDecrease = { onTempChange(hvacState.tempRight - 0.5f, false) }
                )
                // 右座椅加热 + 通风
                SeatControlCard(
                    title = "副驾驶座椅",
                    heatLevel = hvacState.seatHeatRight,
                    ventLevel = hvacState.seatVentRight,
                    isEnabled = hvacState.powerOn,
                    onHeatChange = { onSeatHeat(it, false) },
                    onVentChange = { onSeatVent(it, false) }
                )
            }
        }
    }
}

/**
 * 空调总开关
 */
@Composable
private fun HvacPowerSwitch(isOn: Boolean, onToggle: (Boolean) -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isOn) QYTheme.Primary else QYTheme.Card,
        animationSpec = tween(300), label = "hvac_power"
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onToggle(!isOn) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Power,
            contentDescription = "空调开关",
            tint = if (isOn) QYTheme.TextOnPrimary else QYTheme.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = if (isOn) "已开启" else "已关闭",
            color = if (isOn) QYTheme.TextOnPrimary else QYTheme.TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 温度控制卡片
 */
@Composable
private fun TemperatureCard(
    title: String,
    temp: Float,
    isEnabled: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, color = QYTheme.TextHint, fontSize = 13.sp)
            Text(
                text = "%.1f°C".format(temp),
                color = if (isEnabled) QYTheme.Primary else QYTheme.TextHint,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TempButton(icon = Icons.Default.Remove, enabled = isEnabled && temp > 16f, onClick = onDecrease)
                TempButton(icon = Icons.Default.Add, enabled = isEnabled && temp < 30f, onClick = onIncrease)
            }
        }
    }
}

@Composable
private fun TempButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (enabled) QYTheme.Primary.copy(0.15f) else QYTheme.Divider)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) QYTheme.Primary else QYTheme.TextHint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * 风速控制卡片
 */
@Composable
private fun FanSpeedCard(
    fanSpeed: Int,
    autoOn: Boolean,
    isEnabled: Boolean,
    onSpeedChange: (Int) -> Unit
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
            Text("风速", color = QYTheme.TextHint, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (autoOn) "AUTO" else "风速 $fanSpeed",
                    color = if (isEnabled) QYTheme.Primary else QYTheme.TextHint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = if (isEnabled) QYTheme.PrimaryLight else QYTheme.TextHint,
                    modifier = Modifier.size(28.dp)
                )
            }
            // 风速滑块
            Slider(
                value = fanSpeed.toFloat(),
                onValueChange = { if (isEnabled) onSpeedChange(it.toInt()) },
                valueRange = 0f..8f,
                steps = 7,
                enabled = isEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = QYTheme.Primary,
                    activeTrackColor = QYTheme.Primary,
                    inactiveTrackColor = QYTheme.Divider
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("低", color = QYTheme.TextHint, fontSize = 11.sp)
                Text("高", color = QYTheme.TextHint, fontSize = 11.sp)
            }
        }
    }
}

/**
 * 除雾控制卡片
 */
@Composable
private fun DefrosterCard(
    frontOn: Boolean,
    rearOn: Boolean,
    isEnabled: Boolean,
    onFrontToggle: (Boolean) -> Unit,
    onRearToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("除雾/除霜", color = QYTheme.TextHint, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DefrosterButton(
                    label = "前除雾",
                    isOn = frontOn,
                    isEnabled = isEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = { onFrontToggle(!frontOn) }
                )
                DefrosterButton(
                    label = "后除雾",
                    isOn = rearOn,
                    isEnabled = isEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = { onRearToggle(!rearOn) }
                )
            }
        }
    }
}

@Composable
private fun DefrosterButton(
    label: String,
    isOn: Boolean,
    isEnabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isOn && isEnabled) QYTheme.PrimaryLight.copy(0.2f) else QYTheme.IconBg,
        label = "defroster"
    )
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isOn && isEnabled) QYTheme.PrimaryLight else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isOn && isEnabled) QYTheme.PrimaryLight else QYTheme.TextHint,
            fontSize = 14.sp,
            fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * 座椅控制卡片
 */
@Composable
private fun SeatControlCard(
    title: String,
    heatLevel: Int,
    ventLevel: Int,
    isEnabled: Boolean,
    onHeatChange: (Int) -> Unit,
    onVentChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QYTheme.Card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = QYTheme.TextHint, fontSize = 13.sp)
            // 座椅加热
            SeatLevelRow(
                label = "加热",
                icon = Icons.Default.Whatshot,
                level = heatLevel,
                maxLevel = 3,
                activeColor = QYTheme.Error.copy(0.8f),
                isEnabled = isEnabled,
                onChange = onHeatChange
            )
            // 座椅通风
            SeatLevelRow(
                label = "通风",
                icon = Icons.Default.Air,
                level = ventLevel,
                maxLevel = 3,
                activeColor = QYTheme.PrimaryLight,
                isEnabled = isEnabled,
                onChange = onVentChange
            )
        }
    }
}

@Composable
private fun SeatLevelRow(
    label: String,
    icon: ImageVector,
    level: Int,
    maxLevel: Int,
    activeColor: Color,
    isEnabled: Boolean,
    onChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null,
                tint = if (level > 0 && isEnabled) activeColor else QYTheme.TextHint,
                modifier = Modifier.size(18.dp))
            Text(label, color = QYTheme.TextSecondary, fontSize = 13.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..maxLevel) {
                val isActive = i <= level && level > 0
                val targetLevel = if (level == i && i > 0) 0 else i
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isActive && isEnabled) activeColor else QYTheme.Divider)
                        .clickable(enabled = isEnabled) { onChange(targetLevel) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (i == 0) "关" else "$i",
                        color = if (isActive && isEnabled) QYTheme.TextPrimary else QYTheme.TextHint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

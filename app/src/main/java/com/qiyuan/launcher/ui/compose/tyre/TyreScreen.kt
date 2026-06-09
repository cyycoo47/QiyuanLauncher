package com.qiyuan.launcher.ui.compose.tyre

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiyuan.launcher.data.model.TyrePressureState
import com.qiyuan.launcher.ui.compose.QYTheme

/**
 * 胎压监测页面
 */
@Composable
fun TyreScreen(tyreState: TyrePressureState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QYTheme.Background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("胎压监测", color = QYTheme.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            // 整体告警提示
            val hasWarn = tyreState.warnFrontLeft || tyreState.warnFrontRight ||
                    tyreState.warnRearLeft || tyreState.warnRearRight
            if (hasWarn) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(QYTheme.Error.copy(0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = QYTheme.Error, modifier = Modifier.size(18.dp))
                    Text("胎压异常！请立即检查", color = QYTheme.Error, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 车辆俯视图布局 - 四轮
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 前排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TyreCard(
                    label = "左前",
                    pressure = tyreState.frontLeft,
                    temp = tyreState.tempFrontLeft,
                    isWarn = tyreState.warnFrontLeft
                )
                // 车头图标占位
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▲ 前", color = QYTheme.TextHint, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
                TyreCard(
                    label = "右前",
                    pressure = tyreState.frontRight,
                    temp = tyreState.tempFrontRight,
                    isWarn = tyreState.warnFrontRight
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 后排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TyreCard(
                    label = "左后",
                    pressure = tyreState.rearLeft,
                    temp = tyreState.tempRearLeft,
                    isWarn = tyreState.warnRearLeft
                )
                Box(
                    modifier = Modifier.width(120.dp).height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▼ 后", color = QYTheme.TextHint, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
                TyreCard(
                    label = "右后",
                    pressure = tyreState.rearRight,
                    temp = tyreState.tempRearRight,
                    isWarn = tyreState.warnRearRight
                )
            }
        }

        // 底部说明
        Text(
            text = "正常胎压范围：230 - 260 kPa（冷车）",
            color = QYTheme.TextHint,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TyreCard(
    label: String,
    pressure: Float,
    temp: Float,
    isWarn: Boolean
) {
    val cardBg = if (isWarn) QYTheme.Error.copy(0.12f) else QYTheme.Card
    val borderColor = if (isWarn) QYTheme.Error else Color.Transparent
    val pressureColor = if (isWarn) QYTheme.Error else if (pressure in 230f..260f) QYTheme.Success else QYTheme.Warning

    Card(
        modifier = Modifier
            .width(150.dp)
            .border(width = if (isWarn) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isWarn) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = QYTheme.Error, modifier = Modifier.size(16.dp))
                }
                Text(text = label, color = if (isWarn) QYTheme.Error else QYTheme.TextHint, fontSize = 14.sp)
            }

            // 胎压大值
            Text(
                text = if (pressure > 0f) "%.0f".format(pressure) else "--",
                color = pressureColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text("kPa", color = QYTheme.TextHint, fontSize = 12.sp)

            Divider(color = QYTheme.Divider, thickness = 1.dp)

            // 胎温
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("胎温:", color = QYTheme.TextHint, fontSize = 12.sp)
                Text(
                    text = if (temp > 0f) "%.1f°C".format(temp) else "--",
                    color = QYTheme.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 状态标签
            val statusText = when {
                pressure <= 0f -> "未获取"
                isWarn -> "异常"
                pressure < 220f -> "偏低"
                pressure > 270f -> "偏高"
                else -> "正常"
            }
            val statusColor = when {
                pressure <= 0f -> QYTheme.TextHint
                isWarn -> QYTheme.Error
                pressure < 220f || pressure > 270f -> QYTheme.Warning
                else -> QYTheme.Success
            }
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(0.1f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

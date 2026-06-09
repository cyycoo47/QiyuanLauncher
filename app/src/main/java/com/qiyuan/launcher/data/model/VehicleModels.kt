package com.qiyuan.launcher.data.model

/**
 * 车辆状态数据模型
 */
data class VehicleState(
    val speed: Float = 0f,              // 车速 km/h
    val gear: String = "--",            // 档位 P/R/N/D
    val batteryLevel: Float = 0f,       // 电池电量 0-100%
    val rangeRemaining: Float = 0f,     // 续航里程 km
    val outsideTemp: Float = 0f,        // 车外温度 ℃
    val isReady: Boolean = false,       // READY 状态
    val isCharging: Boolean = false,    // 充电状态
    val chargePortConnected: Boolean = false, // 充电口连接
    val totalMileage: Float = 0f,       // 总里程 km
    val tripMileage: Float = 0f,        // 小计里程 km
    val drivingMode: Int = 0,           // 驾驶模式
    val sdkConnected: Boolean = false   // SDK 连接状态
)

/**
 * 空调状态数据模型
 */
data class HvacState(
    val powerOn: Boolean = false,       // 空调开关
    val acOn: Boolean = false,          // AC 开关
    val autoOn: Boolean = false,        // 自动模式
    val tempLeft: Float = 22.0f,        // 左侧温度 ℃
    val tempRight: Float = 22.0f,       // 右侧温度 ℃
    val fanSpeed: Int = 0,              // 风速 0-8
    val fanDirection: Int = 0,          // 风向
    val frontDefroster: Boolean = false,// 前除雾
    val rearDefroster: Boolean = false, // 后除雾/除霜
    val seatHeatLeft: Int = 0,          // 左座椅加热 0-3
    val seatHeatRight: Int = 0,         // 右座椅加热 0-3
    val seatVentLeft: Int = 0,          // 左座椅通风 0-3
    val seatVentRight: Int = 0          // 右座椅通风 0-3
)

/**
 * 胎压数据模型
 */
data class TyrePressureState(
    val frontLeft: Float = 0f,          // 左前胎压 kPa
    val frontRight: Float = 0f,         // 右前胎压 kPa
    val rearLeft: Float = 0f,           // 左后胎压 kPa
    val rearRight: Float = 0f,          // 右后胎压 kPa
    val tempFrontLeft: Float = 0f,      // 左前胎温 ℃
    val tempFrontRight: Float = 0f,     // 右前胎温 ℃
    val tempRearLeft: Float = 0f,       // 左后胎温 ℃
    val tempRearRight: Float = 0f,      // 右后胎温 ℃
    val warnFrontLeft: Boolean = false, // 左前告警
    val warnFrontRight: Boolean = false,// 右前告警
    val warnRearLeft: Boolean = false,  // 左后告警
    val warnRearRight: Boolean = false  // 右后告警
)

/**
 * 车辆控制状态模型
 */
data class VehicleControlState(
    val doorLockAll: Boolean = true,       // 全车门锁状态
    val doorOpenFrontLeft: Boolean = false,// 左前门开启状态
    val doorOpenFrontRight: Boolean = false,// 右前门开启状态
    val doorOpenRearLeft: Boolean = false, // 左后门开启状态
    val doorOpenRearRight: Boolean = false,// 右后门开启状态
    val trunkOpen: Boolean = false,        // 后备箱开启状态
    val windowPosFrontLeft: Int = 0,       // 左前窗位置 0-100
    val windowPosFrontRight: Int = 0,      // 右前窗位置 0-100
    val windowPosRearLeft: Int = 0,        // 左后窗位置 0-100
    val windowPosRearRight: Int = 0,       // 右后窗位置 0-100
    val mirrorFolded: Boolean = false,     // 后视镜折叠状态
    val headlightOn: Boolean = false,      // 大灯状态
    val fogLightFront: Boolean = false,    // 前雾灯
    val fogLightRear: Boolean = false      // 后雾灯
)

/**
 * 档位枚举辅助函数
 */
fun gearIntToString(gearValue: Int): String {
    return when (gearValue) {
        4 -> "P"
        3 -> "R"
        2 -> "N"
        8 -> "D"
        16 -> "D"
        else -> if (gearValue in 1..7) gearValue.toString() else "--"
    }
}

/**
 * 驾驶模式名称
 */
fun drivingModeToString(mode: Int): String {
    return when (mode) {
        0 -> "标准"
        1 -> "经济"
        2 -> "运动"
        3 -> "雪地"
        4 -> "越野"
        else -> "标准"
    }
}

package com.qiyuan.launcher.data.repository

import android.content.Context
import android.util.Log
import com.changan.sda.opensdk.client.CaOpenSdkManager
import com.changan.sda.opensdk.client.OpenSdkInitCallback
import com.changan.sda.opensdk.client.callback.CarPropertyEventCallback
import com.changan.sda.opensdk.client.consts.VehicleAreaSeat
import com.changan.sda.opensdk.client.consts.VehicleAreaType
import com.changan.sda.opensdk.client.consts.VehiclePropertyIds
import com.changan.sda.opensdk.api.entity.CarPropertyValue
import com.qiyuan.launcher.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 车辆数据 Repository
 * 封装启源 A06 OpenSDK 的所有数据获取和控制操作
 */
class VehicleRepository(private val context: Context) {

    companion object {
        private const val TAG = "VehicleRepository"
        private const val AREA_GLOBAL = 0
    }

    private val sdk = CaOpenSdkManager.getInstance()

    // --- 状态 Flow ---
    private val _sdkConnected = MutableStateFlow(false)
    val sdkConnected: StateFlow<Boolean> = _sdkConnected.asStateFlow()

    private val _sdkError = MutableStateFlow<String?>(null)
    val sdkError: StateFlow<String?> = _sdkError.asStateFlow()

    private val _vehicleState = MutableStateFlow(VehicleState())
    val vehicleState: StateFlow<VehicleState> = _vehicleState.asStateFlow()

    private val _hvacState = MutableStateFlow(HvacState())
    val hvacState: StateFlow<HvacState> = _hvacState.asStateFlow()

    private val _tyreState = MutableStateFlow(TyrePressureState())
    val tyreState: StateFlow<TyrePressureState> = _tyreState.asStateFlow()

    private val _controlState = MutableStateFlow(VehicleControlState())
    val controlState: StateFlow<VehicleControlState> = _controlState.asStateFlow()

    /**
     * 初始化 SDK
     */
    fun initSdk() {
        try {
            sdk.init(context)
            sdk.registerInitCallback(object : OpenSdkInitCallback {
                override fun onInitSuccess() {
                    Log.i(TAG, "SDK 初始化成功")
                    _sdkConnected.value = true
                    _sdkError.value = null
                    subscribeAllProperties()
                    fetchInitialData()
                }

                override fun onInitError(errorMsg: String?) {
                    Log.e(TAG, "SDK 初始化失败: $errorMsg")
                    _sdkConnected.value = false
                    _sdkError.value = "SDK 初始化失败: $errorMsg"
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "SDK 初始化异常: ${e.message}", e)
            _sdkError.value = "SDK 初始化异常: ${e.message}"
        }
    }

    /**
     * 订阅所有需要实时监听的车辆属性
     */
    private fun subscribeAllProperties() {
        // 车速
        subscribeProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, AREA_GLOBAL)
        // 档位
        subscribeProperty(VehiclePropertyIds.GEAR_SELECTION, AREA_GLOBAL)
        subscribeProperty(VehiclePropertyIds.CURRENT_GEAR, AREA_GLOBAL)
        // 电量
        subscribeProperty(VehiclePropertyIds.EV_BATTERY_LEVEL, AREA_GLOBAL)
        subscribeProperty(VehiclePropertyIds.RANGE_REMAINING, AREA_GLOBAL)
        // 环境
        subscribeProperty(VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE, AREA_GLOBAL)
        // 充电
        subscribeProperty(VehiclePropertyIds.EV_CHARGE_STATE, AREA_GLOBAL)
        subscribeProperty(VehiclePropertyIds.EV_CHARGE_PORT_CONNECTED, AREA_GLOBAL)
        // 空调
        subscribeProperty(VehiclePropertyIds.HVAC_AC_ON, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_POWER_ON, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_AUTO_ON, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_TEMPERATURE_SET, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_TEMPERATURE_SET, VehicleAreaSeat.SEAT_ROW_1_RIGHT)
        subscribeProperty(VehiclePropertyIds.HVAC_FAN_SPEED, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_FAN_DIRECTION, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_SEAT_TEMPERATURE, VehicleAreaSeat.SEAT_ROW_1_RIGHT)
        subscribeProperty(VehiclePropertyIds.HVAC_SEAT_VENTILATION, VehicleAreaSeat.SEAT_ROW_1_LEFT)
        subscribeProperty(VehiclePropertyIds.HVAC_SEAT_VENTILATION, VehicleAreaSeat.SEAT_ROW_1_RIGHT)
        subscribeProperty(VehiclePropertyIds.HVAC_DEFROSTER, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL)
        subscribeProperty(VehiclePropertyIds.HVAC_ELECTRIC_DEFROSTER_ON, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL)
        // 胎压
        subscribeProperty(VehiclePropertyIds.TIRE_PRESSURE, AREA_GLOBAL)
        // 车门
        subscribeProperty(VehiclePropertyIds.DOOR_LOCK, AREA_GLOBAL)
        // 灯光
        subscribeProperty(VehiclePropertyIds.HEADLIGHTS_STATE, AREA_GLOBAL)
        subscribeProperty(VehiclePropertyIds.FOG_LIGHTS_STATE, AREA_GLOBAL)
        // 驾驶模式
        subscribeProperty(VehiclePropertyIds.ID_DRIVING_MODE, AREA_GLOBAL)
        // READY状态
        subscribeProperty(VehiclePropertyIds.ID_DRIVE_READY_STATUS, AREA_GLOBAL)
    }

    private fun subscribeProperty(propertyId: Int, areaId: Int) {
        try {
            sdk.registerPropertyCallback(propertyCallback, propertyId, areaId.toFloat())
        } catch (e: Exception) {
            Log.w(TAG, "订阅属性 $propertyId 失败: ${e.message}")
        }
    }

    /**
     * 统一车辆属性回调
     */
    private val propertyCallback = object : CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue?) {
            value ?: return
            handlePropertyChange(value.propertyId, value.areaId, value.value)
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            Log.w(TAG, "属性错误: propertyId=$propertyId, areaId=$areaId")
        }
    }

    /**
     * 处理属性变化
     */
    private fun handlePropertyChange(propertyId: Int, areaId: Int, value: Any?) {
        when (propertyId) {
            // --- 车辆基础状态 ---
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                val speed = (value as? Float) ?: return
                _vehicleState.value = _vehicleState.value.copy(speed = speed)
            }
            VehiclePropertyIds.GEAR_SELECTION,
            VehiclePropertyIds.CURRENT_GEAR -> {
                val gear = (value as? Int) ?: return
                _vehicleState.value = _vehicleState.value.copy(gear = gearIntToString(gear))
            }
            VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                val level = (value as? Float) ?: return
                _vehicleState.value = _vehicleState.value.copy(batteryLevel = level)
            }
            VehiclePropertyIds.RANGE_REMAINING -> {
                val range = (value as? Float) ?: return
                _vehicleState.value = _vehicleState.value.copy(rangeRemaining = range / 1000f) // 转换为 km
            }
            VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE -> {
                val temp = (value as? Float) ?: return
                _vehicleState.value = _vehicleState.value.copy(outsideTemp = temp)
            }
            VehiclePropertyIds.EV_CHARGE_STATE -> {
                val state = (value as? Int) ?: return
                _vehicleState.value = _vehicleState.value.copy(isCharging = state != 0)
            }
            VehiclePropertyIds.EV_CHARGE_PORT_CONNECTED -> {
                val connected = (value as? Boolean) ?: return
                _vehicleState.value = _vehicleState.value.copy(chargePortConnected = connected)
            }
            VehiclePropertyIds.ID_DRIVING_MODE -> {
                val mode = (value as? Int) ?: return
                _vehicleState.value = _vehicleState.value.copy(drivingMode = mode)
            }
            VehiclePropertyIds.ID_DRIVE_READY_STATUS -> {
                val ready = (value as? Int) ?: return
                _vehicleState.value = _vehicleState.value.copy(isReady = ready != 0)
            }

            // --- 空调 ---
            VehiclePropertyIds.HVAC_POWER_ON -> {
                val on = (value as? Boolean) ?: return
                _hvacState.value = _hvacState.value.copy(powerOn = on)
            }
            VehiclePropertyIds.HVAC_AC_ON -> {
                val on = (value as? Boolean) ?: return
                _hvacState.value = _hvacState.value.copy(acOn = on)
            }
            VehiclePropertyIds.HVAC_AUTO_ON -> {
                val on = (value as? Boolean) ?: return
                _hvacState.value = _hvacState.value.copy(autoOn = on)
            }
            VehiclePropertyIds.HVAC_TEMPERATURE_SET -> {
                val temp = (value as? Float) ?: return
                if (areaId == VehicleAreaSeat.SEAT_ROW_1_LEFT) {
                    _hvacState.value = _hvacState.value.copy(tempLeft = temp)
                } else {
                    _hvacState.value = _hvacState.value.copy(tempRight = temp)
                }
            }
            VehiclePropertyIds.HVAC_FAN_SPEED -> {
                val speed = (value as? Int) ?: return
                _hvacState.value = _hvacState.value.copy(fanSpeed = speed)
            }
            VehiclePropertyIds.HVAC_FAN_DIRECTION -> {
                val dir = (value as? Int) ?: return
                _hvacState.value = _hvacState.value.copy(fanDirection = dir)
            }
            VehiclePropertyIds.HVAC_SEAT_TEMPERATURE -> {
                val heat = (value as? Int) ?: return
                if (areaId == VehicleAreaSeat.SEAT_ROW_1_LEFT) {
                    _hvacState.value = _hvacState.value.copy(seatHeatLeft = heat)
                } else {
                    _hvacState.value = _hvacState.value.copy(seatHeatRight = heat)
                }
            }
            VehiclePropertyIds.HVAC_SEAT_VENTILATION -> {
                val vent = (value as? Int) ?: return
                if (areaId == VehicleAreaSeat.SEAT_ROW_1_LEFT) {
                    _hvacState.value = _hvacState.value.copy(seatVentLeft = vent)
                } else {
                    _hvacState.value = _hvacState.value.copy(seatVentRight = vent)
                }
            }
            VehiclePropertyIds.HVAC_DEFROSTER -> {
                val on = (value as? Boolean) ?: return
                _hvacState.value = _hvacState.value.copy(frontDefroster = on)
            }
            VehiclePropertyIds.HVAC_ELECTRIC_DEFROSTER_ON -> {
                val on = (value as? Boolean) ?: return
                _hvacState.value = _hvacState.value.copy(rearDefroster = on)
            }

            // --- 胎压 ---
            VehiclePropertyIds.TIRE_PRESSURE -> {
                val pressure = (value as? Float) ?: return
                val pressureKpa = pressure / 1000f
                _tyreState.value = when (areaId) {
                    0x10 -> _tyreState.value.copy(frontLeft = pressureKpa)  // WHEEL_FRONT_LEFT
                    0x20 -> _tyreState.value.copy(frontRight = pressureKpa) // WHEEL_FRONT_RIGHT
                    0x40 -> _tyreState.value.copy(rearLeft = pressureKpa)   // WHEEL_REAR_LEFT
                    0x80 -> _tyreState.value.copy(rearRight = pressureKpa)  // WHEEL_REAR_RIGHT
                    else -> _tyreState.value
                }
            }

            // --- 车门 ---
            VehiclePropertyIds.DOOR_LOCK -> {
                val locked = (value as? Boolean) ?: return
                _controlState.value = _controlState.value.copy(doorLockAll = locked)
            }

            // --- 灯光 ---
            VehiclePropertyIds.HEADLIGHTS_STATE -> {
                val state = (value as? Int) ?: return
                _controlState.value = _controlState.value.copy(headlightOn = state != 0)
            }
            VehiclePropertyIds.FOG_LIGHTS_STATE -> {
                val state = (value as? Int) ?: return
                _controlState.value = _controlState.value.copy(fogLightFront = state != 0)
            }
        }
    }

    /**
     * 主动获取一次初始数据
     */
    private fun fetchInitialData() {
        try {
            // 获取车速
            sdk.getProperty(Float::class.java, VehiclePropertyIds.PERF_VEHICLE_SPEED, AREA_GLOBAL)
                ?.data?.let { _vehicleState.value = _vehicleState.value.copy(speed = it as Float) }
            // 获取电量
            sdk.getProperty(Float::class.java, VehiclePropertyIds.EV_BATTERY_LEVEL, AREA_GLOBAL)
                ?.data?.let { _vehicleState.value = _vehicleState.value.copy(batteryLevel = it as Float) }
            // 获取续航
            sdk.getProperty(Float::class.java, VehiclePropertyIds.RANGE_REMAINING, AREA_GLOBAL)
                ?.data?.let {
                    val range = (it as Float) / 1000f
                    _vehicleState.value = _vehicleState.value.copy(rangeRemaining = range)
                }
            // 获取车外温度
            sdk.getProperty(Float::class.java, VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE, AREA_GLOBAL)
                ?.data?.let { _vehicleState.value = _vehicleState.value.copy(outsideTemp = it as Float) }
            // 获取总里程
            sdk.getProperty(Float::class.java, VehiclePropertyIds.ID_DRIVE_TOTAL_MILE_VALUE, AREA_GLOBAL)
                ?.data?.let { _vehicleState.value = _vehicleState.value.copy(totalMileage = it as Float) }
            // 获取小计里程
            sdk.getProperty(Float::class.java, VehiclePropertyIds.ID_DRIVE_TRIP_MILES_VALUE, AREA_GLOBAL)
                ?.data?.let { _vehicleState.value = _vehicleState.value.copy(tripMileage = it as Float) }
            // 获取空调温度
            sdk.getProperty(Float::class.java, VehiclePropertyIds.HVAC_TEMPERATURE_SET,
                VehicleAreaSeat.SEAT_ROW_1_LEFT)
                ?.data?.let { _hvacState.value = _hvacState.value.copy(tempLeft = it as Float) }
            sdk.getProperty(Float::class.java, VehiclePropertyIds.HVAC_TEMPERATURE_SET,
                VehicleAreaSeat.SEAT_ROW_1_RIGHT)
                ?.data?.let { _hvacState.value = _hvacState.value.copy(tempRight = it as Float) }
        } catch (e: Exception) {
            Log.e(TAG, "初始数据获取失败: ${e.message}", e)
        }
    }

    // ======================== 空调控制方法 ========================

    /**
     * 切换空调电源
     */
    fun setHvacPower(on: Boolean): Boolean {
        return try {
            val result = sdk.setProperty(Boolean::class.java, VehiclePropertyIds.HVAC_POWER_ON,
                VehicleAreaSeat.SEAT_ROW_1_LEFT, on)
            result?.isSuccess == true
        } catch (e: Exception) {
            Log.e(TAG, "空调开关失败: ${e.message}", e)
            false
        }
    }

    /**
     * 设置空调温度
     */
    fun setHvacTemperature(temp: Float, isLeft: Boolean): Boolean {
        val areaId = if (isLeft) VehicleAreaSeat.SEAT_ROW_1_LEFT else VehicleAreaSeat.SEAT_ROW_1_RIGHT
        return try {
            val result = sdk.setProperty(Float::class.java, VehiclePropertyIds.HVAC_TEMPERATURE_SET,
                areaId, temp)
            result?.isSuccess == true
        } catch (e: Exception) {
            Log.e(TAG, "设置温度失败: ${e.message}", e)
            false
        }
    }

    /**
     * 设置风速
     */
    fun setFanSpeed(speed: Int): Boolean {
        return try {
            val result = sdk.setProperty(Int::class.java, VehiclePropertyIds.HVAC_FAN_SPEED,
                VehicleAreaSeat.SEAT_ROW_1_LEFT, speed)
            result?.isSuccess == true
        } catch (e: Exception) {
            Log.e(TAG, "设置风速失败: ${e.message}", e)
            false
        }
    }

    /**
     * 切换前除雾
     */
    fun setFrontDefroster(on: Boolean): Boolean {
        return try {
            val result = sdk.setProperty(Boolean::class.java, VehiclePropertyIds.HVAC_DEFROSTER,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL, on)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 切换后除雾/电加热
     */
    fun setRearDefroster(on: Boolean): Boolean {
        return try {
            val result = sdk.setProperty(Boolean::class.java,
                VehiclePropertyIds.HVAC_ELECTRIC_DEFROSTER_ON,
                VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL, on)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 设置座椅加热等级
     */
    fun setSeatHeat(level: Int, isLeft: Boolean): Boolean {
        val areaId = if (isLeft) VehicleAreaSeat.SEAT_ROW_1_LEFT else VehicleAreaSeat.SEAT_ROW_1_RIGHT
        return try {
            val result = sdk.setProperty(Int::class.java, VehiclePropertyIds.HVAC_SEAT_TEMPERATURE,
                areaId, level)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 设置座椅通风等级
     */
    fun setSeatVent(level: Int, isLeft: Boolean): Boolean {
        val areaId = if (isLeft) VehicleAreaSeat.SEAT_ROW_1_LEFT else VehicleAreaSeat.SEAT_ROW_1_RIGHT
        return try {
            val result = sdk.setProperty(Int::class.java, VehiclePropertyIds.HVAC_SEAT_VENTILATION,
                areaId, level)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    // ======================== 车辆控制方法 ========================

    /**
     * 控制车窗 (position: 0=全关, 100=全开)
     */
    fun setWindowPosition(position: Int, areaId: Int): Boolean {
        return try {
            val result = sdk.setProperty(Int::class.java, VehiclePropertyIds.WINDOW_POS,
                areaId, position)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 切换后视镜折叠
     */
    fun setMirrorFold(fold: Boolean): Boolean {
        return try {
            val result = sdk.setProperty(Boolean::class.java, VehiclePropertyIds.MIRROR_FOLD,
                AREA_GLOBAL, fold)
            result?.isSuccess == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            sdk.unRegisterPropertyCallback(propertyCallback, VehiclePropertyIds.PERF_VEHICLE_SPEED)
            sdk.unRegisterPropertyCallback(propertyCallback, VehiclePropertyIds.GEAR_SELECTION)
            sdk.unRegisterPropertyCallback(propertyCallback, VehiclePropertyIds.EV_BATTERY_LEVEL)
        } catch (e: Exception) {
            Log.e(TAG, "释放资源失败: ${e.message}", e)
        }
    }
}

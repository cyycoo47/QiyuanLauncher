package com.qiyuan.launcher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qiyuan.launcher.data.repository.VehicleRepository
import com.qiyuan.launcher.data.model.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 统一车辆数据 ViewModel
 * 所有页面共享同一个 ViewModel 实例，避免重复创建 SDK 连接
 */
class VehicleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VehicleRepository(application.applicationContext)

    // 对外暴露 StateFlow
    val sdkConnected: StateFlow<Boolean> = repository.sdkConnected
    val sdkError: StateFlow<String?> = repository.sdkError
    val vehicleState: StateFlow<VehicleState> = repository.vehicleState
    val hvacState: StateFlow<HvacState> = repository.hvacState
    val tyreState: StateFlow<TyrePressureState> = repository.tyreState
    val controlState: StateFlow<VehicleControlState> = repository.controlState

    init {
        initSdk()
    }

    private fun initSdk() {
        viewModelScope.launch {
            repository.initSdk()
        }
    }

    // ======================== 空调控制 ========================

    fun setHvacPower(on: Boolean) {
        viewModelScope.launch { repository.setHvacPower(on) }
    }

    fun setHvacTemperature(temp: Float, isLeft: Boolean) {
        viewModelScope.launch { repository.setHvacTemperature(temp, isLeft) }
    }

    fun setFanSpeed(speed: Int) {
        viewModelScope.launch { repository.setFanSpeed(speed) }
    }

    fun setFrontDefroster(on: Boolean) {
        viewModelScope.launch { repository.setFrontDefroster(on) }
    }

    fun setRearDefroster(on: Boolean) {
        viewModelScope.launch { repository.setRearDefroster(on) }
    }

    fun setSeatHeat(level: Int, isLeft: Boolean) {
        viewModelScope.launch { repository.setSeatHeat(level, isLeft) }
    }

    fun setSeatVent(level: Int, isLeft: Boolean) {
        viewModelScope.launch { repository.setSeatVent(level, isLeft) }
    }

    // ======================== 车辆控制 ========================

    fun setWindowAll(open: Boolean) {
        val position = if (open) 100 else 0
        viewModelScope.launch {
            // 四扇窗分别控制，areaId 参考 VehicleAreaWindow
            repository.setWindowPosition(position, 0x01) // FRONT_LEFT
            repository.setWindowPosition(position, 0x02) // FRONT_RIGHT
            repository.setWindowPosition(position, 0x04) // REAR_LEFT
            repository.setWindowPosition(position, 0x08) // REAR_RIGHT
        }
    }

    fun setMirrorFold(fold: Boolean) {
        viewModelScope.launch { repository.setMirrorFold(fold) }
    }

    override fun onCleared() {
        super.onCleared()
        repository.release()
    }
}

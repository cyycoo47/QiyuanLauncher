# 启源车机桌面 - QiyuanLauncher

基于启源A06 OpenSDK开发的Android原生车机桌面应用，适配长安启源A06车型。

## 📱 功能概览

| 功能模块 | 描述 |
|---------|------|
| **车机桌面** | 横屏应用网格，快捷控制卡片，实时顶部状态栏 |
| **行车仪表盘** | 车速弧形仪表、电量进度环、续航/里程/驾驶模式 |
| **空调控制** | 左右独立温度、风速滑块、座椅加热/通风、前后除雾 |
| **车辆控制** | 车门/车窗状态显示、车窗一键升降、后视镜折叠、灯光状态 |
| **胎压监测** | 四轮胎压/胎温实时显示，异常高亮告警 |
| **设置中心** | SDK连接状态、应用信息、权限说明 |

## 🛠️ 技术栈

- **语言**：Kotlin 2.0
- **UI框架**：Jetpack Compose + Material3
- **架构**：MVVM + Repository
- **导航**：Navigation Compose
- **车机SDK**：启源A06 OpenSDK v1.0.0.0
- **最低Android版本**：9.0（API 28）
- **目标SDK**：Android 13（API 33）

## 📂 项目结构

```
QiyuanLauncher/
├── app/
│   ├── libs/
│   │   └── opensdk-client_V1.0.0.0.aar     ← 启源SDK（须自行放入）
│   └── src/main/
│       ├── AndroidManifest.xml              ← 全部车机权限声明
│       └── java/com/qiyuan/launcher/
│           ├── MainActivity.kt              ← Launcher入口
│           ├── data/
│           │   ├── model/VehicleModels.kt   ← 数据模型
│           │   └── repository/VehicleRepository.kt  ← SDK封装
│           ├── viewmodel/
│           │   └── VehicleViewModel.kt      ← 统一ViewModel
│           └── ui/compose/
│               ├── QYTheme.kt               ← 配色系统
│               ├── AppNavigation.kt         ← 导航 + 底部Nav
│               ├── HomeScreen.kt            ← 主桌面
│               ├── dashboard/DashboardScreen.kt
│               ├── hvac/HvacScreen.kt
│               ├── vehicle/VehicleControlScreen.kt
│               ├── tyre/TyreScreen.kt
│               └── settings/SettingsScreen.kt
└── gradle/
    └── libs.versions.toml                   ← 依赖版本管理
```

## 🚀 构建步骤

### 前置条件

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK API 28+
- **启源A06开发者账号**（申请SDK权限）

### 步骤 1：放入 SDK AAR

将 `opensdk-client_V1.0.0.0.aar` 复制到：
```
app/libs/opensdk-client_V1.0.0.0.aar
```

### 步骤 2：在 Android Studio 打开项目

```bash
# 打开项目目录
File → Open → 选择 QiyuanLauncher/ 目录
```

### 步骤 3：同步 Gradle

```
点击 "Sync Now" 或执行：
./gradlew :app:dependencies
```

### 步骤 4：构建 APK

```bash
# 调试版
./gradlew assembleDebug

# 发布版（需签名配置）
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

### 步骤 5：安装到车机

```bash
adb connect <车机IP>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## ⚙️ SDK 权限配置

### 必须在车机系统中授予的权限

应用需要以下 `android.car.permission.*` 权限，通常需要车机厂商预置或通过 adb 授予：

```bash
# 批量授予权限（需 root 或系统签名）
adb shell pm grant com.qiyuan.launcher android.car.permission.CAR_SPEED
adb shell pm grant com.qiyuan.launcher android.car.permission.CAR_ENERGY
adb shell pm grant com.qiyuan.launcher android.car.permission.CAR_ENERGY_PORTS
adb shell pm grant com.qiyuan.launcher android.car.permission.CAR_TIRES
adb shell pm grant com.qiyuan.launcher android.car.permission.CAR_POWERTRAIN
adb shell pm grant com.qiyuan.launcher android.car.permission.ENV_OUTSIDE_TEMPERATURE
adb shell pm grant com.qiyuan.launcher android.car.permission.CONTROL_CAR_CLIMATE
adb shell pm grant com.qiyuan.launcher android.car.permission.CONTROL_CAR_WINDOWS
adb shell pm grant com.qiyuan.launcher android.car.permission.CONTROL_CAR_MIRRORS
adb shell pm grant com.qiyuan.launcher android.car.permission.READ_CAR_DISPLAY_UNITS
```

### 设置为默认桌面

```bash
# 设置为默认 HOME
adb shell cmd package set-home-activity com.qiyuan.launcher/.MainActivity
```

## 🔌 SDK 集成说明

### SDK 初始化

SDK 在 `VehicleViewModel` 初始化时自动启动，通过 `VehicleRepository` 封装所有操作：

```kotlin
// 自动在 ViewModel.init{} 调用
repository.initSdk()  // → 注册 OpenSdkInitCallback → 订阅所有属性
```

### 数据流

```
启源SDK (CaOpenSdkManager)
    ↓ CarPropertyEventCallback
VehicleRepository (StateFlow)
    ↓ collectAsStateWithLifecycle
VehicleViewModel
    ↓
Compose UI (自动重组)
```

### 订阅的车辆属性

| 属性 | Property ID | 说明 |
|------|-------------|------|
| 车速 | `PERF_VEHICLE_SPEED` | Float，单位 m/s，UI换算为 km/h |
| 档位 | `GEAR_SELECTION` | Int，P=4, R=3, N=2, D=8/16 |
| 电量 | `EV_BATTERY_LEVEL` | Float，0-100% |
| 续航 | `RANGE_REMAINING` | Float，单位 m，UI换算为 km |
| 车外温 | `ENV_OUTSIDE_TEMPERATURE` | Float，℃ |
| 空调电源 | `HVAC_POWER_ON` | Boolean |
| 温度设定 | `HVAC_TEMPERATURE_SET` | Float，℃，按 areaId 区分左/右 |
| 风速 | `HVAC_FAN_SPEED` | Int，0-8 |
| 胎压 | `TIRE_PRESSURE` | Float，单位 Pa，UI换算为 kPa |
| READY | `ID_DRIVE_READY_STATUS` | Int，≠0 为 READY |

## 🎨 UI 设计规范

| 元素 | 值 |
|------|-----|
| 主背景色 | `#0D1117` |
| 卡片背景 | `#1E2530` |
| 主色调（青蓝） | `#00BCD4` |
| 强调色（橙） | `#FF9800` |
| 告警色（红） | `#F44336` |
| 正常色（绿） | `#4CAF50` |
| 主要字体大小 | ≥ 24sp（车速52sp） |
| 触控最小尺寸 | 48dp |
| 卡片圆角 | 12~20dp |
| 屏幕方向 | 强制横屏 |

## ⚠️ 注意事项

1. **SDK 权限**：启源A06 SDK 使用 `android.car.permission.*` 权限体系，普通应用无法直接申请，需要车机厂商授权或系统预置。

2. **SDK 连接**：SDK 连接依赖车机系统服务，模拟器/非车机设备上 SDK 将初始化失败，UI 会显示错误提示，但其他 UI 仍可正常展示（显示默认值 0）。

3. **续航单位**：SDK 的 `RANGE_REMAINING` 返回值单位为**米（m）**，代码中已自动换算为公里（÷1000）。

4. **胎压单位**：SDK 的 `TIRE_PRESSURE` 返回值单位为**帕（Pa）**，代码中已自动换算为千帕（÷1000）。

5. **areaId**：空调温度、座椅控制等 HVAC 属性的 areaId 使用 `VehicleAreaSeat`（驾驶员=`SEAT_ROW_1_LEFT`，副驾=`SEAT_ROW_1_RIGHT`）。

6. **Launcher 设置**：应用已声明 `android.intent.category.HOME`，安装后可在系统设置中设为默认桌面；也可通过 adb 命令强制设置。

## 📋 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-05-17 | 初始版本，基于启源A06 OpenSDK v1.0.0.0 |

## 📄 许可

本项目代码版权归开发者所有。启源A06 OpenSDK 版权归长安汽车/启源品牌所有，使用前请遵守相关 SDK 使用协议。

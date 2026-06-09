package com.qiyuan.launcher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.qiyuan.launcher.ui.compose.QYTheme
import com.qiyuan.launcher.ui.compose.QiyuanLauncherApp

/**
 * 启源车机桌面主 Activity
 * 配置为 HOME 类型的 Launcher
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏显示，隐藏系统状态栏（车机通常有自己的状态栏）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // 保持屏幕常亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            QYLauncherTheme {
                QiyuanLauncherApp()
            }
        }
    }

    override fun onBackPressed() {
        // 作为 Launcher，拦截返回键，不退出应用
    }
}

@Composable
fun QYLauncherTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = QYTheme.Primary,
        background = QYTheme.Background,
        surface = QYTheme.Surface,
        onPrimary = QYTheme.TextOnPrimary,
        onBackground = QYTheme.TextPrimary,
        onSurface = QYTheme.TextPrimary,
        secondary = QYTheme.Accent,
        error = QYTheme.Error
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

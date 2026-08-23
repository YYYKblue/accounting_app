package com.yyykblue.accounting.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yyykblue.accounting.notification.PaymentNotificationListener

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val listenerEnabled = rememberNotificationListenerState(context)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("自动记账设置", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "简账不会轮询或读取支付账户，只在系统投递支付通知时尝试解析，并把账单保存在本机。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        SettingCard(
            title = "通知使用权",
            subtitle = if (listenerEnabled) "已开启，可以接收支付通知" else "未开启，自动记账不会生效",
            enabled = listenerEnabled,
            icon = Icons.Default.NotificationsActive,
            onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        )
        SettingCard(
            title = "电池策略设为无限制",
            subtitle = "在红米 K70 / 澎湃 OS 中减少后台监听被停用",
            icon = Icons.Default.BatterySaver,
            onClick = { openBatterySettings(context) },
        )
        SettingCard(
            title = "允许后台自启动",
            subtitle = "打开小米自启动管理；若页面不可用，请从系统设置手动进入",
            icon = Icons.Default.PlayCircle,
            onClick = { openAutoStartSettings(context) },
        )
        Spacer(Modifier.height(12.dp))
        Text("识别范围", fontWeight = FontWeight.Bold)
        Text(
            "已内置支付宝、微信，以及常见银行 App 的严格规则。通知必须同时包含明确金额和付款/到账等词语，普通聊天和营销通知不会入账。自动账单仍需人工核对。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("隐私", fontWeight = FontWeight.Bold)
        Text(
            "账单和原始匹配文本只存放在本机 Room 数据库；当前版本没有账号、云同步、广告或数据上传代码。卸载应用会清除本机数据。",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("版本 0.1.0", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean? = null,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (enabled != null) {
                Icon(
                    if (enabled) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                )
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun rememberNotificationListenerState(context: Context): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) enabled = isNotificationListenerEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return enabled
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val component = ComponentName(context, PaymentNotificationListener::class.java).flattenToString()
    return Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        ?.split(':')
        ?.any { it.equals(component, ignoreCase = true) }
        ?: false
}

private fun openBatterySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
}

private fun openAutoStartSettings(context: Context) {
    val candidates = listOf(
        ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"),
    )
    val opened = candidates.any { component ->
        runCatching {
            context.startActivity(Intent().setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
    }
    if (!opened) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            },
        )
    }
}

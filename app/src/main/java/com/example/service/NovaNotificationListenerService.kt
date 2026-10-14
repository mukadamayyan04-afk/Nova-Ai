package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object NovaNotificationHub {
    private val _incomingNotifications = MutableSharedFlow<NotificationItem>(extraBufferCapacity = 64)
    val incomingNotifications = _incomingNotifications.asSharedFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected = _isServiceConnected.asStateFlow()

    fun emitNotification(item: NotificationItem) {
        _incomingNotifications.tryEmit(item)
    }

    fun setServiceConnected(connected: Boolean) {
        _isServiceConnected.value = connected
    }

    fun isPermissionGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(context.packageName) == true
    }

    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

class NovaNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NovaNotificationHub.setServiceConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NovaNotificationHub.setServiceConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Skip our own notifications or ongoing system alerts
        val pkg = sbn.packageName ?: return
        if (pkg == packageName) return
        if ((sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim() ?: ""

        if (title.isEmpty() && text.isEmpty()) return

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }

        val item = NotificationItem(
            id = UUID.randomUUID().toString(),
            packageName = pkg,
            appName = appName,
            title = title,
            content = text,
            timestamp = sbn.postTime
        )

        NovaNotificationHub.emitNotification(item)
    }
}

package com.example.projectmatrix.data.notification

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.projectmatrix.MainActivity
import com.example.projectmatrix.R
import com.example.projectmatrix.domain.repository.MessengerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatNotificationWatcher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: MessengerRepository,
    private val preferences: SharedPreferences,
) : Application.ActivityLifecycleCallbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var foregroundActivities = 0
    private var started = false

    fun start() {
        if (started) return
        started = true
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(this)
        createChannel()
        scope.launch {
            while (isActive) {
                delay(5_000)
                poll()
            }
        }
    }

    private suspend fun poll() {
        if (foregroundActivities > 0 || repository.token == null) return
        runCatching { repository.chats() }.getOrNull()
            ?.filter { chat ->
                chat.unreadCount > 0 &&
                    chat.lastMessage != null &&
                    chat.lastMessage.senderId != repository.currentUserId
            }
            ?.forEach { chat ->
                val message = chat.lastMessage ?: return@forEach
                val key = "notified_${chat.id}"
                if (preferences.getString(key, null) == message.id) return@forEach
                preferences.edit().putString(key, message.id).apply()
                showNotification(
                    id = chat.id.hashCode(),
                    title = chat.companion.displayName,
                    text = message.body,
                )
            }
    }

    private fun showNotification(id: Int, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "MIN messages", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    override fun onActivityStarted(activity: Activity) {
        foregroundActivities += 1
    }

    override fun onActivityStopped(activity: Activity) {
        foregroundActivities = (foregroundActivities - 1).coerceAtLeast(0)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private companion object {
        const val CHANNEL_ID = "min_messages"
    }
}

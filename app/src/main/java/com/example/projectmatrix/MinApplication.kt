package com.example.projectmatrix

import android.app.Application
import com.example.projectmatrix.data.notification.ChatNotificationWatcher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MinApplication : Application() {
    @Inject
    lateinit var notificationWatcher: ChatNotificationWatcher

    override fun onCreate() {
        super.onCreate()
        notificationWatcher.start()
    }
}

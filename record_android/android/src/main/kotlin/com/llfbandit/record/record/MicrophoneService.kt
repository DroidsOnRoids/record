package com.llfbandit.record.record

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class MicrophoneService : Service() {
    companion object {
        private const val microphoneChannelId = "microphoneChannelId"
    }


    override fun onCreate() {
        startForeground()
        super.onCreate()
    }
    private fun startForeground() {
        // Before starting the service as foreground check that the app has the
        // appropriate runtime permissions. In this case, verify that the user has
        // granted the RECORD_AUDIO permission.
        val cameraPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (cameraPermission == PackageManager.PERMISSION_DENIED) {
            // Without RECORD_AUDIO permissions the service cannot run in the foreground
            // Consider informing user or updating your app UI if visible.
            stopSelf()
            return
        }

        try {
            // Create the notification to display while the service is running
            val notification = NotificationCompat.Builder(this, microphoneChannelId).apply {
                setContentTitle("Microphone Service")
                setOngoing(true)
                setContentText("Running background service to analyze your sleep")
                val importance = NotificationManager.IMPORTANCE_LOW
                NotificationChannel(microphoneChannelId, "Microphone Service", importance).apply {
                    description = "Running service to analyze sleep"
                    with((applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)) {
                        createNotificationChannel(this@apply)
                    }
                }
            }
                .build()
            // need core 1.12 and higher and SDK 29 and higher, so no support for AGP7
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ 100, // Cannot be 0
                /* notification = */ notification,
                /* foregroundServiceType = */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                },
            )
            Log.d("MicrophoneService", "Foreground task started")
        } catch (e: Exception) {
            Log.d("MicrophoneService Error", "Foreground task error: $e")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                // TODO - implement cathing error action
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
            }
            // ...
        }
    }

    // Mandatory override when extend the Service()
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
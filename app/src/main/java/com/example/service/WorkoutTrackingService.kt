package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.*

class WorkoutTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    private var routineTitle: String = "Active Workout"
    private var startTimestamp: Long = 0L
    private var completedSets: Int = 0
    private var totalSets: Int = 0
    private var isPaused: Boolean = false
    private var pauseTimestamp: Long? = null
    private var totalPausedSeconds: Long = 0L

    companion object {
        const val CHANNEL_ID = "ironlog_workout_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_OPEN_ACTIVE_WORKOUT = "com.example.ACTION_OPEN_ACTIVE_WORKOUT"

        const val EXTRA_ROUTINE_TITLE = "extra_routine_title"
        const val EXTRA_START_TIMESTAMP = "extra_start_timestamp"
        const val EXTRA_COMPLETED_SETS = "extra_completed_sets"
        const val EXTRA_TOTAL_SETS = "extra_total_sets"
        const val EXTRA_IS_PAUSED = "extra_is_paused"
        const val EXTRA_PAUSE_TIMESTAMP = "extra_pause_timestamp"
        const val EXTRA_TOTAL_PAUSED_SECONDS = "extra_total_paused_seconds"

        fun start(
            context: Context,
            routineTitle: String,
            startTimestamp: Long,
            isPaused: Boolean = false,
            pauseTimestamp: Long? = null,
            totalPausedSeconds: Long = 0L
        ) {
            try {
                val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_ROUTINE_TITLE, routineTitle)
                    putExtra(EXTRA_START_TIMESTAMP, startTimestamp)
                    putExtra(EXTRA_IS_PAUSED, isPaused)
                    if (pauseTimestamp != null) putExtra(EXTRA_PAUSE_TIMESTAMP, pauseTimestamp)
                    putExtra(EXTRA_TOTAL_PAUSED_SECONDS, totalPausedSeconds)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("WorkoutTrackingService", "Failed to start foreground service: ${e.message}", e)
            }
        }

        fun update(
            context: Context,
            completedSets: Int,
            totalSets: Int,
            isPaused: Boolean = false,
            pauseTimestamp: Long? = null,
            totalPausedSeconds: Long = 0L
        ) {
            try {
                val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                    action = ACTION_UPDATE
                    putExtra(EXTRA_COMPLETED_SETS, completedSets)
                    putExtra(EXTRA_TOTAL_SETS, totalSets)
                    putExtra(EXTRA_IS_PAUSED, isPaused)
                    if (pauseTimestamp != null) putExtra(EXTRA_PAUSE_TIMESTAMP, pauseTimestamp)
                    putExtra(EXTRA_TOTAL_PAUSED_SECONDS, totalPausedSeconds)
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("WorkoutTrackingService", "Failed to update service: ${e.message}", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, WorkoutTrackingService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("WorkoutTrackingService", "Failed to stop service: ${e.message}", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_ROUTINE_TITLE) ?: "Active Workout"
                val start = intent.getLongExtra(EXTRA_START_TIMESTAMP, System.currentTimeMillis())
                routineTitle = title
                startTimestamp = if (start > 0) start else System.currentTimeMillis()
                isPaused = intent.getBooleanExtra(EXTRA_IS_PAUSED, false)
                pauseTimestamp = if (intent.hasExtra(EXTRA_PAUSE_TIMESTAMP)) intent.getLongExtra(EXTRA_PAUSE_TIMESTAMP, 0L) else null
                totalPausedSeconds = intent.getLongExtra(EXTRA_TOTAL_PAUSED_SECONDS, 0L)
                startTracking()
            }
            ACTION_UPDATE -> {
                completedSets = intent.getIntExtra(EXTRA_COMPLETED_SETS, completedSets)
                totalSets = intent.getIntExtra(EXTRA_TOTAL_SETS, totalSets)
                isPaused = intent.getBooleanExtra(EXTRA_IS_PAUSED, isPaused)
                pauseTimestamp = if (intent.hasExtra(EXTRA_PAUSE_TIMESTAMP)) intent.getLongExtra(EXTRA_PAUSE_TIMESTAMP, 0L) else pauseTimestamp
                totalPausedSeconds = intent.getLongExtra(EXTRA_TOTAL_PAUSED_SECONDS, totalPausedSeconds)
                updateNotification()
            }
            ACTION_STOP -> {
                stopTracking()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        createNotificationChannel()
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("WorkoutTrackingService", "startForeground failed: ${e.message}", e)
        }

        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            Log.e("WorkoutTrackingService", "Failed to update notification: ${e.message}", e)
        }
    }

    private fun buildNotification(): Notification {
        val elapsedSeconds = if (startTimestamp > 0) {
            val now = System.currentTimeMillis()
            val rawElapsed = (now - startTimestamp) / 1000L
            val ongoingPaused = if (isPaused && pauseTimestamp != null && pauseTimestamp!! > 0L) {
                (now - pauseTimestamp!!) / 1000L
            } else {
                0L
            }
            val effectivePaused = totalPausedSeconds + ongoingPaused
            maxOf(0L, rawElapsed - effectivePaused)
        } else {
            0L
        }
        val hours = elapsedSeconds / 3600
        val mins = (elapsedSeconds % 3600) / 60
        val secs = elapsedSeconds % 60
        val formattedTime = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }

        val statusPrefix = if (isPaused) "⏸ Paused" else "⚡ Elapsed"
        val contentText = "$statusPrefix: $formattedTime • Sets Completed: $completedSets"

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_ACTIVE_WORKOUT
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active: $routineTitle")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return builder.build()
    }

    private fun stopTracking() {
        tickerJob?.cancel()
        tickerJob = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i("WorkoutTrackingService", "onTaskRemoved: App swiped away. Preserving active workout in foreground service and Room.")
        // Ensure service remains alive and foreground notification keeps running
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Workout Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active workout timer and completed sets in real-time"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}

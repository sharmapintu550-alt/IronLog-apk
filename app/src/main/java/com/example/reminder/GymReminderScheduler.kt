package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.WeeklySplitEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "GymReminderScheduler"
const val CHANNEL_ID_GYM_REMINDERS = "channel_gym_reminders"
const val EXTRA_SCHEDULED_ID = "extra_scheduled_id"
const val EXTRA_ROUTINE_NAME = "extra_routine_name"
const val EXTRA_ROUTINE_ID = "extra_routine_id"
const val ACTION_START_WORKOUT = "com.example.reminder.ACTION_START_WORKOUT"
const val ACTION_POSTPONE_30M = "com.example.reminder.ACTION_POSTPONE_30M"
const val ACTION_MARK_REST_DAY = "com.example.reminder.ACTION_MARK_REST_DAY"

class GymReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME) ?: "Daily Workout"
        val routineIdStr = intent.getStringExtra(EXTRA_ROUTINE_ID)
        val scheduledIdStr = intent.getStringExtra(EXTRA_SCHEDULED_ID)

        when (action) {
            ACTION_POSTPONE_30M -> {
                // Postpone by 30 minutes
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val nextIntent = Intent(context, GymReminderReceiver::class.java).apply {
                    putExtra(EXTRA_ROUTINE_NAME, routineName)
                    putExtra(EXTRA_ROUTINE_ID, routineIdStr)
                    putExtra(EXTRA_SCHEDULED_ID, scheduledIdStr)
                }
                val pi = PendingIntent.getBroadcast(
                    context,
                    (scheduledIdStr?.hashCode() ?: 100) + 1,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val triggerAt = System.currentTimeMillis() + (30 * 60 * 1000L)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }

                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(scheduledIdStr?.hashCode() ?: 999)
            }
            ACTION_MARK_REST_DAY -> {
                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(scheduledIdStr?.hashCode() ?: 999)
            }
            else -> {
                // Trigger notification alert
                showGymNotification(context, routineName, routineIdStr, scheduledIdStr)
            }
        }
    }

    private fun showGymNotification(
        context: Context,
        routineName: String,
        routineIdStr: String?,
        scheduledIdStr: String?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel if Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_GYM_REMINDERS,
                "Gym Workout Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you when it is time to crush your scheduled workout session"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap content intent -> Open app into workout
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTINE_ID, routineIdStr)
            putExtra(EXTRA_ROUTINE_NAME, routineName)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            scheduledIdStr?.hashCode() ?: 200,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Postpone 30m
        val postponeIntent = Intent(context, GymReminderReceiver::class.java).apply {
            action = ACTION_POSTPONE_30M
            putExtra(EXTRA_ROUTINE_NAME, routineName)
            putExtra(EXTRA_ROUTINE_ID, routineIdStr)
            putExtra(EXTRA_SCHEDULED_ID, scheduledIdStr)
        }
        val postponePi = PendingIntent.getBroadcast(
            context,
            (scheduledIdStr?.hashCode() ?: 300) + 1,
            postponeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Mark as Rest Day
        val restIntent = Intent(context, GymReminderReceiver::class.java).apply {
            action = ACTION_MARK_REST_DAY
            putExtra(EXTRA_SCHEDULED_ID, scheduledIdStr)
        }
        val restPi = PendingIntent.getBroadcast(
            context,
            (scheduledIdStr?.hashCode() ?: 400) + 2,
            restIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GYM_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Gym Time: $routineName")
            .setContentText("Ready to crush your workout today? Tap to launch your session.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(0, "Start", openPendingIntent)
            .addAction(0, "Postpone 30m", postponePi)
            .addAction(0, "Rest Day", restPi)
            .build()

        notificationManager.notify(scheduledIdStr?.hashCode() ?: 999, notification)
    }
}

object GymReminderScheduler {

    fun scheduleReminder(
        context: Context,
        split: WeeklySplitEntity
    ) {
        if (split.isRestDay || split.routineId == null) return
        val reminderTime = split.reminderTime // e.g. "06:30 PM"
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Parse reminderTime ("06:30 PM")
        val parts = reminderTime.split(" ", ":")
        var hour = if (parts.isNotEmpty()) parts[0].toIntOrNull() ?: 18 else 18
        val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 30 else 30
        val isPm = parts.any { it.equals("PM", ignoreCase = true) }
        val isAm = parts.any { it.equals("AM", ignoreCase = true) }
        if (isPm && hour < 12) hour += 12
        if (isAm && hour == 12) hour = 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Align with split.dayOfWeek (1 = Monday ... 7 = Sunday)
            val calDayOfWeek = when (split.dayOfWeek) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            set(Calendar.DAY_OF_WEEK, calDayOfWeek)
        }

        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val intent = Intent(context, GymReminderReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULED_ID, "split_${split.dayOfWeek}")
            putExtra(EXTRA_ROUTINE_NAME, split.routineTitle ?: "Workout")
            putExtra(EXTRA_ROUTINE_ID, split.routineId)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            split.dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
            Log.d(TAG, "Scheduled gym reminder for ${split.routineTitle} at ${cal.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }

    fun cancelReminder(context: Context, dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, GymReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            dayOfWeek,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }
}

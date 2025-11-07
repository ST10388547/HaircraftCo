package com.example.haircraftco

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationsHelper {

    private const val BOOKING_CHANNEL_ID = "booking_channel"
    private const val WELCOME_CHANNEL_ID = "welcome_channel"

    /** 1️⃣ Send Welcome Notification **/
    fun sendWelcomeNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WELCOME_CHANNEL_ID,
                "Welcome Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Channel for welcome messages" }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, WELCOME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Welcome to Haircraft!")
            .setContentText("Your number one hair doctor ✂️")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1001, notification)
    }

    /** 2️⃣ Schedule booking reminders **/
    fun scheduleBookingReminders(
        context: Context,
        bookingId: Int,
        bookingTimeMillis: Long
    ) {
        // Reminder 1: one day before
        scheduleBookingNotification(context, bookingTimeMillis, bookingId, 24 * 60 * 60 * 1000L, "Your booking is tomorrow!")

        // Reminder 2: on the same day (2 hours before)
        scheduleBookingNotification(context, bookingTimeMillis, bookingId + 10000, 2 * 60 * 60 * 1000L, "Your booking is today!")
    }

    private fun scheduleBookingNotification(
        context: Context,
        bookingTimeMillis: Long,
        requestCode: Int,
        remindBeforeMillis: Long,
        message: String
    ) {
        val intent = Intent(context, BookingNotificationReceiver::class.java).apply {
            putExtra("message", message)
            putExtra("bookingId", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            bookingTimeMillis - remindBeforeMillis,
            pendingIntent
        )
    }

    /** 3️⃣ Receiver to show notifications **/
    class BookingNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message") ?: "You have an upcoming booking"
            val bookingId = intent.getIntExtra("bookingId", 0)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create channel for booking reminders
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    BOOKING_CHANNEL_ID,
                    "Booking Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Reminders for upcoming bookings" }
                notificationManager.createNotificationChannel(channel)
            }

            // Open ProfileActivity when clicked
            val openProfileIntent = Intent(context, ProfileActivity::class.java)
            val contentIntent = PendingIntent.getActivity(
                context,
                bookingId,
                openProfileIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, BOOKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Haircraft Reminder")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .build()

            notificationManager.notify(bookingId, notification)
        }
    }
}

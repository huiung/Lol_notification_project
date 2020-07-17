package com.example.lol_notification_project.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.lol_notification_project.R

class RestartService : Service() { //startForegroundService시 notification이 뜨는것을 없애기 위한 방법.  여기서 startService를 호출하고 얘는 종료

    private val channelId = "my_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val builder1 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) //smallIcon
            .setContentTitle("LOL 알리미") //Title
            .setContentText("서비스가 실행 중입니다.") //내용

        val notificationId1 = 101
        val notification = builder1.build()
        startForeground(notificationId1, notification);
        val intent2 = Intent(this, UndeadService::class.java)
        startService(intent2)
        stopForeground(true)
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_des)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

package com.example.lol_notification_project.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.lol_notification_project.service.UndeadService

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, p1: Intent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //오레오 이상에선 background에서 startService 불가
            val intent = Intent(context, UndeadService::class.java)
            context.startForegroundService(intent)
        }
        else {
            val intent = Intent(context, UndeadService::class.java)
            context.startService(intent)
        }
    }
}

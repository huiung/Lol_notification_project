package com.example.lol_notification_project.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.lol_notification_project.Service.RestartService
import com.example.lol_notification_project.Service.UndeadService

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //오레오 이상에선 background에서 startService 불가
            val intent = Intent(context, RestartService::class.java)
            context.startForegroundService(intent)
        }
        else {
            val intent = Intent(context, UndeadService::class.java)
            context.startService(intent)
        }
    }
}

package com.example.lol_notification_project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //오레오 이상에선 background에서 startService 불가
            val intent = Intent(p0, UndeadService.javaClass)
            p0?.startForegroundService(intent)
        }
        else {
            val intent = Intent(p0, UndeadService.javaClass)
            p0?.startService(intent)
        }

    }
}
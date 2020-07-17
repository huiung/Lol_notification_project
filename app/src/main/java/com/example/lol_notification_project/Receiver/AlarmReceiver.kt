package com.example.lol_notification_project.Receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.lol_notification_project.Service.RestartService
import com.example.lol_notification_project.Service.UndeadService

class AlarmReceiver : BroadcastReceiver(){
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("mytag","리시버 동작")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //오레오 이상에선 background에서 startService 불가
            val intent = Intent(p0, RestartService::class.java)
            Log.d("mytag","서비스 재개!")
            p0?.startForegroundService(intent)
        }
        else {
            val intent = Intent(p0, UndeadService::class.java)
            p0?.startService(intent)
        }
    }
}
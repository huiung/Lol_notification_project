package com.example.lol_notification_project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (null == UndeadService.serviceIntent) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java);
            startService(foregroundServiceIntent);
        } else { //실행중이면 Intent저장
            foregroundServiceIntent = UndeadService.serviceIntent;
        }
    }

    override fun onDestroy() { //파괴되면 stopService 호출
        super.onDestroy()

        if(null != foregroundServiceIntent) {
            stopService(foregroundServiceIntent)
            foregroundServiceIntent = null;
        }

    }

}

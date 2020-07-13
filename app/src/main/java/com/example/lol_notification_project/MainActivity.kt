package com.example.lol_notification_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lol_notification_project.JsonType.Spectator
import com.example.lol_notification_project.JsonType.Summoner
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var id: String
    lateinit var call: Call<Summoner>
    lateinit var call2: Call<Spectator>
    private val api_key = "RGAPI-18b4aaf9-942f-4072-acfa-56842c53d2e8"
    var summonername = "나는평타싸개유저"

    private val channelId = "my_channel"

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

    fun sendNotification() {

        val GROUP_KEY_NOTIFY = "group_key_notify"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder1 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) //smallIcon
            .setContentTitle("LOL 알리미") //Title
            .setContentText("${summonername} 소환사가 게임을 시작했습니다.") //내용
            .setAutoCancel(true) //알림 클릭시 알림 제거 여부
            .setContentIntent(pendingIntent) //클릭시 pendingIntent의 Activity로 이동

        val notificationId1 = 100

        NotificationManagerCompat.from(this).apply {
            notify(notificationId1, builder1.build())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        retrofit = RetrofitClient.getInstnace()
        myAPI = retrofit.create(MyServer::class.java)

        call = myAPI.getsummoner(summonername, api_key)

        val r = Runnable() {
            /*
            val time = System.currentTimeMillis() + 5*1000

            while(System.currentTimeMillis() < time) {
                synchronized(this) {
                    try {
                        Thread.sleep(time - System.currentTimeMillis())
                    } catch (e: Exception) {

                    }
                }
            }
            */
            var response = call.execute()
            if(response.isSuccessful) {//먼저 소환사 이름으로 encryptedId 얻어옴
                id = response.body()?.id ?: "no id"

                call2 = myAPI.getspectator(id, api_key)
                var response2 = call2.execute() //encryptedId 이용해서 현재 게임 여부 확인
                if (response2.isSuccessful) { //응답이 왔다면 게임중임 따라서 알림 발사
                    sendNotification()
                }
                else {
                    //게임중 아님 앱 화면에 표시
                }
            }
            else {
                //그런 소환사 없음 -> DB 추가할때 처리
            }
        }

        val t = Thread(r)
        t.start()
    }



}

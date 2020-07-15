package com.example.lol_notification_project

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lol_notification_project.JsonType.Spectator
import com.example.lol_notification_project.JsonType.Summoner
import retrofit2.Call
import retrofit2.Retrofit
import java.util.*

class UndeadService : Service() { //서비스 manifest에 등록해야함 안해서 개삽질. ..

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var id: String
    lateinit var call: Call<Summoner>
    lateinit var call2: Call<Spectator>
    private val api_key = "RGAPI-5a72056a-0bfa-49fe-8440-f80b8933958e"
    private var summonername = "CCrazy"
    private val channelId = "my_channel"


    companion object {
        var serviceIntent: Intent? = null //static
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("mytag", "하윙")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceIntent = intent
        createNotificationChannel()
        initNotification()
        retrofit = RetrofitClient.getInstnace()
        myAPI = retrofit.create(MyServer::class.java)

        val r = Runnable() {

            val time = System.currentTimeMillis() + 5*1000
            var cnt = 0
            while(true) { //
                synchronized(this) {
                    try {
                        call = myAPI.getsummoner(summonername, api_key)
                        var response = call.execute()
                        if(response.isSuccessful) {//먼저 소환사 이름으로 encryptedId 얻어옴
                            id = response.body()?.id ?: "no id"

                            call2 = myAPI.getspectator(id, api_key)
                            var response2 = call2.execute() //encryptedId 이용해서 현재 게임 여부 확인
                            if (response2.isSuccessful) { //응답이 왔다면 게임중임 따라서 알림 발사

                                /*for(i in 0..9)
                                    Log.d("mytag", response2.body()!!.participants[i].summonerName)*/
                                Log.d("mytag", cnt++.toString())
                                sendNotification()
                            }
                            else {
                                //게임중 아님 앱 화면에 표시
                                Log.d("mytag", "현재 게임중이 아닙니다.")
                            }
                        }
                        else {
                            //그런 소환사 없음 -> DB 추가할때 처리
                        }

                        Thread.sleep(10000) //1초 쉬고 쿼리 날림
                    } catch (e: Exception) {
                    }
                }
            }
        }

        val t = Thread(r)
        t.start()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() { //Service Destroy 시 Alarm을 호출함 -> Alarm은 받은 intent를 broadcats -> AlarmErceiver가 이를 수신하여 서비스 재시작
        super.onDestroy()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)
        val intent = Intent("com.example.lol_notification.ALARM")
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3)
        val intent = Intent("com.example.lol_notification.ALARM")
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
    }

    fun sendNotification() {

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

    fun initNotification() {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder1 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) //smallIcon
            .setContentTitle("LOL 알리미") //Title
            .setContentText("서비스가 실행 중입니다.") //내용
            .setAutoCancel(true) //알림 클릭시 알림 제거 여부
            .setContentIntent(pendingIntent) //클릭시 pendingIntent의 Activity로 이동

        val notificationId1 = 101

        val notification = builder1.build()

        startForeground(notificationId1, notification);
    }

}
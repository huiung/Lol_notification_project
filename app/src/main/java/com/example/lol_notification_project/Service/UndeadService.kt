package com.example.lol_notification_project.Service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lol_notification_project.Receiver.AlarmReceiver
import com.example.lol_notification_project.JsonType.Spectator
import com.example.lol_notification_project.JsonType.Summoner
import com.example.lol_notification_project.MainActivity
import com.example.lol_notification_project.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.Retrofit2.MyServer
import com.example.lol_notification_project.Retrofit2.RetrofitClient
import retrofit2.Call
import retrofit2.Retrofit
import java.util.*

class UndeadService : Service() {

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var id: String
    lateinit var call: Call<Summoner>
    lateinit var call2: Call<Spectator>
    private val api_key = "RGAPI-75374fc3-04ec-47e3-8bef-855be879266f"
    private val channelId = "my_channel"
    var mainThread: Thread? = null

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
        retrofit = RetrofitClient.getInstnace()
        myAPI = retrofit.create(MyServer::class.java)

        mainThread = Thread( Runnable() {
            var cnt = 0
            var flag = true
            while(flag) {
                synchronized(this) {
                    try {
                        val allname = Preferences.getAllKeys(applicationContext)
                        var iterator = allname.iterator()
                        var curint = 0
                        while(iterator.hasNext()) {
                            val curname = iterator.next()
                            Log.d("mytag", curname)

                            call = myAPI.getsummoner(curname, api_key)
                            var response = call.execute()
                            if (response.isSuccessful) {//먼저 소환사 이름으로 encryptedId 얻어옴
                                id = response.body()?.id ?: "no id"

                                call2 = myAPI.getspectator(id, api_key)
                                var response2 = call2.execute() //encryptedId 이용해서 현재 게임 여부 확인
                                if (response2.isSuccessful) { //응답이 왔다면 게임중임 따라서 알림 발사

                                    /*for(i in 0..9)
                                    Log.d("mytag", response2.body()!!.participants[i].summonerName)*/
                                    sendNotification(curname, curint++)
                                } else {
                                    //게임중 아님 앱 화면에 표시
                                    Log.d("mytag", "현재 게임중이 아닙니다.")
                                }
                            } else {
                                //존재하는 소환사만 애초에 등록 했으므로 올일 없음.
                            }
                        } //for
                        Thread.sleep(12000) //60초 쉬고 쿼리 날림
                    } catch (e: Exception) {
                        flag = false
                    }
                }
            } //while
        })
        mainThread?.start()

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() { //Service Destroy 시 Alarm을 호출함 -> Alarm은 받은 intent를 broadcats -> Alarmreceiver가 이를 수신하여 서비스 재시작
        super.onDestroy()

        Log.d("mytag", "서비스 onDestroy")!!
        setAlarmTimer() //알람을 키고 진행중인 쓰레드 모두 종료
        Thread.currentThread().interrupt()
        mainThread?.interrupt()
        mainThread = null
        serviceIntent = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) { //Task Kill시
        super.onTaskRemoved(rootIntent)

        Log.d("mytag", "onTaskRemoved")!!
        setAlarmTimer()
    }

    private fun sendNotification(curname: String, id: Int) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder1 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) //smallIcon
            .setContentTitle("LOL 알리미") //Title
            .setContentText("${curname} 소환사가 게임을 시작했습니다.") //내용
            .setAutoCancel(true) //알림 클릭시 알림 제거 여부
            .setContentIntent(pendingIntent) //클릭시 pendingIntent의 Activity로 이동


        NotificationManagerCompat.from(this).apply {
            notify(id, builder1.build())
        }
    }

    private fun setAlarmTimer() { //1초후 알람 작동.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 1)
        val intent = Intent(this, AlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
    }

    private fun createNotificationChannel() { //채널 생성 오레오 이상부터 필수.
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
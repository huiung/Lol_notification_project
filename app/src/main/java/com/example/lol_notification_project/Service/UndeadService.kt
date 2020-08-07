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
import com.example.lol_notification_project.Model.Data.Spectator
import com.example.lol_notification_project.View.MainActivity
import com.example.lol_notification_project.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.Model.SummonerAPI
import com.example.lol_notification_project.Model.RetrofitClient
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Retrofit
import java.util.*

class UndeadService : Service() {

    lateinit var retrofit: Retrofit
    lateinit var myAPI: SummonerAPI
    lateinit var scope: CoroutineScope
    var api_key: String? = " "
    private val channelId = "my_channel"
    var isswitch = false
    var job: Job? = null

    companion object {
        var serviceIntent: Intent? = null //static
    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Dispatchers.Default)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isswitch = Preferences.getBool(this, "switch")
        serviceIntent = intent
        createNotificationChannel()
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()
        api_key = Preferences.getAPI(this, "Api_key")

        api_key?.let { api_key ->
            job = scope.launch {
                var flag = true
                while (flag) {
                        try {
                            val allname = Preferences.getAll(baseContext)
                            var curint = 0
                            allname?.let {
                                for ((key, value) in it.entries) {
                                    val curname = key
                                    val curId = value.toString()
                                    var response2 = myAPI.getspectator(curId, api_key)
                                    if (response2.isSuccessful) { //응답이 왔다면 게임중임 따라서 알림 발사
                                        if (Preferences.getLong(baseContext, curname + " game") != response2.body()!!.gameId) { //동일게임이면 알림 X
                                            response2.body()!!.gameId?.let {
                                                Preferences.setLong(baseContext, curname + " game", it)
                                            }
                                            sendNotification(curname, curint++)
                                        } else { //이전에 알림 보낸게임과 동일게임임
                                            Log.d("mytag", "동일게임")
                                        }
                                    } else {
                                        //게임중 아님 앱 화면에 표시
                                        Log.d("mytag", "현재 게임중이 아닙니다.")
                                    }
                                }
                            }
                            delay(60000) //80초 쉬고 쿼리 날림
                            isswitch = Preferences.getBool(baseContext, "switch")
                            if(!isswitch) {
                                stopSelf(startId)
                            }
                        } catch (e: Exception) {
                            flag = false
                        }
                } //while
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() { //Service Destroy 시 Alarm을 호출함 -> Alarm은 받은 intent를 broadcats -> Alarmreceiver가 이를 수신하여 서비스 재시작
        super.onDestroy()

        Log.d("mytag", "서비스 onDestroy")
        //알람을 키고 진행중인 job cancel
        if(isswitch) setAlarmTimer()

        job?.let {
            it.cancel()
        }
        serviceIntent = null
    }


    override fun onTaskRemoved(rootIntent: Intent?) { //Task Kill시
        super.onTaskRemoved(rootIntent)

        Log.d("mytag", "onTaskRemoved")
        if(isswitch) setAlarmTimer()
        job?.let {
            it.cancel()
        }
        serviceIntent = null
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
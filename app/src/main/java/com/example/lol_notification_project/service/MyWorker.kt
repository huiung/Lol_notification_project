package com.example.lol_notification_project.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lol_notification_project.R
import com.example.lol_notification_project.model.Preferences
import com.example.lol_notification_project.model.RetrofitClient
import com.example.lol_notification_project.model.SummonerAPI
import com.example.lol_notification_project.view.MainActivity
import kotlinx.coroutines.*
import retrofit2.Retrofit

class MyWorker(private val context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    lateinit var retrofit: Retrofit
    lateinit var myAPI: SummonerAPI
    var api_key: String? = " "
    private val channelId = "my_channel"


    override suspend fun doWork(): Result = coroutineScope { //기본 default Dispacthers

        createNotificationChannel()
        Log.d("mytag", "작업 시작!")
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()
        api_key = Preferences.getAPI(context, "Api_key")

        api_key?.let { api_key ->
            try {
                val allname = Preferences.getAll(context)
                var curint = 0
                allname?.let {
                        for ((key, value) in it.entries) {
                            val curname = key
                            val curId = value.toString()
                            var response2 = myAPI.getspectator(curId, api_key)
                            if (response2.isSuccessful) { //응답이 왔다면 게임중임 따라서 알림 발사
                                if (Preferences.getLong(
                                        context,
                                        curname + " game"
                                    ) != response2.body()!!.gameId
                                ) { //동일게임이면 알림 X
                                    response2.body()!!.gameId?.let {
                                        Preferences.setLong(context, curname + " game", it)
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
            } catch (e: Exception) {
            }
        }

        Result.success()
    }


    private fun createNotificationChannel() { //채널 생성 오레오 이상부터 필수.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_des)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(curname: String, id: Int) {

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder1 = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) //smallIcon
            .setContentTitle("LOL 알리미") //Title
            .setContentText("${curname} 소환사가 게임을 시작했습니다.") //내용
            .setAutoCancel(true) //알림 클릭시 알림 제거 여부
            .setContentIntent(pendingIntent) //클릭시 pendingIntent의 Activity로 이동


        NotificationManagerCompat.from(context).apply {
            notify(id, builder1.build())
        }
    }


}
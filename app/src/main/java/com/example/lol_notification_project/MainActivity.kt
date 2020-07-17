package com.example.lol_notification_project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lol_notification_project.JsonType.Summoner
import com.example.lol_notification_project.Retrofit2.MyServer
import com.example.lol_notification_project.Retrofit2.RetrofitClient
import com.example.lol_notification_project.Service.UndeadService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var call: Call<Summoner>
    lateinit var id: String
    private val api_key = "RGAPI-75374fc3-04ec-47e3-8bef-855be879266f"

    companion object {
        var mToast: Toast? = null //static
    }


    val handler = @SuppressLint("HandlerLeak")
    object: Handler() {
        override fun handleMessage(msg: Message) { //스레드가 핸들러에게 메시지를 보낼 때 호출됨
            val bundle = msg.data
            val str = bundle.getString("content")

            if(str == null) makeToastNoexist()
            else makeToast(str)

            super.handleMessage(msg)
        }
    }

    fun makeToast(str: String) {
        if (Preferences.getbool(applicationContext, str)) {
            if (mToast != null) { //토스트 중복 방지
                mToast!!.cancel()
                mToast = Toast.makeText(applicationContext, "이미 등록된 아이디 입니다", Toast.LENGTH_SHORT
                )
            } else mToast =
                Toast.makeText(applicationContext, "이미 등록된 아이디 입니다", Toast.LENGTH_SHORT)
            mToast?.show();

        } else {
            Preferences.setbool(applicationContext, str, true)
            if (mToast != null) {
                mToast!!.cancel()
                mToast = Toast.makeText(applicationContext, "등록 완료", Toast.LENGTH_SHORT)
            } else mToast =
                Toast.makeText(applicationContext, "등록 완료", Toast.LENGTH_SHORT)
            mToast?.show();
        }
    }

    fun makeToastNoexist() {
        if (mToast != null) {
            mToast!!.cancel()
            mToast = Toast.makeText(applicationContext, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT)
        } else mToast =
            Toast.makeText(applicationContext, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT)
        mToast?.show();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofit = RetrofitClient.getInstnace()
        myAPI = retrofit.create(MyServer::class.java)


        button.setOnClickListener { //등록
            val str = editText.text.toString()
            if(str != "") { //아이디가 있는지 확인해야 해서 Retrofit2 이용 -> 스레드 하나 생성해야함 -> Toast띄우는건 UI 작업 -> main Thread에서 해야하니까 핸들러 이용
                val r = Runnable {
                    call = myAPI.getsummoner(str, api_key)
                    var response = call.execute()
                    if (response.isSuccessful) {//먼저 소환사 이름으로 encryptedId 얻어옴
                        val msg = handler.obtainMessage()
                        val bundle = Bundle()
                        bundle.putString("content", str)
                        msg.data = bundle
                        handler.sendMessage(msg)
                    }
                    else {
                        val msg = handler.obtainMessage()
                        handler.sendMessage(msg)
                    }
                }
                val t = Thread(r)
                t.start()
            }
        }

        button2.setOnClickListener {//삭제

            val str = editText.text.toString()
            if(str != "") {
                if(Preferences.getbool(applicationContext, str)) {
                    Preferences.removebool(applicationContext, str)
                    if(mToast != null) {
                        mToast!!.cancel()
                        mToast = Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_SHORT)
                    }
                    else mToast = Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_SHORT)
                    mToast?.show();
                }
                else {
                    if(mToast != null) {
                        mToast!!.cancel()
                        mToast = Toast.makeText(applicationContext, "등록되지 않은 소환사 입니다", Toast.LENGTH_SHORT)
                    }
                    else mToast = Toast.makeText(applicationContext, "등록되지 않은 소환사 입니다", Toast.LENGTH_SHORT)
                    mToast?.show();
                }
            }
        }


        if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java);
            startService(foregroundServiceIntent);
        } else { //실행중이면 Intent저장
            foregroundServiceIntent = UndeadService.serviceIntent;
        }
    }

    override fun onDestroy() { //파괴되면 stopService 호출
        super.onDestroy()

        Log.d("mytag", "onDestroy!!")
        Log.d("mytag", foregroundServiceIntent.toString())

    }

}

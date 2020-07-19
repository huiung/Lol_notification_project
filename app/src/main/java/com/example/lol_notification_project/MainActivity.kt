package com.example.lol_notification_project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lol_notification_project.JsonType.LeagueEntryDTO
import com.example.lol_notification_project.JsonType.Summoner
import com.example.lol_notification_project.Retrofit2.MyServer
import com.example.lol_notification_project.Retrofit2.RetrofitClient
import com.example.lol_notification_project.Service.UndeadService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Retrofit
import java.net.Inet4Address


class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var call: Call<Summoner>
    lateinit var call2: Call<Set<LeagueEntryDTO>>
    lateinit var asynctask: SummonerAsyncTask
    lateinit var recyclerView: RecyclerView
    private val api_key = "RGAPI-209a229e-c2fa-4b24-bc9a-76b2f8ac7096"

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
            mToast?.show()

        } else {
            Preferences.setbool(applicationContext, str, true)
            if (mToast != null) {
                mToast!!.cancel()
                mToast = Toast.makeText(applicationContext, "등록 완료", Toast.LENGTH_SHORT)
            } else mToast =
                Toast.makeText(applicationContext, "등록 완료", Toast.LENGTH_SHORT)
            mToast?.show()
        }
    }

    fun makeToastNoexist() {
        if (mToast != null) {
            mToast!!.cancel()
            mToast = Toast.makeText(applicationContext, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT)
        } else mToast =
            Toast.makeText(applicationContext, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT)
        mToast?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()


        button.setOnClickListener { //등록
            val str = editText.text.toString()
            if(str != "") { //아이디가 있는지 확인해야 해서 Retrofit2 이용 -> 스레드 하나 생성해야함 -> Toast띄우는건 UI 작업 -> main Thread에서 해야하니까 핸들러 이용 AsyncTask 쓰면 편하지만 일단 이렇게 해버림.
                val r = Runnable {
                    call = myAPI.getsummoner(str, api_key)
                    val response = call.execute()
                    if (response.isSuccessful) {
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
            if(str != "") if(Preferences.getbool(applicationContext, str)) {
                Preferences.removebool(applicationContext, str)
                if(mToast != null) {
                    mToast!!.cancel()
                    mToast = Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_SHORT)
                }
                else mToast = Toast.makeText(applicationContext, "삭제 완료", Toast.LENGTH_SHORT)
                mToast?.show()
            }
            else {
                if(mToast != null) {
                    mToast!!.cancel()
                    mToast = Toast.makeText(applicationContext, "등록되지 않은 소환사 입니다", Toast.LENGTH_SHORT)
                }
                else mToast = Toast.makeText(applicationContext, "등록되지 않은 소환사 입니다", Toast.LENGTH_SHORT)
                mToast?.show()
            }
        }

        asynctask = SummonerAsyncTask()
        asynctask.execute()

        if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java);
            startService(foregroundServiceIntent)
        } else { //실행중이면 Intent저장
            foregroundServiceIntent = UndeadService.serviceIntent
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        asynctask.cancel(false)
        Log.d("mytag", "onDestroy!!")

    }

    inner class SummonerAsyncTask : AsyncTask<Void, Void, Void>() { //앱 실행시 등록된 소환사들을 cardview 형태로 뿌려줌

        lateinit var allname: MutableSet<String>
        lateinit var iterator: MutableIterator<String>
        var summonerInfo: ArrayList<SummonerInfo> = ArrayList()

        override fun onPreExecute() { //Retrofit 세팅
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: Void?): Void? { //API 호출해서 소환사 이름, 레벨, Icon ID, 티어 정보, 랭크게임 승 / 패, 현재 포인트 각각 저장.

            allname = Preferences.getAllKeys(applicationContext)
            iterator = allname.iterator()

            while(iterator.hasNext() && !isCancelled) {
                val curInfo = SummonerInfo()
                var id: String? = null
                val curname = iterator.next()
                call = myAPI.getsummoner(curname, api_key)
                val response = call.execute()
                if (response.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                    curInfo.name = response.body()!!.name
                    curInfo.profileIconId = response.body()!!.profileIconId
                    curInfo.summonerLevel = response.body()!!.summonerLevel
                    id = response.body()!!.id //Encrypted summonerId

                    call2 = myAPI.getLeague(id, api_key)
                    val response2 = call2.execute()
                    if(response2.isSuccessful) { //League에서 티어 랭크 승/패 포인트 알 수 있음 언랭이면 모든값 null

                        val Infoiterator = response2.body()!!.iterator()
                        while(Infoiterator.hasNext()) {
                            val curLeague = Infoiterator.next()
                            if(curLeague.queueType == "RANKED_SOLO_5x5") { //솔로랭크만 확인
                                curInfo.leaguePoints = curLeague.leaguePoints
                                curInfo.wins = curLeague.wins
                                curInfo.losses = curLeague.losses
                                curInfo.rank = curLeague.rank
                                curInfo.tier = curLeague.tier

                                summonerInfo.add(curInfo) //각 소환사에 대한 정보 추가
                            }
                            else continue;
                        }
                    }
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Void?) { // recyclerView ㄲㄲ
            super.onPostExecute(result)
            recyclerView = recyclerview_main
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.adapter = SummonerAdapter(summonerInfo, applicationContext)
        }
    }

}

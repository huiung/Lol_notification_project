package com.example.lol_notification_project

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lol_notification_project.JsonType.LeagueEntryDTO
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
    lateinit var call2: Call<Set<LeagueEntryDTO>>
    lateinit var asynctask: SummonerAsyncTask
    lateinit var storeuser: StoreUser
    lateinit var recyclerView: RecyclerView
    var api_key: String? = " "
    var isswitch = false

    companion object {
        var mToast: Toast? = null //static
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isswitch = Preferences.getBool(applicationContext, "switch")
        api_key = Preferences.getAPI(applicationContext, "Api_key")
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()
        asynctask = SummonerAsyncTask()

        if(isswitch) {
            switch1.toggle()
        }

        button.setOnClickListener { //등록
            storeuser = StoreUser()
            storeuser.execute()
        }

        button2.setOnClickListener {//삭제
            val str = editText.text.toString()
            if(Preferences.getString(applicationContext, str) != "NoID") {
                Preferences.removeString(applicationContext, str)
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

        button3.setOnClickListener {
            val str = editText2.text.toString()
            Log.d("mytag", str)
            api_key = str
            Preferences.setAPI(applicationContext, "Api_key", str)
            if(mToast != null) {
                mToast!!.cancel()
                mToast = Toast.makeText(applicationContext, "변경 완료", Toast.LENGTH_SHORT)
            }
            else mToast = Toast.makeText(applicationContext, "변경 완료", Toast.LENGTH_SHORT)
            mToast?.show()
        }

        switch1.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if(b) {
                Preferences.setBool(applicationContext, "switch", true)
                if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
                    foregroundServiceIntent = Intent(this, UndeadService::class.java);
                    startService(foregroundServiceIntent)
                } else { //실행중이면 Intent저장
                    foregroundServiceIntent = UndeadService.serviceIntent
                }
            }
            else {
                Preferences.setBool(applicationContext, "switch", false)
            }
        }

        asynctask.execute()


        if(isswitch) {
            if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
                foregroundServiceIntent = Intent(this, UndeadService::class.java);
                startService(foregroundServiceIntent)
            } else { //실행중이면 Intent저장
                foregroundServiceIntent = UndeadService.serviceIntent
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        asynctask.cancel(false)
    }

    inner class SummonerAsyncTask : AsyncTask<Void, Void, Void>() { //앱 실행시 등록된 소환사들을 cardview 형태로 뿌려줌

        lateinit var allname: MutableMap<String, *>
        lateinit var iterator: MutableIterator<String>
        var summonerInfo: ArrayList<SummonerInfo> = ArrayList()


        override fun doInBackground(vararg p0: Void?): Void? { //API 호출해서 소환사 이름, 레벨, Icon ID, 티어 정보, 랭크게임 승 / 패, 현재 포인트 각각 저장.
            allname = Preferences.getAll(applicationContext)!!
            for ((key, value) in allname.entries) {
                if(isCancelled) break
                else {
                    api_key?.let { api_key->
                        val curname = key
                        val curid = value.toString()
                        val curInfo = SummonerInfo()
                        call = myAPI.getsummoner(curname, api_key)
                        val response = call.execute()
                        if (response.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                            curInfo.name = response.body()!!.name
                            curInfo.profileIconId = response.body()!!.profileIconId
                            curInfo.summonerLevel = response.body()!!.summonerLevel
                        }
                        call2 = myAPI.getLeague(curid, api_key)
                        val response2 = call2.execute()
                        if (response2.isSuccessful) { //League에서 티어 랭크 승/패 포인트 알 수 있음 언랭이면 모든값 null
                            val Infoiterator = response2.body()!!.iterator()
                            while (Infoiterator.hasNext()) {
                                val curLeague = Infoiterator.next()
                                if (curLeague.queueType == "RANKED_SOLO_5x5") { //솔로랭크만 확인
                                    curInfo.leaguePoints = curLeague.leaguePoints
                                    curInfo.wins = curLeague.wins
                                    curInfo.losses = curLeague.losses
                                    curInfo.rank = curLeague.rank
                                    curInfo.tier = curLeague.tier
                                    summonerInfo.add(curInfo) //각 소환사에 대한 정보 추가
                                } else continue;
                            }
                        }
                    }
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) { // recyclerView ㄲㄲ
            super.onPostExecute(result)
            recyclerView = recyclerview_main
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.adapter = SummonerAdapter(summonerInfo, applicationContext)
        }
    }

    inner class StoreUser : AsyncTask<Void, Void, Void>() {

        val str = editText.text.toString()
        var cryptedid:String? = null
        override fun doInBackground(vararg p0: Void?): Void? {
            if(str != "" && !isCancelled) {
                api_key?.let {api_key->
                    call = myAPI.getsummoner(str, api_key)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        cryptedid = response.body()?.id
                    }
                }
            }
            return null
        }
        override fun onPostExecute(result: Void?) {
            if(cryptedid == null) makeToastNoexist()
            else makeToast(str, cryptedid!!)
        }
    }


    fun makeToast(key: String, value: String) {
        if (Preferences.getString(applicationContext, key) != "NoID") {
            if (mToast != null) { //토스트 중복 방지
                mToast!!.cancel()
                mToast = Toast.makeText(applicationContext, "이미 등록된 아이디 입니다", Toast.LENGTH_SHORT)
            }
            else mToast = Toast.makeText(applicationContext, "이미 등록된 아이디 입니다", Toast.LENGTH_SHORT)
            mToast?.show()

        } else {
            Preferences.setString(applicationContext, key, value)
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

}

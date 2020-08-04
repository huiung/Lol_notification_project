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
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import retrofit2.Call
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    lateinit var retrofit: Retrofit
    lateinit var myAPI: MyServer
    lateinit var call: Call<Summoner>
    lateinit var call2: Call<Set<LeagueEntryDTO>>
    val summonerAdapter = SummonerAdapter(arrayListOf(), this)

    lateinit var allname: MutableMap<String, *>
    lateinit var iterator: MutableIterator<String>
    var summonerInfo: ArrayList<SummonerInfo> = ArrayList()

    var summonerJob: Job? = null
    var showJob: Job? = null
    var storeUserJob: Job? = null
    var showUserJob: Job? = null

    var scope = CoroutineScope(Dispatchers.Default)
    var api_key: String? = " "
    var isswitch = false

    companion object {
        var mToast: Toast? = null //static
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isswitch = Preferences.getBool(this, "switch")
        api_key = Preferences.getAPI(this, "Api_key")
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()

        if(isswitch) {
            switch1.toggle()
        }

        button.setOnClickListener { //등록
            storeUserJob = scope.launch {
                if(isActive) {
                    val id = StoreCoroutine()
                    showUserJob = CoroutineScope(Dispatchers.Main).launch {
                        if (id.second == null) makeToastComment("존재하지 않는 아이디입니다.")
                        else makeToast(id.first!!, id.second!!)
                    }
                }
            }
        }

        button2.setOnClickListener {//삭제
            val str = editText.text.toString()
            if(Preferences.getString(this, str) != "NoID") {
                Preferences.removeString(this, str)
                makeToastComment("삭제 완료")
            }
            else {
                makeToastComment("등록되지 않은 소환사 입니다.")
            }
        }

        button3.setOnClickListener {
            val str = editText2.text.toString()
            api_key = str
            Preferences.setAPI(this, "Api_key", str)
            makeToastComment("변경 완료")
        }

        switch1.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if(b) {
                Preferences.setBool(this, "switch", true)
                if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
                    foregroundServiceIntent = Intent(this, UndeadService::class.java);
                    startService(foregroundServiceIntent)
                } else { //실행중이면 Intent저장
                    foregroundServiceIntent = UndeadService.serviceIntent
                }
            }
            else {
                Preferences.setBool(this, "switch", false)
            }
        }

        swipe_refresh.setOnRefreshListener {
            Log.d("mytag", "하이^^")
            summonerJob = scope.launch {
                if(isActive) {
                    SummonerCoroutine()
                    showJob = CoroutineScope(Dispatchers.Main).launch {
                        summonerAdapter.updateSummoner(summonerInfo)
                        recyclerview_main.adapter = summonerAdapter
                    }
                }
                swipe_refresh.isRefreshing = false
            }
        }

        summonerJob = scope.launch {
            if(isActive) {
                SummonerCoroutine()
                showJob = CoroutineScope(Dispatchers.Main).launch {
                    recyclerview_main.apply {
                        layoutManager = LinearLayoutManager(baseContext)
                        summonerAdapter.updateSummoner(summonerInfo)
                        adapter = summonerAdapter
                    }
                }
            }
        }

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
        summonerJob?.let {
            it.cancel()
        }
        showJob?.let {
            it.cancel()
        }
        storeUserJob?.let {
            it.cancel()
        }
        showUserJob?.let{
            it.cancel()
        }
    }


    fun SummonerCoroutine() {
        summonerInfo.clear()
        allname = Preferences.getAll(this)!!
        for ((key, value) in allname.entries) {
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

    fun StoreCoroutine(): Pair<String?, String?> {
        val str = editText.text.toString()
        var cryptedid:String? = null

        if(str != "") {
            api_key?.let {api_key->
                call = myAPI.getsummoner(str, api_key)
                val response = call.execute()
                if (response.isSuccessful) {
                    cryptedid = response.body()?.id
                }
            }
        }
        return Pair<String?, String?>(str, cryptedid)
    }

    fun makeToast(key: String, value: String) {
        if (Preferences.getString(this, key) != "NoID") {
            makeToastComment("이미 등록된 아이디 입니다.")

        } else {
            Preferences.setString(this, key, value)
            makeToastComment("등록 완료")
        }
    }

    fun makeToastComment(str: String) {
        if (mToast != null) {
            mToast!!.cancel()
            mToast = Toast.makeText(this, str, Toast.LENGTH_SHORT)
        } else mToast =
            Toast.makeText(this, str, Toast.LENGTH_SHORT)
        mToast?.show()
    }

}

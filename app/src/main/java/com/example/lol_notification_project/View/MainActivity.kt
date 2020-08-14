package com.example.lol_notification_project.View

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lol_notification_project.Model.SummonerAPI
import com.example.lol_notification_project.Model.RetrofitClient
import com.example.lol_notification_project.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.Service.UndeadService
import com.example.lol_notification_project.SummonerAdapter
import com.example.lol_notification_project.SummonerInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    var foregroundServiceIntent: Intent? = null

    lateinit var retrofit: Retrofit
    lateinit var myAPI: SummonerAPI
    val summonerAdapter = SummonerAdapter(arrayListOf(), this)

    lateinit var allname: MutableMap<String, *>
    lateinit var iterator: MutableIterator<String>
    var summonerInfo: ArrayList<SummonerInfo> = ArrayList()

    var summonerJob: Job? = null
    var storeUserJob: Job? = null

    var scope = CoroutineScope(Dispatchers.Default)
    var api_key: String? = " "
    var isswitch = false

    companion object {
        var mToast: Toast? = null //static
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isswitch = Preferences.getBool(
            this,
            "switch"
        )
        api_key = Preferences.getAPI(
            this,
            "Api_key"
        )
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()

        if (isswitch) {
            switch1.toggle()
        }

        registerbtn.setOnClickListener {
            //등록
            storeUserJob = scope.launch {
                if (isActive) {
                    val id = storeid()
                    withContext(Dispatchers.Main) {
                        if (id.second == null) makeToastComment("존재하지 않는 아이디입니다.")
                        else makeToast(id.first!!, id.second!!)
                        editText.setText("")
                    }
                }
            }
        }

        deletebtn.setOnClickListener {
            //삭제
            val str = editText.text.toString()
            if (Preferences.getString(
                    this,
                    str
                ) != "NoID"
            ) {
                Preferences.removeString(
                    this,
                    str
                )
                makeToastComment("삭제 완료")
            } else {
                makeToastComment("등록되지 않은 소환사 입니다.")
            }

            editText.setText("")
        }

        changekeybtn.setOnClickListener {
            api_key = editText2.text.toString()
            Preferences.setAPI(
                this,
                "Api_key",
                api_key!!
            )
            makeToastComment("변경 완료")
            editText2.setText("")
        }

        Link_button.setOnClickListener {

            //startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://auth.riotgames.com/login#client_id=riot-developer-portal&redirect_uri=https%3A%2F%2Fdeveloper.riotgames.com%2Foauth2-callback&response_type=code&scope=openid%20email%20summoner")))
            startActivity(Intent(baseContext, webViewActivity::class.java))
        }

        switch1.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b) {
                Preferences.setBool(
                    this,
                    "switch",
                    true
                )
                serviceIntent()
            } else {
                Preferences.setBool(
                    this,
                    "switch",
                    false
                )
            }
        }

        swipe_refresh.setOnRefreshListener {
            summonerJob = scope.launch {
                if (isActive) {
                    storeSummoner()
                    withContext(Dispatchers.Main) {
                        summonerAdapter.updateSummoner(summonerInfo)
                        recyclerview_main.adapter = summonerAdapter
                    }
                }
                swipe_refresh.isRefreshing = false
            }
        }

        summonerJob = scope.launch {
            if (isActive) {
                storeSummoner()
                withContext(Dispatchers.Main) {
                    recyclerview_main.apply {
                        layoutManager = LinearLayoutManager(baseContext)
                        summonerAdapter.updateSummoner(summonerInfo)
                        adapter = summonerAdapter
                    }
                }
            }
        }


        if (isswitch) {
            serviceIntent()
        }
    }

    private fun serviceIntent() {
        if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java);
            startService(foregroundServiceIntent)
        } else { //실행중이면 Intent저장
            foregroundServiceIntent = UndeadService.serviceIntent
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        summonerJob?.let {
            it.cancel()
        }
        storeUserJob?.let {
            it.cancel()
        }

    }


    private suspend fun storeSummoner() {
        summonerInfo.clear()
        allname = Preferences.getAll(this)!!
        for ((key, value) in allname.entries) {
            api_key?.let { api_key ->
                val curname = key
                val curid = value.toString()
                val curInfo = SummonerInfo()
                val response = myAPI.getsummoner(curname, api_key)
                if (response.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                    curInfo.name = response.body()!!.name
                    curInfo.profileIconId = response.body()!!.profileIconId
                    curInfo.summonerLevel = response.body()!!.summonerLevel
                }

                val response2 = myAPI.getLeague(curid, api_key)
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

    private suspend fun storeid(): Pair<String?, String?> {
        val str = editText.text.toString()
        var cryptedid: String? = null
        if (str != "") {
            api_key?.let { api_key ->
                val response = myAPI.getsummoner(str, api_key)
                if (response.isSuccessful) {
                    cryptedid = response.body()?.id
                }
            }
        }
        return Pair<String?, String?>(str, cryptedid)
    }

    private fun makeToast(key: String, value: String) {
        if (Preferences.getString(
                this,
                key
            ) != "NoID"
        ) {
            makeToastComment("이미 등록된 아이디 입니다.")

        } else {
            Preferences.setString(
                this,
                key,
                value
            )
            makeToastComment("등록 완료")
        }
    }

    private fun makeToastComment(str: String) {
        if (mToast != null) {
            mToast!!.cancel()
            mToast = Toast.makeText(this, str, Toast.LENGTH_SHORT)
        } else mToast =
            Toast.makeText(this, str, Toast.LENGTH_SHORT)
        mToast?.show()
    }

}

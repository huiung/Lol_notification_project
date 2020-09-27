package com.example.lol_notification_project.view

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lol_notification_project.adapter.SummonerAdapter
import com.example.lol_notification_project.model.SummonerAPI
import com.example.lol_notification_project.model.RetrofitClient
import com.example.lol_notification_project.model.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.service.UndeadService
import com.example.lol_notification_project.model.data.SummonerInfo
import com.example.lol_notification_project.databinding.ActivityMainBinding
import com.example.lol_notification_project.util.makeToast
import com.example.lol_notification_project.util.makeToastComment
import com.example.lol_notification_project.viewmodel.CardViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch.view.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener {

    var foregroundServiceIntent: Intent? = null

    lateinit var alert : AlertDialog.Builder
    lateinit var retrofit: Retrofit
    lateinit var myAPI: SummonerAPI
    lateinit var drawerSwitch: SwitchCompat
    lateinit var viewModel: CardViewModel

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{ // 메뉴 버튼
                main_drawer_layout.openDrawer(GravityCompat.START)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_remove-> {
                addremove()
            }
            R.id.change_key-> {
                changeapi()
            }
            R.id.issue_key-> {
                startActivity(Intent(baseContext, webViewActivity::class.java))
            }
        }
        return false
    }


    override fun onBackPressed() { //뒤로가기 처리
        if(main_drawer_layout.isDrawerOpen(GravityCompat.START)){
            main_drawer_layout.closeDrawers()
        } else{
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProviders.of(this ).get(CardViewModel::class.java)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        initNavigationDrawer()
        initProgressBar()


        alert = AlertDialog.Builder(this)

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
            drawerSwitch.toggle()
        }

        drawerSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            setSwitch(b)
        }

        swipe_refresh.setOnRefreshListener {
            summonerJob = scope.launch {
                if (isActive) {
                    storeSummoner()
                    withContext(Dispatchers.Main) {
                        summonerAdapter.updateSummoner(summonerInfo)
                        binding.recyclerviewMain.adapter = summonerAdapter // binding
                    }
                }
                swipe_refresh.isRefreshing = false
            }
        }

        binding.recyclerviewMain.apply {
            layoutManager = LinearLayoutManager(baseContext)
            adapter = summonerAdapter
        }

        summonerJob = scope.launch {
            if (isActive) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.VISIBLE
                }
                storeSummoner()
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
                withContext(Dispatchers.Main) {
                    binding.recyclerviewMain.apply {
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

    private fun initProgressBar() {
        progressBar.isIndeterminate = true
        progressBar.indeterminateDrawable.setColorFilter(
            Color.rgb(60, 179, 113),
            PorterDuff.Mode.MULTIPLY
        ) // Progress Bar 색깔
    }

    private fun initNavigationDrawer() {
        setSupportActionBar(main_layout_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_36) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        main_navigationView.setNavigationItemSelectedListener(this) // Listener
        drawerSwitch =
            main_navigationView.menu.findItem(R.id.nav_switch).actionView.drawer_switch //switch
    }

    private fun setSwitch(b: Boolean) {
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
            stopService(Intent(this, UndeadService::class.java))
        }
    }

    private fun changeapi() {
        alert.setTitle("API 키 변경").setMessage("변경할 키를 입력해 주세요.")
        val idText = EditText(this)
        alert.setView(idText)

        alert.setPositiveButton("변경") { p0, p1 ->
            api_key = idText.text.toString()
            Preferences.setAPI(
                baseContext,
                "Api_key",
                api_key!!
            )
            makeToastComment("변경 완료", baseContext)
        }

        alert.setNegativeButton("취소"
        ) { p0, p1 -> }
        alert.create().show()
    }

    private fun serviceIntent() {
        if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //오레오 이상에선 background에서 startService 불가
                startForegroundService(foregroundServiceIntent)
            }
            else {
                startService(foregroundServiceIntent)
            }
        } else { //실행중이면 Intent저장
            foregroundServiceIntent = UndeadService.serviceIntent
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        summonerJob?.cancel()
        storeUserJob?.cancel()
    }


    private suspend fun storeSummoner() {
        summonerInfo.clear()
        allname = Preferences.getAll(this)!!
        for ((key, value) in allname.entries) {
            api_key?.let { api_key ->
                val curname = key
                val curid = value.toString()
                val curInfo =
                    SummonerInfo()
                val response = myAPI.getsummoner(curname, api_key)
                if (response.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                    curInfo.name = response.body()!!.name
                    curInfo.profileIconId = "https://ddragon.leagueoflegends.com/cdn/10.14.1/img/profileicon/${response.body()!!.profileIconId}.png"
                    curInfo.summonerLevel = "LV: " + response.body()!!.summonerLevel
                }

                val response2 = myAPI.getLeague(curid, api_key)
                if (response2.isSuccessful) { //League에서 티어 랭크 승/패 포인트 알 수 있음 언랭이면 모든값 null
                    val Infoiterator = response2.body()!!.iterator()
                    while (Infoiterator.hasNext()) {
                        val curLeague = Infoiterator.next()
                        if (curLeague.queueType == "RANKED_SOLO_5x5") { //솔로랭크만 확인
                            curInfo.leaguePoints = curLeague.leaguePoints.toString() +"점"
                            curInfo.wins_losses = "이번시즌 전적\n" +curLeague.wins.toString() +"승 "+curLeague.losses.toString() +"패"
                            curInfo.tier_rank = curLeague.tier + " " + curLeague.rank
                            summonerInfo.add(curInfo) //각 소환사에 대한 정보 추가
                        } else continue;
                    }
                }

            }
        }
    }

    private suspend fun storeid(str: String): Pair<String?, String?> {

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

    private fun addremove() {
        var id = ""
        alert.setTitle("소환사 등록/삭제").setMessage("소환사 이름을 입력해 주세요.")
        val idText = EditText(this)
        alert.setView(idText)

        alert.setPositiveButton("등록") { p0, p1 ->
            storeUserJob = scope.launch {
                if (isActive) {
                    id = idText.text.toString()
                    val curid = storeid(id)
                    withContext(Dispatchers.Main) {
                        if (curid.second == null) makeToastComment(
                            "존재하지 않는 아이디입니다.",
                            baseContext
                        )
                        else makeToast(curid.first!!, curid.second!!, baseContext)
                    }
                }
            }
        }

        alert.setNegativeButton("삭제") { p0, p1 ->
            id = idText.text.toString()
            if (Preferences.getString(
                    baseContext,
                    id
                ) != "NoID"
            ) {
                Preferences.removeString(
                    baseContext,
                    id
                )
                makeToastComment("삭제 완료", baseContext)
            } else {
                makeToastComment("등록되지 않은 소환사 입니다.", baseContext)
            }
        }
        alert.create().show()
    }
}
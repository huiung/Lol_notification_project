package com.example.lol_notification_project.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lol_notification_project.data.remote.SummonerAPI
import com.example.lol_notification_project.data.remote.RetrofitClient
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.R
import com.example.lol_notification_project.service.UndeadService
import com.example.lol_notification_project.databinding.ActivityMainBinding
import com.example.lol_notification_project.ui.webview.webViewActivity
import com.example.lol_notification_project.util.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch.view.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.coroutines.*
import retrofit2.Retrofit


class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener {

    var foregroundServiceIntent: Intent? = null
    var api_key: String? = " "
    var isswitch = false

    lateinit var alert : AlertDialog.Builder
    lateinit var retrofit: Retrofit
    lateinit var myAPI: SummonerAPI
    lateinit var drawerSwitch: SwitchCompat
    lateinit var mainviewModel: MainViewModel
    lateinit var summonerjob: Job

    companion object {
        var mToast: Toast? = null //static
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val allname = Preferences.getAll(baseContext)!!
        mainviewModel = ViewModelProviders.of(this ).get(MainViewModel::class.java)
        binding.vm = mainviewModel
        binding.lifecycleOwner = this

        initNavigationDrawer()
        setvariable()

        swipe_refresh.setOnRefreshListener {
            api_key = Preferences.getAPI(this, "Api_key")
            mainviewModel.refresh(allname, api_key)
        }

        binding.recyclerviewMain.apply {
            layoutManager = LinearLayoutManager(baseContext)
        }

        mainviewModel.refresh(allname, api_key)
        if (isswitch) {
            drawerSwitch.toggle()
            serviceIntent()
        }
    }

    private fun setvariable() {
        alert = AlertDialog.Builder(this)
        isswitch = Preferences.getBool(this, "switch")
        api_key = Preferences.getAPI(this, "Api_key")
        retrofit = RetrofitClient.getInstnace()
        myAPI = RetrofitClient.getServer()
    }

    private fun initNavigationDrawer() {
        setSupportActionBar(main_layout_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_36) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        main_navigationView.setNavigationItemSelectedListener(this) // Listener
        drawerSwitch = main_navigationView.menu.findItem(R.id.nav_switch).actionView.drawer_switch //switch
        drawerSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            setSwitch(b)
        }
    }

    private fun setSwitch(b: Boolean) {
        if (b) {
            Preferences.setBool(this, "switch", true)
            serviceIntent()
        } else {
            Preferences.setBool(this, "switch", false)
            stopService(Intent(this, UndeadService::class.java))
        }
    }

    private fun serviceIntent() {
        if (UndeadService.serviceIntent == null) { //서비스가 실행중이지 않으면 실행
            foregroundServiceIntent = Intent(this, UndeadService::class.java)

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
                changeapi(baseContext, alert)
            }
            R.id.issue_key-> {
                startActivity(Intent(baseContext, webViewActivity::class.java))
            }
        }
        return false
    }

    fun addremove() { // 소환사 등록/삭제관련 alertdialog
        val idText = EditText(this)
        alert.setTitle("소환사 등록/삭제").setMessage("소환사 이름을 입력해 주세요.")
            .setView(idText)
        var curid: Pair<String?, String?>

        alert.setPositiveButton("등록") { p0, p1 ->
            summonerjob = CoroutineScope(Dispatchers.Main).launch {
                val id = idText.text.toString()
                if (isActive) {
                    withContext(Dispatchers.Default) {
                        api_key = Preferences.getAPI(baseContext, "Api_key")
                        curid = storeid(api_key, id, myAPI)
                    }
                    if (curid.second == null) makeToastComment("존재하지 않는 아이디입니다.", baseContext)
                    else makeToast(curid.first!!, curid.second!!, baseContext)
                }
            }
        }
            .setNegativeButton("삭제") { p0, p1 ->
                val id = idText.text.toString()
                if (Preferences.getString(baseContext, id) != "NoID") {
                    Preferences.removeString(baseContext, id)
                    makeToastComment("삭제 완료", baseContext)
                } else {
                    makeToastComment("등록되지 않은 소환사 입니다.", baseContext)
                }
            }
            .create().show()
    }

    override fun onBackPressed() { //뒤로가기 처리
        if(main_drawer_layout.isDrawerOpen(GravityCompat.START)){
            main_drawer_layout.closeDrawers()
        } else{
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        summonerjob.cancel()
    }
}
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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lol_notification_project.R
import com.example.lol_notification_project.service.UndeadService
import com.example.lol_notification_project.databinding.ActivityMainBinding
import com.example.lol_notification_project.ui.webview.webViewActivity
import com.example.lol_notification_project.util.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_switch.view.*
import kotlinx.android.synthetic.main.main_toolbar.*
import org.koin.android.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener {

    var foregroundServiceIntent: Intent? = null

    lateinit var alert : AlertDialog.Builder
    lateinit var drawerSwitch: SwitchCompat

    val mainviewModel: MainViewModel by viewModel()

    companion object {
        var mToast: Toast? = null //static
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = mainviewModel
        binding.lifecycleOwner = this

        alert = AlertDialog.Builder(this)
        initNavigationDrawer()
        observeViewModel()

        swipe_refresh.setOnRefreshListener {
            mainviewModel.refresh()
        }

        binding.recyclerviewMain.apply {
            layoutManager = LinearLayoutManager(baseContext)
        }

    mainviewModel.refresh()

}

private fun initNavigationDrawer() {
    setSupportActionBar(main_layout_toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_36) // 홈버튼 이미지 변경
    supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
    main_navigationView.setNavigationItemSelectedListener(this) // Listener
    drawerSwitch = main_navigationView.menu.findItem(R.id.nav_switch).actionView.drawer_switch //switch
    mainviewModel.isswitch.value?.run { if(this)drawerSwitch.toggle() }
    drawerSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
        mainviewModel.toggle()
    }
}

    private fun observeViewModel() {
        mainviewModel.isswitch.observe(this, Observer{isswitch ->
            if(isswitch) {
                serviceIntent()
            }
            else {
                stopService(Intent(this, UndeadService::class.java))
            }
        })

        mainviewModel.check_id.observe(this, Observer {check_id ->
            if(check_id) {
                makeToastComment("삭제 완료", baseContext)
            } else {
                makeToastComment("등록되지 않은 소환사 입니다.", baseContext)
            }
        })

        mainviewModel.add_id.observe(this, Observer { add_id ->
            if(add_id.second == null) {
                makeToastComment("존재하지 않는 아이디입니다.", baseContext)
            }
            else {
                makeToast(add_id.first!!, add_id.second!!, baseContext)
            }
        })
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
                changeapi(alert)
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

        alert.setPositiveButton("등록") { p0, p1 ->
                mainviewModel.addsummoner(idText.text.toString())
            }
            .setNegativeButton("삭제") { p0, p1 ->
                mainviewModel.check_id(idText.text.toString())
            }
            .create().show()
    }

    fun changeapi(alert:AlertDialog.Builder) { //api 변경 관련 alertdialog
        val idText = EditText(baseContext)
        alert.setTitle("API 키 변경").setMessage("변경할 키를 입력해 주세요.")
            .setView(idText)

        alert.setPositiveButton("변경") { p0, p1 ->
            mainviewModel.changeApi(idText.text.toString())
            makeToastComment("변경 완료", baseContext)
            }
            .setNegativeButton("취소") { p0, p1 -> }
            .create().show()
    }

    override fun onBackPressed() { //뒤로가기 처리
        if(main_drawer_layout.isDrawerOpen(GravityCompat.START)){
            main_drawer_layout.closeDrawers()
        } else{
            super.onBackPressed()
        }
    }

}
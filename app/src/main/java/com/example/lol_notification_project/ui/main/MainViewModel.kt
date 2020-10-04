package com.example.lol_notification_project.ui.main

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.data.remote.RetrofitClient
import com.example.lol_notification_project.data.model.SummonerInfo
import com.example.lol_notification_project.data.remote.SummonerAPI
import com.example.lol_notification_project.util.makeToast
import com.example.lol_notification_project.util.makeToastComment
import com.example.lol_notification_project.util.storeid
import kotlinx.coroutines.*

class MainViewModel() : ViewModel() {

    private val _summonerInfo = MutableLiveData<List<SummonerInfo>>()
    val summonerInfo: LiveData<List<SummonerInfo>> get() = _summonerInfo

    private val _swipe_refresh = MutableLiveData<Boolean>()
    val swipe_refresh : LiveData<Boolean> get() = _swipe_refresh

    fun refresh(allname: MutableMap<String, *>, api_key: String?) {
        fetchSummoners(allname, api_key)

    }

    private fun fetchSummoners(allname: MutableMap<String, *>, api_key: String?) {
        var curval : List<SummonerInfo>

        viewModelScope.launch {
            _swipe_refresh.value = true
            withContext(Dispatchers.Default) {
                curval = storeSummoner(allname, api_key)
            }
            _summonerInfo.value = curval
            _swipe_refresh.value =  false
        }
    }

    private suspend fun storeSummoner(allname: MutableMap<String, *>, api_key: String?) : List<SummonerInfo> {

        val retrofit = RetrofitClient.getInstnace()
        val myAPI = RetrofitClient.getServer()
        val summoner: MutableList<SummonerInfo> = arrayListOf()

        Log.d("mytag", api_key.toString())

        for ((key, value) in allname.entries) {
            api_key?.let { api_key ->
                val curname = key
                val curid = value.toString()
                val curInfo = SummonerInfo()
                val response_summoner = myAPI.getsummoner(curname, api_key)
                if (response_summoner.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                    curInfo.name = response_summoner.body()!!.name
                    curInfo.profileIconId = "https://ddragon.leagueoflegends.com/cdn/10.14.1/img/profileicon/${response_summoner.body()!!.profileIconId}.png"
                    curInfo.summonerLevel = "LV: " + response_summoner.body()!!.summonerLevel
                }

                val response_league = myAPI.getLeague(curid, api_key)
                if (response_league.isSuccessful) { //League에서 티어 랭크 승/패 포인트 알 수 있음 언랭이면 모든값 null
                    val Infoiterator = response_league.body()!!.iterator()
                    while (Infoiterator.hasNext()) {
                        val curLeague = Infoiterator.next()
                        if (curLeague.queueType == "RANKED_SOLO_5x5") { //솔로랭크만 확인
                            curInfo.leaguePoints = curLeague.leaguePoints.toString() +"점"
                            curInfo.wins_losses = "이번시즌 전적\n" +curLeague.wins.toString() +"승 "+curLeague.losses.toString() +"패"
                            curInfo.tier_rank = curLeague.tier + " " + curLeague.rank
                            summoner.add(curInfo) //각 소환사에 대한 정보 추가
                        } else continue;
                    }
                }
            }
        }
        return summoner
    }

    override fun onCleared() {
        super.onCleared()
    }
}
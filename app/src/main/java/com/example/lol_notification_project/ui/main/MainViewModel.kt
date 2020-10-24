package com.example.lol_notification_project.ui.main


import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.data.model.SummonerInfo
import com.example.lol_notification_project.data.remote.SummonerAPI
import com.example.lol_notification_project.util.storeid
import kotlinx.coroutines.*

class MainViewModel(private val myAPI: SummonerAPI, val context: Context) : ViewModel() {

    private val _summonerInfo = MutableLiveData<List<SummonerInfo>>() //RecyclerView Data
    val summonerInfo: LiveData<List<SummonerInfo>> get() = _summonerInfo

    private val _swipe_refresh = MutableLiveData<Boolean>()
    val swipe_refresh : LiveData<Boolean> get() = _swipe_refresh

    private val _isswitch = MutableLiveData<Boolean>()
    val isswitch : LiveData<Boolean> get() = _isswitch

    private val _api_key = MutableLiveData<String>()
    val api_key : LiveData<String> get() = _api_key

    private val _allname = MutableLiveData<MutableMap<String, *> >()
    val allname : LiveData<MutableMap<String, *> > get() = _allname

    private val _check_id = MutableLiveData<Boolean> ()
    val check_id : LiveData<Boolean> get() = _check_id

    private val _add_id = MutableLiveData<Pair<String?, String?>> ()
    val add_id : LiveData<Pair<String?, String?>> get() = _add_id

    init {
        _isswitch.value = Preferences.getBool(context, "switch")
        _api_key.value = Preferences.getAPI(context, "Api_key")
        Preferences.getAll(context)?.run { _allname.value = this }
    }


    fun check_id(id: String) {
        if (Preferences.getString(context, id) != "NoID") {
            Preferences.removeString(context, id)
            _check_id.value = true
        } else {
            _check_id.value = false
        }
    }

    fun addsummoner(id: String) {
        var curid : Pair<String?, String?>
        viewModelScope.launch {
            if (isActive) {
                withContext(Dispatchers.Default) {
                    curid = storeid(api_key.value, id, myAPI)
                }

                if(curid.second != null)
                _add_id.value = curid

            }
        }
    }

    fun changeApi(key: String) {
        _api_key.value = key
        Preferences.setAPI(context, "Api_key", key)
    }

    fun toggle() {
        _isswitch.value = !Preferences.getBool(context, "switch")
        Preferences.setBool(context, "switch", _isswitch.value!!)
    }

    fun refresh() {
        fetchSummoners()
    }

    private fun fetchSummoners() {
        var curval : List<SummonerInfo>
        Preferences.getAll(context)?.run { _allname.value = this }
        viewModelScope.launch {
            if(isActive) {
                _swipe_refresh.value = true
                withContext(Dispatchers.Default) {
                    curval = storeSummoner()
                }
                _summonerInfo.value = curval
                _swipe_refresh.value = false
            }
        }
    }



    private suspend fun storeSummoner() : List<SummonerInfo> {

        val summoner: MutableList<SummonerInfo> = arrayListOf()

        for ((key, value) in allname.value?.entries!!) {
            api_key.value?.run {
                val curname = key
                val curid = value.toString()
                val curInfo = SummonerInfo()
                val response_summoner = myAPI.getsummoner(curname, this)
                if (response_summoner.isSuccessful) { // Summoner에서 레벨, Icon ID 획득 가능
                    curInfo.name = response_summoner.body()?.name
                    curInfo.profileIconId = "https://ddragon.leagueoflegends.com/cdn/10.14.1/img/profileicon/${response_summoner.body()?.profileIconId}.png"
                    curInfo.summonerLevel = "LV: " + response_summoner.body()?.summonerLevel
                }

                val response_league = myAPI.getLeague(curid, this)
                if (response_league.isSuccessful) { //League에서 티어 랭크 승/패 포인트 알 수 있음 언랭이면 모든값 null
                    val Infoiterator = response_league.body()?.iterator() ?: iterator {  }
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

}
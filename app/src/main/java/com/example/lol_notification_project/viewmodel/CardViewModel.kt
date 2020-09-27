package com.example.lol_notification_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lol_notification_project.model.data.SummonerInfo

class CardViewModel : ViewModel() {

    private val _summonerInfo = MutableLiveData<List<SummonerInfo>>()
    val summonerInfo: LiveData<List<SummonerInfo>> get() = _summonerInfo

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading


    fun refresh() {
        fetchSummoners()
    }

    private fun fetchSummoners() {



    }


    override fun onCleared() {
        super.onCleared()
    }
}
package com.example.lol_notification_project.data.repository

import com.example.lol_notification_project.data.local.Preferences
import com.example.lol_notification_project.data.model.LeagueEntryDTO
import com.example.lol_notification_project.data.model.Spectator
import com.example.lol_notification_project.data.model.Summoner
import com.example.lol_notification_project.data.remote.SummonerAPI
import retrofit2.Response

class MainRepository(private val myAPI: SummonerAPI, private val Preferences: Preferences) {

    suspend fun getspectator(encryptedSummonerId : String?, api_key : String) : Response<Spectator> {
        return myAPI.getspectator(encryptedSummonerId, api_key)
    }

    suspend fun getsummoner(summonerName : String, api_key : String): Response<Summoner> {
        return myAPI.getsummoner(summonerName, api_key)
    }

    suspend fun getLeague(encryptedSummonerId : String?,api_key: String): Response<Set<LeagueEntryDTO>> {
        return myAPI.getLeague(encryptedSummonerId, api_key)
    }

    fun setAPI(key: String, value: String) {
        Preferences.setAPI(key, value)
    }

    fun getAPI(key: String) : String? {
        return Preferences.getAPI(key)
    }

    fun setBool(key: String, value: Boolean) {
        Preferences.setBool(key, value)
    }

    fun getBool(key: String) : Boolean {
        return Preferences.getBool(key)
    }

    fun setString(key: String, value: String) {
        Preferences.setString(key, value)
    }

    fun getString(key: String) : String? {
        return Preferences.getString(key)
    }

    fun getAll() : MutableMap<String, *>? {
        return Preferences.getAll()
    }

    fun removeString(key: String) {
        Preferences.removeString(key)
    }

    fun setLong(key: String, value: Long) {
        Preferences.setLong(key, value)
    }

    fun getLong(key: String) : Long {
        return Preferences.getLong(key)
    }

}
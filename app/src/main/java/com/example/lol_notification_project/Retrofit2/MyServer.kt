package com.example.lol_notification_project.Retrofit2

import com.example.lol_notification_project.JsonType.LeagueEntryDTO
import com.example.lol_notification_project.JsonType.Spectator
import com.example.lol_notification_project.JsonType.Summoner
import retrofit2.Call
import retrofit2.http.*

interface MyServer {

    @GET("summoner/v4/summoners/by-name/{summonerName}")
    fun getsummoner(
        @Path("summonerName") summonerName : String,
        @Query("api_key") api_key : String
    ): Call<Summoner>

    @GET("spectator/v4/active-games/by-summoner/{encryptedSummonerId}")
    fun getspectator(
        @Path("encryptedSummonerId") encryptedSummonerId : String?,
        @Query("api_key") api_key : String
    ): Call<Spectator>

    @GET("league/v4/entries/by-summoner/{encryptedSummonerId}")
    fun getLeague(
        @Path("encryptedSummonerId") encryptedSummonerId : String?,
        @Query("api_key") api_key: String
    ): Call<Set<LeagueEntryDTO>>

}
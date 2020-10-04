package com.example.lol_notification_project.data.remote

import com.example.lol_notification_project.data.model.LeagueEntryDTO
import com.example.lol_notification_project.data.model.Spectator
import com.example.lol_notification_project.data.model.Summoner
import retrofit2.Response
import retrofit2.http.*

interface SummonerAPI {

    @GET("summoner/v4/summoners/by-name/{summonerName}")
    suspend fun getsummoner(
        @Path("summonerName") summonerName : String,
        @Query("api_key") api_key : String
    ): Response<Summoner>

    @GET("spectator/v4/active-games/by-summoner/{encryptedSummonerId}")
    suspend fun getspectator(
        @Path("encryptedSummonerId") encryptedSummonerId : String?,
        @Query("api_key") api_key : String
    ): Response<Spectator>

    @GET("league/v4/entries/by-summoner/{encryptedSummonerId}")
    suspend fun getLeague(
        @Path("encryptedSummonerId") encryptedSummonerId : String?,
        @Query("api_key") api_key: String
    ): Response<Set<LeagueEntryDTO>>

}
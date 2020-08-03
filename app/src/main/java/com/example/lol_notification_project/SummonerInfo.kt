package com.example.lol_notification_project

class SummonerInfo() {
    var profileIconId: String? = null
    var name: String? = null
    var summonerLevel: String? = null
    var tier: String? = null
    var rank: String? = null
    var leaguePoints: Int? = null
    var wins: Int? = null
    var losses: Int? = null


    override fun toString(): String {
        return "tier={$tier}, rank={$rank}, wins={$wins}, losses={$losses}\n"
    }

}
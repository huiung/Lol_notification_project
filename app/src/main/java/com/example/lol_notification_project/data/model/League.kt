package com.example.lol_notification_project.data.model

class LeagueEntryDTO {
    var leagueId: String? = null
    var summonerId: String? = null
    var summonerName: String? = null
    var queueType: String? = null
    var tier: String? = null
    var rank: String? = null
    var leaguePoints: Int? = null
    var wins: Int? = null
    var losses: Int? = null
    var hotStreak: Boolean? = null
    var veteran: Boolean? = null
    var freshBlood: Boolean? = null
    var inactive: Boolean?= null
    var miniSeries : MiniSeriesDTO? = null


    override fun toString(): String {
        return "[wins = $wins, freshBlood = $freshBlood, summonerName = $summonerName, leaguePoints = $leaguePoints, losses = $losses, inactive = $inactive, tier = $tier, veteran = $veteran, leagueId = $leagueId, hotStreak = $hotStreak, queueType = $queueType, rank = $rank, summonerId = $summonerId]"
    }
}

class MiniSeriesDTO {

    var losses: Int? = null
    var progress: String? = null
    var target: Int? = null
    var wins: Int? = null

}
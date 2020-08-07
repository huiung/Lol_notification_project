package com.example.lol_notification_project.Model.Data

class Summoner {
    var accountId: String? = null
    var profileIconId: String? = null
    var revisionDate: String? = null
    var name: String? = null
    var puuid: String? = null
    var id: String? = null
    var summonerLevel: String? = null

    override fun toString(): String {
        return "[accountId = $accountId, profileIconId = $profileIconId, revisionDate = $revisionDate, name = $name, puuid = $puuid, id = $id, summonerLevel = $summonerLevel]"
    }
}
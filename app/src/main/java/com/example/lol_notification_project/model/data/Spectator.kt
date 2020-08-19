package com.example.lol_notification_project.model.data

class Spectator {
    var gameId: Long? = null
    var gameType: String? = null
    var gameStartTime: Long? = null
    var mapId: Long? = null
    var gameLength: String? = null
    var platformId: String? = null
    var gameMode: String? = null
    lateinit var bannedChampions: List<BannedChampion>
    var gameQueueConfigId: Long? = null
    var observers: Observers? = null
    lateinit var participants: List<CurrentGameParticipant>

    override fun toString(): String {
        return "[gameId = $gameId, gameType = $gameType, gameStartTime = $gameStartTime, mapId = $mapId, platformId = $platformId, gameLength = $gameLength, gameMode = $gameMode, gameQueueConfigId = $gameQueueConfigId, bannedChampions = $bannedChampions, observers = $observers, participants = $participants]"
    }
}

class BannedChampion {

    var pickTurn: Int? = null
    var championId: Long? = null
    var teamId: Long? = null


    override fun toString(): String {
        return "[pickTurn = $pickTurn, championId = $championId, teamId = $teamId]"
    }
}

class Observers {
    var encryptionKey: String? = null

    override fun toString(): String {
        return "[encryptionKey = $encryptionKey]"
    }
}

class CurrentGameParticipant {
    var championId: Long? = null
    private var perks: Perks? = null
    var profileIconId: Long? = null
    var bot: Boolean? = null
    var teamId: Long? = null
    var summonerName: String? = null
    var summonerId: String? = null
    var spell1Id: Long? = null
    var spell2Id: Long? = null
    lateinit var gameCustomizationObjects: List<GameCustomizationObject>

    override fun toString(): String {
        return "[championId = $championId, profileIconId = $profileIconId, bot = $bot, teamId = $teamId, summonerName = $summonerName, spell1Id = $spell1Id, summonerId = $summonerId, spell2Id = $spell2Id, perks = $perks, gameCustomizationObjects = $gameCustomizationObjects]"
    }
}

class Perks {
    lateinit var perkIds: List<Long>
    var perkSubStyle: Long? = null
    var perkStyle: Long? = null

    override fun toString(): String {
        return "[perkSubStyle = $perkSubStyle, perkIds = $perkIds, perkStyle = $perkStyle]"
    }
}

class GameCustomizationObject {
    var category: String? = null
    var content: String? = null
}
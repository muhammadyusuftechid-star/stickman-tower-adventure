package com.stickman.tower.rpg

/**
 * Data class representing persistent player progression.
 */
data class SaveData(
    var playerLevel: Int = 1,
    var currentExp: Int = 0,
    var gold: Int = 0,
    var highestTowerFloor: Int = 1,
    var unlockedCharacters: ArrayList<String> = arrayListOf("STICKMAN_SWORD"),
    var selectedArchetype: String = "BALANCED"
)

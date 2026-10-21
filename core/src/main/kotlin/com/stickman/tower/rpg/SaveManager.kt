package com.stickman.tower.rpg

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

/**
 * Handles persistent serialization of player save data using local files and Preferences fallback.
 */
object SaveManager {
    private const val SAVE_FILE_NAME = "stickman_save.json"
    private const val PREF_NAME = "StickmanSavePrefs"
    private val json = Json(JsonWriter.OutputType.json)

    fun save(data: SaveData) {
        try {
            val jsonString = json.toJson(data)
            val file = Gdx.files.local(SAVE_FILE_NAME)
            file.writeString(jsonString, false)
            Gdx.app.log("SaveManager", "Saved progress to local file: Lv.${data.playerLevel}, EXP=${data.currentExp}, Gold=${data.gold}")
        } catch (e: Exception) {
            Gdx.app.error("SaveManager", "Failed to save to local file, falling back to Preferences: ${e.message}")
            try {
                val prefs = Gdx.app.getPreferences(PREF_NAME)
                prefs.putInteger("playerLevel", data.playerLevel)
                prefs.putInteger("currentExp", data.currentExp)
                prefs.putInteger("gold", data.gold)
                prefs.putInteger("highestTowerFloor", data.highestTowerFloor)
                prefs.flush()
            } catch (prefEx: Exception) {
                Gdx.app.error("SaveManager", "Failed to save to Preferences: ${prefEx.message}")
            }
        }
    }

    fun load(): SaveData {
        try {
            val file = Gdx.files.local(SAVE_FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readString()
                val data = json.fromJson(SaveData::class.java, jsonString)
                if (data != null) {
                    Gdx.app.log("SaveManager", "Loaded save from local file: Lv.${data.playerLevel}, EXP=${data.currentExp}, Gold=${data.gold}")
                    return data
                }
            }
        } catch (e: Exception) {
            Gdx.app.error("SaveManager", "Failed to read save file, attempting Preferences: ${e.message}")
        }

        // Try Preferences fallback
        try {
            val prefs = Gdx.app.getPreferences(PREF_NAME)
            if (prefs.contains("playerLevel")) {
                val data = SaveData(
                    playerLevel = prefs.getInteger("playerLevel", 1),
                    currentExp = prefs.getInteger("currentExp", 0),
                    gold = prefs.getInteger("gold", 0),
                    highestTowerFloor = prefs.getInteger("highestTowerFloor", 1)
                )
                Gdx.app.log("SaveManager", "Loaded save from Preferences: Lv.${data.playerLevel}")
                return data
            }
        } catch (e: Exception) {
            Gdx.app.error("SaveManager", "Failed to read Preferences: ${e.message}")
        }

        Gdx.app.log("SaveManager", "No existing save found. Initializing new default save.")
        return SaveData()
    }
}

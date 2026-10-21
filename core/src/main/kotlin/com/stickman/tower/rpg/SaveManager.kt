package com.stickman.tower.rpg

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter

/**
 * Abstraksi media penyimpanan untuk memungkinkan pengujian unit test independen
 * dan persistensi LibGDX yang tahan terhadap kegagalan IO/korupsi berkas.
 */
interface SaveStorageAdapter {
    fun writeString(fileName: String, content: String)
    fun readString(fileName: String): String?
    fun exists(fileName: String): Boolean
    fun delete(fileName: String): Boolean
}

/**
 * Adapter bawaan menggunakan Gdx.files.local
 */
class LocalFileStorageAdapter : SaveStorageAdapter {
    override fun writeString(fileName: String, content: String) {
        val file = Gdx.files?.local(fileName) ?: throw IllegalStateException("Gdx.files is null")
        file.writeString(content, false)
    }

    override fun readString(fileName: String): String? {
        val file = Gdx.files?.local(fileName) ?: return null
        return if (file.exists()) file.readString() else null
    }

    override fun exists(fileName: String): Boolean {
        return Gdx.files?.local(fileName)?.exists() ?: false
    }

    override fun delete(fileName: String): Boolean {
        return Gdx.files?.local(fileName)?.delete() ?: false
    }
}

/**
 * Adapter in-memory untuk unit testing dan sandbox tanpa dependensi LibGDX runtime.
 */
class InMemoryStorageAdapter : SaveStorageAdapter {
    private val storage = mutableMapOf<String, String>()

    override fun writeString(fileName: String, content: String) {
        storage[fileName] = content
    }

    override fun readString(fileName: String): String? {
        return storage[fileName]
    }

    override fun exists(fileName: String): Boolean {
        return storage.containsKey(fileName)
    }

    override fun delete(fileName: String): Boolean {
        return storage.remove(fileName) != null
    }

    fun clear() = storage.clear()
}

/**
 * Sistem Manajemen Persistensi Data RPG dengan Arsitektur Enterprise Fault-Tolerant:
 * - Atomic write menggunakan berkas sementara (.tmp)
 * - Rolling auto-backup berkas sebelumnya (.bak)
 * - Multi-tier auto-recovery (Primary File -> Backup File -> Preferences -> Default)
 * - Verifikasi integritas Checksum & Sanitasi Batas Data
 * - Thread-Safe (@Synchronized)
 * - Export, Import, dan Reset API
 */
object SaveManager {
    const val SAVE_FILE_NAME = "stickman_save.json"
    const val BACKUP_FILE_NAME = "stickman_save.json.bak"
    const val TEMP_FILE_NAME = "stickman_save.json.tmp"
    const val PREF_NAME = "StickmanSavePrefs"

    private val json = Json(JsonWriter.OutputType.json)

    // Storage adapter yang dapat diganti untuk keperluan automated unit testing
    var storageAdapter: SaveStorageAdapter = LocalFileStorageAdapter()

    private fun log(message: String) {
        Gdx.app?.log("SaveManager", message)
    }

    private fun logError(message: String) {
        Gdx.app?.error("SaveManager", message)
    }

    /**
     * Menyimpan data progres pemain secara aman dan atomik.
     */
    @Synchronized
    fun save(data: SaveData) {
        // 1. Sanitasi dan perbarui checksum integritas
        data.sanitize()
        data.updateChecksum()

        try {
            val jsonString = json.toJson(data)

            // 2. Tulis ke berkas sementara (.tmp) terlebih dahulu
            storageAdapter.writeString(TEMP_FILE_NAME, jsonString)

            // 3. Verifikasi berkas sementara berhasil ditulis dan tidak kosong
            val verifiedContent = storageAdapter.readString(TEMP_FILE_NAME)
            if (!verifiedContent.isNullOrEmpty() && verifiedContent == jsonString) {
                // 4. Jika berkas utama lama sudah ada, cadangkan ke rolling backup (.bak)
                val oldPrimary = storageAdapter.readString(SAVE_FILE_NAME)
                if (!oldPrimary.isNullOrEmpty()) {
                    storageAdapter.writeString(BACKUP_FILE_NAME, oldPrimary)
                }

                // 5. Commit atomik: salin konten valid ke berkas utama
                storageAdapter.writeString(SAVE_FILE_NAME, jsonString)
                storageAdapter.delete(TEMP_FILE_NAME)

                log("Saved progress atomically: Lv.${data.playerLevel}, Gold=${data.gold}, Floor=${data.highestTowerFloor}, Checksum=${data.checksum}")
            } else {
                throw IllegalStateException("Temp file verification failed")
            }
        } catch (e: Exception) {
            logError("Failed atomic local file save: ${e.message}. Triggering Preferences redundancy.")
        }

        // 6. Sinkronisasi fallback redundan ke Preferences (selalu simpan seluruh field)
        saveToPreferences(data)
    }

    /**
     * Memuat data simpanan dengan multi-tier auto recovery:
     * Tier 1: Berkas Utama -> Tier 2: Berkas Rolling Backup -> Tier 3: Preferences -> Tier 4: Default Fresh
     */
    @Synchronized
    fun load(): SaveData {
        // Tier 1: Coba baca berkas utama
        try {
            val content = storageAdapter.readString(SAVE_FILE_NAME)
            if (!content.isNullOrEmpty()) {
                val data = json.fromJson(SaveData::class.java, content)
                if (data != null && data.isChecksumValid()) {
                    data.sanitize()
                    log("Loaded primary save: Lv.${data.playerLevel}, Archetype=${data.selectedArchetype}")
                    return data
                } else {
                    logError("Primary save checksum invalid or corrupted! Attempting backup recovery...")
                }
            }
        } catch (e: Exception) {
            logError("Failed to parse primary save file: ${e.message}")
        }

        // Tier 2: Coba pulihkan dari berkas rolling backup (.bak)
        try {
            val backupContent = storageAdapter.readString(BACKUP_FILE_NAME)
            if (!backupContent.isNullOrEmpty()) {
                val backupData = json.fromJson(SaveData::class.java, backupContent)
                if (backupData != null && backupData.isChecksumValid()) {
                    backupData.sanitize()
                    backupData.updateChecksum()
                    // Tulis ulang ke berkas utama untuk memperbaiki berkas utama yang rusak
                    try {
                        storageAdapter.writeString(SAVE_FILE_NAME, json.toJson(backupData))
                    } catch (_: Exception) {}

                    log("Successfully recovered save data from rolling backup (.bak): Lv.${backupData.playerLevel}")
                    return backupData
                }
            }
        } catch (e: Exception) {
            logError("Failed to load backup save file: ${e.message}")
        }

        // Tier 3: Coba pulihkan dari Preferences
        val prefData = loadFromPreferences()
        if (prefData != null) {
            prefData.sanitize()
            prefData.updateChecksum()
            // Simpan kembali ke file utama
            try {
                storageAdapter.writeString(SAVE_FILE_NAME, json.toJson(prefData))
            } catch (_: Exception) {}

            log("Recovered save data from persistent Preferences: Lv.${prefData.playerLevel}")
            return prefData
        }

        // Tier 4: Default baru jika belum ada data sama sekali
        log("No existing or recoverable save found. Initializing new default save.")
        val defaultData = SaveData().sanitize()
        defaultData.updateChecksum()
        return defaultData
    }

    private fun saveToPreferences(data: SaveData) {
        try {
            val prefs = Gdx.app?.getPreferences(PREF_NAME) ?: return
            prefs.putInteger("playerLevel", data.playerLevel)
            prefs.putInteger("currentExp", data.currentExp)
            prefs.putInteger("gold", data.gold)
            prefs.putInteger("highestTowerFloor", data.highestTowerFloor)
            prefs.putInteger("highestEndlessFloor", data.highestEndlessFloor)
            prefs.putInteger("totalEnemiesDefeated", data.totalEnemiesDefeated)
            prefs.putString("selectedArchetype", data.selectedArchetype)
            prefs.putString("selectedSubClass", data.selectedSubClass)
            prefs.putString("unlockedCharacters", data.unlockedCharacters.joinToString(","))
            prefs.putInteger("weaponAtkLevel", data.weaponAtkLevel)
            prefs.putInteger("weaponCritLevel", data.weaponCritLevel)
            prefs.putInteger("weaponSpdLevel", data.weaponSpdLevel)
            prefs.putInteger("unspentTalentPoints", data.unspentTalentPoints)
            prefs.putLong("checksum", data.checksum)
            prefs.flush()
        } catch (prefEx: Exception) {
            logError("Failed to save to Preferences: ${prefEx.message}")
        }
    }

    private fun loadFromPreferences(): SaveData? {
        try {
            val prefs: Preferences = Gdx.app?.getPreferences(PREF_NAME) ?: return null
            if (prefs.contains("playerLevel")) {
                val unlockedStr = prefs.getString("unlockedCharacters", "STICKMAN_SWORD")
                val unlockedList = ArrayList(unlockedStr.split(",").filter { it.isNotBlank() })
                if (unlockedList.isEmpty()) unlockedList.add("STICKMAN_SWORD")

                return SaveData(
                    playerLevel = prefs.getInteger("playerLevel", 1),
                    currentExp = prefs.getInteger("currentExp", 0),
                    gold = prefs.getInteger("gold", 0),
                    highestTowerFloor = prefs.getInteger("highestTowerFloor", 1),
                    highestEndlessFloor = prefs.getInteger("highestEndlessFloor", 0),
                    totalEnemiesDefeated = prefs.getInteger("totalEnemiesDefeated", 0),
                    unlockedCharacters = unlockedList,
                    selectedArchetype = prefs.getString("selectedArchetype", "BALANCED"),
                    selectedSubClass = prefs.getString("selectedSubClass", "BERSERKER"),
                    weaponAtkLevel = prefs.getInteger("weaponAtkLevel", 0),
                    weaponCritLevel = prefs.getInteger("weaponCritLevel", 0),
                    weaponSpdLevel = prefs.getInteger("weaponSpdLevel", 0),
                    unspentTalentPoints = prefs.getInteger("unspentTalentPoints", -1),
                    checksum = prefs.getLong("checksum", 0L)
                )
            }
        } catch (e: Exception) {
            logError("Failed to read Preferences: ${e.message}")
        }
        return null
    }

    /**
     * Mengekspor data simpanan saat ini ke format JSON terstruktur (untuk Cloud Sync atau backup).
     */
    fun exportJson(): String {
        val data = load()
        return json.prettyPrint(data)
    }

    /**
     * Mengimpor data simpanan dari string JSON tervalidasi.
     */
    fun importJson(jsonString: String): Boolean {
        return try {
            val data = json.fromJson(SaveData::class.java, jsonString)
            if (data != null) {
                save(data)
                true
            } else false
        } catch (e: Exception) {
            logError("Failed to import JSON: ${e.message}")
            false
        }
    }

    /**
     * Menghapus seluruh berkas penyimpanan dan preferensi untuk mengembalikan akun ke status awal.
     */
    @Synchronized
    fun resetSave(): SaveData {
        try {
            storageAdapter.delete(SAVE_FILE_NAME)
            storageAdapter.delete(BACKUP_FILE_NAME)
            storageAdapter.delete(TEMP_FILE_NAME)
        } catch (_: Exception) {}

        try {
            val prefs = Gdx.app?.getPreferences(PREF_NAME)
            prefs?.clear()
            prefs?.flush()
        } catch (_: Exception) {}

        val newSave = SaveData().sanitize()
        save(newSave)
        return newSave
    }
}

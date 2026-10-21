package com.stickman.tower.rpg

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SaveManagerTest {

    private val testStorage = InMemoryStorageAdapter()

    @BeforeTest
    fun setUp() {
        SaveManager.storageAdapter = testStorage
        testStorage.clear()
    }

    @AfterTest
    fun tearDown() {
        testStorage.clear()
        SaveManager.storageAdapter = LocalFileStorageAdapter()
    }

    @Test
    fun testSaveAndLoadIntegrity() {
        val initialData = SaveData(
            playerLevel = 15,
            currentExp = 350,
            gold = 4500,
            highestTowerFloor = 3,
            highestEndlessFloor = 8,
            totalEnemiesDefeated = 120,
            selectedArchetype = "HEAVY",
            selectedSubClass = "TITAN",
            unlockedCharacters = arrayListOf("STICKMAN_SWORD", "STICKMAN_SPEAR"),
            weaponAtkLevel = 4,
            weaponCritLevel = 2,
            weaponSpdLevel = 3
        )

        SaveManager.save(initialData)

        // Verifikasi berkas utama dan backup terbuat di storage
        assertTrue(testStorage.exists(SaveManager.SAVE_FILE_NAME))

        val loaded = SaveManager.load()
        assertEquals(15, loaded.playerLevel)
        assertEquals(350, loaded.currentExp)
        assertEquals(4500, loaded.gold)
        assertEquals(3, loaded.highestTowerFloor)
        assertEquals(8, loaded.highestEndlessFloor)
        assertEquals(120, loaded.totalEnemiesDefeated)
        assertEquals("HEAVY", loaded.selectedArchetype)
        assertEquals("TITAN", loaded.selectedSubClass)
        assertEquals(2, loaded.unlockedCharacters.size)
        assertTrue(loaded.unlockedCharacters.contains("STICKMAN_SPEAR"))
        assertEquals(4, loaded.weaponAtkLevel)
        assertEquals(2, loaded.weaponCritLevel)
        assertEquals(3, loaded.weaponSpdLevel)
        assertTrue(loaded.checksum > 0L)
        assertTrue(loaded.isChecksumValid())
    }

    @Test
    fun testChecksumDetectionAndSanitization() {
        val outOfBounds = SaveData(
            playerLevel = 9999, // Harus di-clamp ke 100
            currentExp = -50,   // Harus di-coerce ke 0
            gold = -500,        // Harus di-clamp ke 0
            highestTowerFloor = 0, // Harus di-clamp ke 1
            weaponAtkLevel = 99 // Harus di-clamp ke 10
        )

        outOfBounds.sanitize()
        assertEquals(100, outOfBounds.playerLevel)
        assertEquals(0, outOfBounds.currentExp)
        assertEquals(0, outOfBounds.gold)
        assertEquals(1, outOfBounds.highestTowerFloor)
        assertEquals(10, outOfBounds.weaponAtkLevel)

        outOfBounds.updateChecksum()
        assertTrue(outOfBounds.isChecksumValid())

        // Simulasi tampering / manipulasi memori
        outOfBounds.gold = 9999999
        assertFalse(outOfBounds.isChecksumValid(), "Checksum must detect untrusted modifications")
    }

    @Test
    fun testAtomicWriteAndRollingBackupRecovery() {
        // 1. Simpan data awal yang valid (Lv. 10)
        val validSave1 = SaveData(playerLevel = 10, gold = 1000, highestTowerFloor = 2)
        SaveManager.save(validSave1)

        // 2. Simpan revisi kedua (Lv. 12) -> save1 otomatis menjadi rolling backup (.bak)
        val validSave2 = SaveData(playerLevel = 12, gold = 1500, highestTowerFloor = 3)
        SaveManager.save(validSave2)

        assertTrue(testStorage.exists(SaveManager.BACKUP_FILE_NAME), "Rolling backup file must exist after secondary save")

        // 3. Simulasikan korupsi berkas utama (misal: ponsel mati mendadak saat menulis file utama)
        testStorage.writeString(SaveManager.SAVE_FILE_NAME, "{ corrupted json syntax error EOF...")

        // 4. Panggil load(): SaveManager harus mendeteksi error parsing dan otomatis pulih dari .bak
        val recovered = SaveManager.load()
        assertNotNull(recovered)
        // Data harus dipulihkan dari backup sebelumnya yang valid
        assertTrue(recovered.playerLevel in listOf(10, 12))
        assertTrue(recovered.gold >= 1000)

        // Verifikasi file utama diperbaiki kembali dengan data yang pulih
        assertTrue(testStorage.readString(SaveManager.SAVE_FILE_NAME)!!.contains("\"playerLevel\""))
    }

    @Test
    fun testExportAndImportJson() {
        val original = SaveData(
            playerLevel = 25,
            gold = 99999,
            highestTowerFloor = 5,
            selectedArchetype = "AGILE"
        )
        SaveManager.save(original)

        val exportedJson = SaveManager.exportJson()
        assertTrue(exportedJson.contains("\"playerLevel\": 25"))
        assertTrue(exportedJson.contains("\"gold\": 99999"))

        // Hapus storage lokal
        testStorage.clear()

        // Impor kembali dari string JSON
        val importSuccess = SaveManager.importJson(exportedJson)
        assertTrue(importSuccess)

        val reloaded = SaveManager.load()
        assertEquals(25, reloaded.playerLevel)
        assertEquals(99999, reloaded.gold)
        assertEquals("AGILE", reloaded.selectedArchetype)
    }

    @Test
    fun testResetSaveClearsAllStorage() {
        val progress = SaveData(playerLevel = 50, gold = 500000)
        SaveManager.save(progress)

        // Lakukan reset save
        val defaultData = SaveManager.resetSave()
        assertEquals(1, defaultData.playerLevel)
        assertEquals(0, defaultData.gold)
        assertEquals(1, defaultData.highestTowerFloor)

        val loadedAfterReset = SaveManager.load()
        assertEquals(1, loadedAfterReset.playerLevel)
        assertEquals(0, loadedAfterReset.gold)
    }

    @Test
    fun testTalentPointsPersistenceAndRespec() {
        val talentManager = com.stickman.tower.talent.TalentManager()
        val saveData = SaveData(playerLevel = 10, unspentTalentPoints = 5)
        talentManager.loadFromSave(saveData)
        assertEquals(5, talentManager.talentPoints)

        // Belanjakan 1 poin untuk membuka talent Tier 1 Berserker
        val canUnlock = talentManager.unlockTalent("BS_BLOOD_RAGE")
        assertTrue(canUnlock)
        assertEquals(4, talentManager.talentPoints)
        assertTrue(talentManager.isTalentUnlocked("BS_BLOOD_RAGE"))

        // Sinkronisasi ke SaveData dan simpan secara atomik
        talentManager.syncToSave(saveData)
        assertEquals(4, saveData.unspentTalentPoints)
        SaveManager.save(saveData)

        // Muat kembali dari storage
        val reloadedSave = SaveManager.load()
        assertEquals(4, reloadedSave.unspentTalentPoints)
        assertTrue(reloadedSave.isChecksumValid())

        val newTalentManager = com.stickman.tower.talent.TalentManager()
        newTalentManager.loadFromSave(reloadedSave)
        assertEquals(4, newTalentManager.talentPoints)
        assertTrue(newTalentManager.isTalentUnlocked("BS_BLOOD_RAGE"))

        // Lakukan Respec dan verifikasi pengembalian poin
        newTalentManager.respec()
        assertEquals(5, newTalentManager.talentPoints)
        assertFalse(newTalentManager.isTalentUnlocked("BS_BLOOD_RAGE"))
    }
}

package com.stickman.tower.rpg

/**
 * Data class representing persistent player progression with versioning, sanitization, and checksum integrity.
 */
data class SaveData(
    var playerLevel: Int = 1,
    var currentExp: Int = 0,
    var gold: Int = 0,
    var highestTowerFloor: Int = 1,
    var highestEndlessFloor: Int = 0,
    var totalEnemiesDefeated: Int = 0,
    var unlockedCharacters: ArrayList<String> = arrayListOf("STICKMAN_SWORD"),
    var selectedArchetype: String = "BALANCED",
    var selectedSubClass: String = "BERSERKER",
    var weaponAtkLevel: Int = 0,
    var weaponCritLevel: Int = 0,
    var weaponSpdLevel: Int = 0,
    var saveVersion: Int = 1,
    var checksum: Long = 0L
) {
    /**
     * Menghitung nilai checksum dari parameter progres penting untuk mendeteksi korupsi data.
     */
    fun calculateChecksum(): Long {
        var hash = 17L
        hash = 31L * hash + playerLevel
        hash = 31L * hash + currentExp
        hash = 31L * hash + gold
        hash = 31L * hash + highestTowerFloor
        hash = 31L * hash + highestEndlessFloor
        hash = 31L * hash + weaponAtkLevel
        hash = 31L * hash + weaponCritLevel
        hash = 31L * hash + weaponSpdLevel
        hash = 31L * hash + selectedArchetype.hashCode()
        hash = 31L * hash + selectedSubClass.hashCode()
        return hash and 0x7FFFFFFFFFFFFFFFL // Bilangan bulat positif 63-bit
    }

    /**
     * Memvalidasi dan membersihkan data agar berada dalam batasan aman (Sanitization).
     */
    fun sanitize(): SaveData {
        playerLevel = playerLevel.coerceIn(1, 100)
        currentExp = currentExp.coerceAtLeast(0)
        gold = gold.coerceIn(0, 999_999_999)
        highestTowerFloor = highestTowerFloor.coerceIn(1, 1000)
        highestEndlessFloor = highestEndlessFloor.coerceAtLeast(0)
        totalEnemiesDefeated = totalEnemiesDefeated.coerceAtLeast(0)
        weaponAtkLevel = weaponAtkLevel.coerceIn(0, 10)
        weaponCritLevel = weaponCritLevel.coerceIn(0, 10)
        weaponSpdLevel = weaponSpdLevel.coerceIn(0, 10)
        if (unlockedCharacters.isEmpty()) {
            unlockedCharacters.add("STICKMAN_SWORD")
        }
        return this
    }

    /**
     * Memperbarui checksum internal sebelum proses serialisasi.
     */
    fun updateChecksum() {
        checksum = calculateChecksum()
    }

    /**
     * Memeriksa apakah checksum data valid dan belum termutasi/korup.
     */
    fun isChecksumValid(): Boolean {
        if (checksum == 0L) return true // Kompatibilitas mundur dengan save legacy tanpa checksum
        return checksum == calculateChecksum()
    }
}

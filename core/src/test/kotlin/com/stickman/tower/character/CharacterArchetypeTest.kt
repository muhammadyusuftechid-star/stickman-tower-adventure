package com.stickman.tower.character

import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.rpg.SaveData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterArchetypeTest {

    @Test
    fun testArchetypeAttributes() {
        // BALANCED
        val balanced = CharacterArchetype.BALANCED
        assertEquals("Balanced Blade", balanced.displayName)
        assertEquals(7.5f, balanced.moveSpeed)
        assertEquals(11.5f, balanced.jumpVelocity)
        assertEquals(1.0f, balanced.baseDamageMultiplier)

        // AGILE
        val agile = CharacterArchetype.AGILE
        assertEquals("Shadow Daggers", agile.displayName)
        assertEquals(9.5f, agile.moveSpeed)
        assertTrue(agile.moveSpeed > balanced.moveSpeed)
        assertEquals(0.85f, agile.baseDamageMultiplier)
        assertEquals(0.15f, agile.baseCritBonus)

        // HEAVY
        val heavy = CharacterArchetype.HEAVY
        assertEquals("Titan Greatsword", heavy.displayName)
        assertEquals(5.8f, heavy.moveSpeed)
        assertTrue(heavy.moveSpeed < balanced.moveSpeed)
        assertEquals(1.55f, heavy.baseDamageMultiplier)
        assertTrue(heavy.baseDefenseBonus > 0f)
    }

    @Test
    fun testArchetypeFromString() {
        assertEquals(CharacterArchetype.BALANCED, CharacterArchetype.fromString("BALANCED"))
        assertEquals(CharacterArchetype.AGILE, CharacterArchetype.fromString("AGILE"))
        assertEquals(CharacterArchetype.HEAVY, CharacterArchetype.fromString("HEAVY"))
        assertEquals(CharacterArchetype.BALANCED, CharacterArchetype.fromString("UNKNOWN_VALUE"))
    }

    @Test
    fun testSaveDataArchetypePersistence() {
        val stats = CharacterStats(level = 3, currentExp = 45, gold = 120)
        val saveData = stats.toSaveData(highestFloor = 2, selectedArchetype = "AGILE")

        assertEquals(3, saveData.playerLevel)
        assertEquals(45, saveData.currentExp)
        assertEquals(120, saveData.gold)
        assertEquals(2, saveData.highestTowerFloor)
        assertEquals("AGILE", saveData.selectedArchetype)
    }
}

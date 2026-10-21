package com.stickman.tower.rpg

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

/**
 * Manages player RPG statistics, leveling curve, damage calculation, and EXP/Gold accumulation.
 */
class CharacterStats(
    var level: Int = 1,
    val baseMaxHp: Float = 100f,
    val baseAttack: Float = 15f,
    var defense: Float = 2f,
    var critRate: Float = 0.08f,
    var critMultiplier: Float = 1.6f,
    var currentExp: Int = 0,
    var gold: Int = 0
) {
    val maxHp: Float
        get() = baseMaxHp * (1f + 0.10f * (level - 1))

    val attackPower: Float
        get() = baseAttack * (1f + 0.08f * (level - 1))

    val requiredExp: Int
        get() = max(50, floor(50.0 * (level.toDouble()).pow(1.4)).toInt())

    /**
     * Adds EXP and returns the number of levels gained (0 if no level up occurred).
     */
    fun addExp(amount: Int): Int {
        var levelsGained = 0
        currentExp += amount
        while (currentExp >= requiredExp) {
            currentExp -= requiredExp
            level++
            levelsGained++
        }
        return levelsGained
    }

    /**
     * Adds gold coins collected.
     */
    fun addGold(amount: Int) {
        gold += amount
    }

    /**
     * Calculates final damage based on skill base damage, attack scaling, target defense, and critical chance.
     * Returns a Pair of (finalDamage: Float, isCritical: Boolean).
     */
    fun calculateDamage(skillBaseDamage: Float, targetDefense: Float = 0f): Pair<Float, Boolean> {
        val rawDamage = skillBaseDamage * (attackPower / baseAttack)
        val isCrit = Random.nextFloat() < critRate
        val multiplier = if (isCrit) critMultiplier else 1.0f
        val finalDmg = max(1f, (rawDamage - targetDefense) * multiplier)
        return Pair(finalDmg, isCrit)
    }

    fun toSaveData(highestFloor: Int = 1): SaveData {
        return SaveData(
            playerLevel = level,
            currentExp = currentExp,
            gold = gold,
            highestTowerFloor = highestFloor
        )
    }

    fun loadFromSaveData(data: SaveData) {
        this.level = max(1, data.playerLevel)
        this.currentExp = max(0, data.currentExp)
        this.gold = max(0, data.gold)
    }
}

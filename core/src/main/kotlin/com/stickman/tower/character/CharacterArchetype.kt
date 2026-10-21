package com.stickman.tower.character

/**
 * Arketipe Karakter Stickman sesuai GDD v0.1:
 * - BALANCED: Gaya seimbang (pedang tunggal).
 * - AGILE: Gaya bertempo cepat (pisau ganda, lari kencang, crit tinggi).
 * - HEAVY: Gaya bertempo berat (pedang raksasa masif, defense tinggi, knockback kuat).
 */
enum class CharacterArchetype(
    val displayName: String,
    val title: String,
    val description: String,
    val moveSpeed: Float,
    val jumpVelocity: Float,
    val baseDamageMultiplier: Float,
    val baseDefenseBonus: Float,
    val baseCritBonus: Float,
    val skillName: String,
    val ultName: String
) {
    BALANCED(
        displayName = "Balanced Blade",
        title = "The Wandering Swordsman",
        description = "Gaya seimbang dengan mobilitas stabil dan kombo pedang 3-hit yang luwes.",
        moveSpeed = 7.5f,
        jumpVelocity = 11.5f,
        baseDamageMultiplier = 1.0f,
        baseDefenseBonus = 0f,
        baseCritBonus = 0f,
        skillName = "Dash Strike",
        ultName = "Bladestorm 360°"
    ),
    AGILE(
        displayName = "Shadow Daggers",
        title = "The Silent Assassin",
        description = "Kecepatan luar biasa dengan serangan cepat bertubi-tubi dan peluang critical tinggi.",
        moveSpeed = 9.5f,
        jumpVelocity = 12.2f,
        baseDamageMultiplier = 0.85f,
        baseDefenseBonus = -1.0f,
        baseCritBonus = 0.15f,
        skillName = "Shadow Dash",
        ultName = "Phantom Flurry"
    ),
    HEAVY(
        displayName = "Titan Greatsword",
        title = "The Tower Juggernaut",
        description = "Kekuatan brutal dengan pertahanan tebal dan hantaman pedang raksasa berdaya hancur tinggi.",
        moveSpeed = 5.8f,
        jumpVelocity = 10.8f,
        baseDamageMultiplier = 1.55f,
        baseDefenseBonus = 3.5f,
        baseCritBonus = -0.02f,
        skillName = "Ground Breaker",
        ultName = "Meteor Cleave"
    );

    companion object {
        fun fromString(name: String): CharacterArchetype {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BALANCED
        }
    }
}

package com.stickman.tower.combat

/**
 * Komponen Skill biasa berbasis waktu cooldown.
 */
class Skill(
    val name: String = "Dash Strike",
    val cooldown: Float = 4.5f
) {
    var remainingCooldown: Float = 0f
        private set

    val isReady: Boolean get() = remainingCooldown <= 0f
    val cooldownPercent: Float get() = (remainingCooldown / cooldown).coerceIn(0f, 1f)

    fun update(delta: Float) {
        if (remainingCooldown > 0f) {
            remainingCooldown = (remainingCooldown - delta).coerceAtLeast(0f)
        }
    }

    /**
     * Mengaktifkan skill jika cooldown telah selesai.
     */
    fun use(): Boolean {
        if (isReady) {
            remainingCooldown = cooldown
            return true
        }
        return false
    }

    fun reset() {
        remainingCooldown = 0f
    }
}

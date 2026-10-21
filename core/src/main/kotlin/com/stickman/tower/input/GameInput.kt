package com.stickman.tower.input

/**
 * Antarmuka abstraksi input untuk kontrol gameplay.
 * Memisahkan logika karakter dari platform fisik (Keyboard vs Android Touchscreen).
 */
interface GameInput {
    fun isMoveLeft(): Boolean
    fun isMoveRight(): Boolean
    fun isJumpJustPressed(): Boolean
    fun isJumpPressed(): Boolean
    fun isAttackJustPressed(): Boolean
    fun isDodgeJustPressed(): Boolean
    fun isSkillJustPressed(): Boolean
    fun isUltimateJustPressed(): Boolean
}

package com.stickman.tower.input

/**
 * Menggabungkan input keyboard (Desktop) dan virtual touch controller (Android)
 * ke dalam satu antarmuka GameInput yang mulus.
 */
class CompositeInput(
    val keyboard: KeyboardInput,
    val virtual: VirtualController
) : GameInput {
    fun update() {
        virtual.update()
    }

    override fun isMoveLeft() = keyboard.isMoveLeft() || virtual.isMoveLeft()
    override fun isMoveRight() = keyboard.isMoveRight() || virtual.isMoveRight()
    override fun isJumpJustPressed() = keyboard.isJumpJustPressed() || virtual.isJumpJustPressed()
    override fun isJumpPressed() = keyboard.isJumpPressed() || virtual.isJumpPressed()
    override fun isAttackJustPressed() = keyboard.isAttackJustPressed() || virtual.isAttackJustPressed()
    override fun isDodgeJustPressed() = keyboard.isDodgeJustPressed() || virtual.isDodgeJustPressed()
    override fun isSkillJustPressed() = keyboard.isSkillJustPressed() || virtual.isSkillJustPressed()
    override fun isUltimateJustPressed() = keyboard.isUltimateJustPressed() || virtual.isUltimateJustPressed()
}

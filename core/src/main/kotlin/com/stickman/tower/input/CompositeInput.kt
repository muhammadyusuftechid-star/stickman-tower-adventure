package com.stickman.tower.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * Menggabungkan input keyboard (Desktop), virtual touch controller (Android),
 * dan Gamepad fisik (Bluetooth/USB) ke dalam satu antarmuka GameInput yang mulus.
 */
class CompositeInput(
    val keyboard: KeyboardInput,
    val virtual: VirtualController,
    val gamepad: GamepadInput = GamepadInput()
) : GameInput {

    fun update(viewport: Viewport? = null) {
        gamepad.update()

        // Auto-detection: Sembunyikan tombol layar sentuh jika gamepad aktif digunakan,
        // namun segera tampilkan kembali jika pemain menyentuh layar.
        val settings = TouchSettingsManager.load()
        if (settings.autoHideOnGamepad && gamepad.hasActiveGamepad()) {
            val isScreenTouched = Gdx.input != null && (Gdx.input.isTouched(0) || Gdx.input.isTouched(1))
            if (isScreenTouched) {
                virtual.isVisible = true
            } else if (gamepad.isGamepadInUse) {
                virtual.isVisible = false
            }
        } else {
            virtual.isVisible = true
        }

        virtual.update(viewport)
    }

    override fun isMoveLeft() = keyboard.isMoveLeft() || virtual.isMoveLeft() || gamepad.isMoveLeft()
    override fun isMoveRight() = keyboard.isMoveRight() || virtual.isMoveRight() || gamepad.isMoveRight()
    override fun isJumpJustPressed() = keyboard.isJumpJustPressed() || virtual.isJumpJustPressed() || gamepad.isJumpJustPressed()
    override fun isJumpPressed() = keyboard.isJumpPressed() || virtual.isJumpPressed() || gamepad.isJumpPressed()
    override fun isAttackJustPressed() = keyboard.isAttackJustPressed() || virtual.isAttackJustPressed() || gamepad.isAttackJustPressed()
    override fun isDodgeJustPressed() = keyboard.isDodgeJustPressed() || virtual.isDodgeJustPressed() || gamepad.isDodgeJustPressed()
    override fun isSkillJustPressed() = keyboard.isSkillJustPressed() || virtual.isSkillJustPressed() || gamepad.isSkillJustPressed()
    override fun isUltimateJustPressed() = keyboard.isUltimateJustPressed() || virtual.isUltimateJustPressed() || gamepad.isUltimateJustPressed()
    fun isPauseJustPressed() = gamepad.isPauseJustPressed()
}

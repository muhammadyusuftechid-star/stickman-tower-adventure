package com.stickman.tower.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys

/**
 * Implementasi input keyboard untuk build Desktop (LWJGL3).
 * Mendukung tombol A/D, Panah Kiri/Kanan, serta Space/W/K untuk melompat.
 */
class KeyboardInput : GameInput {
    override fun isMoveLeft(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyPressed(Keys.A) || input.isKeyPressed(Keys.LEFT)
    }

    override fun isMoveRight(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyPressed(Keys.D) || input.isKeyPressed(Keys.RIGHT)
    }

    override fun isJumpJustPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyJustPressed(Keys.SPACE) ||
               input.isKeyJustPressed(Keys.W) ||
               input.isKeyJustPressed(Keys.UP) ||
               input.isKeyJustPressed(Keys.K)
    }

    override fun isJumpPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyPressed(Keys.SPACE) ||
               input.isKeyPressed(Keys.W) ||
               input.isKeyPressed(Keys.UP) ||
               input.isKeyPressed(Keys.K)
    }

    override fun isAttackJustPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyJustPressed(Keys.J) ||
               input.isKeyJustPressed(Keys.ENTER) ||
               input.isKeyJustPressed(Keys.X) ||
               input.isButtonJustPressed(Buttons.LEFT)
    }

    override fun isDodgeJustPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyJustPressed(Keys.L) ||
               input.isKeyJustPressed(Keys.SHIFT_LEFT) ||
               input.isKeyJustPressed(Keys.C)
    }

    override fun isSkillJustPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyJustPressed(Keys.U) ||
               input.isKeyJustPressed(Keys.I)
    }

    override fun isUltimateJustPressed(): Boolean {
        val input = Gdx.input ?: return false
        return input.isKeyJustPressed(Keys.O) ||
               input.isKeyJustPressed(Keys.Q)
    }
}

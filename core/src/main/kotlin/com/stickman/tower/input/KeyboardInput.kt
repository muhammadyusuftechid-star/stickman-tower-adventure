package com.stickman.tower.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys

/**
 * Implementasi input keyboard untuk build Desktop (LWJGL3).
 * Mendukung tombol A/D, Panah Kiri/Kanan, serta Space/W/K untuk melompat.
 */
class KeyboardInput : GameInput {
    override fun isMoveLeft(): Boolean {
        return Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)
    }

    override fun isMoveRight(): Boolean {
        return Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)
    }

    override fun isJumpJustPressed(): Boolean {
        return Gdx.input.isKeyJustPressed(Keys.SPACE) ||
               Gdx.input.isKeyJustPressed(Keys.W) ||
               Gdx.input.isKeyJustPressed(Keys.UP) ||
               Gdx.input.isKeyJustPressed(Keys.K)
    }

    override fun isJumpPressed(): Boolean {
        return Gdx.input.isKeyPressed(Keys.SPACE) ||
               Gdx.input.isKeyPressed(Keys.W) ||
               Gdx.input.isKeyPressed(Keys.UP) ||
               Gdx.input.isKeyPressed(Keys.K)
    }

    override fun isAttackJustPressed(): Boolean {
        return Gdx.input.isKeyJustPressed(Keys.J) ||
               Gdx.input.isKeyJustPressed(Keys.ENTER) ||
               Gdx.input.isKeyJustPressed(Keys.X) ||
               Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)
    }
}

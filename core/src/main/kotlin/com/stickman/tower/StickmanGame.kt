package com.stickman.tower

import com.badlogic.gdx.Game
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.screen.MainMenuScreen

/**
 * Game root controller untuk Stickman Tower Adventure.
 * Mengelola transisi antar screen menggunakan libGDX Game framework.
 */
class StickmanGame : Game() {
    override fun create() {
        AudioManager.init()
        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        super.dispose()
        screen?.dispose()
        AudioManager.dispose()
    }
}

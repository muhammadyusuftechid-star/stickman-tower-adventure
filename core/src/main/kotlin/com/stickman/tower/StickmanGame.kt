package com.stickman.tower

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.screen.MainMenuScreen
import com.stickman.tower.ui.FontManager

/**
 * Game root controller untuk Stickman Tower Adventure.
 * Mengelola transisi antar screen menggunakan libGDX Game framework,
 * dengan otomatisasi pembebasan memori native (auto-dispose old screen).
 */
class StickmanGame : Game() {
    override fun create() {
        AudioManager.init()
        setScreen(MainMenuScreen(this))
    }

    override fun setScreen(screen: Screen?) {
        val oldScreen = this.screen
        super.setScreen(screen)
        oldScreen?.dispose()
    }

    override fun dispose() {
        super.dispose()
        screen?.dispose()
        AudioManager.dispose()
        FontManager.dispose()
    }
}


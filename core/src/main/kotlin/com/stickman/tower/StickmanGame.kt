package com.stickman.tower

import com.badlogic.gdx.Game
import com.stickman.tower.screen.PlayScreen

/**
 * Game root controller untuk Stickman Tower Adventure.
 * Mengelola transisi antar screen menggunakan libGDX Game framework.
 */
class StickmanGame : Game() {
    override fun create() {
        // Luncurkan langsung ke PlayScreen untuk pengujian platformer
        setScreen(PlayScreen())
    }

    override fun dispose() {
        super.dispose()
        screen?.dispose()
    }
}

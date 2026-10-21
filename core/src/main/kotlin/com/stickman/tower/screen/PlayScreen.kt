package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils
import com.stickman.tower.character.Player
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.CameraManager

/**
 * Screen gameplay utama untuk menguji platformer physics,
 * movement, collision, camera follow, dan animasi stickman.
 */
class PlayScreen : Screen {
    private val cameraManager = CameraManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont() // Font bawaan libGDX untuk status debug

    private val input = KeyboardInput()
    private val player = Player(spawnX = 0f, spawnY = 2f)

    private val platforms = mutableListOf<Platform>()
    private var showDebugBounds = false

    override fun show() {
        // Konfigurasi Batasan Kamera (Level Boundaries)
        cameraManager.setBounds(left = -12f, bottom = -1f, right = 33f, top = 14f)

        // Setup Arena Platform Uji Coba
        platforms.clear()

        // 1. Lantai Dasar Utama
        platforms.add(Platform(x = -12f, y = 0f, width = 45f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))

        // 2. Dinding Pembatas Kiri dan Kanan
        platforms.add(Platform(x = -12f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 32f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        // 3. Platform Mengambang Bertingkat (Uji Coba Lompatan)
        platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.5f, y = 4.3f, width = 3.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 14.5f, y = 6.2f, width = 5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 22f, y = 3.5f, width = 4f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        // 4. Rintangan Kotak Rendah
        platforms.add(Platform(x = -4f, y = 1f, width = 1.5f, height = 1.2f, color = Color(0.28f, 0.32f, 0.40f, 1f)))
    }

    override fun render(delta: Float) {
        // Toggle Debug Bounding Box via F1 atau TAB
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        // Clamp delta agar tidak loncat saat lag/freeze sesaat
        val safeDelta = delta.coerceAtMost(1f / 30f)

        // 1. Pembaruan Logika Gameplay
        player.update(safeDelta, input, platforms)

        // 2. Pembaruan Kamera Mengikuti Karakter
        val playerCenterX = player.position.x + player.width / 2f
        val playerCenterY = player.position.y + player.height / 2f + 0.5f
        cameraManager.update(playerCenterX, playerCenterY, safeDelta)

        // 3. Bersihkan Layar (Dark Slate Modern Theme)
        ScreenUtils.clear(0.08f, 0.09f, 0.13f, 1f)

        // 4. Render Dunia 2D Menggunakan ShapeRenderer
        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        // Mode Filled untuk Platform dan Karakter
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (platform in platforms) {
            platform.render(shapeRenderer)
        }
        player.render(shapeRenderer)
        shapeRenderer.end()

        // Mode Line untuk Debug Bounding Box (jika aktif)
        if (showDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            player.renderDebug(shapeRenderer)
            for (platform in platforms) {
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.rect(platform.bounds.x, platform.bounds.y, platform.bounds.width, platform.bounds.height)
            }
            shapeRenderer.end()
        }

        // 5. Render Informasi Debug Overlay di Pojok Layar
        renderDebugOverlay()
    }

    private fun renderDebugOverlay() {
        batch.begin()
        font.color = Color.CYAN
        font.draw(batch, "Stickman Tower - Phase 1 Prototype", 15f, Gdx.graphics.height - 15f)

        font.color = Color.WHITE
        font.draw(batch, "FPS: ${Gdx.graphics.framesPerSecond}", 15f, Gdx.graphics.height - 35f)
        font.draw(
            batch,
            "State: ${player.state} | Grounded: ${player.isGrounded} | Facing: ${if (player.facingRight) "Right" else "Left"}",
            15f,
            Gdx.graphics.height - 55f
        )
        font.draw(
            batch,
            String.format("Pos: (%.2f, %.2f) | Vel: (%.2f, %.2f)", player.position.x, player.position.y, player.velocity.x, player.velocity.y),
            15f,
            Gdx.graphics.height - 75f
        )

        font.color = Color.LIGHT_GRAY
        font.draw(
            batch,
            "Kontrol: [A / D] Gerak | [SPACE / W] Lompat | [F1 / TAB] Debug Bounding Box",
            15f,
            25f
        )
        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        cameraManager.resize(width, height)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
    }
}

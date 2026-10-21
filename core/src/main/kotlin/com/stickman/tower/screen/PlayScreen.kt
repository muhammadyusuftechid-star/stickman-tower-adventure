package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.stickman.tower.character.Player
import com.stickman.tower.combat.FloatingText
import com.stickman.tower.combat.HitStopManager
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.CameraManager
import com.stickman.tower.world.Checkpoint
import com.stickman.tower.world.StageExit

/**
 * Screen gameplay utama Fase 3:
 * Mengintegrasikan AI musuh lengkap, pertarungan dua arah, checkpoint lentera,
 * portal stage exit, serta game loop MVP (Playing, Game Over, Stage Clear).
 */
class PlayScreen : Screen {
    private val cameraManager = CameraManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val input = KeyboardInput()
    private val player = Player(spawnX = -1f, spawnY = 1.5f)

    private val platforms = mutableListOf<Platform>()
    private val enemies = mutableListOf<Enemy>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val hitStopManager = HitStopManager()

    // Elemen Stage Interaktif
    private val checkpoint = Checkpoint(x = 12f, y = 1f)
    private val stageExit = StageExit(x = 28f, y = 1f)
    private val activeCheckpointPos = Vector2(-1f, 1.5f)

    // Status Game Loop MVP
    var gameStatus: GameStatus = GameStatus.PLAYING
        private set

    private var showDebugBounds = false

    override fun show() {
        // Konfigurasi Batasan Kamera (Level Boundaries)
        cameraManager.setBounds(left = -12f, bottom = -1f, right = 33f, top = 14f)

        setupStage()
    }

    private fun setupStage() {
        // 1. Setup Platform Arena
        platforms.clear()
        platforms.add(Platform(x = -12f, y = 0f, width = 45f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))
        platforms.add(Platform(x = -12f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 32f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        // Platform Mengambang Bertingkat
        platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.5f, y = 4.3f, width = 3.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 13.5f, y = 6.2f, width = 5.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 21f, y = 3.5f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        // 2. Setup Musuh Bertaring (Enemy Patrol & Guard)
        enemies.clear()
        enemies.add(Enemy(spawnX = 5.0f, spawnY = 1.2f))   // Patroli lantai 1
        enemies.add(Enemy(spawnX = 15.0f, spawnY = 7.0f))  // Penjaga platform atas
        enemies.add(Enemy(spawnX = 23.5f, spawnY = 1.2f))  // Penjaga depan portal exit

        gameStatus = GameStatus.PLAYING
    }

    override fun render(delta: Float) {
        // Toggle Debug Bounding Box via F1 atau TAB
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        // Restart atau Respawn saat kondisi Game Over / Stage Clear
        handleStatusInput()

        val safeDelta = delta.coerceAtMost(1f / 30f)

        // 1. Hit Stop Micro-Freeze Update
        val isFrozen = hitStopManager.update(safeDelta)

        if (!isFrozen && gameStatus == GameStatus.PLAYING) {
            // Pembaruan Pemain
            player.update(safeDelta, input, platforms)

            // Cek Trigger Checkpoint
            if (checkpoint.checkTrigger(player.bounds)) {
                activeCheckpointPos.set(checkpoint.x, checkpoint.y + 0.2f)
                floatingTexts.add(
                    FloatingText("CHECKPOINT!", checkpoint.x, checkpoint.y + 2.2f, Color.CYAN, duration = 1.5f)
                )
            }

            // Pembaruan Seluruh Musuh
            for (enemy in enemies) {
                enemy.update(safeDelta, player, platforms)
            }

            // 2. Deteksi Pertarungan Dua Arah (Pemain <-> Musuh)
            handleTwoWayCombat()

            // 3. Evaluasi Kondisi Menang & Kalah
            evaluateGameConditions()
        }

        // 4. Perbarui Teks Floating Damage
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.update(safeDelta)
            if (ft.isExpired) iterator.remove()
        }

        // 5. Perbarui Kamera Mengikuti Pemain
        val playerCenterX = player.position.x + player.width / 2f
        val playerCenterY = player.position.y + player.height / 2f + 0.5f
        cameraManager.update(playerCenterX, playerCenterY, safeDelta)

        // 6. Bersihkan Layar
        ScreenUtils.clear(0.08f, 0.09f, 0.13f, 1f)

        // 7. Render Objek 2D
        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        // Render Mode Filled
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (platform in platforms) {
            platform.render(shapeRenderer)
        }
        checkpoint.render(shapeRenderer)
        stageExit.render(shapeRenderer)

        for (enemy in enemies) {
            enemy.render(shapeRenderer)
        }
        player.render(shapeRenderer)
        shapeRenderer.end()

        // Render Mode Line untuk Debug
        if (showDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            player.renderDebug(shapeRenderer)
            for (enemy in enemies) {
                enemy.renderDebug(shapeRenderer)
            }
            for (platform in platforms) {
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.rect(platform.bounds.x, platform.bounds.y, platform.bounds.width, platform.bounds.height)
            }
            shapeRenderer.end()
        }

        // 8. Render Floating Texts
        batch.begin()
        for (ft in floatingTexts) {
            ft.render(batch, font, cameraManager.camera)
        }
        batch.end()

        // 9. Render HUD & Banner Game Over / Victory
        renderHUDAndOverlays()
    }

    private fun handleTwoWayCombat() {
        // A. Serangan Pemain Mengenai Musuh
        if (player.hitbox.isActive) {
            for (enemy in enemies) {
                if (!enemy.health.isDead && !player.hitbox.hitEntities.contains(enemy)) {
                    if (player.hitbox.bounds.overlaps(enemy.hurtbox.bounds)) {
                        player.hitbox.hitEntities.add(enemy)

                        val knockX = if (player.facingRight) player.hitbox.knockbackX else -player.hitbox.knockbackX
                        val knockY = player.hitbox.knockbackY

                        if (enemy.takeHit(player.hitbox.damage, knockX, knockY)) {
                            hitStopManager.trigger(0.06f)
                            floatingTexts.add(
                                FloatingText(
                                    "-${player.hitbox.damage.toInt()}",
                                    enemy.position.x + enemy.width / 2f,
                                    enemy.position.y + enemy.height + 0.35f,
                                    Color(1f, 0.25f, 0.25f, 1f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // B. Serangan Musuh Mengenai Pemain
        for (enemy in enemies) {
            if (enemy.hitbox.isActive && !enemy.hitbox.hitEntities.contains(player) && !player.health.isDead) {
                if (enemy.hitbox.bounds.overlaps(player.hurtbox.bounds)) {
                    enemy.hitbox.hitEntities.add(player)

                    val knockX = if (enemy.facingRight) enemy.hitbox.knockbackX else -enemy.hitbox.knockbackX
                    val knockY = enemy.hitbox.knockbackY

                    if (player.takeHit(enemy.hitbox.damage, knockX, knockY)) {
                        hitStopManager.trigger(0.08f)
                        floatingTexts.add(
                            FloatingText(
                                "-${enemy.hitbox.damage.toInt()}",
                                player.position.x + player.width / 2f,
                                player.position.y + player.height + 0.35f,
                                Color(1f, 0.35f, 0.35f, 1f)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun evaluateGameConditions() {
        // Cek Kekalahan: HP Pemain == 0
        if (player.health.isDead && gameStatus == GameStatus.PLAYING) {
            gameStatus = GameStatus.GAME_OVER
            floatingTexts.add(
                FloatingText("DEFEATED!", player.position.x, player.position.y + 2.2f, Color.RED, duration = 2.0f)
            )
        }

        // Cek Kemenangan: Menyentuh Portal Exit setelah minimal mengalahkan 2 musuh
        val aliveEnemies = enemies.count { !it.health.isDead }
        if (aliveEnemies == 0 && stageExit.bounds.overlaps(player.bounds) && gameStatus == GameStatus.PLAYING) {
            gameStatus = GameStatus.STAGE_CLEAR
            floatingTexts.add(
                FloatingText("VICTORY!", stageExit.x + 0.8f, stageExit.y + 3.2f, Color.GOLD, duration = 2.5f)
            )
        }
    }

    private fun handleStatusInput() {
        if (gameStatus == GameStatus.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                // Respawn di Checkpoint Terakhir
                player.position.set(activeCheckpointPos.x, activeCheckpointPos.y)
                player.velocity.set(0f, 0f)
                player.health.reset()
                gameStatus = GameStatus.PLAYING
            }
        } else if (gameStatus == GameStatus.STAGE_CLEAR) {
            if (Gdx.input.isKeyJustPressed(Keys.R) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                // Restart Stage MVP
                player.position.set(-1f, 1.5f)
                player.velocity.set(0f, 0f)
                player.health.reset()
                activeCheckpointPos.set(-1f, 1.5f)
                setupStage()
            }
        }
    }

    private fun renderHUDAndOverlays() {
        batch.begin()

        // 1. Player Health Bar di Pojok Kiri Atas
        val hpPercent = (player.health.currentHp / player.health.maxHp).coerceIn(0f, 1f)
        font.color = Color.WHITE
        font.draw(batch, "HP PEMAIN:", 20f, Gdx.graphics.height - 20f)

        // 2. Info Status Stage
        val remaining = enemies.count { !it.health.isDead }
        font.color = if (remaining == 0) Color.GREEN else Color.ORANGE
        font.draw(batch, "Sisa Musuh: $remaining / ${enemies.size}", 20f, Gdx.graphics.height - 45f)

        font.color = if (checkpoint.isActivated) Color.CYAN else Color.GRAY
        font.draw(batch, "Checkpoint: ${if (checkpoint.isActivated) "AKTIF" else "BELUM"}", 20f, Gdx.graphics.height - 70f)

        // 3. Petunjuk Kontrol Bawah Layar
        font.color = Color.LIGHT_GRAY
        font.draw(
            batch,
            "Kontrol: [A / D] Gerak | [SPACE / W] Lompat | [J / ENTER / Klik] Serang | [F1 / TAB] Debug",
            20f,
            25f
        )

        // 4. Banner Game Over Overlay
        if (gameStatus == GameStatus.GAME_OVER) {
            font.color = Color.RED
            font.draw(batch, "=== GAME OVER ===", Gdx.graphics.width / 2f - 75f, Gdx.graphics.height / 2f + 20f)
            font.color = Color.WHITE
            font.draw(batch, "Tekan [R] untuk Respawn di Checkpoint", Gdx.graphics.width / 2f - 140f, Gdx.graphics.height / 2f - 10f)
        }

        // 5. Banner Stage Clear Overlay (MVP Tercapai!)
        if (gameStatus == GameStatus.STAGE_CLEAR) {
            font.color = Color.GOLD
            font.draw(batch, "★ STAGE CLEAR! MVP VALIDATED! ★", Gdx.graphics.width / 2f - 130f, Gdx.graphics.height / 2f + 30f)
            font.color = Color.WHITE
            font.draw(batch, "Semua musuh dikalahkan & Portal tercapai!", Gdx.graphics.width / 2f - 150f, Gdx.graphics.height / 2f + 5f)
            font.color = Color.CYAN
            font.draw(batch, "Tekan [SPACE] atau [R] untuk Main Ulang", Gdx.graphics.width / 2f - 140f, Gdx.graphics.height / 2f - 25f)
        }

        batch.end()

        // Render Bar HP Pemain Visual Menggunakan ShapeRenderer (Screen Space)
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Frame Bar
        shapeRenderer.color = Color(0.15f, 0.15f, 0.2f, 0.8f)
        shapeRenderer.rect(120f, Gdx.graphics.height - 35f, 150f, 16f)

        // Fill HP
        shapeRenderer.color = when {
            hpPercent > 0.5f -> Color.GREEN
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color.RED
        }
        shapeRenderer.rect(120f, Gdx.graphics.height - 35f, 150f * hpPercent, 16f)

        shapeRenderer.end()
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

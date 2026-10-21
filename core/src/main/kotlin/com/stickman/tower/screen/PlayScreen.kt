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
import com.stickman.tower.combat.ComboStep
import com.stickman.tower.combat.FloatingText
import com.stickman.tower.combat.HitStopManager
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.input.CompositeInput
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.input.VirtualController
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.CameraManager
import com.stickman.tower.world.Checkpoint
import com.stickman.tower.world.StageExit

/**
 * Screen gameplay utama Fase 4:
 * Mendukung Combo 3-Hit, Dodge Roll (i-Frames & Afterimages),
 * Skill 1 Dash Strike, Ultimate Bladestorm 360°,
 * serta On-Screen Virtual Touch Controller untuk Android & Desktop.
 */
class PlayScreen : Screen {
    private val cameraManager = CameraManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()

    // Dual Input Layer: Keyboard Desktop + Virtual Controller Mobile Touch
    private val keyboardInput = KeyboardInput()
    val virtualController = VirtualController()
    private val input = CompositeInput(keyboardInput, virtualController)

    private val player = Player(spawnX = -1f, spawnY = 1.5f)

    private val platforms = mutableListOf<Platform>()
    private val enemies = mutableListOf<Enemy>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val hitStopManager = HitStopManager()

    // Elemen Stage Interaktif
    private val checkpoint = Checkpoint(x = 12f, y = 1f)
    private val stageExit = StageExit(x = 28f, y = 1f)
    private val activeCheckpointPos = Vector2(-1f, 1.5f)

    // Status Game Loop
    var gameStatus: GameStatus = GameStatus.PLAYING
        private set

    private var showDebugBounds = false

    override fun show() {
        cameraManager.setBounds(left = -12f, bottom = -1f, right = 33f, top = 14f)
        virtualController.resize(Gdx.graphics.width, Gdx.graphics.height)
        setupStage()
    }

    private fun setupStage() {
        platforms.clear()
        platforms.add(Platform(x = -12f, y = 0f, width = 45f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))
        platforms.add(Platform(x = -12f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 32f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.5f, y = 4.3f, width = 3.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 13.5f, y = 6.2f, width = 5.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 21f, y = 3.5f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        enemies.clear()
        enemies.add(Enemy(spawnX = 5.0f, spawnY = 1.2f))
        enemies.add(Enemy(spawnX = 15.0f, spawnY = 7.0f))
        enemies.add(Enemy(spawnX = 23.5f, spawnY = 1.2f))

        gameStatus = GameStatus.PLAYING
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        // Perbarui Input Virtual Controller
        input.update()

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

        // 7. Render Objek Dunia 2D
        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (platform in platforms) platform.render(shapeRenderer)
        checkpoint.render(shapeRenderer)
        stageExit.render(shapeRenderer)

        for (enemy in enemies) enemy.render(shapeRenderer)
        player.render(shapeRenderer)
        shapeRenderer.end()

        // Debug Bounding Boxes
        if (showDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            player.renderDebug(shapeRenderer)
            for (enemy in enemies) enemy.renderDebug(shapeRenderer)
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

        // 9. Render Virtual Controller (On-Screen Touch Buttons untuk Android/Desktop)
        virtualController.render(shapeRenderer, batch, font, player.skill1, player.ultimate)

        // 10. Render HUD Status Pemain
        renderHUD()
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

                            // Akumulasi Energi Ultimate
                            val energyGain = when {
                                player.isUlting -> 0f
                                player.comboManager.currentStep == ComboStep.HIT_3 -> 15f
                                player.isSkillStriking -> 12f
                                else -> 8f
                            }
                            player.ultimate.addEnergy(energyGain)

                            // Floating Damage Text
                            val textColor = when {
                                player.isUlting -> Color.GOLD
                                player.isSkillStriking -> Color(0.9f, 0.4f, 1f, 1f)
                                player.comboManager.currentStep == ComboStep.HIT_3 -> Color.ORANGE
                                else -> Color(1f, 0.25f, 0.25f, 1f)
                            }

                            floatingTexts.add(
                                FloatingText(
                                    "-${player.hitbox.damage.toInt()}",
                                    enemy.position.x + enemy.width / 2f,
                                    enemy.position.y + enemy.height + 0.35f,
                                    textColor
                                )
                            )
                        }
                    }
                }
            }
        }

        // B. Serangan Musuh Mengenai Pemain (Dodge memberikan invulnerability!)
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
        if (player.health.isDead && gameStatus == GameStatus.PLAYING) {
            gameStatus = GameStatus.GAME_OVER
            floatingTexts.add(
                FloatingText("DEFEATED!", player.position.x, player.position.y + 2.2f, Color.RED, duration = 2.0f)
            )
        }

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
                player.position.set(activeCheckpointPos.x, activeCheckpointPos.y)
                player.velocity.set(0f, 0f)
                player.health.reset()
                player.ultimate.reset()
                gameStatus = GameStatus.PLAYING
            }
        } else if (gameStatus == GameStatus.STAGE_CLEAR) {
            if (Gdx.input.isKeyJustPressed(Keys.R) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                player.position.set(-1f, 1.5f)
                player.velocity.set(0f, 0f)
                player.health.reset()
                player.ultimate.reset()
                activeCheckpointPos.set(-1f, 1.5f)
                setupStage()
            }
        }
    }

    private fun renderHUD() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        // 1. Render Bar HP & Energy Menggunakan ShapeRenderer
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // HP Bar Background
        shapeRenderer.color = Color(0.12f, 0.15f, 0.2f, 0.85f)
        shapeRenderer.rect(100f, h - 35f, 160f, 16f)

        // HP Bar Fill
        val hpPercent = (player.health.currentHp / player.health.maxHp).coerceIn(0f, 1f)
        shapeRenderer.color = when {
            hpPercent > 0.5f -> Color.GREEN
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color.RED
        }
        shapeRenderer.rect(100f, h - 35f, 160f * hpPercent, 16f)

        // Energy Bar Background
        shapeRenderer.color = Color(0.12f, 0.15f, 0.2f, 0.85f)
        shapeRenderer.rect(100f, h - 55f, 160f, 12f)

        // Energy Bar Fill (Kuning Emas)
        shapeRenderer.color = if (player.ultimate.isReady) Color(1f, 0.85f, 0.2f, 1f) else Color(0.8f, 0.65f, 0.1f, 0.85f)
        shapeRenderer.rect(100f, h - 55f, 160f * player.ultimate.energyPercent, 12f)

        shapeRenderer.end()

        // 2. Teks HUD Informasi
        batch.begin()
        font.color = Color.WHITE
        font.draw(batch, "HP:", 20f, h - 22f)

        font.color = Color.GOLD
        font.draw(batch, "ENERGY:", 20f, h - 45f)

        // Combo Step Indicator
        val comboText = when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> "COMBO: [HIT 1]"
            ComboStep.HIT_2 -> "COMBO: [HIT 2]"
            ComboStep.HIT_3 -> "COMBO: [HIT 3 FINISHER!]"
        }
        font.color = when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> Color.YELLOW
            ComboStep.HIT_2 -> Color.ORANGE
            ComboStep.HIT_3 -> Color(1f, 0.25f, 0.25f, 1f)
        }
        font.draw(batch, comboText, 280f, h - 22f)

        // Skill & Dodge Status
        font.color = if (player.skill1.isReady) Color(0.85f, 0.45f, 1f, 1f) else Color.GRAY
        val skillStatus = if (player.skill1.isReady) "READY" else String.format("%.1fs", player.skill1.remainingCooldown)
        font.draw(batch, "Skill [U]: $skillStatus", 280f, h - 45f)

        font.color = if (player.dodgeCooldown <= 0f) Color(0.3f, 0.8f, 1f, 1f) else Color.GRAY
        val dodgeStatus = if (player.dodgeCooldown <= 0f) "READY" else String.format("%.1fs", player.dodgeCooldown)
        font.draw(batch, "Dodge [L/Shift]: $dodgeStatus", 420f, h - 45f)

        // Musuh Tersisa
        val remaining = enemies.count { !it.health.isDead }
        font.color = if (remaining == 0) Color.GREEN else Color.ORANGE
        font.draw(batch, "Sisa Musuh: $remaining / ${enemies.size}", 20f, h - 75f)

        // Overlays Game Over / Victory
        if (gameStatus == GameStatus.GAME_OVER) {
            font.color = Color.RED
            font.draw(batch, "=== GAME OVER ===", w / 2f - 75f, h / 2f + 30f)
            font.color = Color.WHITE
            font.draw(batch, "Tekan [R] untuk Respawn di Checkpoint", w / 2f - 140f, h / 2f)
        }

        if (gameStatus == GameStatus.STAGE_CLEAR) {
            font.color = Color.GOLD
            font.draw(batch, "★ STAGE CLEAR! PHASE 4 COMPLETED! ★", w / 2f - 160f, h / 2f + 40f)
            font.color = Color.WHITE
            font.draw(batch, "Semua musuh ditumpas & Gerbang Menara terbuka!", w / 2f - 180f, h / 2f + 10f)
            font.color = Color.CYAN
            font.draw(batch, "Tekan [SPACE] atau [R] untuk Main Ulang", w / 2f - 140f, h / 2f - 20f)
        }

        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        cameraManager.resize(width, height)
        virtualController.resize(width, height)
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

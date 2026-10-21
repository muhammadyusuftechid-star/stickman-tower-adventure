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
import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.world.CameraManager
import com.stickman.tower.world.Checkpoint
import com.stickman.tower.world.StageExit

/**
 * Screen gameplay utama Fase 5:
 * Mengintegrasikan Progresi RPG (Level, EXP, Stat Scaling, Koin, Level-Up Event),
 * Penyimpanan Data Lokal (Save & Load JSON/Preferences),
 * Combat Lanjutan (Combo 3-Hit, Dodge, Skills, Ultimate),
 * serta Enhanced In-Game RPG HUD.
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

    // Data Progresi RPG & Karakter Pemain
    private val saveData = SaveManager.load()
    private val stats = CharacterStats().apply { loadFromSaveData(saveData) }
    private val player = Player(spawnX = -1f, spawnY = 1.5f, stats = stats)

    private val platforms = mutableListOf<Platform>()
    private val enemies = mutableListOf<Enemy>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val hitStopManager = HitStopManager()

    // Elemen Stage Interaktif
    private val checkpoint = Checkpoint(x = 12f, y = 1f)
    private val stageExit = StageExit(x = 28f, y = 1f)
    private val activeCheckpointPos = Vector2(-1f, 1.5f)

    // Status Game Loop & Notifikasi Save
    var gameStatus: GameStatus = GameStatus.PLAYING
        private set

    private var saveNotificationTimer = 0f
    private var saveNotificationText = ""
    private var hasSavedStageClear = false
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

        hasSavedStageClear = false
        gameStatus = GameStatus.PLAYING
    }

    private fun triggerSave(reason: String) {
        val floor = if (gameStatus == GameStatus.STAGE_CLEAR) 2 else 1
        SaveManager.save(player.stats.toSaveData(highestFloor = floor))
        saveNotificationText = "Progress Tersimpan ($reason)"
        saveNotificationTimer = 2.5f
    }

    private fun grantRewards(exp: Int, gold: Int, x: Float, y: Float) {
        player.stats.addGold(gold)
        floatingTexts.add(FloatingText("+$gold G", x + 0.35f, y, Color.YELLOW, duration = 1.2f))

        val levelsGained = player.stats.addExp(exp)
        floatingTexts.add(FloatingText("+$exp EXP", x - 0.35f, y + 0.45f, Color.CYAN, duration = 1.2f))

        if (levelsGained > 0) {
            player.onLevelUp(levelsGained)
            floatingTexts.add(
                FloatingText("★ LEVEL UP! Lv.${player.stats.level} ★", player.position.x, player.position.y + 2.5f, Color.ORANGE, duration = 2.4f)
            )
            triggerSave("Level Up Lv.${player.stats.level}")
        }
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        // Perbarui Input Virtual Controller
        input.update()

        handleStatusInput()

        val safeDelta = delta.coerceAtMost(1f / 30f)
        if (saveNotificationTimer > 0f) {
            saveNotificationTimer = (saveNotificationTimer - safeDelta).coerceAtLeast(0f)
        }

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
                triggerSave("Checkpoint")
            }

            // Pembaruan Seluruh Musuh
            for (enemy in enemies) {
                enemy.update(safeDelta, player, platforms)
                // Cek reward musuh yang berhasil dieliminasi
                if (enemy.health.isDead && !enemy.rewardClaimed) {
                    enemy.rewardClaimed = true
                    grantRewards(enemy.expReward, enemy.goldReward, enemy.position.x + enemy.width / 2f, enemy.position.y + 1f)
                }
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

        // 10. Render Enhanced In-Game RPG HUD
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

                            // Floating Damage & Critical Text
                            val critPrefix = if (player.hitbox.isCritical) "CRIT " else "-"
                            val textColor = when {
                                player.hitbox.isCritical -> Color.GOLD
                                player.isUlting -> Color.GOLD
                                player.isSkillStriking -> Color(0.9f, 0.4f, 1f, 1f)
                                player.comboManager.currentStep == ComboStep.HIT_3 -> Color.ORANGE
                                else -> Color(1f, 0.25f, 0.25f, 1f)
                            }

                            floatingTexts.add(
                                FloatingText(
                                    "$critPrefix${player.hitbox.damage.toInt()}",
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
            if (!hasSavedStageClear) {
                hasSavedStageClear = true
                grantRewards(100, 50, stageExit.x + 0.8f, stageExit.y + 1.5f)
                triggerSave("Stage Clear Floor 1")
            }
        }
    }

    private fun handleStatusInput() {
        if (gameStatus == GameStatus.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Keys.R)) {
                player.position.set(activeCheckpointPos.x, activeCheckpointPos.y)
                player.velocity.set(0f, 0f)
                player.resetToStats()
                gameStatus = GameStatus.PLAYING
            }
        } else if (gameStatus == GameStatus.STAGE_CLEAR) {
            if (Gdx.input.isKeyJustPressed(Keys.R) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                player.position.set(-1f, 1.5f)
                player.velocity.set(0f, 0f)
                player.resetToStats()
                activeCheckpointPos.set(-1f, 1.5f)
                setupStage()
            }
        }
    }

    private fun renderHUD() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        // 1. Render Elemen Latar Bar & Kartu Karakter (ShapeRenderer)
        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, w, h)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Panel Latar Player Status Card (Kiri Atas)
        shapeRenderer.color = Color(0.08f, 0.11f, 0.16f, 0.90f)
        shapeRenderer.rect(12f, h - 90f, 290f, 78f)

        // Level Badge Box
        shapeRenderer.color = Color(0.15f, 0.22f, 0.32f, 1f)
        shapeRenderer.rect(18f, h - 84f, 55f, 66f)

        // HP Bar Background
        shapeRenderer.color = Color(0.12f, 0.15f, 0.2f, 1f)
        shapeRenderer.rect(80f, h - 35f, 210f, 16f)

        // HP Bar Fill
        val hpPercent = (player.health.currentHp / player.health.maxHp).coerceIn(0f, 1f)
        shapeRenderer.color = when {
            hpPercent > 0.5f -> Color(0.18f, 0.85f, 0.35f, 1f)
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color(0.95f, 0.25f, 0.25f, 1f)
        }
        shapeRenderer.rect(80f, h - 35f, 210f * hpPercent, 16f)

        // EXP Bar Background
        shapeRenderer.color = Color(0.12f, 0.15f, 0.2f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f, 12f)

        // EXP Bar Fill (Cyan Elegan)
        val expPercent = (player.stats.currentExp.toFloat() / player.stats.requiredExp.toFloat()).coerceIn(0f, 1f)
        shapeRenderer.color = Color(0.15f, 0.75f, 0.95f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f * expPercent, 12f)

        // Ultimate Energy Bar Background
        shapeRenderer.color = Color(0.12f, 0.15f, 0.2f, 1f)
        shapeRenderer.rect(80f, h - 69f, 210f, 10f)

        // Ultimate Energy Bar Fill (Kuning Emas)
        shapeRenderer.color = if (player.ultimate.isReady) Color(1f, 0.85f, 0.2f, 1f) else Color(0.85f, 0.65f, 0.15f, 0.85f)
        shapeRenderer.rect(80f, h - 69f, 210f * player.ultimate.energyPercent, 10f)

        // Save Notification Pill (Jika Aktif)
        if (saveNotificationTimer > 0f) {
            shapeRenderer.color = Color(0.12f, 0.55f, 0.28f, 0.90f)
            shapeRenderer.rect(w / 2f - 110f, h - 38f, 220f, 26f)
        }

        shapeRenderer.end()

        // 2. Render Teks HUD Informasi
        batch.begin()

        // Level Badge Text
        font.color = Color(0.7f, 0.85f, 1f, 1f)
        font.draw(batch, "LVL", 32f, h - 28f)
        font.color = Color.WHITE
        font.draw(batch, "${player.stats.level}", 36f, h - 50f)

        // HP Overlay Text
        font.color = Color.WHITE
        font.draw(batch, "HP: ${player.health.currentHp.toInt()} / ${player.health.maxHp.toInt()}", 86f, h - 22f)

        // EXP Overlay Text
        font.color = Color(0.85f, 0.95f, 1f, 1f)
        val expPctText = (expPercent * 100).toInt()
        font.draw(batch, "EXP: ${player.stats.currentExp}/${player.stats.requiredExp} ($expPctText%)", 86f, h - 42f)

        // Ultimate Text
        font.color = if (player.ultimate.isReady) Color.GOLD else Color.WHITE
        val ultStatus = if (player.ultimate.isReady) "READY [O]" else "${(player.ultimate.energyPercent * 100).toInt()}%"
        font.draw(batch, "ULT: $ultStatus", 86f, h - 58f)

        // Stats Bar & Coins (Di bawah Player Card)
        font.color = Color.GOLD
        font.draw(batch, "[G] ${player.stats.gold} Coins", 15f, h - 100f)
        font.color = Color(0.8f, 0.85f, 0.9f, 1f)
        font.draw(batch, "ATK: ${player.stats.attackPower.toInt()} | DEF: ${player.stats.defense.toInt()} | CRIT: ${(player.stats.critRate * 100).toInt()}%", 125f, h - 100f)

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
        font.draw(batch, comboText, 320f, h - 22f)

        // Skill & Dodge Status
        font.color = if (player.skill1.isReady) Color(0.85f, 0.45f, 1f, 1f) else Color.GRAY
        val skillStatus = if (player.skill1.isReady) "READY" else String.format("%.1fs", player.skill1.remainingCooldown)
        font.draw(batch, "Skill [U]: $skillStatus", 320f, h - 45f)

        font.color = if (player.dodgeCooldown <= 0f) Color(0.3f, 0.8f, 1f, 1f) else Color.GRAY
        val dodgeStatus = if (player.dodgeCooldown <= 0f) "READY" else String.format("%.1fs", player.dodgeCooldown)
        font.draw(batch, "Dodge [L/Shift]: $dodgeStatus", 450f, h - 45f)

        // Musuh Tersisa
        val remaining = enemies.count { !it.health.isDead }
        font.color = if (remaining == 0) Color.GREEN else Color.ORANGE
        font.draw(batch, "Sisa Musuh: $remaining / ${enemies.size}", 320f, h - 70f)

        // Notifikasi Auto-Save
        if (saveNotificationTimer > 0f) {
            font.color = Color.WHITE
            font.draw(batch, "[OK] $saveNotificationText", w / 2f - 95f, h - 21f)
        }

        // Overlays Game Over / Victory
        if (gameStatus == GameStatus.GAME_OVER) {
            font.color = Color.RED
            font.draw(batch, "=== GAME OVER ===", w / 2f - 75f, h / 2f + 30f)
            font.color = Color.WHITE
            font.draw(batch, "Tekan [R] untuk Respawn di Checkpoint", w / 2f - 140f, h / 2f)
        }

        if (gameStatus == GameStatus.STAGE_CLEAR) {
            font.color = Color.GOLD
            font.draw(batch, "★ STAGE CLEAR! PHASE 5 RPG PROGRESSION COMPLETED! ★", w / 2f - 210f, h / 2f + 40f)
            font.color = Color.WHITE
            font.draw(batch, "Player Lv.${player.stats.level} | Total Gold: ${player.stats.gold} G", w / 2f - 110f, h / 2f + 12f)
            font.color = Color.CYAN
            font.draw(batch, "Tekan [SPACE] atau [R] untuk Main Ulang & Naik Level", w / 2f - 170f, h / 2f - 18f)
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

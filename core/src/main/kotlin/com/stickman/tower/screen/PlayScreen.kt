package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.character.CharacterArchetype
import com.stickman.tower.character.Player
import com.stickman.tower.combat.ComboStep
import com.stickman.tower.combat.FloatingText
import com.stickman.tower.combat.HitStopManager
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.input.CompositeInput
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.input.VirtualController
import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.ui.FontManager
import com.stickman.tower.vfx.ParticleManager
import com.stickman.tower.world.CameraManager
import com.stickman.tower.world.TowerFloor
import com.stickman.tower.world.TowerLevelBuilder
import kotlin.math.sin

/**
 * Screen gameplay utama:
 * Memisahkan World Camera (16x9m physics) dan Screen UI Viewport (1280x720px)
 * untuk menjamin HUD dan Virtual Controller terkunci pixel-perfect di segala resolusi.
 */
class PlayScreen(private val game: StickmanGame? = null) : Screen {
    private val cameraManager = CameraManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()

    // Dedicated UI Viewport Pass (Pixel-Perfect 1280x720 Screen Space)
    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(1280f, 720f, uiCamera)
    private val uiTouchVec = Vector3()

    // Dual Input Layer: Keyboard Desktop + Virtual Controller Mobile Touch
    private val keyboardInput = KeyboardInput()
    val virtualController = VirtualController()
    private val input = CompositeInput(keyboardInput, virtualController)

    // Data Progresi RPG & Karakter Pemain (Sesuai Arketipe Tersimpan)
    private val saveData = SaveManager.load()
    private val stats = CharacterStats().apply { loadFromSaveData(saveData) }
    private val archetype = CharacterArchetype.fromString(saveData.selectedArchetype)
    private val player = Player(spawnX = -1f, spawnY = 1.5f, stats = stats, archetype = archetype)

    // Manajer Partikel VFX
    val particleManager = ParticleManager()

    // Sistem Pause Menu
    var isPaused: Boolean = false
    private var pauseSelectedIndex: Int = 0
    private val pauseMenuItems = listOf("RESUME", "RESTART FLOOR", "AUDIO: ON", "MAIN MENU")

    // Data Lantai Menara Aktif
    var currentFloorNumber: Int = 1
        private set
    private lateinit var currentFloor: TowerFloor

    private val floatingTexts = mutableListOf<FloatingText>()
    private val hitStopManager = HitStopManager()
    private val activeCheckpointPos = Vector2(-1f, 1.5f)

    // Status Game Loop & Notifikasi Save
    var gameStatus: GameStatus = GameStatus.PLAYING
        private set

    private var saveNotificationTimer = 0f
    private var saveNotificationText = ""
    private var hasSavedStageClear = false
    private var showDebugBounds = false

    // Ghost Health Bar (Lag Bar untuk visual impact saat terkena damage)
    private var playerGhostHp: Float = player.health.currentHp
    private var playerGhostDelay: Float = 0f
    private var bossGhostHp: Float = 0f
    private var bossGhostDelay: Float = 0f

    // Micro-animasi HUD Juice
    private var prevComboStep: ComboStep = ComboStep.HIT_1
    private var comboPopTimer: Float = 0f
    private var hudAnimTime: Float = 0f

    override fun show() {
        Gdx.input?.setCatchKey(Keys.BACK, true)
        AudioManager.init()
        FontManager.init()
        if (Gdx.graphics != null && Gdx.graphics.width > 0 && Gdx.graphics.height > 0) {
            uiViewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        }
        virtualController.resize(1280, 720)

        // Hubungkan Callback Partikel VFX Pemain
        player.onLand = { x, y -> particleManager.spawnDust(x, y, count = 6) }
        player.onJump = { x, y -> particleManager.spawnDust(x, y, count = 5) }
        player.onDodge = { x, y -> particleManager.spawnDust(x, y, count = 8) }

        loadFloor(1)
    }

    /**
     * Memuat layout lantai menara tertentu (1, 2, atau 3).
     */
    fun loadFloor(floorNumber: Int) {
        currentFloorNumber = floorNumber.coerceIn(1, 3)
        currentFloor = TowerLevelBuilder.buildFloor(currentFloorNumber)

        cameraManager.setBounds(
            left = currentFloor.mapBounds.x,
            bottom = currentFloor.mapBounds.y,
            right = currentFloor.mapBounds.x + currentFloor.mapBounds.width,
            top = currentFloor.mapBounds.height
        )

        player.position.set(currentFloor.spawnX, currentFloor.spawnY)
        player.velocity.set(0f, 0f)
        activeCheckpointPos.set(currentFloor.spawnX, currentFloor.spawnY)

        hasSavedStageClear = false
        gameStatus = GameStatus.PLAYING

        playerGhostHp = player.health.currentHp
        playerGhostDelay = 0f
        bossGhostHp = currentFloor.boss?.health?.currentHp ?: 0f
        bossGhostDelay = 0f
        prevComboStep = ComboStep.HIT_1
        comboPopTimer = 0f

        floatingTexts.clear()
        particleManager.clear()
        floatingTexts.add(
            FloatingText("=== ${currentFloor.floorName} ===", player.position.x, player.position.y + 2.5f, Color.GOLD, duration = 2.8f)
        )
    }

    private fun triggerSave(reason: String, highestFloor: Int = currentFloorNumber) {
        val saveFloor = kotlin.math.max(saveData.highestTowerFloor, highestFloor)
        SaveManager.save(player.stats.toSaveData(highestFloor = saveFloor, selectedArchetype = archetype.name))
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
            AudioManager.playLevelUp()
            particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height, count = 25, color = Color.CYAN)
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

        // Toggle Pause dengan ESC, P, atau Tombol Android BACK (Hanya saat status PLAYING atau sudah PAUSED)
        val isBackTriggered = Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
                              Gdx.input.isKeyJustPressed(Keys.P) ||
                              Gdx.input.isKeyJustPressed(Keys.BACK)

        if (isBackTriggered && (gameStatus == GameStatus.PLAYING || isPaused)) {
            isPaused = !isPaused
            pauseSelectedIndex = 0
            if (isPaused) AudioManager.playDodge() else AudioManager.playSlashLight()
        }

        // Cek klik tombol Pause di pojok kanan atas HUD
        if (Gdx.input.justTouched() && (gameStatus == GameStatus.PLAYING || isPaused)) {
            uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            uiViewport.unproject(uiTouchVec)
            if (uiTouchVec.x in (1280f - 110f)..1280f && uiTouchVec.y in (720f - 45f)..720f) {
                isPaused = !isPaused
                pauseSelectedIndex = 0
                if (isPaused) AudioManager.playDodge() else AudioManager.playSlashLight()
            }
        }

        val safeDelta = delta.coerceAtMost(1f / 30f)

        if (isPaused) {
            handlePauseInput()
            renderWorld(0f)
            uiViewport.apply()
            shapeRenderer.projectionMatrix = uiCamera.combined
            batch.projectionMatrix = uiCamera.combined
            renderHUD()
            renderPauseOverlay()
            return
        }

        // Perbarui Input Virtual Controller dengan uiViewport
        input.update(uiViewport)
        handleStatusInput()

        hudAnimTime += safeDelta

        // Pembaruan Ghost HP Bar Pemain (Smooth Decay Lag)
        if (playerGhostDelay > 0f) {
            playerGhostDelay = (playerGhostDelay - safeDelta).coerceAtLeast(0f)
        } else if (playerGhostHp > player.health.currentHp) {
            val decaySpeed = ((playerGhostHp - player.health.currentHp) * 4.5f + 25f) * safeDelta
            playerGhostHp = (playerGhostHp - decaySpeed).coerceAtLeast(player.health.currentHp)
        } else {
            playerGhostHp = player.health.currentHp
        }

        // Pembaruan Ghost HP Bar Boss
        val activeBoss = currentFloor.boss
        if (activeBoss != null) {
            if (bossGhostHp == 0f || bossGhostHp < activeBoss.health.currentHp) {
                bossGhostHp = activeBoss.health.currentHp
            }
            if (bossGhostDelay > 0f) {
                bossGhostDelay = (bossGhostDelay - safeDelta).coerceAtLeast(0f)
            } else if (bossGhostHp > activeBoss.health.currentHp) {
                val decaySpeed = ((bossGhostHp - activeBoss.health.currentHp) * 4f + 35f) * safeDelta
                bossGhostHp = (bossGhostHp - decaySpeed).coerceAtLeast(activeBoss.health.currentHp)
            } else {
                bossGhostHp = activeBoss.health.currentHp
            }
        }

        // Pembaruan Animasi Combo Pop
        if (player.comboManager.currentStep != prevComboStep) {
            prevComboStep = player.comboManager.currentStep
            comboPopTimer = 0.28f
        }
        if (comboPopTimer > 0f) {
            comboPopTimer = (comboPopTimer - safeDelta).coerceAtLeast(0f)
        }

        if (saveNotificationTimer > 0f) {
            saveNotificationTimer = (saveNotificationTimer - safeDelta).coerceAtLeast(0f)
        }

        // 1. Hit Stop Micro-Freeze Update
        val isFrozen = hitStopManager.update(safeDelta)

        if (!isFrozen && gameStatus == GameStatus.PLAYING) {
            // Pembaruan Pemain
            player.update(safeDelta, input, currentFloor.platforms)
            particleManager.update(safeDelta)

            // Cek Rintangan Duri Lantai (Spikes)
            for (spike in currentFloor.spikes) {
                if (spike.checkCollision(player.bounds) && !player.health.isInvulnerable && !player.isDodging) {
                    if (player.takeHit(spike.damage, 0f, 8.0f)) {
                        playerGhostDelay = 0.35f
                        hitStopManager.trigger(0.06f)
                        AudioManager.playHit()
                        particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 8, color = Color.RED)
                        floatingTexts.add(
                            FloatingText("-${spike.damage.toInt()}", player.position.x + player.width / 2f, player.position.y + player.height + 0.3f, Color.RED)
                        )
                    }
                }
            }

            // Cek Interaksi Peti Harta (Chests)
            for (chest in currentFloor.chests) {
                if (!chest.isOpened) {
                    val isSwordHit = player.hitbox.isActive && player.hitbox.bounds.overlaps(chest.bounds)
                    val isBodyTouch = player.bounds.overlaps(chest.bounds)
                    if (isSwordHit || isBodyTouch) {
                        if (chest.open()) {
                            player.stats.addGold(chest.goldReward)
                            player.health.heal(chest.healReward)
                            AudioManager.playChest()
                            particleManager.spawnHitSparks(chest.x + chest.width / 2f, chest.y + chest.height / 2f, count = 22, color = Color.GOLD)
                            floatingTexts.add(FloatingText("+${chest.goldReward} G", chest.x + 0.2f, chest.y + 1.2f, Color.YELLOW, duration = 1.4f))
                            floatingTexts.add(FloatingText("+${chest.healReward.toInt()} HP", chest.x + 0.2f, chest.y + 1.7f, Color.GREEN, duration = 1.4f))
                            triggerSave("Peti Terbuka")
                        }
                    }
                }
            }

            // Cek Trigger Checkpoint
            if (currentFloor.checkpoint != null && currentFloor.checkpoint!!.checkTrigger(player.bounds)) {
                activeCheckpointPos.set(currentFloor.checkpoint!!.x, currentFloor.checkpoint!!.y + 0.2f)
                floatingTexts.add(
                    FloatingText("CHECKPOINT!", currentFloor.checkpoint!!.x, currentFloor.checkpoint!!.y + 2.2f, Color.CYAN, duration = 1.5f)
                )
                triggerSave("Checkpoint Lantai $currentFloorNumber")
            }

            // Pembaruan Musuh Biasa
            for (enemy in currentFloor.enemies) {
                enemy.update(safeDelta, player, currentFloor.platforms)
                if (enemy.health.isDead && !enemy.rewardClaimed) {
                    enemy.rewardClaimed = true
                    AudioManager.playDeath()
                    particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 16, color = Color.RED)
                    grantRewards(enemy.expReward, enemy.goldReward, enemy.position.x + enemy.width / 2f, enemy.position.y + 1f)
                }
            }

            // Pembaruan Boss (Jika Ada)
            val boss = currentFloor.boss
            if (boss != null) {
                boss.update(safeDelta, player, currentFloor.platforms)
                if (boss.health.isDead && !boss.rewardClaimed) {
                    boss.rewardClaimed = true
                    AudioManager.playDeath()
                    particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 35, color = Color.GOLD)
                    grantRewards(boss.expReward, boss.goldReward, boss.position.x + boss.width / 2f, boss.position.y + 1.5f)
                    floatingTexts.add(
                        FloatingText("★ BOSS DEFEATED! ★", boss.position.x, boss.position.y + 3.2f, Color.GOLD, duration = 3.5f)
                    )
                }
            }

            // 2. Deteksi Pertarungan Dua Arah (Pemain <-> Musuh / Boss)
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

        // 5. Render Objek Dunia 2D & Partikel (World Space: Meters)
        renderWorld(safeDelta)

        // 6. UI Viewport Pass (Pixel-Perfect 1280x720 Screen Space)
        uiViewport.apply()
        shapeRenderer.projectionMatrix = uiCamera.combined
        batch.projectionMatrix = uiCamera.combined

        // 7. Render Virtual Controller (On-Screen Touch Buttons untuk Android/Desktop)
        virtualController.render(
            shapeRenderer,
            batch,
            if (FontManager.isInitialized) FontManager.smallFont else font,
            player.skill1,
            player.ultimate
        )

        // 8. Render Enhanced In-Game RPG HUD
        renderHUD()
    }

    private fun renderWorld(delta: Float) {
        val playerCenterX = player.position.x + player.width / 2f
        val playerCenterY = player.position.y + player.height / 2f + 0.5f
        cameraManager.update(playerCenterX, playerCenterY, delta)

        val clearR = if (currentFloor.isBossFloor) 0.12f else 0.08f
        val clearG = if (currentFloor.isBossFloor) 0.08f else 0.09f
        val clearB = if (currentFloor.isBossFloor) 0.12f else 0.13f
        ScreenUtils.clear(clearR, clearG, clearB, 1f)

        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (platform in currentFloor.platforms) platform.render(shapeRenderer)
        for (spike in currentFloor.spikes) spike.render(shapeRenderer)
        for (chest in currentFloor.chests) chest.render(shapeRenderer)
        currentFloor.checkpoint?.render(shapeRenderer)
        currentFloor.exitPortal.render(shapeRenderer)

        for (enemy in currentFloor.enemies) enemy.render(shapeRenderer)
        currentFloor.boss?.render(shapeRenderer)
        player.render(shapeRenderer)

        // Render Efek Partikel VFX
        particleManager.render(shapeRenderer)
        shapeRenderer.end()

        // Debug Bounding Boxes
        if (showDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            player.renderDebug(shapeRenderer)
            for (enemy in currentFloor.enemies) enemy.renderDebug(shapeRenderer)
            currentFloor.boss?.renderDebug(shapeRenderer)
            for (platform in currentFloor.platforms) {
                shapeRenderer.color = if (platform.isOneWay) Color.CYAN else Color.YELLOW
                shapeRenderer.rect(platform.bounds.x, platform.bounds.y, platform.bounds.width, platform.bounds.height)
            }
            shapeRenderer.end()
        }

        if (floatingTexts.isNotEmpty()) {
            uiViewport.apply()
            batch.projectionMatrix = uiCamera.combined
            batch.begin()
            val dmgFont = if (FontManager.isInitialized) FontManager.regularFont else font
            for (ft in floatingTexts) {
                ft.render(batch, dmgFont, cameraManager.camera)
            }
            batch.end()
        }
    }

    private fun handleTwoWayCombat() {
        val boss = currentFloor.boss

        // A. Serangan Pemain Mengenai Musuh Biasa
        if (player.hitbox.isActive) {
            for (enemy in currentFloor.enemies) {
                if (!enemy.health.isDead && !player.hitbox.hitEntities.contains(enemy)) {
                    if (player.hitbox.bounds.overlaps(enemy.hurtbox.bounds)) {
                        player.hitbox.hitEntities.add(enemy)

                        val knockX = if (player.facingRight) player.hitbox.knockbackX else -player.hitbox.knockbackX
                        val knockY = player.hitbox.knockbackY

                        if (enemy.takeHit(player.hitbox.damage, knockX, knockY)) {
                            hitStopManager.trigger(0.06f)
                            AudioManager.playHit()
                            particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 10, color = if (player.hitbox.isCritical) Color.GOLD else Color.WHITE)

                            val energyGain = when {
                                player.isUlting -> 0f
                                player.comboManager.currentStep == ComboStep.HIT_3 -> 15f
                                player.isSkillStriking -> 12f
                                else -> 8f
                            }
                            player.ultimate.addEnergy(energyGain)

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
                                    textColor,
                                    isCritical = player.hitbox.isCritical
                                )
                            )
                        }
                    }
                }
            }

            // Serangan Pemain Mengenai Boss
            if (boss != null && !boss.health.isDead && !player.hitbox.hitEntities.contains(boss)) {
                if (player.hitbox.bounds.overlaps(boss.hurtbox.bounds)) {
                    player.hitbox.hitEntities.add(boss)

                    val knockX = if (player.facingRight) player.hitbox.knockbackX else -player.hitbox.knockbackX
                    val knockY = player.hitbox.knockbackY

                    if (boss.takeHit(player.hitbox.damage, knockX, knockY)) {
                        bossGhostDelay = 0.35f
                        hitStopManager.trigger(0.06f)
                        AudioManager.playHit()
                        particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 14, color = Color.ORANGE)

                        val energyGain = when {
                            player.isUlting -> 0f
                            player.comboManager.currentStep == ComboStep.HIT_3 -> 18f
                            player.isSkillStriking -> 15f
                            else -> 10f
                        }
                        player.ultimate.addEnergy(energyGain)

                        val critPrefix = if (player.hitbox.isCritical) "CRIT " else "-"
                        val textColor = if (player.hitbox.isCritical) Color.GOLD else Color.RED
                        floatingTexts.add(
                            FloatingText(
                                "$critPrefix${player.hitbox.damage.toInt()}",
                                boss.position.x + boss.width / 2f,
                                boss.position.y + boss.height + 0.45f,
                                textColor,
                                isCritical = player.hitbox.isCritical
                            )
                        )
                    }
                }
            }
        }

        // B. Serangan Musuh Biasa Mengenai Pemain
        for (enemy in currentFloor.enemies) {
            if (enemy.hitbox.isActive && !enemy.hitbox.hitEntities.contains(player) && !player.health.isDead) {
                if (enemy.hitbox.bounds.overlaps(player.hurtbox.bounds)) {
                    enemy.hitbox.hitEntities.add(player)

                    val knockX = if (enemy.facingRight) enemy.hitbox.knockbackX else -enemy.hitbox.knockbackX
                    val knockY = enemy.hitbox.knockbackY

                    if (player.takeHit(enemy.hitbox.damage, knockX, knockY)) {
                        playerGhostDelay = 0.35f
                        hitStopManager.trigger(0.08f)
                        AudioManager.playHit()
                        particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 10, color = Color.RED)
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

        // C. Serangan Melee Boss Mengenai Pemain
        if (boss != null && boss.hitbox.isActive && !boss.hitbox.hitEntities.contains(player) && !player.health.isDead) {
            if (boss.hitbox.bounds.overlaps(player.hurtbox.bounds)) {
                boss.hitbox.hitEntities.add(player)

                val knockX = if (boss.facingRight) boss.hitbox.knockbackX else -boss.hitbox.knockbackX
                val knockY = boss.hitbox.knockbackY

                if (player.takeHit(boss.hitbox.damage, knockX, knockY)) {
                    playerGhostDelay = 0.35f
                    hitStopManager.trigger(0.09f)
                    AudioManager.playHit()
                    particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 12, color = Color.RED)
                    floatingTexts.add(
                        FloatingText(
                            "-${boss.hitbox.damage.toInt()}",
                            player.position.x + player.width / 2f,
                            player.position.y + player.height + 0.35f,
                            Color(1f, 0.25f, 0.25f, 1f)
                        )
                    )
                }
            }
        }

        // D. Proyektil Wind Slash Boss Mengenai Pemain
        if (boss != null) {
            for (proj in boss.projectiles) {
                if (!proj.isDestroyed && proj.bounds.overlaps(player.hurtbox.bounds) && !player.health.isDead) {
                    proj.isDestroyed = true
                    val knockX = if (proj.velocityX > 0f) 6.5f else -6.5f
                    if (player.takeHit(proj.damage, knockX, 3.5f)) {
                        playerGhostDelay = 0.35f
                        hitStopManager.trigger(0.06f)
                        AudioManager.playHit()
                        particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 10, color = Color.RED)
                        floatingTexts.add(
                            FloatingText(
                                "-${proj.damage.toInt()}",
                                player.position.x + player.width / 2f,
                                player.position.y + player.height + 0.35f,
                                Color.RED
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

        val isFloorCleared = if (currentFloor.isBossFloor) {
            currentFloor.boss?.health?.isDead == true
        } else {
            currentFloor.enemies.all { it.health.isDead }
        }

        if (isFloorCleared && currentFloor.exitPortal.bounds.overlaps(player.bounds) && gameStatus == GameStatus.PLAYING) {
            gameStatus = GameStatus.STAGE_CLEAR
            floatingTexts.add(
                FloatingText("VICTORY!", currentFloor.exitPortal.x + 0.8f, currentFloor.exitPortal.y + 3.2f, Color.GOLD, duration = 2.5f)
            )
            if (!hasSavedStageClear) {
                hasSavedStageClear = true
                val nextFloorNum = (currentFloorNumber + 1).coerceAtMost(3)
                val expReward = if (currentFloor.isBossFloor) 250 else 100
                val goldReward = if (currentFloor.isBossFloor) 150 else 60
                grantRewards(expReward, goldReward, currentFloor.exitPortal.x + 0.8f, currentFloor.exitPortal.y + 1.5f)
                triggerSave("Lantai $currentFloorNumber Selesai", highestFloor = nextFloorNum)
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
            if (currentFloorNumber < 3) {
                // Maju ke Lantai Selanjutnya
                if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                    loadFloor(currentFloorNumber + 1)
                } else if (Gdx.input.isKeyJustPressed(Keys.R)) {
                    // Main Ulang Lantai Ini
                    loadFloor(currentFloorNumber)
                }
            } else {
                // Lantai 3 Selesai (Menara Ditaklukkan) -> Replay dari Lantai 1
                if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.R)) {
                    loadFloor(1)
                }
            }
        }
    }

    private fun renderHUD() {
        val w = 1280f
        val h = 720f

        val hpPercent = (player.health.currentHp / player.health.maxHp).coerceIn(0f, 1f)
        val ghostHpPercent = (playerGhostHp / player.health.maxHp).coerceIn(0f, 1f)
        val isLowHp = hpPercent <= 0.25f
        val dangerThrob = if (isLowHp) ((Math.sin(hudAnimTime.toDouble() * 9.0).toFloat() + 1f) * 0.5f) else 0f

        // 1. Render Elemen Latar Bar & Kartu Karakter (ShapeRenderer pada Screen Space 1280x720)
        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Panel Latar Player Status Card (Kiri Atas - Obsidian Glassmorphism)
        shapeRenderer.color = Color(0.06f + 0.08f * dangerThrob, 0.09f * (1f - dangerThrob), 0.15f * (1f - dangerThrob), 0.94f)
        shapeRenderer.rect(12f, h - 90f, 290f, 78f)

        // Top Accent Line (Red danger pulse or Sleek Blue/Gold)
        shapeRenderer.color = if (isLowHp) Color(1f, 0.2f, 0.2f, 0.7f + 0.3f * dangerThrob) else Color(0.22f, 0.38f, 0.60f, 0.75f)
        shapeRenderer.rectLine(12f, h - 12f, 302f, h - 12f, 2f)

        // Level Badge Box
        shapeRenderer.color = Color(0.10f, 0.15f, 0.24f, 1f)
        shapeRenderer.rect(18f, h - 84f, 55f, 66f)
        shapeRenderer.color = Color(0.85f, 0.70f, 0.25f, 0.85f)
        shapeRenderer.rectLine(18f, h - 84f, 73f, h - 84f, 2f)

        // HP Bar Trough (Dark Obsidian)
        shapeRenderer.color = Color(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 35f, 210f, 16f)

        // HP Amber Ghost Catch-Up Bar
        if (ghostHpPercent > hpPercent) {
            shapeRenderer.color = Color(0.95f, 0.62f, 0.15f, 0.90f)
            shapeRenderer.rect(80f, h - 35f, 210f * ghostHpPercent, 16f)
        }

        // HP Main Bar Fill (Emerald -> Gold -> Pulsing Crimson Danger)
        val hpBarColor = when {
            hpPercent > 0.5f -> Color(0.18f, 0.85f, 0.38f, 1f)
            hpPercent > 0.25f -> Color(0.95f, 0.75f, 0.15f, 1f)
            else -> {
                val throb = (Math.sin(hudAnimTime.toDouble() * 10.0).toFloat() + 1f) * 0.5f
                Color(0.88f + 0.12f * throb, 0.15f * (1f - throb), 0.15f * (1f - throb), 1f)
            }
        }
        shapeRenderer.color = hpBarColor
        shapeRenderer.rect(80f, h - 35f, 210f * hpPercent, 16f)

        // HP Bar Highlight Rim
        shapeRenderer.color = Color(1f, 1f, 1f, 0.22f)
        shapeRenderer.rectLine(80f, h - 20f, 290f, h - 20f, 1f)

        // EXP Bar Background & Fill
        shapeRenderer.color = Color(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f, 12f)

        val expPercent = (player.stats.currentExp.toFloat() / player.stats.requiredExp.toFloat()).coerceIn(0f, 1f)
        shapeRenderer.color = Color(0.15f, 0.75f, 0.95f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f * expPercent, 12f)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.20f)
        shapeRenderer.rectLine(80f, h - 42f, 290f, h - 42f, 1f)

        // Ultimate Energy Bar Background & Fill
        shapeRenderer.color = Color(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 69f, 210f, 10f)

        val ultColor = if (player.ultimate.isReady) {
            val ultPulse = (Math.sin(hudAnimTime.toDouble() * 7.0).toFloat() + 1f) * 0.5f
            Color(1f, 0.85f + 0.15f * ultPulse, 0.2f, 1f)
        } else {
            Color(0.85f, 0.65f, 0.15f, 0.85f)
        }
        shapeRenderer.color = ultColor
        shapeRenderer.rect(80f, h - 69f, 210f * player.ultimate.energyPercent, 10f)

        // Save Notification Pill (Jika Aktif)
        if (saveNotificationTimer > 0f) {
            shapeRenderer.color = Color(0.12f, 0.55f, 0.28f, 0.90f)
            shapeRenderer.rect(w / 2f - 110f, h - 38f, 220f, 26f)
        }

        // Tombol Pause (Pojok Kanan Atas)
        shapeRenderer.color = Color(0.12f, 0.16f, 0.25f, 0.85f)
        shapeRenderer.rect(w - 110f, h - 45f, 98f, 32f)
        shapeRenderer.color = Color(0.9f, 0.75f, 0.25f, 0.85f)
        shapeRenderer.rectLine(w - 110f, h - 45f, w - 12f, h - 45f, 2f)

        // Bar HP Boss Raksasa dengan Amber Ghost Bar & Phase 2 Threshold Marker
        val boss = currentFloor.boss
        if (boss != null && !boss.health.isDead) {
            val bossBarW = 440f
            val bossBarH = 22f
            val bossBarX = w / 2f - bossBarW / 2f
            val bossBarY = h - 36f

            // Frame Gelap Ornate
            shapeRenderer.color = Color(0.05f, 0.06f, 0.09f, 0.96f)
            shapeRenderer.rect(bossBarX - 5f, bossBarY - 5f, bossBarW + 10f, bossBarH + 10f)

            // Outer border line
            shapeRenderer.color = if (boss.isPhase2) Color(0.95f, 0.35f, 0.15f, 0.9f) else Color(0.80f, 0.68f, 0.30f, 0.85f)
            shapeRenderer.rectLine(bossBarX - 5f, bossBarY + bossBarH + 5f, bossBarX + bossBarW + 5f, bossBarY + bossBarH + 5f, 2f)
            shapeRenderer.rectLine(bossBarX - 5f, bossBarY - 5f, bossBarX + bossBarW + 5f, bossBarY - 5f, 1.5f)

            // Trough
            shapeRenderer.color = Color(0.08f, 0.08f, 0.12f, 1f)
            shapeRenderer.rect(bossBarX, bossBarY, bossBarW, bossBarH)

            // Boss Ghost Catch-Up Bar (Amber)
            val bossHpPct = (boss.health.currentHp / boss.health.maxHp).coerceIn(0f, 1f)
            val bossGhostPct = (bossGhostHp / boss.health.maxHp).coerceIn(0f, 1f)
            if (bossGhostPct > bossHpPct) {
                shapeRenderer.color = Color(0.95f, 0.62f, 0.15f, 0.90f)
                shapeRenderer.rect(bossBarX, bossBarY, bossBarW * bossGhostPct, bossBarH)
            }

            // Boss Main Fill Bar (Crimson ke Api Fasa 2)
            val bossFillColor = if (boss.isPhase2) {
                val flame = (Math.sin(hudAnimTime.toDouble() * 9.0).toFloat() + 1f) * 0.5f
                Color(1f, 0.22f + 0.18f * flame, 0.1f, 1f)
            } else {
                Color(0.85f, 0.15f, 0.24f, 1f)
            }
            shapeRenderer.color = bossFillColor
            shapeRenderer.rect(bossBarX, bossBarY, bossBarW * bossHpPct, bossBarH)

            // Top subtle highlight line
            shapeRenderer.color = Color(1f, 1f, 1f, 0.25f)
            shapeRenderer.rectLine(bossBarX, bossBarY + bossBarH - 2f, bossBarX + bossBarW * bossHpPct, bossBarY + bossBarH - 2f, 1.5f)

            // Phase 2 Threshold Marker (50% HP Indicator Divider)
            val markerX = bossBarX + bossBarW * 0.5f
            shapeRenderer.color = Color(1f, 0.88f, 0.35f, 0.95f)
            shapeRenderer.rectLine(markerX, bossBarY - 4f, markerX, bossBarY + bossBarH + 4f, 2.5f)
            shapeRenderer.triangle(
                markerX - 4f, bossBarY + bossBarH + 5f,
                markerX + 4f, bossBarY + bossBarH + 5f,
                markerX, bossBarY + bossBarH + 10f
            )
        }

        shapeRenderer.end()

        // 2. Render Teks HUD Informasi
        batch.projectionMatrix = uiCamera.combined
        batch.begin()

        // Level Badge Text
        val smallF = if (FontManager.isInitialized) FontManager.smallFont else font
        val regF = if (FontManager.isInitialized) FontManager.regularFont else font
        val headF = if (FontManager.isInitialized) FontManager.headerFont else font

        smallF.color = Color(0.7f, 0.85f, 1f, 1f)
        smallF.draw(batch, "LVL", 32f, h - 28f)
        headF.color = Color.WHITE
        headF.draw(batch, "${player.stats.level}", 34f, h - 50f)

        // HP Overlay Text
        smallF.color = Color.WHITE
        smallF.draw(batch, "HP: ${player.health.currentHp.toInt()} / ${player.health.maxHp.toInt()}", 86f, h - 22f)

        // EXP Overlay Text
        smallF.color = Color(0.85f, 0.95f, 1f, 1f)
        val expPctText = (expPercent * 100).toInt()
        smallF.draw(batch, "EXP: ${player.stats.currentExp}/${player.stats.requiredExp} ($expPctText%)", 86f, h - 42f)

        // Ultimate Text
        smallF.color = if (player.ultimate.isReady) Color.GOLD else Color.WHITE
        val ultStatus = if (player.ultimate.isReady) "READY [O]" else "${(player.ultimate.energyPercent * 100).toInt()}%"
        smallF.draw(batch, "ULT: $ultStatus", 86f, h - 58f)

        // Stats Bar & Coins (Di bawah Player Card)
        regF.color = Color.GOLD
        regF.draw(batch, "❖ ${player.stats.gold} Coins", 15f, h - 100f)
        regF.color = Color(0.8f, 0.85f, 0.9f, 1f)
        regF.draw(batch, "ATK: ${player.stats.attackPower.toInt()} | DEF: ${player.stats.defense.toInt()} | CRIT: ${(player.stats.critRate * 100).toInt()}%", 145f, h - 100f)

        // Floor Name Banner
        headF.color = Color.CYAN
        headF.draw(batch, currentFloor.floorName, 320f, h - 70f)

        // Combo Step Indicator dengan Pop Bounce
        val comboText = when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> "COMBO: [HIT 1]"
            ComboStep.HIT_2 -> "COMBO: [HIT 2]"
            ComboStep.HIT_3 -> "COMBO: [HIT 3 FINISHER!]"
        }
        regF.color = when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> Color(1f, 0.92f, 0.2f, 1f)
            ComboStep.HIT_2 -> Color(1f, 0.62f, 0.12f, 1f)
            ComboStep.HIT_3 -> Color(1f, 0.25f, 0.25f, 1f)
        }
        if (comboPopTimer > 0f) {
            val bounce = 1.0f + (comboPopTimer / 0.28f) * 0.25f
            regF.data.setScale(bounce)
        }
        regF.draw(batch, comboText, 320f, h - 20f)
        if (comboPopTimer > 0f) {
            regF.data.setScale(1.0f)
        }

        // Skill & Dodge Status
        regF.color = if (player.skill1.isReady) Color(0.85f, 0.45f, 1f, 1f) else Color.GRAY
        val skillStatus = if (player.skill1.isReady) "READY" else {
            val t = (player.skill1.remainingCooldown * 10).toInt().coerceAtLeast(0)
            "${t / 10}.${t % 10}s"
        }
        regF.draw(batch, "Skill [U]: $skillStatus", 320f, h - 45f)

        regF.color = if (player.dodgeCooldown <= 0f) Color(0.3f, 0.8f, 1f, 1f) else Color.GRAY
        val dodgeStatus = if (player.dodgeCooldown <= 0f) "READY" else {
            val t = (player.dodgeCooldown * 10).toInt().coerceAtLeast(0)
            "${t / 10}.${t % 10}s"
        }
        regF.draw(batch, "Dodge [L/Shift]: $dodgeStatus", 490f, h - 45f)

        // Teks Tombol Pause
        regF.color = Color.WHITE
        regF.draw(batch, "PAUSE", w - 88f, h - 23f)

        // Teks Bar Boss
        if (boss != null && !boss.health.isDead) {
            regF.color = if (boss.isPhase2) Color(1f, 0.65f, 0.35f, 1f) else Color.WHITE
            val enrageTag = if (boss.isPhase2) " ❖ ENRAGED FASE 2 ❖" else ""
            regF.draw(batch, "☠ ${boss.name} [${boss.health.currentHp.toInt()}/${boss.health.maxHp.toInt()}]$enrageTag", w / 2f - 180f, h - 14f)
        }

        // Notifikasi Auto-Save
        if (saveNotificationTimer > 0f) {
            smallF.color = Color.WHITE
            smallF.draw(batch, "✓ $saveNotificationText", w / 2f - 90f, h - 20f)
        }

        // Overlays Game Over / Victory
        if (gameStatus == GameStatus.GAME_OVER) {
            headF.color = Color.RED
            headF.draw(batch, "=== GAME OVER ===", w / 2f - 120f, h / 2f + 40f)
            regF.color = Color.WHITE
            regF.draw(batch, "Tekan [R] untuk Respawn di Checkpoint", w / 2f - 160f, h / 2f)
        }

        if (gameStatus == GameStatus.STAGE_CLEAR) {
            if (currentFloorNumber < 3) {
                headF.color = Color.GOLD
                headF.draw(batch, "★ ${currentFloor.floorName.uppercase()} SELESAI! ★", w / 2f - 180f, h / 2f + 45f)
                regF.color = Color.WHITE
                regF.draw(batch, "Tekan [SPACE] untuk Naik ke Lantai ${currentFloorNumber + 1}", w / 2f - 170f, h / 2f + 12f)
                regF.color = Color.CYAN
                regF.draw(batch, "Tekan [R] untuk Main Ulang Lantai Ini", w / 2f - 140f, h / 2f - 18f)
            } else {
                headF.color = Color.GOLD
                headF.draw(batch, "★ CONGRATULATIONS! SELURUH MENARA TELAH DITAKLUKKAN! ★", w / 2f - 290f, h / 2f + 45f)
                regF.color = Color.WHITE
                regF.draw(batch, "Tower Guardian Berhasil Ditumbangkan! Player Lv.${player.stats.level} | Gold: ${player.stats.gold} G", w / 2f - 240f, h / 2f + 12f)
                regF.color = Color.CYAN
                regF.draw(batch, "Tekan [SPACE] atau [R] untuk Mengulang Menara dari Lantai 1", w / 2f - 220f, h / 2f - 18f)
            }
        }

        batch.end()
    }

    private fun handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            isPaused = false
            AudioManager.playSlashLight()
            return
        }

        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            pauseSelectedIndex = (pauseSelectedIndex - 1 + pauseMenuItems.size) % pauseMenuItems.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            pauseSelectedIndex = (pauseSelectedIndex + 1) % pauseMenuItems.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            executePauseSelection()
        }

        if (Gdx.input.justTouched()) {
            uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            uiViewport.unproject(uiTouchVec)
            val touchX = uiTouchVec.x
            val touchY = uiTouchVec.y
            val w = 1280f
            val h = 720f
            val menuW = 280f
            val startY = h / 2f + 35f
            val itemH = 46f
            val menuX = w / 2f - menuW / 2f

            for (i in pauseMenuItems.indices) {
                val itemY = startY - (i * itemH)
                if (touchX in menuX..(menuX + menuW) && touchY in itemY..(itemY + itemH)) {
                    pauseSelectedIndex = i
                    executePauseSelection()
                    break
                }
            }
        }
    }

    private fun executePauseSelection() {
        when (pauseSelectedIndex) {
            0 -> { // Resume
                isPaused = false
                AudioManager.playSlashLight()
            }
            1 -> { // Restart Floor
                isPaused = false
                loadFloor(currentFloorNumber)
                AudioManager.playSlashLight()
            }
            2 -> { // Toggle Audio
                AudioManager.toggleMute()
                if (!AudioManager.isMuted) AudioManager.playChest()
            }
            3 -> { // Main Menu
                isPaused = false
                AudioManager.playDodge()
                if (game != null) {
                    game.setScreen(MainMenuScreen(game))
                }
            }
        }
    }

    private fun renderPauseOverlay() {
        val w = 1280f
        val h = 720f

        // Dimmer background
        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.75f)
        shapeRenderer.rect(0f, 0f, w, h)

        // Pause Menu Card (Dark Obsidian with Gold Accent)
        val cardW = 340f
        val cardH = 280f
        val cardX = w / 2f - cardW / 2f
        val cardY = h / 2f - cardH / 2f
        shapeRenderer.color = Color(0.08f, 0.11f, 0.18f, 0.95f)
        shapeRenderer.rect(cardX, cardY, cardW, cardH)
        shapeRenderer.color = Color(0.9f, 0.75f, 0.25f, 0.9f)
        shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 3f)

        // Selected button highlight
        val startY = h / 2f + 35f
        val itemH = 46f
        val btnY = startY - (pauseSelectedIndex * itemH)
        shapeRenderer.color = Color(0.15f, 0.35f, 0.65f, 0.75f)
        shapeRenderer.rect(cardX + 25f, btnY - 5f, cardW - 50f, 38f)

        shapeRenderer.end()

        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        val regF = if (FontManager.isInitialized) FontManager.regularFont else font
        val headF = if (FontManager.isInitialized) FontManager.headerFont else font

        headF.color = Color.GOLD
        headF.draw(batch, "=== GAME PAUSED ===", w / 2f - 110f, cardY + cardH - 22f)

        for (i in pauseMenuItems.indices) {
            val itemText = if (i == 2) {
                if (AudioManager.isMuted) "AUDIO: MUTED" else "AUDIO: ON"
            } else {
                pauseMenuItems[i]
            }
            val curY = startY - (i * itemH) + 20f
            if (i == pauseSelectedIndex) {
                regF.color = Color.YELLOW
                regF.draw(batch, "> $itemText <", w / 2f - 70f, curY)
            } else {
                regF.color = Color.WHITE
                regF.draw(batch, itemText, w / 2f - 55f, curY)
            }
        }
        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        cameraManager.resize(width, height)
        uiViewport.update(width, height, true)
        virtualController.resize(1280, 720)
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

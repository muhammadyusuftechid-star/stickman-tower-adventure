package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.audio.HapticManager
import com.stickman.tower.character.CharacterArchetype
import com.stickman.tower.character.Player
import com.stickman.tower.combat.ComboStep
import com.stickman.tower.combat.FloatingText
import com.stickman.tower.combat.HitStopManager
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.CombatDirector
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.endless.FloorMutator
import com.stickman.tower.input.CompositeInput
import com.stickman.tower.input.GameInput
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.input.TouchSettingsManager
import com.stickman.tower.input.VirtualController
import com.stickman.tower.relic.RelicManager
import com.stickman.tower.relic.RelicType
import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.talent.TalentManager
import com.stickman.tower.ui.FontManager
import com.stickman.tower.vfx.ParticleManager
import com.badlogic.gdx.utils.Align
import com.stickman.tower.world.CameraManager
import com.stickman.tower.tools.DevToolsOverlay
import com.stickman.tower.world.LoreTablet
import com.stickman.tower.world.StoryDialogue
import com.stickman.tower.world.TowerFloor
import kotlin.math.abs
import com.stickman.tower.world.TowerLevelBuilder
import com.stickman.tower.world.ProceduralBackgroundRenderer
import kotlin.math.sin

enum class GameMode {
    STORY,
    ENDLESS
}

/**
 * Screen gameplay utama:
 * Memisahkan World Camera (16x9m physics) dan Screen UI Viewport (1280x720px)
 * untuk menjamin HUD dan Virtual Controller terkunci pixel-perfect di segala resolusi.
 * Mendukung Story Mode (Lantai 1-3) dan Endless Spire Mode (Lantai 4 ke atas).
 */
class PlayScreen(
    private val game: StickmanGame? = null,
    val initialFloor: Int = 1,
    var gameMode: GameMode = GameMode.STORY
) : Screen {
    companion object {
        val COLOR_MIASMA = Color(0.7f, 0.2f, 0.9f, 1f)
        val COLOR_SKILL_DMG = Color(0.9f, 0.4f, 1f, 1f)
        val COLOR_ENEMY_DMG = Color(1f, 0.25f, 0.25f, 1f)
        val COLOR_CRIT_DMG = Color(1f, 0.35f, 0.35f, 1f)
        val COLOR_PURPLE_SPARK = Color(0.85f, 0.35f, 1f, 1f)
        val COLOR_PURPLE_DMG = Color(0.9f, 0.3f, 1f, 1f)
    }

    private val cameraManager = CameraManager()
    private val backgroundRenderer = ProceduralBackgroundRenderer()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()

    // Dedicated UI Viewport Pass (Pixel-Perfect 1280x720 Screen Space)
    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(1280f, 720f, uiCamera)
    private val uiTouchVec = Vector3()
    private val glyphLayout = GlyphLayout()

    // Dual Input Layer: Keyboard Desktop + Virtual Controller Mobile Touch
    private val keyboardInput = KeyboardInput()
    val virtualController = VirtualController()
    private val input = CompositeInput(keyboardInput, virtualController)
    private val noInput = object : GameInput {
        override fun isMoveLeft(): Boolean = false
        override fun isMoveRight(): Boolean = false
        override fun isJumpJustPressed(): Boolean = false
        override fun isJumpPressed(): Boolean = false
        override fun isAttackJustPressed(): Boolean = false
        override fun isDodgeJustPressed(): Boolean = false
        override fun isSkillJustPressed(): Boolean = false
        override fun isUltimateJustPressed(): Boolean = false
    }

    // Data Progresi RPG & Karakter Pemain (Sesuai Arketipe Tersimpan)
    private val saveData = SaveManager.load()
    private val stats = CharacterStats().apply { loadFromSaveData(saveData) }
    private val archetype = CharacterArchetype.fromString(saveData.selectedArchetype)
    private val player = Player(spawnX = -1f, spawnY = 1.5f, stats = stats, archetype = archetype)

    // Manajer Partikel VFX
    val particleManager = ParticleManager()

    // Manajer Relik Pasif (Fase 10) & Talent Tree (Fase 13)
    val relicManager = RelicManager()
    val talentManager = TalentManager().apply { loadFromSave(saveData) }
    var isChoosingRelic: Boolean = false
    var relicOffer: List<RelicType> = emptyList()
    var relicSelectedIndex: Int = 0

    // Parameter Endless Spire & Mutator (Fase 13)
    var activeMutator: FloorMutator = FloorMutator.NONE
    private var miasmaTimer = 0f
    var endlessSurvivalTime = 0f
    var endlessEnemiesKilled = 0

    // Sistem Pause Menu & Developer QA Tools
    var isPaused: Boolean = false
    private var pauseSelectedIndex: Int = 0
    private val pauseMenuItems = listOf("RESUME", "RESTART FLOOR", "BLACKSMITH FORGE", "TALENT TREE", "CONTROL SETTINGS", "AUDIO: ON", "HAPTIC: ON", "DEVELOPER TOOLS", "MAIN MENU")
    val devTools = DevToolsOverlay()

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

    // Boss Presentation & Sinematik (Fase 11)
    var isBossIntroActive: Boolean = false
    var bossIntroTimer: Float = 0f
    var bossHpBarAnimated: Float = 0f
    var hasPlayedBossIntro: Boolean = false
    var lastBossIntroFloor: Int = -1

    // Slow Motion Super Finish & Shockwave (Fase 11)
    var bossSlowMoTimer: Float = 0f
    var isBossShockwaveActive: Boolean = false
    var shockwaveRadius: Float = 0f
    var shockwaveCenterX: Float = 0f
    var shockwaveCenterY: Float = 0f
    private var bossSlowMoTriggered: Boolean = false
    private var isFirstShow = true
    private var inputCooldown = 0.25f

    // Story Odyssey Presentation & Dialogues (Fase 14)
    var actBannerTimer: Float = 0f
    var activeLoreTablet: LoreTablet? = null
    var activeStoryDialogue: StoryDialogue? = null
    var nearbyTabletPrompt: LoreTablet? = null
    var nearbyDialoguePrompt: StoryDialogue? = null

    // Sistem Pencapaian & Trofi (Fase 15)
    private var activeAchievementBanner: com.stickman.tower.achievement.Achievement? = null
    private var achievementBannerTimer: Float = 0f

    override fun show() {
        Gdx.input?.setCatchKey(Keys.BACK, true)
        AudioManager.init()
        HapticManager.init()
        FontManager.init()
        inputCooldown = 0.25f
        val freshSave = SaveManager.load()
        stats.loadFromSaveData(freshSave)
        com.stickman.tower.achievement.AchievementManager.loadFromSave(freshSave)
        if (Gdx.graphics != null && Gdx.graphics.width > 0 && Gdx.graphics.height > 0) {
            uiViewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        }
        virtualController.applySettings(TouchSettingsManager.load(), 1280, 720)

        // Hubungkan Callback Partikel VFX Pemain
        player.onLand = { x, y -> particleManager.spawnDust(x, y, count = 6) }
        player.onJump = { x, y -> particleManager.spawnDust(x, y, count = 5) }
        player.onDodge = { x, y -> particleManager.spawnDust(x, y, count = 8) }

        // Hubungkan Callback DevTools QA & Profiler
        devTools.onWarpFloor = { targetFloor ->
            gameMode = if (targetFloor >= 11) GameMode.ENDLESS else GameMode.STORY
            loadFloor(targetFloor)
            floatingTexts.add(
                FloatingText("WARPED TO FLOOR $targetFloor", player.position.x, player.position.y + 2.5f, Color.CYAN)
            )
        }
        devTools.onGrantGold = { amount ->
            player.stats.addGold(amount)
            floatingTexts.add(
                FloatingText("+$amount GOLD [DEV CHEAT]", player.position.x, player.position.y + 2.5f, Color.GOLD)
            )
        }
        devTools.onGrantLevel = {
            grantRewards(player.stats.requiredExp - player.stats.currentExp, 0, player.position.x, player.position.y + 1f)
        }
        devTools.onHealFull = {
            player.health.heal(player.health.maxHp)
            floatingTexts.add(
                FloatingText("FULL HEAL [DEV]", player.position.x, player.position.y + 2.5f, Color.GREEN)
            )
        }
        devTools.onKillAllEnemies = {
            for (enemy in currentFloor.enemies) {
                if (!enemy.health.isDead) {
                    enemy.health.takeDamage(enemy.health.currentHp)
                }
            }
            val b = currentFloor.boss
            if (b != null && !b.health.isDead) {
                b.health.takeDamage(b.health.currentHp)
            }
            floatingTexts.add(
                FloatingText("ALL MOBS ELIMINATED [DEV]", player.position.x, player.position.y + 2.5f, Color.RED)
            )
        }

        if (isFirstShow) {
            isFirstShow = false
            loadFloor(initialFloor)
        }
    }

    /**
     * Memuat layout lantai menara tertentu (Story 1-3 atau Endless 4+).
     */
    fun loadFloor(floorNumber: Int) {
        CombatDirector.reset()
        currentFloorNumber = if (gameMode == GameMode.STORY) floorNumber.coerceIn(1, 10) else floorNumber.coerceAtLeast(11)
        activeMutator = if (gameMode == GameMode.ENDLESS) FloorMutator.getRandomMutator(currentFloorNumber) else FloorMutator.NONE
        currentFloor = if (gameMode == GameMode.STORY) TowerLevelBuilder.buildFloor(currentFloorNumber) else TowerLevelBuilder.buildEndlessFloor(currentFloorNumber, activeMutator)

        cameraManager.setBounds(
            left = currentFloor.mapBounds.x,
            bottom = currentFloor.mapBounds.y,
            right = currentFloor.mapBounds.x + currentFloor.mapBounds.width,
            top = currentFloor.mapBounds.height
        )

        player.position.set(currentFloor.spawnX, currentFloor.spawnY)
        player.velocity.set(0f, 0f)
        player.maxAirJumps = relicManager.getMaxAirJumps()
        player.airJumpsRemaining = player.maxAirJumps
        player.cooldownMultiplier = relicManager.getCooldownMultiplier() * talentManager.getCooldownMultiplier()
        player.gravityScale = currentFloor.gravityScale * activeMutator.gravityMultiplier
        player.damageReductionMultiplier = talentManager.getDamageReductionMultiplier()
        player.hasSuperArmor = talentManager.hasSuperArmor()
        player.hasUndyingWill = talentManager.hasUndyingWill()
        player.undyingWillUsed = false
        activeCheckpointPos.set(currentFloor.spawnX, currentFloor.spawnY)

        hasSavedStageClear = false
        gameStatus = GameStatus.PLAYING

        playerGhostHp = player.health.currentHp
        playerGhostDelay = 0f
        miasmaTimer = 0f
        bossGhostHp = currentFloor.boss?.health?.currentHp ?: 0f
        bossGhostDelay = 0f
        prevComboStep = ComboStep.HIT_1
        comboPopTimer = 0f

        floatingTexts.clear()
        particleManager.clear()

        bossSlowMoTriggered = false
        bossSlowMoTimer = 0f
        isBossShockwaveActive = false
        shockwaveRadius = 0f

        actBannerTimer = 3.5f
        activeLoreTablet = null
        activeStoryDialogue = null
        nearbyTabletPrompt = null
        nearbyDialoguePrompt = null

        if (currentFloor.isBossFloor && lastBossIntroFloor != currentFloorNumber) {
            isBossIntroActive = true
            bossIntroTimer = 2.8f
            bossHpBarAnimated = 0f
            hasPlayedBossIntro = true
            lastBossIntroFloor = currentFloorNumber
            AudioManager.playBossRoar()
            HapticManager.vibrateHeavy()
            cameraManager.shake(0.8f, 0.6f)
        } else {
            isBossIntroActive = false
            bossIntroTimer = 0f
            bossHpBarAnimated = 1f
        }

        floatingTexts.add(
            FloatingText("=== ${currentFloor.floorName} ===", player.position.x, player.position.y + 2.5f, Color.GOLD, duration = 2.8f)
        )
    }

    private fun triggerSave(reason: String, highestFloor: Int = currentFloorNumber) {
        if (gameMode == GameMode.STORY) {
            val saveFloor = kotlin.math.max(saveData.highestTowerFloor, highestFloor)
            saveData.highestTowerFloor = saveFloor
        } else if (gameMode == GameMode.ENDLESS) {
            saveData.highestEndlessFloor = kotlin.math.max(saveData.highestEndlessFloor, highestFloor)
        }
        saveData.totalEnemiesDefeated += endlessEnemiesKilled
        saveData.selectedArchetype = archetype.name
        val updatedSave = player.stats.applyToSaveData(saveData)
        SaveManager.save(updatedSave)
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
        // 1. Polling input controller di awal frame
        input.update(uiViewport)

        // Hotkey DevTools Console & Profiler (F1/F3/~) dan Bounding Boxes (TAB)
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.F3) || Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
            devTools.toggle()
        }
        if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        // Sinkronisasi status QA Cheats ke Player
        player.isGodMode = devTools.isGodMode
        if (devTools.isInfiniteEnergy) {
            player.skill1.reset()
            player.ultimate.addEnergy(player.ultimate.maxEnergy)
        }

        // Konsumsi sentuhan panel Developer Tools jika sedang terbuka
        if (devTools.isVisible && Gdx.input.justTouched()) {
            val handled = devTools.handleTouchDown(Gdx.input.x, Gdx.input.y, uiViewport, currentFloor)
            if (handled) {
                inputCooldown = 0.15f
            }
        }

        val safeDelta = delta.coerceAtMost(1f / 30f)
        if (inputCooldown > 0f) {
            inputCooldown = (inputCooldown - safeDelta).coerceAtLeast(0f)
        }

        // Toggle Pause dengan ESC, P, Tombol Android BACK, atau Gamepad START
        val isBackTriggered = Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
                              Gdx.input.isKeyJustPressed(Keys.P) ||
                              Gdx.input.isKeyJustPressed(Keys.BACK) ||
                              input.isPauseJustPressed()

        if (inputCooldown <= 0f) {
            if (isBackTriggered) {
                if (gameStatus == GameStatus.PLAYING && !isPaused) {
                    isPaused = true
                    pauseSelectedIndex = 0
                    AudioManager.playDodge()
                    inputCooldown = 0.20f
                } else if (isPaused) {
                    isPaused = false
                    AudioManager.playSlashLight()
                    inputCooldown = 0.20f
                } else if (gameStatus == GameStatus.GAME_OVER || gameStatus == GameStatus.STAGE_CLEAR) {
                    AudioManager.playDodge()
                    if (game != null) {
                        (game as? StickmanGame)?.releasePreservedScreen(this)
                        game.setScreen(MainMenuScreen(game))
                    }
                    return
                }
            }

            // Cek klik tombol Pause di pojok kanan atas HUD (Toleransi area sentuh luas & aman dari notch layar HP)
            if (Gdx.input.justTouched() && (gameStatus == GameStatus.PLAYING || isPaused)) {
                uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                uiViewport.unproject(uiTouchVec)
                if (uiTouchVec.x in (1280f - 180f)..1280f && uiTouchVec.y in (720f - 75f)..720f) {
                    isPaused = !isPaused
                    pauseSelectedIndex = 0
                    if (isPaused) AudioManager.playDodge() else AudioManager.playSlashLight()
                    inputCooldown = 0.20f
                }
            }
        }

        if (isPaused) {
            handlePauseInput()
            if (game != null && game.screen !== this) return
            renderWorld(0f)
            uiViewport.apply()
            shapeRenderer.projectionMatrix = uiCamera.combined
            batch.projectionMatrix = uiCamera.combined
            renderHUD()
            renderPauseOverlay()
            return
        }

        if (isChoosingRelic) {
            handleRelicChoiceInput()
            renderWorld(0f)
            uiViewport.apply()
            shapeRenderer.projectionMatrix = uiCamera.combined
            batch.projectionMatrix = uiCamera.combined
            renderHUD()
            renderRelicChoiceOverlay()
            return
        }

        if (activeLoreTablet != null) {
            handleLoreTabletInput()
            renderWorld(0f)
            uiViewport.apply()
            shapeRenderer.projectionMatrix = uiCamera.combined
            batch.projectionMatrix = uiCamera.combined
            renderHUD()
            renderLoreTabletOverlay()
            return
        }

        if (activeStoryDialogue != null) {
            handleStoryDialogueInput()
            renderWorld(0f)
            uiViewport.apply()
            shapeRenderer.projectionMatrix = uiCamera.combined
            batch.projectionMatrix = uiCamera.combined
            renderHUD()
            renderStoryDialogueOverlay()
            return
        }

        // Pembaruan Sinematik Boss Intro (Fase 11)
        if (isBossIntroActive) {
            if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.justTouched()) {
                isBossIntroActive = false
                bossIntroTimer = 0f
                bossHpBarAnimated = 1f
            } else {
                bossIntroTimer = (bossIntroTimer - safeDelta).coerceAtLeast(0f)
                bossHpBarAnimated = (1f - (bossIntroTimer / 2.8f)).coerceIn(0f, 1f)
                if (bossIntroTimer <= 0f) {
                    isBossIntroActive = false
                    bossHpBarAnimated = 1f
                }
            }
        }

        // Pembaruan Radial Shockwave & Time Dilation Super Finish (Fase 11)
        if (isBossShockwaveActive) {
            shockwaveRadius += 16f * safeDelta
            if (shockwaveRadius >= 22f) {
                isBossShockwaveActive = false
            }
        }

        val updateDelta = if (bossSlowMoTimer > 0f) {
            bossSlowMoTimer = (bossSlowMoTimer - safeDelta).coerceAtLeast(0f)
            safeDelta * 0.25f
        } else {
            safeDelta
        }

        handleStatusInput()

        hudAnimTime += safeDelta
        if (actBannerTimer > 0f) {
            actBannerTimer = (actBannerTimer - safeDelta).coerceAtLeast(0f)
        }

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

        // Pembaruan Antrean Notifikasi Banner Achievement (Fase 15)
        if (activeAchievementBanner == null) {
            val nextAchv = com.stickman.tower.achievement.AchievementManager.pollNotification()
            if (nextAchv != null) {
                activeAchievementBanner = nextAchv
                achievementBannerTimer = 3.8f
                AudioManager.playChest()
                HapticManager.vibrateMedium()
            }
        } else {
            achievementBannerTimer -= safeDelta
            if (achievementBannerTimer <= 0f) {
                activeAchievementBanner = null
            }
        }

        // 1. Hit Stop Micro-Freeze Update
        val isFrozen = hitStopManager.update(safeDelta)

        if (!isFrozen && gameStatus == GameStatus.PLAYING) {
            // Pembaruan Pemain (abaikan input kendali jika sedang intro sinematik)
            val activeInput = if (isBossIntroActive) noInput else input
            player.update(updateDelta, activeInput, currentFloor.platforms)
            particleManager.update(updateDelta)

            // Cek Batas Jurang / Out of Bounds (Mencegah karakter jatuh ke jurang hampa tanpa batas)
            if (player.position.y < currentFloor.mapBounds.y - 3.5f && !player.health.isDead) {
                if (devTools.isGodMode) {
                    player.position.set(currentFloor.spawnX, currentFloor.spawnY)
                    player.velocity.set(0f, 0f)
                } else {
                    player.health.takeDamage(9999f)
                }
            }

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
                            if (currentFloorNumber == 2 && chest.x > 20f && relicManager.activeRelics.size < 2) {
                                triggerRelicChoice()
                            }
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

            // Pembaruan Prasasti Batu Hitam Kuno (The Lore Tablets)
            nearbyTabletPrompt = null
            for (tablet in currentFloor.loreTablets) {
                tablet.update(updateDelta)
                val dist = player.position.dst(tablet.x + tablet.width / 2f, tablet.y + tablet.height / 2f)
                if (dist < 2.0f || player.bounds.overlaps(tablet.bounds)) {
                    nearbyTabletPrompt = tablet
                    if (Gdx.input.isKeyJustPressed(Keys.E)) {
                        activeLoreTablet = tablet
                        tablet.isRead = true
                        AudioManager.playChest()
                        break
                    }
                }
            }

            // Pembaruan Dialog Naratif Karakter (NPC & Cutscenes)
            nearbyDialoguePrompt = null
            for (dialogue in currentFloor.storyDialogues) {
                val dist = player.position.dst(dialogue.triggerX, dialogue.triggerY)
                if (dist <= dialogue.triggerRadius) {
                    if (!dialogue.isTriggered) {
                        nearbyDialoguePrompt = dialogue
                        if (activeStoryDialogue == null && activeLoreTablet == null) {
                            activeStoryDialogue = dialogue
                            AudioManager.playLevelUp()
                            break
                        }
                    }
                }
            }

            // Pembaruan Sistem AI Combat Director & Musuh Biasa
            CombatDirector.update(updateDelta)
            for (enemy in currentFloor.enemies) {
                enemy.update(updateDelta, player, currentFloor.platforms)
                if (enemy.health.isDead && !enemy.rewardClaimed) {
                    enemy.rewardClaimed = true
                    endlessEnemiesKilled++
                    AudioManager.playDeath()
                    particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 16, color = Color.RED)
                    grantRewards(enemy.expReward, enemy.goldReward, enemy.position.x + enemy.width / 2f, enemy.position.y + 1f)
                    relicManager.onEnemyDefeated(player, floatingTexts, particleManager)
                    com.stickman.tower.achievement.AchievementManager.onEnemyKilled(false, currentFloorNumber, saveData = saveData)
                }
            }

            // Pembaruan Boss (Jika Ada)
            val boss = currentFloor.boss
            if (boss != null) {
                val wasPhase2 = boss.isPhase2
                boss.update(updateDelta, player, currentFloor.platforms)
                if (!wasPhase2 && boss.isPhase2) {
                    cameraManager.shake(0.35f, 0.25f)
                    AudioManager.playBossRoar()
                    HapticManager.vibrateHeavy()
                    floatingTexts.add(
                        FloatingText("❖ ${boss.name}: \"${boss.enrageQuote}\"", boss.position.x + boss.width / 2f, boss.position.y + boss.height + 1.2f, Color.SCARLET, duration = 4.0f)
                    )
                }
                if (boss.health.isDead && !boss.rewardClaimed) {
                    boss.rewardClaimed = true
                    endlessEnemiesKilled++
                    AudioManager.playDeath()
                    particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 35, color = Color.GOLD)
                    grantRewards(boss.expReward, boss.goldReward, boss.position.x + boss.width / 2f, boss.position.y + 1.5f)
                    com.stickman.tower.achievement.AchievementManager.onEnemyKilled(true, currentFloorNumber, boss.name, saveData = saveData)
                    floatingTexts.add(
                        FloatingText("★ BOSS DEFEATED! ★", boss.position.x, boss.position.y + 3.2f, Color.GOLD, duration = 3.5f)
                    )
                    floatingTexts.add(
                        FloatingText("\"${boss.deathQuote}\"", boss.position.x + boss.width / 2f, boss.position.y + 2.2f, Color.LIGHT_GRAY, duration = 4.5f)
                    )
                }
            }

            // 2. Deteksi Pertarungan Dua Arah (Pemain <-> Musuh / Boss)
            handleTwoWayCombat()

            // Toxic Miasma Mutator Tick (Fase 13)
            if (activeMutator == FloorMutator.TOXIC_MIASMA && !player.health.isDead) {
                miasmaTimer += updateDelta
                if (miasmaTimer >= 4.5f) {
                    miasmaTimer = 0f
                    player.takeHit(4f, 0f, 0f)
                    cameraManager.shake(0.2f, 0.15f)
                    particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 8, color = Color.PURPLE)
                    floatingTexts.add(
                        FloatingText("-4 MIASMA", player.position.x + player.width / 2f, player.position.y + player.height + 0.4f, COLOR_MIASMA)
                    )
                }
            }

            // Endless Survival Time Tracking
            if (gameMode == GameMode.ENDLESS && !player.health.isDead) {
                endlessSurvivalTime += updateDelta
            }

            // 3. Evaluasi Kondisi Menang & Kalah
            evaluateGameConditions()
        }

        // 4. Perbarui Teks Floating Damage (Zero-Allocation Reverse Indexed Loop)
        for (i in floatingTexts.indices.reversed()) {
            val ft = floatingTexts[i]
            ft.update(updateDelta)
            if (ft.isExpired) {
                floatingTexts.removeAt(i)
            }
        }

        // 5. Render Objek Dunia 2D & Partikel (World Space: Meters)
        renderWorld(safeDelta)

        // 6. UI Viewport Pass (Pixel-Perfect 1280x720 Screen Space)
        uiViewport.apply()
        shapeRenderer.projectionMatrix = uiCamera.combined
        batch.projectionMatrix = uiCamera.combined

        // 7. Pass UI Geometri (Consolidated ShapeRenderer Pass: Virtual Controller + HUD)
        virtualController.renderShapes(
            shapeRenderer,
            player.skill1,
            player.skill2,
            player.skill3,
            player.ultimate
        )
        renderHUDShapes()
        if (isBossIntroActive) {
            renderBossIntroShapes()
        }

        // 8. Pass UI Teks & Ikon (Consolidated Single Continuous SpriteBatch Pass)
        batch.begin()
        virtualController.renderText(
            batch,
            FontManager.smallFont,
            player.skill1,
            player.skill2,
            player.skill3,
            player.ultimate
        )
        renderHUDText()
        if (isBossIntroActive) {
            renderBossIntroText()
        }
        batch.end()

        // 9. Developer Tools & Profiler Overlay (Senior Tools Architecture)
        devTools.render(shapeRenderer, batch, uiCamera, player, currentFloor, particleManager.activeParticleCount)
    }

    private fun renderWorld(delta: Float) {
        val lookaheadTarget = if (abs(player.velocity.x) > 0.6f) (if (player.facingRight) 1.5f else -1.5f) else 0f
        val targetCamX = if (isBossIntroActive && currentFloor.boss != null) currentFloor.boss!!.position.x + currentFloor.boss!!.width / 2f else player.position.x + player.width / 2f + lookaheadTarget
        val targetCamY = if (isBossIntroActive && currentFloor.boss != null) currentFloor.boss!!.position.y + 1.2f else player.position.y + player.height / 2f + 0.5f
        cameraManager.update(targetCamX, targetCamY, delta)

        // Zero-Allocation Clear Color Selection
        val clearR: Float
        val clearG: Float
        val clearB: Float
        when {
            currentFloor.isSafeZone -> { clearR = 0.14f; clearG = 0.08f; clearB = 0.04f }
            currentFloor.floorNumber == 9 -> { clearR = 0.16f; clearG = 0.03f; clearB = 0.05f }
            currentFloor.floorNumber in 4..5 -> { clearR = 0.10f; clearG = 0.05f; clearB = 0.15f }
            currentFloor.floorNumber in 7..8 -> { clearR = 0.04f; clearG = 0.05f; clearB = 0.12f }
            currentFloor.floorNumber == 10 -> { clearR = 0.02f; clearG = 0.02f; clearB = 0.05f }
            currentFloor.isBossFloor -> { clearR = 0.12f; clearG = 0.08f; clearB = 0.12f }
            else -> { clearR = 0.08f; clearG = 0.09f; clearB = 0.13f }
        }
        ScreenUtils.clear(clearR, clearG, clearB, 1f)

        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // 1. Render Atmospheric Parallax Background (Zero-Allocation Layered Depth)
        backgroundRenderer.render(
            shapeRenderer,
            cameraManager.camera,
            currentFloor.floorNumber,
            hudAnimTime,
            currentFloor.mapBounds.width
        )

        for (platform in currentFloor.platforms) platform.render(shapeRenderer)
        for (spike in currentFloor.spikes) spike.render(shapeRenderer)
        for (chest in currentFloor.chests) chest.render(shapeRenderer)
        for (tablet in currentFloor.loreTablets) tablet.render(shapeRenderer)
        currentFloor.checkpoint?.render(shapeRenderer)
        currentFloor.exitPortal.render(shapeRenderer)

        // Render Tokoh Cerita (NPC)
        if (currentFloor.floorNumber == 6) {
            renderVulcanusNPC(shapeRenderer)
        } else if (currentFloor.floorNumber == 1) {
            renderAriaNPC(shapeRenderer)
        }

        for (enemy in currentFloor.enemies) enemy.render(shapeRenderer)
        currentFloor.boss?.render(shapeRenderer)
        player.render(shapeRenderer)
        player.renderProjectiles(shapeRenderer)

        // Render Efek Partikel VFX
        particleManager.render(shapeRenderer)

        // Render Radial Shockwave Super Finish Boss (Fase 11)
        if (isBossShockwaveActive && shockwaveRadius > 0f) {
            val shockAlpha = (1f - shockwaveRadius / 22f).coerceIn(0f, 1f)
            shapeRenderer.setColor(1f, 1f, 1f, shockAlpha * 0.9f)
            shapeRenderer.circle(shockwaveCenterX, shockwaveCenterY, shockwaveRadius, 36)
            shapeRenderer.setColor(0.9f, 0.7f, 0.2f, shockAlpha * 0.5f)
            shapeRenderer.circle(shockwaveCenterX, shockwaveCenterY, (shockwaveRadius - 0.35f).coerceAtLeast(0f), 36)
        }
        shapeRenderer.end()

        // Debug Bounding Boxes
        if (showDebugBounds || devTools.showHitboxes) {
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
            val dmgFont = FontManager.regularFont
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

                        val isHeavyAttack = player.comboManager.currentStep == ComboStep.HIT_3 ||
                                player.isSkillStriking || player.isSkill2Active || player.isSkill3Active || player.isUlting

                        if (enemy.canBlockHitFrom(player.position.x)) {
                            if (isHeavyAttack) {
                                enemy.breakGuard(1.8f)
                                AudioManager.playParry()
                                hitStopManager.trigger(0.08f)
                                particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height * 0.6f, count = 18, color = Color.GOLD)
                                floatingTexts.add(
                                    FloatingText("★ GUARD BREAK! ★", enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height + 0.45f, Color.GOLD, isCritical = true)
                                )
                            } else {
                                AudioManager.playParry()
                                hitStopManager.trigger(0.04f)
                                particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height * 0.6f, count = 10, color = Color.CYAN)
                                floatingTexts.add(
                                    FloatingText("BLOCKED!", enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height + 0.35f, Color.CYAN)
                                )
                                enemy.velocity.x += if (player.facingRight) 2.0f else -2.0f
                                continue
                            }
                        }

                        val knockX = if (player.facingRight) player.hitbox.knockbackX else -player.hitbox.knockbackX
                        val knockY = player.hitbox.knockbackY

                        if (enemy.takeHit(player.hitbox.damage, knockX, knockY)) {
                            val combo = player.comboManager.registerHit()
                            hitStopManager.trigger(0.06f)
                            AudioManager.playHit()
                            particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 10, color = if (player.hitbox.isCritical) Color.GOLD else Color.WHITE)
                            relicManager.onComboHit(combo, player, currentFloor.enemies, currentFloor.boss, floatingTexts, particleManager)
                            com.stickman.tower.achievement.AchievementManager.onComboHit(combo, saveData)

                            // Lifesteal Mutator & Talents (Fase 13)
                            val lifesteal = activeMutator.lifestealPercent
                            if (lifesteal > 0f) {
                                val heal = player.hitbox.damage * lifesteal
                                if (heal >= 0.5f) {
                                    player.health.heal(heal)
                                    floatingTexts.add(
                                        FloatingText("+${heal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.3f, Color.GREEN)
                                    )
                                }
                            }

                            // Toxic Blade Sub-class Talent (Fase 13)
                            if (talentManager.hasToxicBlade()) {
                                enemy.applyPoison(3.5f, player.baseAttackPower * 0.35f)
                                particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 5, color = Color.GREEN)
                            }

                            val energyGain = when {
                                player.isUlting -> 0f
                                player.comboManager.currentStep == ComboStep.HIT_3 -> 15f
                                player.isSkillStriking || player.isSkill2Active || player.isSkill3Active -> 12f
                                else -> 8f
                            }
                            player.ultimate.addEnergy(energyGain)

                            val critPrefix = if (player.hitbox.isCritical) "CRIT " else "-"
                            val textColor = when {
                                player.hitbox.isCritical -> Color.GOLD
                                player.isUlting -> Color.GOLD
                                player.isSkillStriking || player.isSkill2Active || player.isSkill3Active -> COLOR_SKILL_DMG
                                player.comboManager.currentStep == ComboStep.HIT_3 -> Color.ORANGE
                                else -> COLOR_ENEMY_DMG
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
                        val combo = player.comboManager.registerHit()
                        bossGhostDelay = 0.35f
                        hitStopManager.trigger(0.06f)
                        AudioManager.playHit()
                        particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 14, color = Color.ORANGE)
                        relicManager.onComboHit(combo, player, currentFloor.enemies, currentFloor.boss, floatingTexts, particleManager)
                        com.stickman.tower.achievement.AchievementManager.onComboHit(combo, saveData)

                        // Lifesteal Mutator & Talents (Fase 13)
                        val lifesteal = activeMutator.lifestealPercent
                        if (lifesteal > 0f) {
                            val heal = player.hitbox.damage * lifesteal
                            if (heal >= 0.5f) {
                                player.health.heal(heal)
                                floatingTexts.add(
                                    FloatingText("+${heal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.3f, Color.GREEN)
                                )
                            }
                        }

                        // Toxic Blade Sub-class Talent (Fase 13)
                        if (talentManager.hasToxicBlade()) {
                            boss.applyPoison(3.5f, player.baseAttackPower * 0.35f)
                            particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 6, color = Color.GREEN)
                        }

                        // Super Finish Slow-Motion & Radial Shockwave jika Boss Mati (Fase 11)
                        if (boss.health.isDead && !bossSlowMoTriggered) {
                            bossSlowMoTriggered = true
                            bossSlowMoTimer = 0.55f
                            isBossShockwaveActive = true
                            shockwaveRadius = 0.5f
                            shockwaveCenterX = boss.position.x + boss.width / 2f
                            shockwaveCenterY = boss.position.y + boss.height / 2f
                            cameraManager.shake(1.2f, 0.7f)
                            HapticManager.vibrateHeavy()
                        }

                        val energyGain = when {
                            player.isUlting -> 0f
                            player.comboManager.currentStep == ComboStep.HIT_3 -> 18f
                            player.isSkillStriking || player.isSkill2Active || player.isSkill3Active -> 15f
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

        // A2. Proyektil Pemain (Skill 1: Gale Crescent / Shuriken / Shockwave) mengenai Musuh & Boss
        for (i in 0 until player.projectiles.size) {
            val proj = player.projectiles[i]
            if (proj.isDestroyed) continue

            for (enemy in currentFloor.enemies) {
                if (!enemy.health.isDead && proj.bounds.overlaps(enemy.hurtbox.bounds)) {
                    if (enemy.canBlockHitFrom(proj.position.x)) {
                        AudioManager.playParry()
                        particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height * 0.6f, count = 10, color = Color.CYAN)
                        floatingTexts.add(
                            FloatingText("BLOCKED!", enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height + 0.35f, Color.CYAN)
                        )
                        proj.isDestroyed = true
                        break
                    }
                    val knockX = if (proj.velocityX > 0) 6f else -6f
                    if (enemy.takeHit(proj.damage, knockX, 2f)) {
                        particleManager.spawnHitSparks(enemy.position.x + enemy.width / 2f, enemy.position.y + enemy.height / 2f, count = 8, color = proj.color)
                        floatingTexts.add(
                            FloatingText(
                                "-${proj.damage.toInt()}",
                                enemy.position.x + enemy.width / 2f,
                                enemy.position.y + enemy.height + 0.35f,
                                proj.color
                            )
                        )
                        player.comboManager.registerHit()
                        player.ultimate.addEnergy(8f)
                    }
                    proj.isDestroyed = true
                    break
                }
            }

            if (!proj.isDestroyed && boss != null && !boss.health.isDead && proj.bounds.overlaps(boss.hurtbox.bounds)) {
                val knockX = if (proj.velocityX > 0) 4f else -4f
                if (boss.takeHit(proj.damage, knockX, 1.5f)) {
                    particleManager.spawnHitSparks(boss.position.x + boss.width / 2f, boss.position.y + boss.height / 2f, count = 10, color = proj.color)
                    floatingTexts.add(
                        FloatingText(
                            "-${proj.damage.toInt()}",
                            boss.position.x + boss.width / 2f,
                            boss.position.y + boss.height + 0.5f,
                            proj.color
                        )
                    )
                    player.comboManager.registerHit()
                    player.ultimate.addEnergy(10f)
                }
                proj.isDestroyed = true
            }
        }

        // B. Serangan Musuh Biasa Mengenai Pemain (Dapat di-Parry / Guard Break)
        for (enemy in currentFloor.enemies) {
            if (enemy.hitbox.isActive && !enemy.hitbox.hitEntities.contains(player) && !player.health.isDead) {
                if (enemy.hitbox.bounds.overlaps(player.hurtbox.bounds)) {
                    enemy.hitbox.hitEntities.add(player)

                    if (player.isParrying) {
                        // Sistem Perfect Parry Fase 11: Stun musuh 1.5s, netralkan damage, beri buff ATK + energy
                        enemy.stun(1.5f)
                        player.triggerParry()
                        val parryCombo = player.comboManager.registerHit()
                        relicManager.onComboHit(parryCombo, player, currentFloor.enemies, currentFloor.boss, floatingTexts, particleManager)
                        if (talentManager.hasParryHeal()) {
                            val parryHeal = player.health.maxHp * 0.15f
                            player.health.heal(parryHeal)
                            floatingTexts.add(
                                FloatingText("+${parryHeal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.35f, Color.GREEN)
                            )
                        }
                        hitStopManager.trigger(0.08f)
                        AudioManager.playParry()
                        com.stickman.tower.achievement.AchievementManager.onPerfectParry(saveData)
                        HapticManager.vibrateHeavy()
                        input.gamepad.vibrate(200, 1.0f)
                        cameraManager.shake(0.5f, 0.25f)
                        particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 22, color = Color.GOLD)
                        floatingTexts.add(
                            FloatingText(
                                "✨ PERFECT PARRY! ✨",
                                player.position.x + player.width / 2f,
                                player.position.y + player.height + 0.5f,
                                Color.GOLD,
                                duration = 1.4f,
                                isCritical = true
                            )
                        )
                    } else {
                        val knockX = if (enemy.facingRight) enemy.hitbox.knockbackX else -enemy.hitbox.knockbackX
                        val knockY = enemy.hitbox.knockbackY

                        if (player.takeHit(enemy.hitbox.damage, knockX, knockY)) {
                            playerGhostDelay = 0.35f
                            hitStopManager.trigger(0.08f)
                            AudioManager.playHit()
                            HapticManager.vibrateMedium()
                            input.gamepad.vibrate(150, 0.6f)
                            particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 10, color = Color.RED)
                            floatingTexts.add(
                                FloatingText(
                                    "-${enemy.hitbox.damage.toInt()}",
                                    player.position.x + player.width / 2f,
                                    player.position.y + player.height + 0.35f,
                                    COLOR_CRIT_DMG
                                )
                            )
                        }
                    }
                }
            }
        }

        // C. Serangan Melee Boss Mengenai Pemain (Dapat di-Parry / Guard Break)
        if (boss != null && boss.hitbox.isActive && !boss.hitbox.hitEntities.contains(player) && !player.health.isDead) {
            if (boss.hitbox.bounds.overlaps(player.hurtbox.bounds)) {
                boss.hitbox.hitEntities.add(player)

                if (player.isParrying) {
                    // Sistem Perfect Parry Fase 11: Stun Boss 1.5s (Guard Break!), netralkan damage
                    boss.stun(1.5f)
                    player.triggerParry()
                    val parryCombo = player.comboManager.registerHit()
                    relicManager.onComboHit(parryCombo, player, currentFloor.enemies, currentFloor.boss, floatingTexts, particleManager)
                    if (talentManager.hasParryHeal()) {
                        val parryHeal = player.health.maxHp * 0.15f
                        player.health.heal(parryHeal)
                        floatingTexts.add(
                            FloatingText("+${parryHeal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.35f, Color.GREEN)
                        )
                    }
                    hitStopManager.trigger(0.08f)
                    AudioManager.playParry()
                    com.stickman.tower.achievement.AchievementManager.onPerfectParry(saveData)
                    HapticManager.vibrateHeavy()
                    input.gamepad.vibrate(250, 1.0f)
                    cameraManager.shake(0.8f, 0.35f)
                    particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 28, color = Color.GOLD)
                    floatingTexts.add(
                        FloatingText(
                            "✨ PERFECT PARRY! ✨",
                            player.position.x + player.width / 2f,
                            player.position.y + player.height + 0.5f,
                            Color.GOLD,
                            duration = 1.4f,
                            isCritical = true
                        )
                    )
                } else {
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
                                COLOR_ENEMY_DMG
                            )
                        )
                    }
                }
            }
        }

        // D. Proyektil Wind Slash Boss Mengenai Pemain (Dapat di-Deflect)
        if (boss != null) {
            for (proj in boss.projectiles) {
                if (!proj.isDestroyed && proj.bounds.overlaps(player.hurtbox.bounds) && !player.health.isDead) {
                    proj.isDestroyed = true

                    if (player.isParrying) {
                        player.triggerParry()
                        if (talentManager.hasParryHeal()) {
                            val parryHeal = player.health.maxHp * 0.15f
                            player.health.heal(parryHeal)
                            floatingTexts.add(
                                FloatingText("+${parryHeal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.35f, Color.GREEN)
                            )
                        }
                        hitStopManager.trigger(0.08f)
                        AudioManager.playParry()
                        HapticManager.vibrateHeavy()
                        input.gamepad.vibrate(200, 1.0f)
                        cameraManager.shake(0.6f, 0.3f)
                        particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 20, color = Color.GOLD)
                        floatingTexts.add(
                            FloatingText(
                                "✨ DEFLECTED! ✨",
                                player.position.x + player.width / 2f,
                                player.position.y + player.height + 0.5f,
                                Color.GOLD,
                                duration = 1.4f,
                                isCritical = true
                            )
                        )
                    } else {
                        val knockX = if (proj.velocityX > 0f) 6.5f else -6.5f
                        if (player.takeHit(proj.damage, knockX, 3.5f)) {
                            playerGhostDelay = 0.35f
                            hitStopManager.trigger(0.06f)
                            AudioManager.playHit()
                            HapticManager.vibrateMedium()
                            input.gamepad.vibrate(150, 0.6f)
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

        // E. Proyektil Ranged Enemy Mengenai Pemain (Dapat di-Deflect)
        for (enemy in currentFloor.enemies) {
            if (enemy.projectiles.isNotEmpty()) {
                for (proj in enemy.projectiles) {
                    if (!proj.isDestroyed && proj.bounds.overlaps(player.hurtbox.bounds) && !player.health.isDead) {
                        proj.isDestroyed = true

                        if (player.isParrying) {
                            player.triggerParry()
                            if (talentManager.hasParryHeal()) {
                                val parryHeal = player.health.maxHp * 0.15f
                                player.health.heal(parryHeal)
                                floatingTexts.add(
                                    FloatingText("+${parryHeal.toInt()} HP", player.position.x + player.width / 2f, player.position.y + player.height + 0.35f, Color.GREEN)
                                )
                            }
                            hitStopManager.trigger(0.08f)
                            AudioManager.playParry()
                            HapticManager.vibrateHeavy()
                            input.gamepad.vibrate(200, 1.0f)
                            cameraManager.shake(0.5f, 0.25f)
                            particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 18, color = Color.GOLD)
                            floatingTexts.add(
                                FloatingText(
                                    "✨ DEFLECTED! ✨",
                                    player.position.x + player.width / 2f,
                                    player.position.y + player.height + 0.5f,
                                    Color.GOLD,
                                    duration = 1.4f,
                                    isCritical = true
                                )
                            )
                        } else {
                            val knockX = if (proj.velocityX > 0f) 5.0f else -5.0f
                            if (player.takeHit(proj.damage, knockX, 2.5f)) {
                                playerGhostDelay = 0.35f
                                hitStopManager.trigger(0.05f)
                                AudioManager.playHit()
                                particleManager.spawnHitSparks(player.position.x + player.width / 2f, player.position.y + player.height / 2f, count = 8, color = COLOR_PURPLE_SPARK)
                                floatingTexts.add(
                                    FloatingText(
                                        "-${proj.damage.toInt()}",
                                        player.position.x + player.width / 2f,
                                        player.position.y + player.height + 0.35f,
                                        COLOR_PURPLE_DMG
                                    )
                                )
                            }
                        }
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

        val isFloorCleared = if (currentFloor.isSafeZone) {
            true
        } else if (currentFloor.isBossFloor) {
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
                if (gameMode == GameMode.STORY) {
                    val nextFloorNum = (currentFloorNumber + 1).coerceAtMost(10)
                    val expReward = if (currentFloor.isBossFloor) (currentFloor.boss?.expReward ?: 250) else (100 + currentFloorNumber * 20)
                    val goldReward = if (currentFloor.isBossFloor) (currentFloor.boss?.goldReward ?: 150) else (60 + currentFloorNumber * 10)
                    grantRewards(expReward, goldReward, currentFloor.exitPortal.x + 0.8f, currentFloor.exitPortal.y + 1.5f)

                    if (currentFloorNumber == 10) {
                        triggerSave("Puncak Menara Ditaklukkan! (Story Selesai)", highestFloor = 11)
                        saveData.highestEndlessFloor = kotlin.math.max(saveData.highestEndlessFloor, 11)
                    } else {
                        triggerSave("Lantai $currentFloorNumber Selesai", highestFloor = nextFloorNum)
                    }

                    if (currentFloorNumber == 1 && relicManager.activeRelics.isEmpty()) {
                        triggerRelicChoice()
                    } else if (currentFloorNumber in listOf(3, 5, 8) && relicManager.activeRelics.size < 4) {
                        triggerRelicChoice()
                    }
                } else {
                    // Endless Mode Stage Clear
                    val expReward = 120 + (currentFloorNumber * 25)
                    val goldReward = 75 + (currentFloorNumber * 15)
                    grantRewards(expReward, goldReward, currentFloor.exitPortal.x + 0.8f, currentFloor.exitPortal.y + 1.5f)

                    // Milestone Talent Point: +1 Poin setiap 2 lantai endless
                    if (currentFloorNumber % 2 == 0) {
                        talentManager.addTalentPoints(1)
                        talentManager.syncToSave(saveData)
                        SaveManager.save(saveData)
                        floatingTexts.add(
                            FloatingText("+1 TALENT POINT! ✦", player.position.x + player.width / 2f, player.position.y + player.height + 0.9f, Color.YELLOW, duration = 2.2f)
                        )
                    }
                    com.stickman.tower.achievement.AchievementManager.onEndlessFloorReached(currentFloorNumber, saveData)
                    triggerSave("Endless Lantai $currentFloorNumber Selesai", highestFloor = currentFloorNumber + 1)
                }
            }
        }
    }

    private fun handleStatusInput() {
        val w = 1280f
        val h = 720f

        if (gameStatus == GameStatus.GAME_OVER) {
            val keyRespawn = Gdx.input.isKeyJustPressed(Keys.R) ||
                             Gdx.input.isKeyJustPressed(Keys.SPACE) ||
                             Gdx.input.isKeyJustPressed(Keys.ENTER) ||
                             input.isJumpJustPressed() ||
                             input.isAttackJustPressed()
            val keyMenu = Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
            var touchRespawn = false
            var touchMenu = false

            if (Gdx.input.justTouched()) {
                uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                uiViewport.unproject(uiTouchVec)
                val tx = uiTouchVec.x
                val ty = uiTouchVec.y

                // Tombol Menu Utama di Layar Game Over: [w/2 + 20 .. w/2 + 260, h/2 - 85 .. h/2 - 15]
                // Tombol Respawn di Layar Game Over: [w/2 - 260 .. w/2 - 10, h/2 - 85 .. h/2 - 15]
                if (tx in (w / 2f + 15f)..(w / 2f + 265f) && ty in (h / 2f - 85f)..(h / 2f - 15f)) {
                    touchMenu = true
                } else if (tx in (w / 2f - 265f)..(w / 2f - 15f) && ty in (h / 2f - 85f)..(h / 2f - 15f)) {
                    touchRespawn = true
                }
            }

            if (touchMenu || keyMenu) {
                AudioManager.playDodge()
                if (game != null) {
                    (game as? StickmanGame)?.releasePreservedScreen(this)
                    game.setScreen(MainMenuScreen(game))
                }
                return
            }

            if (keyRespawn || touchRespawn) {
                player.respawn(activeCheckpointPos.x, activeCheckpointPos.y)
                currentFloor.boss?.let { b ->
                    if (!b.health.isDead) {
                        b.projectiles.clear()
                    }
                }
                gameStatus = GameStatus.PLAYING
                AudioManager.playSlashLight()
            }
        } else if (gameStatus == GameStatus.STAGE_CLEAR) {
            val keyProceed = Gdx.input.isKeyJustPressed(Keys.SPACE) ||
                             Gdx.input.isKeyJustPressed(Keys.ENTER) ||
                             input.isJumpJustPressed() ||
                             input.isAttackJustPressed()
            val keyRestart = Gdx.input.isKeyJustPressed(Keys.R)
            val keyMenu = Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
            var touchProceed = false
            var touchRetry = false
            var touchMenu = false

            if (Gdx.input.justTouched()) {
                uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                uiViewport.unproject(uiTouchVec)
                val tx = uiTouchVec.x
                val ty = uiTouchVec.y

                val btnYMin = h / 2f - 75f
                val btnYMax = h / 2f - 15f
                val menu3YMin = h / 2f - 135f
                val menu3YMax = h / 2f - 80f

                if (tx in (w / 2f - 275f)..(w / 2f - 10f) && ty in btnYMin..btnYMax) {
                    touchProceed = true
                } else if (tx in (w / 2f + 10f)..(w / 2f + 275f) && ty in btnYMin..btnYMax) {
                    touchRetry = true
                } else if (tx in (w / 2f - 150f)..(w / 2f + 150f) && ty in menu3YMin..menu3YMax) {
                    touchMenu = true
                }
            }

            if (touchMenu || keyMenu) {
                AudioManager.playDodge()
                if (game != null) {
                    (game as? StickmanGame)?.releasePreservedScreen(this)
                    game.setScreen(MainMenuScreen(game))
                }
                return
            }

            val proceedTriggered = keyProceed || touchProceed
            val retryTriggered = keyRestart || touchRetry

            if (gameMode == GameMode.STORY) {
                if (currentFloorNumber < 10) {
                    if (proceedTriggered) {
                        loadFloor(currentFloorNumber + 1)
                    } else if (retryTriggered) {
                        loadFloor(currentFloorNumber)
                    }
                } else {
                    // Lantai 10 Selesai (Final Story Boss Kalah) -> Buka Akses Endless Spire!
                    if (proceedTriggered) {
                        gameMode = GameMode.ENDLESS
                        loadFloor(11)
                    } else if (retryTriggered) {
                        loadFloor(1)
                    }
                }
            } else {
                // Endless Mode: Lanjut ke Lantai Berikutnya
                if (proceedTriggered) {
                    loadFloor(currentFloorNumber + 1)
                } else if (retryTriggered) {
                    loadFloor(currentFloorNumber)
                }
            }
        }
    }

    private fun renderHUD() {
        renderHUDShapes()
        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        renderHUDText()
        batch.end()
    }

    private fun renderHUDShapes() {
        val w = 1280f
        val h = 720f

        val hpPercent = (player.health.currentHp / player.health.maxHp).coerceIn(0f, 1f)
        val ghostHpPercent = (playerGhostHp / player.health.maxHp).coerceIn(0f, 1f)
        val isLowHp = hpPercent <= 0.25f
        val dangerThrob = if (isLowHp) ((Math.sin(hudAnimTime.toDouble() * 9.0).toFloat() + 1f) * 0.5f) else 0f

        // 1. Render Elemen Latar Bar & Kartu Karakter (ShapeRenderer pada Screen Space 1280x720)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Panel Latar Player Status Card (Kiri Atas - Obsidian Glassmorphism)
        shapeRenderer.setColor(0.06f + 0.08f * dangerThrob, 0.09f * (1f - dangerThrob), 0.15f * (1f - dangerThrob), 0.94f)
        shapeRenderer.rect(12f, h - 90f, 290f, 78f)

        // Top Accent Line (Red danger pulse or Sleek Blue/Gold)
        if (isLowHp) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.7f + 0.3f * dangerThrob)
        } else {
            shapeRenderer.setColor(0.22f, 0.38f, 0.60f, 0.75f)
        }
        shapeRenderer.rectLine(12f, h - 12f, 302f, h - 12f, 2f)

        // Level Badge Box
        shapeRenderer.setColor(0.10f, 0.15f, 0.24f, 1f)
        shapeRenderer.rect(18f, h - 84f, 55f, 66f)
        shapeRenderer.setColor(0.85f, 0.70f, 0.25f, 0.85f)
        shapeRenderer.rectLine(18f, h - 84f, 73f, h - 84f, 2f)

        // HP Bar Trough (Dark Obsidian)
        shapeRenderer.setColor(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 35f, 210f, 16f)

        // HP Amber Ghost Catch-Up Bar
        if (ghostHpPercent > hpPercent) {
            shapeRenderer.setColor(0.95f, 0.62f, 0.15f, 0.90f)
            shapeRenderer.rect(80f, h - 35f, 210f * ghostHpPercent, 16f)
        }

        // HP Main Bar Fill (Emerald -> Gold -> Pulsing Crimson Danger)
        if (hpPercent > 0.5f) {
            shapeRenderer.setColor(0.18f, 0.85f, 0.38f, 1f)
        } else if (hpPercent > 0.25f) {
            shapeRenderer.setColor(0.95f, 0.75f, 0.15f, 1f)
        } else {
            val throb = (Math.sin(hudAnimTime.toDouble() * 10.0).toFloat() + 1f) * 0.5f
            shapeRenderer.setColor(0.88f + 0.12f * throb, 0.15f * (1f - throb), 0.15f * (1f - throb), 1f)
        }
        shapeRenderer.rect(80f, h - 35f, 210f * hpPercent, 16f)

        // HP Bar Highlight Rim
        shapeRenderer.setColor(1f, 1f, 1f, 0.22f)
        shapeRenderer.rectLine(80f, h - 20f, 290f, h - 20f, 1f)

        // EXP Bar Background & Fill
        shapeRenderer.setColor(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f, 12f)

        val expPercent = (player.stats.currentExp.toFloat() / player.stats.requiredExp.toFloat()).coerceIn(0f, 1f)
        shapeRenderer.setColor(0.15f, 0.75f, 0.95f, 1f)
        shapeRenderer.rect(80f, h - 53f, 210f * expPercent, 12f)
        shapeRenderer.setColor(1f, 1f, 1f, 0.20f)
        shapeRenderer.rectLine(80f, h - 42f, 290f, h - 42f, 1f)

        // Ultimate Energy Bar Background & Fill
        shapeRenderer.setColor(0.05f, 0.06f, 0.09f, 1f)
        shapeRenderer.rect(80f, h - 69f, 210f, 10f)

        if (player.ultimate.isReady) {
            val ultPulse = (Math.sin(hudAnimTime.toDouble() * 7.0).toFloat() + 1f) * 0.5f
            shapeRenderer.setColor(1f, 0.85f + 0.15f * ultPulse, 0.2f, 1f)
        } else {
            shapeRenderer.setColor(0.85f, 0.65f, 0.15f, 0.85f)
        }
        shapeRenderer.rect(80f, h - 69f, 210f * player.ultimate.energyPercent, 10f)

        // Save Notification Pill (Jika Aktif)
        if (saveNotificationTimer > 0f) {
            shapeRenderer.setColor(0.12f, 0.55f, 0.28f, 0.90f)
            shapeRenderer.rect(w / 2f - 110f, h - 38f, 220f, 26f)
        }

        // Tombol Pause (Pojok Kanan Atas)
        shapeRenderer.setColor(0.12f, 0.16f, 0.25f, 0.85f)
        shapeRenderer.rect(w - 110f, h - 45f, 98f, 32f)
        shapeRenderer.setColor(0.9f, 0.75f, 0.25f, 0.85f)
        shapeRenderer.rectLine(w - 110f, h - 45f, w - 12f, h - 45f, 2f)

        // Bar HP Boss Raksasa dengan Amber Ghost Bar & Phase 2 Threshold Marker
        val boss = currentFloor.boss
        if (boss != null && !boss.health.isDead) {
            val bossBarW = 440f
            val bossBarH = 22f
            val bossBarX = w / 2f - bossBarW / 2f
            val bossBarY = h - 36f

            // Frame Gelap Ornate
            shapeRenderer.setColor(0.05f, 0.06f, 0.09f, 0.96f)
            shapeRenderer.rect(bossBarX - 5f, bossBarY - 5f, bossBarW + 10f, bossBarH + 10f)

            // Outer border line
            if (boss.isPhase2) {
                shapeRenderer.setColor(0.95f, 0.35f, 0.15f, 0.9f)
            } else {
                shapeRenderer.setColor(0.80f, 0.68f, 0.30f, 0.85f)
            }
            shapeRenderer.rectLine(bossBarX - 5f, bossBarY + bossBarH + 5f, bossBarX + bossBarW + 5f, bossBarY + bossBarH + 5f, 2f)
            shapeRenderer.rectLine(bossBarX - 5f, bossBarY - 5f, bossBarX + bossBarW + 5f, bossBarY - 5f, 1.5f)

            // Trough
            shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 1f)
            shapeRenderer.rect(bossBarX, bossBarY, bossBarW, bossBarH)

            // Boss Ghost Catch-Up Bar (Amber)
            val currentBossHpPct = (boss.health.currentHp / boss.health.maxHp).coerceIn(0f, 1f)
            val bossHpPct = if (isBossIntroActive) bossHpBarAnimated else currentBossHpPct
            val bossGhostPct = (bossGhostHp / boss.health.maxHp).coerceIn(0f, 1f)
            if (!isBossIntroActive && bossGhostPct > bossHpPct) {
                shapeRenderer.setColor(0.95f, 0.62f, 0.15f, 0.90f)
                shapeRenderer.rect(bossBarX, bossBarY, bossBarW * bossGhostPct, bossBarH)
            }

            // Boss Main Fill Bar (Crimson ke Api Fasa 2)
            if (boss.isPhase2) {
                val flame = (Math.sin(hudAnimTime.toDouble() * 9.0).toFloat() + 1f) * 0.5f
                shapeRenderer.setColor(1f, 0.22f + 0.18f * flame, 0.1f, 1f)
            } else {
                shapeRenderer.setColor(0.85f, 0.15f, 0.24f, 1f)
            }
            shapeRenderer.rect(bossBarX, bossBarY, bossBarW * bossHpPct, bossBarH)

            // Top subtle highlight line
            shapeRenderer.setColor(1f, 1f, 1f, 0.25f)
            shapeRenderer.rectLine(bossBarX, bossBarY + bossBarH - 2f, bossBarX + bossBarW * bossHpPct, bossBarY + bossBarH - 2f, 1.5f)

            // Phase 2 Threshold Marker (50% HP Indicator Divider)
            val markerX = bossBarX + bossBarW * 0.5f
            shapeRenderer.setColor(1f, 0.88f, 0.35f, 0.95f)
            shapeRenderer.rectLine(markerX, bossBarY - 4f, markerX, bossBarY + bossBarH + 4f, 2.5f)
            shapeRenderer.triangle(
                markerX - 4f, bossBarY + bossBarH + 5f,
                markerX + 4f, bossBarY + bossBarH + 5f,
                markerX, bossBarY + bossBarH + 10f
            )
        }

        // Mutator Badge Background
        if (gameMode == GameMode.ENDLESS && activeMutator != FloorMutator.NONE) {
            shapeRenderer.setColor(0.12f, 0.08f, 0.18f, 0.85f)
            shapeRenderer.rect(w / 2f - 240f, h - 110f, 480f, 38f)
            shapeRenderer.setColor(0.85f, 0.45f, 0.95f, 0.9f)
            shapeRenderer.rectLine(w / 2f - 240f, h - 72f, w / 2f + 240f, h - 72f, 2f)
        }

        // Overlays Game Over / Stage Clear Buttons (ShapeRenderer)
        if (gameStatus == GameStatus.GAME_OVER) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.75f)
            shapeRenderer.rect(0f, 0f, w, h)

            val cardW = 560f
            val cardH = 220f
            val cardX = w / 2f - cardW / 2f
            val cardY = h / 2f - cardH / 2f
            shapeRenderer.setColor(0.08f, 0.10f, 0.16f, 0.95f)
            shapeRenderer.rect(cardX, cardY, cardW, cardH)
            shapeRenderer.setColor(0.9f, 0.2f, 0.25f, 0.92f)
            shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 3f)

            // Tombol 1: Respawn (Emerald)
            shapeRenderer.setColor(0.14f, 0.38f, 0.22f, 0.95f)
            shapeRenderer.rect(w / 2f - 250f, h / 2f - 75f, 230f, 50f)
            shapeRenderer.setColor(0.25f, 0.9f, 0.45f, 0.95f)
            shapeRenderer.rectLine(w / 2f - 250f, h / 2f - 25f, w / 2f - 20f, h / 2f - 25f, 2.5f)

            // Tombol 2: Menu Utama (Dark Slate)
            shapeRenderer.setColor(0.14f, 0.18f, 0.26f, 0.95f)
            shapeRenderer.rect(w / 2f + 20f, h / 2f - 75f, 230f, 50f)
            shapeRenderer.setColor(0.7f, 0.78f, 0.9f, 0.9f)
            shapeRenderer.rectLine(w / 2f + 20f, h / 2f - 25f, w / 2f + 250f, h / 2f - 25f, 2.5f)
        }

        if (gameStatus == GameStatus.STAGE_CLEAR) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.70f)
            shapeRenderer.rect(0f, 0f, w, h)

            val cardW = 600f
            val cardH = 280f
            val cardX = w / 2f - cardW / 2f
            val cardY = h / 2f - cardH / 2f
            shapeRenderer.setColor(0.08f, 0.11f, 0.18f, 0.96f)
            shapeRenderer.rect(cardX, cardY, cardW, cardH)
            shapeRenderer.setColor(0.95f, 0.75f, 0.25f, 0.95f)
            shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 3f)

            // Tombol 1: Lanjut Menara (Glowing Emerald / Gold)
            shapeRenderer.setColor(0.16f, 0.36f, 0.20f, 0.95f)
            shapeRenderer.rect(w / 2f - 270f, h / 2f - 70f, 250f, 48f)
            shapeRenderer.setColor(0.3f, 0.95f, 0.45f, 0.95f)
            shapeRenderer.rectLine(w / 2f - 270f, h / 2f - 22f, w / 2f - 20f, h / 2f - 22f, 2.5f)

            // Tombol 2: Main Ulang (Cyan Slate)
            shapeRenderer.setColor(0.14f, 0.20f, 0.30f, 0.95f)
            shapeRenderer.rect(w / 2f + 20f, h / 2f - 70f, 250f, 48f)
            shapeRenderer.setColor(0.35f, 0.85f, 1f, 0.9f)
            shapeRenderer.rectLine(w / 2f + 20f, h / 2f - 22f, w / 2f + 270f, h / 2f - 22f, 2.5f)

            // Tombol 3: Menu Utama (Dark Slate with Gold line)
            shapeRenderer.setColor(0.12f, 0.16f, 0.24f, 0.95f)
            shapeRenderer.rect(w / 2f - 140f, h / 2f - 128f, 280f, 44f)
            shapeRenderer.setColor(0.9f, 0.75f, 0.25f, 0.9f)
            shapeRenderer.rectLine(w / 2f - 140f, h / 2f - 84f, w / 2f + 140f, h / 2f - 84f, 2.5f)
        }

        // Kotak Prompt Prasasti (Dipindahkan ke Shape Pass agar tidak menginterupsi Batch Pass)
        if (gameStatus == GameStatus.PLAYING && nearbyTabletPrompt != null) {
            val pw = 360f
            val ph = 36f
            val px = (w - pw) / 2f
            val py = 135f
            shapeRenderer.setColor(0.08f, 0.12f, 0.18f, 0.90f)
            shapeRenderer.rect(px, py, pw, ph)
            shapeRenderer.setColor(0.85f, 0.70f, 0.25f, 0.90f)
            shapeRenderer.rectLine(px, py + ph, px + pw, py + ph, 2f)
        }

        renderActBannerShapes()
        renderAchievementBannerShapes()

        shapeRenderer.end()
    }

    private fun renderHUDText() {
        val w = 1280f
        val h = 720f
        val expPercent = (player.stats.currentExp.toFloat() / player.stats.requiredExp.toFloat()).coerceIn(0f, 1f)
        val boss = currentFloor.boss

        // Level Badge Text
        val smallF = FontManager.smallFont
        val regF = FontManager.regularFont
        val headF = FontManager.headerFont

        smallF.setColor(0.7f, 0.85f, 1f, 1f)
        smallF.draw(batch, "LVL", 32f, h - 28f)
        headF.color = Color.WHITE
        headF.draw(batch, "${player.stats.level}", 34f, h - 50f)

        // HP Overlay Text
        smallF.color = Color.WHITE
        smallF.draw(batch, "HP: ${player.health.currentHp.toInt()} / ${player.health.maxHp.toInt()}", 86f, h - 22f)

        // EXP Overlay Text
        smallF.setColor(0.85f, 0.95f, 1f, 1f)
        val expPctText = (expPercent * 100).toInt()
        smallF.draw(batch, "EXP: ${player.stats.currentExp}/${player.stats.requiredExp} ($expPctText%)", 86f, h - 42f)

        // Ultimate Text
        if (player.ultimate.isReady) smallF.color = Color.GOLD else smallF.color = Color.WHITE
        val ultStatus = if (player.ultimate.isReady) "READY [O]" else "${(player.ultimate.energyPercent * 100).toInt()}%"
        smallF.draw(batch, "ULT: $ultStatus", 86f, h - 58f)

        // Stats Bar & Coins (Di bawah Player Card)
        regF.color = Color.GOLD
        regF.draw(batch, "❖ ${player.stats.gold} Coins", 15f, h - 100f)
        regF.setColor(0.8f, 0.85f, 0.9f, 1f)
        val atkBonusStr = if (player.stats.weaponAtkBonus > 0f) " (+${player.stats.weaponAtkBonus.toInt()})" else ""
        regF.draw(batch, "ATK: ${player.stats.attackPower.toInt()}$atkBonusStr | DEF: ${player.stats.defense.toInt()} | CRIT: ${(player.stats.critRate * 100).toInt()}%", 145f, h - 100f)

        // Active Relics Display
        if (relicManager.activeRelics.isNotEmpty()) {
            val relicStr = relicManager.activeRelics.joinToString("  ") { "${it.iconRune} ${it.title}" }
            smallF.setColor(0.3f, 0.85f, 1f, 1f)
            smallF.draw(batch, "RELICS: $relicStr", 15f, h - 122f)
        }

        // Floor Name Banner
        headF.color = Color.CYAN
        headF.draw(batch, currentFloor.floorName, 320f, h - 70f)

        // Combo Step Indicator dengan Pop Bounce
        val comboText = when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> "COMBO: [HIT 1]"
            ComboStep.HIT_2 -> "COMBO: [HIT 2]"
            ComboStep.HIT_3 -> "COMBO: [HIT 3 FINISHER!]"
        }
        when (player.comboManager.currentStep) {
            ComboStep.HIT_1 -> regF.setColor(1f, 0.92f, 0.2f, 1f)
            ComboStep.HIT_2 -> regF.setColor(1f, 0.62f, 0.12f, 1f)
            ComboStep.HIT_3 -> regF.setColor(1f, 0.25f, 0.25f, 1f)
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
        if (player.skill1.isReady) regF.setColor(0.85f, 0.45f, 1f, 1f) else regF.color = Color.GRAY
        val skillStatus = if (player.skill1.isReady) "READY" else {
            val t = (player.skill1.remainingCooldown * 10).toInt().coerceAtLeast(0)
            "${t / 10}.${t % 10}s"
        }
        regF.draw(batch, "Skill [U]: $skillStatus", 320f, h - 45f)

        if (player.dodgeCooldown <= 0f) regF.setColor(0.3f, 0.8f, 1f, 1f) else regF.color = Color.GRAY
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
            if (boss.isPhase2) regF.setColor(1f, 0.65f, 0.35f, 1f) else regF.color = Color.WHITE
            val enrageTag = if (boss.isPhase2) " ❖ ENRAGED FASE 2 ❖" else ""
            val hpDisplay = if (isBossIntroActive) (bossHpBarAnimated * boss.health.maxHp).toInt() else boss.health.currentHp.toInt()
            regF.draw(batch, "☠ ${boss.name} [$hpDisplay/${boss.health.maxHp.toInt()}]$enrageTag", w / 2f - 180f, h - 14f)
        }

        // Notifikasi Auto-Save
        if (saveNotificationTimer > 0f) {
            smallF.color = Color.WHITE
            smallF.draw(batch, "✓ $saveNotificationText", w / 2f - 90f, h - 20f)
        }

        // Mutator Badge Text
        if (gameMode == GameMode.ENDLESS && activeMutator != FloorMutator.NONE) {
            val mutatorBadge = "${activeMutator.iconRune} MUTATOR: ${activeMutator.displayName}"
            regF.color = Color.ORANGE
            regF.draw(batch, mutatorBadge, w / 2f - 130f, h - 76f)
            smallF.setColor(0.9f, 0.9f, 0.9f, 0.85f)
            smallF.draw(batch, activeMutator.description, w / 2f - 230f, h - 94f)
        }

        // Overlays Game Over / Victory
        if (gameStatus == GameStatus.GAME_OVER) {
            headF.setColor(1f, 0.25f, 0.25f, 1f)
            headF.draw(batch, "=== GAME OVER ===", w / 2f - 110f, h / 2f + 65f)
            regF.setColor(0.9f, 0.9f, 0.95f, 1f)
            regF.draw(batch, "Pahlawan gugur! Sentuh tombol di bawah atau ketuk layar:", w / 2f - 230f, h / 2f + 20f)

            // Tombol Teks
            headF.color = Color.WHITE
            headF.draw(batch, "RESPAWN (R)", w / 2f - 200f, h / 2f - 42f)
            headF.setColor(0.9f, 0.95f, 1f, 1f)
            headF.draw(batch, "MENU UTAMA", w / 2f + 70f, h / 2f - 42f)
        }

        if (gameStatus == GameStatus.STAGE_CLEAR) {
            val isStory = gameMode == GameMode.STORY
            headF.color = Color.GOLD
            val bannerTitle = if (isStory) {
                if (currentFloorNumber < 10) "★ ${currentFloor.floorName.uppercase()} SELESAI! ★" else "★ TOWER CONQUERED! THE ENDLESS PARADOX BEGINS! ★"
            } else {
                "★ ENDLESS FLOOR $currentFloorNumber CLEARED! ★"
            }
            glyphLayout.setText(headF, bannerTitle)
            headF.draw(batch, bannerTitle, (w - glyphLayout.width) / 2f, h / 2f + 75f)

            regF.color = Color.WHITE
            val subText = if (isStory) {
                if (currentFloorNumber < 10) "Musuh telah dibersihkan! Bersiap untuk lantai berikutnya."
                else "Sang Penulis Takdir tumbang! Pecahnya pena takdir membuka dimensi Endless Spire."
            } else {
                val talentPtText = if (currentFloorNumber % 2 == 0) " | +1 TALENT POINT!" else ""
                "Kills: $endlessEnemiesKilled | Waktu: ${endlessSurvivalTime.toInt()}s$talentPtText"
            }
            glyphLayout.setText(regF, subText)
            regF.draw(batch, subText, (w - glyphLayout.width) / 2f, h / 2f + 25f)

            val nextFloorNumber = if (isStory && currentFloorNumber >= 10) 11 else currentFloorNumber + 1
            val nextFloorLabel = if (isStory && currentFloorNumber >= 10) "ENDLESS SPIRE (F11)" else "LANTAI $nextFloorNumber"

            headF.color = Color.WHITE
            headF.draw(batch, "LANJUT ($nextFloorLabel)", w / 2f - 240f, h / 2f - 40f)
            headF.setColor(0.85f, 0.95f, 1f, 1f)
            headF.draw(batch, "MAIN ULANG (R)", w / 2f + 55f, h / 2f - 40f)
            headF.setColor(0.9f, 0.75f, 0.25f, 1f)
            headF.draw(batch, "MENU UTAMA (ESC)", w / 2f - 105f, h / 2f - 98f)
        }

        if (gameStatus == GameStatus.PLAYING) {
            nearbyTabletPrompt?.let { tablet ->
                val promptText = if (tablet.isRead) "[E] / [TAP] BACA ULANG PRASASTI" else "✦ [E] / [TAP] BACA PRASASTI KUNO ✦"
                glyphLayout.setText(regF, promptText)
                val pw = glyphLayout.width + 30f
                val ph = 36f
                val px = (w - pw) / 2f
                val py = 135f

                regF.color = Color.GOLD
                regF.draw(batch, promptText, px + (pw - glyphLayout.width) / 2f, py + 25f)

                if (Gdx.input.justTouched()) {
                    uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                    uiViewport.unproject(uiTouchVec)
                    if (uiTouchVec.x in px..(px + pw) && uiTouchVec.y in py..(py + ph)) {
                        activeLoreTablet = tablet
                        tablet.isRead = true
                        AudioManager.playChest()
                    }
                }
            }
        }

        renderAchievementBannerText()
        renderActBannerText()
    }

    private fun handlePauseInput() {
        if (inputCooldown > 0f) return

        if (Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.ESCAPE) || input.isPauseJustPressed()) {
            isPaused = false
            AudioManager.playSlashLight()
            inputCooldown = 0.20f
            return
        }

        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W) || input.gamepad.isUpJustPressed()) {
            pauseSelectedIndex = (pauseSelectedIndex - 1 + pauseMenuItems.size) % pauseMenuItems.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S) || input.gamepad.isDownJustPressed()) {
            pauseSelectedIndex = (pauseSelectedIndex + 1) % pauseMenuItems.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE) || input.isJumpJustPressed() || input.isAttackJustPressed()) {
            executePauseSelection()
        }

        if (Gdx.input.justTouched()) {
            uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            uiViewport.unproject(uiTouchVec)
            val touchX = uiTouchVec.x
            val touchY = uiTouchVec.y
            val w = 1280f
            val h = 720f
            val menuW = 320f
            val startY = h / 2f + 155f
            val itemH = 38f
            val menuX = w / 2f - menuW / 2f

            for (i in pauseMenuItems.indices) {
                val itemY = startY - (i * itemH)
                if (touchX in (menuX - 35f)..(menuX + menuW + 35f) && touchY in (itemY - 14f)..(itemY + itemH - 4f)) {
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
            2 -> { // Blacksmith Forge
                AudioManager.playSlashLight()
                if (game != null) {
                    (game as? StickmanGame)?.preserveScreen(this)
                    game.setScreen(BlacksmithScreen(game, this))
                }
            }
            3 -> { // Talent Tree (Fase 13)
                AudioManager.playSlashLight()
                if (game != null) {
                    (game as? StickmanGame)?.preserveScreen(this)
                    game.setScreen(TalentTreeScreen(game, this))
                }
            }
            4 -> { // Control Settings (Fase 12)
                AudioManager.playSlashLight()
                if (game != null) {
                    (game as? StickmanGame)?.preserveScreen(this)
                    game.setScreen(ControlSettingsScreen(game, this))
                }
            }
            5 -> { // Toggle Audio
                AudioManager.toggleMute()
                if (!AudioManager.isMuted) AudioManager.playChest()
            }
            6 -> { // Toggle Haptic
                HapticManager.toggle()
                AudioManager.playSlashLight()
            }
            7 -> { // Developer Tools & Profiler (Tools Architecture)
                isPaused = false
                devTools.isVisible = true
                AudioManager.playSlashLight()
            }
            8 -> { // Main Menu
                isPaused = false
                AudioManager.playDodge()
                if (game != null) {
                    (game as? StickmanGame)?.releasePreservedScreen(this)
                    game.setScreen(MainMenuScreen(game))
                }
            }
        }
    }

    private fun renderPauseOverlay() {
        val w = 1280f
        val h = 720f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // Dimmer background
        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f)
        shapeRenderer.rect(0f, 0f, w, h)

        // Pause Menu Card (Dark Obsidian with Gold Accent)
        val cardW = 380f
        val cardH = 500f
        val cardX = w / 2f - cardW / 2f
        val cardY = h / 2f - cardH / 2f
        shapeRenderer.setColor(0.08f, 0.11f, 0.18f, 0.95f)
        shapeRenderer.rect(cardX, cardY, cardW, cardH)
        shapeRenderer.setColor(0.9f, 0.75f, 0.25f, 0.9f)
        shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 3f)

        // Selected button highlight
        val startY = h / 2f + 155f
        val itemH = 38f
        val btnY = startY - (pauseSelectedIndex * itemH)
        shapeRenderer.setColor(0.15f, 0.35f, 0.65f, 0.75f)
        shapeRenderer.rect(cardX + 25f, btnY - 8f, cardW - 50f, 32f)

        shapeRenderer.end()

        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        val regF = FontManager.regularFont
        val headF = FontManager.headerFont

        headF.color = Color.GOLD
        headF.draw(batch, "=== GAME PAUSED ===", w / 2f - 110f, cardY + cardH - 22f)

        for (i in pauseMenuItems.indices) {
            val itemText = when (i) {
                5 -> if (AudioManager.isMuted) "AUDIO: MUTED" else "AUDIO: ON"
                6 -> if (HapticManager.isEnabled) "HAPTIC: ON" else "HAPTIC: OFF"
                else -> pauseMenuItems[i]
            }
            val curY = startY - (i * itemH) + 16f
            if (i == pauseSelectedIndex) {
                regF.color = Color.YELLOW
                regF.draw(batch, "> $itemText <", w / 2f - 85f, curY)
            } else {
                regF.color = Color.WHITE
                regF.draw(batch, itemText, w / 2f - 70f, curY)
            }
        }
        batch.end()
    }

    fun triggerRelicChoice() {
        val available = RelicType.getRandomRelics(3, relicManager.activeRelics)
        if (available.isNotEmpty()) {
            relicOffer = available
            relicSelectedIndex = 0
            isChoosingRelic = true
            AudioManager.playChest()
            HapticManager.vibrateMedium()
        }
    }

    private fun handleRelicChoiceInput() {
        if (relicOffer.isEmpty()) {
            isChoosingRelic = false
            return
        }

        if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.A)) {
            relicSelectedIndex = (relicSelectedIndex - 1 + relicOffer.size) % relicOffer.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || Gdx.input.isKeyJustPressed(Keys.D)) {
            relicSelectedIndex = (relicSelectedIndex + 1) % relicOffer.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1) && relicOffer.size >= 1) {
            relicSelectedIndex = 0
            confirmRelicChoice()
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_2) && relicOffer.size >= 2) {
            relicSelectedIndex = 1
            confirmRelicChoice()
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_3) && relicOffer.size >= 3) {
            relicSelectedIndex = 2
            confirmRelicChoice()
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            confirmRelicChoice()
        }

        if (Gdx.input.justTouched()) {
            uiTouchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            uiViewport.unproject(uiTouchVec)

            val total = relicOffer.size
            val cardW = 320f
            val cardH = 340f
            val gap = 40f
            val startX = (1280f - (total * cardW + (total - 1) * gap)) / 2f
            val cardY = (720f - cardH) / 2f - 20f

            for (i in relicOffer.indices) {
                val cx = startX + i * (cardW + gap)
                if (uiTouchVec.x in cx..(cx + cardW) && uiTouchVec.y in cardY..(cardY + cardH)) {
                    relicSelectedIndex = i
                    confirmRelicChoice()
                    break
                }
            }
        }
    }

    private fun confirmRelicChoice() {
        if (relicSelectedIndex in relicOffer.indices) {
            val chosen = relicOffer[relicSelectedIndex]
            relicManager.add(chosen)
            player.maxAirJumps = relicManager.getMaxAirJumps()
            player.airJumpsRemaining = player.maxAirJumps
            player.cooldownMultiplier = relicManager.getCooldownMultiplier()
            floatingTexts.add(
                FloatingText(
                    text = "RELIC ACQUIRED: ${chosen.title}!",
                    worldX = player.position.x,
                    worldY = player.position.y + player.height + 1.2f,
                    color = Color.GOLD,
                    duration = 2.5f
                )
            )
            isChoosingRelic = false
        }
    }

    private fun renderRelicChoiceOverlay() {
        val w = 1280f
        val h = 720f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dimmer background
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f)
        shapeRenderer.rect(0f, 0f, w, h)

        // Sanctum Header Box
        shapeRenderer.setColor(0.08f, 0.11f, 0.18f, 0.95f)
        shapeRenderer.rect(140f, 570f, 1000f, 90f)
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 0.85f)
        shapeRenderer.rectLine(140f, 570f, 1140f, 570f, 2.5f)

        val total = relicOffer.size
        val cardW = 320f
        val cardH = 340f
        val gap = 40f
        val startX = (w - (total * cardW + (total - 1) * gap)) / 2f
        val cardY = (h - cardH) / 2f - 30f

        for (i in relicOffer.indices) {
            val cx = startX + i * (cardW + gap)
            val isSelected = i == relicSelectedIndex

            // Card body
            if (isSelected) {
                shapeRenderer.setColor(0.12f, 0.18f, 0.28f, 0.95f)
            } else {
                shapeRenderer.setColor(0.07f, 0.09f, 0.14f, 0.90f)
            }
            shapeRenderer.rect(cx, cardY, cardW, cardH)

            // Glowing border
            val pulse = if (isSelected) (sin(hudAnimTime * 6f) + 1f) * 0.15f else 0f
            if (isSelected) {
                shapeRenderer.setColor(0.2f, 0.85f, 1f, 0.85f + pulse)
            } else {
                shapeRenderer.setColor(0.3f, 0.35f, 0.45f, 0.6f)
            }
            val borderThick = if (isSelected) 3f else 1.5f
            shapeRenderer.rectLine(cx, cardY, cx + cardW, cardY, borderThick)
            shapeRenderer.rectLine(cx, cardY + cardH, cx + cardW, cardY + cardH, borderThick)
            shapeRenderer.rectLine(cx, cardY, cx, cardY + cardH, borderThick)
            shapeRenderer.rectLine(cx + cardW, cardY, cx + cardW, cardY + cardH, borderThick)

            // Select button at card bottom
            val btnW = 200f
            val btnH = 42f
            val btnX = cx + (cardW - btnW) / 2f
            val btnY = cardY + 25f
            if (isSelected) {
                shapeRenderer.setColor(0.15f, 0.6f, 0.85f, 0.95f)
            } else {
                shapeRenderer.setColor(0.12f, 0.15f, 0.22f, 0.8f)
            }
            shapeRenderer.rect(btnX, btnY, btnW, btnH)
            if (isSelected) {
                shapeRenderer.color = Color.CYAN
            } else {
                shapeRenderer.setColor(0.35f, 0.4f, 0.5f, 0.6f)
            }
            shapeRenderer.rectLine(btnX, btnY, btnX + btnW, btnY, 1.5f)
            shapeRenderer.rectLine(btnX, btnY + btnH, btnX + btnW, btnY + btnH, 1.5f)
        }

        shapeRenderer.end()

        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        val titleF = FontManager.titleFont
        val headF = FontManager.headerFont
        val regF = FontManager.regularFont

        titleF.color = Color.CYAN
        titleF.draw(batch, "ANCIENT RELIC SANCTUARY", 180f, 638f)
        regF.setColor(0.85f, 0.9f, 1f, 0.9f)
        regF.draw(batch, "Commune with the Spire: Select one passive relic to augment your ascension", 180f, 600f)

        for (i in relicOffer.indices) {
            val relic = relicOffer[i]
            val cx = startX + i * (cardW + gap)
            val isSelected = i == relicSelectedIndex

            headF.color = if (isSelected) Color.GOLD else Color.WHITE
            val runeTitle = "${relic.iconRune} ${relic.title}"
            glyphLayout.setText(headF, runeTitle)
            headF.draw(batch, runeTitle, cx + (cardW - glyphLayout.width) / 2f, cardY + cardH - 35f)

            regF.setColor(0.80f, 0.85f, 0.95f, 0.9f)
            glyphLayout.setText(regF, relic.description)
            regF.draw(batch, relic.description, cx + 25f, cardY + cardH - 95f, cardW - 50f, 1, true)

            // Tombol pilih
            val btnW = 200f
            val btnY = cardY + 25f
            val btnText = if (isSelected) "⚔  SELECT  ⚔" else "SELECT"
            headF.color = if (isSelected) Color.WHITE else Color.LIGHT_GRAY
            glyphLayout.setText(headF, btnText)
            headF.draw(batch, btnText, cx + (cardW - glyphLayout.width) / 2f, btnY + 28f)
        }

        regF.setColor(0.6f, 0.65f, 0.75f, 0.8f)
        glyphLayout.setText(regF, "[A / D / LEFT / RIGHT / TOUCH] Browse  |  [1 / 2 / 3 / SPACE / ENTER] Claim Relic")
        regF.draw(batch, "[A / D / LEFT / RIGHT / TOUCH] Browse  |  [1 / 2 / 3 / SPACE / ENTER] Claim Relic", (w - glyphLayout.width) / 2f, cardY - 20f)

        batch.end()
    }

    private fun renderBossIntroOverlay() {
        renderBossIntroShapes()
        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        renderBossIntroText()
        batch.end()
    }

    private fun renderBossIntroShapes() {
        val w = 1280f
        val h = 720f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Letterbox Bars (Top & Bottom Sinematik)
        val barHeight = 85f
        shapeRenderer.setColor(0f, 0f, 0f, 0.95f)
        shapeRenderer.rect(0f, h - barHeight, w, barHeight)
        shapeRenderer.rect(0f, 0f, w, barHeight)

        // Gold Accent Lines on Letterbox Edge
        shapeRenderer.setColor(0.92f, 0.78f, 0.28f, 0.75f)
        shapeRenderer.rectLine(0f, h - barHeight, w, h - barHeight, 2f)
        shapeRenderer.rectLine(0f, barHeight, w, barHeight, 2f)

        // Title Card Plaque (Center-Lower Obsidian Glassmorphism)
        val cardW = 620f
        val cardH = 100f
        val cardX = (w - cardW) / 2f
        val cardY = 190f
        shapeRenderer.setColor(0.06f, 0.08f, 0.14f, 0.94f)
        shapeRenderer.rect(cardX, cardY, cardW, cardH)

        // Card Border Lines
        shapeRenderer.setColor(0.92f, 0.78f, 0.28f, 0.9f)
        shapeRenderer.rectLine(cardX, cardY, cardX + cardW, cardY, 2.5f)
        shapeRenderer.rectLine(cardX, cardY + cardH, cardX + cardW, cardY + cardH, 2.5f)
        shapeRenderer.rectLine(cardX, cardY, cardX, cardY + cardH, 2f)
        shapeRenderer.rectLine(cardX + cardW, cardY, cardX + cardW, cardY + cardH, 2f)

        shapeRenderer.end()
    }

    private fun renderBossIntroText() {
        val w = 1280f
        val barHeight = 85f
        val cardH = 100f
        val cardY = 190f

        val titleF = FontManager.titleFont
        val regF = FontManager.regularFont
        val smallF = FontManager.smallFont

        // Boss Title in Gold
        val currentBoss = currentFloor.boss
        titleF.setColor(1f, 0.85f, 0.25f, 1f)
        val bossTitle = "⚔ ${currentBoss?.name ?: "BOSS"} ⚔"
        glyphLayout.setText(titleF, bossTitle)
        titleF.draw(batch, bossTitle, (w - glyphLayout.width) / 2f, cardY + cardH - 24f)

        // Boss Subtitle / Lore
        regF.setColor(0.85f, 0.90f, 1f, 0.95f)
        val bossSub = "${currentBoss?.subtitle ?: "Tower Boss"}  •  Lantai $currentFloorNumber"
        glyphLayout.setText(regF, bossSub)
        regF.draw(batch, bossSub, (w - glyphLayout.width) / 2f, cardY + 38f)

        // Skip prompt inside bottom letterbox
        val skipPulse = (sin(hudAnimTime * 6f) + 1f) * 0.5f
        smallF.setColor(0.85f + 0.15f * skipPulse, 0.85f + 0.15f * skipPulse, 0.85f, 0.85f)
        val skipText = "[TOUCH / SPACE / ENTER] Skip Intro"
        glyphLayout.setText(smallF, skipText)
        smallF.draw(batch, skipText, (w - glyphLayout.width) / 2f, barHeight / 2f + 6f)
    }

    private fun handleLoreTabletInput() {
        val tablet = activeLoreTablet ?: return
        if (Gdx.input.isKeyJustPressed(Keys.E) ||
            Gdx.input.isKeyJustPressed(Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Keys.BACK) ||
            input.isAttackJustPressed() ||
            input.isJumpJustPressed() ||
            Gdx.input.justTouched()) {
            tablet.isRead = true
            activeLoreTablet = null
            AudioManager.playSlashLight()
            inputCooldown = 0.2f
        }
    }

    private fun handleStoryDialogueInput() {
        val dialogue = activeStoryDialogue ?: return
        if (Gdx.input.isKeyJustPressed(Keys.E) ||
            Gdx.input.isKeyJustPressed(Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Keys.ENTER) ||
            input.isAttackJustPressed() ||
            input.isJumpJustPressed() ||
            Gdx.input.justTouched()) {
            val hasNext = dialogue.advance()
            if (!hasNext) {
                dialogue.isTriggered = true
                activeStoryDialogue = null
            }
            AudioManager.playSlashLight()
            inputCooldown = 0.2f
        }
    }

    private fun renderLoreTabletOverlay() {
        val tablet = activeLoreTablet ?: return
        val w = 1280f
        val h = 720f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dark dim background
        shapeRenderer.setColor(0f, 0f, 0f, 0.82f)
        shapeRenderer.rect(0f, 0f, w, h)

        // Ancient Obsidian Tablet Plaque
        val boxW = 860f
        val boxH = 460f
        val boxX = (w - boxW) / 2f
        val boxY = (h - boxH) / 2f

        shapeRenderer.setColor(0.08f, 0.10f, 0.16f, 0.96f)
        shapeRenderer.rect(boxX, boxY, boxW, boxH)

        // Ancient Gold Border with pulse
        val pulse = (sin(hudAnimTime * 4f) + 1f) * 0.15f
        shapeRenderer.setColor(0.85f + pulse, 0.70f + pulse, 0.25f, 0.9f)
        val borderThick = 2.5f
        shapeRenderer.rectLine(boxX, boxY, boxX + boxW, boxY, borderThick)
        shapeRenderer.rectLine(boxX, boxY + boxH, boxX + boxW, boxY + boxH, borderThick)
        shapeRenderer.rectLine(boxX, boxY, boxX, boxY + boxH, borderThick)
        shapeRenderer.rectLine(boxX + boxW, boxY, boxX + boxW, boxY + boxH, borderThick)

        // Top Rune Symbol Center Circle
        shapeRenderer.setColor(0.3f, 0.75f, 1f, 0.85f)
        shapeRenderer.circle(w / 2f, boxY + boxH - 45f, 22f, 16)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(w / 2f, boxY + boxH - 45f, 10f, 10)

        // Close Button Bar at bottom
        val btnW = 320f
        val btnH = 42f
        val btnX = (w - btnW) / 2f
        val btnY = boxY + 28f
        shapeRenderer.setColor(0.14f, 0.20f, 0.30f, 0.95f)
        shapeRenderer.rect(btnX, btnY, btnW, btnH)
        shapeRenderer.setColor(0.3f, 0.8f, 1f, 0.85f)
        shapeRenderer.rectLine(btnX, btnY, btnX + btnW, btnY, 1.5f)
        shapeRenderer.rectLine(btnX, btnY + btnH, btnX + btnW, btnY + btnH, 1.5f)

        shapeRenderer.end()

        batch.projectionMatrix = uiCamera.combined
        batch.begin()
        val headF = FontManager.headerFont
        val regF = FontManager.regularFont

        // Header Title
        headF.setColor(1f, 0.85f, 0.3f, 1f)
        glyphLayout.setText(headF, tablet.title)
        headF.draw(batch, tablet.title, (w - glyphLayout.width) / 2f, boxY + boxH - 85f)

        // Tablet Lore Body Text
        regF.setColor(0.90f, 0.92f, 0.98f, 0.96f)
        regF.draw(batch, tablet.text, boxX + 60f, boxY + boxH - 145f, boxW - 120f, Align.left, true)

        // Button Label
        headF.setColor(0.4f, 0.9f, 1f, 1f)
        val closeLabel = "[SPACE / TAP] TUTUP PRASASTI"
        glyphLayout.setText(headF, closeLabel)
        headF.draw(batch, closeLabel, (w - glyphLayout.width) / 2f, btnY + 28f)

        batch.end()
    }

    private fun renderStoryDialogueOverlay() {
        val dialogue = activeStoryDialogue ?: return
        val line = dialogue.currentLine ?: return
        val w = 1280f
        val h = 720f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.projectionMatrix = uiCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dialogue Box at screen bottom
        val boxX = 100f
        val boxY = 40f
        val boxW = 1080f
        val boxH = 175f

        shapeRenderer.setColor(0.06f, 0.08f, 0.14f, 0.95f)
        shapeRenderer.rect(boxX, boxY, boxW, boxH)

        // Border colored with speaker color
        shapeRenderer.setColor(line.speakerColor.r, line.speakerColor.g, line.speakerColor.b, 0.9f)
        shapeRenderer.rectLine(boxX, boxY + boxH, boxX + boxW, boxY + boxH, 2.5f)
        shapeRenderer.rectLine(boxX, boxY, boxX + boxW, boxY, 2f)
        shapeRenderer.rectLine(boxX, boxY, boxX, boxY + boxH, 2f)
        shapeRenderer.rectLine(boxX + boxW, boxY, boxX + boxW, boxY + boxH, 2f)

        // Speaker Tag Box (Dynamic sizing based on title length for zero text clipping)
        val headF = FontManager.headerFont
        val regF = FontManager.regularFont
        val speakerTag = "${line.speaker.uppercase()}  •  ${line.title}"
        glyphLayout.setText(headF, speakerTag)
        val tagW = kotlin.math.max(320f, glyphLayout.width + 36f)
        val tagH = 38f
        val tagX = boxX + 25f
        val tagY = boxY + boxH - 6f
        shapeRenderer.setColor(0.12f, 0.16f, 0.24f, 0.98f)
        shapeRenderer.rect(tagX, tagY, tagW, tagH)
        shapeRenderer.setColor(line.speakerColor)
        shapeRenderer.rectLine(tagX, tagY + tagH, tagX + tagW, tagY + tagH, 2.5f)
        shapeRenderer.rectLine(tagX, tagY, tagX, tagY + tagH, 1.5f)
        shapeRenderer.rectLine(tagX + tagW, tagY, tagX + tagW, tagY + tagH, 1.5f)

        shapeRenderer.end()

        batch.projectionMatrix = uiCamera.combined
        batch.begin()

        // Speaker Name
        headF.setColor(line.speakerColor)
        headF.draw(batch, speakerTag, tagX + 18f, tagY + tagH - 12f)

        // Dialogue Text
        regF.setColor(0.95f, 0.96f, 1f, 1f)
        regF.draw(batch, line.text, boxX + 40f, boxY + boxH - 35f, boxW - 80f, Align.left, true)

        // Advance Prompt (Right-aligned with pulsing glow)
        val blink = (sin(hudAnimTime * 6f) + 1f) * 0.5f
        regF.setColor(1f, 0.85f, 0.3f, 0.65f + 0.35f * blink)
        val promptText = "LANJUT [TAP / SPACE] ▶"
        glyphLayout.setText(regF, promptText)
        regF.draw(batch, promptText, boxX + boxW - glyphLayout.width - 32f, boxY + 28f)

        batch.end()
    }

    private fun renderActBannerShapes() {
        if (actBannerTimer <= 0f) return
        val w = 1280f

        val alpha = if (actBannerTimer > 2.8f) {
            (3.5f - actBannerTimer) / 0.7f
        } else if (actBannerTimer < 0.7f) {
            actBannerTimer / 0.7f
        } else {
            1.0f
        }.coerceIn(0f, 1f)

        if (alpha <= 0.01f) return

        // Background banner strip
        val bannerY = 590f
        val bannerH = 85f
        shapeRenderer.setColor(0.04f, 0.06f, 0.10f, 0.80f * alpha)
        shapeRenderer.rect(0f, bannerY, w, bannerH)

        // Golden glow accent lines
        shapeRenderer.setColor(0.85f, 0.70f, 0.25f, 0.85f * alpha)
        shapeRenderer.rectLine(0f, bannerY, w, bannerY, 1.5f)
        shapeRenderer.rectLine(0f, bannerY + bannerH, w, bannerY + bannerH, 1.5f)
    }

    private fun renderActBannerText() {
        if (actBannerTimer <= 0f) return
        val w = 1280f

        val alpha = if (actBannerTimer > 2.8f) {
            (3.5f - actBannerTimer) / 0.7f
        } else if (actBannerTimer < 0.7f) {
            actBannerTimer / 0.7f
        } else {
            1.0f
        }.coerceIn(0f, 1f)

        if (alpha <= 0.01f) return

        val bannerY = 590f
        val bannerH = 85f
        val titleF = FontManager.titleFont
        val regF = FontManager.regularFont

        titleF.setColor(1f, 0.85f, 0.3f, alpha)
        val actTitle = currentFloor.actTitle
        glyphLayout.setText(titleF, actTitle)
        titleF.draw(batch, actTitle, (w - glyphLayout.width) / 2f, bannerY + bannerH - 18f)

        regF.setColor(0.75f, 0.85f, 1f, alpha)
        val actSub = currentFloor.actSubtitle
        glyphLayout.setText(regF, actSub)
        regF.draw(batch, actSub, (w - glyphLayout.width) / 2f, bannerY + 28f)
    }

    private fun renderVulcanusNPC(shapeRenderer: ShapeRenderer) {
        val anvilX = 10.5f
        val anvilY = 2.0f
        val vulcanusX = 9.8f
        val vulcanusY = 2.0f

        // Forge Anvil Iron Block
        shapeRenderer.setColor(0.20f, 0.22f, 0.26f, 1f)
        shapeRenderer.rect(anvilX - 0.4f, anvilY, 0.8f, 0.7f)
        shapeRenderer.rect(anvilX - 0.6f, anvilY + 0.5f, 1.2f, 0.25f)

        // Ember Glow on Anvil
        val emberPulse = (sin(hudAnimTime * 4f) + 1f) * 0.5f
        shapeRenderer.setColor(1f, 0.45f + 0.2f * emberPulse, 0.1f, 0.75f)
        shapeRenderer.circle(anvilX, anvilY + 0.76f, 0.14f, 10)

        // Vulcanus Legs
        shapeRenderer.setColor(0.12f, 0.08f, 0.06f, 1f)
        shapeRenderer.rect(vulcanusX - 0.22f, vulcanusY, 0.16f, 0.9f)
        shapeRenderer.rect(vulcanusX + 0.06f, vulcanusY, 0.16f, 0.9f)

        // Torso & Leather Apron
        shapeRenderer.setColor(0.38f, 0.20f, 0.10f, 1f)
        shapeRenderer.rect(vulcanusX - 0.35f, vulcanusY + 0.9f, 0.7f, 1.1f)

        // Head & Headband
        shapeRenderer.setColor(0.15f, 0.12f, 0.10f, 1f)
        shapeRenderer.circle(vulcanusX, vulcanusY + 2.25f, 0.26f, 14)
        shapeRenderer.setColor(1.0f, 0.55f, 0.15f, 1f)
        shapeRenderer.rect(vulcanusX - 0.28f, vulcanusY + 2.22f, 0.56f, 0.08f)

        // Massive Sledgehammer resting on anvil
        shapeRenderer.setColor(0.45f, 0.30f, 0.15f, 1f)
        shapeRenderer.rectLine(vulcanusX + 0.2f, vulcanusY + 1.4f, anvilX, anvilY + 0.8f, 0.08f)
        shapeRenderer.setColor(0.5f, 0.52f, 0.58f, 1f)
        shapeRenderer.rect(anvilX - 0.15f, anvilY + 0.75f, 0.35f, 0.22f)
    }

    private fun renderAriaNPC(shapeRenderer: ShapeRenderer) {
        val ariaX = 2.0f
        val hoverY = 1.6f + sin(hudAnimTime * 3f) * 0.12f
        val pulse = (sin(hudAnimTime * 5f) + 1f) * 0.5f

        // Golden Anima Spirit Aura
        shapeRenderer.setColor(1f, 0.85f, 0.25f, 0.35f + 0.25f * pulse)
        shapeRenderer.circle(ariaX, hoverY, 0.42f, 16)

        // Inner glowing soul sphere
        shapeRenderer.setColor(1f, 0.92f, 0.55f, 0.85f)
        shapeRenderer.circle(ariaX, hoverY, 0.22f, 14)

        // Center white core
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(ariaX, hoverY, 0.10f, 10)
    }

    private fun renderAchievementBannerShapes() {
        val banner = activeAchievementBanner ?: return
        val w = 1280f
        val bw = 460f
        val bh = 56f
        val bx = (w - bw) / 2f
        val by = 645f

        // Pill Latar Belakang Glassmorphism
        shapeRenderer.setColor(0.06f, 0.08f, 0.14f, 0.95f)
        shapeRenderer.rect(bx, by, bw, bh)

        // Border Emas Menyala dengan Efek Denyut
        val pulse = kotlin.math.sin(hudAnimTime.toDouble() * 6.0).toFloat() * 0.1f
        shapeRenderer.setColor(0.95f, 0.82f, 0.25f, 0.88f + pulse)
        shapeRenderer.rectLine(bx, by, bx + bw, by, 2f)
        shapeRenderer.rectLine(bx, by + bh, bx + bw, by + bh, 2f)
        shapeRenderer.rectLine(bx, by, bx, by + bh, 3f)
        shapeRenderer.rectLine(bx + bw, by, bx + bw, by + bh, 3f)

        // Badge Kotak Trofi
        shapeRenderer.setColor(0.85f, 0.65f, 0.15f, 0.35f)
        shapeRenderer.rect(bx + 8f, by + 8f, 40f, 40f)
    }

    private fun renderAchievementBannerText() {
        val banner = activeAchievementBanner ?: return
        val w = 1280f
        val bw = 460f
        val bx = (w - bw) / 2f
        val by = 645f

        val headF = if (FontManager.isInitialized) FontManager.headerFont else BitmapFont()
        val regF = if (FontManager.isInitialized) FontManager.regularFont else BitmapFont()

        headF.setColor(1f, 0.88f, 0.25f, 1f)
        headF.draw(batch, "★", bx + 19f, by + 36f)

        headF.setColor(1f, 0.85f, 0.3f, 1f)
        headF.draw(batch, "PRESTASI TERBUKA!", bx + 60f, by + 44f)

        regF.setColor(0.9f, 0.95f, 1f, 1f)
        val textDesc = "${banner.title} (+${banner.goldReward} Gold)"
        regF.draw(batch, textDesc, bx + 60f, by + 22f)
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
        particleManager.clear()
        shapeRenderer.dispose()
        batch.dispose()
    }
}

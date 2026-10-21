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
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.audio.HapticManager
import com.stickman.tower.bounty.BountyContract
import com.stickman.tower.bounty.BountyManager
import com.stickman.tower.bounty.BountyRegistry
import com.stickman.tower.bounty.BountyStatus
import com.stickman.tower.challenge.ChallengeDefinition
import com.stickman.tower.challenge.ChallengeManager
import com.stickman.tower.challenge.ChallengeMedal
import com.stickman.tower.challenge.ChallengeRegistry
import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.ui.FontManager
import kotlin.math.sin

/**
 * Screen Terpadu Arena Ujian Para Kaligrafer & Papan Buronan Elit:
 * Memfasilitasi pemain memilih dan menjalankan Ujian Khusus (The Scribe's Gauntlets)
 * serta mengikat Kontrak Perburuan Monster Elit berafiks mutator (The Spire Bounty Board).
 */
class GauntletScreen(
    private val game: StickmanGame?,
    private val stats: CharacterStats? = null,
    private val returnScreen: Screen? = null
) : Screen {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(1280f, 720f, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val glyphLayout = GlyphLayout()
    private val touchVec = Vector3()

    enum class ScreenTab(val title: String) {
        GAUNTLET("🏛️ Monolit Ujian"),
        BOUNTY("☠ Papan Buronan")
    }

    private var activeTab = ScreenTab.GAUNTLET
    private var selectedIndex = 0
    private var pulseTimer = 0f

    // Tab Buttons
    private val btnTabGauntlet = Rectangle(100f, 600f, 260f, 48f)
    private val btnTabBounty = Rectangle(375f, 600f, 260f, 48f)

    // Action Buttons
    private val btnBack = Rectangle(40f, 35f, 180f, 50f)
    private val btnAction = Rectangle(820f, 45f, 360f, 55f)

    // Fonts
    private val titleFont: BitmapFont = FontManager.titleFont
    private val headerFont: BitmapFont = FontManager.headerFont
    private val regularFont: BitmapFont = FontManager.regularFont
    private val smallFont: BitmapFont = FontManager.smallFont

    override fun show() {
        val currentSave = SaveManager.load()
        ChallengeManager.loadFromSaveData(currentSave)
        BountyManager.loadFromSaveData(currentSave)
        selectedIndex = 0
    }

    override fun render(delta: Float) {
        pulseTimer += delta
        handleInput()

        ScreenUtils.clear(0.05f, 0.06f, 0.09f, 1f)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        batch.projectionMatrix = camera.combined

        // 1. SHAPE RENDERER PASS
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Panel Kiri (Daftar Tantangan / Buronan)
        shapeRenderer.setColor(0.09f, 0.11f, 0.16f, 0.90f)
        shapeRenderer.rect(100f, 100f, 440f, 480f)

        // Panel Kanan (Detail Terpilih)
        shapeRenderer.setColor(0.11f, 0.14f, 0.20f, 0.92f)
        shapeRenderer.rect(560f, 100f, 620f, 480f)

        // Tab Navigation Buttons
        drawTabButton(btnTabGauntlet, activeTab == ScreenTab.GAUNTLET)
        drawTabButton(btnTabBounty, activeTab == ScreenTab.BOUNTY)

        // Tombol Kembali
        shapeRenderer.setColor(0.18f, 0.22f, 0.32f, 0.90f)
        shapeRenderer.rect(btnBack.x, btnBack.y, btnBack.width, btnBack.height)

        // Render Cards pada Panel Kiri
        when (activeTab) {
            ScreenTab.GAUNTLET -> renderGauntletCardsShapes()
            ScreenTab.BOUNTY -> renderBountyCardsShapes()
        }

        // Render Action Button Shape
        renderActionButtonShape()

        shapeRenderer.end()

        // 2. SPRITE BATCH TEXT PASS
        batch.begin()

        // Header Title
        titleFont.setColor(0.96f, 0.82f, 0.35f, 1f)
        titleFont.draw(batch, "⚔ ARENA UJIAN & PAPAN BURONAN", 100f, 690f)

        // Tab Text
        drawTabText(btnTabGauntlet, "🏛️ Monolit Ujian", activeTab == ScreenTab.GAUNTLET)
        drawTabText(btnTabBounty, "☠ Papan Buronan", activeTab == ScreenTab.BOUNTY)

        // Tombol Kembali Text
        regularFont.color = Color.WHITE
        regularFont.draw(batch, "◀ KEMBALI [B]", btnBack.x + 25f, btnBack.y + 32f)

        // Render List & Details Text
        when (activeTab) {
            ScreenTab.GAUNTLET -> {
                renderGauntletCardsText()
                val list = ChallengeRegistry.getAll()
                if (selectedIndex in list.indices) {
                    renderGauntletDetails(list[selectedIndex])
                }
            }
            ScreenTab.BOUNTY -> {
                renderBountyCardsText()
                val list = BountyRegistry.getAll()
                if (selectedIndex in list.indices) {
                    renderBountyDetails(list[selectedIndex])
                }
            }
        }

        // Render Action Button Text
        renderActionButtonText()

        batch.end()
    }

    private fun drawTabButton(rect: Rectangle, isActive: Boolean) {
        if (isActive) {
            shapeRenderer.setColor(0.24f, 0.30f, 0.44f, 0.95f)
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
            shapeRenderer.setColor(0.95f, 0.80f, 0.30f, 1f)
            shapeRenderer.rect(rect.x, rect.y, rect.width, 4f)
        } else {
            shapeRenderer.setColor(0.12f, 0.15f, 0.22f, 0.70f)
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
        }
    }

    private fun drawTabText(rect: Rectangle, label: String, isActive: Boolean) {
        if (isActive) {
            regularFont.setColor(0.95f, 0.85f, 0.40f, 1f)
        } else {
            regularFont.setColor(0.60f, 0.65f, 0.75f, 0.8f)
        }
        regularFont.draw(batch, label, rect.x + 25f, rect.y + 32f)
    }

    private fun renderGauntletCardsShapes() {
        val list = ChallengeRegistry.getAll()
        for (i in list.indices) {
            val itemY = 520f - (i * 74f)
            if (itemY < 110f) break

            val isSelected = (i == selectedIndex)
            if (isSelected) {
                shapeRenderer.setColor(0.22f, 0.28f, 0.42f, 0.90f)
                shapeRenderer.rect(108f, itemY, 424f, 66f)
                shapeRenderer.setColor(0.95f, 0.80f, 0.30f, 1f)
                shapeRenderer.rect(108f, itemY, 4f, 66f)
            } else {
                shapeRenderer.setColor(0.13f, 0.16f, 0.23f, 0.65f)
                shapeRenderer.rect(108f, itemY, 424f, 66f)
            }
        }
    }

    private fun renderGauntletCardsText() {
        val list = ChallengeRegistry.getAll()
        for (i in list.indices) {
            val itemY = 520f - (i * 74f)
            if (itemY < 110f) break

            val c = list[i]
            val bestMedal = ChallengeManager.getBestMedal(c.id)
            val isSelected = (i == selectedIndex)

            regularFont.color = if (isSelected) Color.WHITE else Color.LIGHT_GRAY
            regularFont.draw(batch, "${bestMedal.symbol} ${c.title}", 125f, itemY + 48f)

            smallFont.setColor(0.70f, 0.78f, 0.90f, 0.9f)
            smallFont.draw(batch, "Lantai ${c.targetFloor} • Rekor: ${bestMedal.displayName}", 125f, itemY + 24f)
        }
    }

    private fun renderBountyCardsShapes() {
        val list = BountyRegistry.getAll()
        for (i in list.indices) {
            val itemY = 520f - (i * 74f)
            if (itemY < 110f) break

            val isSelected = (i == selectedIndex)
            if (isSelected) {
                shapeRenderer.setColor(0.25f, 0.20f, 0.35f, 0.90f)
                shapeRenderer.rect(108f, itemY, 424f, 66f)
                shapeRenderer.setColor(0.90f, 0.45f, 0.35f, 1f)
                shapeRenderer.rect(108f, itemY, 4f, 66f)
            } else {
                shapeRenderer.setColor(0.13f, 0.15f, 0.22f, 0.65f)
                shapeRenderer.rect(108f, itemY, 424f, 66f)
            }
        }
    }

    private fun renderBountyCardsText() {
        val list = BountyRegistry.getAll()
        for (i in list.indices) {
            val itemY = 520f - (i * 74f)
            if (itemY < 110f) break

            val b = list[i]
            val status = BountyManager.getStatus(b.id)
            val isSelected = (i == selectedIndex)

            val statusColor = when (status) {
                BountyStatus.AVAILABLE -> Color.LIGHT_GRAY
                BountyStatus.ACCEPTED -> Color.ORANGE
                BountyStatus.SLAIN -> Color.GREEN
                BountyStatus.CLAIMED -> Color.GRAY
            }

            regularFont.color = if (isSelected) Color.WHITE else Color.LIGHT_GRAY
            val runes = b.affixes.joinToString("") { it.rune }
            regularFont.draw(batch, "$runes ${b.targetName}", 125f, itemY + 48f)

            smallFont.color = statusColor
            smallFont.draw(batch, "Lantai ${b.targetFloor} • [${status.label}]", 125f, itemY + 24f)
        }
    }

    private fun renderGauntletDetails(c: ChallengeDefinition) {
        val startX = 585f
        var curY = 555f

        // Kategori & Subtitle
        smallFont.setColor(0.95f, 0.80f, 0.30f, 1f)
        smallFont.draw(batch, "UJIAN KALIGRAFER  |  LANTAI ${c.targetFloor}  |  ${c.subtitle.uppercase()}", startX, curY)
        curY -= 25f

        // Title
        headerFont.color = Color.WHITE
        headerFont.draw(batch, c.title, startX, curY)
        curY -= 32f

        // Lore Synopsis
        smallFont.setColor(0.80f, 0.85f, 0.90f, 1f)
        glyphLayout.setText(smallFont, c.synopsis, Color.WHITE, 570f, 0, true)
        smallFont.draw(batch, c.synopsis, startX, curY, 570f, 0, true)
        curY -= glyphLayout.height + 18f

        // Handicap Warning
        regularFont.setColor(0.95f, 0.35f, 0.35f, 1f)
        regularFont.draw(batch, "⚠️ KETENTUAN HANDICAP:", startX, curY)
        curY -= 20f

        smallFont.setColor(1.0f, 0.80f, 0.80f, 0.95f)
        glyphLayout.setText(smallFont, c.handicapDescription, Color.WHITE, 570f, 0, true)
        smallFont.draw(batch, c.handicapDescription, startX, curY, 570f, 0, true)
        curY -= glyphLayout.height + 20f

        // Thresholds
        regularFont.setColor(0.95f, 0.82f, 0.35f, 1f)
        regularFont.draw(batch, "━━ SYARAT MEDALI ━━", startX, curY)
        curY -= 24f

        smallFont.setColor(1.0f, 0.84f, 0.0f, 1f)
        smallFont.draw(batch, "🥇 Medali Emas: Skor ≥ ${c.goldThreshold}", startX, curY)
        curY -= 20f

        smallFont.setColor(0.75f, 0.75f, 0.75f, 1f)
        smallFont.draw(batch, "🥈 Medali Perak: Skor ≥ ${c.silverThreshold}", startX, curY)
        curY -= 20f

        smallFont.setColor(0.80f, 0.50f, 0.20f, 1f)
        smallFont.draw(batch, "🥉 Medali Perunggu: Skor ≥ ${c.bronzeThreshold}", startX, curY)
        curY -= 25f

        // Hadiah
        regularFont.setColor(0.40f, 0.85f, 0.95f, 1f)
        regularFont.draw(batch, "━━ IMBALAN KELULUSAN ━━", startX, curY)
        curY -= 24f

        val relicText = c.relicReward?.let { " + Relik ${it.title}" } ?: ""
        val tpText = if (c.talentPointsReward > 0) " + ${c.talentPointsReward} Talent Point" else ""
        smallFont.setColor(0.90f, 0.90f, 0.95f, 1f)
        smallFont.draw(batch, "💰 +${c.goldReward} Gold  •  ✨ +${c.expReward} EXP$relicText$tpText", startX, curY)
    }

    private fun renderBountyDetails(b: BountyContract) {
        val startX = 585f
        var curY = 555f

        // Kategori & Subtitle
        smallFont.setColor(0.95f, 0.50f, 0.35f, 1f)
        smallFont.draw(batch, "KONTRAK BURONAN ELIT  |  LANTAI ${b.targetFloor}", startX, curY)
        curY -= 25f

        // Target Name & Title
        headerFont.color = Color.WHITE
        headerFont.draw(batch, b.targetName, startX, curY)
        curY -= 25f

        smallFont.setColor(0.85f, 0.70f, 0.55f, 1f)
        smallFont.draw(batch, b.targetTitle, startX, curY)
        curY -= 28f

        // Synopsis Lore
        smallFont.setColor(0.80f, 0.85f, 0.90f, 1f)
        glyphLayout.setText(smallFont, b.synopsis, Color.WHITE, 570f, 0, true)
        smallFont.draw(batch, b.synopsis, startX, curY, 570f, 0, true)
        curY -= glyphLayout.height + 20f

        // Modifikator Mutasi (Affixes)
        regularFont.setColor(0.95f, 0.40f, 0.40f, 1f)
        regularFont.draw(batch, "━━ MUTASI AFIKS ELIT ━━", startX, curY)
        curY -= 24f

        for (affix in b.affixes) {
            smallFont.setColor(0.95f, 0.80f, 0.30f, 1f)
            smallFont.draw(batch, "${affix.rune} ${affix.title}:", startX, curY)
            curY -= 18f

            smallFont.setColor(0.85f, 0.85f, 0.90f, 0.9f)
            smallFont.draw(batch, "   ${affix.description}", startX, curY)
            curY -= 22f
        }

        // Imbalan
        curY -= 5f
        regularFont.setColor(0.40f, 0.85f, 0.95f, 1f)
        regularFont.draw(batch, "━━ IMBALAN PERBURUAN ━━", startX, curY)
        curY -= 24f

        smallFont.setColor(0.90f, 0.90f, 0.95f, 1f)
        smallFont.draw(batch, "💰 +${b.rewardGold} Gold  •  ✨ +${b.rewardExp} EXP", startX, curY)
        curY -= 20f

        smallFont.setColor(0.35f, 0.90f, 0.65f, 1f)
        smallFont.draw(batch, "🎁 Bonus: ${b.bonusRewardDesc}", startX, curY)
    }

    private fun renderActionButtonShape() {
        val pulse = (sin(pulseTimer * 6f) + 1f) * 0.5f

        when (activeTab) {
            ScreenTab.GAUNTLET -> {
                val list = ChallengeRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val c = list[selectedIndex]
                    if (ChallengeManager.canClaim(c.id)) {
                        shapeRenderer.setColor(0.2f + 0.1f * pulse, 0.75f + 0.15f * pulse, 0.3f, 0.95f)
                        shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                    } else {
                        shapeRenderer.setColor(0.25f, 0.35f, 0.55f, 0.90f)
                        shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                    }
                }
            }
            ScreenTab.BOUNTY -> {
                val list = BountyRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val b = list[selectedIndex]
                    when (BountyManager.getStatus(b.id)) {
                        BountyStatus.AVAILABLE -> {
                            shapeRenderer.setColor(0.75f, 0.40f, 0.15f, 0.95f)
                            shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                        }
                        BountyStatus.ACCEPTED -> {
                            shapeRenderer.setColor(0.30f, 0.35f, 0.45f, 0.75f)
                            shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                        }
                        BountyStatus.SLAIN -> {
                            shapeRenderer.setColor(0.2f + 0.1f * pulse, 0.75f + 0.15f * pulse, 0.3f, 0.95f)
                            shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                        }
                        BountyStatus.CLAIMED -> {
                            shapeRenderer.setColor(0.20f, 0.25f, 0.22f, 0.60f)
                            shapeRenderer.rect(btnAction.x, btnAction.y, btnAction.width, btnAction.height)
                        }
                    }
                }
            }
        }
    }

    private fun renderActionButtonText() {
        regularFont.color = Color.WHITE

        when (activeTab) {
            ScreenTab.GAUNTLET -> {
                val list = ChallengeRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val c = list[selectedIndex]
                    if (ChallengeManager.canClaim(c.id)) {
                        regularFont.draw(batch, "★ KLAIM HADIAH UJIAN", btnAction.x + 50f, btnAction.y + 35f)
                    } else if (ChallengeManager.isClaimed(c.id)) {
                        regularFont.setColor(0.7f, 0.9f, 0.7f, 0.9f)
                        regularFont.draw(batch, "✓ HADIAH TELAH DIKLAIM", btnAction.x + 40f, btnAction.y + 35f)
                    } else {
                        regularFont.draw(batch, "⚔ IKUTI UJIAN DI LANTAI ${c.targetFloor}", btnAction.x + 35f, btnAction.y + 35f)
                    }
                }
            }
            ScreenTab.BOUNTY -> {
                val list = BountyRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val b = list[selectedIndex]
                    when (BountyManager.getStatus(b.id)) {
                        BountyStatus.AVAILABLE -> {
                            regularFont.draw(batch, "📜 TERIMA KONTRAK BURONAN", btnAction.x + 35f, btnAction.y + 35f)
                        }
                        BountyStatus.ACCEPTED -> {
                            regularFont.setColor(0.9f, 0.7f, 0.3f, 1f)
                            regularFont.draw(batch, "⚔ SEDANG DIBURU DI LANTAI ${b.targetFloor}", btnAction.x + 25f, btnAction.y + 35f)
                        }
                        BountyStatus.SLAIN -> {
                            regularFont.draw(batch, "★ KLAIM IMBALAN BURONAN", btnAction.x + 40f, btnAction.y + 35f)
                        }
                        BountyStatus.CLAIMED -> {
                            regularFont.setColor(0.6f, 0.7f, 0.6f, 0.8f)
                            regularFont.draw(batch, "✓ KONTRAK TELAH TUNTAS", btnAction.x + 50f, btnAction.y + 35f)
                        }
                    }
                }
            }
        }
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.B)) {
            AudioManager.playSlashLight()
            goBack()
            return
        }

        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(touchVec)

            if (btnTabGauntlet.contains(touchVec.x, touchVec.y)) {
                if (activeTab != ScreenTab.GAUNTLET) {
                    activeTab = ScreenTab.GAUNTLET
                    selectedIndex = 0
                    AudioManager.playSlashLight()
                    HapticManager.vibrateLight()
                }
            } else if (btnTabBounty.contains(touchVec.x, touchVec.y)) {
                if (activeTab != ScreenTab.BOUNTY) {
                    activeTab = ScreenTab.BOUNTY
                    selectedIndex = 0
                    AudioManager.playSlashLight()
                    HapticManager.vibrateLight()
                }
            } else if (btnBack.contains(touchVec.x, touchVec.y)) {
                AudioManager.playSlashLight()
                HapticManager.vibrateMedium()
                goBack()
            } else if (btnAction.contains(touchVec.x, touchVec.y)) {
                handleActionButtonClick()
            } else if (touchVec.x in 100f..540f && touchVec.y in 100f..580f) {
                // Pilih item di panel kiri
                val itemCount = if (activeTab == ScreenTab.GAUNTLET) ChallengeRegistry.getAll().size else BountyRegistry.getAll().size
                for (i in 0 until itemCount) {
                    val itemY = 520f - (i * 74f)
                    if (touchVec.y in itemY..(itemY + 66f)) {
                        selectedIndex = i
                        AudioManager.playSlashLight()
                        HapticManager.vibrateLight()
                        break
                    }
                }
            }
        }
    }

    private fun handleActionButtonClick() {
        when (activeTab) {
            ScreenTab.GAUNTLET -> {
                val list = ChallengeRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val c = list[selectedIndex]
                    if (ChallengeManager.canClaim(c.id)) {
                        val save = SaveManager.load()
                        ChallengeManager.claimReward(c.id, save) { gold, exp, _, tp ->
                            stats?.addGold(gold)
                            stats?.addExp(exp)
                            SaveManager.save(save)
                            AudioManager.playLevelUp()
                            HapticManager.vibrateMedium()
                        }
                    }
                }
            }
            ScreenTab.BOUNTY -> {
                val list = BountyRegistry.getAll()
                if (selectedIndex in list.indices) {
                    val b = list[selectedIndex]
                    val status = BountyManager.getStatus(b.id)
                    val save = SaveManager.load()

                    if (status == BountyStatus.AVAILABLE) {
                        BountyManager.acceptContract(b.id)
                        BountyManager.syncToSaveData(save)
                        SaveManager.save(save)
                        AudioManager.playSlashLight()
                        HapticManager.vibrateMedium()
                    } else if (status == BountyStatus.SLAIN) {
                        BountyManager.claimReward(b.id, save) { gold, exp, _ ->
                            stats?.addGold(gold)
                            stats?.addExp(exp)
                            SaveManager.save(save)
                            AudioManager.playLevelUp()
                            HapticManager.vibrateMedium()
                        }
                    }
                }
            }
        }
    }

    private fun goBack() {
        if (returnScreen != null) {
            game?.screen = returnScreen
        } else if (game != null) {
            game.screen = MainMenuScreen(game)
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
    }
}

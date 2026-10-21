package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.character.CharacterArchetype
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.ui.FontManager
import kotlin.math.sin

/**
 * Screen Pemilihan Karakter:
 * Menampilkan preview stickman beranimasi di atas altar runic dengan senjata khasnya,
 * kartu statistik lengkap (Speed, Attack, Defense, Crit, Skill, Ult),
 * serta navigasi vektor panah dan tipografi FreeType anti-aliased.
 */
class CharacterSelectScreen(private val game: StickmanGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(1280f, 720f, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val glyphLayout = GlyphLayout()

    private val archetypes = CharacterArchetype.entries.toTypedArray()
    private var selectedIndex = 0
    private var animTime = 0f

    override fun show() {
        FontManager.init()
        val currentSave = SaveManager.load()
        selectedIndex = archetypes.indexOfFirst { it.name.equals(currentSave.selectedArchetype, ignoreCase = true) }
            .coerceAtLeast(0)
    }

    override fun render(delta: Float) {
        animTime += delta
        handleInput()

        ScreenUtils.clear(0.06f, 0.08f, 0.14f, 1f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        batch.projectionMatrix = camera.combined

        val currentArchetype = archetypes[selectedIndex]

        // 1. Gambar Geometri & Podium
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Latar Belakang Kartu Kanan (Statistik & Kemampuan)
        shapeRenderer.color = Color(0.06f, 0.09f, 0.15f, 0.92f)
        shapeRenderer.rect(620f, 120f, 560f, 480f)
        shapeRenderer.color = Color(0.9f, 0.75f, 0.25f, 0.85f)
        shapeRenderer.rect(620f, 596f, 560f, 3f)

        // Podium Karakter di Kiri (Runic Slate Altar)
        val podiumCenterX = 350f
        val podiumBaseY = 170f
        shapeRenderer.color = Color(0.1f, 0.14f, 0.22f, 0.95f)
        shapeRenderer.rect(podiumCenterX - 180f, podiumBaseY - 24f, 360f, 34f)
        shapeRenderer.color = Color(0.9f, 0.75f, 0.25f, 0.8f)
        shapeRenderer.rectLine(podiumCenterX - 180f, podiumBaseY + 10f, podiumCenterX + 180f, podiumBaseY + 10f, 3f)

        // Cincin Cahaya Aura Halus
        val pulse = sin(animTime * 3f) * 0.08f
        shapeRenderer.color = Color(0.15f, 0.45f, 0.8f, 0.22f + pulse)
        shapeRenderer.circle(podiumCenterX, podiumBaseY + 10f, 85f, 24)

        // Gambar Karakter Stickman Preview di Podium
        renderStickmanPreview(shapeRenderer, podiumCenterX, podiumBaseY + 10f, currentArchetype)

        // Stat Bar Visual di Kartu Kanan
        renderStatBars(shapeRenderer, currentArchetype)

        // Tombol Navigasi Bawah
        // Tombol Select & Play (Gilded Obsidian)
        val btnPlayX = 720f
        val btnPlayY = 45f
        shapeRenderer.color = Color(0.10f, 0.18f, 0.14f, 0.95f)
        shapeRenderer.rect(btnPlayX, btnPlayY, 260f, 52f)
        shapeRenderer.color = Color(0.25f, 0.85f, 0.45f, 0.9f)
        shapeRenderer.rectLine(btnPlayX, btnPlayY + 52f, btnPlayX + 260f, btnPlayY + 52f, 3f)

        // Tombol Back (Muted Obsidian)
        val btnBackX = 300f
        val btnBackY = 45f
        shapeRenderer.color = Color(0.12f, 0.15f, 0.22f, 0.95f)
        shapeRenderer.rect(btnBackX, btnBackY, 220f, 52f)
        shapeRenderer.color = Color(0.6f, 0.7f, 0.85f, 0.8f)
        shapeRenderer.rectLine(btnBackX, btnBackY + 52f, btnBackX + 220f, btnBackY + 52f, 3f)

        // Tombol Panah Kiri / Kanan Podium (Vektor Segitiga)
        shapeRenderer.color = Color(0.12f, 0.16f, 0.25f, 0.85f)
        shapeRenderer.rect(podiumCenterX - 230f, 320f, 44f, 56f)
        shapeRenderer.rect(podiumCenterX + 190f, 320f, 44f, 56f)

        // Vektor Segitiga Panah Kiri
        shapeRenderer.color = Color(1f, 0.85f, 0.25f, 0.95f)
        shapeRenderer.triangle(
            podiumCenterX - 218f, 348f,
            podiumCenterX - 200f, 362f,
            podiumCenterX - 200f, 334f
        )
        // Vektor Segitiga Panah Kanan
        shapeRenderer.triangle(
            podiumCenterX + 222f, 348f,
            podiumCenterX + 204f, 362f,
            podiumCenterX + 204f, 334f
        )

        shapeRenderer.end()

        // 2. Render Tipografi & Teks (FontManager FreeType Anti-Aliased)
        batch.begin()
        val titleF = if (FontManager.isInitialized) FontManager.titleFont else BitmapFont()
        val headF = if (FontManager.isInitialized) FontManager.headerFont else BitmapFont()
        val regF = if (FontManager.isInitialized) FontManager.regularFont else BitmapFont()

        // Judul Atas
        titleF.color = Color.WHITE
        glyphLayout.setText(titleF, "CHOOSE YOUR HERO")
        titleF.draw(batch, "CHOOSE YOUR HERO", (1280f - glyphLayout.width) / 2f, 675f)

        // Nama & Gelar Karakter Terpilih (Kiri Atas Podium)
        headF.color = Color.CYAN
        glyphLayout.setText(headF, currentArchetype.displayName)
        headF.draw(batch, currentArchetype.displayName, podiumCenterX - glyphLayout.width / 2f, 595f)

        regF.color = Color.GOLD
        glyphLayout.setText(regF, currentArchetype.title)
        regF.draw(batch, currentArchetype.title, podiumCenterX - glyphLayout.width / 2f, 560f)

        // Teks Kartu Statistik (Kanan)
        headF.color = Color.WHITE
        headF.draw(batch, "ATTRIBUTES & SKILLS", 650f, 570f)

        regF.color = Color(0.85f, 0.88f, 0.95f, 1f)
        regF.draw(batch, currentArchetype.description, 650f, 525f, 500f, -1, true)

        regF.color = Color.WHITE
        regF.draw(batch, "Movement Speed:", 650f, 435f)
        regF.draw(batch, "Base Attack Multiplier:", 650f, 395f)
        regF.draw(batch, "Defense Bonus:", 650f, 355f)
        regF.draw(batch, "Critical Rate Bonus:", 650f, 315f)

        // Info Kemampuan Unik (Skill & Ult)
        regF.color = Color.CYAN
        regF.draw(batch, "Special Skill: ${currentArchetype.skillName}", 650f, 255f)
        regF.color = Color.GOLD
        regF.draw(batch, "Ultimate: ${currentArchetype.ultName}", 650f, 215f)

        // Teks Tombol Bawah
        headF.color = Color.WHITE
        glyphLayout.setText(headF, "CONFIRM & PLAY")
        headF.draw(batch, "CONFIRM & PLAY", btnPlayX + (260f - glyphLayout.width) / 2f, btnPlayY + 36f)

        regF.color = Color(0.9f, 0.9f, 0.95f, 1f)
        glyphLayout.setText(regF, "< MAIN MENU")
        regF.draw(batch, "< MAIN MENU", btnBackX + (220f - glyphLayout.width) / 2f, btnBackY + 34f)

        batch.end()
    }

    private fun renderStickmanPreview(shapeRenderer: ShapeRenderer, x: Float, y: Float, archetype: CharacterArchetype) {
        val headRadius = 26f
        val headY = y + 175f
        val neckY = headY - headRadius
        val pelvisY = y + 75f
        val shoulderY = neckY - 14f

        val breath = sin(animTime * 3f) * 3f

        // Kepala
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(x, headY + breath, headRadius, 20)

        // Mata
        shapeRenderer.color = Color.DARK_GRAY
        shapeRenderer.circle(x + 10f, headY + breath + 4f, 4f, 8)

        // Tubuh / Tulang Belakang
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rectLine(x, neckY + breath, x, pelvisY, 8f)

        // Kaki Tegak
        shapeRenderer.rectLine(x, pelvisY, x - 28f, y, 7f)
        shapeRenderer.rectLine(x, pelvisY, x + 28f, y, 7f)

        // Lengan & Senjata Berdasarkan Arketipe
        when (archetype) {
            CharacterArchetype.BALANCED -> {
                // Tangan Kiri
                shapeRenderer.rectLine(x, shoulderY + breath, x - 32f, shoulderY - 30f + breath, 6f)
                // Tangan Kanan (Pegang Pedang Tegak)
                val handX = x + 35f
                val handY = shoulderY - 20f + breath
                shapeRenderer.rectLine(x, shoulderY + breath, handX, handY, 6f)

                // Pedang Perak Elegan
                val swordTipX = handX + 25f
                val swordTipY = handY + 110f
                shapeRenderer.color = Color(0.9f, 0.95f, 1f, 1f)
                shapeRenderer.rectLine(handX, handY, swordTipX, swordTipY, 9f)
                // Crossguard
                shapeRenderer.color = Color.GOLD
                shapeRenderer.rectLine(handX - 16f, handY + 16f, handX + 16f, handY + 12f, 7f)
            }
            CharacterArchetype.AGILE -> {
                // Dual Daggers: Pose Siaga Assassin
                // Tangan Kiri (Belati Terbalik)
                val leftHandX = x - 38f
                val leftHandY = shoulderY - 10f + breath
                shapeRenderer.rectLine(x, shoulderY + breath, leftHandX, leftHandY, 6f)
                shapeRenderer.color = Color(0.3f, 0.9f, 1f, 1f)
                shapeRenderer.rectLine(leftHandX, leftHandY, leftHandX - 25f, leftHandY + 45f, 6f)

                // Tangan Kanan (Belati Siaga Depan)
                shapeRenderer.color = Color.WHITE
                val rightHandX = x + 42f
                val rightHandY = shoulderY - 5f + breath
                shapeRenderer.rectLine(x, shoulderY + breath, rightHandX, rightHandY, 6f)
                shapeRenderer.color = Color(0.3f, 0.9f, 1f, 1f)
                shapeRenderer.rectLine(rightHandX, rightHandY, rightHandX + 35f, rightHandY + 35f, 6f)
            }
            CharacterArchetype.HEAVY -> {
                // Colossal Greatsword: Pedang Raksasa di Punggung/Samping
                val handX = x + 32f
                val handY = shoulderY - 25f + breath
                shapeRenderer.rectLine(x, shoulderY + breath, handX, handY, 7f)

                // Bilah Raksasa Slab Tebal
                val swordTipX = handX + 30f
                val swordTipY = handY + 145f
                shapeRenderer.color = Color(0.3f, 0.32f, 0.38f, 1f)
                shapeRenderer.rectLine(handX, handY, swordTipX, swordTipY, 20f)
                // Tepian Berapi
                shapeRenderer.color = Color(1f, 0.5f, 0.1f, 1f)
                shapeRenderer.rectLine(handX, handY, swordTipX, swordTipY, 8f)
                // Crossguard Raksasa Emas
                shapeRenderer.color = Color.GOLD
                shapeRenderer.rectLine(handX - 22f, handY + 20f, handX + 22f, handY + 16f, 10f)
            }
        }
    }

    private fun renderStatBars(shapeRenderer: ShapeRenderer, archetype: CharacterArchetype) {
        val barX = 890f
        val barW = 240f
        val barH = 14f

        fun drawBar(y: Float, fillPct: Float, color: Color) {
            // Latar Belakang Bar
            shapeRenderer.color = Color(0.2f, 0.25f, 0.35f, 1f)
            shapeRenderer.rect(barX, y, barW, barH)
            // Isi Bar
            shapeRenderer.color = color
            shapeRenderer.rect(barX, y, barW * fillPct.coerceIn(0f, 1f), barH)
        }

        // Speed (5.8f to 9.5f)
        val speedPct = ((archetype.moveSpeed - 5f) / 5f)
        drawBar(422f, speedPct, Color.CYAN)

        // Attack Multiplier (0.85f to 1.55f)
        val atkPct = (archetype.baseDamageMultiplier / 1.7f)
        drawBar(382f, atkPct, Color.RED)

        // Defense (-1.0f to +3.5f)
        val defPct = ((archetype.baseDefenseBonus + 1.5f) / 5.5f)
        drawBar(342f, defPct, Color.GREEN)

        // Crit (-0.02f to +0.15f)
        val critPct = ((archetype.baseCritBonus + 0.05f) / 0.25f)
        drawBar(302f, critPct, Color.ORANGE)
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.A)) {
            selectedIndex = (selectedIndex - 1 + archetypes.size) % archetypes.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || Gdx.input.isKeyJustPressed(Keys.D)) {
            selectedIndex = (selectedIndex + 1) % archetypes.size
            AudioManager.playJump()
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            confirmSelection()
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            AudioManager.playDodge()
            game.setScreen(MainMenuScreen(game))
        }

        // Klik Mouse & Layar Sentuh
        if (Gdx.input.justTouched()) {
            val touch = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f), viewport.screenX.toFloat(), viewport.screenY.toFloat(), viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat())

            // Panah Kiri Podium
            if (touch.x in 80f..160f && touch.y in 310f..390f) {
                selectedIndex = (selectedIndex - 1 + archetypes.size) % archetypes.size
                AudioManager.playJump()
            }
            // Panah Kanan Podium
            if (touch.x in 520f..600f && touch.y in 310f..390f) {
                selectedIndex = (selectedIndex + 1) % archetypes.size
                AudioManager.playJump()
            }
            // Tombol Confirm & Play
            if (touch.x in 720f..980f && touch.y in 45f..97f) {
                confirmSelection()
            }
            // Tombol Back
            if (touch.x in 300f..520f && touch.y in 45f..97f) {
                AudioManager.playDodge()
                game.setScreen(MainMenuScreen(game))
            }
        }
    }

    private fun confirmSelection() {
        val selected = archetypes[selectedIndex]
        val save = SaveManager.load()
        save.selectedArchetype = selected.name
        SaveManager.save(save)

        AudioManager.playLevelUp()
        game.setScreen(PlayScreen(game))
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

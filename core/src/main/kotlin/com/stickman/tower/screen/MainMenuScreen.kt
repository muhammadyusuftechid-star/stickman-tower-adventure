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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.character.CharacterArchetype
import com.stickman.tower.rpg.SaveData
import com.stickman.tower.rpg.SaveManager
import com.stickman.tower.ui.FontManager
import kotlin.math.sin

/**
 * Menu Utama Bergaya Dark Fantasy / Spire of Shadows:
 * Menampilkan animasi partikel atmosferik, navigasi tombol keyboard & sentuh/mouse,
 * info profil progresi RPG, opsi audio, dan tipografi anti-aliased FreeType.
 */
class MainMenuScreen(private val game: StickmanGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(1280f, 720f, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val glyphLayout = GlyphLayout()

    // Partikel Latar Belakang (Recycled in-place, zero GC allocation)
    private class MenuParticle(
        val pos: Vector2 = Vector2(),
        val vel: Vector2 = Vector2(),
        var size: Float = 0f,
        var color: Color = Color.WHITE,
        var maxLife: Float = 1f
    ) {
        var life: Float = 0f
        val isExpired: Boolean get() = life >= maxLife

        fun reset(initial: Boolean = false) {
            val x = MathUtils.random(0f, 1280f)
            val y = if (initial) MathUtils.random(0f, 720f) else -10f
            pos.set(x, y)
            val vy = MathUtils.random(25f, 75f)
            val vx = MathUtils.random(-15f, 15f)
            vel.set(vx, vy)
            size = MathUtils.random(2f, 5f)
            val isCyan = MathUtils.randomBoolean(0.6f)
            color = if (isCyan) Color(0.2f, 0.8f, 1f, MathUtils.random(0.3f, 0.7f))
                    else Color(1f, 0.75f, 0.2f, MathUtils.random(0.3f, 0.7f))
            maxLife = MathUtils.random(8f, 16f)
            life = 0f
        }
    }
    private val particles = mutableListOf<MenuParticle>()

    // Tombol Menu
    private val menuItems = listOf(
        "START TOWER",
        "SELECT CHARACTER",
        "AUDIO: ON",
        "RESET SAVE DATA",
        "QUIT GAME"
    )
    private var selectedIndex = 0
    private var animTime = 0f
    private var feedbackNotice = ""
    private var feedbackTimer = 0f

    private var currentSaveData: SaveData = SaveManager.load()

    override fun show() {
        AudioManager.init()
        FontManager.init()
        currentSaveData = SaveManager.load()
        particles.clear()
        for (i in 0 until 45) {
            val p = MenuParticle()
            p.reset(initial = true)
            particles.add(p)
        }
    }

    override fun render(delta: Float) {
        animTime += delta
        if (feedbackTimer > 0f) {
            feedbackTimer -= delta
            if (feedbackTimer <= 0f) feedbackNotice = ""
        }

        handleInput()
        updateParticles(delta)

        ScreenUtils.clear(0.05f, 0.07f, 0.12f, 1f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        batch.projectionMatrix = camera.combined

        // 1. Gambar Partikel Latar Belakang
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (p in particles) {
            val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
            shapeRenderer.color = Color(p.color.r, p.color.g, p.color.b, p.color.a * alpha)
            shapeRenderer.circle(p.pos.x, p.pos.y, p.size)
        }

        // Panel Judul & Kartu Status Pemain (Dark Slate Obsidian dengan Gold Line)
        shapeRenderer.color = Color(0.06f, 0.09f, 0.15f, 0.90f)
        shapeRenderer.rect(140f, 40f, 1000f, 120f)
        shapeRenderer.color = Color(0.9f, 0.75f, 0.25f, 0.7f)
        shapeRenderer.rectLine(140f, 158f, 1140f, 158f, 2f)

        // Highlight Tombol Terpilih (Sleek Obsidian dengan Trim Emas)
        val buttonWidth = 420f
        val buttonHeight = 48f
        val startY = 460f
        val spacingY = 62f
        val btnX = (1280f - buttonWidth) / 2f
        val btnY = startY - (selectedIndex * spacingY)

        val pulse = sin(animTime * 6f) * 0.08f
        shapeRenderer.color = Color(0.12f, 0.16f, 0.25f, 0.90f)
        shapeRenderer.rect(btnX - 10f, btnY - 6f, buttonWidth + 20f, buttonHeight + 12f)
        shapeRenderer.color = Color(0.95f, 0.75f, 0.2f, 0.85f + pulse)
        shapeRenderer.rectLine(btnX - 10f, btnY - 6f, btnX + buttonWidth + 10f, btnY - 6f, 2f)
        shapeRenderer.rectLine(btnX - 10f, btnY + buttonHeight + 6f, btnX + buttonWidth + 10f, btnY + buttonHeight + 6f, 2f)
        shapeRenderer.rectLine(btnX - 10f, btnY - 6f, btnX - 10f, btnY + buttonHeight + 6f, 3f)
        shapeRenderer.rectLine(btnX + buttonWidth + 10f, btnY - 6f, btnX + buttonWidth + 10f, btnY + buttonHeight + 6f, 3f)

        shapeRenderer.end()

        // 2. Render Teks & Tipografi (FontManager FreeType Anti-Aliased)
        batch.begin()

        val titleF = if (FontManager.isInitialized) FontManager.titleFont else BitmapFont()
        val headF = if (FontManager.isInitialized) FontManager.headerFont else BitmapFont()
        val regF = if (FontManager.isInitialized) FontManager.regularFont else BitmapFont()

        // Judul Utama
        titleF.color = Color.WHITE
        glyphLayout.setText(titleF, "STICKMAN TOWER ADVENTURE")
        titleF.draw(batch, "STICKMAN TOWER ADVENTURE", (1280f - glyphLayout.width) / 2f, 650f)

        // Subtitle Emas
        regF.color = Color.GOLD
        val subtitle = "Ascend the Spire of Shadows • 2D Action RPG"
        glyphLayout.setText(regF, subtitle)
        regF.draw(batch, subtitle, (1280f - glyphLayout.width) / 2f, 595f)

        // Tombol-tombol Navigasi
        for (i in menuItems.indices) {
            val itemText = if (i == 2) {
                if (AudioManager.isMuted) "AUDIO: MUTED" else "AUDIO: ON"
            } else {
                menuItems[i]
            }

            val curY = startY - (i * spacingY) + 32f
            if (i == selectedIndex) {
                headF.color = Color(1f, 0.85f, 0.25f, 1f)
                val markedText = "⚔  $itemText  ⚔"
                glyphLayout.setText(headF, markedText)
                headF.draw(batch, markedText, (1280f - glyphLayout.width) / 2f, curY)
            } else {
                headF.color = Color(0.82f, 0.86f, 0.95f, 0.9f)
                glyphLayout.setText(headF, itemText)
                headF.draw(batch, itemText, (1280f - glyphLayout.width) / 2f, curY)
            }
        }

        // Kartu Profil Pemain
        val currentArchetype = CharacterArchetype.fromString(currentSaveData.selectedArchetype)
        regF.color = Color.CYAN
        regF.draw(batch, "HERO STATUS:", 170f, 135f)
        regF.color = Color.WHITE
        regF.draw(batch, "Level: ${currentSaveData.playerLevel}", 170f, 95f)
        regF.draw(batch, "Gold: ${currentSaveData.gold} G", 360f, 95f)
        regF.draw(batch, "Highest Floor: Floor ${currentSaveData.highestTowerFloor}", 540f, 95f)
        regF.color = Color.GOLD
        regF.draw(batch, "Archetype: ${currentArchetype.displayName}", 780f, 95f)

        // Feedback / Notifikasi aksi
        if (feedbackNotice.isNotEmpty()) {
            regF.color = Color.GREEN
            glyphLayout.setText(regF, feedbackNotice)
            regF.draw(batch, feedbackNotice, (1280f - glyphLayout.width) / 2f, 195f)
        }

        // Petunjuk Kontrol
        regF.color = Color(0.6f, 0.65f, 0.75f, 0.8f)
        glyphLayout.setText(regF, "[W/S / UP/DOWN / CLICK] Navigate  |  [ENTER / SPACE] Select")
        regF.draw(batch, "[W/S / UP/DOWN / CLICK] Navigate  |  [ENTER / SPACE] Select", (1280f - glyphLayout.width) / 2f, 25f)

        batch.end()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            selectedIndex = (selectedIndex - 1 + menuItems.size) % menuItems.size
            AudioManager.playJump()
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            selectedIndex = (selectedIndex + 1) % menuItems.size
            AudioManager.playJump()
        }

        // Deteksi Hover & Klik Mouse
        if (Gdx.input.isTouched) {
            val touchPoint = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f), viewport.screenX.toFloat(), viewport.screenY.toFloat(), viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat())
            val buttonWidth = 420f
            val buttonHeight = 48f
            val startY = 460f
            val spacingY = 62f
            val btnX = (1280f - buttonWidth) / 2f

            for (i in menuItems.indices) {
                val btnY = startY - (i * spacingY)
                if (touchPoint.x in btnX..(btnX + buttonWidth) && touchPoint.y in btnY..(btnY + buttonHeight)) {
                    selectedIndex = i
                    if (Gdx.input.justTouched()) {
                        executeSelection()
                    }
                    break
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            executeSelection()
        }
    }

    private fun executeSelection() {
        when (selectedIndex) {
            0 -> {
                AudioManager.playSlashLight()
                game.setScreen(PlayScreen(game))
            }
            1 -> {
                AudioManager.playSlashLight()
                game.setScreen(CharacterSelectScreen(game))
            }
            2 -> {
                AudioManager.toggleMute()
                if (!AudioManager.isMuted) AudioManager.playChest()
            }
            3 -> {
                SaveManager.save(SaveData())
                currentSaveData = SaveManager.load()
                feedbackNotice = "Save Data Successfully Reset!"
                feedbackTimer = 3.0f
                AudioManager.playDeath()
            }
            4 -> {
                Gdx.app.exit()
            }
        }
    }

    private fun updateParticles(delta: Float) {
        for (p in particles) {
            p.life += delta
            p.pos.x += p.vel.x * delta
            p.pos.y += p.vel.y * delta
            if (p.isExpired || p.pos.y > 730f) {
                p.reset(initial = false)
            }
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

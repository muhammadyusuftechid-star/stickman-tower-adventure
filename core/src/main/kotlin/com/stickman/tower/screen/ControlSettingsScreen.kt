package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.audio.HapticManager
import com.stickman.tower.combat.Skill
import com.stickman.tower.combat.UltimateEnergy
import com.stickman.tower.input.GamepadInput
import com.stickman.tower.input.TouchControlSettings
import com.stickman.tower.input.TouchSettingsManager
import com.stickman.tower.input.VirtualController
import com.stickman.tower.ui.FontManager

/**
 * Layar Pengaturan Kontrol & Ergonomi (Fase 12):
 * - Kustomisasi posisi tombol sentuh (Interactive Drag & Drop)
 * - Slider Skala (70% - 130%) & Transparansi (30% - 100%)
 * - Auto-Detection & Status Gamepad Fisik (XInput / DualShock / Android)
 * - Uji Coba Haptic & Gamepad Rumble
 */
class ControlSettingsScreen(
    private val game: StickmanGame? = null,
    private val previousScreen: Screen? = null
) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1280f, 720f, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val glyphLayout = GlyphLayout()
    private val touchVec = Vector3()

    // Data Pengaturan Aktif
    private val settings: TouchControlSettings = TouchSettingsManager.load()
    private val previewController = VirtualController()
    private val gamepadInput = GamepadInput()
    private val dummySkill = Skill(cooldown = 4.5f)
    private val dummyUltimate = UltimateEnergy(maxEnergy = 100f).apply { addEnergy(100f) }

    // State Dragging
    private enum class DragTarget { NONE, DPAD, ACTION }
    private var currentDrag = DragTarget.NONE
    private var dragTouchStartX = 0f
    private var dragTouchStartY = 0f
    private var initialDpadX = 0f
    private var initialDpadY = 0f
    private var initialActionX = 0f
    private var initialActionY = 0f

    // Tombol-tombol Pengaturan Atas
    private val btnScaleMinus = Rectangle(350f, 600f, 44f, 34f)
    private val btnScalePlus = Rectangle(490f, 600f, 44f, 34f)
    private val btnOpacityMinus = Rectangle(650f, 600f, 44f, 34f)
    private val btnOpacityPlus = Rectangle(790f, 600f, 44f, 34f)

    private val btnToggleAutoHide = Rectangle(880f, 600f, 220f, 34f)
    private val btnTestRumble = Rectangle(350f, 550f, 180f, 34f)
    private val btnResetDefaults = Rectangle(560f, 550f, 180f, 34f)
    private val btnSaveExit = Rectangle(770f, 550f, 180f, 34f)

    private var rumbleFeedbackTimer = 0f

    override fun show() {
        Gdx.input?.setCatchKey(Keys.BACK, true)
        FontManager.init()
        AudioManager.init()
        HapticManager.init()
        if (Gdx.graphics != null && Gdx.graphics.width > 0 && Gdx.graphics.height > 0) {
            viewport.update(Gdx.graphics.width, Gdx.graphics.height, true)
        }
        previewController.applySettings(settings, 1280, 720)
    }

    override fun render(delta: Float) {
        val safeDelta = delta.coerceAtMost(1f / 30f)
        if (rumbleFeedbackTimer > 0f) {
            rumbleFeedbackTimer = (rumbleFeedbackTimer - safeDelta).coerceAtLeast(0f)
        }

        handleInput()

        ScreenUtils.clear(0.06f, 0.08f, 0.12f, 1f)
        viewport.apply()
        shapeRenderer.projectionMatrix = camera.combined
        batch.projectionMatrix = camera.combined

        renderGridAndGuides()
        previewController.render(shapeRenderer, batch, FontManager.smallFont, dummySkill, dummyUltimate)
        renderUIHeader()
    }

    private fun isRectTouched(r: Rectangle, tx: Float, ty: Float, padding: Float = 14f): Boolean {
        return tx >= r.x - padding && tx <= r.x + r.width + padding &&
               ty >= r.y - padding && ty <= r.y + r.height + padding
    }

    private fun handleInput() {
        // Tombol Kembali (ESC atau Android BACK)
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            saveAndExit()
            return
        }

        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(touchVec)
            val tx = touchVec.x
            val ty = touchVec.y

            // Cek Klik Tombol Konfigurasi Atas dengan padding toleransi sentuh
            when {
                isRectTouched(btnScaleMinus, tx, ty) -> {
                    settings.scale = (settings.scale - 0.05f).coerceAtLeast(0.70f)
                    applyAndPreview()
                    AudioManager.playJump()
                }
                isRectTouched(btnScalePlus, tx, ty) -> {
                    settings.scale = (settings.scale + 0.05f).coerceAtMost(1.30f)
                    applyAndPreview()
                    AudioManager.playJump()
                }
                isRectTouched(btnOpacityMinus, tx, ty) -> {
                    settings.opacity = (settings.opacity - 0.05f).coerceAtLeast(0.30f)
                    applyAndPreview()
                    AudioManager.playJump()
                }
                isRectTouched(btnOpacityPlus, tx, ty) -> {
                    settings.opacity = (settings.opacity + 0.05f).coerceAtMost(1.00f)
                    applyAndPreview()
                    AudioManager.playJump()
                }
                isRectTouched(btnToggleAutoHide, tx, ty) -> {
                    settings.autoHideOnGamepad = !settings.autoHideOnGamepad
                    applyAndPreview()
                    AudioManager.playDodge()
                }
                isRectTouched(btnTestRumble, tx, ty) -> {
                    HapticManager.vibrateHeavy()
                    gamepadInput.vibrate(300, 1.0f)
                    rumbleFeedbackTimer = 0.6f
                    AudioManager.playParry()
                }
                isRectTouched(btnResetDefaults, tx, ty) -> {
                    val def = TouchSettingsManager.resetToDefault()
                    settings.dpadOffsetX = def.dpadOffsetX
                    settings.dpadOffsetY = def.dpadOffsetY
                    settings.actionOffsetX = def.actionOffsetX
                    settings.actionOffsetY = def.actionOffsetY
                    settings.scale = def.scale
                    settings.opacity = def.opacity
                    settings.autoHideOnGamepad = def.autoHideOnGamepad
                    applyAndPreview()
                    AudioManager.playChest()
                }
                isRectTouched(btnSaveExit, tx, ty, padding = 18f) -> {
                    saveAndExit()
                    return
                }
            }

            // Cek Mulai Drag D-Pad Cluster
            val dpadCenterX = (previewController.btnLeft.x + previewController.btnRight.x) / 2f
            val dpadCenterY = (previewController.btnLeft.y + previewController.btnRight.y) / 2f
            if (Vector3.dst(tx, ty, 0f, dpadCenterX, dpadCenterY, 0f) < 130f * settings.scale) {
                currentDrag = DragTarget.DPAD
                dragTouchStartX = tx
                dragTouchStartY = ty
                initialDpadX = settings.dpadOffsetX
                initialDpadY = settings.dpadOffsetY
            }

            // Cek Mulai Drag Action Cluster
            val actionCenterX = previewController.btnAttack.x - 60f * settings.scale
            val actionCenterY = previewController.btnAttack.y + 60f * settings.scale
            if (Vector3.dst(tx, ty, 0f, actionCenterX, actionCenterY, 0f) < 170f * settings.scale) {
                currentDrag = DragTarget.ACTION
                dragTouchStartX = tx
                dragTouchStartY = ty
                initialActionX = settings.actionOffsetX
                initialActionY = settings.actionOffsetY
            }
        }

        if (Gdx.input.isTouched) {
            touchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(touchVec)
            val dx = touchVec.x - dragTouchStartX
            val dy = touchVec.y - dragTouchStartY

            when (currentDrag) {
                DragTarget.DPAD -> {
                    settings.dpadOffsetX = initialDpadX + dx
                    settings.dpadOffsetY = initialDpadY + dy
                    applyAndPreview()
                }
                DragTarget.ACTION -> {
                    settings.actionOffsetX = initialActionX + dx
                    settings.actionOffsetY = initialActionY + dy
                    applyAndPreview()
                }
                DragTarget.NONE -> {}
            }
        } else {
            currentDrag = DragTarget.NONE
        }
    }

    private fun applyAndPreview() {
        settings.clamp()
        previewController.applySettings(settings, 1280, 720)
    }

    private fun saveAndExit() {
        TouchSettingsManager.save(settings)
        AudioManager.playSlashLight()
        if (game != null) {
            if (previousScreen != null) {
                game.setScreen(previousScreen)
            } else {
                game.setScreen(MainMenuScreen(game))
            }
        }
    }

    private fun renderGridAndGuides() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.12f, 0.16f, 0.24f, 0.5f)

        // Grid background halus untuk presisi penyelarasan
        for (gx in 0..1280 step 80) {
            shapeRenderer.line(gx.toFloat(), 0f, gx.toFloat(), 720f)
        }
        for (gy in 0..720 step 80) {
            shapeRenderer.line(0f, gy.toFloat(), 1280f, gy.toFloat())
        }

        // Bounding box area drag D-Pad (Biru Cyan)
        shapeRenderer.color = if (currentDrag == DragTarget.DPAD) Color.CYAN else Color(0.2f, 0.6f, 0.9f, 0.4f)
        val dpadBoxW = 260f * settings.scale
        val dpadBoxH = 150f * settings.scale
        val dpadBoxX = previewController.btnLeft.x - 60f * settings.scale
        val dpadBoxY = previewController.btnLeft.y - 65f * settings.scale
        shapeRenderer.rect(dpadBoxX, dpadBoxY, dpadBoxW, dpadBoxH)

        // Bounding box area drag Action Cluster (Merah Emas)
        shapeRenderer.color = if (currentDrag == DragTarget.ACTION) Color.GOLD else Color(0.9f, 0.4f, 0.2f, 0.4f)
        val actionBoxW = 340f * settings.scale
        val actionBoxH = 340f * settings.scale
        val actionBoxX = previewController.btnAttack.x - 220f * settings.scale
        val actionBoxY = previewController.btnAttack.y - 75f * settings.scale
        shapeRenderer.rect(actionBoxX, actionBoxY, actionBoxW, actionBoxH)

        shapeRenderer.end()
    }

    private fun renderUIHeader() {
        val w = 1280f
        val headF = FontManager.headerFont
        val regF = FontManager.regularFont
        val smallF = FontManager.smallFont
        val titleF = FontManager.titleFont

        // Header Panel Glassmorphism
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.06f, 0.08f, 0.14f, 0.95f)
        shapeRenderer.rect(40f, 530f, w - 80f, 170f)

        // Gold border accent
        shapeRenderer.color = Color(0.92f, 0.78f, 0.28f, 0.9f)
        shapeRenderer.rectLine(40f, 530f, w - 40f, 530f, 2.5f)

        // Tombol-tombol Panel Atas
        drawButtonCard(btnScaleMinus, Color(0.15f, 0.2f, 0.3f, 0.9f), Color.CYAN)
        drawButtonCard(btnScalePlus, Color(0.15f, 0.2f, 0.3f, 0.9f), Color.CYAN)
        drawButtonCard(btnOpacityMinus, Color(0.15f, 0.2f, 0.3f, 0.9f), Color.CYAN)
        drawButtonCard(btnOpacityPlus, Color(0.15f, 0.2f, 0.3f, 0.9f), Color.CYAN)

        drawButtonCard(btnToggleAutoHide, Color(0.15f, 0.22f, 0.32f, 0.9f), if (settings.autoHideOnGamepad) Color.GREEN else Color.LIGHT_GRAY)
        drawButtonCard(btnTestRumble, Color(0.2f, 0.16f, 0.32f, 0.9f), if (rumbleFeedbackTimer > 0f) Color.GOLD else Color.MAGENTA)
        drawButtonCard(btnResetDefaults, Color(0.25f, 0.15f, 0.15f, 0.9f), Color.ORANGE)
        drawButtonCard(btnSaveExit, Color(0.12f, 0.45f, 0.25f, 0.95f), Color.GREEN)

        shapeRenderer.end()

        // Gambar Teks
        batch.begin()

        titleF.color = Color.GOLD
        titleF.draw(batch, "🎮 CONTROL SETTINGS & ERGONOMICS", 60f, 680f)

        regF.color = Color(0.85f, 0.90f, 1f, 0.9f)
        regF.draw(batch, "Drag the highlighted D-Pad and Action button clusters to customize your layout.", 60f, 642f)

        // Teks Pengaturan Skala
        regF.color = Color.WHITE
        regF.draw(batch, "SCALE: ${(settings.scale * 100).toInt()}%", 400f, 624f)
        headF.color = Color.CYAN
        headF.draw(batch, "-", btnScaleMinus.x + 16f, btnScaleMinus.y + 26f)
        headF.draw(batch, "+", btnScalePlus.x + 14f, btnScalePlus.y + 26f)

        // Teks Pengaturan Opasitas
        regF.color = Color.WHITE
        regF.draw(batch, "OPACITY: ${(settings.opacity * 100).toInt()}%", 698f, 624f)
        headF.color = Color.CYAN
        headF.draw(batch, "-", btnOpacityMinus.x + 16f, btnOpacityMinus.y + 26f)
        headF.draw(batch, "+", btnOpacityPlus.x + 14f, btnOpacityPlus.y + 26f)

        // Teks Auto-Hide on Gamepad
        val autoHideText = if (settings.autoHideOnGamepad) "AUTO-HIDE: ON" else "AUTO-HIDE: OFF"
        regF.color = if (settings.autoHideOnGamepad) Color.GREEN else Color.LIGHT_GRAY
        glyphLayout.setText(regF, autoHideText)
        regF.draw(batch, autoHideText, btnToggleAutoHide.x + (btnToggleAutoHide.width - glyphLayout.width) / 2f, btnToggleAutoHide.y + 23f)

        // Teks Baris Tombol Aksi Bawah
        val rumbleText = if (rumbleFeedbackTimer > 0f) "RUMBLING! ✨" else "TEST RUMBLE"
        regF.color = Color.WHITE
        glyphLayout.setText(regF, rumbleText)
        regF.draw(batch, rumbleText, btnTestRumble.x + (btnTestRumble.width - glyphLayout.width) / 2f, btnTestRumble.y + 23f)

        val resetText = "RESET DEFAULT"
        regF.color = Color(1f, 0.8f, 0.8f, 1f)
        glyphLayout.setText(regF, resetText)
        regF.draw(batch, resetText, btnResetDefaults.x + (btnResetDefaults.width - glyphLayout.width) / 2f, btnResetDefaults.y + 23f)

        val saveText = "✓ SAVE & EXIT"
        regF.color = Color.WHITE
        glyphLayout.setText(regF, saveText)
        regF.draw(batch, saveText, btnSaveExit.x + (btnSaveExit.width - glyphLayout.width) / 2f, btnSaveExit.y + 23f)

        // Status Gamepad Fisik (Kiri Bawah Header)
        val hasGamepad = gamepadInput.hasActiveGamepad()
        val gamepadName = gamepadInput.activeController?.name ?: "Unknown Controller"
        val statusText = if (hasGamepad) "● GAMEPAD: CONNECTED ($gamepadName)" else "○ GAMEPAD: NOT DETECTED (Touch / Keyboard Ready)"
        smallF.color = if (hasGamepad) Color(0.2f, 0.95f, 0.4f, 1f) else Color.LIGHT_GRAY
        smallF.draw(batch, statusText, 60f, 572f)

        // Hint Petunjuk di Bagian Bawah Layar
        smallF.color = Color(0.7f, 0.75f, 0.85f, 0.7f)
        val dragHint = "DRAG D-PAD OR ACTION CLUSTER ANYWHERE ON SCREEN  •  [ESC / BACK] TO RETURN"
        glyphLayout.setText(smallF, dragHint)
        smallF.draw(batch, dragHint, (w - glyphLayout.width) / 2f, 25f)

        batch.end()
    }

    private fun drawButtonCard(rect: Rectangle, bgColor: Color, borderColor: Color) {
        shapeRenderer.color = bgColor
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
        shapeRenderer.color = borderColor
        shapeRenderer.rectLine(rect.x, rect.y, rect.x + rect.width, rect.y, 1.5f)
        shapeRenderer.rectLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height, 1.5f)
        shapeRenderer.rectLine(rect.x, rect.y, rect.x, rect.y + rect.height, 1.5f)
        shapeRenderer.rectLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height, 1.5f)
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

package com.stickman.tower.tools

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.character.Player
import com.stickman.tower.ui.FontManager
import com.stickman.tower.world.TowerFloor

/**
 * In-Game Developer Console, Profiler & QA Testing Overlay:
 * Menyediakan alat pemantau performa realtime, pengubah state permainan (God Mode, Infinite SP),
 * stage warper, dan validator level interaktif yang dapat diakses melalui keyboard (F1/F3/~)
 * maupun tombol sentuh pada Android.
 */
class DevToolsOverlay {

    var isVisible: Boolean = false

    // QA & Cheat Toggles
    var isGodMode: Boolean = false
    var isInfiniteEnergy: Boolean = false
    var showHitboxes: Boolean = false

    // Status Audit Level Terakhir
    var lastAuditSummary: String = "Audit belum dijalankan. Tekan [RUN AUDIT]."

    // Callback Aksi yang Dihubungkan ke PlayScreen
    var onWarpFloor: ((Int) -> Unit)? = null
    var onGrantGold: ((Int) -> Unit)? = null
    var onGrantLevel: (() -> Unit)? = null
    var onHealFull: (() -> Unit)? = null
    var onKillAllEnemies: (() -> Unit)? = null

    // Tombol Interaktif (Bounding Boxes untuk Touch & Mouse Click)
    private val btnClose = Rectangle(1000f, 580f, 40f, 35f)
    private val btnGodMode = Rectangle(260f, 440f, 175f, 38f)
    private val btnInfEnergy = Rectangle(450f, 440f, 175f, 38f)
    private val btnHitboxes = Rectangle(640f, 440f, 175f, 38f)
    private val btnRunAudit = Rectangle(830f, 440f, 175f, 38f)

    private val btnHeal = Rectangle(260f, 380f, 175f, 38f)
    private val btnGrantGold = Rectangle(450f, 380f, 175f, 38f)
    private val btnGrantLevel = Rectangle(640f, 380f, 175f, 38f)
    private val btnKillEnemies = Rectangle(830f, 380f, 175f, 38f)

    private val btnWarpF1 = Rectangle(260f, 320f, 135f, 38f)
    private val btnWarpF2 = Rectangle(410f, 320f, 135f, 38f)
    private val btnWarpF3 = Rectangle(560f, 320f, 135f, 38f)
    private val btnWarpEndless5 = Rectangle(710f, 320f, 135f, 38f)
    private val btnWarpEndless10 = Rectangle(860f, 320f, 145f, 38f)

    private val touchPoint = Vector3()

    fun toggle() {
        isVisible = !isVisible
    }

    /**
     * Memproses input klik / sentuhan pada panel Developer Tools.
     */
    fun handleTouchDown(screenX: Int, screenY: Int, viewport: Viewport, currentFloor: TowerFloor): Boolean {
        if (!isVisible) return false

        touchPoint.set(screenX.toFloat(), screenY.toFloat(), 0f)
        viewport.unproject(touchPoint)
        val tx = touchPoint.x
        val ty = touchPoint.y

        if (btnClose.contains(tx, ty)) {
            isVisible = false
            return true
        }

        if (btnGodMode.contains(tx, ty)) {
            isGodMode = !isGodMode
            return true
        }

        if (btnInfEnergy.contains(tx, ty)) {
            isInfiniteEnergy = !isInfiniteEnergy
            return true
        }

        if (btnHitboxes.contains(tx, ty)) {
            showHitboxes = !showHitboxes
            return true
        }

        if (btnRunAudit.contains(tx, ty)) {
            val report = LevelSanityValidator.validateFloor(currentFloor)
            lastAuditSummary = report.summary()
            return true
        }

        if (btnHeal.contains(tx, ty)) {
            onHealFull?.invoke()
            return true
        }

        if (btnGrantGold.contains(tx, ty)) {
            onGrantGold?.invoke(1000)
            return true
        }

        if (btnGrantLevel.contains(tx, ty)) {
            onGrantLevel?.invoke()
            return true
        }

        if (btnKillEnemies.contains(tx, ty)) {
            onKillAllEnemies?.invoke()
            return true
        }

        if (btnWarpF1.contains(tx, ty)) {
            onWarpFloor?.invoke(1)
            return true
        }
        if (btnWarpF2.contains(tx, ty)) {
            onWarpFloor?.invoke(2)
            return true
        }
        if (btnWarpF3.contains(tx, ty)) {
            onWarpFloor?.invoke(3)
            return true
        }
        if (btnWarpEndless5.contains(tx, ty)) {
            onWarpFloor?.invoke(5)
            return true
        }
        if (btnWarpEndless10.contains(tx, ty)) {
            onWarpFloor?.invoke(10)
            return true
        }

        // Konsumsi sentuhan jika berada di dalam area panel modal
        if (tx in 220f..1060f && ty in 90f..630f) {
            return true
        }

        return false
    }

    /**
     * Merender panel profil dan alat pengembang pada viewport UI.
     */
    fun render(
        shapeRenderer: ShapeRenderer,
        batch: SpriteBatch,
        camera: OrthographicCamera,
        player: Player,
        currentFloor: TowerFloor,
        particleCount: Int
    ) {
        if (!isVisible) return

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        val regF = FontManager.regularFont
        val headF = FontManager.headerFont

        // 1. Gambar Panel Latar Belakang Modal (Dark Obsidian)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Backdrop Dimmer
        shapeRenderer.setColor(0f, 0f, 0f, 0.70f)
        shapeRenderer.rect(0f, 0f, 1280f, 720f)

        // Panel Utama
        shapeRenderer.setColor(0.06f, 0.08f, 0.12f, 0.95f)
        shapeRenderer.rect(220f, 90f, 840f, 540f)

        // Header Bar Neon Cyan
        shapeRenderer.setColor(0.12f, 0.65f, 0.85f, 1f)
        shapeRenderer.rectLine(220f, 630f, 1060f, 630f, 3f)

        // Tombol Tutup [ X ]
        shapeRenderer.setColor(0.85f, 0.20f, 0.25f, 0.9f)
        shapeRenderer.rect(btnClose.x, btnClose.y, btnClose.width, btnClose.height)

        // Render Tombol-Tombol Aksi
        renderButtonShape(shapeRenderer, btnGodMode, if (isGodMode) Color.GREEN else Color.DARK_GRAY)
        renderButtonShape(shapeRenderer, btnInfEnergy, if (isInfiniteEnergy) Color.CYAN else Color.DARK_GRAY)
        renderButtonShape(shapeRenderer, btnHitboxes, if (showHitboxes) Color.ORANGE else Color.DARK_GRAY)
        renderButtonShape(shapeRenderer, btnRunAudit, Color.ROYAL)

        renderButtonShape(shapeRenderer, btnHeal, Color(0.2f, 0.6f, 0.3f, 1f))
        renderButtonShape(shapeRenderer, btnGrantGold, Color(0.7f, 0.55f, 0.1f, 1f))
        renderButtonShape(shapeRenderer, btnGrantLevel, Color(0.3f, 0.4f, 0.8f, 1f))
        renderButtonShape(shapeRenderer, btnKillEnemies, Color(0.7f, 0.2f, 0.2f, 1f))

        renderButtonShape(shapeRenderer, btnWarpF1, Color(0.2f, 0.3f, 0.4f, 1f))
        renderButtonShape(shapeRenderer, btnWarpF2, Color(0.2f, 0.3f, 0.4f, 1f))
        renderButtonShape(shapeRenderer, btnWarpF3, Color(0.6f, 0.2f, 0.2f, 1f))
        renderButtonShape(shapeRenderer, btnWarpEndless5, Color(0.4f, 0.2f, 0.5f, 1f))
        renderButtonShape(shapeRenderer, btnWarpEndless10, Color(0.5f, 0.1f, 0.6f, 1f))

        shapeRenderer.end()

        // 2. Render Teks & Metrik Profiler
        batch.projectionMatrix = camera.combined
        batch.begin()

        headF.setColor(0.2f, 0.85f, 1.0f, 1f)
        headF.draw(batch, "⚙ DEVELOPER CONSOLE & PROFILER", 250f, 615f)

        regF.setColor(1f, 1f, 1f, 1f)
        regF.draw(batch, "X", btnClose.x + 13f, btnClose.y + 25f)

        // Metrik Realtime
        val fps = Gdx.graphics.framesPerSecond
        val deltaMs = (Gdx.graphics.deltaTime * 1000f).toInt()
        val memUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
        val memMaxMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        regF.color = if (fps >= 55) Color.GREEN else if (fps >= 30) Color.YELLOW else Color.RED
        regF.draw(batch, "FPS: $fps (${deltaMs}ms)", 250f, 565f)

        regF.color = Color.WHITE
        regF.draw(batch, "Heap: ${memUsedMB}MB / ${memMaxMB}MB", 450f, 565f)
        regF.draw(batch, "Floor: ${currentFloor.floorName}", 720f, 565f)

        regF.setColor(0.85f, 0.85f, 0.85f, 1f)
        val enemyCount = currentFloor.enemies.count { !it.health.isDead }
        val projCount = currentFloor.enemies.sumOf { it.projectiles.size } + (currentFloor.boss?.projectiles?.size ?: 0)
        regF.draw(batch, "Entities: Mobs: $enemyCount | Proj: $projCount | Particles: $particleCount", 250f, 530f)

        val playerGrounded = if (player.isGrounded) "GROUNDED" else "AIRBORNE"
        val pX = "%.1f".format(player.position.x)
        val pY = "%.1f".format(player.position.y)
        regF.draw(batch, "Player: Pos($pX, $pY) | $playerGrounded | HP:${player.health.currentHp.toInt()}/${player.health.maxHp.toInt()}", 250f, 500f)

        // Label Tombol
        renderButtonText(batch, regF, btnGodMode, if (isGodMode) "GOD MODE: ON" else "GOD MODE: OFF")
        renderButtonText(batch, regF, btnInfEnergy, if (isInfiniteEnergy) "INF ENERGY: ON" else "INF ENERGY: OFF")
        renderButtonText(batch, regF, btnHitboxes, if (showHitboxes) "HITBOXES: ON" else "HITBOXES: OFF")
        renderButtonText(batch, regF, btnRunAudit, "RUN LEVEL AUDIT")

        renderButtonText(batch, regF, btnHeal, "+ HEAL FULL HP")
        renderButtonText(batch, regF, btnGrantGold, "+ 1000 GOLD")
        renderButtonText(batch, regF, btnGrantLevel, "+ 1 LEVEL UP")
        renderButtonText(batch, regF, btnKillEnemies, "KILL ALL MOBS")

        renderButtonText(batch, regF, btnWarpF1, "WARP F1")
        renderButtonText(batch, regF, btnWarpF2, "WARP F2")
        renderButtonText(batch, regF, btnWarpF3, "WARP BOSS")
        renderButtonText(batch, regF, btnWarpEndless5, "WARP ENDLESS 5")
        renderButtonText(batch, regF, btnWarpEndless10, "WARP ENDLESS 10")

        // Hasil Audit Terakhir
        regF.setColor(0.4f, 0.95f, 0.4f, 1f)
        regF.draw(batch, "Level Audit Status:", 250f, 250f)
        regF.setColor(0.9f, 0.9f, 0.9f, 1f)
        regF.draw(batch, lastAuditSummary, 250f, 220f)

        regF.setColor(0.6f, 0.6f, 0.6f, 1f)
        regF.draw(batch, "Tip: Tekan F1/F3/~ pada keyboard atau tombol DevTools di menu pause untuk buka/tutup.", 250f, 140f)

        batch.end()
    }

    private fun renderButtonShape(shapeRenderer: ShapeRenderer, btn: Rectangle, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height)
        shapeRenderer.setColor(0.9f, 0.9f, 0.9f, 0.6f)
        shapeRenderer.rectLine(btn.x, btn.y, btn.x + btn.width, btn.y, 1f)
    }

    private fun renderButtonText(batch: SpriteBatch, font: BitmapFont, btn: Rectangle, text: String) {
        font.color = Color.WHITE
        font.draw(batch, text, btn.x + 10f, btn.y + btn.height / 2f + 6f)
    }
}

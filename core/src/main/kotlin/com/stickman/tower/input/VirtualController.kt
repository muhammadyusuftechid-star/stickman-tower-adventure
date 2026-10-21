package com.stickman.tower.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.combat.Skill
import com.stickman.tower.combat.UltimateEnergy

/**
 * On-Screen Virtual Touch Controller untuk target layar sentuh Android dan playtesting desktop.
 * Dilengkapi dengan ikon vektor geometris murni (Pedang ATK, Sayap JUMP, Bayangan DODGE, Rune SKILL, Mahkota Surya ULT)
 * serta deteksi multi-touch presisi berbasis viewport unproject.
 */
class VirtualController : GameInput {
    // Tombol D-Pad Kiri Layar (Floating Circles)
    val btnLeft = Circle()
    val btnRight = Circle()

    // Tombol Aksi Kanan Layar (Floating Circles)
    val btnAttack = Circle()
    val btnJump = Circle()
    val btnDodge = Circle()
    val btnSkill = Circle()
    val btnUltimate = Circle()

    // Status Penekanan Frame
    private var pressedLeft = false
    private var pressedRight = false
    private var pressedJump = false
    private var justPressedJump = false
    private var justPressedAttack = false
    private var justPressedDodge = false
    private var justPressedSkill = false
    private var justPressedUltimate = false

    // State Pelacakan Tombol
    private var wasAttackPressed = false
    private var wasJumpPressed = false
    private var wasDodgePressed = false
    private var wasSkillPressed = false
    private var wasUltimatePressed = false

    private val touchVec = Vector3()
    var isVisible = true
    var settings: TouchControlSettings = TouchSettingsManager.load()

    /**
     * Menerapkan konfigurasi tata letak dan ergonomi tombol baru.
     */
    fun applySettings(newSettings: TouchControlSettings, virtualWidth: Int = 1280, virtualHeight: Int = 720) {
        settings = newSettings.copy()
        resize(virtualWidth, virtualHeight)
    }

    /**
     * Mengatur posisi dan ukuran tombol dalam koordinat virtual UI Canvas (default 1280x720)
     * berdasarkan skala dan offset kustomisasi pengguna.
     */
    fun resize(virtualWidth: Int = 1280, virtualHeight: Int = 720) {
        val w = virtualWidth.toFloat()
        val h = virtualHeight.toFloat()
        val scale = settings.scale

        // Ukuran dan Posisi D-Pad Lingkaran Ergonomis (Kiri Bawah dengan offset pengguna)
        val dpadRadius = 45f * scale
        val dpadBaseX = 85f + settings.dpadOffsetX
        val dpadBaseY = 95f + settings.dpadOffsetY
        val dpadSpacing = 115f * scale

        btnLeft.set(dpadBaseX, dpadBaseY, dpadRadius)
        btnRight.set(dpadBaseX + dpadSpacing, dpadBaseY, dpadRadius)

        // Posisi Tombol Aksi Konsentris (Kanan Bawah dengan offset pengguna)
        val actionBaseX = (w - 120f) + settings.actionOffsetX
        val actionBaseY = 110f + settings.actionOffsetY

        btnAttack.set(actionBaseX, actionBaseY, 54f * scale)
        btnJump.set(actionBaseX - 115f * scale, actionBaseY - 35f * scale, 42f * scale)
        btnDodge.set(actionBaseX - 125f * scale, actionBaseY + 65f * scale, 40f * scale)
        btnSkill.set(actionBaseX, actionBaseY + 125f * scale, 42f * scale)
        btnUltimate.set(actionBaseX - 105f * scale, actionBaseY + 165f * scale, 44f * scale)
    }

    /**
     * Memperbarui status penekanan tombol menggunakan deteksi multi-touch (pointer 0..4).
     * Jika [viewport] disertakan, koordinat sentuhan fisik di-unproject ke koordinat canvas virtual UI.
     */
    fun update(viewport: Viewport? = null) {
        val prevAttack = wasAttackPressed
        val prevJump = wasJumpPressed
        val prevDodge = wasDodgePressed
        val prevSkill = wasSkillPressed
        val prevUltimate = wasUltimatePressed

        pressedLeft = false
        pressedRight = false
        pressedJump = false
        var curAttack = false
        var curJump = false
        var curDodge = false
        var curSkill = false
        var curUltimate = false

        for (pointer in 0..4) {
            if (Gdx.input != null && Gdx.input.isTouched(pointer)) {
                touchVec.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat(), 0f)
                if (viewport != null) {
                    viewport.unproject(touchVec)
                } else if (Gdx.graphics != null) {
                    touchVec.y = Gdx.graphics.height - touchVec.y
                }
                val touchX = touchVec.x
                val touchY = touchVec.y

                if (btnLeft.contains(touchX, touchY)) pressedLeft = true
                if (btnRight.contains(touchX, touchY)) pressedRight = true

                if (btnAttack.contains(touchX, touchY)) curAttack = true
                if (btnJump.contains(touchX, touchY)) {
                    curJump = true
                    pressedJump = true
                }
                if (btnDodge.contains(touchX, touchY)) curDodge = true
                if (btnSkill.contains(touchX, touchY)) curSkill = true
                if (btnUltimate.contains(touchX, touchY)) curUltimate = true
            }
        }

        justPressedAttack = curAttack && !prevAttack
        justPressedJump = curJump && !prevJump
        justPressedDodge = curDodge && !prevDodge
        justPressedSkill = curSkill && !prevSkill
        justPressedUltimate = curUltimate && !prevUltimate

        wasAttackPressed = curAttack
        wasJumpPressed = curJump
        wasDodgePressed = curDodge
        wasSkillPressed = curSkill
        wasUltimatePressed = curUltimate
    }

    override fun isMoveLeft() = pressedLeft
    override fun isMoveRight() = pressedRight
    override fun isJumpJustPressed() = justPressedJump
    override fun isJumpPressed() = pressedJump
    override fun isAttackJustPressed() = justPressedAttack
    override fun isDodgeJustPressed() = justPressedDodge
    override fun isSkillJustPressed() = justPressedSkill
    override fun isUltimateJustPressed() = justPressedUltimate

    /**
     * Menggambar tombol kontrol melayang dan ikon vektor aksi sesuai skala dan transparansi kustom.
     */
    fun render(
        shapeRenderer: ShapeRenderer,
        batch: SpriteBatch,
        font: BitmapFont,
        skill: Skill,
        ultimate: UltimateEnergy
    ) {
        if (!isVisible) return

        val op = settings.opacity
        val sc = settings.scale

        // 1. Gambar Background Tombol Melayang (Translucent Dark Obsidian Glass)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // D-Pad Kiri & Kanan (Lingkaran Elegan)
        shapeRenderer.color = if (pressedLeft) Color(0.2f, 0.6f, 0.95f, 0.75f * op) else Color(0.08f, 0.12f, 0.18f, 0.45f * op)
        shapeRenderer.circle(btnLeft.x, btnLeft.y, btnLeft.radius, 24)

        shapeRenderer.color = if (pressedRight) Color(0.2f, 0.6f, 0.95f, 0.75f * op) else Color(0.08f, 0.12f, 0.18f, 0.45f * op)
        shapeRenderer.circle(btnRight.x, btnRight.y, btnRight.radius, 24)

        // Vektor Segitiga Panah D-Pad Kiri & Kanan
        shapeRenderer.color = if (pressedLeft) Color(1f, 1f, 1f, op) else Color(0.75f, 0.85f, 0.95f, 0.85f * op)
        shapeRenderer.triangle(
            btnLeft.x - 14f * sc, btnLeft.y,
            btnLeft.x + 10f * sc, btnLeft.y + 16f * sc,
            btnLeft.x + 10f * sc, btnLeft.y - 16f * sc
        )
        shapeRenderer.triangle(
            btnRight.x + 14f * sc, btnRight.y,
            btnRight.x - 10f * sc, btnRight.y + 16f * sc,
            btnRight.x - 10f * sc, btnRight.y - 16f * sc
        )

        // Tombol Attack (Merah Crimson)
        shapeRenderer.color = if (wasAttackPressed) Color(0.95f, 0.2f, 0.25f, 0.85f * op) else Color(0.75f, 0.15f, 0.2f, 0.5f * op)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 28)

        // Vektor Bilah Pedang ATK
        val atkCx = btnAttack.x
        val atkCy = btnAttack.y + 4f * sc
        shapeRenderer.color = Color(0.95f, 0.95f, 1f, 0.95f * op)
        shapeRenderer.rectLine(atkCx - 14f * sc, atkCy - 14f * sc, atkCx + 16f * sc, atkCy + 16f * sc, 4f * sc)
        shapeRenderer.color = Color(1f, 0.85f, 0.25f, 0.95f * op) // Gagang Emas
        shapeRenderer.rectLine(atkCx - 9f * sc, atkCy - 1f * sc, atkCx - 1f * sc, atkCy - 9f * sc, 4.5f * sc)
        shapeRenderer.circle(atkCx - 13f * sc, atkCy - 13f * sc, 3.5f * sc)

        // Tombol Jump (Hijau Emerald)
        shapeRenderer.color = if (wasJumpPressed) Color(0.2f, 0.85f, 0.4f, 0.85f * op) else Color(0.15f, 0.65f, 0.3f, 0.5f * op)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 24)

        // Vektor Sayap/Panah Ganda JUMP
        val jmpCx = btnJump.x
        val jmpCy = btnJump.y + 4f * sc
        shapeRenderer.color = Color(0.9f, 1f, 0.9f, 0.95f * op)
        shapeRenderer.rectLine(jmpCx - 12f * sc, jmpCy + 2f * sc, jmpCx, jmpCy + 12f * sc, 3.5f * sc)
        shapeRenderer.rectLine(jmpCx, jmpCy + 12f * sc, jmpCx + 12f * sc, jmpCy + 2f * sc, 3.5f * sc)
        shapeRenderer.rectLine(jmpCx - 10f * sc, jmpCy - 6f * sc, jmpCx, jmpCy + 3f * sc, 3f * sc)
        shapeRenderer.rectLine(jmpCx, jmpCy + 3f * sc, jmpCx + 10f * sc, jmpCy - 6f * sc, 3f * sc)

        // Tombol Dodge (Biru Azure)
        shapeRenderer.color = if (wasDodgePressed) Color(0.2f, 0.65f, 1f, 0.85f * op) else Color(0.15f, 0.45f, 0.85f, 0.5f * op)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 24)

        // Vektor Jejak Bayangan Melesat DODGE
        val ddgCx = btnDodge.x
        val ddgCy = btnDodge.y + 4f * sc
        shapeRenderer.color = Color(0.8f, 0.95f, 1f, 0.95f * op)
        shapeRenderer.rectLine(ddgCx - 14f * sc, ddgCy + 7f * sc, ddgCx + 6f * sc, ddgCy + 7f * sc, 3f * sc)
        shapeRenderer.rectLine(ddgCx - 10f * sc, ddgCy, ddgCx + 14f * sc, ddgCy, 4f * sc)
        shapeRenderer.rectLine(ddgCx - 14f * sc, ddgCy - 7f * sc, ddgCx + 6f * sc, ddgCy - 7f * sc, 3f * sc)
        shapeRenderer.triangle(ddgCx + 17f * sc, ddgCy, ddgCx + 11f * sc, ddgCy + 5f * sc, ddgCx + 11f * sc, ddgCy - 5f * sc)

        // Tombol Skill 1 (Ungu Arcane)
        val skillColor = if (skill.isReady) Color(0.7f, 0.3f, 0.95f, 0.65f * op) else Color(0.3f, 0.2f, 0.4f, 0.45f * op)
        shapeRenderer.color = skillColor
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 24)

        // Vektor Berlian Runic SKILL
        val sklCx = btnSkill.x
        val sklCy = btnSkill.y + 4f * sc
        shapeRenderer.color = if (skill.isReady) Color(0.92f, 0.75f, 1f, 0.95f * op) else Color(0.55f, 0.45f, 0.65f, 0.6f * op)
        shapeRenderer.triangle(sklCx, sklCy + 14f * sc, sklCx - 11f * sc, sklCy, sklCx + 11f * sc, sklCy)
        shapeRenderer.triangle(sklCx, sklCy - 14f * sc, sklCx - 11f * sc, sklCy, sklCx + 11f * sc, sklCy)
        if (skill.isReady) {
            shapeRenderer.color = Color(1f, 1f, 1f, op)
            shapeRenderer.triangle(sklCx, sklCy + 6f * sc, sklCx - 5f * sc, sklCy, sklCx + 5f * sc, sklCy)
            shapeRenderer.triangle(sklCx, sklCy - 6f * sc, sklCx - 5f * sc, sklCy, sklCx + 5f * sc, sklCy)
        }

        // Tombol Ultimate (Emas Surya menyala jika 100%)
        val ultColor = if (ultimate.isReady) Color(1f, 0.85f, 0.2f, 0.85f * op) else Color(0.45f, 0.38f, 0.15f, 0.45f * op)
        shapeRenderer.color = ultColor
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 24)

        // Vektor Mahkota Surya Bintang ULT
        val ultCx = btnUltimate.x
        val ultCy = btnUltimate.y + 4f * sc
        shapeRenderer.color = if (ultimate.isReady) Color(1f, 0.95f, 0.4f, op) else Color(0.7f, 0.65f, 0.4f, 0.6f * op)
        shapeRenderer.circle(ultCx, ultCy, 7f * sc, 16)
        shapeRenderer.rectLine(ultCx, ultCy + 8f * sc, ultCx, ultCy + 16f * sc, 3.5f * sc)
        shapeRenderer.rectLine(ultCx, ultCy - 8f * sc, ultCx, ultCy - 16f * sc, 3.5f * sc)
        shapeRenderer.rectLine(ultCx - 8f * sc, ultCy, ultCx - 16f * sc, ultCy, 3.5f * sc)
        shapeRenderer.rectLine(ultCx + 8f * sc, ultCy, ultCx + 16f * sc, ultCy, 3.5f * sc)
        shapeRenderer.rectLine(ultCx - 6f * sc, ultCy + 6f * sc, ultCx - 11f * sc, ultCy + 11f * sc, 2.5f * sc)
        shapeRenderer.rectLine(ultCx + 6f * sc, ultCy + 6f * sc, ultCx + 12f * sc, ultCy + 12f * sc, 2.5f * sc)
        shapeRenderer.rectLine(ultCx - 6f * sc, ultCy - 6f * sc, ultCx - 11f * sc, ultCy - 11f * sc, 2.5f * sc)
        shapeRenderer.rectLine(ultCx + 6f * sc, ultCy - 6f * sc, ultCx + 12f * sc, ultCy - 12f * sc, 2.5f * sc)

        shapeRenderer.end()

        // 2. Gambar Garis Tepi (Outline Lingkaran dengan Efek Halus)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.85f, 0.9f, 1f, 0.5f * op)
        shapeRenderer.circle(btnLeft.x, btnLeft.y, btnLeft.radius, 24)
        shapeRenderer.circle(btnRight.x, btnRight.y, btnRight.radius, 24)

        shapeRenderer.color = Color(1f, 0.6f, 0.6f, 0.6f * op)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 28)

        shapeRenderer.color = Color(0.6f, 1f, 0.7f, 0.6f * op)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 24)

        shapeRenderer.color = Color(0.6f, 0.8f, 1f, 0.6f * op)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 24)

        shapeRenderer.color = if (skill.isReady) Color(0.9f, 0.7f, 1f, 0.7f * op) else Color(0.5f, 0.4f, 0.6f, 0.4f * op)
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 24)

        shapeRenderer.color = if (ultimate.isReady) Color(1f, 0.95f, 0.4f, 0.9f * op) else Color(0.6f, 0.5f, 0.3f, 0.4f * op)
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 24)

        // Lingkaran cooldown pada tombol skill
        if (!skill.isReady) {
            shapeRenderer.color = Color(0.1f, 0.1f, 0.15f, 0.8f * op)
            shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius * skill.cooldownPercent, 20)
        }
        shapeRenderer.end()

        // 3. Gambar Teks Label Tombol (Sub-label Halus & Countdown)
        batch.begin()
        font.color = Color(0.9f, 0.9f, 0.95f, 0.85f * op)

        // Sub-labels di bawah ikon
        font.draw(batch, "ATK", btnAttack.x - 14f * sc, btnAttack.y - 22f * sc)
        font.draw(batch, "JUMP", btnJump.x - 17f * sc, btnJump.y - 18f * sc)
        font.draw(batch, "DODGE", btnDodge.x - 22f * sc, btnDodge.y - 18f * sc)

        if (skill.isReady) {
            font.draw(batch, "SKILL", btnSkill.x - 17f * sc, btnSkill.y - 18f * sc)
        } else {
            font.color = Color(1f, 1f, 0.2f, op)
            val tenths = (skill.remainingCooldown * 10).toInt().coerceAtLeast(0)
            font.draw(batch, "${tenths / 10}.${tenths % 10}s", btnSkill.x - 14f * sc, btnSkill.y + 4f * sc)
        }

        if (ultimate.isReady) {
            font.color = Color(1f, 0.85f, 0.25f, op)
            font.draw(batch, "ULT!", btnUltimate.x - 14f * sc, btnUltimate.y - 18f * sc)
        } else {
            font.color = Color(0.9f, 0.85f, 0.7f, 0.8f * op)
            font.draw(batch, "${(ultimate.energyPercent * 100).toInt()}%", btnUltimate.x - 14f * sc, btnUltimate.y + 4f * sc)
        }

        batch.end()
    }
}

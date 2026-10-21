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

    /**
     * Mengatur posisi tombol dalam koordinat virtual UI Canvas (default 1280x720).
     */
    fun resize(virtualWidth: Int = 1280, virtualHeight: Int = 720) {
        val w = virtualWidth.toFloat()
        val h = virtualHeight.toFloat()

        // Ukuran dan Posisi D-Pad Lingkaran Ergonomis (Kiri Bawah)
        val dpadRadius = 45f
        btnLeft.set(85f, 95f, dpadRadius)
        btnRight.set(200f, 95f, dpadRadius)

        // Posisi Tombol Aksi Konsentris (Kanan Bawah)
        btnAttack.set(w - 120f, 110f, 54f)     // Tombol Serang Utama (Besar)
        btnJump.set(w - 235f, 75f, 42f)        // Tombol Lompat
        btnDodge.set(w - 245f, 175f, 40f)      // Tombol Dodge
        btnSkill.set(w - 120f, 235f, 42f)      // Tombol Skill 1
        btnUltimate.set(w - 225f, 275f, 44f)   // Tombol Ultimate (Atas Kanan)
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
     * Menggambar tombol kontrol melayang dan ikon vektor aksi.
     */
    fun render(
        shapeRenderer: ShapeRenderer,
        batch: SpriteBatch,
        font: BitmapFont,
        skill: Skill,
        ultimate: UltimateEnergy
    ) {
        if (!isVisible) return

        // 1. Gambar Background Tombol Melayang (Translucent Dark Obsidian Glass)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // D-Pad Kiri & Kanan (Lingkaran Elegan)
        shapeRenderer.color = if (pressedLeft) Color(0.2f, 0.6f, 0.95f, 0.75f) else Color(0.08f, 0.12f, 0.18f, 0.45f)
        shapeRenderer.circle(btnLeft.x, btnLeft.y, btnLeft.radius, 24)

        shapeRenderer.color = if (pressedRight) Color(0.2f, 0.6f, 0.95f, 0.75f) else Color(0.08f, 0.12f, 0.18f, 0.45f)
        shapeRenderer.circle(btnRight.x, btnRight.y, btnRight.radius, 24)

        // Vektor Segitiga Panah D-Pad Kiri & Kanan
        shapeRenderer.color = if (pressedLeft) Color.WHITE else Color(0.75f, 0.85f, 0.95f, 0.85f)
        shapeRenderer.triangle(
            btnLeft.x - 14f, btnLeft.y,
            btnLeft.x + 10f, btnLeft.y + 16f,
            btnLeft.x + 10f, btnLeft.y - 16f
        )
        shapeRenderer.triangle(
            btnRight.x + 14f, btnRight.y,
            btnRight.x - 10f, btnRight.y + 16f,
            btnRight.x - 10f, btnRight.y - 16f
        )

        // Tombol Attack (Merah Crimson)
        shapeRenderer.color = if (wasAttackPressed) Color(0.95f, 0.2f, 0.25f, 0.85f) else Color(0.75f, 0.15f, 0.2f, 0.5f)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 28)

        // Vektor Bilah Pedang ATK
        val atkCx = btnAttack.x
        val atkCy = btnAttack.y + 4f
        shapeRenderer.color = Color(0.95f, 0.95f, 1f, 0.95f)
        shapeRenderer.rectLine(atkCx - 14f, atkCy - 14f, atkCx + 16f, atkCy + 16f, 4f)
        shapeRenderer.color = Color(1f, 0.85f, 0.25f, 0.95f) // Gagang Emas
        shapeRenderer.rectLine(atkCx - 9f, atkCy - 1f, atkCx - 1f, atkCy - 9f, 4.5f)
        shapeRenderer.circle(atkCx - 13f, atkCy - 13f, 3.5f)

        // Tombol Jump (Hijau Emerald)
        shapeRenderer.color = if (wasJumpPressed) Color(0.2f, 0.85f, 0.4f, 0.85f) else Color(0.15f, 0.65f, 0.3f, 0.5f)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 24)

        // Vektor Sayap/Panah Ganda JUMP
        val jmpCx = btnJump.x
        val jmpCy = btnJump.y + 4f
        shapeRenderer.color = Color(0.9f, 1f, 0.9f, 0.95f)
        shapeRenderer.rectLine(jmpCx - 12f, jmpCy + 2f, jmpCx, jmpCy + 12f, 3.5f)
        shapeRenderer.rectLine(jmpCx, jmpCy + 12f, jmpCx + 12f, jmpCy + 2f, 3.5f)
        shapeRenderer.rectLine(jmpCx - 10f, jmpCy - 6f, jmpCx, jmpCy + 3f, 3f)
        shapeRenderer.rectLine(jmpCx, jmpCy + 3f, jmpCx + 10f, jmpCy - 6f, 3f)

        // Tombol Dodge (Biru Azure)
        shapeRenderer.color = if (wasDodgePressed) Color(0.2f, 0.65f, 1f, 0.85f) else Color(0.15f, 0.45f, 0.85f, 0.5f)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 24)

        // Vektor Jejak Bayangan Melesat DODGE
        val ddgCx = btnDodge.x
        val ddgCy = btnDodge.y + 4f
        shapeRenderer.color = Color(0.8f, 0.95f, 1f, 0.95f)
        shapeRenderer.rectLine(ddgCx - 14f, ddgCy + 7f, ddgCx + 6f, ddgCy + 7f, 3f)
        shapeRenderer.rectLine(ddgCx - 10f, ddgCy, ddgCx + 14f, ddgCy, 4f)
        shapeRenderer.rectLine(ddgCx - 14f, ddgCy - 7f, ddgCx + 6f, ddgCy - 7f, 3f)
        shapeRenderer.triangle(ddgCx + 17f, ddgCy, ddgCx + 11f, ddgCy + 5f, ddgCx + 11f, ddgCy - 5f)

        // Tombol Skill 1 (Ungu Arcane)
        val skillColor = if (skill.isReady) Color(0.7f, 0.3f, 0.95f, 0.65f) else Color(0.3f, 0.2f, 0.4f, 0.45f)
        shapeRenderer.color = skillColor
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 24)

        // Vektor Berlian Runic SKILL
        val sklCx = btnSkill.x
        val sklCy = btnSkill.y + 4f
        shapeRenderer.color = if (skill.isReady) Color(0.92f, 0.75f, 1f, 0.95f) else Color(0.55f, 0.45f, 0.65f, 0.6f)
        shapeRenderer.triangle(sklCx, sklCy + 14f, sklCx - 11f, sklCy, sklCx + 11f, sklCy)
        shapeRenderer.triangle(sklCx, sklCy - 14f, sklCx - 11f, sklCy, sklCx + 11f, sklCy)
        if (skill.isReady) {
            shapeRenderer.color = Color.WHITE
            shapeRenderer.triangle(sklCx, sklCy + 6f, sklCx - 5f, sklCy, sklCx + 5f, sklCy)
            shapeRenderer.triangle(sklCx, sklCy - 6f, sklCx - 5f, sklCy, sklCx + 5f, sklCy)
        }

        // Tombol Ultimate (Emas Surya menyala jika 100%)
        val ultColor = if (ultimate.isReady) Color(1f, 0.85f, 0.2f, 0.85f) else Color(0.45f, 0.38f, 0.15f, 0.45f)
        shapeRenderer.color = ultColor
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 24)

        // Vektor Mahkota Surya Bintang ULT
        val ultCx = btnUltimate.x
        val ultCy = btnUltimate.y + 4f
        shapeRenderer.color = if (ultimate.isReady) Color(1f, 0.95f, 0.4f, 1f) else Color(0.7f, 0.65f, 0.4f, 0.6f)
        shapeRenderer.circle(ultCx, ultCy, 7f, 16)
        shapeRenderer.rectLine(ultCx, ultCy + 8f, ultCx, ultCy + 16f, 3.5f)
        shapeRenderer.rectLine(ultCx, ultCy - 8f, ultCx, ultCy - 16f, 3.5f)
        shapeRenderer.rectLine(ultCx - 8f, ultCy, ultCx - 16f, ultCy, 3.5f)
        shapeRenderer.rectLine(ultCx + 8f, ultCy, ultCx + 16f, ultCy, 3.5f)
        shapeRenderer.rectLine(ultCx - 6f, ultCy + 6f, ultCx - 11f, ultCy + 11f, 2.5f)
        shapeRenderer.rectLine(ultCx + 6f, ultCy + 6f, ultCx + 12f, ultCy + 12f, 2.5f)
        shapeRenderer.rectLine(ultCx - 6f, ultCy - 6f, ultCx - 11f, ultCy - 11f, 2.5f)
        shapeRenderer.rectLine(ultCx + 6f, ultCy - 6f, ultCx + 12f, ultCy - 12f, 2.5f)

        shapeRenderer.end()

        // 2. Gambar Garis Tepi (Outline Lingkaran dengan Efek Halus)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.85f, 0.9f, 1f, 0.5f)
        shapeRenderer.circle(btnLeft.x, btnLeft.y, btnLeft.radius, 24)
        shapeRenderer.circle(btnRight.x, btnRight.y, btnRight.radius, 24)

        shapeRenderer.color = Color(1f, 0.6f, 0.6f, 0.6f)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 28)

        shapeRenderer.color = Color(0.6f, 1f, 0.7f, 0.6f)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 24)

        shapeRenderer.color = Color(0.6f, 0.8f, 1f, 0.6f)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 24)

        shapeRenderer.color = if (skill.isReady) Color(0.9f, 0.7f, 1f, 0.7f) else Color(0.5f, 0.4f, 0.6f, 0.4f)
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 24)

        shapeRenderer.color = if (ultimate.isReady) Color(1f, 0.95f, 0.4f, 0.9f) else Color(0.6f, 0.5f, 0.3f, 0.4f)
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 24)

        // Lingkaran cooldown pada tombol skill
        if (!skill.isReady) {
            shapeRenderer.color = Color(0.1f, 0.1f, 0.15f, 0.8f)
            shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius * skill.cooldownPercent, 20)
        }
        shapeRenderer.end()

        // 3. Gambar Teks Label Tombol (Sub-label Halus & Countdown)
        batch.begin()
        font.color = Color(0.9f, 0.9f, 0.95f, 0.85f)

        // Sub-labels di bawah ikon
        font.draw(batch, "ATK", btnAttack.x - 14f, btnAttack.y - 22f)
        font.draw(batch, "JUMP", btnJump.x - 17f, btnJump.y - 18f)
        font.draw(batch, "DODGE", btnDodge.x - 22f, btnDodge.y - 18f)

        if (skill.isReady) {
            font.draw(batch, "SKILL", btnSkill.x - 17f, btnSkill.y - 18f)
        } else {
            font.color = Color.YELLOW
            val tenths = (skill.remainingCooldown * 10).toInt().coerceAtLeast(0)
            font.draw(batch, "${tenths / 10}.${tenths % 10}s", btnSkill.x - 14f, btnSkill.y + 4f)
        }

        if (ultimate.isReady) {
            font.color = Color.GOLD
            font.draw(batch, "ULT!", btnUltimate.x - 14f, btnUltimate.y - 18f)
        } else {
            font.color = Color(0.9f, 0.85f, 0.7f, 0.8f)
            font.draw(batch, "${(ultimate.energyPercent * 100).toInt()}%", btnUltimate.x - 14f, btnUltimate.y + 4f)
        }

        batch.end()
    }
}

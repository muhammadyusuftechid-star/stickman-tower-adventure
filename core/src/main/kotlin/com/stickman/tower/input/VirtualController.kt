package com.stickman.tower.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.combat.Skill
import com.stickman.tower.combat.UltimateEnergy

/**
 * On-Screen Virtual Touch Controller untuk target layar sentuh Android.
 * Menyediakan tombol D-Pad kiri-kanan serta tombol aksi (Attack, Jump, Dodge, Skill, Ultimate).
 * Mendukung multi-touch hingga 5 jari secara bersamaan.
 */
class VirtualController : GameInput {
    // Tombol D-Pad Kiri Layar
    private val btnLeft = Rectangle()
    private val btnRight = Rectangle()

    // Tombol Aksi Kanan Layar
    private val btnAttack = Circle()
    private val btnJump = Circle()
    private val btnDodge = Circle()
    private val btnSkill = Circle()
    private val btnUltimate = Circle()

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

    var isVisible = true

    fun resize(width: Int, height: Int) {
        val w = width.toFloat()
        val h = height.toFloat()

        // Ukuran dan Posisi D-Pad (Kiri Bawah)
        val btnSize = 85f
        btnLeft.set(30f, 30f, btnSize, btnSize)
        btnRight.set(135f, 30f, btnSize, btnSize)

        // Posisi Tombol Aksi (Kanan Bawah)
        btnAttack.set(w - 110f, 95f, 52f)     // Tombol Serang Utama
        btnJump.set(w - 205f, 50f, 40f)       // Tombol Lompat
        btnDodge.set(w - 215f, 150f, 38f)     // Tombol Dodge
        btnSkill.set(w - 110f, 220f, 40f)     // Tombol Skill 1
        btnUltimate.set(w - 205f, 245f, 42f)  // Tombol Ultimate
    }

    fun update() {
        // Simpan state penekanan sebelumnya
        val prevAttack = wasAttackPressed
        val prevJump = wasJumpPressed
        val prevDodge = wasDodgePressed
        val prevSkill = wasSkillPressed
        val prevUltimate = wasUltimatePressed

        // Reset state aktif
        pressedLeft = false
        pressedRight = false
        pressedJump = false
        var curAttack = false
        var curJump = false
        var curDodge = false
        var curSkill = false
        var curUltimate = false

        // Deteksi Multi-Touch (Pointer 0 s/d 4)
        for (pointer in 0..4) {
            if (Gdx.input.isTouched(pointer)) {
                val touchX = Gdx.input.getX(pointer).toFloat()
                val touchY = (Gdx.graphics.height - Gdx.input.getY(pointer)).toFloat() // Invert Y ke Screen Space

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

        // Hitung Just Pressed
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
     * Menggambar antarmuka tombol sentuh di layar menggunakan ShapeRenderer dan SpriteBatch.
     */
    fun render(
        shapeRenderer: ShapeRenderer,
        batch: SpriteBatch,
        font: BitmapFont,
        skill: Skill,
        ultimate: UltimateEnergy
    ) {
        if (!isVisible) return

        shapeRenderer.projectionMatrix.setToOrtho2D(0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        // 1. Gambar Background Lingkaran/Kotak Tombol (Translucent Glassmorphism)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // D-Pad Kiri & Kanan
        shapeRenderer.color = if (pressedLeft) Color(0.2f, 0.6f, 0.9f, 0.7f) else Color(0.15f, 0.18f, 0.24f, 0.5f)
        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height)

        shapeRenderer.color = if (pressedRight) Color(0.2f, 0.6f, 0.9f, 0.7f) else Color(0.15f, 0.18f, 0.24f, 0.5f)
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height)

        // Tombol Attack (Merah)
        shapeRenderer.color = if (wasAttackPressed) Color(1f, 0.2f, 0.25f, 0.85f) else Color(0.85f, 0.25f, 0.3f, 0.55f)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 24)

        // Tombol Jump (Hijau)
        shapeRenderer.color = if (wasJumpPressed) Color(0.25f, 0.9f, 0.4f, 0.85f) else Color(0.2f, 0.75f, 0.35f, 0.55f)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 20)

        // Tombol Dodge (Biru)
        shapeRenderer.color = if (wasDodgePressed) Color(0.3f, 0.7f, 1f, 0.85f) else Color(0.25f, 0.55f, 0.95f, 0.55f)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 20)

        // Tombol Skill 1 (Ungu dengan indikator cooldown)
        val skillColor = if (skill.isReady) Color(0.75f, 0.35f, 0.95f, 0.65f) else Color(0.35f, 0.25f, 0.45f, 0.5f)
        shapeRenderer.color = skillColor
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 20)

        // Tombol Ultimate (Emas menyala terang jika 100%)
        val ultColor = if (ultimate.isReady) Color(1f, 0.85f, 0.2f, 0.85f) else Color(0.45f, 0.4f, 0.2f, 0.5f)
        shapeRenderer.color = ultColor
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 22)

        shapeRenderer.end()

        // 2. Gambar Garis Tepi (Outline)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE

        shapeRenderer.rect(btnLeft.x, btnLeft.y, btnLeft.width, btnLeft.height)
        shapeRenderer.rect(btnRight.x, btnRight.y, btnRight.width, btnRight.height)
        shapeRenderer.circle(btnAttack.x, btnAttack.y, btnAttack.radius, 24)
        shapeRenderer.circle(btnJump.x, btnJump.y, btnJump.radius, 20)
        shapeRenderer.circle(btnDodge.x, btnDodge.y, btnDodge.radius, 20)
        shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius, 20)
        shapeRenderer.circle(btnUltimate.x, btnUltimate.y, btnUltimate.radius, 22)

        // Lingkaran cooldown pada tombol skill
        if (!skill.isReady) {
            shapeRenderer.color = Color.BLACK
            shapeRenderer.circle(btnSkill.x, btnSkill.y, btnSkill.radius * skill.cooldownPercent, 16)
        }
        shapeRenderer.end()

        // 3. Gambar Teks Label Tombol
        batch.begin()
        font.color = Color.WHITE

        // D-Pad Labels
        font.draw(batch, "◀", btnLeft.x + 35f, btnLeft.y + 50f)
        font.draw(batch, "▶", btnRight.x + 35f, btnRight.y + 50f)

        // Action Labels
        font.draw(batch, "ATK", btnAttack.x - 16f, btnAttack.y + 6f)
        font.draw(batch, "JUMP", btnJump.x - 18f, btnJump.y + 6f)
        font.draw(batch, "DODGE", btnDodge.x - 24f, btnDodge.y + 6f)

        if (skill.isReady) {
            font.draw(batch, "SKILL", btnSkill.x - 18f, btnSkill.y + 6f)
        } else {
            font.color = Color.YELLOW
            font.draw(batch, String.format("%.1fs", skill.remainingCooldown), btnSkill.x - 14f, btnSkill.y + 6f)
            font.color = Color.WHITE
        }

        if (ultimate.isReady) {
            font.color = Color.GOLD
            font.draw(batch, "ULT!", btnUltimate.x - 16f, btnUltimate.y + 6f)
        } else {
            font.color = Color.LIGHT_GRAY
            font.draw(batch, "${(ultimate.energyPercent * 100).toInt()}%", btnUltimate.x - 14f, btnUltimate.y + 6f)
        }

        batch.end()
    }
}

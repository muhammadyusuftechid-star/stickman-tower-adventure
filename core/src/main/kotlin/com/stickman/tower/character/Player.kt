package com.stickman.tower.character

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.stickman.tower.input.GameInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

/**
 * Entitas karakter pemain dengan kontrol kinematik responsif,
 * coyote time, jump buffering, dan rendering stickman dinamis.
 */
class Player(spawnX: Float = 0f, spawnY: Float = 2f) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    // Dimensi bounding box karakter
    val width = 0.8f
    val height = 1.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    // Status Karakter
    var state: CharacterState = CharacterState.IDLE
        private set
    var isGrounded: Boolean = false
        private set
    var facingRight: Boolean = true
        private set

    // Timer Toleransi Platformer Modern
    private var coyoteTimer: Float = 0f
    private var jumpBufferTimer: Float = 0f

    // Timer Animasi Visual
    private var animTime: Float = 0f

    fun update(delta: Float, input: GameInput, platforms: List<Platform>) {
        animTime += delta

        // 1. Input Gerak Horizontal
        val moveDir = when {
            input.isMoveLeft() -> -1f
            input.isMoveRight() -> 1f
            else -> 0f
        }

        if (moveDir != 0f) {
            facingRight = moveDir > 0
            val targetVx = moveDir * GameConfig.MOVE_SPEED
            val accelStep = GameConfig.ACCELERATION * delta
            velocity.x = MathUtils.lerp(velocity.x, targetVx, accelStep.coerceAtMost(1f))
        } else {
            val decelStep = GameConfig.DECELERATION * delta
            velocity.x = MathUtils.lerp(velocity.x, 0f, decelStep.coerceAtMost(1f))
            if (abs(velocity.x) < 0.05f) velocity.x = 0f
        }

        // 2. Pembaruan Timer Coyote & Jump Buffer
        if (isGrounded) {
            coyoteTimer = GameConfig.COYOTE_TIME
        } else {
            coyoteTimer = (coyoteTimer - delta).coerceAtLeast(0f)
        }

        if (input.isJumpJustPressed()) {
            jumpBufferTimer = GameConfig.JUMP_BUFFER_TIME
        } else {
            jumpBufferTimer = (jumpBufferTimer - delta).coerceAtLeast(0f)
        }

        // 3. Eksekusi Lompatan
        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocity.y = GameConfig.JUMP_VELOCITY
            jumpBufferTimer = 0f
            coyoteTimer = 0f
            isGrounded = false
        }

        // 4. Gravitasi & Lompatan Variabel (Variable Jump Height)
        val gravityMultiplier = if (velocity.y > 0f && !input.isJumpPressed()) 1.8f else 1f
        velocity.y += GameConfig.GRAVITY * gravityMultiplier * delta
        velocity.y = velocity.y.coerceAtLeast(-25f) // Terminal velocity

        // 5. Resolusi Collision Axis-Separated (X terlebih dahulu, lalu Y)
        resolveCollisionX(delta, platforms)
        resolveCollisionY(delta, platforms)

        // 6. Pembaruan State Machine
        updateState()
    }

    private fun resolveCollisionX(delta: Float, platforms: List<Platform>) {
        position.x += velocity.x * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (bounds.overlaps(platform.bounds)) {
                if (velocity.x > 0f) {
                    position.x = platform.left - width
                    velocity.x = 0f
                } else if (velocity.x < 0f) {
                    position.x = platform.right
                    velocity.x = 0f
                }
                bounds.setPosition(position.x, position.y)
            }
        }
    }

    private fun resolveCollisionY(delta: Float, platforms: List<Platform>) {
        isGrounded = false
        position.y += velocity.y * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (bounds.overlaps(platform.bounds)) {
                if (velocity.y <= 0f) {
                    // Mendarat di atas platform
                    position.y = platform.top
                    velocity.y = 0f
                    isGrounded = true
                } else if (velocity.y > 0f) {
                    // Membentur langit-langit
                    position.y = platform.bottom - height
                    velocity.y = 0f
                }
                bounds.setPosition(position.x, position.y)
            }
        }
    }

    private fun updateState() {
        state = when {
            !isGrounded && velocity.y > 0.5f -> CharacterState.JUMP
            !isGrounded && velocity.y <= 0.5f -> CharacterState.FALL
            isGrounded && abs(velocity.x) > 0.1f -> CharacterState.RUN
            else -> CharacterState.IDLE
        }
    }

    /**
     * Menggambar stickman visual proporsional dengan persendian animasi.
     */
    fun render(shapeRenderer: ShapeRenderer) {
        val centerX = position.x + width / 2f
        val footY = position.y
        val headRadius = 0.22f
        val headCenterY = position.y + height - headRadius - 0.05f
        val neckY = headCenterY - headRadius
        val pelvisY = position.y + 0.75f
        val shoulderY = neckY - 0.15f
        val lookDir = if (facingRight) 1f else -1f

        // Warna Stickman (Garis Putih Terang dengan Outline Hitam)
        shapeRenderer.color = Color.WHITE

        // 1. Gambar Kepala
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // 2. Mata (Menghadap arah karakter)
        shapeRenderer.color = Color.DARK_GRAY
        val eyeX = centerX + (headRadius * 0.45f * lookDir)
        val eyeY = headCenterY + 0.04f
        shapeRenderer.circle(eyeX, eyeY, 0.04f, 8)

        // Kembali ke warna tubuh
        shapeRenderer.color = Color.WHITE

        // 3. Tulang Belakang (Spine)
        shapeRenderer.rectLine(centerX, neckY, centerX, pelvisY, 0.08f)

        // 4. Perhitungan Rotasi Kaki & Tangan
        var legSwing = 0f
        var armSwing = 0f

        when (state) {
            CharacterState.RUN -> {
                val cycle = sin(animTime * 14f)
                legSwing = cycle * 0.38f
                armSwing = -cycle * 0.35f
            }
            CharacterState.JUMP -> {
                legSwing = -0.15f
                armSwing = 0.3f
            }
            CharacterState.FALL -> {
                legSwing = 0.12f
                armSwing = 0.2f
            }
            CharacterState.IDLE -> {
                val breathe = sin(animTime * 3f) * 0.02f
                legSwing = 0.04f
                armSwing = breathe
            }
        }

        // 5. Kaki Kiri & Kanan
        val legLeftFootX = centerX - 0.12f * lookDir + (legSwing * lookDir)
        val legRightFootX = centerX + 0.12f * lookDir - (legSwing * lookDir)
        val legLeftFootY = footY + (if (state == CharacterState.RUN && legSwing > 0) 0.15f else 0f)
        val legRightFootY = footY + (if (state == CharacterState.RUN && legSwing < 0) 0.15f else 0f)

        shapeRenderer.rectLine(centerX, pelvisY, legLeftFootX, legLeftFootY, 0.07f)
        shapeRenderer.rectLine(centerX, pelvisY, legRightFootX, legRightFootY, 0.07f)

        // 6. Tangan Kiri & Kanan
        val armLeftHandX = centerX - 0.28f * lookDir - (armSwing * lookDir)
        val armRightHandX = centerX + 0.28f * lookDir + (armSwing * lookDir)
        val handY = shoulderY - 0.35f

        shapeRenderer.rectLine(centerX, shoulderY, armLeftHandX, handY, 0.06f)
        shapeRenderer.rectLine(centerX, shoulderY, armRightHandX, handY, 0.06f)
    }

    /**
     * Render bounding box debug (garis hijau).
     */
    fun renderDebug(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
    }
}

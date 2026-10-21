package com.stickman.tower.character

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.stickman.tower.combat.AttackPhase
import com.stickman.tower.combat.Health
import com.stickman.tower.combat.Hitbox
import com.stickman.tower.combat.Hurtbox
import com.stickman.tower.input.GameInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

/**
 * Entitas karakter pemain dengan kontrol kinematik responsif,
 * coyote time, jump buffering, sistem combat hitbox/hurtbox,
 * serta animasi stickman dinamis dengan tebasan pedang.
 */
class Player(spawnX: Float = 0f, spawnY: Float = 2f) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    // Dimensi bounding box karakter
    val width = 0.8f
    val height = 1.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    // Komponen Combat
    val health = Health(maxHp = 100f, invulnerableDuration = 0.35f)
    val hurtbox = Hurtbox(this)
    val hitbox = Hitbox(this, damage = 15f, knockbackX = 6.5f, knockbackY = 3.5f)

    // Siklus Fase Serangan
    var attackPhase: AttackPhase = AttackPhase.INACTIVE
        private set
    private var attackTimer: Float = 0f

    companion object {
        const val ATTACK_STARTUP = 0.08f
        const val ATTACK_ACTIVE = 0.12f
        const val ATTACK_RECOVERY = 0.15f
        const val TOTAL_ATTACK_TIME = ATTACK_STARTUP + ATTACK_ACTIVE + ATTACK_RECOVERY
    }

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
        health.update(delta)

        // Perbarui posisi hurtbox agar selalu mengikuti tubuh
        hurtbox.set(position.x, position.y, width, height)

        // 1. Cek Pemicu Serangan
        if (input.isAttackJustPressed() && attackPhase == AttackPhase.INACTIVE && !health.isDead) {
            attackPhase = AttackPhase.STARTUP
            attackTimer = 0f
            hitbox.reset()
            // Kurangi kecepatan saat mulai menebas untuk memberi bobot
            velocity.x *= 0.3f
        }

        // 2. Pembaruan Siklus Serangan
        updateAttackLifecycle(delta)

        // 3. Input Gerak Horizontal (Hanya bisa bergerak bebas jika tidak sedang Startup/Active attack)
        val canMove = attackPhase == AttackPhase.INACTIVE || attackPhase == AttackPhase.RECOVERY
        val moveDir = when {
            canMove && input.isMoveLeft() -> -1f
            canMove && input.isMoveRight() -> 1f
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

        // 4. Pembaruan Timer Coyote & Jump Buffer
        if (isGrounded) {
            coyoteTimer = GameConfig.COYOTE_TIME
        } else {
            coyoteTimer = (coyoteTimer - delta).coerceAtLeast(0f)
        }

        if (input.isJumpJustPressed() && canMove) {
            jumpBufferTimer = GameConfig.JUMP_BUFFER_TIME
        } else {
            jumpBufferTimer = (jumpBufferTimer - delta).coerceAtLeast(0f)
        }

        // 5. Eksekusi Lompatan
        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocity.y = GameConfig.JUMP_VELOCITY
            jumpBufferTimer = 0f
            coyoteTimer = 0f
            isGrounded = false
        }

        // 6. Gravitasi & Lompatan Variabel (Variable Jump Height)
        val gravityMultiplier = if (velocity.y > 0f && !input.isJumpPressed()) 1.8f else 1f
        velocity.y += GameConfig.GRAVITY * gravityMultiplier * delta
        velocity.y = velocity.y.coerceAtLeast(-25f) // Terminal velocity

        // 7. Resolusi Collision Axis-Separated (X terlebih dahulu, lalu Y)
        resolveCollisionX(delta, platforms)
        resolveCollisionY(delta, platforms)

        // 8. Pembaruan State Machine
        updateState()
    }

    private fun updateAttackLifecycle(delta: Float) {
        if (attackPhase == AttackPhase.INACTIVE) return

        attackTimer += delta

        when {
            attackTimer < ATTACK_STARTUP -> {
                attackPhase = AttackPhase.STARTUP
                hitbox.isActive = false
            }
            attackTimer < ATTACK_STARTUP + ATTACK_ACTIVE -> {
                attackPhase = AttackPhase.ACTIVE
                // Posisikan Hitbox aktif di depan arah hadap karakter
                val hitboxW = 1.3f
                val hitboxH = 1.3f
                val hitboxX = if (facingRight) position.x + width * 0.6f else position.x - hitboxW + width * 0.4f
                val hitboxY = position.y + 0.35f
                hitbox.set(hitboxX, hitboxY, hitboxW, hitboxH, active = true)
            }
            attackTimer < TOTAL_ATTACK_TIME -> {
                attackPhase = AttackPhase.RECOVERY
                hitbox.isActive = false
            }
            else -> {
                attackPhase = AttackPhase.INACTIVE
                hitbox.reset()
            }
        }
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
            attackPhase != AttackPhase.INACTIVE -> CharacterState.ATTACK
            health.isFlashing -> CharacterState.HURT
            !isGrounded && velocity.y > 0.5f -> CharacterState.JUMP
            !isGrounded && velocity.y <= 0.5f -> CharacterState.FALL
            isGrounded && abs(velocity.x) > 0.1f -> CharacterState.RUN
            else -> CharacterState.IDLE
        }
    }

    /**
     * Menerima knockback dan damage jika terkena serangan musuh.
     */
    fun takeHit(amount: Float, knockDirX: Float, knockDirY: Float): Boolean {
        if (health.takeDamage(amount)) {
            velocity.x = knockDirX
            velocity.y = knockDirY
            return true
        }
        return false
    }

    /**
     * Menggambar stickman visual proporsional dengan pedang dan efek tebasan.
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

        // Warna Stickman (Merah saat flash damage, putih saat normal)
        val bodyColor = if (health.isFlashing) Color.RED else Color.WHITE
        shapeRenderer.color = bodyColor

        // 1. Gambar Kepala
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // 2. Mata
        shapeRenderer.color = if (health.isFlashing) Color.YELLOW else Color.DARK_GRAY
        val eyeX = centerX + (headRadius * 0.45f * lookDir)
        val eyeY = headCenterY + 0.04f
        shapeRenderer.circle(eyeX, eyeY, 0.04f, 8)

        // 3. Tulang Belakang (Spine)
        shapeRenderer.color = bodyColor
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
            CharacterState.ATTACK -> {
                legSwing = 0.1f // Kuda-kuda serang
                armSwing = 0.7f // Ayunan tangan ke depan
            }
            CharacterState.HURT -> {
                legSwing = -0.2f
                armSwing = -0.4f
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

        // 6. Tangan Belakang
        val armBackX = centerX - 0.25f * lookDir - (armSwing * lookDir * 0.5f)
        val armBackY = shoulderY - 0.35f
        shapeRenderer.rectLine(centerX, shoulderY, armBackX, armBackY, 0.06f)

        // 7. Tangan Depan (Pemegang Senjata Pedang)
        val armFrontHandX: Float
        val armFrontHandY: Float
        val swordTipX: Float
        val swordTipY: Float

        if (attackPhase == AttackPhase.STARTUP) {
            // Ancang-ancang: mengangkat pedang ke belakang atas
            armFrontHandX = centerX - 0.15f * lookDir
            armFrontHandY = shoulderY + 0.2f
            swordTipX = armFrontHandX - 0.8f * lookDir
            swordTipY = armFrontHandY + 0.6f
        } else if (attackPhase == AttackPhase.ACTIVE) {
            // Tebasan ke depan kuat
            armFrontHandX = centerX + 0.45f * lookDir
            armFrontHandY = shoulderY - 0.05f
            swordTipX = armFrontHandX + 0.95f * lookDir
            swordTipY = armFrontHandY - 0.2f
        } else {
            // Normal pegang pedang menghadap depan bawah
            armFrontHandX = centerX + 0.25f * lookDir + (armSwing * lookDir)
            armFrontHandY = shoulderY - 0.35f
            swordTipX = armFrontHandX + 0.6f * lookDir
            swordTipY = armFrontHandY - 0.4f
        }

        shapeRenderer.rectLine(centerX, shoulderY, armFrontHandX, armFrontHandY, 0.06f)

        // 8. Bilah Pedang (Sword Blade)
        shapeRenderer.color = Color(0.85f, 0.9f, 1f, 1f) // Silver Steel
        shapeRenderer.rectLine(armFrontHandX, armFrontHandY, swordTipX, swordTipY, 0.07f)

        // Gagang Pedang (Hilt)
        shapeRenderer.color = Color(0.6f, 0.4f, 0.2f, 1f) // Gold/Brown
        val hiltDirX = (swordTipY - armFrontHandY) * 0.15f
        val hiltDirY = -(swordTipX - armFrontHandX) * 0.15f
        shapeRenderer.rectLine(
            armFrontHandX - hiltDirX,
            armFrontHandY - hiltDirY,
            armFrontHandX + hiltDirX,
            armFrontHandY + hiltDirY,
            0.05f
        )

        // 9. Efek Visual Tebasan (Slash Arc) saat phase ACTIVE
        if (attackPhase == AttackPhase.ACTIVE) {
            renderSlashArc(shapeRenderer, centerX, shoulderY, lookDir)
        }
    }

    private fun renderSlashArc(shapeRenderer: ShapeRenderer, centerX: Float, centerY: Float, lookDir: Float) {
        val arcRadius = 1.15f
        val arcColor = Color(1f, 0.92f, 0.3f, 0.85f) // Glowing Yellow/Gold
        shapeRenderer.color = arcColor

        val arcCenterX = centerX + 0.25f * lookDir
        val arcCenterY = centerY - 0.1f

        // Gambar lengkungan tebasan energi sabit
        val segments = 8
        val startAngle = if (lookDir > 0) 60f else 120f
        val sweep = if (lookDir > 0) -110f else 110f

        var prevX = arcCenterX + arcRadius * MathUtils.cosDeg(startAngle)
        var prevY = arcCenterY + arcRadius * MathUtils.sinDeg(startAngle)

        for (i in 1..segments) {
            val angle = startAngle + (sweep * (i.toFloat() / segments))
            val currentRadius = arcRadius + sin(i.toFloat() / segments * MathUtils.PI) * 0.18f
            val curX = arcCenterX + currentRadius * MathUtils.cosDeg(angle)
            val curY = arcCenterY + currentRadius * MathUtils.sinDeg(angle)

            val lineWidth = 0.06f + sin(i.toFloat() / segments * MathUtils.PI) * 0.08f
            shapeRenderer.rectLine(prevX, prevY, curX, curY, lineWidth)

            prevX = curX
            prevY = curY
        }
    }

    /**
     * Render bounding box debug (Hijau = Collision, Biru = Hurtbox, Merah = Hitbox).
     */
    fun renderDebug(shapeRenderer: ShapeRenderer) {
        // Collision Box (Hijau)
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)

        // Hurtbox (Biru)
        hurtbox.renderDebug(shapeRenderer)

        // Hitbox (Merah)
        hitbox.renderDebug(shapeRenderer)
    }
}

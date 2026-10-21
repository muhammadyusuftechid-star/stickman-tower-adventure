package com.stickman.tower.character

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.stickman.tower.combat.AttackPhase
import com.stickman.tower.combat.ComboManager
import com.stickman.tower.combat.ComboStep
import com.stickman.tower.combat.Health
import com.stickman.tower.combat.Hitbox
import com.stickman.tower.combat.Hurtbox
import com.stickman.tower.combat.Skill
import com.stickman.tower.combat.UltimateEnergy
import com.stickman.tower.input.GameInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.rpg.CharacterStats
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

/**
 * Entitas Karakter Pemain Fase 5:
 * Didukung model RPG CharacterStats (scaling level, HP, Attack, Defense, Critical),
 * Combo 3-Hit Chain, Dodge Roll ber-i-frames, Skill 1 Dash Strike, dan Ultimate Bladestorm.
 */
class Player(
    spawnX: Float = 0f,
    spawnY: Float = 2f,
    val stats: CharacterStats = CharacterStats()
) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    val width = 0.8f
    val height = 1.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    // Komponen Pertarungan & RPG
    val health = Health(maxHp = stats.maxHp, invulnerableDuration = 0.35f)
    val hurtbox = Hurtbox(this)
    val hitbox = Hitbox(this, damage = stats.calculateDamage(12f).first, knockbackX = 4f, knockbackY = 2.5f)

    val comboManager = ComboManager()
    val skill1 = Skill(name = "Dash Strike", cooldown = 4.5f)
    val ultimate = UltimateEnergy(maxEnergy = 100f)

    // Siklus Fase Serangan Kombo
    var attackPhase: AttackPhase = AttackPhase.INACTIVE
        private set
    private var attackTimer: Float = 0f

    // Parameter Dodge / Dash Roll
    var isDodging = false
        private set
    private var dodgeTimer = 0f
    var dodgeCooldown = 0f
        private set
    private val afterimages = mutableListOf<Vector2>()

    // Parameter Skill 1 (Dash Strike)
    var isSkillStriking = false
        private set
    private var skillTimer = 0f

    // Parameter Ultimate (Bladestorm)
    var isUlting = false
        private set
    private var ultTimer = 0f
    val ultRadius = 3.6f

    companion object {
        const val DODGE_DURATION = 0.22f
        const val DODGE_SPEED = 14.5f
        const val DODGE_COOLDOWN = 0.65f

        const val SKILL_DURATION = 0.18f
        const val SKILL_SPEED = 22.0f

        const val ULT_DURATION = 0.45f
    }

    var state: CharacterState = CharacterState.IDLE
        private set
    var isGrounded: Boolean = false
        private set
    var facingRight: Boolean = true
        private set

    private var coyoteTimer: Float = 0f
    private var jumpBufferTimer: Float = 0f
    private var animTime: Float = 0f

    fun update(delta: Float, input: GameInput, platforms: List<Platform>) {
        animTime += delta
        health.update(delta)
        skill1.update(delta)
        comboManager.update(delta, attackPhase != AttackPhase.INACTIVE)

        if (dodgeCooldown > 0f) {
            dodgeCooldown = (dodgeCooldown - delta).coerceAtLeast(0f)
        }

        hurtbox.set(position.x, position.y, width, height)

        if (!health.isDead) {
            // 1. Cek Pemicu Aksi Khusus
            handleActionTriggers(input)
        }

        // 2. Pembaruan State Khusus (Dodge, Skill, Ultimate, Combo)
        updateSpecialActions(delta)

        // 3. Gerak Horizontal & Fisika (Jika tidak dalam Dodge / Skill / Attack Startup)
        val canControlMove = !isDodging && !isSkillStriking && !isUlting &&
                             (attackPhase == AttackPhase.INACTIVE || attackPhase == AttackPhase.RECOVERY)

        val moveDir = when {
            canControlMove && input.isMoveLeft() -> -1f
            canControlMove && input.isMoveRight() -> 1f
            else -> 0f
        }

        if (canControlMove) {
            if (moveDir != 0f) {
                facingRight = moveDir > 0
                val targetVx = moveDir * GameConfig.MOVE_SPEED
                velocity.x = MathUtils.lerp(velocity.x, targetVx, (GameConfig.ACCELERATION * delta).coerceAtMost(1f))
            } else {
                velocity.x = MathUtils.lerp(velocity.x, 0f, (GameConfig.DECELERATION * delta).coerceAtMost(1f))
                if (abs(velocity.x) < 0.05f) velocity.x = 0f
            }
        }

        // 4. Coyote Time & Jump Buffer
        if (isGrounded) coyoteTimer = GameConfig.COYOTE_TIME
        else coyoteTimer = (coyoteTimer - delta).coerceAtLeast(0f)

        if (input.isJumpJustPressed() && canControlMove) jumpBufferTimer = GameConfig.JUMP_BUFFER_TIME
        else jumpBufferTimer = (jumpBufferTimer - delta).coerceAtLeast(0f)

        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocity.y = GameConfig.JUMP_VELOCITY
            jumpBufferTimer = 0f
            coyoteTimer = 0f
            isGrounded = false
        }

        // 5. Gravitasi
        val gravityMultiplier = if (velocity.y > 0f && !input.isJumpPressed()) 1.8f else 1f
        velocity.y += GameConfig.GRAVITY * gravityMultiplier * delta
        velocity.y = velocity.y.coerceAtLeast(-25f)

        // 6. Resolusi Collision
        resolveCollisionX(delta, platforms)
        resolveCollisionY(delta, platforms)

        // 7. Update State Machine
        updateState()
    }

    private fun handleActionTriggers(input: GameInput) {
        // Pemicu Dodge Roll
        if (input.isDodgeJustPressed() && !isDodging && !isSkillStriking && !isUlting && dodgeCooldown <= 0f) {
            isDodging = true
            dodgeTimer = 0f
            dodgeCooldown = DODGE_COOLDOWN
            attackPhase = AttackPhase.INACTIVE
            hitbox.reset()
            afterimages.clear()
            // Berikan i-Frames kebal penuh selama dodge
            health.takeDamage(0f)
            return
        }

        // Pemicu Skill 1 (Dash Strike)
        if (input.isSkillJustPressed() && skill1.isReady && !isDodging && !isSkillStriking && !isUlting) {
            if (skill1.use()) {
                isSkillStriking = true
                skillTimer = 0f
                attackPhase = AttackPhase.INACTIVE
                val (dmg, isCrit) = stats.calculateDamage(30f)
                hitbox.damage = dmg
                hitbox.isCritical = isCrit
                hitbox.knockbackX = 8.5f
                hitbox.knockbackY = 4.0f
                hitbox.reset()
                return
            }
        }

        // Pemicu Ultimate (Bladestorm)
        if (input.isUltimateJustPressed() && ultimate.isReady && !isDodging && !isSkillStriking && !isUlting) {
            if (ultimate.consume()) {
                isUlting = true
                ultTimer = 0f
                attackPhase = AttackPhase.INACTIVE
                velocity.set(0f, 0f)
                val (dmg, isCrit) = stats.calculateDamage(65f)
                hitbox.damage = dmg
                hitbox.isCritical = isCrit
                hitbox.knockbackX = 12.0f
                hitbox.knockbackY = 6.0f
                hitbox.reset()
                return
            }
        }

        // Pemicu Serangan Kombo (Attack)
        if (input.isAttackJustPressed() && !isDodging && !isSkillStriking && !isUlting) {
            if (attackPhase == AttackPhase.INACTIVE) {
                val hitConfig = comboManager.startNextHit()
                attackPhase = AttackPhase.STARTUP
                attackTimer = 0f
                val (dmg, isCrit) = stats.calculateDamage(hitConfig.damage)
                hitbox.damage = dmg
                hitbox.isCritical = isCrit
                hitbox.knockbackX = hitConfig.knockbackX
                hitbox.knockbackY = hitConfig.knockbackY
                hitbox.reset()
                velocity.x *= 0.3f
            } else if (attackPhase == AttackPhase.RECOVERY || (attackPhase == AttackPhase.ACTIVE && attackTimer > 0.06f)) {
                comboManager.hasBufferedInput = true
            }
        }
    }

    private fun updateSpecialActions(delta: Float) {
        // A. Update Dodge
        if (isDodging) {
            dodgeTimer += delta
            val dashDir = if (facingRight) 1f else -1f
            velocity.x = dashDir * DODGE_SPEED
            velocity.y = 0f // Meluncur lurus

            // Simpan jejak afterimage
            if (afterimages.size < 4) {
                afterimages.add(Vector2(position.x, position.y))
            }

            if (dodgeTimer >= DODGE_DURATION) {
                isDodging = false
                velocity.x *= 0.3f
            }
            return
        }

        // B. Update Skill 1 (Dash Strike)
        if (isSkillStriking) {
            skillTimer += delta
            val strikeDir = if (facingRight) 1f else -1f
            velocity.x = strikeDir * SKILL_SPEED
            velocity.y = 0f

            // Aktifkan Hitbox Dash Strike di sepanjang tubuh
            hitbox.set(position.x - 0.2f, position.y, width + 0.8f, height, active = true)

            if (skillTimer >= SKILL_DURATION) {
                isSkillStriking = false
                hitbox.reset()
                velocity.x *= 0.2f
            }
            return
        }

        // C. Update Ultimate (Bladestorm)
        if (isUlting) {
            ultTimer += delta
            velocity.set(0f, 0f) // Mengambang di udara/tanah saat melepaskan badai

            // Hitbox Radial 360 Derajat
            val centerX = position.x + width / 2f
            val centerY = position.y + height / 2f
            hitbox.set(centerX - ultRadius, centerY - ultRadius, ultRadius * 2f, ultRadius * 2f, active = true)

            if (ultTimer >= ULT_DURATION) {
                isUlting = false
                hitbox.reset()
            }
            return
        }

        // D. Update Siklus Serangan Kombo Biasa
        if (attackPhase != AttackPhase.INACTIVE) {
            attackTimer += delta
            val cfg = comboManager.currentConfig

            when {
                attackTimer < cfg.startup -> {
                    attackPhase = AttackPhase.STARTUP
                    hitbox.isActive = false
                }
                attackTimer < cfg.startup + cfg.active -> {
                    attackPhase = AttackPhase.ACTIVE
                    val hbW = if (comboManager.currentStep == ComboStep.HIT_3) 1.6f else 1.3f
                    val hbH = if (comboManager.currentStep == ComboStep.HIT_3) 1.6f else 1.3f
                    val hbX = if (facingRight) position.x + width * 0.6f else position.x - hbW + width * 0.4f
                    val hbY = position.y + 0.35f
                    hitbox.set(hbX, hbY, hbW, hbH, active = true)
                }
                attackTimer < cfg.totalTime -> {
                    attackPhase = AttackPhase.RECOVERY
                    hitbox.isActive = false
                }
                else -> {
                    // Serangan selesai: cek apakah ada buffer combo untuk pukulan berikutnya
                    if (comboManager.hasBufferedInput) {
                        val nextCfg = comboManager.startNextHit()
                        attackPhase = AttackPhase.STARTUP
                        attackTimer = 0f
                        val (dmg, isCrit) = stats.calculateDamage(nextCfg.damage)
                        hitbox.damage = dmg
                        hitbox.isCritical = isCrit
                        hitbox.knockbackX = nextCfg.knockbackX
                        hitbox.knockbackY = nextCfg.knockbackY
                        hitbox.reset()
                    } else {
                        attackPhase = AttackPhase.INACTIVE
                        hitbox.reset()
                    }
                }
            }
        }
    }

    private fun resolveCollisionX(delta: Float, platforms: List<Platform>) {
        position.x += velocity.x * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            // Abaikan tabrakan horizontal pada One-Way Platform
            if (platform.isOneWay) continue

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
        val previousY = position.y
        isGrounded = false
        position.y += velocity.y * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (platform.isOneWay) {
                // One-Way Platform: Hanya mendarat jika sedang jatuh dari atas platform
                if (velocity.y <= 0f && previousY >= platform.top - 0.20f) {
                    if (bounds.overlaps(platform.bounds)) {
                        position.y = platform.top
                        velocity.y = 0f
                        isGrounded = true
                        bounds.setPosition(position.x, position.y)
                    }
                }
                continue
            }

            if (bounds.overlaps(platform.bounds)) {
                if (velocity.y <= 0f) {
                    position.y = platform.top
                    velocity.y = 0f
                    isGrounded = true
                } else if (velocity.y > 0f) {
                    position.y = platform.bottom - height
                    velocity.y = 0f
                }
                bounds.setPosition(position.x, position.y)
            }
        }
    }

    private fun updateState() {
        state = when {
            isUlting || isSkillStriking || attackPhase != AttackPhase.INACTIVE -> CharacterState.ATTACK
            health.isFlashing -> CharacterState.HURT
            !isGrounded && velocity.y > 0.5f -> CharacterState.JUMP
            !isGrounded && velocity.y <= 0.5f -> CharacterState.FALL
            isGrounded && abs(velocity.x) > 0.1f -> CharacterState.RUN
            else -> CharacterState.IDLE
        }
    }

    fun takeHit(amount: Float, knockDirX: Float, knockDirY: Float): Boolean {
        // Jika sedang dalam Dodge Roll, kebal dari serangan musuh!
        if (isDodging) return false

        val mitigatedDmg = kotlin.math.max(1f, amount - stats.defense)
        if (health.takeDamage(mitigatedDmg)) {
            velocity.x = knockDirX
            velocity.y = knockDirY
            return true
        }
        return false
    }

    /**
     * Dipanggil saat pemain naik level:
     * Menyesuaikan Max HP dengan stat baru dan memulihkan HP secara penuh.
     */
    fun onLevelUp(levelsGained: Int) {
        health.maxHp = stats.maxHp
        health.reset()
    }

    /**
     * Mereset status HP dan energi sesuai statistik tersimpan.
     */
    fun resetToStats() {
        health.maxHp = stats.maxHp
        health.reset()
        ultimate.reset()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val centerX = position.x + width / 2f
        val footY = position.y
        val headRadius = 0.22f
        val headCenterY = position.y + height - headRadius - 0.05f
        val neckY = headCenterY - headRadius
        val pelvisY = position.y + 0.75f
        val shoulderY = neckY - 0.15f
        val lookDir = if (facingRight) 1f else -1f

        // 1. Gambar Afterimages (Jejak Bayangan saat Dodge Roll)
        if (isDodging) {
            for ((idx, ghost) in afterimages.withIndex()) {
                val alpha = (0.2f * (idx + 1)).coerceIn(0.1f, 0.6f)
                shapeRenderer.color = Color(0.3f, 0.7f, 1f, alpha)
                shapeRenderer.circle(ghost.x + width / 2f, ghost.y + height - headRadius - 0.05f, headRadius, 10)
                shapeRenderer.rectLine(ghost.x + width / 2f, ghost.y + 0.75f, ghost.x + width / 2f, ghost.y, 0.06f)
            }
        }

        // 2. Warna Tubuh (Biru saat Dodge, Ungu saat Skill, Emas saat Ult, Merah saat Hurt, Putih normal)
        val bodyColor = when {
            health.isFlashing -> Color.RED
            isDodging -> Color(0.4f, 0.8f, 1f, 1f)
            isSkillStriking -> Color(0.85f, 0.45f, 1f, 1f)
            isUlting -> Color.GOLD
            else -> Color.WHITE
        }
        shapeRenderer.color = bodyColor

        // Kepala
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // Mata
        shapeRenderer.color = if (health.isFlashing) Color.YELLOW else Color.DARK_GRAY
        val eyeX = centerX + (headRadius * 0.45f * lookDir)
        val eyeY = headCenterY + 0.04f
        shapeRenderer.circle(eyeX, eyeY, 0.04f, 8)

        // Tulang Belakang
        shapeRenderer.color = bodyColor
        shapeRenderer.rectLine(centerX, neckY, centerX, pelvisY, 0.08f)

        // Kaki & Tangan
        var legSwing = 0f
        var armSwing = 0f

        when {
            isDodging -> {
                legSwing = -0.4f // Kaki menekuk meluncur
                armSwing = -0.5f
            }
            state == CharacterState.RUN -> {
                val cycle = sin(animTime * 14f)
                legSwing = cycle * 0.38f
                armSwing = -cycle * 0.35f
            }
            state == CharacterState.JUMP -> {
                legSwing = -0.15f
                armSwing = 0.3f
            }
            state == CharacterState.FALL -> {
                legSwing = 0.12f
                armSwing = 0.2f
            }
            state == CharacterState.ATTACK -> {
                legSwing = 0.1f
                armSwing = 0.7f
            }
            else -> {
                legSwing = 0.04f
                armSwing = sin(animTime * 3f) * 0.02f
            }
        }

        val legLeftFootX = centerX - 0.12f * lookDir + (legSwing * lookDir)
        val legRightFootX = centerX + 0.12f * lookDir - (legSwing * lookDir)
        shapeRenderer.rectLine(centerX, pelvisY, legLeftFootX, footY, 0.07f)
        shapeRenderer.rectLine(centerX, pelvisY, legRightFootX, footY, 0.07f)

        // Tangan Belakang
        shapeRenderer.rectLine(centerX, shoulderY, centerX - 0.25f * lookDir - (armSwing * lookDir * 0.5f), shoulderY - 0.35f, 0.06f)

        // Tangan Depan (Pegang Pedang)
        val armFrontHandX: Float
        val armFrontHandY: Float
        val swordTipX: Float
        val swordTipY: Float

        if (attackPhase == AttackPhase.STARTUP) {
            armFrontHandX = centerX - 0.15f * lookDir
            armFrontHandY = shoulderY + 0.2f
            swordTipX = armFrontHandX - 0.8f * lookDir
            swordTipY = armFrontHandY + 0.6f
        } else if (attackPhase == AttackPhase.ACTIVE || isSkillStriking) {
            armFrontHandX = centerX + 0.45f * lookDir
            armFrontHandY = shoulderY - 0.05f
            swordTipX = armFrontHandX + 1.0f * lookDir
            swordTipY = armFrontHandY - 0.15f
        } else {
            armFrontHandX = centerX + 0.25f * lookDir + (armSwing * lookDir)
            armFrontHandY = shoulderY - 0.35f
            swordTipX = armFrontHandX + 0.6f * lookDir
            swordTipY = armFrontHandY - 0.4f
        }

        shapeRenderer.rectLine(centerX, shoulderY, armFrontHandX, armFrontHandY, 0.06f)

        // Bilah Pedang (Sword Blade)
        shapeRenderer.color = when (comboManager.currentStep) {
            ComboStep.HIT_1 -> Color(0.9f, 0.95f, 1f, 1f)
            ComboStep.HIT_2 -> Color(1f, 0.85f, 0.4f, 1f)
            ComboStep.HIT_3 -> Color(1f, 0.45f, 0.2f, 1f)
        }
        shapeRenderer.rectLine(armFrontHandX, armFrontHandY, swordTipX, swordTipY, 0.08f)

        // 3. Efek Visual Tebasan Kombo
        if (attackPhase == AttackPhase.ACTIVE) {
            renderComboSlashArc(shapeRenderer, centerX, shoulderY, lookDir)
        }

        // 4. Efek Visual Ultimate Bladestorm 360 Derajat
        if (isUlting) {
            renderBladestorm(shapeRenderer, centerX, shoulderY)
        }
    }

    private fun renderComboSlashArc(shapeRenderer: ShapeRenderer, centerX: Float, centerY: Float, lookDir: Float) {
        val (arcRadius, arcColor) = when (comboManager.currentStep) {
            ComboStep.HIT_1 -> 1.15f to Color(1f, 0.95f, 0.3f, 0.85f)      // Hit 1: Kuning Emas
            ComboStep.HIT_2 -> 1.35f to Color(1f, 0.6f, 0.15f, 0.9f)       // Hit 2: Oranye Api
            ComboStep.HIT_3 -> 1.70f to Color(1f, 0.2f, 0.25f, 0.95f)      // Hit 3: Merah Raksasa (Finisher)
        }

        shapeRenderer.color = arcColor
        val arcCenterX = centerX + 0.25f * lookDir
        val arcCenterY = centerY - 0.1f

        val segments = 10
        val startAngle = if (lookDir > 0) 70f else 110f
        val sweep = if (lookDir > 0) -130f else 130f

        var prevX = arcCenterX + arcRadius * MathUtils.cosDeg(startAngle)
        var prevY = arcCenterY + arcRadius * MathUtils.sinDeg(startAngle)

        for (i in 1..segments) {
            val angle = startAngle + (sweep * (i.toFloat() / segments))
            val currentRadius = arcRadius + sin(i.toFloat() / segments * MathUtils.PI) * 0.2f
            val curX = arcCenterX + currentRadius * MathUtils.cosDeg(angle)
            val curY = arcCenterY + currentRadius * MathUtils.sinDeg(angle)

            val width = 0.07f + sin(i.toFloat() / segments * MathUtils.PI) * 0.12f
            shapeRenderer.rectLine(prevX, prevY, curX, curY, width)

            prevX = curX
            prevY = curY
        }
    }

    private fun renderBladestorm(shapeRenderer: ShapeRenderer, centerX: Float, centerY: Float) {
        // Lingkaran Radial Badai Pedang
        val expand = (ultTimer / ULT_DURATION) * ultRadius
        shapeRenderer.color = Color(1f, 0.85f, 0.2f, 0.5f)
        shapeRenderer.circle(centerX, centerY, expand, 24)

        // Cincin Luar Cahaya Emas
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(centerX, centerY, expand * 0.95f, 20)
    }

    fun renderDebug(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)

        hurtbox.renderDebug(shapeRenderer)
        hitbox.renderDebug(shapeRenderer)
    }
}

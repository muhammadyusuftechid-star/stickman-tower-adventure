package com.stickman.tower.enemy

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.stickman.tower.character.Player
import com.stickman.tower.combat.Health
import com.stickman.tower.combat.Hitbox
import com.stickman.tower.combat.Hurtbox
import com.stickman.tower.combat.Projectile
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

enum class BossState {
    IDLE,
    CHASE,
    CLEAVE,
    GROUND_SLAM_UP,
    GROUND_SLAM_DOWN,
    ENRAGE_ROAR,
    WIND_SLASH,
    STUNNED,
    DEAD
}

/**
 * Boss Menara Lantai 3: "Tower Guardian"
 * Karakter Boss raksasa berukuran 1.4m x 2.8m dengan 2 Fase Combat:
 * - Fase 1 (HP > 50%): Heavy Cleave + Ground Slam Shockwave.
 * - Fase 2 (HP <= 50% - Enrage Mode): Aura merah menyala, movement speed +50%, dan menembakkan proyektil Wind Slash!
 */
class BossEnemy(spawnX: Float = 14f, spawnY: Float = 2.5f) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    val width = 1.4f
    val height = 2.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    val name: String = "TOWER GUARDIAN"
    val health = Health(maxHp = 350f, invulnerableDuration = 0.15f)
    val hurtbox = Hurtbox(this)
    val hitbox = Hitbox(this, damage = 22f, knockbackX = 8.5f, knockbackY = 4.5f)

    // Hadiah Kemenangan Boss
    val expReward = 200
    val goldReward = 100
    var rewardClaimed = false

    var state = BossState.IDLE
        private set
    var facingRight = false
        private set
    var isPhase2 = false
        private set
    private var hasTriggeredEnrage = false

    private var actionTimer = 0f
    private var attackCooldown = 1.2f
    private var isGrounded = false
    private var animTime = 0f
    private var enragePulse = 0f
    var stunTimer = 0f
        private set
    var poisonTimer = 0f
        private set
    var poisonDps = 0f
        private set
    private var poisonTickTimer = 0f

    fun applyPoison(duration: Float = 3.5f, dps: Float = 12f) {
        poisonTimer = duration.coerceAtLeast(poisonTimer)
        poisonDps = dps.coerceAtLeast(poisonDps)
    }

    val projectiles = mutableListOf<Projectile>()

    companion object {
        const val BASE_SPEED = 3.2f
        const val ENRAGE_SPEED = 4.8f
        const val CLEAVE_RANGE = 2.4f
    }

    fun update(delta: Float, player: Player, platforms: List<Platform>) {
        animTime += delta
        health.update(delta)

        // Ticking Poison DoT
        if (poisonTimer > 0f && !health.isDead) {
            poisonTimer = (poisonTimer - delta).coerceAtLeast(0f)
            poisonTickTimer += delta
            if (poisonTickTimer >= 0.5f) {
                poisonTickTimer = 0f
                health.takeDamage(poisonDps * 0.5f)
            }
        }

        // Update Proyektil Aktif
        val it = projectiles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.update(delta)
            if (p.isDestroyed) it.remove()
        }

        // Cek Transisi Fase 2 (Enrage)
        if (!hasTriggeredEnrage && health.currentHp <= health.maxHp * 0.5f && !health.isDead) {
            hasTriggeredEnrage = true
            isPhase2 = true
            state = BossState.ENRAGE_ROAR
            actionTimer = 0f
            velocity.set(0f, 0f)
            hitbox.reset()
        }

        if (health.isDead) {
            state = BossState.DEAD
            hitbox.reset()
            hurtbox.isActive = false
            velocity.y += GameConfig.GRAVITY * delta
            resolveCollision(delta, platforms)
            return
        }

        if (attackCooldown > 0f) {
            attackCooldown = (attackCooldown - delta).coerceAtLeast(0f)
        }

        hurtbox.set(position.x, position.y, width, height)

        val distToPlayer = player.position.x - (position.x + width / 2f)
        val absDist = abs(distToPlayer)

        // Update AI State Machine
        when (state) {
            BossState.IDLE -> {
                actionTimer += delta
                facingRight = distToPlayer > 0
                velocity.x = 0f
                if (actionTimer >= 0.5f) {
                    state = BossState.CHASE
                    actionTimer = 0f
                }
            }

            BossState.CHASE -> {
                facingRight = distToPlayer > 0
                val moveSpeed = if (isPhase2) ENRAGE_SPEED else BASE_SPEED
                velocity.x = if (facingRight) moveSpeed else -moveSpeed

                // Pilih Pola Serangan Jika Cooldown Selesai
                if (attackCooldown <= 0f) {
                    if (absDist <= CLEAVE_RANGE) {
                        state = BossState.CLEAVE
                        actionTimer = 0f
                        hitbox.damage = if (isPhase2) 28f else 22f
                        hitbox.knockbackX = 8.5f
                        hitbox.knockbackY = 4.0f
                        hitbox.reset()
                        velocity.x = 0f
                    } else if (isPhase2 && absDist > 4.0f && MathUtils.randomBoolean(0.6f)) {
                        // Fase 2: Luncurkan Wind Slash dari Jarak Jauh
                        state = BossState.WIND_SLASH
                        actionTimer = 0f
                        velocity.x = 0f
                    } else if (absDist > 3.0f && MathUtils.randomBoolean(0.5f)) {
                        // Ground Slam Melompat Menghantam Tanah
                        state = BossState.GROUND_SLAM_UP
                        actionTimer = 0f
                        velocity.y = 12.5f
                        velocity.x = if (facingRight) moveSpeed * 1.2f else -moveSpeed * 1.2f
                    }
                }
            }

            BossState.CLEAVE -> {
                actionTimer += delta
                velocity.x = 0f
                when {
                    actionTimer < 0.28f -> {
                        // Startup Windup
                        hitbox.isActive = false
                    }
                    actionTimer < 0.44f -> {
                        // Active Cleave
                        val hbW = 2.4f
                        val hbH = 2.2f
                        val hbX = if (facingRight) position.x + width * 0.7f else position.x - hbW + width * 0.3f
                        val hbY = position.y + 0.3f
                        hitbox.set(hbX, hbY, hbW, hbH, active = true)
                    }
                    actionTimer < 0.70f -> {
                        // Recovery
                        hitbox.reset()
                    }
                    else -> {
                        hitbox.reset()
                        attackCooldown = if (isPhase2) 0.8f else 1.3f
                        state = BossState.IDLE
                        actionTimer = 0f
                    }
                }
            }

            BossState.GROUND_SLAM_UP -> {
                hitbox.isActive = false
                if (velocity.y <= 0f) {
                    // Mulai Menghunjam ke Bawah dengan Kecepatan Tinggi
                    state = BossState.GROUND_SLAM_DOWN
                    velocity.y = -22.0f
                    velocity.x = 0f
                }
            }

            BossState.GROUND_SLAM_DOWN -> {
                if (isGrounded) {
                    // Hantam Tanah! Aktifkan Shockwave Hitbox Luas
                    val shockW = 4.2f
                    val shockH = 1.0f
                    hitbox.damage = if (isPhase2) 32f else 25f
                    hitbox.knockbackX = 9.0f
                    hitbox.knockbackY = 6.0f
                    hitbox.set(position.x + width / 2f - shockW / 2f, position.y, shockW, shockH, active = true)

                    state = BossState.IDLE
                    attackCooldown = if (isPhase2) 1.0f else 1.6f
                    actionTimer = -0.35f // Jeda singkat setelah slam
                }
            }

            BossState.ENRAGE_ROAR -> {
                actionTimer += delta
                enragePulse += delta * 10f
                velocity.set(0f, 0f)
                hitbox.reset()
                if (actionTimer >= 1.2f) {
                    state = BossState.CHASE
                    attackCooldown = 0.4f
                }
            }

            BossState.WIND_SLASH -> {
                actionTimer += delta
                velocity.x = 0f
                if (actionTimer >= 0.25f && projectiles.isEmpty()) {
                    // Tembakkan Proyektil Wind Slash ke Arah Pemain
                    val projVx = if (facingRight) 14f else -14f
                    val projStartX = if (facingRight) position.x + width else position.x - 0.8f
                    projectiles.add(
                        Projectile(
                            startX = projStartX,
                            startY = position.y + height * 0.45f,
                            velocityX = projVx,
                            damage = 22f
                        )
                    )
                }
                if (actionTimer >= 0.55f) {
                    attackCooldown = 1.1f
                    state = BossState.IDLE
                    actionTimer = 0f
                }
            }

            BossState.STUNNED -> {
                actionTimer += delta
                velocity.x = 0f
                hitbox.reset()
                if (actionTimer >= stunTimer) {
                    state = BossState.CHASE
                    attackCooldown = 0.6f
                    actionTimer = 0f
                }
            }

            BossState.DEAD -> {}
        }

        // Gravitasi & Collision
        if (state != BossState.GROUND_SLAM_DOWN) {
            velocity.y += GameConfig.GRAVITY * delta
            velocity.y = velocity.y.coerceAtLeast(-25f)
        }

        resolveCollision(delta, platforms)
    }

    private fun resolveCollision(delta: Float, platforms: List<Platform>) {
        position.x += velocity.x * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (platform.isOneWay) continue
            if (bounds.overlaps(platform.bounds)) {
                if (velocity.x > 0f) position.x = platform.left - width
                else if (velocity.x < 0f) position.x = platform.right
                velocity.x = 0f
                bounds.setPosition(position.x, position.y)
            }
        }

        isGrounded = false
        position.y += velocity.y * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (platform.isOneWay && velocity.y > 0f) continue
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

    fun takeHit(amount: Float, knockX: Float, knockY: Float): Boolean {
        if (health.takeDamage(amount)) {
            // Boss memiliki hyper armor: resistensi knockback tinggi (hanya bergeser sedikit)
            velocity.x += knockX * 0.25f
            return true
        }
        return false
    }

    /**
     * Mengaktifkan status Guard Break / Stun akibat Perfect Parry dari pemain.
     */
    fun stun(duration: Float = 1.5f) {
        state = BossState.STUNNED
        stunTimer = duration
        actionTimer = 0f
        velocity.x = 0f
        hitbox.reset()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val centerX = position.x + width / 2f
        val footY = position.y
        val headRadius = 0.35f
        val headCenterY = position.y + height - headRadius - 0.1f
        val neckY = headCenterY - headRadius
        val pelvisY = position.y + 1.15f
        val shoulderY = neckY - 0.22f
        val lookDir = if (facingRight) 1f else -1f

        // 1. Render Proyektil Wind Slash
        for (p in projectiles) p.render(shapeRenderer)

        // 2. Aura Merah Membara di Fase 2 (Enrage Mode)
        if (isPhase2 && !health.isDead) {
            val pulse = (sin(animTime * 8f) * 0.15f) + 0.35f
            shapeRenderer.color = Color(1f, 0.2f, 0.1f, pulse)
            shapeRenderer.circle(centerX, position.y + height / 2f, (height / 2f) + 0.3f, 20)
        }

        // 3. Warna Tubuh Boss (Hitam legam dengan armor merah marun)
        val bodyColor = when {
            health.isFlashing -> Color.WHITE
            state == BossState.DEAD -> Color(0.25f, 0.25f, 0.25f, 1f)
            isPhase2 -> Color(0.85f, 0.18f, 0.18f, 1f)
            else -> Color(0.18f, 0.18f, 0.22f, 1f)
        }

        // Kepala Boss & Helm Tanduk
        shapeRenderer.color = bodyColor
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // Mata Merah Menyala
        shapeRenderer.color = if (health.isDead) Color.GRAY else Color(1f, 0.15f, 0.15f, 1f)
        shapeRenderer.circle(centerX + lookDir * 0.16f, headCenterY + 0.05f, 0.08f, 10)

        // Tanduk Baja di Kepala
        shapeRenderer.color = Color(0.9f, 0.75f, 0.2f, 1f)
        shapeRenderer.rectLine(centerX, headCenterY + headRadius * 0.6f, centerX + lookDir * 0.45f, headCenterY + headRadius + 0.35f, 0.09f)

        // Visual Guard Break / Stun Stars di atas kepala Bos
        if (state == BossState.STUNNED) {
            shapeRenderer.color = Color.GOLD
            val orbitRadius = 0.52f
            for (i in 0 until 4) {
                val angle = animTime * 5.5f + (i * 1.57f)
                val starX = centerX + kotlin.math.cos(angle.toDouble()).toFloat() * orbitRadius
                val starY = headCenterY + headRadius + 0.42f + kotlin.math.sin(angle.toDouble()).toFloat() * 0.12f
                shapeRenderer.circle(starX, starY, 0.08f, 8)
            }
        }

        // Tubuh / Torso (Lebih tebal & berotot)
        shapeRenderer.color = bodyColor
        shapeRenderer.rectLine(centerX, neckY, centerX, pelvisY, 0.16f)

        // Kaki Boss
        val legAnim = sin(animTime * 6f) * 0.3f
        val footLeftX = if (state == BossState.CHASE) centerX - 0.35f - legAnim else centerX - 0.35f
        val footRightX = if (state == BossState.CHASE) centerX + 0.35f + legAnim else centerX + 0.35f
        shapeRenderer.rectLine(centerX, pelvisY, footLeftX, footY, 0.14f)
        shapeRenderer.rectLine(centerX, pelvisY, footRightX, footY, 0.14f)

        // Tangan & Pedang Raksasa (Greatsword)
        val handX = centerX + lookDir * 0.55f
        val handY = shoulderY - 0.25f
        shapeRenderer.rectLine(centerX, shoulderY, handX, handY, 0.12f)

        // Pedang Raksasa
        val bladeLen = 2.0f
        val bladeAngle = when (state) {
            BossState.CLEAVE -> if (facingRight) 25f else 155f
            BossState.GROUND_SLAM_DOWN -> -90f
            else -> if (facingRight) 65f else 115f
        }
        val swordEndX = handX + MathUtils.cosDeg(bladeAngle) * bladeLen
        val swordEndY = handY + MathUtils.sinDeg(bladeAngle) * bladeLen

        shapeRenderer.color = if (isPhase2) Color(1f, 0.35f, 0.2f, 1f) else Color(0.85f, 0.85f, 0.9f, 1f)
        shapeRenderer.rectLine(handX, handY, swordEndX, swordEndY, 0.15f)
    }

    fun renderDebug(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = Color.MAGENTA
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
        hurtbox.renderDebug(shapeRenderer)
        hitbox.renderDebug(shapeRenderer)
    }
}

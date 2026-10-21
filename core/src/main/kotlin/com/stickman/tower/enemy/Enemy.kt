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
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

/**
 * Entitas Musuh Stickman dengan Artificial Intelligence (AI) Finite State Machine:
 * Patroli mandiri (anti-jurang), deteksi & pengejaran pemain, serangan berancang-ancang (telegraphed),
 * dan reaksi damage / eliminasi.
 */
class Enemy(spawnX: Float, spawnY: Float) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    val width = 0.8f
    val height = 1.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    // Komponen Combat
    val health = Health(maxHp = 50f, invulnerableDuration = 0.25f)
    val hurtbox = Hurtbox(this)
    val hitbox = Hitbox(this, damage = 12f, knockbackX = 5.5f, knockbackY = 3.0f)

    // RPG Progression Reward
    val expReward: Int = 25
    val goldReward: Int = 10
    var rewardClaimed: Boolean = false

    // AI & Parameter Status
    var state = EnemyState.PATROL
        private set
    var facingRight = true
        private set

    private var patrolDir = 1f
    private var attackCooldown = 0f
    private var attackTimer = 0f
    private var deadTimer = 0f
    private var animTime = 0f

    var isGrounded = false
        private set
    var isDespawned = false
        private set

    companion object {
        const val DETECTION_RADIUS = 6.5f
        const val ATTACK_RADIUS = 1.3f
        const val PATROL_SPEED = 2.2f
        const val CHASE_SPEED = 4.2f

        const val ATTACK_WINDUP = 0.25f  // Waktu ancang-ancang (telegraphed warning)
        const val ATTACK_ACTIVE = 0.12f  // Waktu hitbox aktif
        const val ATTACK_RECOVERY = 0.20f
        const val TOTAL_ATTACK_TIME = ATTACK_WINDUP + ATTACK_ACTIVE + ATTACK_RECOVERY
    }

    fun update(delta: Float, player: Player, platforms: List<Platform>) {
        if (isDespawned) return

        animTime += delta
        health.update(delta)

        if (attackCooldown > 0f) {
            attackCooldown = (attackCooldown - delta).coerceAtLeast(0f)
        }

        // Jika Musuh Mati
        if (health.isDead) {
            state = EnemyState.DEAD
            hitbox.reset()
            hurtbox.isActive = false
            deadTimer += delta
            if (deadTimer >= 1.0f) {
                isDespawned = true
            }
            // Gravitasi saat roboh
            velocity.y += GameConfig.GRAVITY * delta
            resolveCollision(delta, platforms)
            return
        }

        // Perbarui posisi Hurtbox
        hurtbox.set(position.x, position.y, width, height)

        // 1. Eksekusi AI State Machine
        when (state) {
            EnemyState.PATROL -> updatePatrolState(delta, player, platforms)
            EnemyState.CHASE -> updateChaseState(delta, player)
            EnemyState.ATTACK -> updateAttackState(delta)
            EnemyState.HURT -> updateHurtState(delta)
            EnemyState.DEAD -> {}
        }

        // 2. Gravitasi & Fisika
        velocity.y += GameConfig.GRAVITY * delta
        velocity.y = velocity.y.coerceAtLeast(-25f)

        // 3. Resolusi Collision
        resolveCollision(delta, platforms)
    }

    private fun updatePatrolState(delta: Float, player: Player, platforms: List<Platform>) {
        // Cek apakah pemain masuk dalam jangkauan deteksi
        val dx = player.position.x - position.x
        val dy = abs(player.position.y - position.y)

        if (abs(dx) < DETECTION_RADIUS && dy < 2.5f && !player.health.isDead) {
            state = EnemyState.CHASE
            return
        }

        // Deteksi Tepi Platform / Jurang (Cliff Avoidance)
        if (isGrounded) {
            val checkX = if (patrolDir > 0) position.x + width + 0.2f else position.x - 0.2f
            val checkY = position.y - 0.3f
            val groundAhead = platforms.any { checkX >= it.left && checkX <= it.right && checkY >= it.bottom && checkY <= it.top + 0.1f }

            if (!groundAhead) {
                patrolDir = -patrolDir
            }
        }

        facingRight = patrolDir > 0
        velocity.x = patrolDir * PATROL_SPEED
    }

    private fun updateChaseState(delta: Float, player: Player) {
        val dx = player.position.x - position.x
        val dy = abs(player.position.y - position.y)

        // Jika pemain kabur terlalu jauh atau pemain sudah mati
        if (abs(dx) > DETECTION_RADIUS * 1.3f || dy > 3.0f || player.health.isDead) {
            state = EnemyState.PATROL
            velocity.x = 0f
            return
        }

        facingRight = dx > 0

        // Jika sudah dalam jangkauan serang
        if (abs(dx) <= ATTACK_RADIUS) {
            if (attackCooldown <= 0f) {
                state = EnemyState.ATTACK
                attackTimer = 0f
                hitbox.reset()
                velocity.x = 0f
            } else {
                // Berhenti bersiap menyerang saat cooldown
                velocity.x = MathUtils.lerp(velocity.x, 0f, 15f * delta)
            }
        } else {
            // Lari mendekati pemain
            val targetVx = if (dx > 0) CHASE_SPEED else -CHASE_SPEED
            velocity.x = MathUtils.lerp(velocity.x, targetVx, 12f * delta)
        }
    }

    private fun updateAttackState(delta: Float) {
        attackTimer += delta
        velocity.x = 0f // Berhenti saat ancang-ancang & menebas

        when {
            attackTimer < ATTACK_WINDUP -> {
                // Ancang-ancang telegraphed (mata menyala)
                hitbox.isActive = false
            }
            attackTimer < ATTACK_WINDUP + ATTACK_ACTIVE -> {
                // Hitbox aktif melukai pemain
                val hbW = 1.25f
                val hbH = 1.25f
                val hbX = if (facingRight) position.x + width * 0.6f else position.x - hbW + width * 0.4f
                val hbY = position.y + 0.35f
                hitbox.set(hbX, hbY, hbW, hbH, active = true)
            }
            attackTimer < TOTAL_ATTACK_TIME -> {
                hitbox.isActive = false
            }
            else -> {
                hitbox.reset()
                attackCooldown = 1.2f
                state = EnemyState.CHASE
            }
        }
    }

    private fun updateHurtState(delta: Float) {
        velocity.x = MathUtils.lerp(velocity.x, 0f, 10f * delta)
        if (!health.isFlashing) {
            state = EnemyState.CHASE
        }
    }

    private fun resolveCollision(delta: Float, platforms: List<Platform>) {
        // Sumbu X
        position.x += velocity.x * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
            if (bounds.overlaps(platform.bounds)) {
                if (velocity.x > 0f) {
                    position.x = platform.left - width
                    patrolDir = -1f
                } else if (velocity.x < 0f) {
                    position.x = platform.right
                    patrolDir = 1f
                }
                velocity.x = 0f
                bounds.setPosition(position.x, position.y)
            }
        }

        // Sumbu Y
        isGrounded = false
        position.y += velocity.y * delta
        bounds.setPosition(position.x, position.y)

        for (platform in platforms) {
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

    /**
     * Menerima damage dari serangan pemain.
     */
    fun takeHit(damage: Float, knockX: Float, knockY: Float): Boolean {
        if (health.takeDamage(damage)) {
            velocity.x = knockX
            velocity.y = knockY

            if (health.isDead) {
                state = EnemyState.DEAD
                hitbox.reset()
                hurtbox.isActive = false
            } else {
                state = EnemyState.HURT
                hitbox.isActive = false
            }
            return true
        }
        return false
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isDespawned) return

        val centerX = position.x + width / 2f
        val footY = position.y
        val headRadius = 0.22f
        val headCenterY = position.y + height - headRadius - 0.05f
        val neckY = headCenterY - headRadius
        val pelvisY = position.y + 0.75f
        val shoulderY = neckY - 0.15f
        val lookDir = if (facingRight) 1f else -1f

        // Warna Tubuh: Merah Crimson (musuh gelap), Berkedip Putih saat hurt
        val bodyColor = if (health.isFlashing) Color.WHITE else Color(0.85f, 0.2f, 0.25f, 1f)
        shapeRenderer.color = bodyColor

        if (state == EnemyState.DEAD) {
            // Animasi roboh di lantai
            val fallProgress = (deadTimer / 1.0f).coerceIn(0f, 1f)
            val corpseAlpha = 1f - fallProgress
            shapeRenderer.color = Color(bodyColor.r, bodyColor.g, bodyColor.b, corpseAlpha)
            shapeRenderer.rectLine(centerX - 0.5f, footY + 0.1f, centerX + 0.5f, footY + 0.1f, 0.1f)
            shapeRenderer.circle(centerX + 0.5f * lookDir, footY + 0.15f, 0.18f, 12)
            return
        }

        // 1. Kepala
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // 2. Mata Merah Menyala / Kuning saat Ancang-ancang Telegraphed
        val isTelegraphing = state == EnemyState.ATTACK && attackTimer < ATTACK_WINDUP
        shapeRenderer.color = if (isTelegraphing) Color.YELLOW else Color(0.4f, 0f, 0f, 1f)
        val eyeX = centerX + (headRadius * 0.45f * lookDir)
        val eyeY = headCenterY + 0.04f
        shapeRenderer.circle(eyeX, eyeY, if (isTelegraphing) 0.07f else 0.045f, 8)

        // Jika sedang ancang-ancang, gambar tanda seru bahaya di atas kepala
        if (isTelegraphing) {
            shapeRenderer.color = Color.YELLOW
            shapeRenderer.rectLine(centerX, headCenterY + 0.4f, centerX, headCenterY + 0.6f, 0.04f)
            shapeRenderer.circle(centerX, headCenterY + 0.32f, 0.03f, 6)
        }

        // 3. Tulang Belakang (Spine)
        shapeRenderer.color = bodyColor
        shapeRenderer.rectLine(centerX, neckY, centerX, pelvisY, 0.08f)

        // 4. Kaki Berjalan / Berlari
        var legSwing = 0f
        if (state == EnemyState.PATROL || state == EnemyState.CHASE) {
            val freq = if (state == EnemyState.CHASE) 15f else 8f
            legSwing = sin(animTime * freq) * 0.32f
        }

        val legLeftFootX = centerX - 0.12f * lookDir + (legSwing * lookDir)
        val legRightFootX = centerX + 0.12f * lookDir - (legSwing * lookDir)
        shapeRenderer.rectLine(centerX, pelvisY, legLeftFootX, footY, 0.07f)
        shapeRenderer.rectLine(centerX, pelvisY, legRightFootX, footY, 0.07f)

        // 5. Tangan dan Senjata Gada / Belati Musuh
        val armHandX: Float
        val armHandY: Float
        val weaponTipX: Float
        val weaponTipY: Float

        if (state == EnemyState.ATTACK && attackTimer >= ATTACK_WINDUP && attackTimer < ATTACK_WINDUP + ATTACK_ACTIVE) {
            // Ayunan tebasan kuat ke depan
            armHandX = centerX + 0.4f * lookDir
            armHandY = shoulderY - 0.05f
            weaponTipX = armHandX + 0.8f * lookDir
            weaponTipY = armHandY - 0.25f

            // Efek tebasan merah musuh
            shapeRenderer.color = Color(1f, 0.15f, 0.15f, 0.8f)
            shapeRenderer.rectLine(armHandX, armHandY + 0.2f, weaponTipX, weaponTipY - 0.1f, 0.08f)
        } else {
            armHandX = centerX + 0.22f * lookDir
            armHandY = shoulderY - 0.35f
            weaponTipX = armHandX + 0.5f * lookDir
            weaponTipY = armHandY - 0.3f
        }

        shapeRenderer.color = bodyColor
        shapeRenderer.rectLine(centerX, shoulderY, armHandX, armHandY, 0.06f)

        // Bilah Belati Musuh (Dark Steel)
        shapeRenderer.color = Color(0.35f, 0.35f, 0.4f, 1f)
        shapeRenderer.rectLine(armHandX, armHandY, weaponTipX, weaponTipY, 0.07f)

        // 6. Mini HP Bar Musuh
        renderHealthBar(shapeRenderer, centerX, position.y + height + 0.2f)
    }

    private fun renderHealthBar(shapeRenderer: ShapeRenderer, centerX: Float, barY: Float) {
        val barW = 0.9f
        val barH = 0.1f
        val barX = centerX - barW / 2f

        shapeRenderer.color = Color(0.1f, 0.1f, 0.15f, 0.8f)
        shapeRenderer.rect(barX, barY, barW, barH)

        val hpPercent = (health.currentHp / health.maxHp).coerceIn(0f, 1f)
        shapeRenderer.color = Color.RED
        shapeRenderer.rect(barX, barY, barW * hpPercent, barH)
    }

    fun renderDebug(shapeRenderer: ShapeRenderer) {
        if (isDespawned) return

        // Collision Box (Hijau)
        shapeRenderer.color = Color.GREEN
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)

        // Hurtbox (Biru/Cyan)
        hurtbox.renderDebug(shapeRenderer)

        // Hitbox (Merah)
        hitbox.renderDebug(shapeRenderer)
    }
}

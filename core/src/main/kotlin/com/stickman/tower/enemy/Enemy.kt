package com.stickman.tower.enemy

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.character.Player
import com.stickman.tower.combat.Health
import com.stickman.tower.combat.Hitbox
import com.stickman.tower.combat.Hurtbox
import com.stickman.tower.combat.Projectile
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.GameConfig
import kotlin.math.abs
import kotlin.math.sin

/**
 * Entitas Musuh Stickman dengan Artificial Intelligence (AI) Finite State Machine:
 * Mendukung varian MELEE (petarung belati crimson jarak dekat) dan RANGED (pemanah sihir amethyst jarak jauh).
 * Dilengkapi patroli mandiri (anti-jurang), kiting menjaga jarak, proyektil, dan reaksi eliminasi.
 */
class Enemy(
    spawnX: Float,
    spawnY: Float,
    val type: EnemyType = EnemyType.MELEE
) {
    val position = Vector2(spawnX, spawnY)
    val velocity = Vector2(0f, 0f)

    val width = 0.8f
    val height = 1.8f
    val bounds = Rectangle(position.x, position.y, width, height)

    // Komponen Combat
    val health = Health(maxHp = if (type == EnemyType.RANGED) 40f else 50f, invulnerableDuration = 0.25f)
    val hurtbox = Hurtbox(this)
    val hitbox = Hitbox(this, damage = if (type == EnemyType.RANGED) 8f else 12f, knockbackX = 5.5f, knockbackY = 3.0f)
    val projectiles = mutableListOf<Projectile>()

    // RPG Progression Reward
    val expReward: Int = if (type == EnemyType.RANGED) 35 else 25
    val goldReward: Int = if (type == EnemyType.RANGED) 15 else 10
    var rewardClaimed: Boolean = false

    // AI & Parameter Status
    var state = EnemyState.PATROL
        private set
    var facingRight = true
        private set

    private var patrolDir = 1f
    private var attackCooldown = 0f
    private var attackTimer = 0f
    private var hasFiredProjectile = false
    private var deadTimer = 0f
    private var animTime = 0f
    var stunTimer = 0f
        private set

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

        // Perbarui Proyektil Ranged
        if (projectiles.isNotEmpty()) {
            val it = projectiles.iterator()
            while (it.hasNext()) {
                val proj = it.next()
                proj.update(delta)
                for (platform in platforms) {
                    if (!proj.isDestroyed && proj.bounds.overlaps(platform.bounds)) {
                        proj.isDestroyed = true
                        break
                    }
                }
                if (proj.isDestroyed) {
                    it.remove()
                }
            }
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
            EnemyState.CHASE -> updateChaseState(delta, player, platforms)
            EnemyState.ATTACK -> updateAttackState(delta)
            EnemyState.HURT -> updateHurtState(delta)
            EnemyState.STUNNED -> updateStunnedState(delta)
            EnemyState.DEAD -> {}
        }

        // 2. Gravitasi & Fisika
        velocity.y += GameConfig.GRAVITY * delta
        velocity.y = velocity.y.coerceAtLeast(-25f)

        // 3. Resolusi Collision
        resolveCollision(delta, platforms)
    }

    private fun updatePatrolState(delta: Float, player: Player, platforms: List<Platform>) {
        val detectRadius = if (type == EnemyType.RANGED) 8.5f else DETECTION_RADIUS
        val dx = player.position.x - position.x
        val dy = abs(player.position.y - position.y)

        if (abs(dx) < detectRadius && dy < 3.2f && !player.health.isDead) {
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

    private fun updateChaseState(delta: Float, player: Player, platforms: List<Platform>) {
        val dx = player.position.x - position.x
        val dy = abs(player.position.y - position.y)
        val dist = abs(dx)
        val maxSight = if (type == EnemyType.RANGED) 12.0f else (DETECTION_RADIUS * 1.3f)

        // Jika pemain kabur terlalu jauh atau pemain sudah mati
        if (dist > maxSight || dy > 4.2f || player.health.isDead) {
            state = EnemyState.PATROL
            velocity.x = 0f
            return
        }

        facingRight = dx > 0

        if (type == EnemyType.RANGED) {
            // Kiting AI: Menjaga jarak optimal 3.5m .. 7.0m dari pemain
            if (dist < 3.2f) {
                // Terlalu dekat! Mundur menjauh dari pemain jika ada platform di belakang
                val retreatDir = if (dx > 0) -1f else 1f
                var canRetreat = true
                if (isGrounded) {
                    val checkX = if (retreatDir > 0) position.x + width + 0.25f else position.x - 0.25f
                    val checkY = position.y - 0.3f
                    canRetreat = platforms.any { checkX >= it.left && checkX <= it.right && checkY >= it.bottom && checkY <= it.top + 0.1f }
                }
                velocity.x = if (canRetreat) retreatDir * (PATROL_SPEED * 1.3f) else 0f
            } else if (dist > 7.2f) {
                // Terlalu jauh! Maju mendekat ke pemain
                val approachDir = if (dx > 0) 1f else -1f
                velocity.x = approachDir * (CHASE_SPEED * 0.85f)
            } else {
                // Berada di sweet spot (3.2m .. 7.2m): Berhenti dan bersiap menembak
                velocity.x = MathUtils.lerp(velocity.x, 0f, 15f * delta)
                if (attackCooldown <= 0f && dy < 2.5f) {
                    state = EnemyState.ATTACK
                    attackTimer = 0f
                    hasFiredProjectile = false
                    velocity.x = 0f
                }
            }
        } else {
            // Melee AI
            if (dist <= ATTACK_RADIUS) {
                if (attackCooldown <= 0f) {
                    state = EnemyState.ATTACK
                    attackTimer = 0f
                    hitbox.reset()
                    velocity.x = 0f
                } else {
                    velocity.x = MathUtils.lerp(velocity.x, 0f, 15f * delta)
                }
            } else {
                val targetVx = if (dx > 0) CHASE_SPEED else -CHASE_SPEED
                velocity.x = MathUtils.lerp(velocity.x, targetVx, 12f * delta)
            }
        }
    }

    private fun updateAttackState(delta: Float) {
        attackTimer += delta
        velocity.x = 0f // Berhenti saat ancang-ancang

        if (type == EnemyType.RANGED) {
            val rangedWindup = 0.40f
            val rangedTotalTime = 0.70f

            if (attackTimer >= rangedWindup && !hasFiredProjectile) {
                hasFiredProjectile = true
                val projVx = if (facingRight) 9.5f else -9.5f
                val projX = if (facingRight) position.x + width + 0.15f else position.x - 0.95f
                val projY = position.y + height * 0.62f
                projectiles.add(
                    Projectile(
                        startX = projX,
                        startY = projY,
                        velocityX = projVx,
                        velocityY = 0f,
                        damage = 14f,
                        width = 0.80f,
                        height = 0.35f,
                        maxLifetime = 2.5f,
                        color = Color(0.75f, 0.25f, 1f, 1f)
                    )
                )
                AudioManager.playSlashLight()
            }

            if (attackTimer >= rangedTotalTime) {
                attackCooldown = 1.8f
                state = EnemyState.CHASE
            }
        } else {
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
    }

    private fun updateHurtState(delta: Float) {
        velocity.x = MathUtils.lerp(velocity.x, 0f, 10f * delta)
        if (!health.isFlashing) {
            state = EnemyState.CHASE
        }
    }

    private fun updateStunnedState(delta: Float) {
        velocity.x = MathUtils.lerp(velocity.x, 0f, 12f * delta)
        hitbox.isActive = false
        stunTimer = (stunTimer - delta).coerceAtLeast(0f)
        if (stunTimer <= 0f) {
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

    /**
     * Mengaktifkan status Guard Break / Stun akibat Perfect Parry dari pemain.
     */
    fun stun(duration: Float = 1.5f) {
        state = EnemyState.STUNNED
        stunTimer = duration
        velocity.x = 0f
        hitbox.isActive = false
        attackCooldown = 0.8f
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (isDespawned) return

        // Render Seluruh Proyektil Ranged Aktif
        if (projectiles.isNotEmpty()) {
            for (proj in projectiles) {
                proj.render(shapeRenderer)
            }
        }

        val centerX = position.x + width / 2f
        val footY = position.y
        val headRadius = 0.22f
        val headCenterY = position.y + height - headRadius - 0.05f
        val neckY = headCenterY - headRadius
        val pelvisY = position.y + 0.75f
        val shoulderY = neckY - 0.15f
        val lookDir = if (facingRight) 1f else -1f

        // Warna Tubuh: Merah Crimson (Melee) / Amethyst Ungu (Ranged), Berkedip Putih saat hurt
        val baseColor = if (type == EnemyType.RANGED) Color(0.68f, 0.28f, 0.95f, 1f) else Color(0.85f, 0.2f, 0.25f, 1f)
        val bodyColor = if (health.isFlashing) Color.WHITE else baseColor
        shapeRenderer.color = bodyColor

        if (state == EnemyState.DEAD) {
            // Animasi roboh di lantai
            val fallProgress = (deadTimer / 1.0f).coerceIn(0f, 1f)
            val corpseAlpha = 1f - fallProgress
            shapeRenderer.setColor(bodyColor.r, bodyColor.g, bodyColor.b, corpseAlpha)
            shapeRenderer.rectLine(centerX - 0.5f, footY + 0.1f, centerX + 0.5f, footY + 0.1f, 0.1f)
            shapeRenderer.circle(centerX + 0.5f * lookDir, footY + 0.15f, 0.18f, 12)
            return
        }

        // 1. Kepala
        shapeRenderer.circle(centerX, headCenterY, headRadius, 16)

        // 2. Mata Merah Menyala / Ungu Sihir / Kuning saat Ancang-ancang Telegraphed
        val isTelegraphing = state == EnemyState.ATTACK && attackTimer < (if (type == EnemyType.RANGED) 0.40f else ATTACK_WINDUP)
        val eyeColor = when {
            state == EnemyState.STUNNED -> Color.GOLD
            isTelegraphing -> Color.YELLOW
            type == EnemyType.RANGED -> Color(0.92f, 0.45f, 1f, 1f)
            else -> Color(0.4f, 0f, 0f, 1f)
        }
        shapeRenderer.color = eyeColor
        val eyeX = centerX + (headRadius * 0.45f * lookDir)
        val eyeY = headCenterY + 0.04f
        shapeRenderer.circle(eyeX, eyeY, if (isTelegraphing) 0.07f else 0.045f, 8)

        // Visual Guard Break / Stun Stars di atas kepala
        if (state == EnemyState.STUNNED) {
            shapeRenderer.color = Color.GOLD
            val orbitRadius = 0.26f
            for (i in 0 until 3) {
                val angle = animTime * 6.5f + (i * 2.094f) // 120 derajat
                val starX = centerX + kotlin.math.cos(angle.toDouble()).toFloat() * orbitRadius
                val starY = headCenterY + headRadius + 0.22f + kotlin.math.sin(angle.toDouble()).toFloat() * 0.08f
                shapeRenderer.circle(starX, starY, 0.04f, 6)
            }
        }

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

        // 5. Tangan dan Senjata (Busur Panah untuk Ranged, Belati untuk Melee)
        if (type == EnemyType.RANGED) {
            val bowX = centerX + 0.35f * lookDir
            val bowTopY = shoulderY + 0.35f
            val bowMidY = shoulderY
            val bowBotY = shoulderY - 0.35f

            shapeRenderer.color = Color(0.55f, 0.40f, 0.25f, 1f) // Kayu busur
            shapeRenderer.rectLine(bowX, bowMidY, bowX + 0.15f * lookDir, bowTopY, 0.05f)
            shapeRenderer.rectLine(bowX, bowMidY, bowX + 0.15f * lookDir, bowBotY, 0.05f)
            // Tali busur
            shapeRenderer.color = Color(0.85f, 0.85f, 0.85f, 0.8f)
            shapeRenderer.rectLine(bowX + 0.15f * lookDir, bowTopY, bowX + 0.15f * lookDir, bowBotY, 0.03f)

            // Tangan memegang busur
            shapeRenderer.color = bodyColor
            shapeRenderer.rectLine(centerX, shoulderY, bowX, bowMidY, 0.06f)

            // Anak panah dan bola energi saat membidik
            if (isTelegraphing) {
                val arrowTipX = bowX + 0.45f * lookDir
                val arrowTailX = bowX - 0.20f * lookDir
                shapeRenderer.color = Color(0.85f, 0.35f, 1f, 1f)
                shapeRenderer.rectLine(arrowTailX, bowMidY, arrowTipX, bowMidY, 0.06f)
                val pulse = (sin(animTime * 15f) + 1f) * 0.03f
                shapeRenderer.circle(arrowTipX, bowMidY, 0.10f + pulse, 10)
            }
        } else {
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
        }

        // 6. Mini HP Bar Musuh
        renderHealthBar(shapeRenderer, centerX, position.y + height + 0.2f)
    }

    private fun renderHealthBar(shapeRenderer: ShapeRenderer, centerX: Float, barY: Float) {
        // Hanya tampilkan jika musuh telah terluka atau sedang dalam pertempuran aktif
        if (health.isDead || (health.currentHp >= health.maxHp && state == EnemyState.PATROL)) return

        val barW = 0.85f
        val barH = 0.08f
        val barX = centerX - barW / 2f

        // Frame Obsidian Gelap
        shapeRenderer.color = Color(0.06f, 0.08f, 0.12f, 0.85f)
        shapeRenderer.rect(barX - 0.02f, barY - 0.02f, barW + 0.04f, barH + 0.04f)

        // Fill Bar Darah Crimson
        val hpPercent = (health.currentHp / health.maxHp).coerceIn(0f, 1f)
        shapeRenderer.color = Color(0.85f, 0.18f, 0.22f, 0.95f)
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

package com.stickman.tower.enemy

import com.stickman.tower.character.Player
import com.stickman.tower.physics.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnemyTest {

    @Test
    fun testEnemyTypeStats() {
        val melee = Enemy(spawnX = 0f, spawnY = 0f, type = EnemyType.MELEE)
        val ranged = Enemy(spawnX = 0f, spawnY = 0f, type = EnemyType.RANGED)

        // Verifikasi HP
        assertEquals(50f, melee.health.maxHp)
        assertEquals(40f, ranged.health.maxHp)

        // Verifikasi Hitbox Melee vs Ranged
        assertEquals(12f, melee.hitbox.damage)
        assertEquals(8f, ranged.hitbox.damage)

        // Verifikasi Reward RPG
        assertEquals(25, melee.expReward)
        assertEquals(10, melee.goldReward)

        assertEquals(35, ranged.expReward)
        assertEquals(15, ranged.goldReward)
    }

    @Test
    fun testRangedEnemyKitingBehavior() {
        val ground = listOf(Platform(x = -10f, y = -1f, width = 40f, height = 1f))
        val ranged = Enemy(spawnX = 10f, spawnY = 0f, type = EnemyType.RANGED)
        val player = Player(spawnX = 12f, spawnY = 0f) // Jarak 2m (terlalu dekat: < 3.2m)

        // Frame 1: Dari PATROL mendeteksi player -> beralih ke CHASE
        ranged.update(delta = 0.05f, player = player, platforms = ground)
        assertEquals(EnemyState.CHASE, ranged.state)

        // Frame 2: Dalam CHASE, karena terlalu dekat (< 3.2m), musuh panah harus mundur (kiting retreat ke kiri)
        ranged.update(delta = 0.05f, player = player, platforms = ground)
        assertTrue(ranged.velocity.x < 0f, "Ranged enemy should retreat when player is too close (< 3.2m)")
    }

    @Test
    fun testRangedEnemyAttackAndProjectile() {
        val ground = listOf(Platform(x = -10f, y = -1f, width = 40f, height = 1f))
        val ranged = Enemy(spawnX = 10f, spawnY = 0f, type = EnemyType.RANGED)
        val player = Player(spawnX = 14.5f, spawnY = 0f) // Jarak 4.5m (sweet spot: 3.2m - 7.2m)

        // Frame 1: Dari PATROL mendeteksi player -> beralih ke CHASE
        ranged.update(delta = 0.05f, player = player, platforms = ground)
        assertEquals(EnemyState.CHASE, ranged.state)

        // Frame 2: Dalam CHASE pada sweet spot -> beralih ke ATTACK
        ranged.update(delta = 0.05f, player = player, platforms = ground)
        assertEquals(EnemyState.ATTACK, ranged.state)

        // Update frame hingga melewati windup (0.40s) untuk menembakkan proyektil panah sihir
        for (i in 0 until 9) {
            ranged.update(delta = 0.05f, player = player, platforms = ground)
        }

        assertTrue(ranged.projectiles.isNotEmpty(), "Ranged enemy should fire a projectile during sweet-spot attack")

        val proj = ranged.projectiles.first()
        assertEquals(14f, proj.damage)
        assertTrue(proj.velocityX > 0f, "Projectile should fly right towards the player")
        assertFalse(proj.isDestroyed)
    }
}

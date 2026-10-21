package com.stickman.tower.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.enemy.EnemyType
import com.stickman.tower.physics.Platform

/**
 * Pembangun Level Menara Bertingkat (Tower Level Builder).
 * Menyediakan layout preset untuk Lantai 1 s/d Lantai 3 (Apex Boss Arena).
 */
object TowerLevelBuilder {

    fun buildFloor(floorNumber: Int): TowerFloor {
        return when (floorNumber) {
            1 -> buildFloor1()
            2 -> buildFloor2()
            3 -> buildFloor3()
            else -> buildFloor3()
        }
    }

    private fun buildFloor1(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Lantai Dasar & Tembok Pembatas
        platforms.add(Platform(x = -10f, y = 0f, width = 42f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 13f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 31f, y = 1f, width = 1f, height = 13f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        // Platform Bertingkat
        platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.5f, y = 4.3f, width = 3.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 13.5f, y = 6.2f, width = 5.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 21f, y = 3.5f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        // One-Way Platform
        platforms.add(Platform(x = 13.5f, y = 3.0f, width = 4.0f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 10.5f, y = 1.0f, width = 2.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 15.5f, y = 6.7f)
        )

        val enemies = listOf(
            Enemy(spawnX = 5.0f, spawnY = 1.2f, type = EnemyType.MELEE),
            Enemy(spawnX = 15.0f, spawnY = 7.0f, type = EnemyType.RANGED),
            Enemy(spawnX = 23.5f, spawnY = 1.2f, type = EnemyType.MELEE)
        )

        return TowerFloor(
            floorNumber = 1,
            floorName = "Lantai 1: Lower Bastion",
            mapBounds = Rectangle(-10f, -1f, 42f, 14f),
            spawnX = -1f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 12f, y = 1f),
            exitPortal = StageExit(x = 28f, y = 1f),
            isBossFloor = false
        )
    }

    private fun buildFloor2(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Lantai Dasar & Tembok Menara Tinggi
        platforms.add(Platform(x = -8f, y = 0f, width = 39f, height = 1f, color = Color(0.16f, 0.20f, 0.26f, 1f)))
        platforms.add(Platform(x = -8f, y = 1f, width = 1f, height = 17f, color = Color(0.12f, 0.15f, 0.20f, 1f)))
        platforms.add(Platform(x = 30f, y = 1f, width = 1f, height = 17f, color = Color(0.12f, 0.15f, 0.20f, 1f)))

        // Rute Panjatan Vertikal via One-Way Platform & Solid Block
        platforms.add(Platform(x = 1.5f, y = 2.6f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 6.5f, y = 4.8f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 12.0f, y = 6.2f, width = 5.0f, height = 0.5f, color = Color(0.22f, 0.30f, 0.42f, 1f)))
        platforms.add(Platform(x = 18.0f, y = 3.2f, width = 4.5f, height = 0.5f, color = Color(0.22f, 0.30f, 0.42f, 1f)))
        platforms.add(Platform(x = 19.0f, y = 7.8f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 23.5f, y = 9.5f, width = 6.0f, height = 0.5f, color = Color(0.26f, 0.36f, 0.50f, 1f)))

        // Duri Lantai yang Mematikan di Bawah Rute Panjatan
        val spikes = listOf(
            SpikeHazard(x = 4.0f, y = 1.0f, width = 3.0f, height = 0.5f),
            SpikeHazard(x = 10.5f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 22.0f, y = 1.0f, width = 4.0f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 13.5f, y = 6.7f),
            TreasureChest(x = 28.0f, y = 10.0f) // Hadiah puncak
        )

        val enemies = listOf(
            Enemy(spawnX = 7.5f, spawnY = 1.2f, type = EnemyType.MELEE),
            Enemy(spawnX = 13.5f, spawnY = 6.8f, type = EnemyType.RANGED),
            Enemy(spawnX = 19.5f, spawnY = 1.2f, type = EnemyType.MELEE),
            Enemy(spawnX = 25.0f, spawnY = 10.1f, type = EnemyType.RANGED)
        )

        return TowerFloor(
            floorNumber = 2,
            floorName = "Lantai 2: Spire of Hazards",
            mapBounds = Rectangle(-8f, -1f, 39f, 18f),
            spawnX = -1f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 14.0f, y = 6.7f),
            exitPortal = StageExit(x = 28.0f, y = 10.0f),
            isBossFloor = false
        )
    }

    private fun buildFloor3(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Arena Bos Menara Tertutup (Grand Boss Hall)
        platforms.add(Platform(x = -4f, y = 0f, width = 32f, height = 1.2f, color = Color(0.20f, 0.12f, 0.16f, 1f)))
        platforms.add(Platform(x = -4f, y = 1.2f, width = 1.2f, height = 15f, color = Color(0.14f, 0.08f, 0.12f, 1f)))
        platforms.add(Platform(x = 26.8f, y = 1.2f, width = 1.2f, height = 15f, color = Color(0.14f, 0.08f, 0.12f, 1f)))

        // Platform Loncatan Tempur Udara
        platforms.add(Platform(x = 3.5f, y = 4.5f, width = 4.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 16.5f, y = 4.5f, width = 4.0f, height = 0.35f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 0.5f, y = 1.2f) // Peti Potion Darurat
        )

        val boss = BossEnemy(spawnX = 17.0f, spawnY = 1.5f)

        return TowerFloor(
            floorNumber = 3,
            floorName = "Lantai 3: Apex Boss Arena",
            mapBounds = Rectangle(-4f, -1f, 32f, 16f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 1.5f, y = 1.2f),
            exitPortal = StageExit(x = 23.5f, y = 1.2f),
            isBossFloor = true
        )
    }
}

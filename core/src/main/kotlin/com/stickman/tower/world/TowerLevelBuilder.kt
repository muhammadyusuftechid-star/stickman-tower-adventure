package com.stickman.tower.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.enemy.EnemyType
import com.stickman.tower.physics.Platform

import com.stickman.tower.endless.FloorMutator

/**
 * Pembangun Level Menara Bertingkat (Tower Level Builder).
 * Menyediakan layout preset untuk Lantai 1 s/d Lantai 3 (Apex Boss Arena),
 * serta generator prosedural dinamis untuk Mode Endless Spire (Lantai 4 ke atas).
 */
object TowerLevelBuilder {

    fun buildFloor(floorNumber: Int, mutator: FloorMutator? = null): TowerFloor {
        return when (floorNumber) {
            1 -> buildFloor1()
            2 -> buildFloor2()
            3 -> buildFloor3()
            else -> buildEndlessFloor(floorNumber, mutator ?: FloorMutator.getRandomMutator(floorNumber))
        }
    }

    private fun buildFloor1(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Lantai Dasar & Tembok Pembatas
        platforms.add(Platform(x = -10f, y = 0f, width = 42f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 13f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 31f, y = 1f, width = 1f, height = 13f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        // Platform Bertingkat (Tinggi lompatan terkalibrasi aman: kenaikan 1.6m - 1.7m)
        platforms.add(Platform(x = 2.0f, y = 2.2f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.0f, y = 3.8f, width = 4.0f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 13.5f, y = 5.4f, width = 5.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 20.5f, y = 3.6f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        // One-Way Stepping Platform (Penyelamat jika jatuh dari platform atas)
        platforms.add(Platform(x = 13.5f, y = 2.4f, width = 4.5f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 9.0f, y = 1.0f, width = 2.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 15.5f, y = 5.9f)
        )

        val enemies = listOf(
            Enemy(spawnX = 4.0f, spawnY = 2.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 15.5f, spawnY = 5.9f, type = EnemyType.RANGED),
            Enemy(spawnX = 23.0f, spawnY = 1.2f, type = EnemyType.MELEE)
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

        // Rute Panjatan Vertikal Berkelanjutan (Kenaikan 1.4m s/d 1.75m per anak tangga)
        platforms.add(Platform(x = 1.5f, y = 2.4f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 6.0f, y = 4.0f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 11.0f, y = 5.6f, width = 4.5f, height = 0.5f, color = Color(0.22f, 0.30f, 0.42f, 1f)))
        platforms.add(Platform(x = 16.5f, y = 7.0f, width = 4.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 22.0f, y = 8.6f, width = 7.0f, height = 0.5f, color = Color(0.26f, 0.36f, 0.50f, 1f)))

        // Tangga Penyelamat (Mencegah Deadlock / Softlock jika pemain jatuh ke bawah)
        platforms.add(Platform(x = 16.5f, y = 3.8f, width = 4.0f, height = 0.5f, color = Color(0.22f, 0.30f, 0.42f, 1f)))
        platforms.add(Platform(x = 23.0f, y = 3.4f, width = 3.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 23.5f, y = 6.0f, width = 3.5f, height = 0.35f, isOneWay = true))

        // Duri Lantai yang Terisolasi Aman di Area Bawah
        val spikes = listOf(
            SpikeHazard(x = 4.0f, y = 1.0f, width = 2.0f, height = 0.5f),
            SpikeHazard(x = 10.0f, y = 1.0f, width = 2.5f, height = 0.5f),
            SpikeHazard(x = 18.0f, y = 1.0f, width = 2.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 12.5f, y = 6.1f),
            TreasureChest(x = 24.5f, y = 9.1f) // Hadiah puncak
        )

        val enemies = listOf(
            Enemy(spawnX = 7.5f, spawnY = 1.2f, type = EnemyType.MELEE),
            Enemy(spawnX = 13.5f, spawnY = 6.1f, type = EnemyType.RANGED),
            Enemy(spawnX = 18.0f, spawnY = 4.3f, type = EnemyType.MELEE),
            Enemy(spawnX = 26.0f, spawnY = 9.1f, type = EnemyType.RANGED)
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
            checkpoint = Checkpoint(x = 13.5f, y = 6.1f),
            exitPortal = StageExit(x = 27.5f, y = 9.1f),
            isBossFloor = false
        )
    }

    private fun buildFloor3(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Arena Bos Menara Tertutup (Grand Boss Hall)
        platforms.add(Platform(x = -4f, y = 0f, width = 32f, height = 1.2f, color = Color(0.20f, 0.12f, 0.16f, 1f)))
        platforms.add(Platform(x = -4f, y = 1.2f, width = 1.2f, height = 15f, color = Color(0.14f, 0.08f, 0.12f, 1f)))
        platforms.add(Platform(x = 26.8f, y = 1.2f, width = 1.2f, height = 15f, color = Color(0.14f, 0.08f, 0.12f, 1f)))

        // Platform Loncatan Tempur Udara (Ketinggian 2.05m dari lantai, dapat dicapai dengan 1x lompatan)
        platforms.add(Platform(x = 3.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 16.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 9.5f, y = 4.6f, width = 5.0f, height = 0.35f, isOneWay = true))

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

    fun buildEndlessFloor(floorNumber: Int, mutator: FloorMutator): TowerFloor {
        val endlessRank = (floorNumber - 3).coerceAtLeast(1)
        val hpScale = 1.0f + endlessRank * 0.15f
        val dmgScale = (1.0f + endlessRank * 0.12f) * mutator.enemyDamageMultiplier
        val isBossFloor = (floorNumber % 5 == 0)

        val platforms = mutableListOf<Platform>()
        val spikes = mutableListOf<SpikeHazard>()
        val chests = mutableListOf<TreasureChest>()
        val enemies = mutableListOf<Enemy>()

        if (isBossFloor) {
            // Apex Boss Hall Variant
            platforms.add(Platform(x = -5f, y = 0f, width = 34f, height = 1.2f, color = Color(0.18f, 0.10f, 0.22f, 1f)))
            platforms.add(Platform(x = -5f, y = 1.2f, width = 1.2f, height = 16f, color = Color(0.12f, 0.06f, 0.16f, 1f)))
            platforms.add(Platform(x = 27.8f, y = 1.2f, width = 1.2f, height = 16f, color = Color(0.12f, 0.06f, 0.16f, 1f)))

            platforms.add(Platform(x = 2.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
            platforms.add(Platform(x = 16.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
            platforms.add(Platform(x = 9.5f, y = 4.8f, width = 5.0f, height = 0.35f, isOneWay = true))

            chests.add(TreasureChest(x = 0.5f, y = 1.2f))

            val boss = BossEnemy(spawnX = 18.0f, spawnY = 1.5f)
            boss.health.setMaxHp(350f * hpScale)
            boss.hitbox.damage *= dmgScale

            return TowerFloor(
                floorNumber = floorNumber,
                floorName = "Endless Spire: Floor $floorNumber [BOSS: ${mutator.title}]",
                mapBounds = Rectangle(-5f, -1f, 34f, 17f),
                spawnX = 0f,
                spawnY = 1.5f,
                platforms = platforms,
                spikes = emptyList(),
                chests = chests,
                enemies = emptyList(),
                boss = boss,
                checkpoint = Checkpoint(x = 1.5f, y = 1.2f),
                exitPortal = StageExit(x = 24.5f, y = 1.2f),
                isBossFloor = true
            )
        }

        // Non-Boss Procedural Variants (0 = Ascent, 1 = Gauntlet, 2 = Colosseum)
        val variant = floorNumber % 3
        val mapWidth = 42f
        val mapHeight = 16f

        platforms.add(Platform(x = -8f, y = 0f, width = mapWidth, height = 1f, color = Color(0.15f, 0.18f, 0.25f, 1f)))
        platforms.add(Platform(x = -8f, y = 1f, width = 1f, height = mapHeight, color = Color(0.10f, 0.12f, 0.18f, 1f)))
        platforms.add(Platform(x = mapWidth - 9f, y = 1f, width = 1f, height = mapHeight, color = Color(0.10f, 0.12f, 0.18f, 1f)))

        when (variant) {
            0 -> {
                // Vertical Ascent Spire (Kenaikan bertahap 1.45m - 1.75m)
                platforms.add(Platform(x = 2f, y = 2.4f, width = 4.0f, height = 0.35f, isOneWay = true))
                platforms.add(Platform(x = 7.0f, y = 4.0f, width = 4.5f, height = 0.5f, color = Color(0.22f, 0.28f, 0.38f, 1f)))
                platforms.add(Platform(x = 12.5f, y = 5.6f, width = 4.0f, height = 0.35f, isOneWay = true))
                platforms.add(Platform(x = 17.5f, y = 4.0f, width = 4.5f, height = 0.5f, color = Color(0.22f, 0.28f, 0.38f, 1f)))
                platforms.add(Platform(x = 23.0f, y = 5.6f, width = 4.5f, height = 0.35f, isOneWay = true))
                platforms.add(Platform(x = 28.5f, y = 3.8f, width = 3.5f, height = 0.35f, isOneWay = true))

                spikes.add(SpikeHazard(x = 4.5f, y = 1.0f, width = 2.0f, height = 0.5f))
                spikes.add(SpikeHazard(x = 14.5f, y = 1.0f, width = 2.5f, height = 0.5f))
                chests.add(TreasureChest(x = 13.5f, y = 6.0f))
            }
            1 -> {
                // Gauntlet of Peril
                platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.32f, 0.44f, 1f)))
                platforms.add(Platform(x = 8.5f, y = 3.8f, width = 4.0f, height = 0.35f, isOneWay = true))
                platforms.add(Platform(x = 14.0f, y = 5.4f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.32f, 0.44f, 1f)))
                platforms.add(Platform(x = 20.0f, y = 4.0f, width = 4.5f, height = 0.5f, color = Color(0.24f, 0.32f, 0.44f, 1f)))
                platforms.add(Platform(x = 26.0f, y = 5.6f, width = 4.5f, height = 0.35f, isOneWay = true))

                spikes.add(SpikeHazard(x = 7.5f, y = 1.0f, width = 2.5f, height = 0.5f))
                spikes.add(SpikeHazard(x = 19.0f, y = 1.0f, width = 2.5f, height = 0.5f))
                chests.add(TreasureChest(x = 15.5f, y = 6.0f))
            }
            else -> {
                // Tiered Colosseum (Puncak tengah dapat diakses dari kedua sisi)
                platforms.add(Platform(x = 2.5f, y = 2.4f, width = 5.0f, height = 0.5f, color = Color(0.26f, 0.35f, 0.48f, 1f)))
                platforms.add(Platform(x = 9.0f, y = 4.2f, width = 5.0f, height = 0.5f, color = Color(0.26f, 0.35f, 0.48f, 1f)))
                platforms.add(Platform(x = 18.0f, y = 4.2f, width = 5.0f, height = 0.5f, color = Color(0.26f, 0.35f, 0.48f, 1f)))
                platforms.add(Platform(x = 24.5f, y = 2.4f, width = 5.0f, height = 0.5f, color = Color(0.26f, 0.35f, 0.48f, 1f)))

                platforms.add(Platform(x = 13.5f, y = 6.0f, width = 5.0f, height = 0.35f, isOneWay = true))

                spikes.add(SpikeHazard(x = 14.5f, y = 1.0f, width = 3.0f, height = 0.5f))
                chests.add(TreasureChest(x = 15.5f, y = 6.5f))
            }
        }

        if (mutator == FloorMutator.SPIKE_FRENZY) {
            spikes.add(SpikeHazard(x = 1.0f, y = 1.0f, width = 2.0f, height = 0.5f))
            spikes.add(SpikeHazard(x = 25.0f, y = 1.0f, width = 2.5f, height = 0.5f))
        }

        // Spawn scaled enemies
        enemies.add(Enemy(spawnX = 5.0f, spawnY = 1.2f, type = EnemyType.MELEE))
        enemies.add(Enemy(spawnX = 11.0f, spawnY = 5.0f, type = EnemyType.RANGED))
        enemies.add(Enemy(spawnX = 17.0f, spawnY = 1.2f, type = EnemyType.MELEE))
        enemies.add(Enemy(spawnX = 23.0f, spawnY = 5.0f, type = EnemyType.RANGED))
        enemies.add(Enemy(spawnX = 27.0f, spawnY = 1.2f, type = EnemyType.MELEE))

        for (enemy in enemies) {
            enemy.health.setMaxHp(enemy.health.maxHp * hpScale)
            enemy.hitbox.damage *= dmgScale
        }

        return TowerFloor(
            floorNumber = floorNumber,
            floorName = "Endless Spire: Floor $floorNumber [${mutator.title}]",
            mapBounds = Rectangle(-8f, -1f, mapWidth, mapHeight),
            spawnX = -1f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 16.0f, y = 6.0f),
            exitPortal = StageExit(x = mapWidth - 12f, y = 1.0f),
            isBossFloor = false
        )
    }
}

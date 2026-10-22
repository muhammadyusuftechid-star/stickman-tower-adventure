package com.stickman.tower.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.BossType
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.enemy.EnemyType
import com.stickman.tower.physics.Platform
import com.stickman.tower.endless.FloorMutator
import com.stickman.tower.narrative.DialogueRegistry

/**
 * Pembangun Level Menara Bertingkat (Tower Level Builder).
 * Mengimplementasikan 10 Lantai Kampanye Cerita Utama (The 10-Floor Story Odyssey)
 * yang terbagi dalam 4 Babak Epik (Act I - Act IV), 5 Pertarungan Bos Legendaris,
 * Prasasti Batu Hitam Kuno (The Lore Tablets), Tempat Perlindungan Vulcanus,
 * serta Generator Prosedural Dinamis untuk Mode Endless Spire (Lantai 11 ke atas).
 */
object TowerLevelBuilder {

    fun buildFloor(floorNumber: Int, mutator: FloorMutator? = null): TowerFloor {
        val floor = when (floorNumber) {
            1 -> buildFloor1()
            2 -> buildFloor2()
            3 -> buildFloor3()
            4 -> buildFloor4()
            5 -> buildFloor5()
            6 -> buildFloor6()
            7 -> buildFloor7()
            8 -> buildFloor8()
            9 -> buildFloor9()
            10 -> buildFloor10()
            else -> buildEndlessFloor(floorNumber, mutator ?: FloorMutator.getRandomMutator(floorNumber))
        }
        return floor.copy(environmentalProps = getEnvironmentalPropsForFloor(floorNumber))
    }

    fun getEnvironmentalPropsForFloor(floorNumber: Int): List<EnvironmentalProp> {
        return when (floorNumber) {
            1 -> listOf(
                EnvironmentalProp(x = 1.0f, y = 5.0f, width = 0.6f, height = 2.4f, type = PropType.BROKEN_CHAIN),
                EnvironmentalProp(x = 14.0f, y = 1.0f, width = 1.2f, height = 2.0f, type = PropType.BEHEADED_STATUE),
                EnvironmentalProp(x = 7.0f, y = 3.5f, width = 1.8f, height = 1.4f, type = PropType.INK_SPLATTER_MURAL)
            )
            2 -> listOf(
                EnvironmentalProp(x = 8.0f, y = 8.0f, width = 0.6f, height = 2.8f, type = PropType.BROKEN_CHAIN),
                EnvironmentalProp(x = 20.0f, y = 1.0f, width = 1.0f, height = 1.8f, type = PropType.BEHEADED_STATUE),
                EnvironmentalProp(x = 16.0f, y = 7.5f, width = 1.6f, height = 1.2f, type = PropType.INK_SPLATTER_MURAL)
            )
            3 -> listOf(
                EnvironmentalProp(x = 3.0f, y = 6.0f, width = 0.6f, height = 2.5f, type = PropType.BROKEN_CHAIN),
                EnvironmentalProp(x = 22.0f, y = 1.0f, width = 1.4f, height = 2.2f, type = PropType.BEHEADED_STATUE),
                EnvironmentalProp(x = 12.0f, y = 4.0f, width = 2.2f, height = 1.6f, type = PropType.INK_SPLATTER_MURAL)
            )
            4 -> listOf(
                EnvironmentalProp(x = 5.0f, y = 1.0f, width = 1.4f, height = 2.2f, type = PropType.SHATTERED_BOOKSHELF),
                EnvironmentalProp(x = 18.0f, y = 4.8f, width = 1.4f, height = 2.2f, type = PropType.SHATTERED_BOOKSHELF),
                EnvironmentalProp(x = 11.0f, y = 1.0f, width = 0.8f, height = 1.8f, type = PropType.LEAKING_ALCHEMY_VAT)
            )
            5 -> listOf(
                EnvironmentalProp(x = 12.0f, y = 1.0f, width = 1.4f, height = 2.4f, type = PropType.BEHEADED_STATUE),
                EnvironmentalProp(x = 42.0f, y = 1.0f, width = 1.2f, height = 2.0f, type = PropType.LEAKING_ALCHEMY_VAT),
                EnvironmentalProp(x = 72.0f, y = 1.0f, width = 1.4f, height = 2.4f, type = PropType.BEHEADED_STATUE)
            )
            6 -> listOf(
                EnvironmentalProp(x = 18.0f, y = 1.2f, width = 2.2f, height = 1.8f, type = PropType.TITAN_ANVIL),
                EnvironmentalProp(x = 35.0f, y = 3.5f, width = 1.6f, height = 2.2f, type = PropType.SHATTERED_BOOKSHELF),
                EnvironmentalProp(x = 55.0f, y = 1.0f, width = 1.2f, height = 2.0f, type = PropType.LEAKING_ALCHEMY_VAT)
            )
            7 -> listOf(
                EnvironmentalProp(x = 15.0f, y = 6.0f, width = 2.0f, height = 3.0f, type = PropType.VOID_WALL_CRACK),
                EnvironmentalProp(x = 56.0f, y = 8.0f, width = 2.4f, height = 3.5f, type = PropType.VOID_WALL_CRACK),
                EnvironmentalProp(x = 90.0f, y = 5.0f, width = 2.0f, height = 3.0f, type = PropType.VOID_WALL_CRACK)
            )
            8 -> listOf(
                EnvironmentalProp(x = 22.0f, y = 4.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK),
                EnvironmentalProp(x = 68.0f, y = 8.5f, width = 1.6f, height = 2.2f, type = PropType.BLOOD_ECLIPSE_ALTAR),
                EnvironmentalProp(x = 105.0f, y = 5.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK)
            )
            9 -> listOf(
                EnvironmentalProp(x = 20.0f, y = 5.0f, width = 1.8f, height = 2.5f, type = PropType.INK_SPLATTER_MURAL),
                EnvironmentalProp(x = 58.0f, y = 1.0f, width = 1.6f, height = 2.4f, type = PropType.LEAKING_ALCHEMY_VAT),
                EnvironmentalProp(x = 92.0f, y = 6.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK)
            )
            10 -> listOf(
                EnvironmentalProp(x = 12.0f, y = 1.0f, width = 1.2f, height = 5.0f, type = PropType.PURE_CANVAS_PILLAR),
                EnvironmentalProp(x = 34.0f, y = 4.2f, width = 1.5f, height = 2.2f, type = PropType.FROZEN_GOLDEN_BRUSH),
                EnvironmentalProp(x = 65.0f, y = 1.0f, width = 1.2f, height = 5.0f, type = PropType.PURE_CANVAS_PILLAR)
            )
            else -> listOf(
                EnvironmentalProp(x = 6.0f, y = 4.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK)
            )
        }
    }

    // =========================================================================
    // BABAK I: UJIAN INISIASI (ACT I: THE INITIATION — LANTAI 1 S/D 3)
    // =========================================================================

    private fun buildFloor1(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val baseColor = Color(0.18f, 0.24f, 0.32f, 1f)
        val wallColor = Color(0.14f, 0.18f, 0.24f, 1f)
        val platColor = Color(0.24f, 0.35f, 0.48f, 1f)

        // Lantai Dasar Bentang Luas (115m) & Dinding Batas
        platforms.add(Platform(x = -10f, y = 0f, width = 125f, height = 1f, color = baseColor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 15f, color = wallColor))
        platforms.add(Platform(x = 114f, y = 1f, width = 1f, height = 15f, color = wallColor))

        // Koridor Platform Bertingkat & One-Way Stepping
        // Sektor Barat (0m - 35m)
        platforms.add(Platform(x = 4.0f, y = 2.4f, width = 5.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 12.0f, y = 4.0f, width = 4.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 19.5f, y = 5.6f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 19.5f, y = 2.5f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 28.0f, y = 3.8f, width = 5.5f, height = 0.5f, color = platColor))

        // Sektor Tengah (36m - 75m)
        platforms.add(Platform(x = 37.0f, y = 2.4f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 44.5f, y = 4.2f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 53.0f, y = 6.0f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 61.5f, y = 4.2f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 69.0f, y = 2.5f, width = 5.5f, height = 0.5f, color = platColor))

        // Sektor Timur (76m - 110m) Menuju Gerbang Menara
        platforms.add(Platform(x = 77.0f, y = 4.2f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 85.5f, y = 5.8f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 85.5f, y = 2.6f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 94.0f, y = 4.0f, width = 5.5f, height = 0.5f, color = platColor))

        val spikes = listOf(
            SpikeHazard(x = 14.0f, y = 1.0f, width = 3.0f, height = 0.5f),
            SpikeHazard(x = 43.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 79.0f, y = 1.0f, width = 3.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 22.0f, y = 6.1f),
            TreasureChest(x = 55.5f, y = 6.5f),
            TreasureChest(x = 88.0f, y = 6.3f)
        )

        val enemies = listOf(
            Enemy(spawnX = 7.0f, spawnY = 2.9f, type = EnemyType.MELEE),
            Enemy(spawnX = 22.0f, spawnY = 6.1f, type = EnemyType.RANGED),
            Enemy(spawnX = 32.0f, spawnY = 1.2f, type = EnemyType.MELEE),
            Enemy(spawnX = 47.0f, spawnY = 4.7f, type = EnemyType.RANGED),
            Enemy(spawnX = 56.0f, spawnY = 6.5f, type = EnemyType.MELEE),
            Enemy(spawnX = 72.0f, spawnY = 3.0f, type = EnemyType.FLYING),
            Enemy(spawnX = 88.0f, spawnY = 6.3f, type = EnemyType.RANGED),
            Enemy(spawnX = 96.0f, spawnY = 1.2f, type = EnemyType.MELEE)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 21.0f,
                y = 5.6f,
                tabletId = "TABLET_1",
                title = "Tablet I: Catatan Hari ke-47",
                text = "Hari ke-47. Kami tiba di gerbang bawah dengan 100 prajurit terbaik. Tubuh kami telah menghitam, jari-jari menyatu menjadi garis padat. Makanan tak lagi berasa, namun pedang terasa seratus kali lebih ringan. Jika catatan ini kau temukan, ketahuilah: jangan pernah berhenti bergerak saat berada di atas platform."
            )
        )

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(1))

        return TowerFloor(
            floorNumber = 1,
            floorName = "Lantai 1: Lower Bastion",
            mapBounds = Rectangle(-10f, -1f, 125f, 16f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 50.0f, y = 1.0f),
            exitPortal = StageExit(x = 104.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "LOWER BASTION",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor2(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val baseColor = Color(0.16f, 0.20f, 0.26f, 1f)
        val wallColor = Color(0.12f, 0.15f, 0.20f, 1f)
        val platColor = Color(0.22f, 0.30f, 0.42f, 1f)

        // Lantai Dasar Bentang Luas (120m) & Tembok Menara Tinggi
        platforms.add(Platform(x = -10f, y = 0f, width = 130f, height = 1f, color = baseColor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 18f, color = wallColor))
        platforms.add(Platform(x = 119f, y = 1f, width = 1f, height = 18f, color = wallColor))

        // Rute Panjatan Vertikal Berkelanjutan
        platforms.add(Platform(x = 3.0f, y = 2.4f, width = 4.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 9.5f, y = 4.2f, width = 4.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 16.5f, y = 6.0f, width = 5.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 24.0f, y = 7.6f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 24.0f, y = 3.2f, width = 4.5f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 33.0f, y = 5.0f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 41.0f, y = 3.2f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 49.0f, y = 5.2f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 57.5f, y = 7.2f, width = 6.0f, height = 0.5f, color = platColor))

        platforms.add(Platform(x = 66.0f, y = 5.0f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 73.5f, y = 3.2f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 81.5f, y = 5.2f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 90.0f, y = 7.2f, width = 6.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 99.0f, y = 4.5f, width = 5.0f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 12.0f, y = 1.0f, width = 3.0f, height = 0.5f),
            SpikeHazard(x = 35.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 68.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 92.0f, y = 1.0f, width = 3.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 26.5f, y = 8.1f),
            TreasureChest(x = 60.0f, y = 7.7f),
            TreasureChest(x = 93.0f, y = 7.7f)
        )

        val enemies = listOf(
            Enemy(spawnX = 10.0f, spawnY = 4.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 26.0f, spawnY = 8.1f, type = EnemyType.RANGED),
            Enemy(spawnX = 44.0f, spawnY = 3.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 60.0f, spawnY = 7.7f, type = EnemyType.FLYING),
            Enemy(spawnX = 76.0f, spawnY = 3.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 93.0f, spawnY = 7.7f, type = EnemyType.RANGED)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 59.0f,
                y = 7.2f,
                tabletId = "TABLET_2",
                title = "Tablet II: Catatan Para Pendaki",
                text = "Kutukan gravitasi ini bukan dari bumi, melainkan dari langit. Para dewa tidak ingin kita terbang. Mereka memasang duri-duri obsidian ini agar darah kita kembali menyuburkan tanah. Jangan pernah ragu saat melangkah: hanya mereka yang terus bergerak yang sampai ke puncak."
            )
        )

        return TowerFloor(
            floorNumber = 2,
            floorName = "Lantai 2: Spire of Hazards",
            mapBounds = Rectangle(-10f, -1f, 130f, 18f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 52.0f, y = 1.0f),
            exitPortal = StageExit(x = 108.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "SPIRE OF HAZARDS",
            loreTablets = loreTablets,
            storyDialogues = emptyList()
        )
    }

    private fun buildFloor3(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val baseColor = Color(0.20f, 0.14f, 0.22f, 1f)
        val wallColor = Color(0.14f, 0.10f, 0.16f, 1f)
        val platColor = Color(0.28f, 0.20f, 0.32f, 1f)

        // Peta Bentang Luas (125m) dengan Runic Barrier Pertama di x = 88m
        platforms.add(Platform(x = -10f, y = 0f, width = 135f, height = 1f, color = baseColor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 18f, color = wallColor))
        platforms.add(Platform(x = 124f, y = 1f, width = 1f, height = 18f, color = wallColor))

        // Koridor Bawah & Tangga Menuju Menara Kunci (Key Tower)
        platforms.add(Platform(x = 4.0f, y = 2.4f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 11.5f, y = 4.2f, width = 5.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 19.5f, y = 6.0f, width = 5.5f, height = 0.5f, color = platColor))

        platforms.add(Platform(x = 28.0f, y = 4.0f, width = 5.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 36.0f, y = 3.0f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 44.5f, y = 5.0f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 53.5f, y = 7.0f, width = 5.5f, height = 0.5f, color = platColor))

        // Altar Kunci Runik (Runic Key Platform di Ketinggian)
        platforms.add(Platform(x = 62.0f, y = 8.5f, width = 7.0f, height = 0.6f, color = Color(0.38f, 0.18f, 0.28f, 1f)))
        platforms.add(Platform(x = 62.0f, y = 3.5f, width = 5.5f, height = 0.35f, isOneWay = true))

        // Koridor Lanjut Menuju Pintu Runic Barrier
        platforms.add(Platform(x = 72.0f, y = 4.5f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 80.5f, y = 3.0f, width = 5.5f, height = 0.5f, color = platColor))

        // Melewati Barrier Menuju Exit Portal
        platforms.add(Platform(x = 94.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 103.0f, y = 5.2f, width = 6.5f, height = 0.5f, color = platColor))

        val spikes = listOf(
            SpikeHazard(x = 14.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 38.0f, y = 1.0f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 74.0f, y = 1.0f, width = 4.0f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 21.0f, y = 6.5f),
            TreasureChest(x = 67.0f, y = 9.1f), // Hadiah di samping kunci
            TreasureChest(x = 106.0f, y = 5.7f)
        )

        val enemies = listOf(
            Enemy(spawnX = 13.0f, spawnY = 4.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 22.0f, spawnY = 6.5f, type = EnemyType.RANGED),
            Enemy(spawnX = 47.0f, spawnY = 5.5f, type = EnemyType.MELEE),
            Enemy(spawnX = 64.0f, spawnY = 9.1f, type = EnemyType.RANGED), // Penjaga Kunci
            Enemy(spawnX = 66.5f, spawnY = 9.1f, type = EnemyType.MELEE),  // Penjaga Kunci
            Enemy(spawnX = 74.0f, spawnY = 5.0f, type = EnemyType.FLYING),
            Enemy(spawnX = 97.0f, spawnY = 4.0f, type = EnemyType.MELEE)
        )

        // Barrier di x = 88m & Kunci di x = 65m, y = 9m
        val barrierId = "BARRIER_FLOOR_3"
        val runicBarriers = listOf(
            RunicBarrier(x = 88.0f, y = 1.0f, width = 0.8f, height = 6.5f, barrierId = barrierId)
        )
        val runicKeys = listOf(
            RunicKey(x = 65.0f, y = 9.2f, targetBarrierId = barrierId)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 20.0f,
                y = 6.0f,
                tabletId = "TABLET_3",
                title = "Tablet III: Segel Gerbang Kuno",
                text = "Pintu gerbang energi merah di depan dijaga oleh Segel Runik Bawah. Jangan membuang waktu mendobraknya dengan pedang biasa: carilah Inti Kristal Kunci di atas menara arsip samping untuk mendisipasi penghalangnya!"
            )
        )

        return TowerFloor(
            floorNumber = 3,
            floorName = "Lantai 3: The Sealed Armory",
            mapBounds = Rectangle(-10f, -1f, 135f, 18f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 48.0f, y = 1.0f),
            exitPortal = StageExit(x = 114.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "THE SEALED ARMORY",
            loreTablets = loreTablets,
            storyDialogues = emptyList(),
            runicBarriers = runicBarriers,
            runicKeys = runicKeys
        )
    }

    // =========================================================================
    // BABAK II: RAHASIA TINTA & LOGAM ABADI (ACT II: SECRETS OF THE INK — LANTAI 4 S/D 6)
    // =========================================================================

    private fun buildFloor4(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val amethystFloor = Color(0.20f, 0.15f, 0.28f, 1f)
        val amethystWall = Color(0.14f, 0.10f, 0.22f, 1f)
        val crystalPlatform = Color(0.26f, 0.20f, 0.38f, 1f)

        // Perpustakaan Kaca Kristal Amethyst Bentang Luas (120m)
        platforms.add(Platform(x = -10f, y = 0f, width = 130f, height = 1f, color = amethystFloor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 18f, color = amethystWall))
        platforms.add(Platform(x = 119f, y = 1f, width = 1f, height = 18f, color = amethystWall))

        // Meja-meja Arsip Kuno & Tangga Kaca Kristal
        platforms.add(Platform(x = 3.0f, y = 2.4f, width = 5.0f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 10.5f, y = 4.2f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 17.5f, y = 6.0f, width = 6.0f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 26.0f, y = 4.2f, width = 5.0f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 33.5f, y = 6.2f, width = 5.5f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 42.0f, y = 3.5f, width = 5.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 50.5f, y = 5.5f, width = 6.0f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 59.0f, y = 7.5f, width = 6.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 68.0f, y = 5.0f, width = 5.0f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 75.5f, y = 3.5f, width = 5.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 83.5f, y = 5.5f, width = 6.0f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 92.0f, y = 7.5f, width = 6.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 101.0f, y = 4.5f, width = 5.5f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 13.0f, y = 1.0f, width = 3.0f, height = 0.5f),
            SpikeHazard(x = 36.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 70.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 94.0f, y = 1.0f, width = 3.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 20.0f, y = 6.5f),
            TreasureChest(x = 61.5f, y = 8.0f),
            TreasureChest(x = 94.5f, y = 8.0f)
        )

        val enemies = listOf(
            Enemy(spawnX = 12.0f, spawnY = 4.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 19.0f, spawnY = 6.5f, type = EnemyType.RANGED),
            Enemy(spawnX = 35.0f, spawnY = 6.7f, type = EnemyType.FLYING),
            Enemy(spawnX = 52.0f, spawnY = 6.0f, type = EnemyType.MELEE),
            Enemy(spawnX = 61.0f, spawnY = 8.0f, type = EnemyType.RANGED),
            Enemy(spawnX = 77.0f, spawnY = 4.0f, type = EnemyType.MELEE),
            Enemy(spawnX = 94.0f, spawnY = 8.0f, type = EnemyType.RANGED)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 19.5f,
                y = 6.0f,
                tabletId = "TABLET_4",
                title = "Tablet IV: Kutukan Sang Penulis",
                text = "Mereka menyebut kami monster karena kami menggunakan panah amethyst dari kejauhan. Tapi mereka tidak mengerti: mendekati bilah pedang di menara ini berarti kematian. Kami menyegel diri dalam ruang arsip ini untuk mencari penawar racun tinta... namun tinta itu sendiri telah menjadi darah kami."
            )
        )

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 2.0f,
                triggerY = 1.5f,
                triggerRadius = 3.0f,
                lines = listOf(
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Perpustakaan Kaca Hitam... di sinilah para cendekiawan Amethyst Weavers meneliti rahasia Tinta Primordial sebelum mereka dikutuk menjadi pemanah bayangan.",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 4,
            floorName = "Lantai 4: The Sunken Archives",
            mapBounds = Rectangle(-10f, -1f, 130f, 18f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 54.0f, y = 1.0f),
            exitPortal = StageExit(x = 110.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "ACT II: SECRETS OF THE INK",
            actSubtitle = "THE SUNKEN ARCHIVES",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            bloodAltar = BloodAltar(x = 65.0f, y = 1.0f)
        )
    }

    private fun buildFloor5(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val arenaFloor = Color(0.22f, 0.18f, 0.16f, 1f)
        val arenaWall = Color(0.14f, 0.11f, 0.10f, 1f)
        val pillarColor = Color(0.32f, 0.25f, 0.20f, 1f)

        // Colosseum Obsidian Bentang Luas (95m)
        platforms.add(Platform(x = -10f, y = 0f, width = 105f, height = 1.2f, color = arenaFloor))
        platforms.add(Platform(x = -10f, y = 1.2f, width = 1.2f, height = 22f, color = arenaWall))
        platforms.add(Platform(x = 94f, y = 1.2f, width = 1.2f, height = 22f, color = arenaWall))

        // Platform Berundak Sayap Kiri (Tribun Pengamat Barat)
        platforms.add(Platform(x = 5.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = pillarColor))
        platforms.add(Platform(x = 13.5f, y = 6.0f, width = 7.0f, height = 0.5f, color = pillarColor))
        platforms.add(Platform(x = 23.0f, y = 4.2f, width = 5.5f, height = 0.35f, isOneWay = true))

        // Platform Akrobatik Tengah (Mengindari Shield Shockwave Oakhaven)
        platforms.add(Platform(x = 34.0f, y = 5.2f, width = 8.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 48.0f, y = 6.8f, width = 7.0f, height = 0.4f, isOneWay = true))

        // Platform Berundak Sayap Kanan (Tribun Pengamat Timur)
        platforms.add(Platform(x = 61.0f, y = 4.2f, width = 5.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 70.0f, y = 6.0f, width = 7.0f, height = 0.5f, color = pillarColor))
        platforms.add(Platform(x = 80.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = pillarColor))

        val chests = listOf(
            TreasureChest(x = 15.0f, y = 6.5f),
            TreasureChest(x = 72.0f, y = 6.5f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 6.0f,
                y = 4.0f,
                tabletId = "TABLET_5",
                title = "Tablet V: Prasasti Colosseum Oakhaven",
                text = "Di arena pasir hitam ini, General Oakhaven bersumpah bahwa tak seorang pun berhak melangkah ke lantai atas tanpa membuktikan ketahanan tekadnya. Perisai besarnya tak tertembus dari depan: pancing serangannya dan tebas dari belakang!"
            )
        )

        val boss = BossEnemy(spawnX = 48.0f, spawnY = 1.5f, type = BossType.TOWER_GUARDIAN)
        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(5))

        return TowerFloor(
            floorNumber = 5,
            floorName = "Lantai 5: The Obsidian Colosseum",
            mapBounds = Rectangle(-10f, -1f, 105f, 24f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 2.0f, y = 1.2f),
            exitPortal = StageExit(x = 86.0f, y = 1.2f),
            isBossFloor = true,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE OBSIDIAN COLOSSEUM",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor6(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val forgeFloor = Color(0.28f, 0.18f, 0.12f, 1f)
        val forgeWall = Color(0.18f, 0.10f, 0.08f, 1f)
        val anvilStone = Color(0.42f, 0.25f, 0.14f, 1f)

        // Bengkel Tempa Logam Vulcanus (Zona Aman / Safe Haven — Bentang 90m)
        platforms.add(Platform(x = -10f, y = 0f, width = 100f, height = 1.2f, color = forgeFloor))
        platforms.add(Platform(x = -10f, y = 1.2f, width = 1.2f, height = 18f, color = forgeWall))
        platforms.add(Platform(x = 89f, y = 1.2f, width = 1.2f, height = 18f, color = forgeWall))

        // Meja Tempa, Rak Senjata, & Tungku Perapian Emas
        platforms.add(Platform(x = 6.0f, y = 2.5f, width = 6.0f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 16.0f, y = 1.2f, width = 9.0f, height = 0.8f, color = anvilStone))
        platforms.add(Platform(x = 29.0f, y = 3.5f, width = 7.0f, height = 0.5f, color = anvilStone))
        platforms.add(Platform(x = 40.0f, y = 2.5f, width = 6.0f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 50.0f, y = 3.8f, width = 8.0f, height = 0.5f, color = anvilStone))
        platforms.add(Platform(x = 64.0f, y = 2.5f, width = 6.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 74.0f, y = 3.5f, width = 7.0f, height = 0.5f, color = anvilStone))

        val chests = listOf(
            TreasureChest(x = 18.0f, y = 2.0f),
            TreasureChest(x = 31.0f, y = 4.0f),
            TreasureChest(x = 53.0f, y = 4.3f),
            TreasureChest(x = 76.0f, y = 4.0f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 19.5f,
                y = 2.0f,
                tabletId = "TABLET_6",
                title = "Tablet VI: Wasiat Sang Penempa Vulcanus",
                text = "Besi meteorit ini adalah satu-satunya benda di semesta yang tidak mempan terhadap Tinta Primordial. Istirahatlah sejenak, Kaelen. Di atas sana, langit menara telah retak dan bayangan ratu amethyst menanti."
            )
        )

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(6))

        return TowerFloor(
            floorNumber = 6,
            floorName = "Lantai 6: The Iron Foundry",
            mapBounds = Rectangle(-10f, -1f, 100f, 18f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = null,
            checkpoint = Checkpoint(x = 17.5f, y = 2.0f),
            exitPortal = StageExit(x = 82.0f, y = 1.2f),
            isBossFloor = false,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE IRON FOUNDRY",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            isSafeZone = true
        )
    }

    private fun buildFloor7(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val astralColor = Color(0.16f, 0.22f, 0.36f, 1f)
        val voidFloor = Color(0.10f, 0.14f, 0.22f, 1f)

        // Tebing Melayang Angkasa Kosmis (Bentang Luas 125m, Gravitasi Rendah -35%)
        platforms.add(Platform(x = -10f, y = 0f, width = 135f, height = 0.8f, color = voidFloor))
        platforms.add(Platform(x = -10f, y = 0.8f, width = 1f, height = 22f, color = astralColor))
        platforms.add(Platform(x = 124f, y = 0.8f, width = 1f, height = 22f, color = astralColor))

        // Rute Lompatan Pulau Terapung Udara
        platforms.add(Platform(x = 3.0f, y = 2.5f, width = 5.0f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 12.0f, y = 4.8f, width = 5.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 21.5f, y = 7.0f, width = 6.0f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 31.0f, y = 5.2f, width = 5.0f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 39.5f, y = 3.5f, width = 5.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 48.5f, y = 6.0f, width = 6.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 58.0f, y = 8.5f, width = 7.5f, height = 0.6f, color = astralColor)) // Pulau Altar Darah
        platforms.add(Platform(x = 69.0f, y = 6.2f, width = 5.5f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 77.5f, y = 4.5f, width = 6.0f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 87.0f, y = 7.0f, width = 6.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 97.0f, y = 5.5f, width = 5.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 105.5f, y = 7.5f, width = 7.0f, height = 0.6f, color = astralColor)) // Puncak Exit

        val spikes = listOf(
            SpikeHazard(x = 8.0f, y = 0.8f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 26.0f, y = 0.8f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 44.0f, y = 0.8f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 72.0f, y = 0.8f, width = 4.5f, height = 0.5f),
            SpikeHazard(x = 92.0f, y = 0.8f, width = 4.0f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 23.0f, y = 7.5f),
            TreasureChest(x = 62.0f, y = 9.1f),
            TreasureChest(x = 89.0f, y = 7.5f),
            TreasureChest(x = 108.0f, y = 8.1f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 22.0f,
                y = 7.0f,
                tabletId = "TABLET_7",
                title = "Tablet VII: Tebing Angkasa Retak",
                text = "Di luar retakan menara ini, gravitasi melemah drastis. Ruang dan waktu dirobek oleh kekuatan Tinta Primordial. Lompatlah dengan penuh keyakinan di antara pulau-pulau melayang!"
            )
        )

        val enemies = listOf(
            Enemy(spawnX = 13.0f, spawnY = 5.3f, type = EnemyType.SHIELDED),
            Enemy(spawnX = 23.0f, spawnY = 7.5f, type = EnemyType.RANGED),
            Enemy(spawnX = 35.0f, spawnY = 6.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 51.0f, spawnY = 6.5f, type = EnemyType.MELEE),
            Enemy(spawnX = 67.0f, spawnY = 7.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 89.0f, spawnY = 7.5f, type = EnemyType.RANGED),
            Enemy(spawnX = 99.0f, spawnY = 6.0f, type = EnemyType.SHIELDED)
        )

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 1.0f,
                triggerY = 1.5f,
                triggerRadius = 3.0f,
                lines = listOf(
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Dinding menara di lantai ini hancur total... Rasakan ringannya lompatanmu! Gravitasi di sini hanya 65% dari biasanya. Gunakan itu untuk melayang melintasi jurang tak berdasar!",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 7,
            floorName = "Lantai 7: The Fractured Precipice",
            mapBounds = Rectangle(-10f, -1f, 135f, 22f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 50.0f, y = 6.0f),
            exitPortal = StageExit(x = 109.0f, y = 7.5f),
            isBossFloor = false,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE FRACTURED PRECIPICE",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            bloodAltar = BloodAltar(x = 60.0f, y = 8.5f),
            gravityScale = 0.65f
        )
    }

    private fun buildFloor8(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val baseColor = Color(0.14f, 0.16f, 0.22f, 1f)
        val wallColor = Color(0.10f, 0.11f, 0.16f, 1f)
        val platColor = Color(0.22f, 0.26f, 0.36f, 1f)

        // Balairung Gema Bayangan Bentang Luas (130m) dengan Runic Barrier ke-2 di x = 94m
        platforms.add(Platform(x = -10f, y = 0f, width = 140f, height = 1f, color = baseColor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 20f, color = wallColor))
        platforms.add(Platform(x = 129f, y = 1f, width = 1f, height = 20f, color = wallColor))

        // Koridor Akrobatik & Menara Pengamat Menuju Kunci Runik
        platforms.add(Platform(x = 4.0f, y = 2.4f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 12.0f, y = 4.2f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 21.0f, y = 6.2f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 30.0f, y = 4.2f, width = 5.0f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 38.0f, y = 3.2f, width = 5.5f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 47.0f, y = 5.2f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 56.5f, y = 7.2f, width = 6.0f, height = 0.5f, color = platColor))

        // Menara Puncak Kunci Runik Lantai 8 (x = 72m, y = 9.5m)
        platforms.add(Platform(x = 68.0f, y = 9.0f, width = 8.5f, height = 0.6f, color = Color(0.36f, 0.20f, 0.32f, 1f)))
        platforms.add(Platform(x = 68.0f, y = 4.0f, width = 6.0f, height = 0.35f, isOneWay = true))

        // Koridor Lanjut Menuju Runic Barrier di x = 94m
        platforms.add(Platform(x = 80.0f, y = 4.5f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 88.0f, y = 3.0f, width = 5.5f, height = 0.5f, color = platColor))

        // Area Pasca Barrier Menuju Gerbang Menara
        platforms.add(Platform(x = 100.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = platColor))
        platforms.add(Platform(x = 109.0f, y = 5.5f, width = 6.5f, height = 0.5f, color = platColor))

        val spikes = listOf(
            SpikeHazard(x = 15.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 42.0f, y = 1.0f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 82.0f, y = 1.0f, width = 4.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 22.5f, y = 6.7f),
            TreasureChest(x = 74.0f, y = 9.6f), // Hadiah di samping Runic Key
            TreasureChest(x = 112.0f, y = 6.0f)
        )

        val enemies = listOf(
            Enemy(spawnX = 14.0f, spawnY = 4.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 23.0f, spawnY = 6.7f, type = EnemyType.RANGED),
            Enemy(spawnX = 49.0f, spawnY = 5.7f, type = EnemyType.SHIELDED),
            Enemy(spawnX = 70.0f, spawnY = 9.6f, type = EnemyType.RANGED), // Penjaga Kunci
            Enemy(spawnX = 73.0f, spawnY = 9.6f, type = EnemyType.SHIELDED), // Penjaga Kunci
            Enemy(spawnX = 82.0f, spawnY = 5.0f, type = EnemyType.FLYING),
            Enemy(spawnX = 102.0f, spawnY = 4.0f, type = EnemyType.MELEE)
        )

        val barrierId = "BARRIER_FLOOR_8"
        val runicBarriers = listOf(
            RunicBarrier(x = 94.0f, y = 1.0f, width = 0.8f, height = 7.0f, barrierId = barrierId)
        )
        val runicKeys = listOf(
            RunicKey(x = 72.0f, y = 9.6f, targetBarrierId = barrierId)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 21.0f,
                y = 6.2f,
                tabletId = "TABLET_8",
                title = "Tablet VIII: Gerbang Segel Gema",
                text = "Balairung ini dijaga oleh Segel Runik Gema. Ratu laba-laba di atas telah memerintahkan para pelindungnya mengunci jalan masuk menuju sarang intinya. Panjatlah menara cermin di tengah untuk merampas Kunci Runik ungu!"
            )
        )

        return TowerFloor(
            floorNumber = 8,
            floorName = "Lantai 8: The Hall of Echoes",
            mapBounds = Rectangle(-10f, -1f, 140f, 20f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 50.0f, y = 1.0f),
            exitPortal = StageExit(x = 118.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE HALL OF ECHOES",
            loreTablets = loreTablets,
            storyDialogues = emptyList(),
            runicBarriers = runicBarriers,
            runicKeys = runicKeys
        )
    }

    private fun buildFloor9(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val violetFloor = Color(0.24f, 0.12f, 0.28f, 1f)
        val violetWall = Color(0.16f, 0.08f, 0.20f, 1f)
        val webPlatform = Color(0.35f, 0.20f, 0.42f, 1f)

        // Koridor Sarang Sutra Kristal Amethyst (Bentang Luas 120m)
        platforms.add(Platform(x = -10f, y = 0f, width = 130f, height = 1f, color = violetFloor))
        platforms.add(Platform(x = -10f, y = 1f, width = 1f, height = 20f, color = violetWall))
        platforms.add(Platform(x = 119f, y = 1f, width = 1f, height = 20f, color = violetWall))

        // Jaring Sutra Gantung & Platform Kristal Amethyst
        platforms.add(Platform(x = 4.0f, y = 2.5f, width = 5.0f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 12.0f, y = 4.5f, width = 5.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 20.5f, y = 6.5f, width = 6.0f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 29.5f, y = 4.2f, width = 5.0f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 38.0f, y = 3.5f, width = 5.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 47.0f, y = 5.8f, width = 6.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 56.5f, y = 8.0f, width = 6.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 66.0f, y = 5.0f, width = 5.0f, height = 0.35f, isOneWay = true))

        platforms.add(Platform(x = 74.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 83.0f, y = 5.8f, width = 6.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 92.5f, y = 7.8f, width = 6.5f, height = 0.5f, color = webPlatform))
        platforms.add(Platform(x = 102.0f, y = 4.5f, width = 5.5f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 15.0f, y = 1.0f, width = 3.5f, height = 0.5f),
            SpikeHazard(x = 41.0f, y = 1.0f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 76.0f, y = 1.0f, width = 4.0f, height = 0.5f),
            SpikeHazard(x = 95.0f, y = 1.0f, width = 4.0f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 22.0f, y = 7.0f),
            TreasureChest(x = 58.0f, y = 8.5f),
            TreasureChest(x = 94.0f, y = 8.3f)
        )

        val enemies = listOf(
            Enemy(spawnX = 13.0f, spawnY = 5.0f, type = EnemyType.MELEE),
            Enemy(spawnX = 22.0f, spawnY = 7.0f, type = EnemyType.RANGED),
            Enemy(spawnX = 35.0f, spawnY = 6.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 49.0f, spawnY = 6.3f, type = EnemyType.SHIELDED),
            Enemy(spawnX = 58.0f, spawnY = 8.5f, type = EnemyType.RANGED),
            Enemy(spawnX = 72.0f, spawnY = 6.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 85.0f, spawnY = 6.3f, type = EnemyType.RANGED),
            Enemy(spawnX = 94.0f, spawnY = 8.3f, type = EnemyType.SHIELDED)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 20.0f,
                y = 6.5f,
                tabletId = "TABLET_9",
                title = "Tablet IX: Peringatan Sarang Sutra",
                text = "Sarang Lady Vespera tepat di atas kita. Udara di sini berbau racun miasma amethyst dan getaran benang sutra. Kabarnya seorang penjelajah bertopeng bayangan terperangkap dalam kepompong kristal di aula utama..."
            )
        )

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 2.0f,
                triggerY = 1.5f,
                triggerRadius = 3.0f,
                lines = listOf(
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Udara di koridor ini sangat dingin dan dipenuhi jaring sutra ungu... Aku merasakan detak jiwa seseorang di lantai 10! Cepatlah, sebelum kepompong kristal itu menelan jiwanya seutuhnya!",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 9,
            floorName = "Lantai 9: The Weavers' Passage",
            mapBounds = Rectangle(-10f, -1f, 130f, 20f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 52.0f, y = 1.0f),
            exitPortal = StageExit(x = 108.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE WEAVERS' PASSAGE",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor10(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val violetFloor = Color(0.20f, 0.10f, 0.26f, 1f)
        val violetWall = Color(0.14f, 0.06f, 0.18f, 1f)
        val crystalMat = Color(0.32f, 0.16f, 0.40f, 1f)

        // The Violet Sanctum — Arena Bos Lady Vespera & Kepompong Zephyr (Bentang Luas 85m)
        platforms.add(Platform(x = -10f, y = 0f, width = 95f, height = 1.2f, color = violetFloor))
        platforms.add(Platform(x = -10f, y = 1.2f, width = 1.2f, height = 22f, color = violetWall))
        platforms.add(Platform(x = 84f, y = 1.2f, width = 1.2f, height = 22f, color = violetWall))

        // Platform Tumpuan Tempur Menghindari Volley Panah & Kabut Miasma Vespera
        platforms.add(Platform(x = 6.0f, y = 3.5f, width = 6.0f, height = 0.5f, color = crystalMat))
        platforms.add(Platform(x = 18.0f, y = 5.5f, width = 6.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 30.0f, y = 4.5f, width = 8.0f, height = 0.5f, color = crystalMat)) // Platform Tumpuan di atas Kepompong Zephyr
        platforms.add(Platform(x = 44.0f, y = 6.5f, width = 7.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 58.0f, y = 4.0f, width = 7.0f, height = 0.5f, color = crystalMat))
        platforms.add(Platform(x = 70.0f, y = 5.8f, width = 6.0f, height = 0.4f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 2.0f, y = 1.2f),
            TreasureChest(x = 76.0f, y = 1.2f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 4.0f,
                y = 1.2f,
                tabletId = "TABLET_10",
                title = "Tablet X: Ambang Sarang Ratu Sutra",
                text = "Lady Vespera mengendalikan seluruh laba-laba dan panah bayangan di Ashen Depths. Di tengah ruangan ini terdapat kepompong kristal sutra tebal tempat ia memerangkap Zephyr. Hancurkan kepompong itu (3 tebasan pedang) untuk membebaskan Zephyr dan bertarung bersama sebagai Tag-Team!"
            )
        )

        val boss = BossEnemy(spawnX = 52.0f, spawnY = 1.5f, type = BossType.AMETHYST_MATRON)
        val zephyrCocoon = ZephyrCocoon(x = 34.0f, y = 1.2f, width = 1.6f, height = 2.4f)
        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(10))

        return TowerFloor(
            floorNumber = 10,
            floorName = "Lantai 10: The Violet Sanctum",
            mapBounds = Rectangle(-10f, -1f, 95f, 22f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 2.0f, y = 1.2f),
            exitPortal = StageExit(x = 76.0f, y = 1.2f),
            isBossFloor = true,
            actTitle = "SAGA I: THE ASHEN DEPTHS",
            actSubtitle = "THE VIOLET SANCTUM",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            zephyrCocoon = zephyrCocoon
        )
    }

    // =========================================================================
    // POST-GAME: PARADOKS ENDLESS SPIRE (LANTAI 11 KE ATAS)
    // =========================================================================

    fun buildEndlessFloor(floorNumber: Int, mutator: FloorMutator): TowerFloor {
        val endlessRank = (floorNumber - 10).coerceAtLeast(1)
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

            // Rotasi Bos Menara di Endless Spire
            val bossType = when ((floorNumber / 5) % 5) {
                1 -> BossType.AMETHYST_MATRON
                2 -> BossType.SHADOW_DOPPELGANGER
                3 -> BossType.CRIMSON_SOVEREIGN
                4 -> BossType.GRAND_CALLIGRAPHER
                else -> BossType.TOWER_GUARDIAN
            }

            val boss = BossEnemy(spawnX = 18.0f, spawnY = 1.5f, type = bossType)
            boss.health.setMaxHp(boss.health.maxHp * hpScale)
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
                isBossFloor = true,
                actTitle = "EPILOGUE: THE ENDLESS PARADOX",
                actSubtitle = "FLOOR $floorNumber",
                environmentalProps = getEnvironmentalPropsForFloor(floorNumber)
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

        // Spawn scaled enemies across all 4 archetypes
        enemies.add(Enemy(spawnX = 5.0f, spawnY = 1.2f, type = EnemyType.SHIELDED))
        enemies.add(Enemy(spawnX = 11.0f, spawnY = 5.0f, type = EnemyType.RANGED))
        enemies.add(Enemy(spawnX = 17.0f, spawnY = 6.0f, type = EnemyType.FLYING))
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
            isBossFloor = false,
            actTitle = "EPILOGUE: THE ENDLESS PARADOX",
            actSubtitle = "FLOOR $floorNumber",
            environmentalProps = getEnvironmentalPropsForFloor(floorNumber),
            bloodAltar = if (floorNumber % 3 == 0) BloodAltar(x = 13.5f, y = 1.0f) else null
        )
    }
}

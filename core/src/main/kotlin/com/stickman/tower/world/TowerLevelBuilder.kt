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
                EnvironmentalProp(x = 6.0f, y = 1.0f, width = 0.9f, height = 2.0f, type = PropType.LEAKING_ALCHEMY_VAT),
                EnvironmentalProp(x = 21.0f, y = 1.0f, width = 0.9f, height = 2.0f, type = PropType.LEAKING_ALCHEMY_VAT),
                EnvironmentalProp(x = 13.0f, y = 4.5f, width = 1.2f, height = 2.0f, type = PropType.SHATTERED_BOOKSHELF)
            )
            6 -> listOf(
                EnvironmentalProp(x = 9.2f, y = 1.0f, width = 1.6f, height = 1.5f, type = PropType.TITAN_ANVIL),
                EnvironmentalProp(x = 16.0f, y = 1.0f, width = 1.2f, height = 1.8f, type = PropType.SHATTERED_BOOKSHELF),
                EnvironmentalProp(x = 23.0f, y = 1.0f, width = 0.8f, height = 1.6f, type = PropType.LEAKING_ALCHEMY_VAT)
            )
            7 -> listOf(
                EnvironmentalProp(x = 8.0f, y = 5.0f, width = 1.8f, height = 2.6f, type = PropType.VOID_WALL_CRACK),
                EnvironmentalProp(x = 20.0f, y = 7.0f, width = 2.0f, height = 3.0f, type = PropType.VOID_WALL_CRACK)
            )
            8 -> listOf(
                EnvironmentalProp(x = 12.0f, y = 4.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK),
                EnvironmentalProp(x = 5.0f, y = 1.0f, width = 1.4f, height = 2.0f, type = PropType.BLOOD_ECLIPSE_ALTAR)
            )
            9 -> listOf(
                EnvironmentalProp(x = 14.0f, y = 1.0f, width = 1.6f, height = 2.2f, type = PropType.BLOOD_ECLIPSE_ALTAR),
                EnvironmentalProp(x = 7.0f, y = 5.0f, width = 1.8f, height = 2.5f, type = PropType.VOID_WALL_CRACK)
            )
            10 -> listOf(
                EnvironmentalProp(x = 2.0f, y = 1.0f, width = 1.0f, height = 4.5f, type = PropType.PURE_CANVAS_PILLAR),
                EnvironmentalProp(x = 26.0f, y = 1.0f, width = 1.0f, height = 4.5f, type = PropType.PURE_CANVAS_PILLAR),
                EnvironmentalProp(x = 14.0f, y = 4.5f, width = 1.2f, height = 3.2f, type = PropType.FROZEN_GOLDEN_BRUSH)
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
            SpikeHazard(x = 8.5f, y = 1.0f, width = 2.0f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 15.5f, y = 5.9f)
        )

        val enemies = listOf(
            Enemy(spawnX = 4.0f, spawnY = 2.7f, type = EnemyType.MELEE),
            Enemy(spawnX = 15.5f, spawnY = 5.9f, type = EnemyType.RANGED),
            Enemy(spawnX = 23.0f, spawnY = 1.2f, type = EnemyType.MELEE)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 14.0f,
                y = 5.4f,
                tabletId = "TABLET_1",
                title = "Tablet I: Catatan Hari ke-47",
                text = "Hari ke-47. Kami tiba di gerbang bawah dengan 100 prajurit terbaik. Tubuh kami telah menghitam, jari-jari menyatu menjadi garis padat. Makanan tak lagi berasa, namun pedang terasa seratus kali lebih ringan. Jika catatan ini kau temukan, ketahuilah: jangan pernah berhenti bergerak saat berada di atas platform."
            )
        )

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(1))

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
            isBossFloor = false,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "LOWER BASTION",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
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

        val loreTablets = listOf(
            LoreTablet(
                x = 12.0f,
                y = 5.6f,
                tabletId = "TABLET_2",
                title = "Tablet II: Catatan Para Pendaki",
                text = "Kutukan gravitasi ini bukan dari bumi, melainkan dari langit. Para dewa tidak ingin kita terbang. Mereka memasang duri-duri obsidian ini agar darah kita kembali menyuburkan tanah. Jangan pernah ragu saat melangkah: hanya mereka yang terus bergerak yang sampai ke puncak."
            )
        )

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 2.0f,
                triggerY = 1.0f,
                triggerRadius = 3.0f,
                lines = listOf(
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Batu obsidian di tebing ini tajam dan tak berbelas kasihan. Teruslah melompat ke atas, jangan biarkan gravitasi menara menarik jiwamu ke jurang kehampaan!",
                        speakerColor = Color.CYAN
                    )
                )
            )
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
            isBossFloor = false,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "SPIRE OF HAZARDS",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
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

        val loreTablets = listOf(
            LoreTablet(
                x = 1.8f,
                y = 1.2f,
                tabletId = "TABLET_3",
                title = "Tablet III: Gerbang Hakim Oakhaven",
                text = "Jenderal Oakhaven berdiri di sini bukan untuk menghalangi kita mencari kebenaran, melainkan menguji apakah jiwa kita cukup tegar menanggung kepedihan di lantai atas. Hormatilah bilah pedangnya."
            )
        )

        val boss = BossEnemy(spawnX = 17.0f, spawnY = 1.5f, type = BossType.TOWER_GUARDIAN)

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(3))

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
            isBossFloor = true,
            actTitle = "ACT I: THE INITIATION",
            actSubtitle = "APEX BOSS ARENA",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    // =========================================================================
    // BABAK II: RAHASIA TINTA & LOGAM ABADI (ACT II: SECRETS OF THE INK — LANTAI 4 S/D 6)
    // =========================================================================

    private fun buildFloor4(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Perpustakaan Kaca Kristal Amethyst
        val amethystFloor = Color(0.20f, 0.15f, 0.28f, 1f)
        val amethystWall = Color(0.14f, 0.10f, 0.22f, 1f)
        val crystalPlatform = Color(0.26f, 0.20f, 0.38f, 1f)

        platforms.add(Platform(x = -8f, y = 0f, width = 44f, height = 1f, color = amethystFloor))
        platforms.add(Platform(x = -8f, y = 1f, width = 1f, height = 16f, color = amethystWall))
        platforms.add(Platform(x = 35f, y = 1f, width = 1f, height = 16f, color = amethystWall))

        // Meja-meja Arsip Kuno & Tangga Kaca Kristal
        platforms.add(Platform(x = 2.0f, y = 2.4f, width = 4.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 8.5f, y = 4.0f, width = 4.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 14.0f, y = 5.6f, width = 5.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 21.0f, y = 4.0f, width = 4.5f, height = 0.5f, color = crystalPlatform))
        platforms.add(Platform(x = 27.0f, y = 5.8f, width = 4.5f, height = 0.35f, isOneWay = true))

        // Tangga Penyelamat
        platforms.add(Platform(x = 8.5f, y = 2.2f, width = 4.0f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 21.0f, y = 2.2f, width = 4.5f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 6.0f, y = 1.0f, width = 2.5f, height = 0.5f),
            SpikeHazard(x = 18.5f, y = 1.0f, width = 2.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 16.5f, y = 6.1f),
            TreasureChest(x = 29.0f, y = 6.3f)
        )

        val enemies = listOf(
            Enemy(spawnX = 4.0f, spawnY = 2.9f, type = EnemyType.MELEE),
            Enemy(spawnX = 15.0f, spawnY = 6.1f, type = EnemyType.RANGED),
            Enemy(spawnX = 22.0f, spawnY = 6.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 28.5f, spawnY = 6.3f, type = EnemyType.RANGED)
        )


        val loreTablets = listOf(
            LoreTablet(
                x = 14.2f,
                y = 5.6f,
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
            mapBounds = Rectangle(-8f, -1f, 44f, 16f),
            spawnX = -1f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 14.0f, y = 1.0f),
            exitPortal = StageExit(x = 32.0f, y = 1.0f),
            isBossFloor = false,
            actTitle = "ACT II: SECRETS OF THE INK",
            actSubtitle = "THE SUNKEN ARCHIVES",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor5(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Kawah Alkimia Anomali — Arena Bos Sang Ratu Busur Bayang
        val vatFloor = Color(0.18f, 0.12f, 0.24f, 1f)
        val vatWall = Color(0.12f, 0.08f, 0.18f, 1f)

        platforms.add(Platform(x = -5f, y = 0f, width = 36f, height = 1.2f, color = vatFloor))
        platforms.add(Platform(x = -5f, y = 1.2f, width = 1.2f, height = 15f, color = vatWall))
        platforms.add(Platform(x = 30f, y = 1.2f, width = 1.2f, height = 15f, color = vatWall))

        // Platform Akrobatik Menghindari Volley Panah Amethyst
        platforms.add(Platform(x = 3.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 17.0f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 10.0f, y = 4.8f, width = 5.0f, height = 0.35f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 1.0f, y = 1.2f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 1.8f,
                y = 1.2f,
                tabletId = "TABLET_5",
                title = "Tablet V: Dinding Kawah Alkimia",
                text = "Racun Miasma ini tidak membunuh tubuh, melainkan melarutkan tekad. Jika kau menghirupnya terlalu lama, kau akan melupakan untuk apa kau mendaki menara ini."
            )
        )

        val boss = BossEnemy(spawnX = 18.0f, spawnY = 1.5f, type = BossType.AMETHYST_MATRON)

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(5))

        return TowerFloor(
            floorNumber = 5,
            floorName = "Lantai 5: Crucible of Mutations",
            mapBounds = Rectangle(-5f, -1f, 36f, 16f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 1.5f, y = 1.2f),
            exitPortal = StageExit(x = 27.0f, y = 1.2f),
            isBossFloor = true,
            actTitle = "ACT II: SECRETS OF THE INK",
            actSubtitle = "CRUCIBLE OF MUTATIONS",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor6(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Bengkel Tempa Logam Vulcanus (Zona Istirahat Aman / Safe Haven)
        val forgeFloor = Color(0.28f, 0.18f, 0.12f, 1f)
        val forgeWall = Color(0.18f, 0.10f, 0.08f, 1f)
        val anvilStone = Color(0.42f, 0.25f, 0.14f, 1f)

        platforms.add(Platform(x = -6f, y = 0f, width = 38f, height = 1.2f, color = forgeFloor))
        platforms.add(Platform(x = -6f, y = 1.2f, width = 1.2f, height = 15f, color = forgeWall))
        platforms.add(Platform(x = 31f, y = 1.2f, width = 1.2f, height = 15f, color = forgeWall))

        // Meja Tempa & Tungku Api Emas
        platforms.add(Platform(x = 8.0f, y = 1.2f, width = 7.0f, height = 0.8f, color = anvilStone))
        platforms.add(Platform(x = 2.0f, y = 3.5f, width = 4.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 19.0f, y = 3.5f, width = 5.0f, height = 0.4f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 3.5f, y = 1.2f),
            TreasureChest(x = 11.5f, y = 2.0f), // Master Blacksmith Relic Chest
            TreasureChest(x = 22.0f, y = 1.2f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 8.8f,
                y = 2.0f,
                tabletId = "TABLET_6",
                title = "Tablet VI: Wasiat Sang Penempa",
                text = "Besi meteorit ini adalah satu-satunya benda di semesta yang tidak mempan terhadap Tinta Primordial. Setiap koin emas adalah tetesan matahari para pendaki masa lalu. Selama apinya menyala, harapan manusia untuk merebut kembali warna takkan pernah padam!"
            )
        )

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(6))

        return TowerFloor(
            floorNumber = 6,
            floorName = "Lantai 6: The Iron Foundry",
            mapBounds = Rectangle(-6f, -1f, 38f, 16f),
            spawnX = -1f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = null,
            checkpoint = Checkpoint(x = 8.5f, y = 2.0f),
            exitPortal = StageExit(x = 27.5f, y = 1.2f),
            isBossFloor = false,
            actTitle = "ACT II: SECRETS OF THE INK",
            actSubtitle = "THE IRON FOUNDRY",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            isSafeZone = true
        )
    }

    // =========================================================================
    // BABAK III: PENDAKIAN ASTRAL & CERMIN JIWA (ACT III: THE ASTRAL CLIMB — LANTAI 7 S/D 9)
    // =========================================================================

    private fun buildFloor7(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        val astralColor = Color(0.16f, 0.22f, 0.36f, 1f)

        // Pulau Mengambang di Luar Angkasa Astral (Anomali Gravitasi Rendah)
        platforms.add(Platform(x = -4f, y = 1.0f, width = 6.0f, height = 1.0f, color = astralColor)) // Pulau Start
        platforms.add(Platform(x = 4.5f, y = 3.2f, width = 4.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 11.0f, y = 5.5f, width = 5.0f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 18.5f, y = 4.2f, width = 4.5f, height = 0.5f, color = astralColor))
        platforms.add(Platform(x = 25.0f, y = 6.2f, width = 6.5f, height = 0.6f, color = astralColor)) // Pulau Puncak Exit

        // Dek Pengaman Bawah Jurang & Jembatan Satu Arah
        platforms.add(Platform(x = 2.0f, y = 0.0f, width = 26.0f, height = 0.6f, color = Color(0.10f, 0.14f, 0.22f, 1f)))
        platforms.add(Platform(x = 11.5f, y = 2.5f, width = 4.0f, height = 0.35f, isOneWay = true))

        val spikes = listOf(
            SpikeHazard(x = 6.0f, y = 0.6f, width = 3.0f, height = 0.5f),
            SpikeHazard(x = 16.0f, y = 0.6f, width = 3.5f, height = 0.5f)
        )

        val chests = listOf(
            TreasureChest(x = 12.5f, y = 6.0f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 5.0f,
                y = 3.2f,
                tabletId = "TABLET_7",
                title = "Tablet VII: Tebing Angkasa Retak",
                text = "Di luar dinding ini tidak ada langit biru. Hanya ada jurang kosmis tanpa batas. Kita tidak sedang mendaki menara di atas tanah... kita sedang memanjat keluar dari penjara dimensi para dewa!"
            )
        )

        val enemies = listOf(
            Enemy(spawnX = 6.0f, spawnY = 3.7f, type = EnemyType.SHIELDED),
            Enemy(spawnX = 13.0f, spawnY = 6.0f, type = EnemyType.RANGED),
            Enemy(spawnX = 20.0f, spawnY = 6.5f, type = EnemyType.FLYING),
            Enemy(spawnX = 27.5f, spawnY = 6.8f, type = EnemyType.SHIELDED)
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
                        text = "Dinding menara ini telah hancur... Kita berada di tepi jurang kosmis! Gravitasi di sini melemah (-35%). Rasakan ringannya lompatanmu saat melayang di antara pulau-pulau obsidian!",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 7,
            floorName = "Lantai 7: The Fractured Precipice",
            mapBounds = Rectangle(-6f, -1f, 40f, 18f),
            spawnX = -1f,
            spawnY = 2.0f,
            platforms = platforms,
            spikes = spikes,
            chests = chests,
            enemies = enemies,
            boss = null,
            checkpoint = Checkpoint(x = 12.0f, y = 6.0f),
            exitPortal = StageExit(x = 29.0f, y = 6.8f),
            isBossFloor = false,
            actTitle = "ACT III: THE ASTRAL CLIMB",
            actSubtitle = "THE FRACTURED PRECIPICE",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues,
            gravityScale = 0.65f // Gravitasi Rendah (-35%) untuk Akrobatik Udara
        )
    }

    private fun buildFloor8(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Balairung Gema Bayangan — Duel Cermin Jiwa (Doppelgänger)
        val mirrorFloor = Color(0.12f, 0.14f, 0.20f, 1f)
        val mirrorWall = Color(0.08f, 0.09f, 0.14f, 1f)

        platforms.add(Platform(x = -5f, y = 0f, width = 36f, height = 1.2f, color = mirrorFloor))
        platforms.add(Platform(x = -5f, y = 1.2f, width = 1.2f, height = 15f, color = mirrorWall))
        platforms.add(Platform(x = 30f, y = 1.2f, width = 1.2f, height = 15f, color = mirrorWall))

        platforms.add(Platform(x = 3.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 17.0f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 10.0f, y = 4.8f, width = 5.0f, height = 0.35f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 1.0f, y = 1.2f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 10.5f,
                y = 4.8f,
                tabletId = "TABLET_8",
                title = "Tablet VIII: Paradoks Menara Abadi",
                text = "Musuh paling berbahaya di puncak menara bukanlah dewa, melainkan dirimu sendiri yang menyerah pada keputusasaan. Ketika kau melihat bayanganmu mengayunkan pedang yang sama, jangan menunduk. Lakukan parry tepat saat kilatan matanya menyala!"
            )
        )

        val boss = BossEnemy(spawnX = 18.0f, spawnY = 1.5f, type = BossType.SHADOW_DOPPELGANGER)

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 4.0f,
                triggerY = 1.2f,
                triggerRadius = 3.5f,
                lines = listOf(
                    DialogueLine(
                        speaker = "The Shadow Doppelgänger",
                        title = "Cermin Jiwa Gelap",
                        text = "Aku adalah cermin jiwamu... keraguanmu, ketakutanmu, dan ambisimu. Menara ini tahu setiap tebasan yang akan kau ayunkan. Kau takkan pernah bisa melampaui bayanganmu sendiri!",
                        speakerColor = Color(0.6f, 0.6f, 0.9f, 1f)
                    ),
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Jangan ragu! Baca prasasti di depan cermin: lakukan parry tepat saat kilatan matanya menyala!",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 8,
            floorName = "Lantai 8: The Hall of Echoes",
            mapBounds = Rectangle(-5f, -1f, 36f, 16f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 1.5f, y = 1.2f),
            exitPortal = StageExit(x = 27.0f, y = 1.2f),
            isBossFloor = true,
            actTitle = "ACT III: THE ASTRAL CLIMB",
            actSubtitle = "THE HALL OF ECHOES",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    private fun buildFloor9(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Kuil Gerhana Darah — Pertarungan Pangeran Belati Darah
        val bloodFloor = Color(0.28f, 0.08f, 0.12f, 1f)
        val bloodWall = Color(0.18f, 0.04f, 0.08f, 1f)

        platforms.add(Platform(x = -5f, y = 0f, width = 36f, height = 1.2f, color = bloodFloor))
        platforms.add(Platform(x = -5f, y = 1.2f, width = 1.2f, height = 15f, color = bloodWall))
        platforms.add(Platform(x = 30f, y = 1.2f, width = 1.2f, height = 15f, color = bloodWall))

        platforms.add(Platform(x = 3.5f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 17.0f, y = 2.9f, width = 4.5f, height = 0.35f, isOneWay = true))
        platforms.add(Platform(x = 10.0f, y = 4.8f, width = 5.0f, height = 0.35f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 1.0f, y = 1.2f)
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 1.8f,
                y = 1.2f,
                tabletId = "TABLET_9",
                title = "Tablet IX: Altar Kuil Gerhana",
                text = "Pangeran kami telah meminum tinta darah. Ia abadi, namun jiwanya telah membusuk. Kematian di ujung pedangmu adalah anugerah terindah yang bisa kau berikan padanya."
            )
        )

        val boss = BossEnemy(spawnX = 18.0f, spawnY = 1.5f, type = BossType.CRIMSON_SOVEREIGN)

        val storyDialogues = listOf(
            StoryDialogue(
                triggerX = 4.0f,
                triggerY = 1.2f,
                triggerRadius = 3.5f,
                lines = listOf(
                    DialogueLine(
                        speaker = "The Crimson Sovereign",
                        title = "Pangeran Belati Darah",
                        text = "Darah para pendaki adalah nektar keabadian! Selama gerhana vampirik ini menyala di langit menara, setiap tetes lukamu akan memulihkan kekuatanku! Bersujudlah di hadapan Pangeran Belati Darah!",
                        speakerColor = Color(1f, 0.2f, 0.2f, 1f)
                    ),
                    DialogueLine(
                        speaker = "Aria",
                        title = "Sang Gema Warna Terakhir",
                        text = "Ksatria ini telah menyerahkan kewarasannya demi keabadian darah. Kalahkan dia untuk membuka portal tangga surga menuju Puncak Menara!",
                        speakerColor = Color.CYAN
                    )
                )
            )
        )

        return TowerFloor(
            floorNumber = 9,
            floorName = "Lantai 9: The Blood Moon Sanctum",
            mapBounds = Rectangle(-5f, -1f, 36f, 16f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 1.5f, y = 1.2f),
            exitPortal = StageExit(x = 27.0f, y = 1.2f),
            isBossFloor = true,
            actTitle = "ACT III: THE ASTRAL CLIMB",
            actSubtitle = "THE BLOOD MOON SANCTUM",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
        )
    }

    // =========================================================================
    // BABAK IV: PUNCAK KANVAS TAKDIR (ACT IV: THE PRIMORDIAL CANVAS — LANTAI 10)
    // =========================================================================

    private fun buildFloor10(): TowerFloor {
        val platforms = mutableListOf<Platform>()
        // Platform Marmer Putih Primordial di Atas Samudra Tinta Hitam Kosmik
        val primordialMarble = Color(0.88f, 0.90f, 0.96f, 1f)
        val divinePillar = Color(0.70f, 0.75f, 0.86f, 1f)

        platforms.add(Platform(x = -6f, y = 0f, width = 38f, height = 1.3f, color = primordialMarble))
        platforms.add(Platform(x = -6f, y = 1.3f, width = 1.3f, height = 16f, color = divinePillar))
        platforms.add(Platform(x = 31f, y = 1.3f, width = 1.3f, height = 16f, color = divinePillar))

        // Altar Langit
        platforms.add(Platform(x = 2.5f, y = 3.0f, width = 4.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 18.0f, y = 3.0f, width = 4.5f, height = 0.4f, isOneWay = true))
        platforms.add(Platform(x = 10.0f, y = 5.2f, width = 5.5f, height = 0.4f, isOneWay = true))

        val chests = listOf(
            TreasureChest(x = 1.0f, y = 1.3f) // Peti Relik Primordial
        )

        val loreTablets = listOf(
            LoreTablet(
                x = 2.0f,
                y = 1.3f,
                tabletId = "TABLET_10",
                title = "Tablet X: Ambang Kanvas Takdir",
                text = "Jika kau membaca tulisan ini, berarti kau telah tiba di hadapan Sang Penulis Takdir. Ingatlah satu hal: pena mereka memang menuliskan takdir kita di masa lalu, tapi pedang kita yang akan menentukan masa depan!"
            )
        )

        val boss = BossEnemy(spawnX = 18.5f, spawnY = 1.5f, type = BossType.GRAND_CALLIGRAPHER)

        val storyDialogues = listOfNotNull(DialogueRegistry.getSceneForFloor(10))

        return TowerFloor(
            floorNumber = 10,
            floorName = "Lantai 10: The Primordial Canvas",
            mapBounds = Rectangle(-6f, -1f, 38f, 17f),
            spawnX = 0f,
            spawnY = 1.5f,
            platforms = platforms,
            spikes = emptyList(),
            chests = chests,
            enemies = emptyList(),
            boss = boss,
            checkpoint = Checkpoint(x = 1.5f, y = 1.3f),
            exitPortal = StageExit(x = 28.5f, y = 1.3f),
            isBossFloor = true,
            actTitle = "ACT IV: THE PRIMORDIAL CANVAS",
            actSubtitle = "SUMMIT OF REALITY",
            loreTablets = loreTablets,
            storyDialogues = storyDialogues
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
            environmentalProps = getEnvironmentalPropsForFloor(floorNumber)
        )
    }
}

package com.stickman.tower.narrative

import com.badlogic.gdx.graphics.Color
import com.stickman.tower.world.DialogueBranchOption
import com.stickman.tower.world.DialogueLine
import com.stickman.tower.world.MoraleBuffType
import com.stickman.tower.world.StoryDialogue

/**
 * Registri resmi naskah lakon sinematik standar industri (Cinematic Screenplay Scenes 01 - 05).
 * Memuat dialog karakter dengan palet warna diegetik, arahan vokal prosedural, dan percabangan moril.
 */
object DialogueRegistry {

    // Palet Warna Suara Diegetik Karakter
    val COLOR_KAELEN = Color(0.95f, 0.95f, 1.0f, 1f)         // Putih Berkilau (Hero Pendaki)
    val COLOR_ARIA = Color(0.35f, 0.85f, 1.0f, 1f)           // Biru Cyan Murni (Radiant Sprite)
    val COLOR_OAKHAVEN = Color(0.55f, 0.85f, 0.45f, 1f)       // Hijau Hutan Lumut (Jenderal Kehormatan)
    val COLOR_MATRON = Color(0.85f, 0.45f, 0.95f, 1f)         // Ungu Kristal Miasma (The Amethyst Matron)
    val COLOR_VULCANUS = Color(1.0f, 0.65f, 0.25f, 1f)        // Oranye Bara Meteorit (Sang Penempa Jiwa)
    val COLOR_CALLIGRAPHER = Color(0.95f, 0.82f, 0.35f, 1f)   // Emas Primordial Gelap (Grand Calligrapher)

    /**
     * SCENE 01: Prolog Gerbang Bawah (Floor 1)
     */
    val SCENE_01_PROLOGUE = StoryDialogue(
        triggerX = 2.0f,
        triggerY = 1.0f,
        triggerRadius = 3.0f,
        lines = listOf(
            DialogueLine(
                speaker = "Aria",
                title = "Peri Cahaya Menara",
                text = "Bangunlah, Pendaki Garis! Tubuhmu... seluruh warnamu telah dihisap oleh kutukan Tinta Primordial!",
                speakerColor = COLOR_ARIA
            ),
            DialogueLine(
                speaker = "Kaelen",
                title = "Pendaki Jiwa Kosong",
                text = "Tanganku... hanya goresan tinta hitam di atas kanvas mati ini. Di mana warna dunia dan ingatan kita?",
                speakerColor = COLOR_KAELEN
            ),
            DialogueLine(
                speaker = "Aria",
                title = "Peri Cahaya Menara",
                text = "Sang Kaligrafer Agung telah menyegelnya di puncak menara. Cabut bilah pedangmu, Kaelen! Tiada jalan pulang selain menembus lantai demi lantai!",
                speakerColor = COLOR_ARIA
            )
        )
    )

    /**
     * SCENE 02: Duel Kehormatan Jenderal Oakhaven (Floor 3)
     */
    val SCENE_02_OAKHAVEN = StoryDialogue(
        triggerX = 4.0f,
        triggerY = 1.2f,
        triggerRadius = 3.5f,
        lines = listOf(
            DialogueLine(
                speaker = "Jenderal Oakhaven",
                title = "Panglima Benteng Kayu",
                text = "Berhenti, jiwa yang tak berkilau. Aku Jenderal Oakhaven, pengawal Benteng Bawah. Pedangku bersumpah pada keabadian ordo hitam-putih.",
                speakerColor = COLOR_OAKHAVEN
            ),
            DialogueLine(
                speaker = "Kaelen",
                title = "Pendaki Jiwa",
                text = "Bebaskan jalan, Jenderal! Kami mendaki bukan untuk menodai kehormatanmu, melainkan memulihkan warna semesta yang dirampas!",
                speakerColor = COLOR_KAELEN
            ),
            DialogueLine(
                speaker = "Jenderal Oakhaven",
                title = "Panglima Benteng Kayu",
                text = "Kata-kata indah tak bernilai di hadapan tebasan baja. Tunjukkan bahwa kehendakmu lebih keras daripada hukum Menara!",
                speakerColor = COLOR_OAKHAVEN
            )
        )
    )

    /**
     * SCENE 03: Kawah Alkimia The Amethyst Matron (Floor 5)
     */
    val SCENE_03_MATRON = StoryDialogue(
        triggerX = 4.0f,
        triggerY = 1.2f,
        triggerRadius = 3.5f,
        lines = listOf(
            DialogueLine(
                speaker = "The Amethyst Matron",
                title = "Ratu Sarang Miasma",
                text = "Ssshh... mangsa baru merayap ke sarang ungu kami. Apakah kau mencium getah manis yang kan melarutkan garis ragamu?",
                speakerColor = COLOR_MATRON
            ),
            DialogueLine(
                speaker = "Aria",
                title = "Peri Cahaya",
                text = "Tutup telingamu dari desisnya, Kaelen! Panah kristal ungunya membius dan meracuni memori para pejuang masa lalu!",
                speakerColor = COLOR_ARIA
            ),
            DialogueLine(
                speaker = "The Amethyst Matron",
                title = "Ratu Sarang Miasma",
                text = "Bahkan busur kristalku yang retak ini masih sanggup menenun keputusasaanmu menjadi kepompong tinta abadi!",
                speakerColor = COLOR_MATRON
            )
        )
    )

    /**
     * SCENE 04: Suaka Paron Vulcanus (Floor 6 - Interactive Branching)
     */
    val SCENE_04_VULCANUS = StoryDialogue(
        triggerX = 10.5f,
        triggerY = 1.5f,
        triggerRadius = 2.8f,
        lines = listOf(
            DialogueLine(
                speaker = "Vulcanus",
                title = "Sang Penempa Jiwa",
                text = "Denting paluku adalah satu-satunya irama di menara ini yang menolak tunduk pada keheningan Tinta Primordial.",
                speakerColor = COLOR_VULCANUS
            ),
            DialogueLine(
                speaker = "Vulcanus",
                title = "Sang Penempa Jiwa",
                text = "Koin emasmu adalah tetesan matahari purba. Namun sebelum kau melangkah ke tebing kosmis di atas, katakan padaku: Apa yang menggerakkan bilah pedangmu?",
                speakerColor = COLOR_VULCANUS,
                branchOptions = listOf(
                    DialogueBranchOption(
                        choiceId = "CHOICE_VENGEANCE",
                        promptText = "[1] Pembalasan Dendam (Hancurkan Tirani Kaligrafer)",
                        reactionSpeaker = "Vulcanus",
                        reactionTitle = "Sang Penempa Jiwa",
                        reactionText = "Bagus! Kemurkaan adalah bahan bakar terbaik untuk meleburkan baja terkeras. Terimalah Kinetic Surge: tebasanmu kan bergetar melipatgandakan daya hancur (+8% ATK)!",
                        moraleBuff = MoraleBuffType.KINETIC_SURGE
                    ),
                    DialogueBranchOption(
                        choiceId = "CHOICE_RESTORATION",
                        promptText = "[2] Restorasi Warna (Pulihkan Kedamaian Aethelgard)",
                        reactionSpeaker = "Vulcanus",
                        reactionTitle = "Sang Penempa Jiwa",
                        reactionText = "Hati yang luhur adalah perisai paling kokoh. Terimalah Spiritual Ward: hangatnya bara api kan memulihkan darahmu dari racun jurang (+15% HP & Regen)!",
                        moraleBuff = MoraleBuffType.SPIRITUAL_WARD
                    )
                )
            ),
            DialogueLine(
                speaker = "Vulcanus",
                title = "Sang Penempa Jiwa",
                text = "Bawalah berkat penempaanku ke jurang kosmis di Lantai 7. Jangan biarkan bara api ini padam sebelum menara runtuh!",
                speakerColor = COLOR_VULCANUS
            )
        )
    )

    /**
     * SCENE 05: Singgasana Kanvas Primordial (Floor 10)
     */
    val SCENE_05_CALLIGRAPHER = StoryDialogue(
        triggerX = 4.0f,
        triggerY = 1.3f,
        triggerRadius = 3.5f,
        lines = listOf(
            DialogueLine(
                speaker = "The Grand Calligrapher",
                title = "Arsitek Tinta Primordial",
                text = "Kalian akhirnya tiba di tepi kenyataan. Menatap kanvas kosong tempat seluruh hukum dan takdir semesta digoreskan.",
                speakerColor = COLOR_CALLIGRAPHER
            ),
            DialogueLine(
                speaker = "Kaelen",
                title = "Pembebas Warna",
                text = "Kau memenjarakan semesta dalam monokrom kaku! Hidup bukanlah naskah mati yang boleh kau hapus sesukamu!",
                speakerColor = COLOR_KAELEN
            ),
            DialogueLine(
                speaker = "The Grand Calligrapher",
                title = "Arsitek Tinta Primordial",
                text = "Warna adalah kekacauan. Hitam dan putih adalah kesempurnaan abadi. Jika kau ingin menggambar ulang takdir... patahkan penaku dengan darahmu!",
                speakerColor = COLOR_CALLIGRAPHER
            )
        )
    )

    fun getSceneForFloor(floor: Int): StoryDialogue? {
        val scene = when (floor) {
            1 -> SCENE_01_PROLOGUE
            3 -> SCENE_02_OAKHAVEN
            5 -> SCENE_03_MATRON
            6 -> SCENE_04_VULCANUS
            10 -> SCENE_05_CALLIGRAPHER
            else -> null
        }
        scene?.reset()
        return scene
    }
}

package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.stickman.tower.StickmanGame
import com.stickman.tower.audio.AudioManager
import com.stickman.tower.audio.HapticManager
import com.stickman.tower.ui.FontManager
import kotlin.math.sin

/**
 * Screen Interaktif Pembaca Kisah & Worldbuilding (Story Codex / Tomes of Aethelgard).
 * Terbuka setelah pemain berhasil menamatkan Story Tower (mengalahkan The Grand Calligrapher di Lantai 10).
 * Menampilkan 9 bab epik dengan navigasi bab, paginasi per halaman, partikel atmosferik,
 * tipografi anti-aliased FreeType, serta dukungan sentuh mobile dan keyboard PC.
 */
class StoryCodexScreen(
    private val game: StickmanGame,
    private val returnScreen: Screen? = null
) : Screen {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(1280f, 720f, camera)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val glyphLayout = GlyphLayout()
    private val touchVec = Vector3()

    private var animTime = 0f
    private var inputCooldown = 0.25f

    data class CodexPage(
        val subtitle: String,
        val paragraphs: List<String>
    )

    data class CodexChapter(
        val title: String,
        val tag: String,
        val pages: List<CodexPage>
    )

    private val chapters: List<CodexChapter> = listOf(
        // 0. Prolog & Kosmologi
        CodexChapter(
            title = "Prolog & Kosmologi Aethelgard",
            tag = "KOSMOLOGI",
            pages = listOf(
                CodexPage(
                    subtitle = "Zaman Keemasan Warna & Para Penulis Takdir Purba",
                    paragraphs = listOf(
                        "Beribu-ribu milenium sebelum waktu mengenal hitungan mundur, dunia Aethelgard adalah bentangan kosmis yang bermandikan keajaiban spektrum warna murni (Prismatic Anima). Lautan memancarkan biru safir, pepohonan membisikkan zamrud kehidupan, dan kota-kota marmer putih menjulang dinaungi langit keemasan.",
                        "Di era itu, peradaban fana hidup berdampingan dengan para dewa yang dikenal sebagai The Grand Calligraphers (Para Penulis Takdir Purba). Menggunakan The Primordial Stylus (Pena Takdir Semesta), para arsitek kosmis ini mengukir jalannya kelahiran, seni, dan kematian dalam gulungan abadi The Cosmic Parchment.",
                        "Namun, seiring berjalannya masa, keberagaman emosi dan kebebasan berkehendak manusia mulai dipandang oleh para dewa sebagai 'noda kotoran' yang merusak harmoni kanvas semesta."
                    )
                ),
                CodexPage(
                    subtitle = "Kataklisme Tinta Agung & Kelahiran Kaum Siluet",
                    paragraphs = listOf(
                        "Dalam satu malam tanpa fajar, The Grand Calligraphers menumpahkan cawan Tinta Primordial ke seluruh semesta dalam tragedi The Great Inking.",
                        "Seluruh spektrum warna disedot habis. Daging, organ tubuh fana, dan ornamen mewah terkikis luruh. Makhluk hidup yang bertahan terkompresi secara kinetik menjadi wujud geometris paling murni: garis-garis hitam padat dengan mata putih menyala yang kelak disebut Kaum Siluet (The Stickmen).",
                        "Meskipun rupa luar mereka direduksi menjadi garis minimalis, pemadatan ini justru mengkristalkan kehendak jiwa (Anima Core) mereka menjadi sangat tajam. Refleks, kekuatan tebasan, dan daya tahan benturan mereka melampaui segala hukum biologis masa lalu."
                    )
                ),
                CodexPage(
                    subtitle = "Kutukan Pasak Bumi & Fenomena Penyusutan Jiwa",
                    paragraphs = listOf(
                        "Agar kaum siluet tidak mampu meraih puncak langit, para dewa menanamkan gravitasi tektonik ekstrem (g = -25.0 m/s2) sebagai belenggu spiritual yang mengunci mereka di dasar bumi.",
                        "Jika seorang siluet berdiam diri terlalu lama di atas tanah, ia akan mengalami The Dissolution (Penyusutan Jiwa)—kaki mereka meleleh menjadi tinta mati dan ingatan mereka terhapus selamanya.",
                        "Satu-satunya jalan mempertahankan eksistensi adalah terus bergerak, melompat ke ketinggian, menangkis serangan musuh melalui Perfect Parry, dan mendaki The Obsidian Spire (Menara Bayang) hingga ke puncak takdir."
                    )
                )
            )
        ),

        // 1. Babak I: Benteng Bawah
        CodexChapter(
            title = "Babak I: Ujian Inisiasi (Lantai 1-3)",
            tag = "LANTAI 1 - 3",
            pages = listOf(
                CodexPage(
                    subtitle = "Terbangun di Kaki Menara & Lentera Aria",
                    paragraphs = listOf(
                        "Kaelen The Bladeweaver terbangun di atas pasir obsidian dingin tanpa mengingat masa lalunya. Satu-satunya tanda pengenal adalah sebuah rune emas menyala di dadanya dan sebilah pedang baja tua di samping gerbang besi raksasa.",
                        "Di sana, ia mendengar suara lembut Aria, Sang Gema Warna Terakhir—sepercik jiwa murni yang menolak mati. Aria membimbingnya melangkah menembus gerbang lantai 1.",
                        "Di Lantai 1 (Lower Bastion), Kaelen menghadapi Crimson Grunts: prajurit garnisun masa lalu yang kehilangan akal sehat dan terinfeksi kegilaan tinta. Di Lantai 2 (Spire of Hazards), ia mengasah ketangkasan fisik melompati jurang duri obsidian yang mematikan."
                    )
                ),
                CodexPage(
                    subtitle = "Duel Kehormatan Melawan Jenderal Oakhaven",
                    paragraphs = listOf(
                        "Di Lantai 3 (Apex Boss Arena), pintu gerbang menuju lantai atas dijaga oleh sosok ksatria raksasa setinggi 2.8 meter dengan tanduk emas: Jenderal Oakhaven (The Tower Guardian).",
                        "Oakhaven bertarung dengan kehormatan prajurit sejati. Saat terluka parah (HP < 50%), sang jenderal mengaum kencang melepaskan badai tebasan angin ganda (Wind Slash Enrage).",
                        "Ketika ditundukkan oleh tebasan Kaelen, Oakhaven menolak tersungkur. Ia menancapkan pedangnya ke lantai dan menyerahkan kunci segel seraya berbisik: 'Tebasanmu tajam tanpa ragu... Namun waspadalah, di atas sana, musuhmu bukanlah monster, melainkan kebenaran kelam di balik penciptaan menara ini!'"
                    )
                )
            )
        ),

        // 2. Babak II: Rahasia Kaca & Alkimia
        CodexChapter(
            title = "Babak II: Rahasia Tinta & Logam Abadi",
            tag = "LANTAI 4 - 6",
            pages = listOf(
                CodexPage(
                    subtitle = "Perpustakaan Kaca Hitam & Ratu Busur Amethyst",
                    paragraphs = listOf(
                        "Memasuki Lantai 4 (The Sunken Archives), suasana benteng militer berganti menjadi reruntuhan perpustakaan kristal kaca hitam. Di sini terungkap bahwa para dewa Calligrapher takut pada 'Warna Emas Jiwa' (The Golden Anima) yang hampir ditemukan manusia.",
                        "Di Lantai 5 (Crucible of Mutations), Kaelen berhadapan dengan The Amethyst Matron (Sang Ratu Busur Bayang)—mantan penyihir agung yang terinfeksi racun purba.",
                        "Sang Ratu melompat lincah antar platform, meluncurkan hujan panah proyektil ungu dan menyelimuti arena dengan Toxic Miasma yang mematikan. Kaelen memadukan luncuran Dash I-Frames dan tebasan udara untuk menumbangkan sang ratu."
                    )
                ),
                CodexPage(
                    subtitle = "Suaka Api Abadi Vulcanus Sang Penempa Jiwa",
                    paragraphs = listOf(
                        "Di Lantai 6 (The Iron Foundry), Kaelen tiba di tempat perlindungan yang hangat: bengkel tempa legendaris milik Vulcanus.",
                        "Di hadapan tungku emas abadi, Vulcanus berkata: 'Denting paluku adalah satu-satunya melodi yang menolak tunduk pada keheningan menara ini! Koin emas yang kau bawa adalah tetesan sinar mentari yang dipadatkan oleh darah pendaki masa lalu. Berikan padaku, kan kutempa senjatamu agar mampu membelah takdir!'",
                        "Di bengkel ini, Kaelen menempa ulang senjatanya, meningkatkan Attack Power, Critical Rate, dan Kecepatan Ayunan sebelum menantang langit menara yang retak."
                    )
                )
            )
        ),

        // 3. Babak III: Pendakian Astral & Cermin
        CodexChapter(
            title = "Babak III: Pendakian Astral & Cermin Jiwa",
            tag = "LANTAI 7 - 9",
            pages = listOf(
                CodexPage(
                    subtitle = "Tebing Gravitasi Retak & Cermin Gema Bayangan",
                    paragraphs = listOf(
                        "Di Lantai 7 (The Fractured Precipice), dinding fisik menara telah hancur terkoyak, memperlihatkan jurang kosmis luas dengan bintang-bintang mati. Medan gravitasi rendah membuat pulau-pulau obsidian melayang, memicu duel akrobatik udara melawan kawanan pemanah bayangan.",
                        "Di Lantai 8 (The Hall of Echoes), menara memunculkan kutukan cermin: The Shadow Doppelgänger—manifestasi bayangan dari ketakutan dan keraguan batin Kaelen sendiri.",
                        "Bayangan tersebut meniru arketipe, kombo pedang, dan parry sang pemain. Kaelen menyadari bahwa musuh terbesar bukanlah para dewa, melainkan keraguan dalam dirinya sendiri. Dengan satu tebasan parry presisi, ia memecahkan ilusi cermin bayangannya."
                    )
                ),
                CodexPage(
                    subtitle = "Kuil Gerhana Darah & Pangeran Vampirik",
                    paragraphs = listOf(
                        "Di Lantai 9 (The Blood Moon Sanctum), langit menara terbakar merah darah oleh fenomena gerhana abadi.",
                        "Arena ini dikuasai oleh The Crimson Sovereign (Pangeran Belati Darah)—seorang ksatria masa lampau yang meminum tinta darah demi keabadian. Setiap tusukan belatinya menyedot darah lawan (Lifesteal 20%).",
                        "Kaelen bertarung dengan disiplin tinggi, menghindari tebasan kilat sang pangeran dan mengeksekusi kombo penutup yang membuka portal tangga surga menuju Lantai 10."
                    )
                )
            )
        ),

        // 4. Babak IV: Puncak Kanvas Takdir
        CodexChapter(
            title = "Babak IV: Puncak Kanvas Takdir (Lantai 10)",
            tag = "LANTAI 10",
            pages = listOf(
                CodexPage(
                    subtitle = "Altar Putih di Atas Samudra Tinta Kosmis",
                    paragraphs = listOf(
                        "Lantai 10 bukanlah ruangan beratap, melainkan sebuah platform marmer putih tak terbatas yang melayang di tengah samudra tinta pekat: The Primordial Canvas.",
                        "Di tengah altar, bertahta The Grand Calligrapher (Sang Penulis Takdir Semesta)—sosok raksasa setinggi 3.4 meter bermantel bintang dengan The Primordial Stylus di genggamannya.",
                        "'Kau telah mendaki terlalu tinggi, wahai garis hitam yang tersesat... Dalam tinta hitam yang sunyi ini, semesta menemukan kedamaian mutlak. Kini, biarkan aku menghapus keberadaanmu dari kanvas ini untuk selamanya!'"
                    )
                ),
                CodexPage(
                    subtitle = "Nyala Kehendak Emas & Patahnya Pena Dewa",
                    paragraphs = listOf(
                        "Pertarungan pamungkas terjadi dalam 3 fase kosmis: pilar tinta tektonik, hujan meteorit sabit hitam, dan pancaran laser pemotong dimensi.",
                        "Pada saat kritis, rune emas di dada Kaelen meledak memancarkan energi The Golden Anima. Menggabungkan seluruh ilmu bela diri yang dipelajarinya, Kaelen melompat menembus badai tinta dan menebaskan pedangnya tepat ke poros pena kosmis sang dewa!",
                        "KRAAASSH! The Primordial Stylus terbelah menjadi dua keping emas yang hancur. Sang Grand Calligrapher menjerit seraya tubuhnya luruh dan tersedot ke dalam pusaran jurang ketiadaan."
                    )
                )
            )
        ),

        // 5. Epilog & Paradoks Endless Spire
        CodexChapter(
            title = "Epilog: Paradoks Abadi Endless Spire",
            tag = "EPILOG",
            pages = listOf(
                CodexPage(
                    subtitle = "Kembalinya Warna & Retaknya Garis Waktu",
                    paragraphs = listOf(
                        "Dengan hancurnya pena penciptaan, kutukan pasak gravitasi lenyap seketika. Dari puncak menara yang retak, jutaan berkas spektrum warna—merah, biru, hijau, dan emas kemuliaan—meledak ke langit Aethelgard, membasuh kegelapan ribuan tahun.",
                        "Namun, patahnya pena dewa memicu anomali metafisika: The Temporal Fracture (Pecahnya Waktu). Menara Obsidian tidak runtuh, melainkan bermutasi menjadi dimensi ruang-waktu tak berbatas: The Endless Spire (Lantai 11 ke atas).",
                        "Lantai-lantai di atas lantai 10 bukanlah bangunan fisik nyata, melainkan ingatan masa lalu menara yang berputar dalam sebuah Infinite Time-Loop dengan mutator anomali yang semakin ganas."
                    )
                ),
                CodexPage(
                    subtitle = "Takdir Abadi Sang Pendaki Menara",
                    paragraphs = listOf(
                        "Kaelen dan para pendekar siluet menyadari bahwa kestabilan realitas Aethelgard bergantung pada kehendak juang yang terus menyala di dalam menara.",
                        "Mereka memilih untuk tidak turun ke dunia fana yang damai. Mereka tetap berada di dalam menara, terus melompat, menebas, dan mendaki Endless Spire tanpa batas.",
                        "Selama ada pedang yang berayun dan kaki yang melompat di puncak menara, kegelapan mutlak takkan pernah mampu menenggelamkan dunia Aethelgard lagi."
                    )
                )
            )
        ),

        // 6. Profil Pendekar Siluet
        CodexChapter(
            title = "Para Pendekar Siluet (The Champions)",
            tag = "KARAKTER",
            pages = listOf(
                CodexPage(
                    subtitle = "Kaelen The Bladeweaver (Arketipe: BALANCED)",
                    paragraphs = listOf(
                        "Murid terakhir Aliran Zephyr dan kapten pengawal kerajaan Aethelgard masa lampau. Bersenjatakan Runed Broadsword.",
                        "Gaya bertarungnya mengutamakan keseimbangan antara tebasan jarak dekat, proyektil angin pemotong barisan (Gale Crescent), tebasan peluncur udara (Rising Dragon), dan tusukan kilat (Dash Strike).",
                        "Kutipan: 'Pedang yang tak diayunkan adalah beban tak berguna. Menara ini meminta kepastian darah, bukan keraguan batin.'"
                    )
                ),
                CodexPage(
                    subtitle = "Zephyr The Shadowstalker (Arketipe: AGILE)",
                    paragraphs = listOf(
                        "Agen rahasia Ordo Eclipse Creed yang beroperasi di lorong-lorong gelap Aethelgard. Bersenjatakan Dual Venom Daggers.",
                        "Mengandalkan kecepatan ekstrem, rentetan tusukan bertubi-tubi (Venom Flurry), lemparan shuriken beracun (Shadow Shuriken), dan teleportasi menembus punggung lawan (Phantom Blink) dengan garansi 100% kritikal.",
                        "Kutipan: 'Bayangan tidak memotong dengan kekuatan kasar, melainkan ketepatan waktu. Setiap celah di zirah musuh adalah pintu masuk kematian.'"
                    )
                ),
                CodexPage(
                    subtitle = "Gorgon The Ironbreaker (Arketipe: HEAVY)",
                    paragraphs = listOf(
                        "Paladin benteng batu meteorit dan penjaga garis depan kerajaan. Bersenjatakan Colossal Obsidian Greatsword.",
                        "Memiliki pertahanan mutlak (Super Armor kebal stagger), hentakan gempa tanah (Earth Shockwave), ayunan badai berputar (Cyclone Cleave), dan raungan perang titan (Titan Roar) yang memulihkan HP serta mementalkan musuh.",
                        "Kutipan: 'Baja tidak mengenal rasa takut. Menara ini akan runtuh di bawah kakiku sebelum tamengku retak!'"
                    )
                )
            )
        ),

        // 7. Bestiary: Monster Kroco & Minion
        CodexChapter(
            title = "Bestiary: Kaum Terinfeksi (Minions)",
            tag = "BESTIARY",
            pages = listOf(
                CodexPage(
                    subtitle = "Crimson Grunt & Amethyst Sniper",
                    paragraphs = listOf(
                        "❖ Crimson Grunt (Infanteri Pedang Karat):",
                        "Sisa-sisa tentara kerajaan yang terinfeksi kegilaan tinta. Menyerang dalam kawanan menggunakan pedang berkarat. Sangat agresif mendekat namun rentan terhadap Perfect Parry dan serangan peluncur udara.",
                        "❖ Amethyst Sniper (Pemanah Kristal Bayang):",
                        "Pemburu elit klan utara yang menyatu dengan kaca menara. Bersembunyi di platform tinggi dan menembakkan proyektil racun berkecepatan 14 m/s dengan taktik kiting mundur. Sangat rapuh jika didekati dengan Dash Strike."
                    )
                ),
                CodexPage(
                    subtitle = "Obsidian Gargoyle & Void Crawler",
                    paragraphs = listOf(
                        "❖ Obsidian Gargoyle (Binatang Buas Langit):",
                        "Patung ornamen pelindung menara yang dihidupkan sisa sihir primordial. Bersarang di langit-langit tinggi, menukik tajam mencakar pemain dan melepaskan sabit angin radial.",
                        "❖ Void Crawler (Parasit Lumpur Tinta):",
                        "Makhluk mutasi tanpa mata yang merayap cepat di permukaan tanah lembap, melompat menyergap dan meninggalkan jejak racun yang memperlambat pergerakan (Slow debuff). Runtuh seketika oleh gempa Earth Shockwave."
                    )
                )
            )
        ),

        // 8. Prasasti Batu Hitam (Lore Tablets)
        CodexChapter(
            title = "Prasasti Kuno Menara (Lore Tablets)",
            tag = "PRASASTI",
            pages = listOf(
                CodexPage(
                    subtitle = "Prasasti Kuno: Lantai 1 s/d Lantai 5",
                    paragraphs = listOf(
                        "📜 Tablet I (Lantai 1): 'Hari ke-47. Kami tiba di gerbang bawah dengan 100 prajurit terbaik. Tubuh kami telah menghitam, jari menyatu menjadi garis padat. Pedang terasa seratus kali lebih ringan. Jangan pernah berhenti bergerak saat berada di atas platform.'",
                        "📜 Tablet II (Lantai 2): 'Kutukan gravitasi ini bukan dari bumi, melainkan dari langit. Para dewa memasang duri obsidian ini agar darah kita kembali menyuburkan tanah.'",
                        "📜 Tablet IV (Lantai 4): 'Kami menyegel diri dalam ruang arsip ini untuk mencari penawar racun tinta... namun tinta itu sendiri telah menjadi darah kami.'"
                    )
                ),
                CodexPage(
                    subtitle = "Prasasti Kuno: Lantai 6 s/d Lantai 10",
                    paragraphs = listOf(
                        "📜 Tablet VI (Lantai 6): 'Besi meteorit Vulcanus adalah satu-satunya benda yang tidak mempan terhadap Tinta Primordial. Selama apinya menyala, harapan manusia takkan pernah padam.'",
                        "📜 Tablet VIII (Lantai 8): 'Musuh paling berbahaya di menara ini bukanlah dewa, melainkan dirimu sendiri yang menyerah. Ketika bayanganmu mengayunkan pedang yang sama, lakukan parry saat kilatan matanya menyala!'",
                        "📜 Tablet X (Lantai 10): 'Pena para dewa memang menuliskan takdir kita di masa lalu, tapi pedang kita yang akan menentukan masa depan!'"
                    )
                )
            )
        )
    )

    private var selectedChapterIndex = 0
    private var currentPageIndex = 0

    // Partikel Latar Belakang (Golden / Cyan Ink Embers)
    private class StoryParticle(
        val pos: Vector2 = Vector2(),
        val vel: Vector2 = Vector2(),
        var size: Float = 0f,
        var color: Color = Color.WHITE,
        var maxLife: Float = 1f
    ) {
        var life: Float = 0f
        val isExpired: Boolean get() = life >= maxLife

        fun reset(initial: Boolean = false) {
            val x = MathUtils.random(0f, 1280f)
            val y = if (initial) MathUtils.random(0f, 720f) else -10f
            pos.set(x, y)
            vel.set(MathUtils.random(-12f, 12f), MathUtils.random(20f, 50f))
            size = MathUtils.random(2f, 4.5f)
            val isGold = MathUtils.randomBoolean(0.7f)
            color = if (isGold) Color(1f, 0.82f, 0.28f, MathUtils.random(0.3f, 0.65f))
                    else Color(0.25f, 0.85f, 1f, MathUtils.random(0.3f, 0.65f))
            maxLife = MathUtils.random(8f, 15f)
            life = 0f
        }
    }
    private val particles = mutableListOf<StoryParticle>()

    override fun show() {
        Gdx.input?.setCatchKey(Keys.BACK, true)
        FontManager.init()
        inputCooldown = 0.25f
        selectedChapterIndex = 0
        currentPageIndex = 0

        particles.clear()
        for (i in 0 until 40) {
            val p = StoryParticle()
            p.reset(initial = true)
            particles.add(p)
        }
    }

    override fun render(delta: Float) {
        animTime += delta
        if (inputCooldown > 0f) {
            inputCooldown -= delta
        } else {
            handleInput()
        }
        if (game.screen !== this) return

        updateParticles(delta)

        ScreenUtils.clear(0.035f, 0.045f, 0.075f, 1f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        batch.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // =========================================================================
        // 1. RENDER LATAR BELAKANG GEOMETRIS & KOTAK CODEX (SHAPE RENDERER)
        // =========================================================================
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Partikel Cahaya Jiwa Menara
        for (p in particles) {
            val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
            shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, p.color.a * alpha * 0.45f)
            shapeRenderer.circle(p.pos.x, p.pos.y, p.size * 1.6f)
            shapeRenderer.setColor(1f, 1f, 1f, alpha * 0.8f)
            shapeRenderer.circle(p.pos.x, p.pos.y, p.size * 0.5f)
        }

        // Panel Judul Atas
        shapeRenderer.setColor(0.06f, 0.08f, 0.14f, 0.95f)
        shapeRenderer.rect(40f, 640f, 1200f, 60f)
        shapeRenderer.setColor(0.95f, 0.78f, 0.25f, 0.85f)
        shapeRenderer.rectLine(40f, 640f, 1240f, 640f, 2.5f)

        // Kolom Kiri: Navigasi Bab (Chapter Sidebar)
        shapeRenderer.setColor(0.05f, 0.07f, 0.12f, 0.92f)
        shapeRenderer.rect(40f, 100f, 360f, 520f)
        shapeRenderer.setColor(0.18f, 0.24f, 0.35f, 0.75f)
        shapeRenderer.rectLine(400f, 100f, 400f, 620f, 2f)

        // Highlight Bab Aktif
        val chapterItemH = 54f
        val chapterStartY = 620f - chapterItemH
        for (i in chapters.indices) {
            val itemY = chapterStartY - (i * chapterItemH)
            if (i == selectedChapterIndex) {
                val pulse = sin(animTime * 5f) * 0.08f
                shapeRenderer.setColor(0.12f, 0.18f, 0.28f, 0.95f)
                shapeRenderer.rect(45f, itemY + 4f, 350f, chapterItemH - 8f)
                shapeRenderer.setColor(0.95f, 0.78f, 0.25f, 0.85f + pulse)
                shapeRenderer.rectLine(45f, itemY + 4f, 45f, itemY + chapterItemH - 4f, 4f)
                shapeRenderer.rectLine(45f, itemY + 4f, 395f, itemY + 4f, 1.5f)
                shapeRenderer.rectLine(45f, itemY + chapterItemH - 4f, 395f, itemY + chapterItemH - 4f, 1.5f)
            } else {
                shapeRenderer.setColor(0.08f, 0.11f, 0.18f, 0.5f)
                shapeRenderer.rect(45f, itemY + 4f, 350f, chapterItemH - 8f)
            }
        }

        // Panel Kanan: Wadah Baca Teks Kisah (Codex Reader Parchment)
        shapeRenderer.setColor(0.06f, 0.08f, 0.13f, 0.94f)
        shapeRenderer.rect(420f, 100f, 820f, 520f)
        shapeRenderer.setColor(0.95f, 0.78f, 0.25f, 0.70f)
        shapeRenderer.rectLine(420f, 620f, 1240f, 620f, 2f)
        shapeRenderer.rectLine(420f, 100f, 1240f, 100f, 2f)
        shapeRenderer.rectLine(420f, 100f, 420f, 620f, 2f)
        shapeRenderer.rectLine(1240f, 100f, 1240f, 620f, 2f)

        // Ornamen Sudut Kotak Baca
        shapeRenderer.setColor(0.95f, 0.78f, 0.25f, 0.9f)
        // Kiri Atas
        shapeRenderer.rectLine(420f, 620f, 445f, 620f, 4f)
        shapeRenderer.rectLine(420f, 620f, 420f, 595f, 4f)
        // Kanan Atas
        shapeRenderer.rectLine(1240f, 620f, 1215f, 620f, 4f)
        shapeRenderer.rectLine(1240f, 620f, 1240f, 595f, 4f)
        // Kiri Bawah
        shapeRenderer.rectLine(420f, 100f, 445f, 100f, 4f)
        shapeRenderer.rectLine(420f, 100f, 420f, 125f, 4f)
        // Kanan Bawah
        shapeRenderer.rectLine(1240f, 100f, 1215f, 100f, 4f)
        shapeRenderer.rectLine(1240f, 100f, 1240f, 125f, 4f)

        // Garis Pemisah Subtitle di Halaman Baca
        shapeRenderer.setColor(0.20f, 0.28f, 0.40f, 0.65f)
        shapeRenderer.rectLine(445f, 545f, 1215f, 545f, 1.5f)

        // Tombol Navigasi Bawah: [❮ SEBELUMNYA] [SELANJUTNYA ❯] dan [KEMBALI]
        // Tombol Page Prev (440f - 580f)
        shapeRenderer.setColor(0.09f, 0.13f, 0.21f, 0.9f)
        shapeRenderer.rect(445f, 40f, 160f, 44f)
        shapeRenderer.setColor(0.25f, 0.35f, 0.50f, 0.8f)
        shapeRenderer.rectLine(445f, 40f, 605f, 40f, 1.5f)
        shapeRenderer.rectLine(445f, 84f, 605f, 84f, 1.5f)

        // Tombol Page Next (630f - 790f)
        shapeRenderer.setColor(0.09f, 0.13f, 0.21f, 0.9f)
        shapeRenderer.rect(635f, 40f, 160f, 44f)
        shapeRenderer.setColor(0.25f, 0.35f, 0.50f, 0.8f)
        shapeRenderer.rectLine(635f, 40f, 795f, 40f, 1.5f)
        shapeRenderer.rectLine(635f, 84f, 795f, 84f, 1.5f)

        // Tombol Kembali Ke Menu (1000f - 1240f)
        shapeRenderer.setColor(0.18f, 0.08f, 0.10f, 0.92f)
        shapeRenderer.rect(980f, 38f, 260f, 46f)
        shapeRenderer.setColor(0.95f, 0.35f, 0.35f, 0.85f)
        shapeRenderer.rectLine(980f, 38f, 1240f, 38f, 2f)
        shapeRenderer.rectLine(980f, 84f, 1240f, 84f, 2f)
        shapeRenderer.rectLine(980f, 38f, 980f, 84f, 2f)
        shapeRenderer.rectLine(1240f, 38f, 1240f, 84f, 2f)

        shapeRenderer.end()

        // =========================================================================
        // 2. RENDER TEKS & TIPOGRAFI LORE (SPRITE BATCH)
        // =========================================================================
        batch.begin()

        val titleF = if (FontManager.isInitialized) FontManager.titleFont else BitmapFont()
        val headF = if (FontManager.isInitialized) FontManager.headerFont else BitmapFont()
        val regF = if (FontManager.isInitialized) FontManager.regularFont else BitmapFont()

        // Judul Layar
        titleF.color = Color.WHITE
        titleF.draw(batch, "KRONIK MENARA AETHELGARD", 60f, 685f)
        regF.color = Color.GOLD
        regF.draw(batch, "Arsip Kisah Penuh & Rahasia Kanvas Dunia Siluet", 750f, 678f)

        // Daftar Bab di Sidebar Kiri
        for (i in chapters.indices) {
            val ch = chapters[i]
            val itemY = chapterStartY - (i * chapterItemH)

            if (i == selectedChapterIndex) {
                headF.setColor(1f, 0.85f, 0.25f, 1f)
                val activeTitle = "❖ ${ch.title}"
                headF.draw(batch, activeTitle, 58f, itemY + 36f)
                regF.setColor(0.4f, 0.85f, 1f, 0.9f)
                regF.draw(batch, "[${ch.tag}]", 58f, itemY + 16f)
            } else {
                headF.setColor(0.75f, 0.80f, 0.90f, 0.85f)
                headF.draw(batch, ch.title, 58f, itemY + 36f)
                regF.setColor(0.5f, 0.55f, 0.65f, 0.7f)
                regF.draw(batch, "[${ch.tag}]", 58f, itemY + 16f)
            }
        }

        // Konten Bab yang Sedang Dipilih
        val curChapter = chapters[selectedChapterIndex]
        val totalPages = curChapter.pages.size
        currentPageIndex = currentPageIndex.coerceIn(0, totalPages - 1)
        val curPage = curChapter.pages[currentPageIndex]

        // Judul Bab & Subtitle Halaman
        headF.setColor(1f, 0.85f, 0.25f, 1f)
        headF.draw(batch, curChapter.title, 450f, 595f)

        regF.setColor(0.4f, 0.85f, 1f, 1f)
        regF.draw(batch, "✦  ${curPage.subtitle}", 450f, 568f)

        // Nomor Halaman
        regF.setColor(0.8f, 0.85f, 0.95f, 0.85f)
        val pageNumStr = "Halaman ${currentPageIndex + 1} / $totalPages"
        regF.draw(batch, pageNumStr, 1100f, 595f)

        // Paragraf Kisah (Wrapped Text)
        var cursorY = 520f
        regF.setColor(0.92f, 0.94f, 0.98f, 0.95f)
        for (paragraph in curPage.paragraphs) {
            glyphLayout.setText(regF, paragraph, Color.WHITE, 760f, Align.left, true)
            regF.draw(batch, paragraph, 450f, cursorY, 760f, Align.left, true)
            cursorY -= (glyphLayout.height + 22f)
        }

        // Teks Tombol Navigasi Bawah
        // Prev Page
        if (currentPageIndex > 0) {
            regF.setColor(1f, 0.85f, 0.3f, 1f)
            glyphLayout.setText(regF, "❮ SEBELUMNYA")
            regF.draw(batch, "❮ SEBELUMNYA", 445f + (160f - glyphLayout.width) / 2f, 68f)
        } else {
            regF.setColor(0.4f, 0.45f, 0.55f, 0.5f)
            glyphLayout.setText(regF, "❮ SEBELUMNYA")
            regF.draw(batch, "❮ SEBELUMNYA", 445f + (160f - glyphLayout.width) / 2f, 68f)
        }

        // Next Page
        if (currentPageIndex < totalPages - 1) {
            regF.setColor(1f, 0.85f, 0.3f, 1f)
            glyphLayout.setText(regF, "SELANJUTNYA ❯")
            regF.draw(batch, "SELANJUTNYA ❯", 635f + (160f - glyphLayout.width) / 2f, 68f)
        } else {
            regF.setColor(0.4f, 0.45f, 0.55f, 0.5f)
            glyphLayout.setText(regF, "SELANJUTNYA ❯")
            regF.draw(batch, "SELANJUTNYA ❯", 635f + (160f - glyphLayout.width) / 2f, 68f)
        }

        // Kembali
        headF.setColor(1f, 0.45f, 0.45f, 1f)
        glyphLayout.setText(headF, "🔙 KEMBALI KE MENU")
        headF.draw(batch, "🔙 KEMBALI KE MENU", 980f + (260f - glyphLayout.width) / 2f, 68f)

        // Petunjuk Tombol Keyboard
        regF.setColor(0.55f, 0.60f, 0.70f, 0.8f)
        regF.draw(batch, "[W/S] Pilih Bab  |  [A/D] Ganti Halaman  |  [ESC/BACK] Kembali", 45f, 62f)

        batch.end()
    }

    private fun handleInput() {
        // Navigasi Bab (Atas/Bawah)
        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            if (selectedChapterIndex > 0) {
                selectedChapterIndex--
                currentPageIndex = 0
                AudioManager.playSlashLight()
            }
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            if (selectedChapterIndex < chapters.size - 1) {
                selectedChapterIndex++
                currentPageIndex = 0
                AudioManager.playSlashLight()
            }
        }

        // Navigasi Paginasi Halaman (Kiri/Kanan)
        if (Gdx.input.isKeyJustPressed(Keys.LEFT) || Gdx.input.isKeyJustPressed(Keys.A)) {
            previousPage()
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT) || Gdx.input.isKeyJustPressed(Keys.D)) {
            nextPage()
        }

        // Deteksi Interaksi Sentuh Layar / Mouse
        if (Gdx.input.justTouched()) {
            touchVec.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            viewport.unproject(touchVec)

            // Klik Sidebar Bab Kiri
            val chapterItemH = 54f
            val chapterStartY = 620f - chapterItemH
            for (i in chapters.indices) {
                val itemY = chapterStartY - (i * chapterItemH)
                if (touchVec.x in 45f..395f && touchVec.y in itemY..(itemY + chapterItemH)) {
                    if (selectedChapterIndex != i) {
                        selectedChapterIndex = i
                        currentPageIndex = 0
                        AudioManager.playSlashLight()
                        HapticManager.vibrateLight()
                    }
                    break
                }
            }

            // Klik Tombol Prev Page (445f..605f, 40f..84f)
            if (touchVec.x in 445f..605f && touchVec.y in 40f..84f) {
                previousPage()
            }

            // Klik Tombol Next Page (635f..795f, 40f..84f)
            if (touchVec.x in 635f..795f && touchVec.y in 40f..84f) {
                nextPage()
            }

            // Klik Tombol Kembali (980f..1240f, 38f..84f)
            if (touchVec.x in 980f..1240f && touchVec.y in 38f..84f) {
                returnToPreviousScreen()
            }
        }

        // Tombol Kembali Keyboard / Android Back
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            returnToPreviousScreen()
        }
    }

    private fun previousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
            AudioManager.playJump()
            HapticManager.vibrateLight()
        }
    }

    private fun nextPage() {
        val totalPages = chapters[selectedChapterIndex].pages.size
        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++
            AudioManager.playJump()
            HapticManager.vibrateLight()
        }
    }

    private fun returnToPreviousScreen() {
        AudioManager.playSlashLight()
        (game as? StickmanGame)?.releasePreservedScreen(returnScreen ?: this)
        game.setScreen(returnScreen ?: MainMenuScreen(game))
    }

    private fun updateParticles(delta: Float) {
        for (p in particles) {
            p.life += delta
            p.pos.x += p.vel.x * delta
            p.pos.y += p.vel.y * delta
            if (p.isExpired || p.pos.y > 730f) {
                p.reset(initial = false)
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
    }
}

# Stickman Tower Adventure — Game Design Document (GDD) v3.0
### *Master Production & 50-Floor Canonical Campaign Architecture*

> **Status Dokumen:** Master Production Locked (v3.0 Canon)  
> **Judul Game:** Stickman Tower Adventure: The Chrono-Spire of Aethelgard  
> **Genre:** 2D Side-Scrolling Action RPG Platformer & Hack-and-Slash Megastructure Odyssey  
> **Target Platform:** Android (Mobile Native) & Desktop (LWJGL3 PC Testbed)  
> **Bahasa Pemrograman:** Kotlin  
> **Framework Engine:** libGDX (OpenGL ES 2.0 / FreeType / Virtual Controller)  
> **Arsitektur Save:** Version 3 Normalized Schema dengan Karakter Terkunci & 64-bit Salted FNV-1a Checksum  
> **Dokumen Terkait:** [LORE_AND_WORLDBUILDING.md](file:///home/yusuf/projekan/game/game1/LORE_AND_WORLDBUILDING.md), [Alur_Pengembangan_Ekspansi_30_Lantai.md](file:///home/yusuf/projekan/game/game1/Alur_Pengembangan_Ekspansi_30_Lantai.md), [Alur_Pembuatan_Aplikasi.md](file:///home/yusuf/projekan/game/game1/Alur_Pembuatan_Aplikasi.md)

---

## 1. Visi Permainan & Pilar Ludonaratif (Game Vision & Narrative Pillars)

### 1.1 Konsep Inti (The Core Premise)
*Stickman Tower Adventure* menggabungkan aksi pertarungan jarak dekat real-time (*hack-and-slash*) berkecepatan tinggi, navigasi platforming presisi, dan kedalaman sistem RPG dalam dunia fantasi gelap yang tereduksi menjadi hitam dan putih pasca-tragedi kosmis *The Great Inking*.

Pemain mengendalikan ksatria siluet garis hitam berinti jiwa (*Anima Core*) yang terbangun di **The Ashen Threshold (Padang Abu Luar Menara)**, dipandu oleh **Aria (Sang Gema Terakhir)** untuk mendaki **The Celestial Chrono-Spire (Menara Bayang)** setinggi **50 lantai kampanye cerita utama**.

Di sepanjang perjalanan, pemain tidak mendaki sendirian: mereka menemukan, menyelamatkan, dan bertarung bersama rekan seperjuangan yang memiliki tugas dan fungsi unik di masing-masing bioma lantai, sebelum bersama-sama menghadapi *The Grand Calligrapher* di Lantai 50 dan menembus batas waktu ke dalam dimensi *Endless Spire* (Lantai 51 ke atas).

```
[PROLOG: PADANG ABU] ──> [KAELEN: LT 1-10] ──> [RESCUE ZEPHYR: LT 10] ──> [ZEPHYR: LT 11-20] ──> [RESCUE GORGON: LT 20]
                                                                                                            │
[PATAHKAN PENA DEWA: LT 50] ◄── [THE TRIAD OF DAWN: LT 31-50] ◄── [GORGON: LT 21-30] ◄──────────────────────┘
          │
          └───> [ENDLESS CHRONO-SPIRE: LANTAI 51+]
```

### 1.2 Enam Pilar Utama Desain (The Six Design Pillars)
1. **Hero Odyssey & In-Game Progression Unlocks:**
   Karakter tidak terbuka begitu saja di menu. Karakter awal pemain adalah **Kaelen The Bladeweaver**. **Zephyr The Shadowstalker** diselamatkan di Lantai 10 dalam duel bersama melawan Lady Vespera, dan **Gorgon The Ironbreaker** diselamatkan di Lantai 20 dari pilar kawah tempa. Setiap pahlawan memiliki peran fungsional unik di bioma berikutnya.
2. **Kinetic Defiance (Perlawanan Terhadap Pelarutan Jiwa):**
   Kutukan gravitasi bumi ($g = -28.0\text{ m/s}^2$) menuntut pergerakan konstan. Berdiam diri terlalu lama di tanah memicu fenomena *The Dissolution*. Pemain harus terus melompat, melakukan *dodge roll*, menangkis serangan secara presisi (*Perfect Parry* jendela 120ms), dan melancarkan kombo 3-hit.
3. **Vast Explorable Level Design (Peta Luas, Panjang & Non-Linear):**
   Setiap lantai merupakan bentangan terstruktur sepanjang **$110\text{m} - 160\text{m}$ horizontal** dengan elevasi vertikal bertingkat **$22\text{m} - 38\text{m}$**. Mengintegrasikan pintu gerbang segel runik (*Runic Barrier Doors*), ruang brankas rahasia (*Secret Vaults*), platform satu arah (*One-Way Platforms*), dan ventilasi lontaran (*Thermal Vents*).
4. **Deep RPG Progression (80-Level Growth Curve):**
   EXP leveling hingga Level 80, alokasi poin bakat di [TalentManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/talent/TalentManager.kt), penempaan senjata di Suaka Vulcanus Lantai 13 ([BlacksmithScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BlacksmithScreen.kt)), serta 6 spesialisasi Sub-Class unik.
5. **Living Bestiary with Lore Origins:**
   18+ jenis musuh bukan sekadar monster tanpa jiwa, melainkan mantan prajurit kerajaan, pustakawan, paladin batu, dan bangsawan vampirik yang terdistorsi oleh tinta kutukan.
6. **Harmoni Total Narasi & Kode (Ludonarrative Integration):**
   Prolog Padang Abu, cutscene co-op tag-team unlock di Lantai 10 dan 20, Story Codex 50 bab, dan 50 Prasasti Batu Hitam interaktif di dalam game.

---

## 2. Struktur Arsitektur 50 Lantai & Pembagian Pahlawan (50-Floor World Design)

### 2.1 Pembagian 5 Babak Besar (The Five Grand Sagas)

| Saga | Lantai | Nama Bioma / Zona | Panjang Map | Pahlawan Terdepan | Bos Utama / Momen Kunci |
| :--- | :---: | :--- | :---: | :---: | :--- |
| **Prolog** | Intro | The Ashen Threshold (Padang Abu) | $90\text{m}$ | Kaelen (Solo) | Bangkitnya Kaelen & Lentera Aria |
| **SAGA I** | **1 - 10** | The Lower Bastion & Sunken Garrison | $110\text{m} - 140\text{m}$ | **Kaelen The Bladeweaver** | **Lt 5: General Oakhaven**<br/>**Lt 10: Lady Vespera (Unlock Zephyr)** |
| **SAGA II**| **11 - 20** | Sunken Archives & Clockwork Foundry | $130\text{m} - 155\text{m}$ | **Zephyr The Shadowstalker** | **Lt 13: Suaka Vulcanus (Safe Hub)**<br/>**Lt 15: Forge-Master Ignis**<br/>**Lt 20: The Slag Colossus (Unlock Gorgon)** |
| **SAGA III**| **21 - 30**| Gilded Catacombs & Basalt Chasm | $135\text{m} - 160\text{m}$ | **Gorgon The Ironbreaker** | **Lt 25: The Molten Golem King**<br/>**Lt 30: The Shadow Doppelgänger** |
| **SAGA IV**| **31 - 40**| Astral Abyss & Hall of Broken Mirrors | $140\text{m} - 160\text{m}$ | **Triad Tag-Team (Bebas Ganti)** | **Lt 35: The Void Chimera**<br/>**Lt 40: Malakor The Crimson Sovereign** |
| **SAGA V** | **41 - 50**| The Celestial Canvas & Primordial Zenith | $130\text{m} - 160\text{m}$ | **The Triad of Dawn (Sinergi 3 Hero)** | **Lt 45: Archon of Pure Geometry**<br/>**Lt 50: The Grand Calligrapher (3-Fase Dewa)** |
| **Endgame**| **51+** | The Endless Chrono-Spire | Prosedural | Bebas Pilih | Infinite Time-Loop & Boss Rush Mode |

---

### 2.2 Sistem Pembukaan Karakter & Penugasan Bioma Khusus

```kotlin
// Skema Logika Unlock Karakter di SaveData & CharacterSelectScreen
fun isCharacterUnlocked(archetype: CharacterArchetype, save: SaveData): Boolean {
    return when (archetype) {
        CharacterArchetype.BALANCED -> true // Kaelen selalu terbuka sejak Prolog
        CharacterArchetype.AGILE -> save.highestTowerFloor >= 10 || save.unlockedCharacters.contains("STICKMAN_DAGGER")
        CharacterArchetype.HEAVY -> save.highestTowerFloor >= 20 || save.unlockedCharacters.contains("STICKMAN_HEAVY")
    }
}
```

* **Lantai 1 - 10 (Kaelen's Vanguard):**
  * *Tugas & Fungsi:* Membuka barikade besi benteng bawah, mempelajari kombo 3-hit pedang, dan menundukkan mantan komandan General Oakhaven.
* **Lantai 11 - 20 (Zephyr's Infiltration):**
  * *Tugas & Fungsi:* Menyelidiki saluran pipa sempit perpustakaan beracun dan labirin roda gigi yang tidak muat dimasuki prajurit berzirah berat. Kecepatan lari tinggi ($9.5\text{ m/s}$) dan belati ganda racun sangat efektif melawan kawanan musuh gesit.
* **Lantai 21 - 30 (Gorgon's Breaker):**
  * *Tugas & Fungsi:* Menghancurkan dinding batu basalt titan yang menghalangi jalan, menahan semburan magma dengan *Super Armor*, dan meremukkan tameng ksatria batu tebal.
* **Lantai 31 - 50 (Triad Synergy & Free Choice):**
  * *Tugas & Fungsi:* Menghadapi anomali gravitasi rendah, bulan gerhana darah, dan surga marmer putih. Pemain dapat berganti karakter di *Altar Prisma* yang tersedia di awal setiap lantai sesuai preferensi taktis.

---

## 3. Spesifikasi Tiga Pendekar Playable (Hero Archetypes)

| Parameter Karakter | Kaelen (Balanced) | Zephyr (Agile) | Gorgon (Heavy) |
| :--- | :--- | :--- | :--- |
| **Status Unlock** | **Playable Sejak Awal** | **Unlocked di Lantai 10** | **Unlocked di Lantai 20** |
| **Gelar Kanonikal** | The Bladeweaver | The Shadowstalker | The Ironbreaker |
| **Senjata Khas** | Runed Broadsword | Dual Venom Daggers | Colossal Obsidian Greatsword |
| **Kecepatan Gerak** | $7.5\text{ m/s}$ | $9.5\text{ m/s}$ | $5.8\text{ m/s}$ |
| **Tinggi Lompatan** | $11.5\text{ m/s}$ ($H_{max} \approx 2.36$m) | $12.2\text{ m/s}$ ($H_{max} \approx 2.65$m) | $10.8\text{ m/s}$ ($H_{max} \approx 2.08$m) |
| **Jarak Lompatan** | $D_{reach} \approx 6.16\text{m}$ | $D_{reach} \approx 8.28\text{m}$ | $D_{reach} \approx 4.47\text{m}$ |
| **Damage Multiplier**| 1.00x | 0.85x | 1.55x |
| **Bonus Base DEF** | +0 DEF | -1.0 DEF | +3.5 DEF (Super Armor Alami) |
| **Bonus Base CRIT**| 8% Base | +15% Bonus (Total 23%) | -2% Penalty (Total 6%) |
| **Skill 1 (Active)** | *Gale Crescent* (CD 3.5s) | *Shadow Shuriken* (CD 2.8s) | *Earth Shockwave* (CD 4.0s) |
| **Skill 2 (Active)** | *Rising Dragon* (CD 4.8s) | *Venom Flurry* (CD 3.8s) | *Cyclone Cleave* (CD 5.2s) |
| **Skill 3 (Active)** | *Dash Strike* (CD 4.2s) | *Phantom Blink* (CD 4.5s) | *Titan Roar* (CD 6.5s) |
| **Ultimate Jurus** | *Bladestorm 360°* (100 SP) | *Phantom Flurry* (80 SP) | *Meteor Cleave* (120 SP) |
| **Pilihan Sub-Class**| **Berserker** / **Guardian** | **Shadow Stalker** / **Venomancer** | **Archmage** / **Spellblade** |

---

## 4. Sistem Pertarungan & Mekanika Inti (Combat Mechanics)

### 4.1 Rantai Serangan Kombo 3-Hit
1. **Hit 1 (Light Slash):** Tebasan pembuka cepat (0.12s startup, 1.0x damage, knockback ringan).
2. **Hit 2 (Cross Slash):** Tebasan silang penyambung (0.14s startup, 1.25x damage, mengangkat musuh sedikit).
3. **Hit 3 (Finisher Smash):** Hantaman pemungkas berdaya hancur tinggi (0.18s startup, 1.85x damage, knockback horizontal $7.0\text{ m/s}$, meremukkan pertahanan perisai musuh / *Guard Break*).

### 4.2 Sistem Tangkisan Sempurna & Elakan (Perfect Parry & Dodge Roll)
* **Dodge Roll:** Durasi 0.22 detik dengan kecepatan luncur $14.5\text{ m/s}$. Cooldown 0.65 detik. Memberikan kekebalan penuh (*Invulnerability Frames / i-frames*).
* **Perfect Parry (Deflect Window):** Jika pemain melakukan *Dodge Roll* tepat dalam kurun waktu **0.12 detik (7 frame)** saat serangan musuh mendarat:
  * Pemain kebal terhadap damage.
  * Memicu **Hitstop Freeze 80 milidetik** yang membekukan layar sesaat.
  * Musuh penyerang mengalami **Stun selama 1.5 detik** dengan pertahanan terbuka (*Dazed State*).
  * Efek audio denting lonceng sakral (*Parry Bell SFX*) berfrekuensi $1,760\text{ Hz}$ dan kilatan partikel emas radial.

---

## 5. Kurva Progresi RPG & Pacing 50 Lantai (RPG Progression Curve)

### 5.1 Skalasi Karakter (Level 1 s/d Level 80)
Kenaikan level pemain dihitung dengan formula non-linear:

$$\text{EXP Ke Level Berikutnya}(L) = 100 \times L^{1.35}$$

* **Lantai 1 - 10 (Saga I):** Target Level Pemain $1 \rightarrow 15$. Membuka seluruh 3 skill aktif Kaelen & membuka Zephyr.
* **Lantai 11 - 20 (Saga II):** Target Level Pemain $15 \rightarrow 30$. Membuka spesialisasi Sub-Class & membuka Gorgon.
* **Lantai 21 - 30 (Saga III):** Target Level Pemain $30 \rightarrow 45$. Penempaan senjata di Suaka Vulcanus hingga Tier +6.
* **Lantai 31 - 40 (Saga IV):** Target Level Pemain $45 \rightarrow 60$. Maksimasi cabang Talent Tree ganda, penempaan Tier +8.
* **Lantai 41 - 50 (Saga V):** Target Level Pemain $60 \rightarrow 80$. Status puncak, senjata Tier +10 Primordial, siap menghadapi The Grand Calligrapher.

### 5.2 Skalasi HP & Kerusakan Musuh Per Saga

| Saga | Rentang Lantai | Rata-rata HP Minion | Damage Minion | HP Bos Utama | Bahaya Lingkungan Kunci |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Saga I (Bastion)** | Lt 1 - 10 | $40 - 85$ | $6 - 12$ | $850$ (Oakhaven) / $1,800$ (Vespera) | Duri, platform retak |
| **Saga II (Foundry)** | Lt 11 - 20 | $110 - 220$ | $14 - 24$ | $3,200$ (Ignis) / $4,500$ (Slag Titan) | Kabut racun, pipa uap panas, roda gigi |
| **Saga III (Basalt)** | Lt 21 - 30 | $260 - 450$ | $26 - 38$ | $6,500$ (Golem King) / $8,200$ (Doppelgänger)| Magma cair, dinding batu titan |
| **Saga IV (Astral)** | Lt 31 - 40 | $500 - 850$ | $40 - 58$ | $9,500$ (Chimera) / $12,500$ (Malakor) | Gravitasi rendah, bulan gerhana darah |
| **Saga V (Zenith)** | Lt 41 - 50 | $900 - 1,800$ | $60 - 90$ | $15,000$ (Archon) / $25,000$ (Calligrapher)| Gelombang ombak tinta, laser void 3 fase |

---

## 6. Bestiarium Komprehensif 18 Tipe Musuh & AI FSM (Enemy Bestiary)

| ID | Nama Musuh | Bioma | HP Base | ATK | DEF | Speed | Range | Pola AI FSM & Ciri Khas Pertarungan |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **E01** | *Crimson Grunt* | Saga I | 45 | 6 | 0 | 3.2 m/s | 1.2 m | Mantan prajurit benteng; ayunan pedang 1-2 hit, mundur saat di-parry. |
| **E02** | *Amethyst Sniper* | Saga I & II | 35 | 9 | 0 | 2.5 m/s | 9.0 m | Pemanah buta Vespera; membidik 1.2s sebelum tembak; lari jika didekati. |
| **E03** | *Crimson Berserker* | Saga I | 80 | 14 | 2 | 2.8 m/s | 1.8 m | Pengawal Oakhaven ber-super armor; kapak lambat peremuk pertahanan. |
| **E04** | *Corrupted Scribe* | Saga II | 70 | 12 | 1 | 3.0 m/s | 7.5 m | Pustakawan dirasuki teks dewa; menembakkan orb tinta pelacak (*Homing*). |
| **E05** | *Amethyst Stalker* | Saga II | 60 | 16 | 0 | 5.5 m/s | 1.4 m | Mantan rekan Zephyr; berkamuflase transparan, menusuk dari belakang. |
| **E06** | *Venom Spitter* | Saga II | 55 | 10 | 1 | 2.0 m/s | 6.0 m | Hewan pelarut bermutasi; menyemburkan genangan racun ber-Poison DoT. |
| **E07** | *Obsidian Golem* | Saga III | 220 | 22 | 6 | 1.8 m/s | 2.0 m | Batu meteorit Vulcanus; kebal stagger; hantaman tinju gempa radial. |
| **E08** | *Clockwork Automaton*| Saga II & III | 140 | 18 | 3 | 4.2 m/s | 1.5 m | Robot pekerja Ignis; memutar gergaji dada beruntun 4-hit dengan cepat. |
| **E09** | *Slag Brute* | Saga III | 180 | 25 | 4 | 2.2 m/s | 2.2 m | Ksatria terkubur lahar; melemparkan bongkahan batu magma mendidih. |
| **E10** | *Void Phantom* | Saga IV | 160 | 28 | 2 | 4.5 m/s | 2.5 m | Hantu pendaki beku di ruang hampa; menyerang menukik dari sudut buta atas. |
| **E11** | *Astral Archer* | Saga IV | 120 | 32 | 1 | 3.5 m/s | 11.0 m | Pemanah pulau melayang; meluncurkan 2 panah serpihan bintang ($18\text{ m/s}$). |
| **E12** | *Echo Stalker* | Saga IV | 200 | 30 | 3 | 5.0 m/s | 1.8 m | Klon cermin hitam; meniru gerakan tebasan pemain dan mampu parry balik. |
| **E13** | *Sanguine Zealot* | Saga IV | 240 | 38 | 3 | 4.8 m/s | 1.6 m | Bangsawan kultus Malakor; tusukan rapier cepat; meledak saat mati. |
| **E14** | *Crimson Vampire Bat*| Saga IV | 110 | 26 | 0 | 6.0 m/s | 1.2 m | Kelelawar terbang lincah; setiap gigitan memulihkan 50% HP yang dicuri. |
| **E15** | *Bloodblade Knight* | Saga IV | 380 | 48 | 7 | 3.0 m/s | 2.4 m | Algojo katedral gotik; pedang eksekusi dua tangan bertebasan darah area. |
| **E16** | *Celestial Sentry* | Saga V | 450 | 55 | 8 | 4.0 m/s | 2.0 m | Siluet putih antibodi dewa; memblokir 3 serangan pertama pemain. |
| **E17** | *Temporal Archon* | Saga V | 380 | 50 | 5 | 3.2 m/s | 8.0 m | Malaikat jam dewa; memancarkan aura *Time Slow* & proyektil laser geometris. |
| **E18** | *Primordial Shadow* | Saga V | 550 | 65 | 6 | 4.6 m/s | 2.2 m | Tetesan tinta murni dewa; membelah menjadi 2 bayangan jika HP $< 30\%$. |

---

## 7. Sistem Koleksi 50 Prasasti Batu Hitam (The 50 Lore Tablets)

Setiap lantai dari Lantai 1 hingga Lantai 50 memuat 1 buah Prasasti Kuno interaktif (*LoreTablet*):
1. **Deteksi Kedekatan:** Ketika pemain berada dalam radius $1.5\text{m}$, muncul prompt interaktif `[E] / [TAP] BACA PRASASTI KUNO`.
2. **Perekaman Save Data:** Prasasti yang telah dibaca otomatis tersimpan ke dalam `saveData.unlockedLoreTablets` (terproteksi FNV-1a Salted Checksum).
3. **Penyimpanan di Story Codex:** Seluruh 50 tablet dapat dibaca ulang kapan saja di layar *Story Codex Screen* dengan tata letak Thematic Ink Pills anti-dempet.
4. **Pemberian Hadiah:** Membaca setiap tablet memberikan bonus instan $+50\text{ Gold}$ dan $+30\text{ EXP}$. Mengumpulkan ke-50 tablet membuka achievement legendaris **"Arsiparis Sejati Aethelgard"** dan gelar kehormatan *The Grand Loremaster of Dawn*.

---

## 8. Matematika Platforming & Kinematika Fisika 50 Lantai

$$\Delta t = \min(\text{Gdx.graphics.deltaTime}, 0.033\text{s})$$
$$v_{y, t+1} = \max(v_{y, t} + g \cdot \Delta t, v_{terminal})$$
$$x_{t+1} = x_t + v_x \cdot \Delta t$$
$$y_{t+1} = y_t + v_{y, t+1} \cdot \Delta t$$

* Gravitasi Standar Menara (Saga I, II, III, V): $g = -28.0\text{ m/s}^2$
* Gravitasi Rendah (Saga IV Astral): $g = -14.0\text{ m/s}^2$
* Batas Kecepatan Jatuh Maksimum: $v_{terminal} = -24.0\text{ m/s}$

> **Golden Rule Level Design 50 Lantai:** Jarak lompatan horizontal celah wajib $\le 4.0\text{ meter}$ untuk jalur utama, agar Gorgon ($D_{reach} \approx 4.47\text{m}$) dapat melompat tanpa terperangkap.

---

## 9. Arsitektur Runic Barrier & Pembuktian Matematis BFS 50 Lantai

Untuk memastikan tidak ada pemain yang terperangkap (*softlock*) pada seluruh 50 lantai:
1. Setiap lantai dimodelkan sebagai graf berarah berbobot $G = (V, E)$, di mana simpul $V$ adalah platform dan sisi $E$ adalah lompatan yang valid secara kinematika.
2. Pintu segel (*Runic Barrier*) dan Kunci Segel (*Rune Key*) diverifikasi:
   $$\text{Path}(S \rightarrow K) \land \text{Path}(K \rightarrow D) \land \text{Path}(D \rightarrow \text{Exit})$$
3. Unit test otomatis `Campaign50FloorsReachabilityTest` mengeksekusi validasi graf ini pada `./gradlew test` sebelum build rilis diperbolehkan.

---
*GDD v3.0 ini mengikat seluruh implementasi teknis kode, generator level, aset visual, audio, dan sistem data game Stickman Tower Adventure (Edisi 50 Lantai).*

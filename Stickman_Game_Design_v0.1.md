# Stickman Tower Adventure — Game Design Document (GDD) v2.0
### *Master Production & 30-Floor Canonical Campaign Edition*

> **Status Dokumen:** Master Production Locked (v2.0 Canon)  
> **Judul Game:** Stickman Tower Adventure: The Chrono-Spire of Aethelgard  
> **Genre:** 2D Side-Scrolling Action RPG Platformer & Hack-and-Slash Megastructure Odyssey  
> **Target Platform:** Android (Mobile Native) & Desktop (LWJGL3 PC Testbed)  
> **Bahasa Pemrograman:** Kotlin  
> **Framework Engine:** libGDX (OpenGL ES 2.0 / FreeType / Virtual Controller)  
> **Arsitektur Save:** Version 2 Normalized Schema dengan 64-bit Salted FNV-1a Checksum  
> **Dokumen Terkait:** [LORE_AND_WORLDBUILDING.md](file:///home/yusuf/projekan/game/game1/LORE_AND_WORLDBUILDING.md), [README.md](file:///home/yusuf/projekan/game/game1/README.md), [Alur_Pengembangan_Ekspansi_30_Lantai.md](file:///home/yusuf/projekan/game/game1/Alur_Pengembangan_Ekspansi_30_Lantai.md)

---

## 1. Visi Permainan & Pilar Ludonaratif (Game Vision & Narrative Pillars)

### 1.1 Konsep Inti (The Core Premise)
*Stickman Tower Adventure* menggabungkan aksi pertarungan jarak dekat real-time (*hack-and-slash*) berkecepatan tinggi, navigasi platforming presisi, dan kedalaman sistem RPG dalam dunia fantasi gelap yang tereduksi menjadi hitam dan putih pasca-tragedi kosmis *The Great Inking*. 

Pemain mengendalikan pendekar siluet garis hitam berinti jiwa (*Anima Core*) yang terbangun di **The Ashen Threshold (Padang Abu Luar Menara)**, dipandu oleh **Aria (Sang Gema Terakhir)** untuk mendaki **The Celestial Chrono-Spire (Menara Bayang)** setinggi **30 lantai kampanye cerita utama**, menumbangkan 6 bos legendaris penjaga babak (*Apex Gatekeepers*), menguak rahasia hilangnya spektrum warna melalui 30 Prasasti Batu Hitam (*The 30 Lore Tablets*), dan menembus batas waktu ke dalam dimensi *Endless Spire* (Lantai 31 ke atas).

```
[PROLOG: PADANG ABU] ──> [PENDAKIAN 30 LANTAI LUAS] ──> [KOMBO & PARRY] ──> [TEMPA VULCANUS] ──> [PATAHKAN PENA DEWA]
         ▲                                                                                                    │
         └────────────────────────── [ENDLESS CHRONO-SPIRE: LANTAI 31+] ◄─────────────────────────────────────┘
```

### 1.2 Enam Pilar Utama Desain (The Six Design Pillars)
1. **Kinetic Defiance (Perlawanan Kinetik Terhadap Pelarutan Jiwa):**
   Mekanik pertarungan menuntut aksi tanpa henti. Berdiam diri terlalu lama di tanah memicu kutukan gravitasi tektonik ($g = -28.0\text{ m/s}^2$). Pemain harus terus melompat, melakukan *dodge roll*, menangkis serangan secara presisi (*Perfect Parry* jendela 120ms), dan melancarkan kombo 3-hit untuk mempertahankan integritas siluet tubuhnya dari fenomena *The Dissolution*.
2. **Precision Combat & Impact Juiciness:**
   Pertarungan yang memuaskan dan berbobot (*juicy*): pemisahan ketat antara [Hitbox.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/combat/Hitbox.kt) dan [Hurtbox.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/combat/Hurtbox.kt), *Hitstop Micro-Freeze* 80 milidetik saat parry sukses, getaran haptic Android, percikan partikel neon sparks, dan angka melayang (*Floating Damage Text*).
3. **Vast Explorable Level Design (Peta Luas, Panjang & Non-Linear):**
   Setiap lantai bukan lagi koridor satu arah sempit, melainkan bentangan terstruktur sepanjang **$110\text{m} - 160\text{m}$ horizontal** dengan elevasi vertikal bertingkat **$22\text{m} - 38\text{m}$**. Mengintegrasikan pintu gerbang segel runik (*Runic Barrier Doors*), kunci runik tersembunyi (*Rune Keys*), ruang brankas rahasia (*Secret Vaults*), dan jalan pintas vertikal (*Gravitational Vents / One-Way Platforms*).
4. **Deep RPG Progression & 60-Level Growth Curve:**
   EXP leveling hingga Level 60, alokasi poin bakat di [TalentManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/talent/TalentManager.kt), penempaan senjata di Suaka Vulcanus Lantai 13 ([BlacksmithScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BlacksmithScreen.kt)), serta 6 spesialisasi Sub-Class unik (*Berserker, Guardian, Shadow Stalker, Venomancer, Archmage, Spellblade*).
5. **Roguelite Run Adaptation (Sistem Relik & Mutator Bioma):**
   Setiap pendakian menawarkan variasi relik acak di [RelicType.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/relic/RelicType.kt) (*Vampiric Fang, Thunder Charm, Wings of Hermes, Hourglass of Chronos*) dan anomali mutator lingkungan bioma (*Toxic Miasma, Low Gravity, Berserk Surge, Vampire Moon, Time Distortion*).
6. **Harmoni Total Narasi & Kode (Ludonarrative Integration):**
   Cerita tidak disajikan sebagai teks tempelan, melainkan terpatri di dalam arsitektur kode: Prolog interaktif di Padang Abu, *Story Codex Screen* 30 bab, 30 Prasasti Batu Hitam interaktif di lantai, dan dialog filosofis NPC Aria serta Vulcanus.

---

## 2. Struktur Arsitektur 30 Lantai (The 30-Floor World & Level Design)

### 2.1 Pembagian 6 Babak Agung (The Six Grand Acts)

| Babak (Act) | Lantai | Nama Bioma / Zona | Panjang Map | Vertikalitas | Bos Utama / Fitur Kunci |
| :--- | :---: | :--- | :---: | :---: | :--- |
| **Prolog** | **Intro** | The Ashen Threshold (Padang Abu) | $90\text{m}$ | $18\text{m}$ | Cutscene Bangkitnya Kaelen & Lentera Aria |
| **ACT I** | **1 - 5** | The Lower Bastion & Sunken Garrison | $110\text{m} - 140\text{m}$ | $22\text{m} - 30\text{m}$ | **Lt 5: General Oakhaven (The Iron Vanguard)** |
| **ACT II** | **6 - 10** | Crypts of Lost Chroma & Sunken Archives | $135\text{m} - 150\text{m}$ | $26\text{m} - 34\text{m}$ | **Lt 10: Lady Vespera (The Amethyst Matron)** |
| **ACT III** | **11 - 15** | Subterranean Foundry & Gilded Catacombs | $130\text{m} - 155\text{m}$ | $24\text{m} - 32\text{m}$ | **Lt 13: Suaka Vulcanus (Hub Safe Zone)**<br/>**Lt 15: Forge-Master Ignis (The Slag Colossus)** |
| **ACT IV** | **16 - 20** | The Astral Rift & Shattered Precipice | $135\text{m} - 160\text{m}$ | $28\text{m} - 38\text{m}$ | **Lt 20: The Shadow Doppelgänger (Mirror Match)** |
| **ACT V** | **21 - 25** | The Crimson Eclipse Sanctum | $135\text{m} - 155\text{m}$ | $26\text{m} - 34\text{m}$ | **Lt 25: Malakor The Crimson Sovereign (Blood Prince)** |
| **ACT VI** | **26 - 30** | The Celestial Zenith & Primordial Canvas | $120\text{m} - 160\text{m}$ | $30\text{m} - 36\text{m}$ | **Lt 30: The Grand Calligrapher (Final Deity Boss)** |
| **Endgame**| **31+** | The Endless Chrono-Spire | Prosedural | Prosedural | Boss Rush Mode & Hall of Legends |

---

### 2.2 Desain Fitur Eksplorasi Map Luas & Panjang

Untuk memastikan map panjang terasa dinamis dan tidak membosankan:
1. **Sistem Gerbang Runik & Kunci Segel (Runic Barrier System):**
   * Pintu keluar lantai (*StageExit*) di beberapa lantai (seperti Lantai 3, 8, 14, 24) dikunci oleh *Runic Barrier* berkode warna.
   * Pemain harus menelusuri rute cabang bawah tanah atau menara samping untuk mengalahkan mini-boss penjaga dan memungut *Rune Key*.
2. **Jalur Bercabang (High-Risk vs Safe Navigation Paths):**
   * **Rute Atas (The High Wire):** Platform kecil yang runtuh (*crumbling platforms*), angin puyuh, duri obsidian, namun terdapat peti emas (*Treasure Chest*) dan relik langka.
   * **Rute Bawah (The Low Road):** Koridor bata datar yang relatif aman dari jurang, namun dihuni oleh gerombolan musuh infanteri dan pemanah.
3. **Penyelamat Vertikal (One-Way Stepping Platforms & Thermal Vents):**
   * Platform satu arah tembus dari bawah (*isOneWay = true*) agar pemain yang jatuh dari lompatan tinggi tidak harus mengulang dari awal lantai.
   * Di Bioma Foundry dan Astral (Lantai 11-20), terdapat *Thermal Vents* (semburan uap/angin gravitasi) yang melontarkan karakter setinggi 12-18 meter ke tingkat map atas.
4. **Ruang Rahasia (Hidden Vaults & Blood Altars):**
   * Dinding rapuh yang dapat dirusak oleh kombo 3-hit atau jurus Ultimate untuk membuka ruang tersembunyi berisi *Lore Tablet*, koin emas kuno, atau *Blood Altar* pengorbanan.

---

## 3. Karakter Playable & Spesialisasi Sub-Class

Pemain dapat memilih salah satu dari 3 Arketipe Inti di [CharacterSelectScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/CharacterSelectScreen.kt):

| Parameter Karakter | Kaelen (Balanced) | Zephyr (Agile) | Gorgon (Heavy) |
| :--- | :--- | :--- | :--- |
| **Gelar Kanonikal** | The Bladeweaver | The Shadowstalker | The Ironbreaker |
| **Senjata Khas** | Runed Broadsword | Dual Venom Daggers | Colossal Obsidian Greatsword |
| **Kecepatan Gerak** | 7.5 m/s | 9.5 m/s | 5.8 m/s |
| **Tinggi Lompatan** | 11.5 m/s ($\approx 2.36$m) | 12.2 m/s ($\approx 2.65$m) | 10.8 m/s ($\approx 2.08$m) |
| **Damage Multiplier**| 1.00x | 0.85x | 1.55x |
| **Bonus Base DEF** | +0 DEF | -1.0 DEF | +3.5 DEF (Super Armor) |
| **Bonus Base CRIT**| 8% Base | +15% Bonus (Total 23%) | -2% Penalty (Total 6%) |
| **Skill 1 (Active)** | *Gale Crescent* (CD 3.5s) | *Shadow Shuriken* (CD 2.8s) | *Earth Shockwave* (CD 4.0s) |
| **Skill 2 (Active)** | *Rising Dragon* (CD 4.8s) | *Venom Flurry* (CD 3.8s) | *Cyclone Cleave* (CD 5.2s) |
| **Skill 3 (Active)** | *Dash Strike* (CD 4.2s) | *Phantom Blink* (CD 4.5s) | *Titan Roar* (CD 6.5s) |
| **Ultimate Jurus** | *Bladestorm 360°* (100 SP) | *Phantom Flurry* (80 SP) | *Meteor Cleave* (120 SP) |
| **Pilihan Sub-Class**| **Berserker** / **Guardian** | **Shadow Stalker** / **Venomancer** | **Archmage** / **Spellblade** |

---

## 4. Sistem Pertarungan & Mekanika Inti (Combat Mechanics)

### 4.1 Rantai Serangan Kombo 3-Hit (Combo Chain)
1. **Hit 1 (Light Slash):** Tebasan pembuka cepat (0.12s startup, 1.0x damage, knockback ringan).
2. **Hit 2 (Cross Slash):** Tebasan silang penyambung (0.14s startup, 1.25x damage, mengangkat musuh sedikit).
3. **Hit 3 (Finisher Smash):** Hantaman pemungkas berdaya hancur tinggi (0.18s startup, 1.85x damage, knockback horizontal 7.0 m/s, meremukkan pertahanan perisai musuh / *Guard Break*).

### 4.2 Sistem Tangkisan Sempurna & Elakan (Perfect Parry & Dodge Roll)
* **Dodge Roll:** Durasi 0.22 detik dengan kecepatan luncur $14.5\text{ m/s}$. Cooldown 0.65 detik. Memberikan kekebalan penuh (*Invulnerability Frames / i-frames*) terhadap seluruh proyektil dan tebasan fisik.
* **Perfect Parry (Deflect Window):** Jika pemain melakukan *Dodge Roll* tepat dalam kurun waktu **0.12 detik (7 frame)** saat serangan musuh mendarat:
  * Pemain tidak menerima damage sama sekali.
  * Memicu **Hitstop Freeze 80 milidetik** yang membekukan layar sesaat.
  * Musuh penyerang mengalami **Stun selama 1.5 detik** dengan pertahanan terbuka (*Dazed State*).
  * Menghasilkan efek audio denting baja sakral (*Parry Bell SFX*) dan kilatan partikel emas radial.
  * Memicu akumulasi kombo instan dan efek pemulihan bakat *Guardian Counter Fortitude*.

---

## 5. Kurva Progresi RPG & Pacing 30 Lantai (RPG Progression Curve)

### 5.1 Skalasi Karakter (Level 1 s/d Level 60)
Kenaikan level pemain dihitung dengan formula non-linear:

$$\text{EXP Ke Level Berikutnya}(L) = 100 \times L^{1.35}$$

* **Lantai 1 - 5 (Act I):** Target Level Pemain $1 \rightarrow 10$. Membuka seluruh 3 skill aktif.
* **Lantai 6 - 10 (Act II):** Target Level Pemain $10 \rightarrow 20$. Membuka pemilihan spesialisasi Sub-Class.
* **Lantai 11 - 15 (Act III):** Target Level Pemain $20 \rightarrow 30$. Penempaan senjata di Suaka Vulcanus hingga Tier +5.
* **Lantai 16 - 20 (Act IV):** Target Level Pemain $30 \rightarrow 40$. Maksimasi cabang Talent Tree utama.
* **Lantai 21 - 25 (Act V):** Target Level Pemain $40 \rightarrow 50$. Penempaan senjata hingga Tier +8, relik ganda.
* **Lantai 26 - 30 (Act VI):** Target Level Pemain $50 \rightarrow 60$. Status puncak, senjata Tier +10 Primordial, siap menghadapi The Grand Calligrapher.

### 5.2 Skalasi HP & Kerusakan Musuh Per Bioma

| Bioma | Rentang Lantai | Rata-rata HP Minion | Damage Minion | HP Bos Utama | Karakteristik Bahaya |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Act I (Benteng)** | Lt 1 - 5 | $40 - 75$ | $6 - 10$ | $850$ (Oakhaven) | Duri, platform retak |
| **Act II (Arsip)** | Lt 6 - 10 | $90 - 150$ | $12 - 18$ | $1,800$ (Vespera) | Kabut racun Miasma, proyektil sniper |
| **Act III (Foundry)** | Lt 11 - 15 | $180 - 320$ | $20 - 28$ | $3,500$ (Ignis) | Magma cair, roda gigi tajam |
| **Act IV (Astral)** | Lt 16 - 20 | $350 - 580$ | $30 - 42$ | $6,200$ (Doppelgänger)| Gravitasi rendah, pantulan jurus cermin |
| **Act V (Eclipse)** | Lt 21 - 25 | $650 - 950$ | $45 - 60$ | $9,800$ (Malakor) | Regenerasi bulan darah, lifesteal 20% |
| **Act VI (Zenith)** | Lt 26 - 30 | $1,100 - 1,800$ | $65 - 85$ | $16,500$ (Calligrapher)| Laser penghapus void, 3 fase kosmis |

---

## 6. Sistem Koleksi & Dokumentasi Terintegrasi (The 30 Lore Tablets)

Setiap lantai kampanye memuat 1 buah Prasasti Kuno interaktif (*LoreTablet*):
1. **Deteksi Kedekatan:** Ketika pemain berada dalam radius $1.5\text{m}$, muncul prompt interaktif `[E] / [TAP] BACA PRASASTI KUNO`.
2. **Perekaman Save Data:** Prasasti yang telah dibaca otomatis tersimpan ke dalam `saveData.unlockedLoreTablets` (terproteksi FNV-1a Salted Checksum).
3. **Penyimpanan di Story Codex:** Seluruh 30 tablet dapat dibaca ulang kapan saja di layar *Story Codex Screen* dengan tata letak Thematic Ink Pills anti-dempet.
4. **Pemberian Hadiah:** Membaca setiap tablet memberikan bonus instan $+50\text{ Gold}$ dan $+25\text{ EXP}$. Mengumpulkan ke-30 tablet membuka achievement legendaris **"Arsiparis Sejati Aethelgard"** dan gelar kehormatan *The Grand Loremaster*.

---
*GDD v2.0 ini mengikat seluruh implementasi teknis kode, generator level, aset visual, audio, dan sistem data game Stickman Tower Adventure.*

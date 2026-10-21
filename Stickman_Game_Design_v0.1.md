# Stickman Tower Adventure — Game Design Document (GDD) v1.0
### *Master Production & Canon Edition — The Grand Chronicle of Aethelgard*

> **Status Dokumen:** Master Production Locked (v1.0 Canon)  
> **Judul Game:** Stickman Tower Adventure: The Chrono-Spire of Aethelgard  
> **Genre:** 2D Side-Scrolling Action RPG Platformer & Hack-and-Slash  
> **Target Platform:** Android (Mobile Native) & Desktop (LWJGL3 PC Testbed)  
> **Bahasa Pemrograman:** Kotlin  
> **Framework Engine:** libGDX (OpenGL ES 2.0 / FreeType / Virtual Controller)  
> **Arsitektur Save:** Version 2 Normalized Schema dengan 64-bit Salted FNV-1a Checksum  
> **Sistem Tooling:** DevTools In-Game Console, BFS Reachability Graph, & Headless Monte-Carlo Balance Simulator  
> **Dokumen Terkait:** [LORE_AND_WORLDBUILDING.md](file:///home/yusuf/projekan/game/game1/LORE_AND_WORLDBUILDING.md), [README.md](file:///home/yusuf/projekan/game/game1/README.md)

---

## 1. Visi Permainan & Pilar Ludonaratif (Game Vision & Narrative Pillars)

### 1.1 Konsep Inti (The Core Premise)
*Stickman Tower Adventure* menggabungkan aksi pertarungan jarak dekat real-time (*hack-and-slash*) berkecepatan tinggi, navigasi platforming presisi, dan kedalaman sistem RPG dalam dunia fantasi gelap yang tereduksi menjadi hitam dan putih pasca-tragedi kosmis *The Great Inking*. 

Pemain mengendalikan pendekar siluet garis hitam berinti jiwa (*Anima Core*) yang mendaki **The Obsidian Spire (Menara Bayang)** setinggi 10 lantai kampanye cerita utama, menumbangkan 5 bos legendaris, menguak rahasia hilangnya spektrum warna melalui Prasasti Batu Hitam (*The Lore Tablets*), dan menembus batas waktu ke dalam dimensi *Endless Spire* (Lantai 11 ke atas).

```
[EKPLORASI PLATFORMING] ──> [KOMBO & PERFECT PARRY] ──> [JARAH GOLD & EXP] ──> [TEMPA SENJATA & BAKAT] ──> [PENDAKIAN MENARA]
         ▲                                                                                                          │
         └────────────────────────── [SIKLUS ROGUELITE & REINKARNASI ANIMA] ◄───────────────────────────────────────┘
```

### 1.2 Enam Pilar Utama Desain (The Six Design Pillars)
1. **Kinetic Defiance (Perlawanan Kinetik Terhadap Pelarutan Jiwa):**
   Mekanik pertarungan menuntut aksi tanpa henti. Berdiam diri terlalu lama di tanah memicu kutukan gravitasi tektonik ($g = -28.0\text{ m/s}^2$). Pemain harus terus melompat, melakukan *dodge roll*, menangkis serangan secara presisi (*Perfect Parry* jendela 120ms), dan melancarkan kombo 3-hit untuk mempertahankan integritas siluet tubuhnya.
2. **Precision Combat & Impact Juiciness:**
   Pertarungan yang memuaskan dan berbobot (*juicy*): pemisahan ketat antara [Hitbox.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/combat/Hitbox.kt) dan [Hurtbox.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/combat/Hurtbox.kt), *Hitstop Micro-Freeze* 80 milidetik saat parry sukses, getaran haptic Android, percikan partikel neon sparks, dan angka melayang (*Floating Damage Text*).
3. **Verified Mathematical Reachability (Platforming Bebas Softlock):**
   Seluruh lantai kampanye (Lantai 1 s/d 10) dan lantai prosedural Endless Spire divalidasi oleh algoritma [LevelReachabilityGraph.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/tools/LevelReachabilityGraph.kt). Lompatan, celah platform, penempatan peti, dan portal keluar dijamin dapat dicapai secara matematis berdasarkan amplop kinematika karakter ($H_{jump} \approx 4.5$m, $D_{reach} \approx 8.5$m).
4. **Deep RPG Progression & Sub-Class Disciplines:**
   EXP leveling, alokasi poin bakat di [TalentManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/talent/TalentManager.kt), penempaan senjata di Suaka Vulcanus ([BlacksmithScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BlacksmithScreen.kt)), serta 6 spesialisasi Sub-Class unik (*Berserker, Guardian, Shadow Stalker, Venomancer, Archmage, Spellblade*).
5. **Roguelite Run Adaptation (Sistem Relik & Mutator):**
   Setiap pendakian menawarkan variasi relik acak di [RelicType.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/relic/RelicType.kt) (*Vampiric Fang, Thunder Charm, Wings of Hermes, Hourglass of Chronos*) dan anomali mutator lingkungan (*Toxic Miasma, Low Gravity, Berserk Surge, Vampire Fog*).
6. **Harmoni Total Narasi & Kode (Ludonarrative Integration):**
   Cerita tidak disajikan sebagai teks tempelan, melainkan terpatri di dalam arsitektur kode: *Story Codex Screen* 9 bab ([StoryCodexScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/StoryCodexScreen.kt)), 10 Prasasti Batu Hitam interaktif di lantai, dan dialog filosofis NPC Aria serta Vulcanus.

---

## 2. Karakter Playable & Spesialisasi Sub-Class

Pemain dapat memilih salah satu dari 3 Arketipe Inti di [CharacterSelectScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/CharacterSelectScreen.kt) atau melakukan *hot-swap* secara langsung melalui DevTools Overlay:

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

### 2.1 Matriks Bakat & Kemampuan Sub-Class (Talent Tree)
* **Berserker (`BS_UNDYING_WILL`):** Menahan kematian fatal satu kali per lantai, memulihkan 25% HP dan memberikan lonjakan Attack Power +50% selama 6 detik.
* **Guardian (`GD_COUNTER_FORTITUDE` & `GD_AEGIS_STANCE`):** Menyerap benturan musuh saat *Perfect Parry* untuk memulihkan 15% Max HP dan memberikan kekebalan total terhadap efek stagger musuh (*Super Armor*).
* **Shadow Stalker (`SS_LETHAL_PRECISION`):** Meningkatkan Critical Chance sebesar +15% dan Critical Multiplier dari 1.6x menjadi 2.2x saat menyerang dari arah punggung lawan.
* **Venomancer (`VM_TOXIC_BLADE`):** Setiap tebasan melapisi musuh dengan racun korosif yang menghasilkan damage periodik (*Damage over Time*) dan memperlambat kecepatan gerak musuh 30%.
* **Archmage (`AM_ARCANE_CONDUIT` & `AM_MANA_SHIELD`):** Mengurangi seluruh cooldown skill sebesar 30% dan mengaktifkan perisai sihir yang menyerap 35% damage menggunakan cadangan energi Ultimate.
* **Spellblade (`SB_ELEMENTAL_SURGE`):** Tebasan pedang raksasa memicu ledakan percikan api dan sengatan petir radial sebesar 25 damage area.

---

## 3. Sistem Pertarungan & Mekanika Inti (Combat Mechanics)

```
       [INPUT SERANGAN] ──> [HITBOX AKTIF (0.15s)] ──> [OVERLAP HURTBOX MUSUH]
                                                               │
                                  ┌────────────────────────────┴────────────────────────────┐
                                  ▼                                                         ▼
                         [NORMAL MELEE HIT]                                       [PERFECT PARRY DEFLECT]
                     Damage Terkalkulasi RPG                                   Hitstop Freeze 80ms
                     Partikel Hit Sparks                                      Stun Musuh 1.5s
                     Penambahan Ultimate Energy (+8%)                         Kombo Counter + Parry Heal
```

### 3.1 Rantai Serangan Kombo 3-Hit (Combo Chain)
1. **Hit 1 (Light Slash):** Tebasan pembuka cepat (0.12s startup, 1.0x damage, knockback ringan).
2. **Hit 2 (Cross Slash):** Tebasan silang penyambung (0.14s startup, 1.25x damage, mengangkat musuh sedikit).
3. **Hit 3 (Finisher Smash):** Hantaman pemungkas berdaya hancur tinggi (0.18s startup, 1.85x damage, knockback horizontal 7.0 m/s, meremukkan pertahanan perisai musuh / *Guard Break*).

### 3.2 Sistem Tangkisan Sempurna & Elakan (Perfect Parry & Dodge Roll)
* **Dodge Roll:** Durasi 0.22 detik dengan kecepatan luncur $14.5\text{ m/s}$. Cooldown 0.65 detik. Memberikan kekebalan penuh (*Invulnerability Frames / i-frames*) terhadap seluruh proyektil dan tebasan fisik.
* **Perfect Parry (Deflect Window):** Jika pemain melakukan *Dodge Roll* tepat dalam kurun waktu **0.12 detik (7 frame)** saat serangan musuh mendarat:
  * Pemain tidak menerima damage sama sekali.
  * Memicu **Hitstop Freeze 80 milidetik** yang membekukan layar sesaat.
  * Musuh penyerang mengalami **Stun selama 1.5 detik** dengan pertahanan terbuka (*Dazed State*).
  * Menghasilkan efek audio denting baja sakral (*Parry Bell SFX*) dan kilatan partikel emas radial.
  * Memicu akumulasi kombo instan dan efek pemulihan bakat *Guardian Counter Fortitude*.

### 3.3 Sistem Pertahanan Musuh (Guard & Backstab)
Musuh tipe *Shielded Knight* memegang perisai obsidian yang memantulkan 100% serangan frontal biasa. Untuk menembusnya, pemain harus:
* Melompati musuh dan menyerang dari belakang (*Cross-up / Backstab*).
* Menggunakan Serangan Finisher Kombo 3 (*Heavy Cleave Guard Break*).
* Menggunakan Active Skill atau Ultimate.
* Melakukan *Perfect Parry* saat musuh mengayunkan gada untuk memecahkan kuda-kuda perisainya.

---

## 4. Struktur Kampanye Menara & Mode Permainan

### 4.1 Kampanye Cerita Utama (The 10-Floor Odyssey)

```
[BABAK I: INISIASI]  ──> Lantai 1 (Lower Bastion), Lantai 2 (Spikes Lab), Lantai 3 (Bos: Jenderal Oakhaven)
[BABAK II: RAHASIA]  ──> Lantai 4 (Sunken Archives), Lantai 5 (Bos: Amethyst Matron), Lantai 6 (Suaka Vulcanus)
[BABAK III: ASTRAL]  ──> Lantai 7 (Void Precipice), Lantai 8 (Bos: Doppelgänger), Lantai 9 (Bos: Crimson Sovereign)
[BABAK IV: PUNCAK]   ──> Lantai 10 (Final Story Boss: The Grand Calligrapher di The Primordial Canvas)
```

Setiap lantai dirancang secara cermat dengan tema lingkungan, arsitektur platforming, musuh khas, dan satu buah Prasasti Batu Hitam (*The Lore Tablets I s/d X*):
* **Lantai 1 s/d 3 (Act I):** Pengenalan mekanik dasar, jurang duri platforming, dan duel kehormatan melawan *Jenderal Oakhaven*.
* **Lantai 4 s/d 6 (Act II):** Arsip kaca ungu terlarang, kawah racun *Toxic Miasma* melawan *The Amethyst Matron*, dan bengkel peristirahatan aman *Vulcanus*.
* **Lantai 7 s/d 9 (Act III):** Anomali gravitasi rendah di angkasa hampa, duel psikologis melawan bayangan sendiri *The Shadow Doppelgänger*, dan kuil gerhana darah melawan *The Crimson Sovereign*.
* **Lantai 10 (Act IV):** Kanvas putih nirbatas tempat *The Grand Calligrapher* bertahta dengan pertarungan 3 fase kosmis dan pematahan pena dewa *The Primordial Stylus*.

### 4.2 Mode Tanpa Akhir (The Endless Spire — Lantai 11+)
Setelah menamatkan Lantai 10, realitas menara retak menjadi lingkaran waktu prosedural tak terbatas (*Infinite Time-Loop*):
* Generator prosedural dinamis merakit kombinasi platform, jurang, jebakan duri, dan musuh secara acak namun terverifikasi *reachable*.
* Setiap lantai menerapkan **Mutator Lingkungan Acak** di [FloorMutator.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/endless/FloorMutator.kt):
  * *Toxic Miasma:* Kabut racun lantai dasar yang mengurangi HP periodik.
  * *Low Gravity:* Gravitasi berkurang 50%, memungkinkan lompatan tinggi melayang.
  * *Berserk Surge:* Kecepatan gerak dan damage seluruh musuh meningkat +40%.
  * *Vampiric Mist:* Serangan musuh menyedot darah pemain.
  * *Gold Rush:* Musuh menjatuhkan koin emas 3 kali lipat lebih banyak.
* Setiap kelipatan 5 lantai (Lantai 15, 20, 25, dst.) menghadirkan pertarungan Bos Milestone dengan scaling HP dan damage yang semakin menantang.

---

## 5. Ekonomi Permainan, Penempaan & Perkembangan RPG

### 5.1 Kurva Leveling & Akumulasi EXP
Formula kebutuhan EXP untuk naik ke level berikutnya dikalkulasikan secara eksponensial di [CharacterStats.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/rpg/CharacterStats.kt):
$$\text{Required EXP} = \max\left(50, \left\lfloor 50 \times \text{Level}^{1.4} \right\rfloor\right)$$
Setiap kali naik level:
* Max HP meningkat $+10\%$.
* Base Attack Power meningkat $+8\%$.
* Pemain memperoleh 1 Talent Point setiap 2 level untuk membuka pohon bakat sub-class.

### 5.2 Penempaan Senjata di Suaka Vulcanus (Lantai 6 & Menu Hub)
Pemain dapat membelanjakan koin emas yang dikumpulkan dari monster dan peti untuk memperkuat senjatanya di [BlacksmithScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BlacksmithScreen.kt) hingga tingkat maksimal (Level 5):
1. **Weapon Attack (+4 ATK per level):** Biaya $= 120 \times (\text{Level} + 1)$ Gold.
2. **Critical Strike (+2.5% CRIT per level):** Biaya $= 150 \times (\text{Level} + 1)$ Gold.
3. **Attack Speed (+3.5% SPD per level):** Biaya $= 140 \times (\text{Level} + 1)$ Gold.

### 5.3 Sistem Prestasi & Gelar Juara (Achievements System)
Terintegrasi langsung di [AchievementManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/achievement/AchievementManager.kt) dengan 8 milestone bergengsi:
* *FIRST_BLOOD:* Tumbangkan musuh pertama di menara.
* *FLOOR_5_REACHED:* Capai Lantai 5 dan kalahkan Sang Ratu Busur Amethyst.
* *TOWER_CONQUEROR:* Tamatkan kampanye cerita utama (Kalahkan The Grand Calligrapher di Lantai 10).
* *COMBO_MASTER:* Raih kombo serangan berturut-turut mencapai 30x.
* *PERFECT_PARRY_MASTER:* Lakukan 20 kali Perfect Parry tanpa gagal.
* *GOLD_HOARDER:* Kumpulkan akumulasi 5.000 Gold.
* *ENDLESS_SURVIVOR:* Capai Lantai 20 di mode Endless Spire.
* *LORE_SCHOLAR:* Baca seluruh 10 Prasasti Batu Hitam di dalam menara.

---

## 6. Antarmuka Pemain, Ergonomi Kontrol & Audio

### 6.1 Tata Letak Kontrol Sentuh Mobile (Curved Ergonomics)
Diimplementasikan pada [VirtualController.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/input/VirtualController.kt):
* **Tangan Kiri (Pergerakan):** Floating D-Pad analog dengan respon kinetik presisi tinggi untuk bergerak ke kiri, kanan, menunduk, dan drop-down dari platform satu arah (*one-way platform*).
* **Tangan Kanan (Aksi Melengkung - Natural Thumb Arc):**
  * Tombol **Attack (Melee Smash)** berada di poros tengah jangkauan ibu jari (Radius 45px).
  * Tombol **Jump** dan **Dodge Roll** berada di kuadran bawah yang mudah diakses saat bereaksi cepat.
  * Tiga tombol **Active Skill (Skill 1, 2, 3)** disusun melengkung di atas tombol Attack dengan indikator visual *radial cooldown sweep*.
  * Tombol **Ultimate Jurus** berada di posisi atas khusus dengan animasi bingkai berdenyut saat energi mencapai 100%.

### 6.2 Dukungan Kontroler Fisik & Desktop
* Mendukung penuh Gamepad/Joystick berkabel dan Bluetooth (XInput / DualShock / DirectInput standard).
* Pemetaan Keyboard PC: `A/D` atau `Panah` untuk gerak, `Space` untuk lompat, `J` untuk serang, `K` untuk dash/dodge, `U/I/O` untuk Skill 1/2/3, `L` untuk Ultimate, `Tab` untuk debug bounds, dan `F1/F3/~` untuk Developer Console.

### 6.3 Desain Audio Prosedural & Feedback Haptik
* **Synthesizer Audio Prosedural:** Seluruh efek suara (*slash, hit, parry bell, dodge whoosh, level up fanfare, boss roar*) disintesis secara dinamis di [AudioManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/audio/AudioManager.kt) tanpa ketergantungan file WAV berat eksternal, menjamin performa loading instan di memori mobile.
* **Integrasi Haptik:** [HapticManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/audio/HapticManager.kt) memberikan getaran mikro berdurasi 25ms saat tebasan mengenai musuh, dan getaran tegas 60ms saat mengeksekusi *Perfect Parry*.

---

## 7. Arsitektur Teknis, Integritas Data & Developer Tools

```
[LIBGDX GAME LOOP (60 FPS)] ──> [PLAYSCREEN RENDERING] ──> [KINETIC COMBAT PIPELINE]
              │                                                        │
              ▼                                                        ▼
[ASYNC WORKER THREAD POOL]                                   [DEVELOPER TOOLS SUITE]
  ├─ Atomic File Commit (.tmp -> .json)                       ├─ Tabbed Dev Console
  ├─ Rotation Backup (.bak)                                   ├─ BFS Reachability Graph
  ├─ 64-bit Salted FNV-1a Anti-Tamper                         ├─ Monte-Carlo Balance Sim
  └─ Cloud 3-Way Conflict Merge                               └─ Asset Pipeline Validator
```

### 7.1 Sistem Penyimpanan Terproteksi (Enterprise Save Architecture)
* **Skema V2 Ternormalisasi:** Menyimpan data secara terisolasi untuk profil karakter, daftar achievement, pohon bakat, dan prasasti lore yang telah dibaca di [SaveData.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/rpg/SaveData.kt).
* **Salted 64-bit FNV-1a Checksum:** Melindungi save file dari manipulasi ilegal (*anti-cheat / anti-tamper*).
* **Multi-Slot Isolation:** Mendukung 3 slot save independen (`Slot 1, 2, 3`) dengan pembacaan metadata instan di [SaveManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/rpg/SaveManager.kt).
* **Non-Blocking Asynchronous Persistence:** Operasi penulisan I/O disk dijalankan pada daemon thread background terpisah (`saveAsync`), menjamin frame rate game tidak pernah drop saat autosave terjadi.
* **Kesiapan Cloud Sync:** Serialisasi payload Base64 ber-signature (`STK2.<payload>.<signature>`) dengan algoritma rekonsiliasi konflik 3 arah (*3-Way Deterministic Merge*).

### 7.2 Ekosistem Alat Pengembang (Developer Tools Suite)
* **Tabbed In-Game Console ([DevToolsOverlay.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/tools/DevToolsOverlay.kt)):** Antarmuka dark-obsidian neon dengan 4 tab kerja (`CHEATS`, `WARP_SPAWN`, `PROFILER_AUDIT`, `CLI_CONSOLE`).
* **Physics Timescale Controller:** Mendukung mode gerak lambat (`0.1x Slow-Mo`), percepatan (`2.0x Fast`), serta mode `PAUSE` dan `STEP 1 FRAME` (1/60 detik per klik) untuk mempermudah QA dan gameplay tuning.
* **Graph-Based Reachability Analyzer ([LevelReachabilityGraph.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/tools/LevelReachabilityGraph.kt)):** Penelusuran lintasan platform berbasis BFS untuk mencegah softlock pada seluruh lantai kampanye dan endless.
* **Headless Combat Balance Simulator ([CombatBalanceSimulator.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/tools/CombatBalanceSimulator.kt)):** Menjalankan simulasi ribuan pertempuran otomatis antar-arketipe vs bos untuk menguji winrate, time-to-kill (TTK), dan laju ekonomi emas per jam.
* **Command Line Dispatcher ([DevCommandDispatcher.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/tools/DevCommandDispatcher.kt)):** Interpreter perintah string teks yang dapat dieksekusi melalui terminal, skrip otomatis, maupun konsol HUD.

---

## 8. Verifikasi Kualitas & Standar Rilis (Quality Assurance & Production Sign-off)

* **Unit Test Automation:** Seluruh modul inti memiliki cakupan pengujian komprehensif sebanyak **92/92 Unit Tests lolos (100% Pass Rate)** pada engine Gradle test runner.
* **Target Build Android:** Kompilasi bersih tanpa *warning* kritis dengan output APK mandiri [StickmanTowerAdventure.apk](file:///home/yusuf/projekan/game/game1/StickmanTowerAdventure.apk).
* **Integritas Versi:** Kode sumber dan dokumen desain tersinkronisasi penuh pada branch utama Git repository (`origin/main`).

---
*Dokumen Game Design Document (GDD) v1.0 ini merupakan spesifikasi desain resmi dan kanon final untuk pengembangan dan produksi game **Stickman Tower Adventure**.*

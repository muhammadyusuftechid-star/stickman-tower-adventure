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

* **Unit Test Automation:** Seluruh modul inti memiliki cakupan pengujian komprehensif sebanyak **149/149 Unit Tests lolos (100% Pass Rate)** pada engine Gradle test runner.
* **Target Build Android:** Kompilasi bersih tanpa *warning* kritis dengan output APK mandiri [StickmanTowerAdventure.apk](file:///home/yusuf/projekan/game/game1/StickmanTowerAdventure.apk) dan Android App Bundle (`.aab`) untuk Google Play Store.
* **Integritas Versi:** Kode sumber dan dokumen desain tersinkronisasi penuh pada branch utama Git repository (`origin/main`).

---

## 📖 9. Panduan Gaya Penulisan & Glosarium Diegetik (Lead Writer Style Guide)

Untuk menjamin kohesi ludonaratif tingkat tinggi, seluruh teks in-game, antarmuka pengguna (UI/HUD), deskripsi item, dialog, dan catatan codex harus mematuhi nomenklatur diegetik berikut:

```
                    [TABEL TRANSISI TERMINOLOGI DIEGETIK]
┌───────────────────────────┬──────────────────────────────┬─────────────────────────────────────┐
│ Istilah Standar Video Game│ Nomenklatur Diegetik Game    │ Konsep Sastra Fiksi                 │
├───────────────────────────┼──────────────────────────────┼─────────────────────────────────────┤
│ Hit Points (HP)           │ Integritas Anima             │ Kepadatan garis siluet tubuh.       │
│ Special Points / Mana (SP)│ Resonansi Tinta              │ Energi kinetik batin untuk jurus.   │
│ Gold Currency             │ Pecahan Aethel / Tetesan Surya│ Serpihan emas kebal korupsi tinta.  │
│ Experience Points (EXP)   │ Rekoleksi Memori             │ Puing ingatan masa lampau.          │
│ Level Up                  │ Penebalan Garis Jiwa         │ Pemadatan eksistensi melawan gravitasi│
│ Checkpoint                │ Prasasti Gema Aria           │ Lentera penambat jiwa pengelana.    │
│ Game Over / Player Death  │ Pelarutan Jiwa (Dissolution) │ Garis memudar menjadi tinta mati.   │
│ Perfect Parry             │ Dentang Penolakan Takdir     │ Pemadatan instan saat benturan bilah│
│ Dodge Roll                │ Selip Bayang (Shadow Slip)   │ Mengurai massa sesaat untuk meluncur│
└───────────────────────────┴──────────────────────────────┴─────────────────────────────────────┘
```

### 9.1 Pilar Nada Bicara Karakter (Character Voice & Tone Pillars)
1. **Arkais & Puitis Minimalis:** Hindari slang modern, humor internet, atau gaya bahasa santai. Gunakan diksi yang tajam, bermartabat, dan penuh resonansi filosofis selayaknya karya sastra fantasi gelap klasik.
2. **Heroik Melankolis (*Grim-Defiant*):** Dunia telah kehilangan warnanya, jutaan jiwa telah luruh menjadi tinta mati, namun protagonis menolak meratap dalam kepasrahan. Setiap kalimat mencerminkan bara tekad yang menyala di tengah keheningan kanvas.
3. **Kontras Sensoris Tajam:** Deskripsi visual selalu menekankan kontras dramatis antara *hitam pekat tinta*, *putih kanvas kosong*, *merah darah gerhana*, dan *pijar emas bilah baja meteorit*.

---

## 🏆 10. Matriks Prestasi Naratif & Kutipan Epigraf (Narrative Achievement Matrix)

Setiap pencapaian yang terdaftar di [AchievementManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/achievement/AchievementManager.kt) bukan sekadar angka statistik, melainkan memiliki kutipan epigraf sastra (*Lore Flavor Epigraph*) yang dapat dibaca pemain di menu pencapaian:

| ID Achievement | Nama Prestasi Resmi | Syarat Teknis | Kutipan Epigraf Sastra (Lore Flavor Text) |
| :--- | :--- | :--- | :--- |
| `FIRST_BLOOD` | **Goresan Pertama di Kanvas** | Tumbangkan 1 musuh pertama | *"Ketika bilah bajamu menorehkan tinta musuh, semesta tersadar bahwa kau bukanlah sketsa pasif yang rela dihapus."* |
| `FLOOR_5_REACHED` | **Penembus Kawah Racun** | Tumbangkan Sang Ratu Busur di Lantai 5 | *"Uap Miasma membakar siluetmu, namun langkah kakimu menolak mundur. Panah sang ratu telah patah di bawah tumitmu."* |
| `TOWER_CONQUEROR` | **Patahnya Pena Takdir** | Kalahkan The Grand Calligrapher di Lantai 10 | *"Dewa pencipta telah tersungkur ke jurang kosmis. Di atas kanvas yang hening, seluruh warna Aethelgard meledak kembali!"* |
| `COMBO_MASTER` | **Tarian Tiga Puluh Garis** | Capai rentetan kombo 30x berturut-turut | *"Tebasan tanpa henti, manuver tanpa cela; waktu menahan napas menyaksikan keindahan simfoni kematianmu."* |
| `PERFECT_PARRY_MASTER` | **Baja Penantang Ajal** | Lakukan 20x Perfect Parry tanpa gagal | *"Dua puluh kali takdir mencoba merobek dadamu, dua puluh kali pula bilah bajamu menertawakan ajal dengan satu denting suci."* |
| `GOLD_HOARDER` | **Penyimpan Cahaya Surya** | Kumpulkan akumulasi 5.000 Pecahan Aethel | *"Lima ribu tetesan matahari di kantongmu; cukup untuk menyalakan kembali tungku peradaban yang telah lama membeku."* |
| `ENDLESS_SURVIVOR` | **Pengelana Paradoks Abadi** | Capai Lantai 20 di mode Endless Spire | *"Dua puluh lantai menembus dinding waktu; hukum kematian telah menyerah untuk mengejar langkah bayanganmu."* |
| `LORE_SCHOLAR` | **Pewaris Seluruh Ratapan** | Buka dan baca seluruh 10 Prasasti Batu Hitam | *"Kau telah mendengarkan ratapan seluruh batu nisan; kini masa lalu Aethelgard yang hilang berdenyut di dalam nadimu."* |

---

## 🎭 11. Desain Naratif Progresi Dinamis & Resonansi Hub (Dynamic NPC State Engine)

Sistem dialog di [StoryDialogue.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/world/StoryDialogue.kt) dan layar hub dirancang bereaksi secara dinamis terhadap kondisi pemain:

### 11.1 Dialog Khusus Berdasarkan Arketipe Karakter (Class-Specific Reactions)
* **Reaksi Vulcanus di Lantai 6:**
  * *Kepada Kaelen (Warrior):* *"Bilah pedangmu seimbang dan kokoh, kapten. Rawat keseimbangannya; hanya pedang yang setia pada pemiliknya yang sanggup menembus kanvas dewa."*
  * *Kepada Zephyr (Rogue):* *"Sepasang belati kaca amethyst? Berbahaya dan licik. Jangan sampai racunnya memakan jemarimu sendiri saat kau menusuk dari kegelapan."*
  * *Kepada Gorgon (Heavy):* *"Hahaha! Ini baru senjata! Pedang kolosal sebesar batu nisan! Dibutuhkan otot siluet berkekuatan raksasa untuk mengayunkannya. Mari kita buat ayunannya sanggup meruntuhkan pilar menara!"*

### 11.2 Dialog Khusus Tingkat Penempaan Senjata (Forge Milestones)
* **Senjata Level 1:** *"Bilahmu telah bersih dari kerak tinta lama. Kau siap menghadapi ksatria benteng bawah."*
* **Senjata Level 3:** *"Kulihat kilatan petir dan api mulai merambat di tepian bilahmu. Logam meteorit ini mulai mengenali niat jiwamu!"*
* **Senjata Level 5 (Masterwork Apex):** *"SEMPURNA! Tidak ada lagi palu yang sanggup menyentuh pedang ini. Ini bukan lagi buatan tanganku; ini adalah karya seni penentang takdir. Naiklah ke puncak, dan hancurkan pena dewa itu!"*

### 11.3 Dialog Dinamis Frekuensi Kematian (Death-Count Resilience System)
Ketika pemain gugur lebih dari 3 kali di lantai yang sama:
* **Gema Suara Aria di Checkpoint:**
  * Kematian 1–2: *"Bangkitlah, saudaraku. Kematian hanyalah jeda sesaat bagi mereka yang memiliki tekad."*
  * Kematian 3–5: *"Aku merasakan kerapatan jiwamu menipis... Tapi tataplah ke atas! Cahaya di puncak menara masih menunggumu. Tarik napas, baca ritme serangan mereka, dan tangkis pada detik yang tepat!"*
  * Kematian > 5: *"Jangan pernah membiarkan menara ini mematahkan martabatmu! Kita telah melangkah terlalu jauh dari gerbang bawah untuk menyerah di tempat ini!"*

---

## 🎯 12. Arsitektur Sistem Quest & Alur Siklus Objektif (Quest Engine Architecture)

Untuk mendukung eksplorasi terarah dan retensi pemain, sistem quest *Stickman Tower Adventure* dirancang menggunakan arsitektur modular terdesentralisasi yang terintegrasi dengan skema penyimpanan V2 [SaveData.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/rpg/SaveData.kt):

```
                        [FINITE STATE MACHINE SISTEM QUEST]
┌───────────┐    Kondisi Terbuka     ┌─────────────┐     Pemain Terima     ┌───────────────────┐
│  LOCKED   │ ─────────────────────> │  AVAILABLE  │ ────────────────────> │ ACTIVE/PROGRESS   │
└───────────┘                        └─────────────┘                       └───────────────────┘
                                                                                     │
                                           ┌─────────────────────────────────────────┴─────────────────────────────────────────┐
                                           ▼                                                                                    ▼
                                 ┌───────────────────┐                                                                ┌───────────────────┐
                                 │  OBJECTIVES MET   │                                                                │      FAILED       │
                                 └───────────────────┘                                                                └───────────────────┘
                                           │                                                                                    │
                                           ▼ Klaim Hadiah di Checkpoint                                                         ▼ Ulangi Tantangan
                                 ┌───────────────────┐                                                                ┌───────────────────┐
                                 │     COMPLETED     │                                                                │       RETRY       │
                                 └───────────────────┘                                                                └───────────────────┘
```

### 12.1 Struktur Data Model Quest (Kotlin Data Specification)
```kotlin
enum class QuestCategory {
    MAIN_ODYSSEY,    // Kampanye Cerita Utama (Lantai 1-10)
    SIDE_HERITAGE,   // Misi Sampingan Warisan Karakter (Aria, Valerius, Vulcanus)
    SCRIBE_TRIAL,    // Ujian Ketangkasan & Kehormatan Kaligrafer (Lantai 6)
    BOUNTY_HUNT      // Kontrak Perburuan Monster Elit
}

enum class ObjectiveType {
    DEFEAT_BOSS,          // Menumbangkan bos lantai tertentu
    REACH_COORDINATE,     // Mencapai koordinat platform tersembunyi
    PARRY_STREAK,         // Eksekusi Perfect Parry beruntun
    COLLECT_ITEM,         // Mengumpulkan pecahan pigmen / bijih meteorit
    UPGRADE_WEAPON,       // Menempa senjata di Suaka Vulcanus
    READ_TABLETS          // Membaca prasasti batu hitam
}

data class QuestObjective(
    val id: String,
    val type: ObjectiveType,
    val targetCount: Int,
    var currentCount: Int = 0,
    val description: String
) {
    val isComplete: Boolean get() = currentCount >= targetCount
}

data class QuestDefinition(
    val id: String,
    val title: String,
    val category: QuestCategory,
    val actNumber: Int,
    val recommendedLevel: Int,
    val objectives: List<QuestObjective>,
    val goldReward: Int,
    val expReward: Int,
    val relicReward: RelicType? = null,
    val talentPointsReward: Int = 0
)
```

### 12.2 Widget HUD Pelacak Misi (Quest Tracker Compass)
* **Desain Visual:** Berada di sudut kanan atas layar permainan (di bawah bar HP/Mana), berukuran $220 \times 60$ dp dengan latar belakang hitam transparan $70\%$ berbingkai garis emas tipis $1$ dp.
* **Informasi Ditampilkan:**
  * Ikon Kategori Quest (`◈` Emas untuk Main Quest, `◇` Cyan untuk Side Quest, `⚔` Merah untuk Bounty).
  * Judul Misi Aktif & Deskripsi Singkat Objektif.
  * Bar Progres Bertingkat (contoh: `Fragmen Pigmen: 3/4`).
  * Efek Partikel Pijar: Saat objektif terpenuhi, widget berkedip keemasan disertai denting audio lembut (*Quest Objective SFX*).

---

## 📋 13. Matriks Desain Misi Lengkap & Kondisi Pemicu (Quest Matrix)

Seluruh misi dirancang dengan amplop kinematika platforming terverifikasi dan kurva kesulitan yang seimbang:

| ID Misi | Kategori & Nama Misi | Level | Lokasi | Kondisi Pemicu (Trigger) | Rantai Objektif Tahap demi Tahap | Ganjaran (Rewards) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `MQ_1` | **Main: Bangkitnya Siluet** | Lv. 1 | Lantai 1-2 | Permainan Dimulai | 1. Ambil pedang di gerbang.<br/>2. Tumbangkan 3 Grunts.<br/>3. Baca Tablet I & II.<br/>4. Masuki portal Lantai 3. | 150 EXP, 120 Gold |
| `MQ_2` | **Main: Ujian Oakhaven** | Lv. 3 | Lantai 3 | Masuki Boss Hall | 1. Tumbangkan Jenderal Oakhaven.<br/>2. Lakukan minimal 1x Perfect Parry.<br/>3. Ambil Segel Benteng Bawah. | 400 EXP, 300 Gold, Relik Aegis |
| `MQ_3` | **Main: Benang Racun Amethyst**| Lv. 5 | Lantai 4-5 | Buka Gerbang Babak II | 1. Hindari panah di Arsip Kaca.<br/>2. Kalahkan The Amethyst Matron.<br/>3. Purifikasi kawah Miasma. | 750 EXP, 450 Gold, Unlock Sub-Class |
| `MQ_4` | **Main: Suaka Paron Logam** | Lv. 6 | Lantai 6 | Tiba di Safe Haven | 1. Temui Vulcanus.<br/>2. Tempa senjata minimal Level 1.<br/>3. Baca Wasiat Sang Penempa. | 300 EXP, Full Restore HP/Mana |
| `MQ_5` | **Main: Cermin Gema Bayang** | Lv. 8 | Lantai 7-8 | Masuki Zona Astral | 1. Navigasi Low Gravity Lantai 7.<br/>2. Kalahkan Shadow Doppelgänger.<br/>3. Tangkis serangan bayangan 2x. | 1.200 EXP, 600 Gold, Relik Sayap Hermes |
| `MQ_6` | **Main: Meredupkan Gerhana** | Lv. 9 | Lantai 9 | Masuki Altar Darah | 1. Kalahkan The Crimson Sovereign.<br/>2. Hindari serangan lifesteal bos.<br/>3. Buka tangga Kanvas Takdir. | 1.800 EXP, 900 Gold, Relik Taring Darah |
| `MQ_7` | **Main: Patahnya Pena Dewa** | Lv. 10| Lantai 10| Tiba di Singgasana | 1. Kalahkan The Grand Calligrapher.<br/>2. Hancurkan The Primordial Stylus.<br/>3. Lepaskan spektrum warna semesta. | 5.000 EXP, 2.500 Gold, Endless Spire |
| `SQ_1` | **Side: Spektrum Terakhir Aria**| Lv. 2-9| Menara | Baca Tablet I / Aria | Kumpulkan 4 Pigmen Warna (Merah di L2, Biru di L4, Hijau di L7, Emas di L9). | Pasif Regen SP +15%, Visual Aria Emas |
| `SQ_2` | **Side: Sumpah Legiun Gugur** | Lv. 2 | Lantai 1-3 | Temukan Tablet I | Cari 3 Lencana Ksatria Legiun Pertama yang gugur di benteng bawah. | 50 Max HP, +25% Spike Resistance |
| `SQ_3` | **Side: Api Penempa Bintang** | Lv. 6 | Lantai 6-7 | Tempa Senjata Lv. 3 | Kumpulkan 3 Bongkahan Meteorit Kosmis di tebing jurang gravitasi Lantai 7. | Senjata Apex Ascension, +10 Base ATK |

---

## ⚡ 14. Sistem Ujian Gauntlet & Papan Kontrak Buronan Hub (Challenge Engine)

Di Suaka Vulcanus (Lantai 6), pemain dapat menguji keterampilan bela diri mereka pada dua fasilitas khusus:

### 14.1 Monolit Ujian Para Kaligrafer (The Scribe's Gauntlets)
Sistem arena tertutup dengan aturan handicap ketat dan pemeringkatan berbasis medali:
1. **Trial of Deflection (Ujian Tangkisan):**
   * *Handicap:* Pemain tidak dapat menyerang; hanya dibekali tombol Dodge/Parry.
   * *Objektif:* Menangkis 10 proyektil dan tebasan musuh berturut-turut.
   * *Skor Medali:* Perunggu (10 Parry), Perak (15 Parry), Emas (25 Parry tanpa henti).
   * *Hadiah Utama:* Relik [CHRONOS_HOURGLASS](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/relic/RelicType.kt).
2. **Trial of the Pacewalker (Ujian Langkah Bayang):**
   * *Handicap:* Lantai arena runtuh secara progresif; jurang duri aktif di bawah.
   * *Objektif:* Capai ujung lintasan platforming 45 meter dalam waktu $< 40$ detik tanpa menyentuh duri.
   * *Hadiah Utama:* Relik [WINGS_OF_HERMES](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/relic/RelicType.kt).
3. **Trial of the Iron Will (Ujian Kehendak Baja):**
   * *Handicap:* Menghadapi bayangan ilusi Jenderal Oakhaven dan Sang Ratu Busur Amethyst sekaligus dengan HP awal terkunci pada $50\%$.
   * *Hadiah Utama:* 2 Poin Bakat (*Talent Points*) instan dan gelar kehormatan *Titan of the Spire*.

### 14.2 Papan Kontrak Buronan Elit (The Spire Bounty Board)
Papan pengumuman obsidian di dekat tungku tempa yang menerbitkan kontrak harian:
* Monster target memiliki penanda visual aura merah menyala (*Elite Champion Glow*), peningkatan HP $+50\%$, dan 1 affix kemampuan khusus (contoh: *Thorns*, *Vampiric*, *Haste*).
* Menyelesaikan kontrak buronan memberikan koin Pecahan Aethel dalam jumlah besar dan bahan material tempa khusus untuk mempercepat progresi RPG pemain.

---

## 🏛️ 15. Pedoman Penceritaan Lingkungan & Arsitektur Menara (Environmental Storytelling)

Untuk menjamin bahwa setiap level bukan sekadar tumpukan balok platforming mekanis melainkan situs sejarah peradaban yang bernyawa, Level Designer wajib menerapkan pedoman visual dan penempatan prop berikut:

```
                  [ARSITEKTUR VERTIKAL MENARA DARI BAWAH KE ATAS]
┌───────────────┬───────────────────────────┬─────────────────────────────────────────────────┐
│ Babak Menara  │ Palet Visual & Tekstur    │ Prop & Elemen Penceritaan Lingkungan            │
├───────────────┼───────────────────────────┼─────────────────────────────────────────────────┤
│ Act I (L1-3)  │ Abu-abu Obsidian & Karat  │ Rantai putus raksasa, patung Solaria terpenggal,│
│               │ Duri Hitam Lancip         │ genangan noda tinta mati, prasasti batu nisan.  │
├───────────────┼───────────────────────────┼─────────────────────────────────────────────────┤
│ Act II (L4-6) │ Ungu Gelap & Jingga Tempa │ Rak buku kaca retak, tabung alkimia bocor,      │
│               │ Kristal Amethyst Transparan│ paron meteorit raksasa, bara api emas berhamburan│
├───────────────┼───────────────────────────┼─────────────────────────────────────────────────┤
│ Act III (L7-9)│ Hitam Indigo & Merah Darah│ Dinding robek berlubang ke jurang kosmis hampa, │
│               │ Bebatuan Melayang Astral  │ altar marmer berlumur darah, gerhana bulan merah│
├───────────────┼───────────────────────────┼─────────────────────────────────────────────────┤
│ Act IV (L10)  │ Putih Marmer Primordial   │ Platform putih tanpa retakan, kuas emas raksasa │
│               │ Nirbatas Monokrom         │ membeku di udara, samudra tinta kosmis di bawah │
└───────────────┴───────────────────────────┴─────────────────────────────────────────────────┘
```

### 15.1 Hierarki Penceritaan Visual (Visual Narrative Hierarchy)
1. **Latar Belakang (Layer Background Parallax):** Menampilkan siluet reruntuhan kota marmer masa lalu di lantai bawah yang perlahan berganti menjadi pemandangan jurang bintang mati dan angkasa hampa saat mendaki ke atas.
2. **Platform & Rintangan (Midground Gameplay):** Kerapatan materi platform merefleksikan usia geologis menara: balok batu kasar berlumut hitam di Lantai 1, kristal kaca ungu tipis di Lantai 4, batuan mengambang melayang di Lantai 7, dan marmer suci putih sempurna di Lantai 10.
3. **Pencahayaan & Atmosfer Partikel (Foreground Lighting):** Partikel kabut hitam debu tinta di Lantai 1-2, uap racun hijau-ungu di Lantai 5, percikan api tempa di Lantai 6, dan kilatan neon keemasan saat memicu checkpoint prasasti gema.

---

## ⚖️ 16. Matriks Konsistensi Kanon Sistem Permainan (System-to-Lore Cohesion Rules)

Setiap parameter gameplay, formula numerik, dan batasan mekanik memiliki pembenaran ontologis yang tidak terbantahkan di dalam fiksi Aethelgard:

| Mekanik Teknis Game | Parameter Sistem | Penjelasan Ontologis Kanonikal (Loremaster Mandate) |
| :--- | :--- | :--- |
| **Batas Tinggi Lompat** | $H_{jump} \approx 2.36 - 2.65$m | Pasak gravitasi tektonik Calligrapher ($g = -28.0\text{ m/s}^2$) menekan siluet agar tidak terbang bebas. |
| **Jendela Perfect Parry**| 120 milidetik (7 frame) | Frekuensi resonansi alami baja meteorit saat memicu pemadatan kuantum tegangan permukaan tinta. |
| **Hitstop Micro-Freeze** | 80 milidetik | Waktu lokal membeku sesaat di sekitar titik benturan akibat pelepasan energi kinetik murni tanpa perantara massa biologis. |
| **Dodge Roll I-Frames** | 200 milidetik invulnerable| Karakter mengurai kerapatan garis siluetnya sesaat (*Phase Shift*) sehingga proyektil dan tebasan menembus kehampaan. |
| **EXP Level-up Curve** | $\text{EXP} = \lfloor 50 \times L^{1.4} \rfloor$ | Semakin tebal garis siluet, semakin banyak energi rekoleksi memori masa lalu yang dibutuhkan untuk memadatkan inti jiwa. |
| **Endless Spire Mutator**| Toxic, Low Grav, Berserk | Pecahnya *The Primordial Stylus* merobek tatanan hukum fisika menara menjadi paradoks dimensi yang berputar tanpa kendali. |
| **Save Checksum FNV-1a**| 64-bit Salted Hash | "Segel Takdir" spiritual yang memastikan catatan jiwa pengelana tidak dirusak oleh korupsi tinta liar. |

---

## 📚 17. Spesifikasi Teknis Sistem Story Codex (Interactive Story Codex Architecture)

Diimplementasikan pada [StoryCodexScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/StoryCodexScreen.kt), fitur ini bertindak sebagai ensiklopedia interaktif in-game yang dibuka setelah pemain mengalahkan The Grand Calligrapher di Lantai 10:

```
[TAMAT LANTAI 10] ──> [FLAG UNLOCKED DI SAVEDATA] ──> [STORY CODEX SCREEN TERBUKA DI MAIN MENU]
                                                                  │
              ┌───────────────────────────────────────────────────┼───────────────────────────────────────────────────┐
              ▼                                                   ▼                                                   ▼
     [9 BAB KANONIKAL]                                   [PAGINASI PER HALAMAN]                              [SISTEM AUDIO BARK]
  Prolog s/d Epilog Endless                           Tipografi FreeType Lembut                           Denting Lonceng & Haptik
```

### 17.1 Struktur Data & Navigasi 9 Bab Kanonikal
* **Bab 0:** Prolog & Kosmologi Aethelgard (*Zaman Keemasan, The Great Inking, Kutukan Gravitasi*).
* **Bab 1:** Babak I: Ujian Inisiasi (*Benteng Bawah & Duel Kehormatan Jenderal Oakhaven*).
* **Bab 2:** Babak II: Rahasia Kaca Hitam (*Arsip Tenggelam & Kawah Racun Ratu Busur Amethyst*).
* **Bab 3:** Babak II: Suaka Penempa Logam (*Tungku Api Emas Vulcanus & Rahasia Besi Meteorit*).
* **Bab 4:** Babak III: Pendakian Astral (*Tebing Angkasa Retak & Anomali Gravitasi Rendah*).
* **Bab 5:** Babak III: Balairung Cermin Bayang (*Konfrontasi Melawan The Shadow Doppelgänger*).
* **Bab 6:** Babak III: Kuil Gerhana Darah (*Tragedi Pangeran Belati Darah The Crimson Sovereign*).
* **Bab 7:** Babak IV: Puncak Kanvas Takdir (*Konfrontasi Pamungkas Melawan The Grand Calligrapher*).
* **Bab 8:** Epilog & Paradoks Abadi (*Patahnya Pena Semesta & Terbukanya Endless Spire*).

### 17.2 Spesifikasi Teknis Rendering & Tipografi
* **Viewport Adaptif:** FitViewport $1280 \times 720$ yang diskalakan secara presisi untuk semua rasio layar smartphone (16:9, 18:9, 20:9).
* **Tipografi Lembut Anti-Aliased:** Font FreeType definisi tinggi [FontManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/ui/FontManager.kt) dengan perataan teks kiri-kanan (*word wrap* pada lebar 780px).
* **Audio & Haptic Feedback:** Efek audio denting lonceng emas (*Parry Bell SFX*) saat membalik bab, dan getaran haptic mikro 25ms di perangkat Android untuk memberikan sensasi fisik membuka lembaran kitab kuno.

---

## 💬 18. Arsitektur Engine Dialog & Protokol Audio Vokal (Dialogue Engine Architecture)

Diimplementasikan pada [StoryDialogue.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/world/StoryDialogue.kt) dan dirender di atas lapisan gameplay [PlayScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/PlayScreen.kt):

```
                   [PROXIMITY TRIGGER: JARAK PEMAIN < 3.0m]
                                      │
                                      ▼
                      [POPUP BUBBLE & TIKIK KOMA TYPEWRITER]
                       Kecepatan: 35 Karakter/Detik
                       Voice Blip: Sintesis Gelombang Audio Prosedural
                                      │
     ┌────────────────────────────────┴────────────────────────────────┐
     ▼                                                                 ▼
[TOUCH / TOMBOL ACTION TAP]                                    [AKHIR PERCAKAPAN]
Lanjut ke Baris Berikutnya (advance)                           isTriggered = true
Animasi Fade-in Teks Lembut                                    Kamera Reset ke Posisi Pemain
```

### 18.1 Spesifikasi Kotak Dialog & Palet Warna Pembicara
* **Dimensi Kotak Dialog:** Kotak marmer hitam transparan ($85\%$ opacity) dengan lebar $900$ dp dan tinggi $130$ dp, berjarak $40$ dp dari tepi bawah layar, dihiasi bingkai garis runik setebal $1.5$ dp.
* **Palet Warna Nama Pembicara (Speaker Name Color Palette):**
  * `Aria` $\rightarrow$ **Color.CYAN** (`#00FFFF`) — Pendar gema cahaya penuntun.
  * `Jenderal Oakhaven` $\rightarrow$ **Color.GOLD** (`#FFD700`) — Kehormatan militer ksatria Solaria.
  * `The Amethyst Matron` $\rightarrow$ **Purple Amethyst** (`#D973FF`) — Kaca kristal racun ungu.
  * `Vulcanus` $\rightarrow$ **Bright Amber Flame** (`#FF9933`) — Kobaran api tungku meteorit abadi.
  * `The Shadow Doppelgänger` $\rightarrow$ **Spectral Lavender** (`#9999E6`) — Ilusi cermin jiwa gelap.
  * `The Crimson Sovereign` $\rightarrow$ **Deep Blood Crimson** (`#FF3333`) — Tinta darah gerhana vampirik.
  * `The Grand Calligrapher` $\rightarrow$ **Pristine Celestial White** (`#F2F5FF`) — Kanvas purba para dewa.

### 18.2 Protokol Suara Vokal Siluet (Procedural Voice Chirp Synthesis)
Karena tubuh siluet tidak lagi memiliki paru-paru dan pita suara biologis berdaging, komunikasi vokal disintesis secara prosedural di [AudioManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/audio/AudioManager.kt) menggunakan getaran osilator gelombang murni saat setiap karakter huruf muncul di layar:
* **Aria:** Nada tinggi lembut gelombang sinus ganda ($880\text{ Hz} - 1046\text{ Hz}$) bernuansa lonceng kristal.
* **Vulcanus:** Nada rendah menggelegar gelombang segitiga kasar ($120\text{ Hz} - 180\text{ Hz}$) menyerupai dengung logam panas.
* **The Grand Calligrapher:** Frekuensi harmonik ganda ($55\text{ Hz} + 440\text{ Hz}$) dengan resonansi gema kosmis tak terbatas.

---

## 🌿 19. Pohon Percabangan Dialog Interaktif & Resonansi Moril (Branching Trees)

Saat berinteraksi dengan NPC di Suaka Vulcanus (Lantai 6), pemain disajikan pilihan dialog bercabang (*Branching Dialogue Choice*) yang memicu efek mekanis sementara (*Morale Surge Buff*):

```
                                [PERCAKAPAN DENGAN VULCANUS]
                                "Untuk apa kau mengangkat pedang ini?"
                                                │
                ┌───────────────────────────────┴───────────────────────────────┐
                ▼                                                               ▼
     [PILIHAN A: PEMBALASAN DENDAM]                                  [PILIHAN B: RESTORASI WARNA]
     "Untuk menghancurkan dewa yang                                  "Untuk mengembalikan keindahan
      merampas wujud manusia kita!"                                   dan kehidupan semesta Aethelgard!"
                │                                                               │
                ▼                                                               ▼
     [RESPON VULCANUS: API BARA]                                     [RESPON VULCANUS: HARMONI BAJA]
     "Bagus! Kemarahan adalah bahan bakar                            "Jiwa yang mulia... Pedangmu akan
      terbaik untuk membelah takdir!"                                 menjadi lentera di tengah kegelapan!"
                │                                                               │
                ▼                                                               ▼
     [BUFF: KINETIC SURGE]                                           [BUFF: SPIRITUAL WARD]
     +8% Base Attack Power di Lantai 7                               +15% Max HP & +1 HP/s Health Regen di Lantai 7
```

*Sistem percabangan ini memperkaya kedalaman RPG dan memberikan dampak naratif langsung pada performa pertarungan pemain.*

---

## 🎵 20. Arsitektur Audio Master BGM Prosedural & Pipeline Post-Processing VFX (Fase 20)

Untuk memperkuat imersi atmosferik dunia monokrom Aethelgard tanpa membebani ukuran file instalasi dengan rekaman WAV/MP3 raksasa, seluruh lanskap audio latar dan efek visual post-processing diproses secara dinamis di [ProceduralMusicEngine.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/audio/ProceduralMusicEngine.kt) dan [PlayScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/PlayScreen.kt):

### 20.1 Sintesis Musik Latar Dinamis (Dynamic Adaptive Soundtrack)
Engine audio menyintesis gelombang frekuensi osilator multi-channel secara real-time yang berevolusi mengikuti tahapan pendakian:
1. **Act I: Benteng Bawah (Lantai 1-3):** Deringan bas rendah dan ritme perkusi terisolasi, mencerminkan reruntuhan kota kuno yang beku.
2. **Act II: Arsip Alkimia Kaca (Lantai 4-6):** Harmoni kristal nada tinggi bergaung misterius dengan efek modulasi frekuensi mirip desis uap racun.
3. **Act III: Tebing Astral & Gerhana (Lantai 7-9):** Ritme tempo cepat dengan disonansi nada kosmis yang memicu ketegangan psikologis.
4. **Act IV: Singgasana Kanvas Primordial (Lantai 10):** Komposisi orkestra megah bertempo penuh saat konfrontasi melawan Sang Kaligrafer Agung.
5. **Endless Spire:** Aransemen poliritmik prosedural paradoks waktu yang tidak pernah berulang sama.
6. **Boss Rush Mode (Gauntlet of Sovereigns):** Ketukan perkusi perang intensif tanpa henti yang memompa adrenalin pemain.
7. **Suaka Vulcanus (Lantai 6 & Hub):** Irama hangat melankolis berpadu dengan dentingan paron pelan di kejauhan.

### 20.2 Pipeline Post-Processing Visual Effects
* **Hitstop Micro-Freeze (80ms):** Menghentikan pembaruan posisi aktor selama 80 milidetik saat *Perfect Parry* atau tebasan finisher mendarat, memberikan bobot kinetik yang kuat (*juiciness*).
* **Screen Flash (0.15s):** Kilatan layar putih monokrom sesaat yang memudar halus saat skill pemungkas dilepaskan.
* **Critical Anima Vignette:** Gradasi bayangan merah pekat berdenyut di keempat sudut layar ketika HP ksatria berada di bawah $25\%$.
* **Kinetic Camera Shake:** Amplitudo getaran kamera proporsional dengan besaran damage yang diterima atau dampak tumbukan meteorit.

---

## 🕊️ 21. Sistem Roh Pendamping Tinta / Ink Familiar Companions (Fase 21)

Diimplementasikan pada [InkFamiliar.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/companion/InkFamiliar.kt) dan [FamiliarType.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/companion/FamiliarType.kt), fitur ini memberikan pendamping spiritual terbang yang melayang di samping bahu ksatria dengan kinematika pegas peredam (*Spring-Damper Kinematics*, $k = 6.5\text{ m/s}$):

```
       [KSATRIA BERGERAK] ──> [TARGET KINEMATIKA POSISI BAHU]
                                       │
                                       ▼
                     [INTERPOLASI LERP SPRING-DAMPER]
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
       [AURA PASIF AKTIF]                            [TRIGGER SERANGAN SPESIAL]
     Bonus Stat & Magnetik                       Kombo 10x / Tebasan / Parry
```

### 21.1 Tiga Roh Pendamping Kanonikal
1. **Gema Lentera Aria (`ARIA_WISP`):**
   * *Syarat Buka:* Terbuka sejak awal pendakian (Lantai 1).
   * *Kemampuan Pasif:* Magnetik emas Aethel (+20% radius daya tarik dan perolehan koin).
   * *Serangan Spesial:* Menembakkan proyektil *Prismatic Spark* (16 DMG) ke musuh terdekat setiap kelipatan 10 kombo.
   * *Palet Visual:* Pendar Cyan Kristal (`#59F2D9`) berpadu Emas Suci (`#FFD94D`).
2. **Bara Suci Vulcanus (`VULCAN_SPARK`):**
   * *Syarat Buka:* Terbuka setelah mengunjungi Suaka Vulcanus di Lantai 6.
   * *Kemampuan Pasif:* Memberikan peluang 25% pembakaran bara api (*Molten Burn*, +14 DMG biasa / +20 DMG bos) pada setiap serangan tebasan pedang.
   * *Palet Visual:* Kobaran Jingga Api (`#FF611F`) dan Kuning Bara (`#FFE040`).
3. **Bayangan Hantu Astral (`PHANTOM_SHADOW`):**
   * *Syarat Buka:* Terbuka setelah menundukkan Doppelgänger di Lantai 8.
   * *Kemampuan Pasif:* Memicu tebasan bayangan radial *Echo Slash* (+24 AoE DMG) seketika saat pemain berhasil melakukan *Perfect Parry*.
   * *Palet Visual:* Ungu Mistis Astral (`#AD38F2`) dan Merah Delima (`#F22E47`).

### 21.2 Quick Selector & Integrasi HUD
Pemain dapat beralih di antara roh yang telah terbuka kapan saja melalui menu jeda (*Pause Menu Quick Selector*) tanpa kehilangan progres pertarungan. Status roh aktif ditampilkan di pojok kiri atas HUD di bawah bilah status karakter.

---

## 🔮 22. Sistem Transmutasi Rune Kuno & Altar Pengorbanan Anima (Fase 22)

### 22.1 Soket Rune Senjata di Suaka Vulcanus
Setiap senjata ksatria memiliki **2 Soket Rune Kuno** yang dapat dipasang di [BlacksmithScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BlacksmithScreen.kt) menggunakan batu rune yang dijatuhkan oleh para bos menara di [RuneType.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/rune/RuneType.kt):

| Tipe Rune | Simbol | Asal Drop Bos | Efek Stat Ksatria |
| :--- | :---: | :--- | :--- |
| **Rune Surya Vulcanus** (`RUNE_SOLAR`) | `☼` | Penempaan Awal | +8 Bonus ATK Senjata Langsung |
| **Rune Rembulan Aria** (`RUNE_LUNAR`) | `☽` | Bos Tower Guardian (L3) | +10% Damage Taken Reduction (Maksimal Cap 40%) |
| **Rune Badai Kael** (`RUNE_TEMPEST`) | `⚡` | Bos Amethyst Matron (L5) | +15% Cooldown Reduction Skill & Dodge (Cap 45%) |
| **Rune Palung Obsidian** (`RUNE_ABYSS`) | `☯` | Bos Grand Calligrapher (L10) | +8% Critical Chance & +4% Vampiric Lifesteal |

### 22.2 Altar Pengorbanan Anima (Blood Altar)
Monolit obsidian kuno setinggi 2.4 meter yang muncul secara prosedural di Lantai 4, Lantai 7, dan setiap kelipatan 3 lantai di Endless Spire melalui [BloodAltar.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/world/BloodAltar.kt):
* **Biaya Pengorbanan:** Pemain harus mempersembahkan **25% Anima HP saat ini** (dijamin aman anti-kill, minimal tersisa 1 HP).
* **Pilihan Tiga Pakta Temporal Eksklusif:**
  1. *Pakta Pembantaian (`CARNAGE`):* +40% Critical Damage, namun pertahanan fisik ksatria berkurang 15%.
  2. *Pakta Ketamakan (`GREED`):* +60% Pecahan Aethel Gold dari musuh, namun musuh bergerak 20% lebih agresif.
  3. *Pakta Kecepatan Jiwa (`SWIFTNESS`):* +35% CDR skill & dodge, namun seluruh pemulihan HP (lifesteal, parry heal) diblokir selama berada di lantai aktif.

---

## 👑 23. Mode Gauntlet Para Raja (Boss Rush) & Balairung Sang Legenda (Fase 23)

Diakses melalui [GauntletScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/GauntletScreen.kt) atau tombol pintas di menu utama, mode ini menghadirkan ujian murni keterampilan bertarung menghadapi kelima penguasa menara secara beruntun tanpa henti di [BossRushManager.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/bossrush/BossRushManager.kt):

### 23.1 Alur Gauntlet 5 Bos Maraton
1. **Tahap 1:** Jenderal Oakhaven (Benteng Bawah — Lantai 3)
2. **Tahap 2:** The Amethyst Matron (Kawah Alkimia Kaca — Lantai 5)
3. **Tahap 3:** The Shadow Doppelgänger (Balairung Cermin Bayang — Lantai 8)
4. **Tahap 4:** The Crimson Sovereign (Kuil Gerhana Darah — Lantai 9)
5. **Tahap 5:** The Grand Calligrapher (Singgasana Kanvas Primordial — Lantai 10)

*Pemulihan Parsial:* Setelah tiap bos dikalahkan, transisi jeda 2.5 detik memulihkan $+35\%$ Anima HP pemain sebelum bos berikutnya diterjunkan ke arena.

### 23.2 Sistem Evaluasi Waktu & Medali Peringkat (BossRushRank)
Waktu tempuh dihitung menggunakan timer stopwatch presisi millidetik zero-heap:
* **Rank S (Grandmaster Kinetik):** Waktu $\le 180$ detik ($3:00$) $\rightarrow$ Hadiah 1.000 Gold + Gelar Kehormatan `TITEL_CHRONO_SLAYER` (*Simbol: ★ S ★*, Emas Bercahaya).
* **Rank A (Veteran Penantang Menara):** Waktu $\le 270$ detik ($4:30$) $\rightarrow$ Hadiah 500 Gold + Gelar Kehormatan `TITEL_SOVEREIGN_BANE` (*Simbol: ◆ A ◆*, Cyan Kristal).
* **Rank B (Ksatria Penyintas):** Berhasil menamatkan seluruh 5 bos $\rightarrow$ Hadiah 250 Gold (*Simbol: ▲ B ▲*, Hijau Zamrud).

### 23.3 Empat Gelar Kehormatan Ksatria Aethelgard (Knight Titles)
Gelar yang terbuka dapat dipasang pemain melalui layar [BossRushScreen.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/screen/BossRushScreen.kt) dan akan dipajang dengan warna khas di atas kepala siluet ksatria pada HUD gameplay:
1. `TITEL_NOVICE` — **"Pendaki Pemula"** (Gelar bawaan bagi seluruh ksatria siluet).
2. `TITEL_SOVEREIGN_BANE` — **"Bencana Para Raja"** (Diberikan atas kemenangan Gauntlet dengan Peringkat A).
3. `TITEL_CHRONO_SLAYER` — **"Penebas Sang Waktu"** (Dianugerahkan kepada petarung agung berperingkat S di bawah 3 menit).
4. `TITEL_SPIRE_CONQUEROR` — **"Penakluk Puncak Menara"** (Dianugerahkan kepada penyintas lantai 20+ Endless Spire).

---

*Dokumen Game Design Document (GDD) v1.0 ini merupakan spesifikasi desain resmi dan kanon final untuk pengembangan dan produksi game **Stickman Tower Adventure**.*






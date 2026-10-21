# 🗺️ Roadmap & Rekomendasi Strategis Pengembangan
## **Stickman Tower Adventure**

Dokumen ini memuat panduan, rekomendasi teknis, dan peta jalan fitur (*strategic roadmap*) untuk mentransformasi purwarupa **Stickman Tower Adventure** menjadi game aksi platformer RPG komersial yang kaya konten, adiktif, dan memiliki replay-value tinggi.

> 📖 **Rencana Kerja & Checklist Dekomposisi Lengkap:** [Alur_Pembuatan_Aplikasi.md](file:///home/yusuf/projekan/game/game1/Alur_Pembuatan_Aplikasi.md)

---

## 📌 Ringkasan Fondasi Saat Ini (Fase 1–9 Selesai)
* **Fisika & Platforming:** Kinematic Box2D-style physics, Raycast grounding, One-way platform, Double jump, Wall slide, Dash.
* **Sistem Tempur & Haptic:** 3-Hit combo chain, Launcher attack, Downward Meteor Cleave, Dodge roll ber-i-frames, Skil unik & Ultimate burst, Hitstop, Dynamic screen shake, serta Android Haptic Feedback (Light, Medium, Heavy vibration).
* **Varian Musuh & Bos:** Musuh Melee (Crimson Daggers), Musuh Ranged (Amethyst Archer dengan kiting AI & proyektil busur), Windup telegraphing, Stun state, Bos 3 lantai dengan Phase 2 Rage Mode, Minion Summoning, dan Wind Slash projectiles.
* **Arketipe Karakter:**
  * ⚔️ **Warrior:** Seimbang, Meteor Cleave, Whirlwind Slash.
  * 🗡️ **Assassin:** Cepat, Shadow Dash, Teleport Backstab, High Critical.
  * 🔮 **Mage:** Jarak jauh, Magic Barrier, Arcane Meteor Storm.
* **Sistem RPG & Progresi:** Leveling (Max Lv. 20), Alokasi stat (ATK, DEF, HP, SPD, CRIT), Perolehan Gold & EXP, Auto-save multi-slot (Desktop JSON + Android SharedPreferences).
* **UI/UX Anti-AI Slop:** Viewport matrix separation (16x9m gameplay vs 1280x720 UI), Tipografi HD FreeType (`NotoSans-Bold.ttf`), Segmented 5-pip stat meters dengan badge tier S/A/B/C, Health bar berlapis dengan Amber Ghost bar, Runic Altar dengan orbital motes.
* **Lintas Platform:** Desktop (LWJGL3) dan Android (Touch D-pad, Action Buttons, Hardware Back key capture, 60 FPS zero-allocation particle pooling, standalone .apk packaging).

---

## ⚔️ 1. Kedalaman Combat & Mekanik Permainan (*Combat Juiciness*)

### 1.1 Sistem Parry & Perfect Deflect (*Just-Frame Guard*)
* **Mekanisme:** Menekan tombol bertahan/dodge pada *window* 0.12 detik (7–8 frame) sebelum serangan musuh mendarat.
* **Efek Keberhasilan:**
  * Dentingan logam (*metallic clank*) audio prosedural dan percikan partikel emas.
  * Musuh mengalami *Guard Break* / pingsan (*stun*) selama 1.5 detik.
  * Pemain mendapat *buff* +25% ATK untuk serangan balasan berikutnya dan +35% energi Ultimate.
* **Tujuan:** Menciptakan *high skill ceiling* bagi pemain hardcore.

### 1.2 Varian Musuh Baru
* **🏹 Ranged Enemy (Skeleton Archer / Shadow Cultist):**
  * Menyerang dari jarak jauh menggunakan proyektil berkecepatan moderat.
  * Memaksa pemain bergerak dinamis, memanfaatkan platforming vertikal dan *dodge roll* untuk mendekat.
* **🦇 Aerial Enemy (Dungeon Bat / Harpy):**
  * Bergerak zig-zag di udara dan menukik menyerang pemain.
  * Melatih pemanfaatan combo udara: *Launcher Attack* $\rightarrow$ *Air Combos* $\rightarrow$ *Downward Meteor Cleave*.
* **🛡️ Armored Vanguard (Musuh Berperisai Besi):**
  * Membawa perisai besar yang kebal terhadap serangan frontal biasa.
  * Mewajibkan pemain berguling ke belakang musuh (*backstab*) atau mengeksekusi serangan berat (*Heavy Slash*) untuk menghancurkan perisainya.

### 1.3 Jebakan & Bahaya Interaktif Menara (*Hazards & Traps*)
* **Crumbling Ledges:** Pijakan yang bergetar dan runtuh 0.8 detik setelah diinjak pemain.
* **Floor Spikes:** Duri lantai berkala yang keluar-masuk sesuai ritme waktu tertentu.
* **Pendulum Blades:** Pisau berayun di koridor sempit yang mengharuskan presisi *dash* atau *slide*.

---

## 💎 2. Progresi RPG & Replayability (*Meta-Progression & Roguelite*)

### 2.1 Toko Blacksmith & Relic Merchant (Lobby Spire) — ✅ Selesai (Fase 10)
Mengoptimalkan perolehan **Gold** yang saat ini didapat dari membunuh musuh dan membuka peti:
* **Upgrade Senjata Permanen (Blacksmith Forge):**
  * *Sharpening Stone:* Meningkatkan Flat ATK (+4 per tingkat, max tier 5).
  * *Hollow Edge:* Meningkatkan Critical Chance (+2.5% per tingkat, max tier 5).
  * *Lightweight Hilt:* Meningkatkan kecepatan tebasan & gerak (*Attack & Movement Speed* +3.5% per tingkat).
* **Relik Pasif Acak (Dapat Dikoleksi Tiap Run):**
  * 🩸 *Vampiric Fang:* Memulihkan 4% Max HP setiap kali menumbangkan musuh.
  * ⚡ *Thunder Charm:* Menyambar musuh terdekat dengan kilat setiap kali kombo menyentuh 10x.
  * 🪶 *Wings of Hermes:* Memberikan kemampuan *Triple Jump*.
  * ⏳ *Hourglass of Chronos:* Mengurangi cooldown skill sebesar 25%.

### 2.2 Mode Menara Tanpa Batas (*Endless Ascent*)
* Terbuka setelah pemain berhasil menyelesaikan Lantai 3 untuk pertama kali.
* Menara menghasilkan lantai tanpa batas dengan tingkat kesulitan berskala eksponensial.
* Setiap lantai memiliki **Mutator Acak**:
  * *Toxic Miasma:* Darah berkurang 1% per 3 detik, namun perolehan EXP meningkat 2x lipat.
  * *Berserk Surge:* Musuh bergerak dan menyerang 40% lebih cepat, namun drop Gold meningkat 3x lipat.
  * *Low Gravity:* Lompatan 50% lebih tinggi dan melayang.

### 2.3 Talent Tree & Sub-Class Tiap Arketipe
* **Warrior:**
  * Jalur A: *Berserker* (Semakin sekarat darah, ATK & Attack Speed meningkat drastis).
  * Jalur B: *Guardian* (Memiliki Super Armor dan shield mitigasi damage).
* **Assassin:**
  * Jalur A: *Shadow Stalker* (Fokus teleportasi di belakang musuh & 100% crit saat backstab).
  * Jalur B: *Venomancer* (Setiap tebasan menumpuk racun *Damage-over-Time*).
* **Mage:**
  * Jalur A: *Archmage* (Area ledakan sihir luas dan cooldown ulti cepat).
  * Jalur B: *Spellblade* (Tebasan pedang berimbun elemen api/es jarak dekat).

---

## 🎨 3. Audio-Visual Immersion & "Game Feel" (*Juiciness*)

### 3.1 Animasi Kartu Masuk Bos (*Boss Title Card Intro*)
* Saat pemain memasuki arena bos lantai:
  * Kamera melakukan *cinematic lerp zoom-in* ke arah bos.
  * Muncul kartu judul gelap-emas dengan font bergaya kanji/metal (contoh: *"GIGAS - THE ANCIENT GOLEM"*).
  * Sound geraman (*boss roar*) dibunyikan, lalu bar darah bos terisi penuh (*fill animation*) sebelum pertarungan dimulai.

### 3.2 Efek Super Finish (*Slow-Motion Death*)
* Pukulan terakhir yang mengalahkan bos lantai memicu perlambatan waktu (*time-dilation* 0.25x) selama 0.5 detik disertai efek gelombang kejut radial putih (*radial shockwave VFX*).

### 3.3 Haptic Vibration Android (`Gdx.input.vibrate`)
* Umpan balik getaran mikro pada smartphone:
  * Getaran halus saat melakukan *Dodge Roll* atau melompat.
  * Getaran medium saat tebasan mendarat telak pada musuh.
  * Getaran berat berdenyut saat pemain terkena serangan besar atau mengeksekusi *Meteor Cleave*.

### 3.4 Musik Latar Dinamis (*Adaptive BGM*)
* Transisi mulus antara dua layer musik:
  * *Exploration Theme:* Melodi ambien tenang dan misterius.
  * *Combat/Boss Theme:* Lapisan perkusi drum bertempo cepat dan distorsi bass yang memompa adrenalin.

---

## 📱 4. Ergonomi Kontrol & Ekosistem Pemain

### 4.1 Dukungan Gamepad / Controller Bluetooth
* Menambahkan modul `gdx-controllers` resmi.
* Pemain di PC maupun smartphone dapat langsung menghubungkan controller (DualShock/DualSense, Xbox Wireless, atau Gamepad Android OTG/Bluetooth) dengan *mapping* tombol otomatis.

### 4.2 Kustomisasi Tata Letak Tombol Layar Sentuh (*Custom Layout*)
* Menu pengaturan di layar Pause:
  * Pemain dapat menggeser posisi D-Pad dan tombol aksi sesuai ukuran layar dan jangkauan ibu jari.
  * Pengatur skala ukuran tombol (70% hingga 130%) dan pengatur transparansi (*opacity slider*).

### 4.3 Cloud Save & Kode Ekspor/Impor Save Data
* Fitur untuk mengekspor status karakter menjadi string teks ringkas (Base64) atau file `.json`.
* Pemain dapat menyalin kode dari PC dan menempelkannya di smartphone untuk melanjutkan progres tanpa kehilangan data.

---

## 📊 5. Matriks Prioritas Pengembangan

| Tahap | Prioritas | Fitur Utama | Estimasi Dampak |
| :--- | :--- | :--- | :--- |
| **Fase 9** | ✅ Selesai | Varian Musuh Ranged & Haptic Feedback Android | Menambah variasi pertarungan & rasa kontrol fisik yang nyata |
| **Fase 10** | ✅ Selesai | Toko Blacksmith (Senjata) & Relik Pasif | Memberikan nilai guna nyata pada perolehan Gold |
| **Fase 11** | ✅ Selesai | Sistem Parry / Counter-Attack & Intro Animasi Bos | Meningkatkan kepuasan aksi (*skill ceiling*) & dramatisasi visual |
| **Fase 12** | ⚡ Menengah | Dukungan Gamepad (PC/Android) & Kustomisasi Tombol Sentuh | Kenyamanan ergonomis bermain di berbagai tipe perangkat |
| **Fase 13** | 🌟 Lanjutan | Mode Menara Tanpa Batas (*Endless Ascent*) & Talent Tree | Replay value tak terbatas dan kedalaman pembangunan karakter (*builds*) |
| **Fase 14** | 🚀 Rilis | Integrasi Google Play Games (Leaderboard & Achievements) | Kesiapan rilis publik di Google Play Store |

---

## 🛠️ 6. Prinsip Rekayasa Perangkat Lunak yang Wajib Dipertahankan
1. **Zero-Allocation Runtime:** Mempertahankan *object pooling* pada seluruh partikel, teks melayang, dan perhitungan vektor agar GC tidak pernah memicu *frame drop* (stuttering) di ponsel berspesifikasi rendah.
2. **Viewport Decoupling:** Mempertahankan pemisahan tegas antara koordinat dunia Box2D (16x9 meter) dan koordinat antarmuka UI Canvas (1280x720 piksel).
3. **Pembersihan Memori Defensif:** Memastikan seluruh tekstur GL, buffer audio OpenAL, dan font FreeType dibebaskan dengan protokol *defensive disposal* pada setiap pergantian layar (*screen transition*).

# Stickman Tower Adventure — Game Design Document (GDD) v0.1

> **Status:** Draft awal  
> **Jenis game:** Single-player 2D side-scrolling action RPG platformer  
> **Target platform:** Android  
> **Bahasa pemrograman:** Kotlin  
> **Framework:** libGDX  
> **Editor:** Google Antigravity

Dokumen ini merangkum keputusan awal tentang **core gameplay**, **genre dan mekanik**, serta **tech stack**. Detail seperti cerita, nama final game, angka statistik, desain karakter, dan keseimbangan gameplay belum dikunci dan dapat berubah selama prototyping.

---

## 1. Core Gameplay

### 1.1 Konsep inti

Game ini adalah game stickman single-player dengan perspektif 2D side-scrolling. Pemain memilih karakter, menjelajahi map dan tower, bertarung melawan musuh serta boss, mengumpulkan reward, lalu memperkuat karakter untuk menghadapi tantangan berikutnya.

Pilar utama game:

1. **Combat:** pertarungan real-time yang responsif dengan combo, dodge, skill, dan pola serangan musuh.
2. **Exploration:** menjelajahi platform, melewati rintangan, menemukan jalan, checkpoint, peti, serta area rahasia.
3. **Progression:** memperoleh EXP, naik level, meningkatkan statistik, membuka skill, dan mengumpulkan equipment.
4. **Character variety:** karakter yang dapat dipilih memiliki gaya bermain dan kemampuan berbeda.
5. **Tower progression:** lantai atau stage memiliki tantangan yang semakin bervariasi dan sulit.

### 1.2 Gameplay loop

Siklus utama permainan:

1. Pilih karakter.
2. Masuk ke map atau lantai tower.
3. Jelajahi area dan temukan musuh, rintangan, serta jalur menuju tujuan.
4. Bertarung dan mengalahkan musuh.
5. Mendapatkan EXP, koin, equipment, atau item lainnya.
6. Memperkuat karakter melalui level, statistik, skill, atau equipment.
7. Menyelesaikan objective, mengalahkan boss jika ada, dan membuka stage berikutnya.
8. Ulangi siklus dengan tantangan yang meningkat.

### 1.3 Aktivitas pemain

- Bergerak ke kiri dan kanan.
- Melompat dan mendarat di platform.
- Menghindari serangan melalui dash atau dodge.
- Melakukan serangan dasar dan combo.
- Menggunakan skill aktif dan ultimate.
- Melawan musuh serta boss.
- Berinteraksi dengan checkpoint, pintu, peti, dan objek tertentu.
- Mengambil reward dan mengembangkan karakter.

### 1.4 Struktur stage dan tower

Struktur stage yang dapat digunakan sebagai pola dasar:

`Masuk stage → Eksplorasi → Combat/rintangan → Reward/checkpoint → Boss atau objective akhir → Exit`

Tidak semua stage harus mengikuti pola yang sama. Variasi stage dapat mencakup pertarungan biasa, eksplorasi, puzzle sederhana, jebakan, tantangan bertahan hidup, area rahasia, atau boss.

Tower akan terdiri dari beberapa lantai atau stage. Dunia juga dapat memiliki map terpisah yang terhubung dengan progresi tower. Jumlah map dan lantai belum ditentukan.

### 1.5 Progression karakter

Sistem perkembangan awal yang direncanakan:

- **Character level:** EXP menaikkan level karakter.
- **Statistik:** misalnya HP, Attack, dan Defense. Statistik tambahan ditentukan saat balancing.
- **Skill progression:** pemain dapat membuka atau meningkatkan skill.
- **Equipment/build:** senjata, armor, atau item dapat mengubah statistik dan gaya bermain.
- **Unlock progression:** progres tertentu membuka lantai, stage, atau konten baru.

Angka EXP, rumus damage, jumlah level, drop rate, dan aturan distribusi poin belum ditentukan. Nilai tersebut akan diatur setelah combat dasar dapat dimainkan dan diuji.

### 1.6 Kondisi menang dan kalah

**Kondisi menang stage:** pemain menyelesaikan objective stage, mencapai exit, atau mengalahkan boss yang menjadi syarat penyelesaian.

**Kondisi kalah:** HP karakter mencapai nol.

**Aturan pemulihan awal yang disarankan:** pemain kembali ke checkpoint terakhir dan stage terkait dimuat ulang. Penalti kematian, kehilangan item, dan detail checkpoint belum dikunci.

### 1.7 Scope MVP

MVP (*Minimum Viable Product*) berfokus pada pembuktian bahwa gameplay inti terasa dan berfungsi dengan baik. Tidak perlu langsung membuat semua karakter atau tower.

**Isi MVP yang disarankan:**

- 1 karakter yang dapat dimainkan.
- Gerakan kiri/kanan, melompat, gravitasi, dan collision platform.
- Serangan dasar, HP, damage, serta hitbox.
- 1 jenis musuh dengan AI sederhana.
- 1 stage kecil yang dapat diselesaikan.
- Sistem menang/kalah dasar.
- Setelah dasar stabil: combo, dodge, cooldown skill, energy ultimate, EXP, dan save lokal.

**Kriteria selesai MVP:** pemain dapat memulai stage, bergerak, bertarung, menerima damage, mengalahkan musuh, menyelesaikan stage, dan melihat progres dasar tersimpan jika fitur save sudah masuk cakupan build tersebut.

---

## 2. Genre dan Mekanik

### 2.1 Genre

Genre yang dipilih untuk rancangan awal:

**2D Side-Scrolling Action RPG Platformer**, dengan elemen:

- **Action platformer:** movement, lompatan, platform, rintangan, dan eksplorasi.
- **Hack-and-slash:** pertarungan real-time, combo, dodge, dan skill.
- **Action RPG:** EXP, level, statistik, skill, equipment, dan build karakter.

Game belum dirancang sebagai Metroidvania. Dunia yang saling terhubung dan kemampuan yang membuka jalur lama dapat dipertimbangkan nanti jika memang sesuai dengan arah desain.

### 2.2 Perspektif dan kontrol

- Perspektif: 2D side-scrolling dari samping.
- Pemain bergerak ke kiri dan kanan.
- Pemain dapat melompat dan jatuh dari platform.
- Kontrol Android menggunakan tombol virtual/touch control.
- Input keyboard digunakan untuk pengujian pada build desktop.
- Ukuran tombol, tata letak UI, dan opsi controller akan dirancang setelah prototype tersedia.

### 2.3 Tempo combat

**Pilihan: Hybrid — karakter tertentu cepat, karakter lainnya berat.**

Contoh arketipe untuk mengarahkan desain, belum daftar karakter final:

- **Fast/agile:** serangan cepat, recovery pendek, mobilitas tinggi, damage per hit relatif lebih rendah.
- **Heavy:** serangan lebih lambat, recovery lebih panjang, damage dan knockback lebih kuat.
- **Balanced:** statistik dan kecepatan berada di antara dua pendekatan tersebut.
- **Ranged/magic:** dapat dipertimbangkan sebagai gaya tambahan jika cocok dengan desain karakter.

Perbedaan karakter sebaiknya tidak hanya berupa warna atau angka statistik. Masing-masing perlu mempunyai gaya bermain, animasi, konfigurasi serangan, dan kemampuan yang terasa berbeda.

### 2.4 Sistem serangan

Rancangan awal:

- Basic attack.
- Combo basic attack hingga 3 hit sebagai target awal.
- Dodge/dash untuk menghindari serangan.
- Skill aktif dengan cooldown.
- Ultimate yang menggunakan energy.
- Serangan udara atau heavy attack dapat ditambahkan setelah combat dasar stabil.

Setiap serangan nantinya dapat memiliki konfigurasi berupa damage, jarak, durasi startup, waktu hit aktif, recovery, knockback, serta aturan combo. Angka final belum ditentukan.

### 2.5 Skill: cooldown dan energy

Pilihan sistem skill adalah **hybrid**:

- **Skill biasa:** menggunakan cooldown. Skill baru dapat digunakan kembali setelah waktu cooldown selesai.
- **Ultimate:** menggunakan energy. Ultimate hanya dapat digunakan jika energy mencukupi biaya yang ditentukan.

Sumber energy ultimate belum dikunci. Salah satu opsi yang dapat diuji adalah energy bertambah saat serangan berhasil mengenai musuh. Opsi lain adalah energy dari menerima damage, mengalahkan musuh, atau kombinasi sumber tersebut. Sistem ini perlu diseimbangkan agar mendorong permainan aktif tanpa mudah dieksploitasi.

### 2.6 Movement dan platforming

Fitur dasar:

- Gerak horizontal.
- Gravitasi dan lompatan.
- Deteksi lantai dan collision dengan platform.
- Deteksi jatuh dari platform.
- Dash/dodge setelah combat dasar berfungsi.

Double jump, wall jump, dan kemampuan traversal khusus adalah fitur opsional yang belum masuk scope awal.

### 2.7 Enemy AI dan boss

AI musuh dasar dapat memiliki state berikut:

- Idle/patrol.
- Detect player.
- Approach/chase.
- Attack.
- Recovery.
- Hurt/knockback.
- Dead.

Boss nantinya dapat mempunyai pola serangan yang dapat dikenali dan fase tambahan. Untuk MVP, cukup gunakan satu musuh sederhana sebelum membuat variasi AI dan boss.

### 2.8 HP, damage, collision, dan hitbox

- HP karakter berkurang ketika terkena serangan valid.
- Serangan menggunakan hitbox terpisah dari collision tubuh dan platform.
- Hitbox serangan aktif pada waktu tertentu sesuai animasi/konfigurasi serangan.
- Musuh memiliki HP dan menerima damage dari serangan pemain.
- Aturan invulnerability saat terkena serangan, knockback, dan jeda setelah menerima damage perlu ditentukan serta diuji.

### 2.9 State machine karakter

State awal yang mungkin digunakan:

`Idle`, `Run`, `Jump`, `Fall`, `Attack1`, `Attack2`, `Attack3`, `Dodge`, `Skill`, `Hurt`, `Dead`.

State tambahan dapat dibuat sesuai kebutuhan. State machine bertanggung jawab mengatur aksi yang sedang dilakukan, validitas input, dan perpindahan antaraksi agar karakter tidak menjalankan aksi yang saling bertentangan.

### 2.10 Prioritas implementasi mekanik

**Prioritas pertama:** movement, jump, gravitasi, collision platform, basic attack, HP, damage, dan satu musuh.

**Prioritas berikutnya:** combo, dodge, enemy AI yang lebih baik, satu stage lengkap, checkpoint, serta UI dasar.

**Setelah itu:** skill cooldown, ultimate energy, EXP/level, save lokal, dan karakter kedua.

**Pengembangan lanjutan:** equipment, skill tree, beragam tower/map, puzzle, secret area, banyak karakter, serta boss dengan beberapa fase.

---

## 3. Tech Stack

### 3.1 Stack yang dipilih

| Bagian | Teknologi | Tanggung jawab |
|---|---|---|
| Bahasa utama | Kotlin | Logika gameplay, karakter, combat, AI, skill, progression, dan save system |
| Game framework | libGDX | Game loop, rendering 2D, kamera, input, sprite, animasi, dan audio |
| Editor kode | Google Antigravity | Menulis, menavigasi, dan membantu pengembangan kode |
| Build system | Gradle + Gradle Wrapper | Dependency dan proses build yang konsisten |
| Java toolchain | JDK yang kompatibel dengan konfigurasi project; JDK 17 sebagai kandidat awal | Menjalankan toolchain JVM dan proses build |
| Target Android | Android SDK dan Android Build Tools yang diperlukan | Build APK dan pengujian perangkat Android |
| Peta level | Tiled Map Editor | Menyusun tilemap, platform, layer, spawn point, checkpoint, dan exit |
| Asset visual | Sprite sheet/TextureAtlas | Mengelola sprite dan animasi karakter, musuh, efek, serta UI |
| Save game | Penyimpanan lokal, misalnya JSON | Menyimpan progres single-player |
| Preferensi sederhana | Preferences | Pengaturan seperti volume dan opsi pemain |
| Version control | Git | Riwayat perubahan kode dan pemulihan versi |

**Catatan kompatibilitas:** versi JDK, Gradle, plugin Android, Android SDK, dan libGDX harus cocok satu sama lain. Saat membuat project, gunakan generator resmi Gdx-Liftoff dan ikuti versi yang dihasilkan project; jangan mengganti versi toolchain secara acak. Gradle Wrapper yang disertakan project sebaiknya digunakan untuk build.

### 3.2 Framework: mengapa libGDX?

libGDX dipilih karena mendukung pengembangan game 2D, dapat digunakan bersama Kotlin, serta menyediakan target desktop dan Android. Kode gameplay dapat ditulis terutama dalam Kotlin. Build desktop memungkinkan pengujian cepat tanpa memasang APK pada setiap perubahan.

Trade-off: libGDX merupakan framework, bukan editor visual game lengkap. Sebagian besar mekanik perlu ditulis dengan kode. Tiled dapat digunakan sebagai editor eksternal untuk membantu perancangan level.

### 3.3 Workflow laptop

**Perangkat yang tersedia:**

- OS: Linux Mint.
- CPU: Intel Core i3 generasi ke-11.
- RAM: 12 GB.
- Storage: 256 GB total, sekitar 30 GB tersisa.
- Editor: Google Antigravity.

Spesifikasi tersebut memadai untuk mulai membuat dan menguji prototype 2D. Storage kosong yang terbatas perlu dikelola karena SDK, cache Gradle, build, dan asset dapat bertambah.

Workflow yang disarankan:

1. Tulis kode Kotlin di Google Antigravity.
2. Build/run modul desktop untuk menguji movement dan combat pada laptop.
3. Gunakan HP Android fisik melalui USB debugging untuk pengujian touch control dan performa perangkat.
4. Build APK untuk pengujian Android.
5. Hindari memasang Android Emulator pada tahap awal jika tidak diperlukan untuk menghemat RAM dan storage.

### 3.4 Struktur proyek yang disarankan

Struktur logis berikut dapat dijadikan pedoman. Generator project bisa menghasilkan susunan folder berbeda, jadi sesuaikan dengan template aktual.

```text
stickman-tower/
├── core/
│   └── src/main/kotlin/com/example/stickman/
│       ├── game/          # Inisialisasi game dan pengaturan umum
│       ├── screen/        # Menu, gameplay, pause, dan hasil stage
│       ├── world/         # Dunia gameplay dan kamera
│       ├── character/     # Model dan controller karakter
│       ├── combat/        # Attack, hitbox, damage, dan combo
│       ├── enemy/         # Musuh dan AI
│       ├── skill/         # Cooldown dan ultimate energy
│       ├── progression/   # EXP, level, dan statistik
│       ├── map/           # Pemuatan stage dan objek map
│       ├── inventory/     # Equipment dan item (tahap lanjutan)
│       ├── save/          # Penyimpanan progres
│       ├── ui/            # HUD dan antarmuka game
│       └── audio/         # Musik dan efek suara
├── android/               # Launcher dan integrasi Android
├── lwjgl3/                # Launcher desktop jika dibuat oleh generator
├── assets/
│   ├── characters/
│   ├── enemies/
│   ├── maps/
│   ├── backgrounds/
│   ├── effects/
│   ├── audio/
│   └── ui/
├── gradlew
├── gradlew.bat
└── build.gradle / file Gradle lain yang dihasilkan generator
```

Nama package `com.example.stickman` adalah placeholder dan perlu diganti dengan package project yang dipilih.

### 3.5 Platformer controller dan fisika

Untuk prototype pertama, gunakan controller platformer sederhana yang ditulis sendiri agar kecepatan lari, gravitasi, lompatan, dan collision bisa disesuaikan dengan rasa combat. Collision platform harus dipisahkan dari hitbox serangan.

Box2D dapat dipertimbangkan jika kebutuhan fisika menjadi lebih kompleks. Jangan menambahnya hanya karena tersedia; lebih baik validasi kebutuhan melalui prototype.

### 3.6 Backend dan database

Backend dan database **belum diperlukan** untuk versi single-player offline. Simpan progres secara lokal terlebih dahulu. Backend baru dapat dipertimbangkan jika ada kebutuhan seperti cloud save, akun, leaderboard online, atau sinkronisasi perangkat.

### 3.7 Urutan setup teknis

1. Periksa Java/JDK dan ruang kosong storage di Linux Mint.
2. Buat project Kotlin menggunakan Gdx-Liftoff.
3. Sertakan modul desktop dan Android jika sesuai kebutuhan project.
4. Buka folder project di Google Antigravity.
5. Jalankan build desktop awal.
6. Buat karakter placeholder yang bisa bergerak dan melompat.
7. Tambahkan collision platform dan kamera.
8. Implementasikan basic attack dan satu musuh.
9. Setelah prototype stabil, build serta uji APK di HP Android.

---

## 4. Keputusan yang sudah ditetapkan

- Game bersifat single-player.
- Perspektif adalah 2D side-scrolling.
- Genre adalah 2D side-scrolling action RPG platformer dengan elemen hack-and-slash.
- Tempo combat hybrid: karakter cepat dan karakter berat dapat memiliki gaya bermain berbeda.
- Skill biasa memakai cooldown.
- Ultimate menggunakan energy.
- Target platform adalah Android.
- Bahasa utama adalah Kotlin.
- Framework game adalah libGDX.
- Editor utama adalah Google Antigravity.
- Pengujian awal dilakukan melalui build desktop; pengujian Android menggunakan perangkat fisik jika tersedia.
- Belum ada kebutuhan backend untuk MVP offline.

## 5. Hal yang belum diputuskan

- Nama final game dan cerita/lore.
- Gaya visual final serta asset art.
- Daftar karakter final dan kemampuan masing-masing.
- Detail kontrol touch dan tata letak HUD.
- Angka damage, HP, cooldown, energy, EXP, dan keseimbangan level.
- Aturan death penalty dan detail checkpoint.
- Jumlah map, tower, lantai, musuh, dan boss.
- Sistem equipment, rarity, skill tree, dan unlock final.
- Versi final JDK serta dependency setelah project awal dibuat.

## 6. Langkah berikutnya

Mulai dengan **prototype teknis kecil**: satu karakter placeholder yang bisa bergerak ke kiri/kanan, melompat, mendarat di platform, dan tampil dengan kamera yang tepat. Setelah movement stabil, implementasikan serangan dasar, satu musuh, damage, lalu combo dan dodge. Jangan mulai dari banyak karakter, map, atau cerita panjang sebelum core gameplay terbukti menyenangkan.

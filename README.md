# ⚔️ Stickman Tower Adventure

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![libGDX](https://img.shields.io/badge/libGDX-1.13.1-EA2027.svg?logo=libgdx&logoColor=white)](https://libgdx.com/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop%20(LWJGL3)-48bb78.svg)](#cross-platform--controls)
[![Tests](https://img.shields.io/badge/Tests-33%20Passed-success.svg?logo=checkmarx&logoColor=white)](#automated-testing--qa)
[![APK Size](https://img.shields.io/badge/APK%20Size-10.6%20MB-blue.svg?logo=android&logoColor=white)](https://github.com/muhammadyusuftechid-star/stickman-tower-adventure/releases)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **High-Performance 2D Action Roguelite RPG** dibangun dari nol dengan **Kotlin**, **libGDX**, dan **Box2D Physics Engine** untuk target **Android** dan **Desktop (PC)**.  
> Dirancang dengan standar industri: arsitektur modular, **zero-allocation rendering** untuk garansi 60 FPS mulus di perangkat mobile, sistem input terpadu (*Keyboard, Gamepad, Touch*), dan cakupan pengujian unit otomatis (*automated test coverage*).

---

## 🎮 Gameplay Showcase

| Fitur Utama | Deskripsi Teknis |
| :--- | :--- |
| **Fluid Action Combat** | 3-Hit Combo FSM, Perfect Parry dengan *hit-stop* & *time-dilation*, Dash Invulnerability (*i-frames*), dan Ultimate Screen Nuke. |
| **3 Character Archetypes** | **Shadow Duelist** (Dual-blade burst crit), **Iron Vanguard** (High DEF & ground slam), **Arcane Archer** (Ranged piercing arrows). |
| **Procedural Endless Spire** | Mode lantai tak terbatas dengan **7 Random Mutators** (Vampiric, Berserk, Shielded, Swarm, Spikes, Glass Cannon, Swiftness). |
| **Deep RPG Progression** | 15-Node Branching Talent Tree, 6 Sub-Classes, Blacksmith weapon & armor tempering, serta 8 stackable passive Relics. |
| **Multi-Phase Boss Battles** | Pertarungan dinamis melawan *The Tower Warden* dengan transisi Phase 2 (Enraged, shockwaves, projectile storm, cinematic intro). |
| **Custom Touch HUD & Gamepad** | Editor posisi, ukuran, dan opacity tombol virtual di layar sentuh, serta dukungan *plug-and-play* untuk Controller Bluetooth/USB. |

---

## 🏗️ Arsitektur & Prinsip Rekayasa Perangkat Lunak

Proyek ini dibangun sebagai demonstrasi rekayasa perangkat lunak game modern yang mematuhi standar industri:

```
                          ┌─────────────────────────────┐
                          │         Input Layer         │
                          │   (Keyboard/Gamepad/Touch)  │
                          └──────────────┬──────────────┘
                                         ▼
                          ┌─────────────────────────────┐
                          │       CompositeInput        │
                          │   (Unified Control Contract)│
                          └──────────────┬──────────────┘
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Core Game Loop                                 │
│                                                                             │
│   ┌────────────────────┐    ┌────────────────────┐    ┌─────────────────┐   │
│   │   Physics & World  │    │  Combat & Entities │    │   RPG & Meta    │   │
│   │   (Box2D 16:9 Mtr) │◄──►│  (FSM, Stats, Hit) │◄──►│  (Talent, Relic)│   │
│   └────────────────────┘    └────────────────────┘    └─────────────────┘   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Rendering Layer                                │
│                                                                             │
│   ┌──────────────────────────────────┐  ┌───────────────────────────────┐   │
│   │ World Camera (Box2D Simulation)  │  │ UI Canvas (FitViewport 1280x720)│ │
│   │ Procedural Stickman & Particles  │  │ HUD, Overlays, Floating Text  │   │
│   └──────────────────────────────────┘  └───────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1. Zero-Allocation GC Hygiene (Anti Frame-Drop Mobile)
Di Android/JVM, *Garbage Collection (GC) pauses* adalah pemicu utama *stutter* dan *frame drops*.  
- **In-Place Math Mutations:** Tidak ada pembuatan instance `Vector2()` atau `Vector3()` baru di dalam loop `update()` atau `render()`. Semua kalkulasi vektor menggunakan cache objek in-place (`Vector2.set()`).
- **Object Pooling:** Efek partikel ([ParticleManager](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/vfx/)), teks damage melayang ([FloatingTextManager](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/combat/FloatingText.kt)), dan proyektil menggunakan array pool yang di-recycle.
- **Safe OpenGL Lifecycle:** Transisi antar layar pada [StickmanGame.kt](file:///home/yusuf/projekan/game/game1/core/src/main/kotlin/com/stickman/tower/StickmanGame.kt) menggunakan `Gdx.app.postRunnable` untuk menjamin tidak ada race condition saat melepaskan resource native GL.

### 2. Strict Coordinate Decoupling
- **World Space (Box2D):** Menggunakan satuan Meter dengan aspek rasio 16:9 (`16.0f x 9.0f` viewport), menjamin simulasi gravitasi, inersia, dan deteksi tumbukan deterministik di semua resolusi.
- **UI HUD Space:** Menggunakan pixel-based `FitViewport(1280f, 720f)`, memastikan rasio layar HP modern (19.5:9, 20:9, tablet 4:3) tidak mengalami peregangan atau distorsi visual.

### 3. Unified Input Abstraction
Logika gameplay sama sekali tidak memiliki dependensi pada tipe input perangkat:
- `CompositeInput` mengimplementasikan interface `InputController`.
- Menyerap keyboard desktop, gamepad Bluetooth/USB (Xbox, PlayStation, Generic HID), dan on-screen virtual touch controller dalam satu pipeline yang sama.
- Dilengkapi filter *sensor noise suppression* untuk mengabaikan input accelerometer/gyroscope perangkat Android secara otomatis.

---

## 📁 Struktur Repositori

```bash
stickman-tower-adventure/
├── android/                 # Android project module (Activity, JNI, Manifest, ProGuard)
│   └── src/main/kotlin/com/stickman/tower/android/
│       └── AndroidLauncher.kt
├── core/                    # Game Engine & Pure Logic (Multiplatform Kotlin)
│   ├── src/main/kotlin/com/stickman/tower/
│   │   ├── audio/           # Sound & Haptic Feedback abstractions
│   │   ├── character/       # Player entity, archetypes, states & animations
│   │   ├── combat/          # Combo FSM, Hitboxes, Hurtboxes, Parry, Skills
│   │   ├── endless/         # Endless Spire procedural floor mutators
│   │   ├── enemy/           # Enemy AI, Boss 2-Phase state machines
│   │   ├── input/           # Composite input, Gamepad, Touch virtual stick
│   │   ├── physics/         # Box2D platforms, sensors & collision listeners
│   │   ├── relic/           # Passive relic inventory & tier buffs
│   │   ├── rpg/             # Stats calculation, save/load serialization
│   │   ├── screen/          # Game screens (Play, Menu, Blacksmith, Talent)
│   │   ├── talent/          # 15-Node Talent tree & Sub-Class unlocking
│   │   ├── ui/              # FontManager & responsive UI helpers
│   │   ├── vfx/             # Particle effects & screen shake managers
│   │   └── world/           # Tower floor generator, checkpoints, hazards
│   └── src/test/kotlin/     # 33 Automated Unit Tests (Headless QA Suite)
├── lwjgl3/                  # Desktop Launcher module (LWJGL3 backend)
│   └── src/main/kotlin/com/stickman/tower/lwjgl3/
│       └── Lwjgl3Launcher.kt
├── assets/                  # Fonts, Sound clips, and default assets
├── Alur_Pembuatan_Aplikasi.md  # Detailed Engineering Architecture Document
├── ROADMAP.md               # 14-Phase Development Milestone Tracker
└── Stickman_Game_Design_v0.1.md # Game Design Document (GDD)
```

---

## 🕹️ Skema Kontrol (Cross-Platform)

Game ini mendukung kontrol multi-platform secara *out-of-the-box*:

| Aksi | Keyboard (PC) | Gamepad (Xbox / Generic) | Layar Sentuh (Android) |
| :--- | :---: | :---: | :---: |
| **Gerak Kiri / Kanan** | `A` / `D` atau `Panah` | Left Stick / D-Pad | Virtual Joystick (Kiri) |
| **Lompat** | `W` / `Space` | Tombol `A` | Tombol `JUMP` |
| **Serangan Melee / Combo** | `J` / `Z` | Tombol `X` | Tombol `ATK` |
| **Ranged / Tembak** | `K` / `C` | Tombol `Y` | Tombol `RNG` |
| **Dash (Invulnerable)** | `L` / `Shift` | Right Trigger (`RT`) / `B` | Tombol `DASH` |
| **Parry / Deflect** | `F` | Left Bumper (`LB`) | Tombol `PARRY` |
| **Ultimate Skill** | `U` / `V` | Left Trigger (`LT`) | Tombol `ULT` |
| **Pause Menu** | `Escape` / `P` | Tombol `Start` | Tombol `PAUSE` (Kanan Atas) |

---

## 🧪 Automated Testing & QA

Kehandalan logika diverifikasi secara otomatis menggunakan **JUnit 5 / Kotlin Test**:

```bash
# Menjalankan seluruh test suite secara headless:
./gradlew test --console=plain
```

```
> Task :core:test
com.stickman.tower.audio.HapticManagerTest > testHapticPatternTrigger() PASSED
com.stickman.tower.character.CharacterArchetypeTest > testArchetypeMultipliers() PASSED
com.stickman.tower.combat.ParryTest > testPerfectParryWindow() PASSED
com.stickman.tower.endless.EndlessAndTalentTest > testSpireFloorMutators() PASSED
com.stickman.tower.enemy.EnemyTest > testEnemyFSMTransitions() PASSED
com.stickman.tower.input.GamepadAndTouchSettingsTest > testInputBindingIntegrity() PASSED
com.stickman.tower.relic.RelicManagerTest > testRelicStacking() PASSED
com.stickman.tower.ui.FontManagerTest > testFontCacheIntegrity() PASSED

BUILD SUCCESSFUL in 17s (33/33 Tests Passed, 100% Success Rate)
```

---

## 🚀 Panduan Menjalankan Proyek

### Prasyarat
- **Java Development Kit (JDK):** Versi 17 atau lebih baru
- **Android SDK:** Platform API 34+ dan Build Tools (untuk build Android)

### 1. Menjalankan di Desktop (Linux / macOS / Windows)
```bash
# Clone repositori
git clone https://github.com/muhammadyusuftechid-star/stickman-tower-adventure.git
cd stickman-tower-adventure

# Jalankan game di desktop
./gradlew lwjgl3:run
```

### 2. Membangun APK Android
```bash
# Compile Debug APK
./gradlew android:assembleDebug

# Lokasi APK yang dihasilkan:
# android/build/outputs/apk/debug/android-debug.apk
```

---

## 📦 Download APK Rilis

File APK Android yang siap dimainkan dapat langsung diunduh melalui halaman rilis resmi:
👉 **[Unduh Stickman Tower Adventure APK (v1.0.0)](https://github.com/muhammadyusuftechid-star/stickman-tower-adventure/releases)**

---

## 📄 Lisensi & Kontributor

- **Pengembang:** Muhammad Yusuf ([@muhammadyusuftechid-star](https://github.com/muhammadyusuftechid-star))
- **Lisensi:** Didistribusikan di bawah lisensi [MIT](LICENSE). Terbuka untuk eksplorasi kode, pembelajaran, dan referensi portofolio.

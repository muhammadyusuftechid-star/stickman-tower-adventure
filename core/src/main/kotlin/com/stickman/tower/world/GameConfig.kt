package com.stickman.tower.world

/**
 * Konfigurasi global dimensi dunia dan konstanta fisika.
 * Menggunakan World Units (1 unit = 1 meter logis), bukan ukuran piksel mentah.
 */
object GameConfig {
    // Dimensi Virtual Viewport (Aspek Rasio 16:9)
    const val VIRTUAL_WIDTH = 16f
    const val VIRTUAL_HEIGHT = 9f

    // Parameter Fisika Gerak Karakter
    const val GRAVITY = -28f
    const val MOVE_SPEED = 7.5f
    const val ACCELERATION = 45f
    const val DECELERATION = 35f
    const val JUMP_VELOCITY = 11.5f

    // Toleransi Platformer Responsif
    const val COYOTE_TIME = 0.12f      // Toleransi lompat setelah lepas dari tepi (detik)
    const val JUMP_BUFFER_TIME = 0.12f  // Buffer input lompat sebelum menyentuh lantai (detik)

    // Kecepatan Kamera Lerp
    const val CAMERA_LERP_SPEED = 5f
}

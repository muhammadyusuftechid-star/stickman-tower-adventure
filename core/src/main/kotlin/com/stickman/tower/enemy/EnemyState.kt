package com.stickman.tower.enemy

/**
 * State machine untuk Artificial Intelligence (AI) musuh.
 */
enum class EnemyState {
    PATROL, // Bergerak santai bolak-balik dalam area platform
    CHASE,  // Mengejar posisi pemain saat terdeteksi
    ATTACK, // Berancang-ancang dan melancarkan tebasan ke arah pemain
    HURT,   // Terkena serangan pemain (knockback & stun sesaat)
    DEAD    // HP habis, animasi tumbang sebelum despawn
}

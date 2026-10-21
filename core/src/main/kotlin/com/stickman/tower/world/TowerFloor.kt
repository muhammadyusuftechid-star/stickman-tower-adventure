package com.stickman.tower.world

import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.enemy.BossEnemy
import com.stickman.tower.enemy.Enemy
import com.stickman.tower.physics.Platform

/**
 * Merepresentasikan data struktural dan objek satu lantai Menara (Tower Floor).
 */
data class TowerFloor(
    val floorNumber: Int,
    val floorName: String,
    val mapBounds: Rectangle,
    val spawnX: Float,
    val spawnY: Float,
    val platforms: List<Platform>,
    val spikes: List<SpikeHazard>,
    val chests: List<TreasureChest>,
    val enemies: List<Enemy>,
    val boss: BossEnemy? = null,
    val checkpoint: Checkpoint?,
    val exitPortal: StageExit,
    val isBossFloor: Boolean = false
)

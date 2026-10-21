package com.stickman.tower.tools

import com.badlogic.gdx.math.Rectangle
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.TowerFloor
import java.util.ArrayDeque
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Laporan hasil audit keterjangkauan (reachability) dan graph traversal pada lantai menara.
 */
data class ReachabilityAuditResult(
    val floorNumber: Int,
    val floorName: String,
    val isExitReachable: Boolean,
    val areCheckpointsReachable: Boolean,
    val totalPlatforms: Int,
    val reachablePlatformsCount: Int,
    val unreachableChestsCount: Int,
    val unreachableLoreTabletsCount: Int,
    val pathLengthToExit: Int,
    val details: List<String>
) {
    val isPassed: Boolean get() = isExitReachable && areCheckpointsReachable && unreachableChestsCount == 0

    fun summary(): String {
        return if (isPassed) {
            "✓ [REACHABILITY PASS] $floorName (Reachable: $reachablePlatformsCount/$totalPlatforms plats, Path: $pathLengthToExit hops)"
        } else {
            val issues = mutableListOf<String>()
            if (!isExitReachable) issues.add("EXIT UNREACHABLE")
            if (!areCheckpointsReachable) issues.add("CHECKPOINT UNREACHABLE")
            if (unreachableChestsCount > 0) issues.add("$unreachableChestsCount CHESTS UNREACHABLE")
            "✗ [REACHABILITY FAIL] $floorName: ${issues.joinToString(", ")}"
        }
    }
}

/**
 * Graph-Based Level Reachability & Kinematic Traversal Validator:
 * Membangun directed reachability graph berdasarkan batas kinematika fisik karakter:
 * - Kecepatan lari horizontal: 7.5 m/s, Dash: 14.5 m/s
 * - Batas lompatan vertikal: Single Jump ~2.45m, Double Jump ~4.50m
 * - Jarak jatuh aman dan jangkauan parabolik udara
 *
 * Mencegah level softlock, rute buntu tanpa jalan keluar, atau portal/chest yang mustahil dicapai.
 */
object LevelReachabilityGraph {

    const val MAX_SINGLE_JUMP_Y = 2.45f
    const val MAX_DOUBLE_JUMP_Y = 4.50f
    const val MAX_HORIZONTAL_REACH_FLAT = 8.5f
    const val GRAVITY = 28.0f
    const val RUN_SPEED = 7.5f
    const val DASH_BOOST = 3.2f

    /**
     * Menganalisis graf keterjangkauan platform pada suatu lantai menara.
     */
    fun analyzeFloor(floor: TowerFloor): ReachabilityAuditResult {
        val platforms = floor.platforms
        val numPlatforms = platforms.size

        if (numPlatforms == 0) {
            return ReachabilityAuditResult(
                floorNumber = floor.floorNumber,
                floorName = floor.floorName,
                isExitReachable = false,
                areCheckpointsReachable = false,
                totalPlatforms = 0,
                reachablePlatformsCount = 0,
                unreachableChestsCount = floor.chests.size,
                unreachableLoreTabletsCount = floor.loreTablets.size,
                pathLengthToExit = -1,
                details = listOf("Tidak ada platform pada lantai ini!")
            )
        }

        // 1. Identifikasi platform awal (Spawn Platform)
        val spawnPlatIdx = findSupportingPlatform(floor.spawnX, floor.spawnY, platforms) ?: 0

        // 2. Identifikasi platform portal keluar (Exit Platform)
        val exitPlatIdx = findSupportingPlatform(floor.exitPortal.x, floor.exitPortal.y, platforms) ?: (numPlatforms - 1)

        // 3. Bangun Directed Adjacency List Graph [u -> list of v]
        val adj = Array(numPlatforms) { mutableListOf<Int>() }
        for (i in 0 until numPlatforms) {
            val pA = platforms[i]
            for (j in 0 until numPlatforms) {
                if (i == j) continue
                val pB = platforms[j]
                if (canTraverse(pA, pB)) {
                    adj[i].add(j)
                }
            }
        }

        // 4. Jalankan Breadth-First Search (BFS) dari spawnPlatIdx
        val visited = BooleanArray(numPlatforms)
        val parent = IntArray(numPlatforms) { -1 }
        val distance = IntArray(numPlatforms) { -1 }
        val queue = ArrayDeque<Int>()

        visited[spawnPlatIdx] = true
        distance[spawnPlatIdx] = 0
        queue.add(spawnPlatIdx)

        while (!queue.isEmpty()) {
            val u = queue.poll()
            for (v in adj[u]) {
                if (!visited[v]) {
                    visited[v] = true
                    parent[v] = u
                    distance[v] = distance[u] + 1
                    queue.add(v)
                }
            }
        }

        val reachablePlatformsCount = visited.count { it }
        val isExitReachable = visited[exitPlatIdx]
        val pathLengthToExit = if (isExitReachable) distance[exitPlatIdx] else -1

        // 5. Cek Keterjangkauan Checkpoint
        val areCheckpointsReachable = if (floor.checkpoint == null) true else {
            val cpPlatIdx = findSupportingPlatform(floor.checkpoint.x, floor.checkpoint.y, platforms)
            cpPlatIdx != null && visited[cpPlatIdx]
        }

        // 6. Cek Keterjangkauan Peti Harta Karun (Chests)
        var unreachableChests = 0
        for (chest in floor.chests) {
            val chestPlatIdx = findSupportingPlatform(chest.x, chest.y, platforms)
            if (chestPlatIdx == null || !visited[chestPlatIdx]) {
                unreachableChests++
            }
        }

        // 7. Cek Keterjangkauan Prasasti Lore (Lore Tablets)
        var unreachableTablets = 0
        for (tablet in floor.loreTablets) {
            val tabPlatIdx = findSupportingPlatform(tablet.x, tablet.y, platforms)
            if (tabPlatIdx == null || !visited[tabPlatIdx]) {
                unreachableTablets++
            }
        }

        val details = mutableListOf<String>()
        if (!isExitReachable) {
            details.add("Exit portal pada (${floor.exitPortal.x}, ${floor.exitPortal.y}) tidak dapat dicapai dari titik spawn!")
        }
        if (!areCheckpointsReachable) {
            details.add("Checkpoint tidak dapat dicapai dari rute utama!")
        }
        if (unreachableChests > 0) {
            details.add("$unreachableChests peti harta karun berada di platform yang tidak terjangkau")
        }

        return ReachabilityAuditResult(
            floorNumber = floor.floorNumber,
            floorName = floor.floorName,
            isExitReachable = isExitReachable,
            areCheckpointsReachable = areCheckpointsReachable,
            totalPlatforms = numPlatforms,
            reachablePlatformsCount = reachablePlatformsCount,
            unreachableChestsCount = unreachableChests,
            unreachableLoreTabletsCount = unreachableTablets,
            pathLengthToExit = pathLengthToExit,
            details = details
        )
    }

    /**
     * Memverifikasi apakah karakter dapat melompat atau jatuh dari Platform A ke Platform B.
     */
    fun canTraverse(a: Platform, b: Platform): Boolean {
        val aLeft = a.left
        val aRight = a.right
        val aTop = a.top

        val bLeft = b.left
        val bRight = b.right
        val bTop = b.top

        // Jarak celah horizontal antara platform A dan B
        val gapX = max(0f, max(aLeft, bLeft) - min(aRight, bRight))
        val deltaY = bTop - aTop

        if (deltaY > 0) {
            // Melompat ke atas (memerlukan ketinggian <= Double Jump Max Height)
            if (deltaY > MAX_DOUBLE_JUMP_Y) return false
            // Jarak horizontal saat melompat ke atas terbatas
            val maxHorizontal = MAX_HORIZONTAL_REACH_FLAT * (1f - (deltaY / (MAX_DOUBLE_JUMP_Y + 1f)) * 0.45f)
            return gapX <= maxHorizontal
        } else {
            // Menuruni platform (jatuh ke bawah)
            val dropY = -deltaY
            // Waktu jatuh bebas t = sqrt(2 * h / g)
            val fallTime = sqrt(2f * dropY / GRAVITY)
            val totalAirTime = 0.82f + fallTime // jump apex air time + fall time
            val maxDropHorizontal = (RUN_SPEED * totalAirTime + DASH_BOOST).coerceAtMost(25.0f)
            return gapX <= maxDropHorizontal
        }
    }

    /**
     * Menemukan index platform yang menopang posisi (x, y).
     */
    fun findSupportingPlatform(x: Float, y: Float, platforms: List<Platform>): Int? {
        // Prioritas 1: Platform tepat di bawah (toleransi y hingga 4.0m ke bawah)
        var bestIdx: Int? = null
        var minDistance = Float.MAX_VALUE

        for ((idx, p) in platforms.withIndex()) {
            if (x >= p.left - 1.2f && x <= p.right + 1.2f) {
                val dist = y - p.top
                if (dist >= -0.5f && dist < minDistance) {
                    minDistance = dist
                    bestIdx = idx
                }
            }
        }

        if (bestIdx != null) return bestIdx

        // Prioritas 2: Platform terdekat secara Euclidean
        for ((idx, p) in platforms.withIndex()) {
            val centerX = (p.left + p.right) / 2f
            val centerY = p.top
            val dx = x - centerX
            val dy = y - centerY
            val dist = dx * dx + dy * dy
            if (dist < minDistance) {
                minDistance = dist
                bestIdx = idx
            }
        }
        return bestIdx
    }
}

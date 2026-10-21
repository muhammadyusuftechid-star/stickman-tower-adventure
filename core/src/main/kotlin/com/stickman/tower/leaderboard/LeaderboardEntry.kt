package com.stickman.tower.leaderboard

/**
 * Entri data papan peringkat ksatria menara (Leaderboard Entry).
 * Digunakan untuk mode Endless Ascent dan Speedrun Odyssey.
 */
data class LeaderboardEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val playerName: String = "Kaelen",
    val characterArchetype: String = "BALANCED",
    val floorReached: Int = 1,
    val score: Long = 0L,
    val clearTimeSeconds: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) : Comparable<LeaderboardEntry> {

    /**
     * Mengurutkan entri berdasarkan skor tertinggi (descending),
     * atau waktu tersingkat (ascending) bila skor sama.
     */
    override fun compareTo(other: LeaderboardEntry): Int {
        val scoreComp = other.score.compareTo(this.score)
        if (scoreComp != 0) return scoreComp
        return this.clearTimeSeconds.compareTo(other.clearTimeSeconds)
    }

    /**
     * Memformat durasi detik menjadi string MM:SS atau HH:MM:SS.
     */
    fun getFormattedTime(): String {
        val hours = clearTimeSeconds / 3600
        val minutes = (clearTimeSeconds % 3600) / 60
        val seconds = clearTimeSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

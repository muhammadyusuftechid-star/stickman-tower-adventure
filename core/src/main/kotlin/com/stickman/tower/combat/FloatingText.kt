package com.stickman.tower.combat

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3

/**
 * Merepresentasikan angka damage atau teks melayang yang muncul di dunia game
 * dan diproyeksikan ke layar dengan efek meluncur ke atas dan memudar (fade-out).
 */
class FloatingText(
    val text: String,
    var worldX: Float,
    var worldY: Float,
    val color: Color = Color.RED,
    val duration: Float = 0.8f,
    val isCritical: Boolean = false
) {
    private var elapsed: Float = 0f
    private val tempVec = Vector3()

    val isExpired: Boolean get() = elapsed >= duration

    fun update(delta: Float) {
        elapsed += delta
        val speed = if (isCritical) 2.2f else 1.5f
        worldY += speed * delta
    }

    fun render(batch: SpriteBatch, font: BitmapFont, camera: Camera) {
        if (isExpired) return

        // Proyeksikan koordinat dunia ke koordinat piksel layar
        tempVec.set(worldX, worldY, 0f)
        camera.project(tempVec)

        // Hitung transparansi fade out
        val alpha = (1f - (elapsed / duration)).coerceIn(0f, 1f)
        val originalColor = font.color

        font.color = Color(color.r, color.g, color.b, alpha)
        font.draw(batch, text, tempVec.x - (if (isCritical) 20f else 10f), tempVec.y)
        font.color = originalColor
    }
}

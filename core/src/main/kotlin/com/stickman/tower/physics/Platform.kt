package com.stickman.tower.physics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

/**
 * Merepresentasikan platform statis atau rintangan di dalam dunia game.
 */
class Platform(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val isOneWay: Boolean = false,
    val color: Color = Color(0.25f, 0.35f, 0.45f, 1f)
) {
    val bounds = Rectangle(x, y, width, height)

    val left: Float get() = bounds.x
    val right: Float get() = bounds.x + bounds.width
    val bottom: Float get() = bounds.y
    val top: Float get() = bounds.y + bounds.height

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = color
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)

        // Render garis tepi atas (top highlight) untuk visual lebih jelas
        shapeRenderer.color = Color(color.r + 0.15f, color.g + 0.15f, color.b + 0.15f, 1f)
        val highlightHeight = 0.08f.coerceAtMost(bounds.height * 0.2f)
        shapeRenderer.rect(bounds.x, top - highlightHeight, bounds.width, highlightHeight)
    }
}

package com.stickman.tower.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.sin

/**
 * Satu baris dialog naratif cerita.
 */
data class DialogueLine(
    val speaker: String,
    val title: String,
    val text: String,
    val speakerColor: Color = Color.GOLD
)

/**
 * Pemicu dialog naratif (NPC atau cutscene trigger).
 */
class StoryDialogue(
    val triggerX: Float,
    val triggerY: Float,
    val triggerRadius: Float = 2.0f,
    val lines: List<DialogueLine>
) {
    var isTriggered: Boolean = false
    var currentLineIndex: Int = 0

    val currentLine: DialogueLine?
        get() = if (currentLineIndex in lines.indices) lines[currentLineIndex] else null

    fun advance(): Boolean {
        currentLineIndex++
        return currentLineIndex < lines.size
    }

    fun reset() {
        currentLineIndex = 0
        isTriggered = false
    }
}

/**
 * Prasasti Batu Hitam (The Lore Tablets) yang dapat dibaca di sudut tersembunyi lantai menara.
 */
class LoreTablet(
    val x: Float,
    val y: Float,
    val tabletId: String,
    val title: String,
    val text: String,
    val width: Float = 1.0f,
    val height: Float = 1.4f
) {
    val bounds = Rectangle(x, y, width, height)
    var isRead: Boolean = false
    private var animTime: Float = 0f

    fun update(delta: Float) {
        animTime += delta
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val centerX = x + width / 2f
        val pulse = (sin(animTime * 3.5f) + 1f) * 0.5f

        // 1. Alas Prasasti Batu Hitam Obsidian
        shapeRenderer.setColor(0.12f, 0.15f, 0.22f, 0.95f)
        shapeRenderer.rect(x - 0.1f, y, width + 0.2f, height)

        // 2. Bingkai Emas Prasasti
        shapeRenderer.setColor(0.85f, 0.70f, 0.25f, 0.85f)
        shapeRenderer.rectLine(x - 0.1f, y + height, x + width + 0.1f, y + height, 0.06f)

        // 3. Simbol Rune Bercahaya di Tengah Prasasti
        shapeRenderer.setColor(0.3f + 0.3f * pulse, 0.7f + 0.3f * pulse, 1f, 0.9f)
        shapeRenderer.circle(centerX, y + height * 0.65f, 0.22f, 12)

        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(centerX, y + height * 0.65f, 0.10f, 8)

        // Garis Hieroglif Kuno
        shapeRenderer.setColor(0.6f, 0.8f, 1f, 0.7f)
        shapeRenderer.rectLine(centerX - 0.25f, y + 0.45f, centerX + 0.25f, y + 0.45f, 0.04f)
        shapeRenderer.rectLine(centerX - 0.20f, y + 0.30f, centerX + 0.20f, y + 0.30f, 0.04f)
        shapeRenderer.rectLine(centerX - 0.28f, y + 0.15f, centerX + 0.28f, y + 0.15f, 0.04f)
    }
}

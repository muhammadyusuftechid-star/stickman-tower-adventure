package com.stickman.tower.vfx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class Particle(
    val position: Vector2,
    val velocity: Vector2,
    val size: Float,
    val color: Color,
    val maxLife: Float
) {
    var life: Float = 0f
    val isExpired: Boolean get() = life >= maxLife
}

class SlashEffect(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val startAngle: Float,
    val endAngle: Float,
    val color: Color,
    val maxLife: Float = 0.18f
) {
    var life: Float = 0f
    val isExpired: Boolean get() = life >= maxLife
}

/**
 * Manajer Efek Visual Partikel (VFX):
 * Mengelola partikel debu loncatan/pendaratan, percikan benturan tebasan, dan garis tebasan senjata.
 */
class ParticleManager {
    private val particles = mutableListOf<Particle>()
    private val slashes = mutableListOf<SlashEffect>()

    fun spawnDust(x: Float, y: Float, count: Int = 5) {
        for (i in 0 until count) {
            val vx = MathUtils.random(-1.8f, 1.8f)
            val vy = MathUtils.random(0.3f, 1.4f)
            val size = MathUtils.random(0.06f, 0.12f)
            val life = MathUtils.random(0.2f, 0.4f)
            particles.add(
                Particle(
                    position = Vector2(x, y),
                    velocity = Vector2(vx, vy),
                    size = size,
                    color = Color(0.6f, 0.65f, 0.7f, 0.6f),
                    maxLife = life
                )
            )
        }
    }

    fun spawnHitSparks(x: Float, y: Float, count: Int = 8, color: Color = Color.GOLD) {
        for (i in 0 until count) {
            val angle = MathUtils.random(0f, 360f)
            val speed = MathUtils.random(4f, 10f)
            val vx = MathUtils.cosDeg(angle) * speed
            val vy = MathUtils.sinDeg(angle) * speed
            val size = MathUtils.random(0.04f, 0.08f)
            val life = MathUtils.random(0.12f, 0.25f)
            particles.add(
                Particle(
                    position = Vector2(x, y),
                    velocity = Vector2(vx, vy),
                    size = size,
                    color = color,
                    maxLife = life
                )
            )
        }
    }

    fun spawnSlash(centerX: Float, centerY: Float, radius: Float, startAngle: Float, endAngle: Float, color: Color) {
        slashes.add(
            SlashEffect(
                centerX = centerX,
                centerY = centerY,
                radius = radius,
                startAngle = startAngle,
                endAngle = endAngle,
                color = color
            )
        )
    }

    fun update(delta: Float) {
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.life += delta
            p.position.x += p.velocity.x * delta
            p.position.y += p.velocity.y * delta
            p.velocity.y -= 4f * delta // gravitasi ringan pada partikel
            if (p.isExpired) pIt.remove()
        }

        val sIt = slashes.iterator()
        while (sIt.hasNext()) {
            val s = sIt.next()
            s.life += delta
            if (s.isExpired) sIt.remove()
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        // Render Partikel
        for (p in particles) {
            val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
            shapeRenderer.color = Color(p.color.r, p.color.g, p.color.b, alpha)
            shapeRenderer.circle(p.position.x, p.position.y, p.size, 8)
        }

        // Render Garis Lengkung Tebasan (Slash Arcs)
        for (s in slashes) {
            val alpha = (1f - (s.life / s.maxLife)).coerceIn(0f, 1f)
            shapeRenderer.color = Color(s.color.r, s.color.g, s.color.b, alpha * 0.8f)

            val segments = 8
            val sweep = s.endAngle - s.startAngle
            var prevX = s.centerX + MathUtils.cosDeg(s.startAngle) * s.radius
            var prevY = s.centerY + MathUtils.sinDeg(s.startAngle) * s.radius

            for (i in 1..segments) {
                val ang = s.startAngle + sweep * (i.toFloat() / segments)
                val curX = s.centerX + MathUtils.cosDeg(ang) * s.radius
                val curY = s.centerY + MathUtils.sinDeg(ang) * s.radius
                shapeRenderer.rectLine(prevX, prevY, curX, curY, 0.08f * alpha)
                prevX = curX
                prevY = curY
            }
        }
    }

    fun clear() {
        particles.clear()
        slashes.clear()
    }
}

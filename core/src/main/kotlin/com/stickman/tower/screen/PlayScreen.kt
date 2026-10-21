package com.stickman.tower.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.ScreenUtils
import com.stickman.tower.character.Player
import com.stickman.tower.character.TrainingDummy
import com.stickman.tower.combat.FloatingText
import com.stickman.tower.combat.HitStopManager
import com.stickman.tower.input.KeyboardInput
import com.stickman.tower.physics.Platform
import com.stickman.tower.world.CameraManager

/**
 * Screen gameplay utama dengan implementasi platforming dan sistem combat:
 * serangan dasar, deteksi hitbox/hurtbox, visual tebasan (slash arc),
 * hit stop micro-freeze, flash red feedback, dan floating damage text.
 */
class PlayScreen : Screen {
    private val cameraManager = CameraManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private val input = KeyboardInput()
    private val player = Player(spawnX = 0f, spawnY = 2f)

    private val platforms = mutableListOf<Platform>()
    private val dummies = mutableListOf<TrainingDummy>()
    private val floatingTexts = mutableListOf<FloatingText>()
    private val hitStopManager = HitStopManager()

    private var showDebugBounds = false

    override fun show() {
        // 1. Konfigurasi Batasan Kamera (Level Boundaries)
        cameraManager.setBounds(left = -12f, bottom = -1f, right = 33f, top = 14f)

        // 2. Setup Arena Platform Uji Coba
        platforms.clear()

        // Lantai Dasar Utama
        platforms.add(Platform(x = -12f, y = 0f, width = 45f, height = 1f, color = Color(0.18f, 0.24f, 0.32f, 1f)))

        // Dinding Pembatas Kiri dan Kanan
        platforms.add(Platform(x = -12f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))
        platforms.add(Platform(x = 32f, y = 1f, width = 1f, height = 12f, color = Color(0.14f, 0.18f, 0.24f, 1f)))

        // Platform Mengambang Bertingkat
        platforms.add(Platform(x = 2.5f, y = 2.4f, width = 4f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 8.5f, y = 4.3f, width = 3.5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 14.5f, y = 6.2f, width = 5f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))
        platforms.add(Platform(x = 22f, y = 3.5f, width = 4f, height = 0.5f, color = Color(0.24f, 0.35f, 0.48f, 1f)))

        // Rintangan Kotak Rendah
        platforms.add(Platform(x = -4f, y = 1f, width = 1.5f, height = 1.2f, color = Color(0.28f, 0.32f, 0.40f, 1f)))

        // 3. Setup Target Latihan (Training Dummy)
        dummies.clear()
        dummies.add(TrainingDummy(spawnX = 5.5f, spawnY = 1.2f))  // Dummy di lantai dasar
        dummies.add(TrainingDummy(spawnX = 10f, spawnY = 5.0f))   // Dummy di platform melayang
    }

    override fun render(delta: Float) {
        // Toggle Debug Bounding Box via F1 atau TAB
        if (Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.TAB)) {
            showDebugBounds = !showDebugBounds
        }

        val safeDelta = delta.coerceAtMost(1f / 30f)

        // 1. Perbarui Timer Hit Stop (Micro-Freeze saat tebasan mengenai sasaran)
        val isFrozen = hitStopManager.update(safeDelta)

        if (!isFrozen) {
            // Perbarui Entitas Karakter dan Dummy jika tidak dalam kondisi hit stop
            player.update(safeDelta, input, platforms)
            for (dummy in dummies) {
                dummy.update(safeDelta, platforms)
            }
        }

        // 2. Deteksi Benturan Hitbox Pemain vs Hurtbox Target
        checkCombatCollisions()

        // 3. Perbarui Floating Damage Texts
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.update(safeDelta)
            if (ft.isExpired) {
                iterator.remove()
            }
        }

        // 4. Perbarui Kamera Mengikuti Karakter
        val playerCenterX = player.position.x + player.width / 2f
        val playerCenterY = player.position.y + player.height / 2f + 0.5f
        cameraManager.update(playerCenterX, playerCenterY, safeDelta)

        // 5. Bersihkan Layar (Dark Slate Modern Theme)
        ScreenUtils.clear(0.08f, 0.09f, 0.13f, 1f)

        // 6. Render Dunia 2D Menggunakan ShapeRenderer
        cameraManager.viewport.apply()
        shapeRenderer.projectionMatrix = cameraManager.camera.combined

        // Mode Filled untuk Platform, Target Dummy, dan Pemain
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (platform in platforms) {
            platform.render(shapeRenderer)
        }
        for (dummy in dummies) {
            dummy.render(shapeRenderer)
        }
        player.render(shapeRenderer)
        shapeRenderer.end()

        // Mode Line untuk Debug Hitbox & Hurtbox (jika aktif)
        if (showDebugBounds) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            player.renderDebug(shapeRenderer)
            for (dummy in dummies) {
                dummy.renderDebug(shapeRenderer)
            }
            for (platform in platforms) {
                shapeRenderer.color = Color.YELLOW
                shapeRenderer.rect(platform.bounds.x, platform.bounds.y, platform.bounds.width, platform.bounds.height)
            }
            shapeRenderer.end()
        }

        // 7. Render Floating Damage Numbers
        batch.begin()
        for (ft in floatingTexts) {
            ft.render(batch, font, cameraManager.camera)
        }
        batch.end()

        // 8. Render Informasi Debug Overlay di Pojok Layar
        renderDebugOverlay()
    }

    private fun checkCombatCollisions() {
        if (!player.hitbox.isActive) return

        for (dummy in dummies) {
            // Cek apakah target belum terkena pada ayunan tebasan ini dan hitbox bertabrakan dengan hurtbox
            if (!player.hitbox.hitEntities.contains(dummy) && player.hitbox.bounds.overlaps(dummy.hurtbox.bounds)) {
                player.hitbox.hitEntities.add(dummy)

                val knockX = if (player.facingRight) player.hitbox.knockbackX else -player.hitbox.knockbackX
                val knockY = player.hitbox.knockbackY

                val hitRegistered = dummy.takeHit(player.hitbox.damage, knockX, knockY)
                if (hitRegistered) {
                    // Pemicu Game Juice:
                    // 1. Frame Freeze Mikro (0.05 detik)
                    hitStopManager.trigger(0.05f)

                    // 2. Munculkan teks angka damage melayang
                    floatingTexts.add(
                        FloatingText(
                            text = "-${player.hitbox.damage.toInt()}",
                            worldX = dummy.position.x + dummy.width / 2f,
                            worldY = dummy.position.y + dummy.height + 0.35f,
                            color = Color(1f, 0.25f, 0.25f, 1f)
                        )
                    )
                }
            }
        }
    }

    private fun renderDebugOverlay() {
        batch.begin()
        font.color = Color.CYAN
        font.draw(batch, "Stickman Tower - Phase 2 (Combat & Hitbox Engine)", 15f, Gdx.graphics.height - 15f)

        font.color = Color.WHITE
        font.draw(batch, "FPS: ${Gdx.graphics.framesPerSecond}", 15f, Gdx.graphics.height - 35f)
        font.draw(
            batch,
            "Player State: ${player.state} | AttackPhase: ${player.attackPhase} | HP: ${player.health.currentHp.toInt()}/${player.health.maxHp.toInt()}",
            15f,
            Gdx.graphics.height - 55f
        )
        font.draw(
            batch,
            String.format(
                "Dummy 1 HP: %d/%d | Dummy 2 HP: %d/%d",
                dummies.getOrNull(0)?.health?.currentHp?.toInt() ?: 0,
                dummies.getOrNull(0)?.health?.maxHp?.toInt() ?: 0,
                dummies.getOrNull(1)?.health?.currentHp?.toInt() ?: 0,
                dummies.getOrNull(1)?.health?.maxHp?.toInt() ?: 0
            ),
            15f,
            Gdx.graphics.height - 75f
        )

        font.color = Color.YELLOW
        font.draw(
            batch,
            "Kontrol: [A / D] Gerak | [SPACE / W] Lompat | [J / ENTER / Klik Kiri] TEBASAN PEDANG | [F1 / TAB] Debug Box",
            15f,
            25f
        )
        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        cameraManager.resize(width, height)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
    }
}

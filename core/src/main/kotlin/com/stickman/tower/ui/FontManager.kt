package com.stickman.tower.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter

/**
 * Manajer Tipografi Terpusat untuk Stickman Tower Adventure.
 * Menghasilkan font beresolusi tinggi, anti-aliased dengan linear filtering menggunakan FreeType,
 * serta memuat karakter set lengkap (termasuk bullet, simbol RPG, dan panah) untuk mencegah
 * rendering simbol kotak kosong (□).
 */
object FontManager {
    var isInitialized = false
        private set

    lateinit var titleFont: BitmapFont
        private set
    lateinit var headerFont: BitmapFont
        private set
    lateinit var regularFont: BitmapFont
        private set
    lateinit var smallFont: BitmapFont
        private set

    const val EXTRA_CHARS = "•★⚔◀▶~✓✕"

    fun init() {
        if (isInitialized) return

        try {
            val boldFile = Gdx.files?.internal("fonts/NotoSans-Bold.ttf")
            val regularFile = Gdx.files?.internal("fonts/NotoSans-Regular.ttf")

            if (boldFile != null && boldFile.exists() && regularFile != null && regularFile.exists()) {
                val boldGen = FreeTypeFontGenerator(boldFile)
                val regularGen = FreeTypeFontGenerator(regularFile)

                val paramTitle = FreeTypeFontParameter().apply {
                    size = 32
                    color = Color.WHITE
                    minFilter = Texture.TextureFilter.Linear
                    magFilter = Texture.TextureFilter.Linear
                    characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS
                }
                titleFont = boldGen.generateFont(paramTitle)

                val paramHeader = FreeTypeFontParameter().apply {
                    size = 22
                    color = Color.WHITE
                    minFilter = Texture.TextureFilter.Linear
                    magFilter = Texture.TextureFilter.Linear
                    characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS
                }
                headerFont = boldGen.generateFont(paramHeader)

                val paramRegular = FreeTypeFontParameter().apply {
                    size = 16
                    color = Color.WHITE
                    minFilter = Texture.TextureFilter.Linear
                    magFilter = Texture.TextureFilter.Linear
                    characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS
                }
                regularFont = regularGen.generateFont(paramRegular)

                val paramSmall = FreeTypeFontParameter().apply {
                    size = 13
                    color = Color.WHITE
                    minFilter = Texture.TextureFilter.Linear
                    magFilter = Texture.TextureFilter.Linear
                    characters = FreeTypeFontGenerator.DEFAULT_CHARS + EXTRA_CHARS
                }
                smallFont = regularGen.generateFont(paramSmall)

                boldGen.dispose()
                regularGen.dispose()
                isInitialized = true
                Gdx.app?.log("FontManager", "FreeType fonts successfully generated and cached.")
                return
            }
        } catch (e: Throwable) {
            Gdx.app?.error("FontManager", "Failed to load FreeType font, falling back to default BitmapFont", e)
        }

        // Fallback jika file tidak ditemukan atau di lingkungan test tanpa backend GL
        createFallbackFonts()
    }

    private fun createFallbackFonts() {
        try {
            titleFont = BitmapFont().apply { data.setScale(2.2f) }
            headerFont = BitmapFont().apply { data.setScale(1.6f) }
            regularFont = BitmapFont().apply { data.setScale(1.1f) }
            smallFont = BitmapFont().apply { data.setScale(0.85f) }
        } catch (_: Throwable) {
            // Dalam headless test tanpa Gdx.graphics/gl
        }
        isInitialized = true
    }

    fun dispose() {
        if (!isInitialized) return
        try {
            if (::titleFont.isInitialized) titleFont.dispose()
            if (::headerFont.isInitialized) headerFont.dispose()
            if (::regularFont.isInitialized) regularFont.dispose()
            if (::smallFont.isInitialized) smallFont.dispose()
        } catch (_: Throwable) {}
        isInitialized = false
    }
}

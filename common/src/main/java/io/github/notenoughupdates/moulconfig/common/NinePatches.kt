package io.github.notenoughupdates.moulconfig.common

import io.github.notenoughupdates.moulconfig.GuiTextures
import juuxel.libninepatch.NinePatch

object NinePatches {
    fun createButton(): NinePatch<MyResourceLocation> {
        return NinePatch.builder(GuiTextures.BUTTON)
            .cornerSize(BUTTON_CORNER_SIZE)
            .cornerUv(BUTTON_CORNER_SIZE / BUTTON_TEXTURE_WIDTH, BUTTON_CORNER_SIZE / BUTTON_TEXTURE_HEIGHT)
            .mode(NinePatch.Mode.STRETCHING)
            .build()
    }
    fun createWhiteButton(): NinePatch<MyResourceLocation> {
        return NinePatch.builder(GuiTextures.BUTTON_WHITE)
            .cornerSize(WHITE_BUTTON_CORNER_SIZE)
            .cornerUv(WHITE_BUTTON_CORNER_SIZE / BUTTON_TEXTURE_WIDTH, WHITE_BUTTON_CORNER_SIZE / BUTTON_TEXTURE_HEIGHT)
            .mode(NinePatch.Mode.STRETCHING)
            .build()
    }

    fun createVanillaPanel(): NinePatch<MyResourceLocation> {
        return NinePatch.builder(GuiTextures.VANILLA_PANEL)
            .cornerSize(PANEL_CORNER_SIZE)
            .cornerUv(PANEL_CORNER_SIZE / PANEL_TEXTURE_SIZE)
            .mode(NinePatch.Mode.STRETCHING)
            .build()
    }

    private const val BUTTON_CORNER_SIZE = 10
    private const val WHITE_BUTTON_CORNER_SIZE = 14
    private const val BUTTON_TEXTURE_WIDTH = 32F
    private const val BUTTON_TEXTURE_HEIGHT = 96F
    private const val PANEL_CORNER_SIZE = 4
    private const val PANEL_TEXTURE_SIZE = 16F
}

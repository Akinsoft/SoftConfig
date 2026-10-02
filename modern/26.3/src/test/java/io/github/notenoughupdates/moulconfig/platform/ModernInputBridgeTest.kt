package io.github.notenoughupdates.moulconfig.platform

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.lwjgl.sdl.SDLScancode
import org.lwjgl.sdl.SDLMouse

class ModernInputBridgeTest {
    @Test
    fun retainsPhysicalKeysAndSavedConfigurationIds() {
        for (letter in 0 until 26) {
            assertEquals(SDLScancode.SDL_SCANCODE_A + letter, ModernInputBridge.toNativeKey(65 + letter))
            assertEquals(65 + letter, ModernInputBridge.toLegacyKey(SDLScancode.SDL_SCANCODE_A + letter))
        }
        assertEquals(SDLScancode.SDL_SCANCODE_ESCAPE, ModernInputBridge.toNativeKey(256))
        assertEquals(SDLScancode.SDL_SCANCODE_BACKSPACE, ModernInputBridge.toNativeKey(259))
        assertEquals(SDLScancode.SDL_SCANCODE_KP_PERIOD, ModernInputBridge.toNativeKey(330))
        assertEquals(SDLScancode.SDL_SCANCODE_KP_DIVIDE, ModernInputBridge.toNativeKey(331))
        assertEquals(SDLScancode.SDL_SCANCODE_KP_MINUS, ModernInputBridge.toNativeKey(333))
        assertEquals(SDLScancode.SDL_SCANCODE_LGUI, ModernInputBridge.toNativeKey(343))
        assertEquals(-1, ModernInputBridge.toLegacyKey(SDLScancode.SDL_SCANCODE_UNKNOWN))
        assertEquals(SDLScancode.SDL_SCANCODE_UNKNOWN, ModernInputBridge.toNativeKey(-1))
        for (saved in 32..348) {
            val nativeKey = ModernInputBridge.toNativeKey(saved)
            if (nativeKey != SDLScancode.SDL_SCANCODE_UNKNOWN) {
                assertEquals(saved, ModernInputBridge.toLegacyKey(nativeKey), "Saved key $saved")
            }
        }
    }

    @Test
    fun retainsLogicalMouseButtonsWithoutKeyboardCollisions() {
        assertEquals(SDLMouse.SDL_BUTTON_LEFT, ModernInputBridge.toNativeMouseButton(0))
        assertEquals(SDLMouse.SDL_BUTTON_RIGHT, ModernInputBridge.toNativeMouseButton(1))
        assertEquals(SDLMouse.SDL_BUTTON_MIDDLE, ModernInputBridge.toNativeMouseButton(2))
        for (saved in 0 until 8) {
            assertEquals(saved, ModernInputBridge.toLegacyMouseButton(ModernInputBridge.toNativeMouseButton(saved)))
        }
        assertEquals(65, ModernInputBridge.toLegacyKey(SDLScancode.SDL_SCANCODE_A))
        assertEquals(3, ModernInputBridge.toLegacyMouseButton(SDLMouse.SDL_BUTTON_X1))
    }
}

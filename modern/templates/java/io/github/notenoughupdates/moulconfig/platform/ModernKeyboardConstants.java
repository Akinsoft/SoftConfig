package io.github.notenoughupdates.moulconfig.platform;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.notenoughupdates.moulconfig.common.IKeyboardConstants;

public class ModernKeyboardConstants implements IKeyboardConstants {
    public static final ModernKeyboardConstants INSTANCE = new ModernKeyboardConstants();

    private ModernKeyboardConstants() {}

    @Override
    public int getBackSpace() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_BACKSPACE);
    }

    @Override
    public int getCtrlLeft() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_LCONTROL);
    }

    @Override
    public int getCtrlRight() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_RCONTROL);
    }

    @Override
    public int getCmdLeft() {
        return ModernInputBridge.toLegacyKey(InputConstants.#if MC >= 260300 KEY_LGUI #elif MC > 12107 KEY_LSUPER #else KEY_LWIN #endif);
    }

    @Override
    public int getCmdRight() {
        return ModernInputBridge.toLegacyKey(InputConstants.#if MC >= 260300 KEY_RGUI #elif MC > 12107 KEY_RSUPER #else KEY_RWIN #endif);
    }

    @Override
    public int getShiftLeft() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_LSHIFT);
    }

    @Override
    public int getShiftRight() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_RSHIFT);
    }

    @Override
    public int getEscape() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_ESCAPE);
    }

    @Override
    public int getNone() {
        return -1;
    }

    @Override
    public int getEnter() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_RETURN);
    }

    @Override
    public int getKeypadEnter() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_NUMPADENTER);
    }

    @Override
    public int getDelete() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_DELETE);
    }

    @Override
    public int getUp() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_UP);
    }

    @Override
    public int getDown() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_DOWN);
    }

    @Override
    public int getRight() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_RIGHT);
    }

    @Override
    public int getLeft() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_LEFT);
    }

    @Override
    public int getHome() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_HOME);
    }

    @Override
    public int getEnd() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_END);
    }

    @Override
    public int getKeyA() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_A);
    }

    @Override
    public int getKeyC() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_C);
    }

    @Override
    public int getKeyX() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_X);
    }

    @Override
    public int getKeyV() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_V);
    }

    @Override
    public int getKeyN() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_N);
    }

    @Override
    public int getKeyF() {
        return ModernInputBridge.toLegacyKey(InputConstants.KEY_F);
    }
}

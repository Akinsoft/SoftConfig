package io.github.notenoughupdates.moulconfig.platform;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
#if MC > 12107
import net.minecraft.client.input.KeyEvent;
#endif

public class ModernKeybindHelper {
    #if MC >= 260300
    private static final int MOUSE_BUTTON_KEY_CODE_OFFSET = -100;
    #endif

    public static StructuredText getKeyName(int keyCode) { // TODO: translations
        if (keyCode == InputConstants.UNKNOWN.getValue()) {
            return StructuredText.of("NONE");
        #if MC >= 260300
        } else if (keyCode < 0) {
            return StructuredText.of("Button " + (keyCode - MOUSE_BUTTON_KEY_CODE_OFFSET));
        #else
        } else if (keyCode >= 0 && keyCode <= 9) {
            return StructuredText.of("Button " + (keyCode + 1));
        #endif
        } else {
            #if MC < 12109
            StructuredText keyName = MoulConfigText.wrap(InputConstants.getKey(keyCode, 0).getDisplayName());
            #else
            StructuredText keyName = MoulConfigText.wrap(InputConstants.getKey(new KeyEvent(keyCode, 0, 0)).getDisplayName());
            #endif
            if (keyName == null) {
                keyName = StructuredText.of("???");
            }
            return keyName;
        }
    }

    public static int getMouseButtonKeyCode(int mouseButton) {
        #if MC >= 260300
        return mouseButton + MOUSE_BUTTON_KEY_CODE_OFFSET;
        #else
        return mouseButton;
        #endif
    }
}

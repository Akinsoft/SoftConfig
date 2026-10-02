package io.github.notenoughupdates.moulconfig.platform;

/** Translates native SDL input while preserving the existing integer config contract. */
public final class ModernInputBridge {
    private ModernInputBridge() {}

    public static int toNativeKey(int legacyKey) {
        #if MC >= 260300
        return switch (legacyKey) {
            case 48 -> 39; // KEY_0
            case 49 -> 30; // KEY_1
            case 50 -> 31; // KEY_2
            case 51 -> 32; // KEY_3
            case 52 -> 33; // KEY_4
            case 53 -> 34; // KEY_5
            case 54 -> 35; // KEY_6
            case 55 -> 36; // KEY_7
            case 56 -> 37; // KEY_8
            case 57 -> 38; // KEY_9
            case 65 -> 4; // KEY_A
            case 66 -> 5; // KEY_B
            case 67 -> 6; // KEY_C
            case 68 -> 7; // KEY_D
            case 69 -> 8; // KEY_E
            case 70 -> 9; // KEY_F
            case 71 -> 10; // KEY_G
            case 72 -> 11; // KEY_H
            case 73 -> 12; // KEY_I
            case 74 -> 13; // KEY_J
            case 75 -> 14; // KEY_K
            case 76 -> 15; // KEY_L
            case 77 -> 16; // KEY_M
            case 78 -> 17; // KEY_N
            case 79 -> 18; // KEY_O
            case 80 -> 19; // KEY_P
            case 81 -> 20; // KEY_Q
            case 82 -> 21; // KEY_R
            case 83 -> 22; // KEY_S
            case 84 -> 23; // KEY_T
            case 85 -> 24; // KEY_U
            case 86 -> 25; // KEY_V
            case 87 -> 26; // KEY_W
            case 88 -> 27; // KEY_X
            case 89 -> 28; // KEY_Y
            case 90 -> 29; // KEY_Z
            case 290 -> 58; // KEY_F1
            case 291 -> 59; // KEY_F2
            case 292 -> 60; // KEY_F3
            case 293 -> 61; // KEY_F4
            case 294 -> 62; // KEY_F5
            case 295 -> 63; // KEY_F6
            case 296 -> 64; // KEY_F7
            case 297 -> 65; // KEY_F8
            case 298 -> 66; // KEY_F9
            case 299 -> 67; // KEY_F10
            case 300 -> 68; // KEY_F11
            case 301 -> 69; // KEY_F12
            case 302 -> 104; // KEY_F13
            case 303 -> 105; // KEY_F14
            case 304 -> 106; // KEY_F15
            case 305 -> 107; // KEY_F16
            case 306 -> 108; // KEY_F17
            case 307 -> 109; // KEY_F18
            case 308 -> 110; // KEY_F19
            case 309 -> 111; // KEY_F20
            case 310 -> 112; // KEY_F21
            case 311 -> 113; // KEY_F22
            case 312 -> 114; // KEY_F23
            case 313 -> 115; // KEY_F24
            case 282 -> 83; // KEY_NUMLOCK
            case 320 -> 98; // KEY_NUMPAD0
            case 321 -> 89; // KEY_NUMPAD1
            case 322 -> 90; // KEY_NUMPAD2
            case 323 -> 91; // KEY_NUMPAD3
            case 324 -> 92; // KEY_NUMPAD4
            case 325 -> 93; // KEY_NUMPAD5
            case 326 -> 94; // KEY_NUMPAD6
            case 327 -> 95; // KEY_NUMPAD7
            case 328 -> 96; // KEY_NUMPAD8
            case 329 -> 97; // KEY_NUMPAD9
            case 335 -> 88; // KEY_NUMPADENTER
            case 336 -> 103; // KEY_NUMPADEQUALS
            case 264 -> 81; // KEY_DOWN
            case 263 -> 80; // KEY_LEFT
            case 262 -> 79; // KEY_RIGHT
            case 265 -> 82; // KEY_UP
            case 334 -> 87; // KEY_ADD
            case 39 -> 52; // KEY_APOSTROPHE
            case 92 -> 49; // KEY_BACKSLASH
            case 44 -> 54; // KEY_COMMA
            case 61 -> 46; // KEY_EQUALS
            case 96 -> 53; // KEY_GRAVE
            case 91 -> 47; // KEY_LBRACKET
            case 45 -> 45; // KEY_MINUS
            case 332 -> 85; // KEY_MULTIPLY
            case 46 -> 55; // KEY_PERIOD
            case 93 -> 48; // KEY_RBRACKET
            case 59 -> 51; // KEY_SEMICOLON
            case 47 -> 56; // KEY_SLASH
            case 32 -> 44; // KEY_SPACE
            case 258 -> 43; // KEY_TAB
            case 342 -> 226; // KEY_LALT
            case 341 -> 224; // KEY_LCONTROL
            case 340 -> 225; // KEY_LSHIFT
            case 343 -> 227; // KEY_LSUPER
            case 346 -> 230; // KEY_RALT
            case 345 -> 228; // KEY_RCONTROL
            case 344 -> 229; // KEY_RSHIFT
            case 347 -> 231; // KEY_RSUPER
            case 257 -> 40; // KEY_RETURN
            case 256 -> 41; // KEY_ESCAPE
            case 259 -> 42; // KEY_BACKSPACE
            case 261 -> 76; // KEY_DELETE
            case 269 -> 77; // KEY_END
            case 268 -> 74; // KEY_HOME
            case 260 -> 73; // KEY_INSERT
            case 267 -> 78; // KEY_PAGEDOWN
            case 266 -> 75; // KEY_PAGEUP
            case 280 -> 57; // KEY_CAPSLOCK
            case 284 -> 72; // KEY_PAUSE
            case 281 -> 71; // KEY_SCROLLLOCK
            case 283 -> 70; // KEY_PRINTSCREEN
            case 333 -> 86; // GLFW_KEY_KP_SUBTRACT
            case 331 -> 84; // GLFW_KEY_KP_DIVIDE
            case 330 -> 99; // GLFW_KEY_KP_DECIMAL
            case 348 -> 101; // GLFW_KEY_MENU
            default -> 0; // SDL_SCANCODE_UNKNOWN
        };
        #else
        return legacyKey;
        #endif
    }

    public static int toLegacyKey(int nativeKey) {
        #if MC >= 260300
        return switch (nativeKey) {
            case 39 -> 48; // KEY_0
            case 30 -> 49; // KEY_1
            case 31 -> 50; // KEY_2
            case 32 -> 51; // KEY_3
            case 33 -> 52; // KEY_4
            case 34 -> 53; // KEY_5
            case 35 -> 54; // KEY_6
            case 36 -> 55; // KEY_7
            case 37 -> 56; // KEY_8
            case 38 -> 57; // KEY_9
            case 4 -> 65; // KEY_A
            case 5 -> 66; // KEY_B
            case 6 -> 67; // KEY_C
            case 7 -> 68; // KEY_D
            case 8 -> 69; // KEY_E
            case 9 -> 70; // KEY_F
            case 10 -> 71; // KEY_G
            case 11 -> 72; // KEY_H
            case 12 -> 73; // KEY_I
            case 13 -> 74; // KEY_J
            case 14 -> 75; // KEY_K
            case 15 -> 76; // KEY_L
            case 16 -> 77; // KEY_M
            case 17 -> 78; // KEY_N
            case 18 -> 79; // KEY_O
            case 19 -> 80; // KEY_P
            case 20 -> 81; // KEY_Q
            case 21 -> 82; // KEY_R
            case 22 -> 83; // KEY_S
            case 23 -> 84; // KEY_T
            case 24 -> 85; // KEY_U
            case 25 -> 86; // KEY_V
            case 26 -> 87; // KEY_W
            case 27 -> 88; // KEY_X
            case 28 -> 89; // KEY_Y
            case 29 -> 90; // KEY_Z
            case 58 -> 290; // KEY_F1
            case 59 -> 291; // KEY_F2
            case 60 -> 292; // KEY_F3
            case 61 -> 293; // KEY_F4
            case 62 -> 294; // KEY_F5
            case 63 -> 295; // KEY_F6
            case 64 -> 296; // KEY_F7
            case 65 -> 297; // KEY_F8
            case 66 -> 298; // KEY_F9
            case 67 -> 299; // KEY_F10
            case 68 -> 300; // KEY_F11
            case 69 -> 301; // KEY_F12
            case 104 -> 302; // KEY_F13
            case 105 -> 303; // KEY_F14
            case 106 -> 304; // KEY_F15
            case 107 -> 305; // KEY_F16
            case 108 -> 306; // KEY_F17
            case 109 -> 307; // KEY_F18
            case 110 -> 308; // KEY_F19
            case 111 -> 309; // KEY_F20
            case 112 -> 310; // KEY_F21
            case 113 -> 311; // KEY_F22
            case 114 -> 312; // KEY_F23
            case 115 -> 313; // KEY_F24
            case 83 -> 282; // KEY_NUMLOCK
            case 98 -> 320; // KEY_NUMPAD0
            case 89 -> 321; // KEY_NUMPAD1
            case 90 -> 322; // KEY_NUMPAD2
            case 91 -> 323; // KEY_NUMPAD3
            case 92 -> 324; // KEY_NUMPAD4
            case 93 -> 325; // KEY_NUMPAD5
            case 94 -> 326; // KEY_NUMPAD6
            case 95 -> 327; // KEY_NUMPAD7
            case 96 -> 328; // KEY_NUMPAD8
            case 97 -> 329; // KEY_NUMPAD9
            case 88 -> 335; // KEY_NUMPADENTER
            case 103 -> 336; // KEY_NUMPADEQUALS
            case 81 -> 264; // KEY_DOWN
            case 80 -> 263; // KEY_LEFT
            case 79 -> 262; // KEY_RIGHT
            case 82 -> 265; // KEY_UP
            case 87 -> 334; // KEY_ADD
            case 52 -> 39; // KEY_APOSTROPHE
            case 49 -> 92; // KEY_BACKSLASH
            case 54 -> 44; // KEY_COMMA
            case 46 -> 61; // KEY_EQUALS
            case 53 -> 96; // KEY_GRAVE
            case 47 -> 91; // KEY_LBRACKET
            case 45 -> 45; // KEY_MINUS
            case 85 -> 332; // KEY_MULTIPLY
            case 55 -> 46; // KEY_PERIOD
            case 48 -> 93; // KEY_RBRACKET
            case 51 -> 59; // KEY_SEMICOLON
            case 56 -> 47; // KEY_SLASH
            case 44 -> 32; // KEY_SPACE
            case 43 -> 258; // KEY_TAB
            case 226 -> 342; // KEY_LALT
            case 224 -> 341; // KEY_LCONTROL
            case 225 -> 340; // KEY_LSHIFT
            case 227 -> 343; // KEY_LSUPER
            case 230 -> 346; // KEY_RALT
            case 228 -> 345; // KEY_RCONTROL
            case 229 -> 344; // KEY_RSHIFT
            case 231 -> 347; // KEY_RSUPER
            case 40 -> 257; // KEY_RETURN
            case 41 -> 256; // KEY_ESCAPE
            case 42 -> 259; // KEY_BACKSPACE
            case 76 -> 261; // KEY_DELETE
            case 77 -> 269; // KEY_END
            case 74 -> 268; // KEY_HOME
            case 73 -> 260; // KEY_INSERT
            case 78 -> 267; // KEY_PAGEDOWN
            case 75 -> 266; // KEY_PAGEUP
            case 57 -> 280; // KEY_CAPSLOCK
            case 72 -> 284; // KEY_PAUSE
            case 71 -> 281; // KEY_SCROLLLOCK
            case 70 -> 283; // KEY_PRINTSCREEN
            case 86 -> 333; // GLFW_KEY_KP_SUBTRACT
            case 84 -> 331; // GLFW_KEY_KP_DIVIDE
            case 99 -> 330; // GLFW_KEY_KP_DECIMAL
            case 101 -> 348; // GLFW_KEY_MENU
            default -> -1;
        };
        #else
        return nativeKey;
        #endif
    }

    public static int toNativeMouseButton(int legacyButton) {
        #if MC >= 260300
        return switch (legacyButton) {
            case 0 -> 1;
            case 1 -> 3;
            case 2 -> 2;
            default -> legacyButton + 1;
        };
        #else
        return legacyButton;
        #endif
    }

    public static int toLegacyMouseButton(int nativeButton) {
        #if MC >= 260300
        return switch (nativeButton) {
            case 1 -> 0;
            case 3 -> 1;
            case 2 -> 2;
            default -> nativeButton - 1;
        };
        #else
        return nativeButton;
        #endif
    }
}

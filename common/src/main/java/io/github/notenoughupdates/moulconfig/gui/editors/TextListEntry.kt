/*
 * Copyright (C) 2026 SoftConfig contributors
 *
 * This file is part of SoftConfig.
 *
 * SoftConfig is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * SoftConfig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SoftConfig. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package io.github.notenoughupdates.moulconfig.gui.editors

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour

data class TextListEntry @JvmOverloads constructor(
    @field:Expose
    var text: String = "",
    @field:Expose
    var colour: ChromaColour = DEFAULT_COLOUR,
    @field:Expose
    var isSoundEnabled: Boolean = false,
    @field:Expose
    var soundVolumePercent: Float = DEFAULT_SOUND_VOLUME_PERCENT,
    @field:Expose
    var sound: String = "",
) {
    val playbackVolume: Float
        get() = DEFAULT_PLAYBACK_VOLUME * soundVolumePercent / DEFAULT_SOUND_VOLUME_PERCENT

    companion object {
        const val MIN_SOUND_VOLUME_PERCENT = 0f
        const val DEFAULT_SOUND_VOLUME_PERCENT = 50f
        const val MAX_SOUND_VOLUME_PERCENT = 450f
        const val DEFAULT_PLAYBACK_VOLUME = 0.25f
        private val DEFAULT_COLOUR: ChromaColour = ChromaColour.fromStaticRGB(255, 255, 255, 255)
    }
}

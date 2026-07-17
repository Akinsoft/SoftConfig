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

import java.awt.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextListEditorModelTest {
    @Test
    fun `entries can be added reordered configured and removed`() {
        val entries = mutableListOf<TextListEntry>()
        var changes = 0
        val model = TextListEditorModel(entries) { changes++ }

        assertEquals(TextListEditorModel.ChangeResult.UNCHANGED, model.addEntry("   "))
        assertEquals(TextListEditorModel.ChangeResult.CHANGED, model.addEntry(" auction "))
        assertEquals(TextListEditorModel.ChangeResult.UNCHANGED, model.addEntry("AUCTION"))
        assertEquals(TextListEditorModel.ChangeResult.CHANGED, model.addEntry("rare drop"))
        assertEquals(TextListEditorModel.ChangeResult.CHANGED, model.moveEntry(1, 0))
        model.toggleSound(entries.first())
        model.setSoundVolume(entries.first(), 500f)
        model.setSound(entries.first(), "minecraft:block.note_block.bell")
        assertEquals(TextListEditorModel.ChangeResult.CHANGED, model.removeEntry(1))

        assertEquals(listOf("rare drop"), entries.map(TextListEntry::text))
        assertTrue(entries.single().isSoundEnabled)
        assertEquals(TextListEntry.MAX_SOUND_VOLUME_PERCENT, entries.single().soundVolumePercent)
        assertEquals(2.25f, entries.single().playbackVolume)
        assertEquals("minecraft:block.note_block.bell", entries.single().sound)
        assertEquals(Color.WHITE.rgb, entries.single().colour.getEffectiveColourRGB())
        assertEquals(7, changes)
    }

    @Test
    fun `entry text can be edited without allowing blanks or duplicates`() {
        val first = TextListEntry("first")
        val second = TextListEntry("second")
        val entries = mutableListOf(first, second)
        var changes = 0
        val model = TextListEditorModel(entries) { changes++ }

        assertEquals(TextListEditorModel.ChangeResult.UNCHANGED, model.setText(first, " "))
        assertEquals(TextListEditorModel.ChangeResult.UNCHANGED, model.setText(first, "SECOND"))
        assertEquals(TextListEditorModel.ChangeResult.CHANGED, model.setText(first, " updated "))

        assertEquals("updated", first.text)
        assertEquals(1, changes)
    }
}

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

import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.function.ToIntFunction

class ChoiceListComponentTest {
    @Test
    fun `picker width follows its longest label`() {
        val placement = measure(listOf("Short", "A much longer choice"), 300, 200)

        assertEquals(128, placement.width)
    }

    @Test
    fun `picker retains its minimum width for short labels`() {
        val placement = measure(listOf("Short"), 300, 200)

        assertEquals(100, placement.width)
    }

    @Test
    fun `picker caps its rows and stays within the screen`() {
        val choices = (1..20).map { "Choice $it" }
        val placement = measure(choices, 300, 1_000, anchorX = 290, anchorY = 990)

        assertEquals(196, placement.x)
        assertEquals(850, placement.y)
        assertEquals(100, placement.width)
        assertEquals(146, placement.height)
    }

    @Test
    fun `picker size is clamped to small screens`() {
        val choices = (1..20).map { "A much longer choice $it" }
        val placement = measure(choices, 80, 100, anchorX = 70, anchorY = 90)

        assertEquals(4, placement.x)
        assertEquals(4, placement.y)
        assertEquals(72, placement.width)
        assertEquals(92, placement.height)
    }

    private fun measure(
        choices: List<String>,
        screenWidth: Int,
        screenHeight: Int,
        anchorX: Int = 20,
        anchorY: Int = 20,
    ): ChoiceListComponent.Placement = ChoiceListComponent.measure(
        choices,
        { StructuredText.of(it) },
        ToIntFunction { it.text.length * 6 },
        anchorX,
        anchorY,
        screenWidth,
        screenHeight,
        100,
        12,
        8,
        4,
    )
}

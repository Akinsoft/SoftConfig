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

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.CenterComponent
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import java.util.Locale

class GuiOptionEditorCombinations(
    option: ProcessedOption,
    providerClass: Class<out ConfigEditorCombinationsProvider<*, *>>,
) : ComponentEditor(option) {
    private val model = CombinationEditorModel(
        option,
        ConfigEditorCombinationsProviderResolver.resolve(providerClass),
    )
    private val component = wrapComponent(
        AddCombinationComponent { openChoiceOverlay(null) },
        LeftColumnCenterComponent(CombinationRowsComponent(model) { openChoiceOverlay(it) }),
    )

    override fun getDelegate(): GuiComponent = component

    override fun fulfillsSearch(word: String): Boolean =
        super.fulfillsSearch(word) || model.searchText.contains(word.lowercase(Locale.ROOT))

    private fun openChoiceOverlay(combinationIndex: Int?) {
        val choices = model.getAvailableChoices(combinationIndex)
        if (choices.isEmpty()) return
        val minecraft = IMinecraft.INSTANCE
        val mousePosition = minecraft.mousePosition
        val placement = ChoiceListComponent.measure(
            choices,
            model::getChoiceLabel,
            mousePosition.first,
            mousePosition.second,
            CombinationRowsComponent.EDITOR_WIDTH,
            Dimensions.MAX_VISIBLE_CHOICES,
            Dimensions.CHOICE_HORIZONTAL_INSET,
            Dimensions.OVERLAY_MARGIN,
        )
        openOverlay(
            ChoiceListComponent(
                choices,
                placement.width,
                placement.height,
                model::getChoiceLabel,
                { choice ->
                    if (model.addChoice(combinationIndex, choice)) closeOverlay()
                },
                ::closeOverlay,
            ),
            placement.x,
            placement.y,
        )
    }

    private class AddCombinationComponent(private val openChoices: () -> Unit) : GuiComponent() {
        override fun getWidth(): Int = CombinationRowsComponent.EDITOR_WIDTH

        override fun getHeight(): Int = Dimensions.ADD_BUTTON_HEIGHT

        override fun render(context: GuiImmediateContext) {
            CombinationRowsComponent.drawButton(
                context,
                buttonLeft(),
                0,
                Dimensions.ADD_BUTTON_WIDTH,
                Dimensions.ADD_BUTTON_HEIGHT,
                StructuredText.of("Add"),
            )
        }

        override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
            if (
                mouseEvent is MouseEvent.Click &&
                mouseEvent.mouseState &&
                mouseEvent.mouseButton == 0 &&
                context.mouseX in buttonLeft() until buttonLeft() + Dimensions.ADD_BUTTON_WIDTH &&
                context.mouseY in 0 until Dimensions.ADD_BUTTON_HEIGHT
            ) {
                openChoices()
                return true
            }
            return false
        }

        private fun buttonLeft(): Int =
            (CombinationRowsComponent.EDITOR_WIDTH - Dimensions.ADD_BUTTON_WIDTH) / 2
    }

    private object Dimensions {
        const val OVERLAY_MARGIN = 4
        const val CHOICE_HORIZONTAL_INSET = 8
        const val MAX_VISIBLE_CHOICES = 12
        const val ADD_BUTTON_WIDTH = 48
        const val ADD_BUTTON_HEIGHT = 16
    }
}

private class LeftColumnCenterComponent(child: GuiComponent) : CenterComponent(child) {
    override fun getChildOffsetX(context: GuiImmediateContext): Int {
        val leftColumnWidth = (context.width + BOTTOM_CONTEXT_INSET) / COLUMN_COUNT - LEFT_COLUMN_INSET
        return maxOf(0, leftColumnWidth / 2 - width / 2)
    }

    private companion object {
        const val BOTTOM_CONTEXT_INSET = 10
        const val COLUMN_COUNT = 3
        const val LEFT_COLUMN_INSET = 10
    }
}

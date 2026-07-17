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

import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.GuiTextures
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.KeyboardConstants
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.common.RenderContext
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.ButtonComponent
import io.github.notenoughupdates.moulconfig.gui.component.CenterComponent
import io.github.notenoughupdates.moulconfig.gui.component.ColorSelectComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.gui.component.SpacerComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.internal.ColourUtil
import io.github.notenoughupdates.moulconfig.internal.LerpingInteger2
import io.github.notenoughupdates.moulconfig.internal.Rect
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import java.util.Locale
import java.util.function.BiFunction
import kotlin.math.max

private data class AudioControlPositions(val playX: Int, val gearX: Int)

private fun audioControlPositions(
    speakerX: Int,
    sliderWidth: Int,
    isVisible: Boolean,
    gap: Int,
): AudioControlPositions? {
    if (!isVisible || sliderWidth <= 0) return null
    val gearX = speakerX - gap - sliderWidth - gap - AudioControlIcons.gearWidth
    return AudioControlPositions(gearX - gap - AudioControlIcons.playWidth, gearX)
}

private fun resolveSound(entry: TextListEntry, defaultSound: MyResourceLocation?): MyResourceLocation? =
    entry.sound.takeIf(String::isNotBlank)?.let(MyResourceLocation::parse) ?: defaultSound

private object AudioControlIcons {
    val gearWidth: Int
        get() = GEAR_PIXELS.first().length
    val playWidth: Int
        get() = PLAY_PIXELS.first().length

    fun drawGear(context: GuiImmediateContext, x: Int, rowY: Int) {
        draw(context, x, rowY, GEAR_Y, GEAR_PIXELS)
    }

    fun drawPlay(context: GuiImmediateContext, x: Int, rowY: Int) {
        draw(context, x, rowY, PLAY_Y, PLAY_PIXELS)
    }

    private fun draw(context: GuiImmediateContext, x: Int, rowY: Int, yOffset: Int, pixels: List<String>) {
        val y = rowY + yOffset
        val width = pixels.first().length
        val isHovered = context.mouseX in x until x + width && context.mouseY in y until y + pixels.size
        val colour = if (isHovered) HOVER_COLOUR else COLOUR
        pixels.forEachIndexed { row, line ->
            line.forEachIndexed { column, pixel ->
                if (pixel == FILLED_PIXEL) {
                    context.renderContext.drawColoredRect(
                        (x + column).toFloat(),
                        (y + row).toFloat(),
                        (x + column + 1).toFloat(),
                        (y + row + 1).toFloat(),
                        colour,
                    )
                }
            }
        }
    }

    private const val GEAR_Y = 3
    private const val PLAY_Y = 4
    private const val FILLED_PIXEL = '#'
    private val HOVER_COLOUR = 0xFFFFFFFF.toInt()
    private val COLOUR = 0xFFB8B8B8.toInt()
    private val GEAR_PIXELS = listOf(
        "...###...",
        ".##...##.",
        ".#######.",
        "###...###",
        "##.....##",
        "###...###",
        ".#######.",
        ".##...##.",
        "...###...",
    )
    private val PLAY_PIXELS = listOf(
        "##.....",
        "####...",
        "######.",
        "#######",
        "######.",
        "####...",
        "##.....",
    )
}

class GuiOptionEditorTextList(
    option: ProcessedOption,
    disabledSound: String = "",
    enabledSound: String = "",
    defaultSound: String = "",
    private val showColour: Boolean = true,
    private val showNotification: Boolean = true,
    private val showVolume: Boolean = true,
) : ComponentEditor(option) {
    private val model = TextListEditorModel(option.get(), option::explicitNotifyChange)
    private val draft = GetSetter.floating("")
    private val disabledSoundId = disabledSound.takeIf(String::isNotBlank)?.let(MyResourceLocation::parse)
    private val enabledSoundId = enabledSound.takeIf(String::isNotBlank)?.let(MyResourceLocation::parse)
    private val defaultSoundId = defaultSound.takeIf(String::isNotBlank)?.let(MyResourceLocation::parse)
    private var dragStartIndex = -1
    private var listRenderX = 0
    private var listRenderY = 0
    private var volumeEntry: TextListEntry? = null
    private var volumeSlider: SliderComponent? = null
    private var isVolumeExpanded = false
    private var volumeControlWidth = 0
    private var trashBounds: Rect? = null
    private val trashAnimation = LerpingInteger2(255, 3, 2)
    private val volumeAnimation = LerpingInteger2(0, VOLUME_ANIMATION_SPEED, VOLUME_ANIMATION_SCALE)
    private val colourControlX = FIRST_CONTROL_X.takeIf { showColour }
    private val notificationControlX = if (showNotification) {
        colourControlX?.plus(COLOUR_SIZE + ROW_CONTROL_GAP) ?: FIRST_CONTROL_X
    } else {
        null
    }
    private val textX = notificationControlX?.plus(BELL_WIDTH + TEXT_CONTROL_GAP)
        ?: colourControlX?.plus(COLOUR_SIZE + TEXT_CONTROL_GAP)
        ?: FIRST_CONTROL_X
    private val addInputComponent = AddInputComponent()
    private val listComponent = ListComponent()
    private val component = wrapComponent(createInputRow(), listComponent)

    override fun getDelegate(): GuiComponent = component

    override fun fulfillsSearch(word: String): Boolean =
        super.fulfillsSearch(word) || model.searchText().contains(word.lowercase(Locale.ROOT))

    private fun createInputRow(): GuiComponent {
        return RowComponent(
            addInputComponent,
            SpacerComponent(GetSetter.constant(CONTROL_GAP), GetSetter.constant(0)),
            TrashComponent(),
        )
    }

    private fun openColourPicker(entry: TextListEntry, context: GuiImmediateContext) {
        @Suppress("DEPRECATION")
        val picker = ColorSelectComponent(
            0,
            0,
            entry.colour.toLegacyString(),
            { value ->
                @Suppress("DEPRECATION")
                model.setColour(entry, ChromaColour.forLegacyString(value))
            },
            ::closeOverlay,
        )
        val minecraft = context.renderContext.minecraft
        val x = context.absoluteMouseX.coerceIn(0, max(0, minecraft.scaledWidth - picker.width))
        val y = context.absoluteMouseY.coerceIn(0, max(0, minecraft.scaledHeight - picker.height))
        openOverlay(picker, x, y)
    }

    private fun startDragging(index: Int, context: GuiImmediateContext, rowTop: Int) {
        dragStartIndex = index
        val mouseOffsetY = context.mouseY - rowTop
        val dragComponent = DragComponent(model.entries[index], context.width, context.mouseX, mouseOffsetY)
        openOverlay(
            dragComponent,
            context.absoluteMouseX - context.mouseX,
            context.absoluteMouseY - mouseOffsetY,
        )
    }

    private fun drawRow(context: GuiImmediateContext, entry: TextListEntry, y: Int) {
        val renderContext = context.renderContext
        val font = IMinecraft.INSTANCE.defaultFontRenderer
        renderContext.drawString(font, StructuredText.of("≡"), HANDLE_X, y + ROW_TEXT_Y, HANDLE_COLOUR, true)
        val colour = if (showColour) {
            ColourUtil.makeOpaque(entry.colour.getEffectiveColour().rgb)
        } else {
            DEFAULT_TEXT_COLOUR
        }
        colourControlX?.let { x ->
            renderContext.drawComplexTexture(
                GuiTextures.COLOUR_SELECTOR_DOT,
                x.toFloat(),
                (y + COLOUR_Y).toFloat(),
                COLOUR_SIZE.toFloat(),
                COLOUR_SIZE.toFloat(),
            ) { draw -> draw.color(colour) }
        }
        notificationControlX?.let { drawBell(context, it, y + BELL_Y, entry.isSoundEnabled) }
        val shouldShowAudioControls = shouldShowAudioControls(entry)
        val speakerX = speakerX(context.width)
        val sliderWidth = if (shouldShowAudioControls && entry === volumeEntry) volumeControlWidth else 0
        val sliderX = speakerX - CONTROL_GAP - sliderWidth
        val textRight = textRight(context.width, entry)
        renderContext.drawStringScaledMaxWidth(
            StructuredText.of(entry.text),
            font,
            textX,
            y + ROW_TEXT_Y,
            true,
            max(0, textRight - textX),
            colour,
        )
        if (sliderWidth >= MIN_VOLUME_SLIDER_RENDER_WIDTH) {
            renderContext.pushMatrix()
            renderContext.translate(sliderX.toFloat(), y.toFloat())
            volumeSlider?.render(context.translated(sliderX, y, sliderWidth, ROW_HEIGHT))
            renderContext.popMatrix()
        }
        val controls = if (shouldShowAudioControls) {
            audioControlPositions(speakerX, sliderWidth, entry === volumeEntry, CONTROL_GAP)
        } else {
            null
        }
        controls?.let {
            AudioControlIcons.drawGear(context, it.gearX, y)
            AudioControlIcons.drawPlay(context, it.playX, y)
        }
        if (shouldShowAudioControls) drawSpeaker(context, entry, speakerX, y)
    }

    private fun drawSpeaker(context: GuiImmediateContext, entry: TextListEntry, x: Int, rowY: Int) {
        val hitboxY = rowY + SPEAKER_HITBOX_Y
        val isHovered = (
            context.mouseX in x until x + SPEAKER_WIDTH &&
                context.mouseY in hitboxY until hitboxY + SPEAKER_HEIGHT
            )
        val isMuted = entry.soundVolumePercent <= TextListEntry.MIN_SOUND_VOLUME_PERCENT
        val colour = when {
            isMuted && isHovered -> SPEAKER_MUTED_HOVER_COLOUR
            isMuted -> SPEAKER_MUTED_COLOUR
            isHovered -> SPEAKER_HOVER_COLOUR
            else -> SPEAKER_COLOUR
        }
        val render = context.renderContext
        render.drawColoredRect(
            x.toFloat(),
            (hitboxY + SPEAKER_BOX_TOP).toFloat(),
            (x + SPEAKER_BOX_RIGHT).toFloat(),
            (hitboxY + SPEAKER_BOX_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            (x + SPEAKER_CONE_MIDDLE_LEFT).toFloat(),
            (hitboxY + SPEAKER_CONE_MIDDLE_TOP).toFloat(),
            (x + SPEAKER_CONE_MIDDLE_RIGHT).toFloat(),
            (hitboxY + SPEAKER_CONE_MIDDLE_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            (x + SPEAKER_CONE_OUTER_LEFT).toFloat(),
            (hitboxY + SPEAKER_CONE_OUTER_TOP).toFloat(),
            (x + SPEAKER_CONE_OUTER_RIGHT).toFloat(),
            (hitboxY + SPEAKER_CONE_OUTER_BOTTOM).toFloat(),
            colour,
        )
        drawSpeakerWaves(render, x, hitboxY, speakerWaveCount(entry.soundVolumePercent), colour)
    }

    private fun speakerX(width: Int): Int = width - SPEAKER_RIGHT_PADDING - SPEAKER_WIDTH

    private fun shouldShowAudioControls(entry: TextListEntry): Boolean =
        showVolume && (!showNotification || entry.isSoundEnabled)

    private fun textRight(width: Int, entry: TextListEntry): Int {
        if (!shouldShowAudioControls(entry)) return width - TEXT_RIGHT_PADDING
        val speakerX = speakerX(width)
        val sliderWidth = if (entry === volumeEntry) volumeControlWidth else 0
        return if (sliderWidth > 0) {
            audioControlPositions(speakerX, sliderWidth, true, CONTROL_GAP)!!.playX - TEXT_RIGHT_PADDING
        } else {
            speakerX - TEXT_RIGHT_PADDING
        }
    }

    private fun drawSpeakerWaves(render: RenderContext, x: Int, y: Int, count: Int, colour: Int) {
        if (count >= SPEAKER_ONE_WAVE) {
            render.drawColoredRect(
                (x + SPEAKER_WAVE_ONE_X).toFloat(),
                (y + SPEAKER_WAVE_ONE_TOP).toFloat(),
                (x + SPEAKER_WAVE_ONE_X + SPEAKER_WAVE_WIDTH).toFloat(),
                (y + SPEAKER_WAVE_ONE_BOTTOM).toFloat(),
                colour,
            )
        }
        if (count >= SPEAKER_TWO_WAVES) {
            drawSplitWave(render, x + SPEAKER_WAVE_TWO_X, y, SPEAKER_WAVE_TWO_TOP, SPEAKER_WAVE_TWO_BOTTOM, colour)
        }
        if (count >= SPEAKER_THREE_WAVES) {
            drawSplitWave(render, x + SPEAKER_WAVE_THREE_X, y, SPEAKER_WAVE_THREE_TOP, SPEAKER_WAVE_THREE_BOTTOM, colour)
        }
    }

    private fun drawSplitWave(
        render: RenderContext,
        x: Int,
        y: Int,
        top: Int,
        bottom: Int,
        colour: Int,
    ) {
        render.drawColoredRect(
            x.toFloat(),
            (y + top).toFloat(),
            (x + SPEAKER_WAVE_WIDTH).toFloat(),
            (y + top + SPEAKER_WAVE_SEGMENT_HEIGHT).toFloat(),
            colour,
        )
        render.drawColoredRect(
            x.toFloat(),
            (y + bottom).toFloat(),
            (x + SPEAKER_WAVE_WIDTH).toFloat(),
            (y + bottom + SPEAKER_WAVE_SEGMENT_HEIGHT).toFloat(),
            colour,
        )
    }

    private fun speakerWaveCount(volume: Float): Int = when {
        volume <= TextListEntry.MIN_SOUND_VOLUME_PERCENT -> 0
        volume <= SPEAKER_ONE_WAVE_MAX_VOLUME -> SPEAKER_ONE_WAVE
        volume <= SPEAKER_TWO_WAVE_MAX_VOLUME -> SPEAKER_TWO_WAVES
        else -> SPEAKER_THREE_WAVES
    }

    private fun openVolume(entry: TextListEntry) {
        if (entry === volumeEntry && isVolumeExpanded) {
            closeVolume()
            return
        }
        volumeEntry = entry
        volumeSlider = SliderComponent(
            object : GetSetter<Float> {
                override fun get(): Float = entry.soundVolumePercent

                override fun set(newValue: Float) {
                    model.setSoundVolume(entry, newValue)
                }
            },
            TextListEntry.MIN_SOUND_VOLUME_PERCENT,
            TextListEntry.MAX_SOUND_VOLUME_PERCENT,
            VOLUME_STEP,
            VOLUME_SLIDER_WIDTH,
        )
        isVolumeExpanded = true
        volumeAnimation.setTarget(VOLUME_SLIDER_WIDTH)
    }

    private fun closeVolume() {
        isVolumeExpanded = false
        volumeAnimation.setTarget(0)
    }

    private fun openSoundPicker(entry: TextListEntry) {
        val selected = resolveSound(entry, defaultSoundId)
        val choices = IMinecraft.INSTANCE.soundIds.toMutableList()
        selected?.let {
            choices.remove(it)
            choices.add(0, it)
        }
        openChoiceOverlay(
            choices,
            { StructuredText.of(soundName(it)) },
            { if (it == selected) SOUND_SELECTED_COLOUR else SOUND_DEFAULT_COLOUR },
            {
                model.setSound(entry, soundName(it))
                closeOverlay()
            },
        )
    }

    private fun previewSound(entry: TextListEntry) {
        resolveSound(entry, defaultSoundId)?.let { IMinecraft.INSTANCE.playSound(it, entry.playbackVolume) }
    }

    private fun soundName(sound: MyResourceLocation): String = "${sound.root}:${sound.path}"

    private fun volumeSliderContext(context: GuiImmediateContext): GuiImmediateContext? {
        val entry = volumeEntry ?: return null
        if (!shouldShowAudioControls(entry)) return null
        val row = model.entries.indexOf(entry)
        if (row < 0 || volumeControlWidth < MIN_VOLUME_SLIDER_RENDER_WIDTH) return null
        val x = speakerX(context.width) - CONTROL_GAP - volumeControlWidth
        return context.translated(x, LIST_BORDER + row * ROW_HEIGHT, volumeControlWidth, ROW_HEIGHT)
    }

    private fun isInsideVolumeControl(context: GuiImmediateContext): Boolean {
        val entry = volumeEntry ?: return false
        val row = model.entries.indexOf(entry)
        if (row < 0) return false
        val rowTop = LIST_BORDER + row * ROW_HEIGHT
        if (context.mouseY !in rowTop until rowTop + ROW_HEIGHT) return false
        val speakerX = speakerX(context.width)
        val sliderX = speakerX - CONTROL_GAP - volumeControlWidth
        val left = audioControlPositions(speakerX, volumeControlWidth, entry === volumeEntry, CONTROL_GAP)?.playX
            ?: sliderX
        return context.mouseX in left until speakerX + SPEAKER_WIDTH
    }

    private fun drawBell(context: GuiImmediateContext, x: Int, y: Int, isEnabled: Boolean) {
        val isHovered = context.mouseX in x until x + BELL_WIDTH && context.mouseY in y until y + BELL_HEIGHT
        val colour = when {
            isEnabled && isHovered -> BELL_ENABLED_HOVER_COLOUR
            isEnabled -> BELL_ENABLED_COLOUR
            isHovered -> BELL_DISABLED_HOVER_COLOUR
            else -> BELL_DISABLED_COLOUR
        }
        val render = context.renderContext
        render.drawColoredRect(
            (x + BELL_CENTRE_LEFT).toFloat(),
            y.toFloat(),
            (x + BELL_CENTRE_RIGHT).toFloat(),
            (y + BELL_CROWN_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            (x + BELL_DOME_LEFT).toFloat(),
            (y + BELL_CROWN_BOTTOM).toFloat(),
            (x + BELL_DOME_RIGHT).toFloat(),
            (y + BELL_DOME_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            (x + BELL_BODY_LEFT).toFloat(),
            (y + BELL_DOME_BOTTOM).toFloat(),
            (x + BELL_BODY_RIGHT).toFloat(),
            (y + BELL_BODY_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            x.toFloat(),
            (y + BELL_BODY_BOTTOM).toFloat(),
            (x + BELL_LIP_RIGHT).toFloat(),
            (y + BELL_LIP_BOTTOM).toFloat(),
            colour,
        )
        render.drawColoredRect(
            (x + BELL_CENTRE_LEFT).toFloat(),
            (y + BELL_LIP_BOTTOM).toFloat(),
            (x + BELL_CENTRE_RIGHT).toFloat(),
            (y + BELL_HEIGHT).toFloat(),
            colour,
        )
    }

    private inner class AddInputComponent : GuiComponent() {
        private var isExpanded = false
        private var editingEntry: TextListEntry? = null
        private val widthAnimation = LerpingInteger2(ADD_BUTTON_WIDTH, ADD_INPUT_ANIMATION_SPEED, 1)
        private val input = object : TextFieldComponent(draft, INPUT_WIDTH, GetSetter.constant(true), "Word or phrase") {
            override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
                if (
                    isFocused && event is KeyboardEvent.KeyPressed && event.pressed &&
                    (event.keycode == KeyboardConstants.enter || event.keycode == KeyboardConstants.keypadEnter)
                ) {
                    confirm()
                    return true
                }
                return super.keyboardEvent(event, context)
            }
        }
        private val addButton = ButtonComponent(
            CenterComponent(TextComponent(StructuredText.of("Add"))),
            2,
            ::expand,
        )

        override fun getWidth(): Int = widthAnimation.value

        override fun getHeight(): Int = CONTROL_HEIGHT

        override fun render(context: GuiImmediateContext) {
            activeComponent().render(context)
        }

        override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
            if (
                isExpanded && mouseEvent is MouseEvent.Click && mouseEvent.mouseState && !context.isHovered
            ) {
                collapse()
                return false
            }
            return activeComponent().mouseEvent(mouseEvent, context)
        }

        override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
            if (
                isExpanded && event is KeyboardEvent.KeyPressed && event.pressed &&
                event.keycode == KeyboardConstants.escape
            ) {
                collapse()
                return true
            }
            return activeComponent().keyboardEvent(event, context)
        }

        override fun <T> foldChildren(initial: T, visitor: BiFunction<GuiComponent, T, T>): T =
            visitor.apply(input, visitor.apply(addButton, initial))

        private fun activeComponent(): GuiComponent = if (isExpanded) input else addButton

        private fun expand() {
            editingEntry = null
            draft.set("")
            showInput()
        }

        fun edit(entry: TextListEntry) {
            editingEntry = entry
            draft.set(entry.text)
            showInput()
        }

        private fun showInput() {
            isExpanded = true
            widthAnimation.setTarget(INPUT_WIDTH)
            input.requestFocus()
        }

        private fun confirm() {
            val entry = editingEntry
            val result = if (entry == null) {
                model.addEntry(draft.get())
            } else {
                model.setText(entry, draft.get())
            }
            if (
                result == TextListEditorModel.ChangeResult.CHANGED ||
                entry != null && draft.get().trim() == entry.text
            ) {
                collapse()
            }
        }

        private fun collapse() {
            isExpanded = false
            widthAnimation.setTarget(ADD_BUTTON_WIDTH)
            input.blur()
            draft.set("")
            editingEntry = null
        }
    }

    private fun reorderDraggedEntry(mouseX: Int, overlayTop: Int, width: Int) {
        if (mouseX !in listRenderX..listRenderX + width) return
        if (model.entries.isEmpty()) return
        val target = ((overlayTop + ROW_HEIGHT / 2 - listRenderY - LIST_BORDER) / ROW_HEIGHT)
            .coerceIn(0, model.entries.lastIndex)
        if (model.moveEntry(dragStartIndex, target) == TextListEditorModel.ChangeResult.CHANGED) {
            dragStartIndex = target
        }
    }

    private inner class TrashComponent : GuiComponent() {
        override fun getWidth(): Int = TRASH_WIDTH

        override fun getHeight(): Int = CONTROL_HEIGHT

        override fun render(context: GuiImmediateContext) {
            trashAnimation.setTarget(if (context.isHovered && dragStartIndex >= 0) 0 else FULL_TINT)
            val nonRedTints = trashAnimation.value
            context.renderContext.drawComplexTexture(
                GuiTextures.DELETE,
                0f,
                1f,
                TRASH_WIDTH.toFloat(),
                TRASH_HEIGHT.toFloat(),
            ) { draw -> draw.color(ColourUtil.packARGB(FULL_TINT, FULL_TINT, nonRedTints, nonRedTints)) }
            trashBounds = Rect.ofGuiImmediateContext(context)
        }

        override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
            if (
                mouseEvent !is MouseEvent.Click || !mouseEvent.mouseState || mouseEvent.mouseButton != 0 ||
                !context.isHovered || model.entries.isEmpty()
            ) {
                return false
            }
            openRemovalOverlay(
                model.entries,
                { StructuredText.of(it.text) },
                { entry ->
                    if (entry === volumeEntry) closeVolume()
                    model.removeEntry(model.entries.indexOf(entry))
                },
            )
            return true
        }
    }

    private inner class ListComponent : GuiComponent() {
        override fun getWidth(): Int = 0

        override fun getHeight(): Int = LIST_BORDER * 2 + max(MINIMUM_ROWS, model.entries.size) * ROW_HEIGHT

        override fun render(context: GuiImmediateContext) {
            listRenderX = context.renderOffsetX
            listRenderY = context.renderOffsetY
            volumeControlWidth = volumeAnimation.value
            if (!isVolumeExpanded && volumeControlWidth == 0) {
                volumeEntry = null
                volumeSlider = null
            }
            context.renderContext.drawColoredRect(
                0f,
                0f,
                context.width.toFloat(),
                context.height.toFloat(),
                LIST_BORDER_COLOUR,
            )
            context.renderContext.drawColoredRect(
                LIST_BORDER.toFloat(),
                LIST_BORDER.toFloat(),
                (context.width - LIST_BORDER).toFloat(),
                (context.height - LIST_BORDER).toFloat(),
                LIST_BACKGROUND_COLOUR,
            )
            model.entries.forEachIndexed { index, entry ->
                if (index != dragStartIndex) drawRow(context, entry, LIST_BORDER + index * ROW_HEIGHT)
            }
        }

        override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
            val sliderContext = volumeSliderContext(context)
            if (sliderContext != null && volumeSlider?.mouseEvent(mouseEvent, sliderContext) == true) return true
            if (mouseEvent !is MouseEvent.Click || !mouseEvent.mouseState) return false
            val index = if (context.mouseY >= LIST_BORDER) {
                (context.mouseY - LIST_BORDER) / ROW_HEIGHT
            } else {
                -1
            }
            val entry = model.entries.getOrNull(index) ?: return false
            return when (mouseEvent.mouseButton) {
                0 -> didHandleLeftClick(context, entry, index)
                1 -> didHandleRightClick(context, entry)
                else -> false
            }
        }

        private fun didHandleRightClick(context: GuiImmediateContext, entry: TextListEntry): Boolean {
            if (context.mouseX !in textX until textRight(context.width, entry)) return false
            closeVolume()
            addInputComponent.edit(entry)
            return true
        }

        private fun didHandleLeftClick(context: GuiImmediateContext, entry: TextListEntry, index: Int): Boolean {
            if (volumeEntry != null && !isInsideVolumeControl(context)) closeVolume()
            val rowTop = LIST_BORDER + index * ROW_HEIGHT
            val speakerX = speakerX(context.width).takeIf { shouldShowAudioControls(entry) }
            val controls = speakerX?.let {
                audioControlPositions(it, volumeControlWidth, entry === volumeEntry, CONTROL_GAP)
            }
            return when {
                speakerX?.let { context.mouseX in it until it + SPEAKER_WIDTH } == true -> {
                    openVolume(entry)
                    true
                }
                controls?.let { context.mouseX in it.playX until it.playX + AudioControlIcons.playWidth } == true -> {
                    previewSound(entry)
                    true
                }
                controls?.let { context.mouseX in it.gearX until it.gearX + AudioControlIcons.gearWidth } == true -> {
                    openSoundPicker(entry)
                    true
                }
                colourControlX?.let { context.mouseX in it until it + COLOUR_SIZE } == true -> {
                    openColourPicker(entry, context)
                    true
                }
                notificationControlX?.let { context.mouseX in it until it + BELL_WIDTH } == true -> {
                    model.toggleSound(entry)
                    if (!entry.isSoundEnabled && entry === volumeEntry) closeVolume()
                    val sound = if (entry.isSoundEnabled) enabledSoundId else disabledSoundId
                    if (sound != null) IMinecraft.INSTANCE.playSound(sound)
                    true
                }
                else -> {
                    startDragging(index, context, rowTop)
                    true
                }
            }
        }
    }

    private inner class DragComponent(
        private val entry: TextListEntry,
        private val componentWidth: Int,
        private val mouseOffsetX: Int,
        private val mouseOffsetY: Int,
    ) : GuiComponent() {
        override fun getWidth(): Int = componentWidth

        override fun getHeight(): Int = ROW_HEIGHT

        override fun render(context: GuiImmediateContext) {
            drawRow(context, entry, 0)
        }

        override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
            if (mouseEvent is MouseEvent.Click && !mouseEvent.mouseState) {
                if (trashBounds?.includesPoint(context.absoluteMouseX, context.absoluteMouseY) == true) {
                    model.removeEntry(dragStartIndex)
                }
                closeOverlay()
                dragStartIndex = -1
                return true
            }
            if (mouseEvent is MouseEvent.Move) {
                val left = context.absoluteMouseX - mouseOffsetX
                val top = context.absoluteMouseY - mouseOffsetY
                openOverlay(this, left, top)
                reorderDraggedEntry(context.absoluteMouseX, top, componentWidth)
            }
            return false
        }
    }

    private companion object {
        private const val INPUT_WIDTH = 100
        private const val ADD_BUTTON_WIDTH = 36
        private const val ADD_INPUT_ANIMATION_SPEED = 1
        private const val CONTROL_HEIGHT = 16
        private const val CONTROL_GAP = 4
        private const val TRASH_WIDTH = 11
        private const val TRASH_HEIGHT = 14
        private const val LIST_BORDER = 1
        private const val ROW_HEIGHT = 16
        private const val ROW_TEXT_Y = 4
        private const val HANDLE_X = 5
        private const val FIRST_CONTROL_X = 16
        private const val ROW_CONTROL_GAP = 6
        private const val TEXT_CONTROL_GAP = 8
        private const val COLOUR_Y = 2
        private const val COLOUR_SIZE = 12
        private const val BELL_Y = 3
        private const val BELL_WIDTH = 9
        private const val BELL_HEIGHT = 10
        private const val TEXT_RIGHT_PADDING = 5
        private const val SPEAKER_WIDTH = 12
        private const val SPEAKER_HEIGHT = 10
        private const val SPEAKER_HITBOX_Y = 3
        private const val SPEAKER_RIGHT_PADDING = 4
        private const val SPEAKER_BOX_TOP = 4
        private const val SPEAKER_BOX_RIGHT = 3
        private const val SPEAKER_BOX_BOTTOM = 6
        private const val SPEAKER_CONE_MIDDLE_LEFT = 3
        private const val SPEAKER_CONE_MIDDLE_TOP = 3
        private const val SPEAKER_CONE_MIDDLE_RIGHT = 4
        private const val SPEAKER_CONE_MIDDLE_BOTTOM = 7
        private const val SPEAKER_CONE_OUTER_LEFT = 4
        private const val SPEAKER_CONE_OUTER_TOP = 2
        private const val SPEAKER_CONE_OUTER_RIGHT = 5
        private const val SPEAKER_CONE_OUTER_BOTTOM = 8
        private const val SPEAKER_WAVE_ONE_X = 6
        private const val SPEAKER_WAVE_ONE_TOP = 4
        private const val SPEAKER_WAVE_ONE_BOTTOM = 6
        private const val SPEAKER_WAVE_TWO_X = 8
        private const val SPEAKER_WAVE_TWO_TOP = 3
        private const val SPEAKER_WAVE_TWO_BOTTOM = 6
        private const val SPEAKER_WAVE_THREE_X = 10
        private const val SPEAKER_WAVE_THREE_TOP = 2
        private const val SPEAKER_WAVE_THREE_BOTTOM = 7
        private const val SPEAKER_WAVE_WIDTH = 1
        private const val SPEAKER_WAVE_SEGMENT_HEIGHT = 1
        private const val SPEAKER_ONE_WAVE = 1
        private const val SPEAKER_TWO_WAVES = 2
        private const val SPEAKER_THREE_WAVES = 3
        private const val SPEAKER_ONE_WAVE_MAX_VOLUME = 50f
        private const val SPEAKER_TWO_WAVE_MAX_VOLUME = 100f
        private const val VOLUME_SLIDER_WIDTH = 50
        private const val MIN_VOLUME_SLIDER_RENDER_WIDTH = 8
        private const val VOLUME_STEP = 5f
        private const val VOLUME_ANIMATION_SPEED = 1
        private const val VOLUME_ANIMATION_SCALE = 2
        private const val MINIMUM_ROWS = 1
        private const val FULL_TINT = 255
        private const val BELL_CENTRE_LEFT = 3
        private const val BELL_CENTRE_RIGHT = 6
        private const val BELL_CROWN_BOTTOM = 1
        private const val BELL_DOME_LEFT = 2
        private const val BELL_DOME_RIGHT = 7
        private const val BELL_DOME_BOTTOM = 3
        private const val BELL_BODY_LEFT = 1
        private const val BELL_BODY_RIGHT = 8
        private const val BELL_BODY_BOTTOM = 7
        private const val BELL_LIP_RIGHT = 9
        private const val BELL_LIP_BOTTOM = 9
        private const val HANDLE_COLOUR = -0x1
        private const val DEFAULT_TEXT_COLOUR = -0x1
        private val BELL_ENABLED_HOVER_COLOUR = 0xFF6FD484.toInt()
        private val BELL_ENABLED_COLOUR = 0xFF55B86A.toInt()
        private val BELL_DISABLED_HOVER_COLOUR = 0xFFD66A6A.toInt()
        private val BELL_DISABLED_COLOUR = 0xFFB85454.toInt()
        private val SPEAKER_HOVER_COLOUR = 0xFFFFFFFF.toInt()
        private val SPEAKER_COLOUR = 0xFFB8B8B8.toInt()
        private val SPEAKER_MUTED_HOVER_COLOUR = 0xFFD66A6A.toInt()
        private val SPEAKER_MUTED_COLOUR = 0xFFB85454.toInt()
        private val SOUND_SELECTED_COLOUR = 0xFFFFFFFF.toInt()
        private val SOUND_DEFAULT_COLOUR = 0xFFA0A0A0.toInt()
        private const val LIST_BORDER_COLOUR = -0x222223
        private const val LIST_BACKGROUND_COLOUR = -0x1000000
    }
}

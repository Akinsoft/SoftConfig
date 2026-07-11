package io.github.notenoughupdates.moulconfig.gui.component

import io.github.notenoughupdates.moulconfig.GuiTextures
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import kotlin.math.max
import kotlin.math.min

private const val SLIDER_HEIGHT = 16
private const val CAP_WIDTH = 4
private const val CAP_THRESHOLD = 5
private const val NOTCH_COUNT = 4
private const val NOTCH_OFFSET = 1
private const val NOTCH_WIDTH = 2
private const val NOTCH_HEIGHT = 4
private const val BUTTON_OFFSET = 4
private const val BUTTON_WIDTH = 8

open class SliderComponent(
    val value: GetSetter<Float>,
    val minValue: Float,
    val maxValue: Float,
    val minStep: Float,
    private val width: Int,
) : GuiComponent() {
    var clicked: Boolean = false
    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return SLIDER_HEIGHT
    }

    override fun render(context: GuiImmediateContext) {
        if (clicked) {
            setValueFromContext(context)
        }
        val value: Float = value.get()
        context.renderContext.drawTexturedRect(
            GuiTextures.SLIDER_ON_CAP, 0F, 0F, CAP_WIDTH.toFloat(), context.height.toFloat()
        )
        context.renderContext.drawTexturedRect(
            GuiTextures.SLIDER_OFF_CAP,
            (context.width - CAP_WIDTH).toFloat(),
            0F,
            CAP_WIDTH.toFloat(),
            context.height.toFloat()
        )
        val sliderPosition = ((value.coerceIn(minValue..maxValue) - minValue) / (maxValue - minValue) * context.width).toInt()
        if (sliderPosition > CAP_THRESHOLD) {
            context.renderContext.drawTexturedRect(
                GuiTextures.SLIDER_ON_SEGMENT,
                CAP_WIDTH.toFloat(),
                0F,
                (sliderPosition - CAP_WIDTH).toFloat(),
                context.height.toFloat()
            )
        }
        if (sliderPosition < context.width - CAP_THRESHOLD) {
            context.renderContext.drawTexturedRect(
                GuiTextures.SLIDER_OFF_SEGMENT,
                sliderPosition.toFloat(),
                0F,
                (context.width - CAP_WIDTH - sliderPosition).toFloat(),
                context.height.toFloat()
            )
        }
        for (i in 0 until NOTCH_COUNT) {
            val notchX = context.width * i / NOTCH_COUNT - NOTCH_OFFSET
            context.renderContext.drawTexturedRect(
                if (notchX > sliderPosition) GuiTextures.SLIDER_OFF_NOTCH else GuiTextures.SLIDER_ON_NOTCH,
                notchX.toFloat(),
                (context.height - NOTCH_HEIGHT) / 2F,
                NOTCH_WIDTH.toFloat(),
                NOTCH_HEIGHT.toFloat()
            )
        }
        context.renderContext.drawTexturedRect(
            GuiTextures.SLIDER_BUTTON,
            (sliderPosition - BUTTON_OFFSET).toFloat(),
            0F,
            BUTTON_WIDTH.toFloat(),
            context.height.toFloat()
        )
    }

    open fun setValueFromContext(context: GuiImmediateContext) {
        var v: Float = context.mouseX * (maxValue - minValue) / context.width + minValue
        v = min(v.toDouble(), maxValue.toDouble()).toFloat()
        v = max(v.toDouble(), minValue.toDouble()).toFloat()
        v = Math.round(v / minStep) * minStep
        value.set(v)
    }

    override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
        if (!context.renderContext.isMouseButtonDown(0)) clicked = false
        if (context.isHovered && mouseEvent is MouseEvent.Click && mouseEvent.mouseState && mouseEvent.mouseButton == 0) {
            clicked = true
        }
        if (clicked) {
            setValueFromContext(context)
            return true
        }
        return false
    }

}

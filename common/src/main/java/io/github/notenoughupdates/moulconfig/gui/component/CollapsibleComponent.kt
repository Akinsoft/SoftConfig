package io.github.notenoughupdates.moulconfig.gui.component

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import java.util.function.Supplier

private const val PADDING = 2
private const val TRIM = 3
private const val ICON_WIDTH = 9

class CollapsibleComponent(
    val title: Supplier<GuiComponent>,
    val body: Supplier<GuiComponent>,
    val collapsedState: GetSetter<Boolean> = GetSetter.floating(
        true
    ),
) : GuiComponent() {

    companion object {
        val fr = IMinecraft.INSTANCE.defaultFontRenderer
        val padding: Int get() = PADDING
        val trim: Int get() = TRIM
        val iconWidth: Int get() = ICON_WIDTH
    }

    override fun getWidth(): Int {
        return maxOf(title.get().width + PADDING + ICON_WIDTH, body.get().width)
    }

    override fun getHeight(): Int {
        return if (collapsedState.get()) {
            maxOf(title.get().height, fr.height)
        } else {
            maxOf(title.get().height, fr.height) + TRIM + body.get().height
        }
    }

    override fun render(context: GuiImmediateContext) {
        val collapsed = collapsedState.get()
        context.renderContext.drawOpenCloseTriangle(!collapsed, 0F, 0F, ICON_WIDTH.toFloat(), ICON_WIDTH.toFloat(), -1)
        val barHeight = maxOf(title.get().height, fr.height)
        context.renderContext.pushMatrix()
        context.renderContext.translate(ICON_WIDTH.toFloat(), 0F)
        title.get().render(context.translated(ICON_WIDTH, 0, context.width - ICON_WIDTH, barHeight))
        context.renderContext.popMatrix()

        if (!collapsed) {
            context.renderContext.drawColoredRect(
                0F,
                barHeight + 1F,
                context.width.toFloat(),
                barHeight + 2F,
                0xFF000000.toInt()
            )

            context.renderContext.pushMatrix()
            context.renderContext.translate(0F, barHeight.toFloat())
            body.get().render(
                context.translated(0, barHeight, context.width, context.height - barHeight)
            )
            context.renderContext.popMatrix()
        }
    }

    override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
        val barHeight = maxOf(title.get().height, fr.height)

        if (mouseEvent is MouseEvent.Click && context.translated(
                0,
                0,
                context.width,
                barHeight
            ).isHovered
        ) {
            if (mouseEvent.mouseState)
                collapsedState.set(!collapsedState.get())
            return true
        }

        return title.get().mouseEvent(
            mouseEvent,
            context.translated(
                ICON_WIDTH,
                0,
                context.width - ICON_WIDTH,
                barHeight
            )
        ) || body.get().mouseEvent(
            mouseEvent, context.translated(0, barHeight, context.width, context.height - barHeight)
        )
    }

    override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
        val barHeight = maxOf(title.get().height, fr.height)
        return title.get().keyboardEvent(
            event,
            context.translated(
                ICON_WIDTH,
                0,
                context.width - ICON_WIDTH,
                barHeight
            )
        ) || body.get().keyboardEvent(
            event, context.translated(0, barHeight, context.width, context.height - barHeight)
        )
    }
}

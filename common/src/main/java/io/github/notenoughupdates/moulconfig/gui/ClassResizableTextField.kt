package io.github.notenoughupdates.moulconfig.gui

import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter

private const val DEFAULT_WIDTH = 20
private const val FIELD_HEIGHT = 18

class ClassResizableTextField(text: GetSetter<String>) : TextFieldComponent(
    text,
    DEFAULT_WIDTH,
) {
    private var width = DEFAULT_WIDTH

    fun setWidth(width: Int) {
        this.width = width
    }

    override fun getWidth(): Int {
        return width
    }

    override fun render(context: GuiImmediateContext) {
        super.render(context.translated(0, 0, width, FIELD_HEIGHT))
    }

    override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
        return super.mouseEvent(mouseEvent, context.translated(0, 0, width, FIELD_HEIGHT))
    }

    override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
        return super.keyboardEvent(event, context.translated(0, 0, width, FIELD_HEIGHT))
    }

}

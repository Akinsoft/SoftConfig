package io.github.notenoughupdates.moulconfig.common

import io.github.notenoughupdates.moulconfig.common.text.StructuredStyle
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.internal.MCLogger
import java.util.stream.Stream

/**
 * Minimal [IMinecraft] implementation for use in unit tests.
 * Only implements what is required for [io.github.notenoughupdates.moulconfig.internal.Warnings] to initialise.
 * All other methods throw [UnsupportedOperationException].
 */
class MockIMinecraft : IMinecraft {

    override fun isDevelopmentEnvironment() = false

    override fun getLogger(label: String) = object : MCLogger {
        override fun warn(text: String) = Unit
        override fun info(text: String) = Unit
        override fun error(text: String, throwable: Throwable) = Unit
    }

    override fun loadResourceLocation(resourceLocation: MyResourceLocation) = TODO()
    override fun isGeneratedSentinel(resourceLocation: MyResourceLocation) = TODO()
    override fun generateDynamicTexture(image: java.awt.image.BufferedImage) = TODO()
    override fun getMousePositionHF() = TODO()
    override fun getDefaultFontRenderer() = TODO()
    override fun getKeyboardConstants() = TODO()
    override fun getScaledWidth() = TODO()
    override fun getScaledHeight() = TODO()
    override fun getScaleFactor() = TODO()
    override fun isOnMacOs() = TODO()
    override fun isMouseButtonDown(mouseButton: Int) = TODO()
    override fun isKeyboardKeyDown(keyCode: Int) = TODO()
    override fun addExtraBuiltinConfigProcessors(processor: io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor<*>) = TODO()
    override fun sendClickableChatMessage(message: io.github.notenoughupdates.moulconfig.common.text.StructuredText, action: String, clickType: ClickType?) = TODO()
    override fun getKeyName(keyCode: Int) = TODO()
    override fun createLiteral(text: String): StructuredText.Mutable = SimpleStructuredText(text)
    override fun createTranslatable(key: String, vararg args: StructuredText): StructuredText.Mutable = SimpleStructuredText(key)
    override fun createStructuredTextInternal(`object`: Any) = TODO()
    override fun registerPlatformTypeMorphisms(universe: io.github.notenoughupdates.moulconfig.xml.XMLUniverse) = TODO()
    @Deprecated("See parent deprecation")
    override fun provideTopLevelRenderContext() = TODO()
    override fun openWrappedScreen(guiContext: io.github.notenoughupdates.moulconfig.gui.GuiContext) = TODO()
    override fun copyToClipboard(string: String) = TODO()
    override fun copyFromClipboard() = TODO()
}

private class SimpleStructuredText(
    private var text: String,
    private var style: StructuredStyle = SimpleStructuredStyle(),
) : StructuredText.Mutable {
    override fun append(text: StructuredText): StructuredText.Mutable {
        this.text += text.text
        return this
    }

    override fun copyShallow(): StructuredText.Mutable = SimpleStructuredText(text, style)
    override fun getText() = text
    override fun getChildren(): Stream<StructuredText> = Stream.empty()
    override fun getStyle() = style
    override fun setStyle(style: StructuredStyle) {
        this.style = style
    }
}

private class SimpleStructuredStyle : StructuredStyle {
    override fun withColour(rgb: Int) = this
    override fun withBold(bold: Boolean) = this
    override fun withItalic(italic: Boolean) = this
    override fun withUnderline(underline: Boolean) = this
    override fun withStrikethrough(strikethrough: Boolean) = this
    override fun withObfuscated(obfuscated: Boolean) = this
}

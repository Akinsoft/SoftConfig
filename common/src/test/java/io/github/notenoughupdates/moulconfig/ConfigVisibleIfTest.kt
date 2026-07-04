package io.github.notenoughupdates.moulconfig

import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.annotations.ConfigVisibleIf
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigVisibleIfTest {
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    private annotation class TestEditor

    private class TestConfig : Config() {
        @field:Category(name = "Events", desc = "Event settings")
        val events = EventSettings()
    }

    private class EventSettings {
        @field:ConfigOption(name = "Show Progress", desc = "Shows progress")
        @field:TestEditor
        var showProgress = false

        @field:ConfigOption(name = "Progress Position", desc = "Controls progress position")
        @field:ConfigVisibleIf("showProgress")
        @field:TestEditor
        var progressPosition = "CENTER"

        @field:ConfigOption(name = "Hidden When Progress Shows", desc = "Checks inverse visibility")
        @field:ConfigVisibleIf(value = "showProgress", expected = false)
        @field:TestEditor
        var hiddenWhenProgressShows = true
    }

    private class TestGuiOptionEditor(option: ProcessedOption) : GuiOptionEditor(option) {
        override fun getHeight() = 45
    }

    @Test
    fun `dependent options follow boolean visibility condition`() {
        val config = TestConfig()
        val options = process(config)

        assertTrue(options.getValue("showProgress").isVisible)
        assertFalse(options.getValue("progressPosition").isVisible)
        assertTrue(options.getValue("hiddenWhenProgressShows").isVisible)

        config.events.showProgress = true

        assertTrue(options.getValue("progressPosition").isVisible)
        assertFalse(options.getValue("hiddenWhenProgressShows").isVisible)
    }

    private fun process(config: TestConfig): Map<String, ProcessedOption> {
        val processor = MoulConfigProcessor(config)
        processor.registerConfigEditor(TestEditor::class.java) { option, _ -> TestGuiOptionEditor(option) }
        val driver = ConfigProcessorDriver(processor)
        driver.checkExpose = false
        driver.processConfig(config)

        return processor.allCategories.values
            .flatMap { it.options }
            .associateBy { (it as ProcessedOption.HasField).field.name }
    }
}

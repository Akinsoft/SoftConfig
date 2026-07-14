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

import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorCombinations
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.annotations.SearchTag
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Type

class ConfigEditorCombinationsTest {
    @Test
    fun `model edits consumer objects and applies provider constraints`() {
        val combinations = mutableListOf<TestCombination>()
        val provider = TestProvider()
        val option = TestOption(combinations)
        val model = CombinationEditorModel(option, provider)

        assertFalse(model.addChoice(0, provider.hub))
        assertTrue(model.addChoice(null, provider.hub))
        assertEquals(1, combinations.size)
        assertNotSame(provider.hub, combinations.single().entries.single())
        val availableIds = model.getAvailableChoices(0).map(provider::getChoiceIdInternal)
        assertFalse(availableIds.contains(provider.garden.id))
        assertTrue(availableIds.contains(provider.spade.id))

        assertTrue(model.addChoice(0, provider.spade))
        assertEquals(listOf("hub", "spade"), combinations.single().entries.map { it.id })
        assertTrue(model.removeChoice(0, 1))
        assertTrue(model.removeChoice(0, 0))
        assertTrue(combinations.isEmpty())
        assertEquals(4, provider.changeCount)
        assertEquals(4, option.notificationCount)
    }

    @Test
    fun `choices may be reused by separate combinations`() {
        val combinations = mutableListOf<TestCombination>()
        val provider = TestProvider()
        val model = CombinationEditorModel(TestOption(combinations), provider)

        assertTrue(model.addChoice(null, provider.hub))
        assertTrue(model.addChoice(null, provider.hub))
        assertEquals(2, combinations.size)
    }

    @Test
    fun `resolver supports classes and Kotlin objects`() {
        assertInstanceOf(
            TestProvider::class.java,
            ConfigEditorCombinationsProviderResolver.resolve(TestProvider::class.java),
        )
        assertSame(
            ObjectProvider,
            ConfigEditorCombinationsProviderResolver.resolve(ObjectProvider::class.java),
        )
    }

    @Test
    fun `processor creates combinations editor from annotation`() {
        val config = TestConfig()
        val processor = MoulConfigProcessor(config)
        processor.registerConfigEditor(ConfigEditorCombinations::class.java) { option, annotation ->
            GuiOptionEditorCombinations(option, annotation.provider.java)
        }
        ConfigProcessorDriver(processor).apply {
            checkExpose = false
            processConfig(config)
        }

        val option = processor.allCategories.values
            .flatMap { it.options }
            .single()
        assertInstanceOf(GuiOptionEditorCombinations::class.java, option.editor)
    }

    private class TestConfig : Config() {
        @field:Category(name = "Rules", desc = "Rule settings")
        val rules = TestSettings()
    }

    private class TestSettings {
        @field:ConfigOption(name = "Combinations", desc = "Combination settings")
        @field:ConfigEditorCombinations(provider = ObjectProvider::class)
        val combinations = mutableListOf<TestCombination>()
    }

    data class TestCombination(val entries: MutableList<TestChoice> = mutableListOf())

    class TestChoice(val id: String, val group: String, val label: String)

    class TestProvider : ConfigEditorCombinationsProvider<TestCombination, TestChoice>(
        TestCombination::class.java,
        TestChoice::class.java,
    ) {
        val hub = TestChoice("hub", "island", "On Island: Hub")
        val garden = TestChoice("garden", "island", "On Island: Garden")
        val spade = TestChoice("spade", "item", "Holding Item: Ancestral Spade")
        var changeCount = 0

        override fun getChoices(): List<TestChoice> = listOf(hub, garden, spade).map(::copy)

        override fun getEntries(combination: TestCombination): MutableList<TestChoice> = combination.entries

        override fun createCombination(firstChoice: TestChoice) = TestCombination(mutableListOf(firstChoice))

        override fun getChoiceLabel(choice: TestChoice): StructuredText = StructuredText.of(choice.label)

        override fun getChoiceId(choice: TestChoice): Any = choice.id

        override fun getChoiceGroup(choice: TestChoice): Any = choice.group

        override fun copyChoice(choice: TestChoice): TestChoice = copy(choice)

        override fun onChanged() {
            changeCount++
        }

        private fun copy(choice: TestChoice): TestChoice = TestChoice(choice.id, choice.group, choice.label)
    }

    object ObjectProvider : ConfigEditorCombinationsProvider<TestCombination, TestChoice>(
        TestCombination::class.java,
        TestChoice::class.java,
    ) {
        override fun getChoices(): List<TestChoice> = emptyList()

        override fun getEntries(combination: TestCombination): MutableList<TestChoice> = combination.entries

        override fun createCombination(firstChoice: TestChoice) = TestCombination(mutableListOf(firstChoice))

        override fun getChoiceLabel(choice: TestChoice): StructuredText = StructuredText.of(choice.label)

        override fun getChoiceId(choice: TestChoice): Any = choice.id
    }

    private class TestOption(private val value: Any) : ProcessedOption {
        private val config = object : Config() {}
        var notificationCount = 0

        override fun getSearchTags(): Array<SearchTag> = emptyArray()
        override fun getAccordionId(): Int = -1
        override fun getEditor(): GuiOptionEditor = error("Not used")
        override fun getCategory(): ProcessedCategory = error("Not used")
        override fun getName(): StructuredText = StructuredText.of("Combinations")
        override fun getDescription(): StructuredText = StructuredText.empty()
        override fun getPath(): String = "rules.combinations"
        override fun getConfig(): Config = config
        override fun get(): Any = value
        override fun getType(): Type = List::class.java
        override fun set(value: Any): Boolean = false
        override fun explicitNotifyChange() {
            notificationCount++
        }
        override fun getDebugDeclarationLocation(): String = "TestOption"
    }
}

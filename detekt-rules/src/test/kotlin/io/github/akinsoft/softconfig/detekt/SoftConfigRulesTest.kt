package io.github.akinsoft.softconfig.detekt

import dev.detekt.test.TestConfig
import dev.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class SoftConfigRulesTest {
    @Test
    fun `ambiguous boolean return reports only unclear names`() {
        val findings = AmbiguousBooleanReturn(TestConfig()).lint(
            """
            fun process(): Boolean = true
            fun isReady(): Boolean = true
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `large loose constant set reports at configured threshold`() {
        val findings = LargeUngroupedConstantSet(TestConfig("threshold" to 3)).lint(
            """
            const val FIRST = 1
            const val SECOND = 2
            const val THIRD = 3
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `named constant object is accepted`() {
        val findings = LargeUngroupedConstantSet(TestConfig("threshold" to 3)).lint(
            """
            object LayoutDimensions {
                const val FIRST = 1
                const val SECOND = 2
                const val THIRD = 3
            }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `new vague utils name reports while compatibility name remains allowed`() {
        val config = TestConfig("allowedNames" to listOf("RenderUtils"))

        assertEquals(1, VagueUtilsName(config).lint("object NetworkUtils").size)
        assertEquals(0, VagueUtilsName(config).lint("object RenderUtils").size)
    }
}

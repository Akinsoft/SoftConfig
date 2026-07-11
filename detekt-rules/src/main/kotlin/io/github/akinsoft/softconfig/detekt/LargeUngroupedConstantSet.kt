package io.github.akinsoft.softconfig.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty

class LargeUngroupedConstantSet(config: Config) : Rule(
    config,
    "Large groups of loose constants should be grouped by concept.",
) {
    private val threshold: Int = config.valueOrDefault("threshold", 15)

    override fun visitKtFile(file: KtFile) {
        inspectScope(file.name, Entity.atPackageOrFirstDecl(file), file.declarations)
        super.visitKtFile(file)
    }

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val scopeName = classOrObject.name ?: return super.visitClassOrObject(classOrObject)
        if (!classOrObject.isNamedConstantGroup()) {
            inspectScope(scopeName, Entity.atName(classOrObject), classOrObject.declarations)
        }
        super.visitClassOrObject(classOrObject)
    }

    private fun inspectScope(scopeName: String, entity: Entity, declarations: List<KtDeclaration>) {
        val constants = declarations.filterIsInstance<KtProperty>().filter { it.isConstantLike() }
        if (constants.size < threshold) return
        report(Finding(entity, "Scope '$scopeName' has ${constants.size} loose constants. Group related values into named concept objects."))
    }

    private fun KtClassOrObject.isNamedConstantGroup(): Boolean =
        this is KtObjectDeclaration &&
            name !in GENERIC_CONSTANT_GROUP_NAMES &&
            declarations.isNotEmpty() &&
            declarations.all { it is KtProperty && it.isConstantLike() }

    private fun KtProperty.isConstantLike(): Boolean {
        if (isVar) return false
        val propertyName = name ?: return false
        if (!hasModifier(KtTokens.CONST_KEYWORD) && !CONSTANT_NAME_PATTERN.matches(propertyName)) return false
        return initializer?.isDataTableOrParserInitializer() != true
    }

    private fun KtExpression.isDataTableOrParserInitializer(): Boolean {
        val expression = text.trimStart()
        return DATA_OR_PARSER_FACTORIES.any(expression::startsWith) || DATA_OR_PARSER_DERIVED_PATTERNS.any(expression::contains)
    }

    private companion object {
        val CONSTANT_NAME_PATTERN = Regex("""[A-Z][A-Z0-9_]*""")
        val GENERIC_CONSTANT_GROUP_NAMES = setOf("CommonConstants", "Constants", "GeneralConstants", "MiscConstants")
        val DATA_OR_PARSER_FACTORIES = listOf(
            "arrayOf(", "buildList(", "buildMap(", "buildSet(", "listOf(", "mapOf(", "mutableListOf(", "mutableMapOf(",
            "mutableSetOf(", "Regex(", "setOf(",
        )
        val DATA_OR_PARSER_DERIVED_PATTERNS = listOf(".associate", ".entries", ".flatMap", ".map")
    }
}

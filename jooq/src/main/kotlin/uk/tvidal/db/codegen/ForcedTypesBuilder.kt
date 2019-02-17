package uk.tvidal.db.codegen

import org.jooq.meta.jaxb.ForcedType
import uk.tvidal.db.codegen.JooqCodegen.Companion.REGEX_ALL
import uk.tvidal.db.codegen.JooqCodegen.Companion.REGEX_TEXT_TYPES

class ForcedTypesBuilder {
    val forcedTypes = mutableListOf<ForcedType>()

    inline fun <reified T, reified B> binding(types: String, expression: String = REGEX_ALL) {
        forcedTypes += ForcedType()
            .withUserType(T::class.qualifiedName)
            .withBinding(B::class.qualifiedName)
            .withTypes(types)
            .withExpression(expression)
    }

    inline fun <reified T, reified C> converter(types: String, expression: String = REGEX_ALL) {
        forcedTypes += ForcedType()
            .withUserType(T::class.qualifiedName)
            .withConverter(C::class.qualifiedName)
            .withExpression(expression)
            .withTypes(types)
    }

    inline fun <reified E : Enum<E>> enumConverter(expression: String, types: String = REGEX_TEXT_TYPES) {
        forcedTypes += ForcedType()
            .withEnumConverter(true)
            .withUserType(E::class.qualifiedName)
            .withExpression(expression)
            .withTypes(types)
    }
}
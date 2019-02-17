package uk.tvidal.db.codegen

import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging.INFO
import org.jooq.meta.jaxb.Schema
import org.jooq.meta.jaxb.Target
import uk.tvidal.db.converter.InstantConverter
import uk.tvidal.db.converter.LocalDateConverter
import java.sql.Driver
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass

abstract class JooqCodegen(
    protected val directory: String = "src/main/java"
) {
    protected abstract val driver: KClass<out Driver>
    protected abstract val schemata: Schema

    protected open fun target(): Target = Target()
        .withDirectory(directory)
        .withPackageName(javaClass.`package`.name)
        .withClean(true)

    protected open fun generate(): Generate = Generate()
        .withJavaTimeTypes(false)
        .withComments(false)
        .withJavadoc(false)
        .withDeprecated(false)

    protected open fun database(): Database = Database()
        .withExcludes("flyway_schema_history")
        .withIncludes(REGEX_ALL)
        .withSchemata(schemata)
        .withForcedTypes {
            converter<Instant, InstantConverter>("timestamp")
            converter<LocalDate, LocalDateConverter>("date")
        }

    operator fun invoke(url: String, username: String, password: String) {

        val jdbc = Jdbc()
            .withDriver(driver.qualifiedName)
            .withUrl(url)
            .withUser(username)
            .withPassword(password)

        val generator = Generator()
            .withDatabase(database())
            .withGenerate(generate())
            .withTarget(target())

        GenerationTool.generate(
            Configuration()
                .withJdbc(jdbc)
                .withGenerator(generator)
                .withLogging(INFO)
        )
    }

    protected fun Database.withForcedTypes(block: ForcedTypesBuilder.() -> Unit): Database = withForcedTypes(
        ForcedTypesBuilder()
            .apply(block)
            .forcedTypes
    )

    companion object {
        const val REGEX_ALL = ".*"
        const val REGEX_TEXT_TYPES = "(var)?char.*|text"
    }
}
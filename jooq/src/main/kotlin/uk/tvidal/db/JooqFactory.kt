package uk.tvidal.db

import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.conf.RenderKeywordCase.UPPER
import org.jooq.conf.RenderQuotedNames.ALWAYS
import org.jooq.conf.Settings
import org.jooq.impl.DSL.using
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource

fun jooqSettings(): Settings = Settings()
    .withRenderFormatted(true)
    .withRenderKeywordCase(UPPER)
    .withRenderQuotedNames(ALWAYS)

fun jooqConfiguration(
    dataSource: DataSource,
    settings: Settings = jooqSettings()
): Configuration = DefaultConfiguration()
    .set(dataSource)
    .set(settings)

fun jooqContext(
    dataSource: DataSource,
    config: Configuration = jooqConfiguration(dataSource)
): DSLContext = using(config)

package uk.tvidal.db.test

import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.conf.RenderNameStyle.UPPER
import org.jooq.conf.Settings
import org.jooq.impl.DSL.using
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultConnectionProvider
import uk.tvidal.db.EntityRepository
import uk.tvidal.db.JooqExecute
import java.sql.DriverManager

class TestRepository : EntityRepository<TestEntity>(TestEntity::class) {

    override val context: DSLContext = using(config)

    override val table = TestTable
    override val sequence = TestTable.TEST_SEQUENCE

    fun list() = query(null).all()

    internal companion object {

        const val testDatabaseUrl = "jdbc:h2:mem:test;INIT=RUNSCRIPT from 'classpath:/test-schema.sql'"

        val config: Configuration

        init {
            val connection = DriverManager
                .getConnection(testDatabaseUrl)

            val connectionProvider = DefaultConnectionProvider(connection)

            val settings = Settings()
                .withExecuteLogging(false)
                .withRenderNameStyle(UPPER)

            config = DefaultConfiguration()
                .set(settings)
                .set(connectionProvider)
                .set(JooqExecute())
        }
    }
}
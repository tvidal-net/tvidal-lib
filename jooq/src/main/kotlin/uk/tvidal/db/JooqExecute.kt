package uk.tvidal.db

import org.jooq.ExecuteContext
import org.jooq.impl.DefaultExecuteListener
import uk.tvidal.logging.KLogging
import java.lang.System.nanoTime

class JooqExecute : DefaultExecuteListener() {

    private var start = ThreadLocal<Long>()

    override fun executeStart(ctx: ExecuteContext) {
        start.set(nanoTime())
        log.debug { ctx.sql() }
    }

    override fun executeEnd(ctx: ExecuteContext) {
        val duration = (nanoTime() - start.get()) / 1000
        log.debug { "query execution time " + String.format("%,.2fms", duration / 1000.0) }
    }

    private companion object : KLogging()
}
package uk.tvidal.db

import org.jooq.DSLContext

class JooqTransaction(val context: DSLContext) : DatabaseTransaction {

    override val inTransaction: Boolean
        get() = hasActiveTransaction

    override fun <T> invoke(block: () -> T): T =
        context.requireTransaction { block() }
}
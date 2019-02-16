package uk.tvidal.db

import org.jooq.DSLContext
import org.jooq.impl.DSL.using

internal val transactionContext = ThreadLocal<DSLContext>()

val hasActiveTransaction: Boolean
    get() = transactionContext.get() != null

fun <T> DSLContext.requireTransaction(block: DSLContext.() -> T): T {
    val current = transactionContext.get()
    return if (current == null) transactionResult { transactionConfiguration ->
        val newTransaction = using(transactionConfiguration)
        transactionContext.set(newTransaction)
        try {
            block(newTransaction)
        } finally {
            transactionContext.remove()
        }
    } else block(current)
}
package uk.tvidal.db

import org.jooq.DSLContext
import org.jooq.impl.DSL.using

internal val transactionContext = ThreadLocal<DSLContext>()

val hasActiveTransaction: Boolean
    get() = transactionContext.get() != null

fun <T> DSLContext.requireTransaction(block: DSLContext.() -> T): T {
    val currentTransaction = transactionContext.get()
    return if (currentTransaction == null) transactionResult { transactionConfiguration ->
        val newTransaction = using(transactionConfiguration)
        transactionContext.set(newTransaction)
        try {
            block(newTransaction)
        } finally {
            transactionContext.remove()
        }
    } else block(currentTransaction)
}
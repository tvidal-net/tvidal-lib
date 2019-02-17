package uk.tvidal.db

import org.jooq.Batch
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Query
import org.jooq.Record
import org.jooq.Table
import org.jooq.exception.NoDataFoundException
import org.jooq.exception.TooManyRowsException
import org.jooq.Sequence as JooqSequence

@Suppress("UNCHECKED_CAST")
abstract class JooqRepository {

    protected abstract val context: DSLContext
    protected abstract val table: Table<*>

    protected val db: DSLContext
        get() = transactionContext.get() ?: context

    protected val primaryKeyFields: List<Field<Any>> by lazy {
        table.primaryKey.fields
            .filterIsInstance<Field<Any>>()
    }

    protected fun wherePrimaryKey(record: Record): Condition {
        var condition: Condition? = null
        for (field in primaryKeyFields) {
            val value = record[field]
            condition = if (condition == null) field.eq(value)
            else condition.and(field.eq(value))
        }
        return condition!!
    }

    protected open fun query(where: Condition) = db
        .select()
        .from(table)
        .where(where)!!

    protected fun <T> transaction(block: DSLContext.() -> T): T = context.requireTransaction(block)

    protected fun ensureAffected(expected: Int, actual: Int) = when (actual) {
        0 -> throw NoDataFoundException("No records affected (expected: $expected)")
        expected -> Unit
        else -> throw TooManyRowsException("$actual records affected (expected: $expected)")
    }

    protected fun Query.executeSingle() = ensureAffected(1, execute())
    protected fun Batch.executeBatch() = ensureAffected(size(), execute().sum())
}
package uk.tvidal.db

import org.jooq.Batch
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Query
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.ResultQuery
import org.jooq.Table
import org.jooq.TableRecord
import org.jooq.exception.NoDataFoundException
import org.jooq.exception.TooManyRowsException
import uk.tvidal.model.Entity
import uk.tvidal.model.MutableEntity
import java.time.Instant
import org.jooq.Sequence as JooqSequence

@Suppress("UNCHECKED_CAST")
abstract class JooqRepository<E : Entity<in Any>, R : TableRecord<R>>(
    private val defaultContext: DSLContext
) {
    protected abstract val table: Table<R>

    protected open val sequence: JooqSequence<Number>?
        get() = null

    protected val idField: Field<Any>
        get() = table.primaryKey.fields[0] as Field<Any>

    protected val context: DSLContext
        get() = transactionContext.get() ?: defaultContext

    protected val recordMapper: RecordMapper<Record, E> = RecordMapper {
        (it as R).toEntity()
    }

    protected abstract fun R.toEntity(): E
    protected abstract fun E.toRecord(): R

    protected open fun query(where: Condition) = context
        .select()
        .from(table)
        .where(where)!!

    operator fun <T> get(id: T): E? = query(idField.eq(id)).one()

    protected fun <T : Record> ResultQuery<T>.one(): E? = fetchOne(recordMapper)
    protected fun <T : Record> ResultQuery<T>.all(): List<E> = fetch(recordMapper)
    protected fun <T : Record> ResultQuery<T>.sequence(): Sequence<E> = asSequence().map(recordMapper::map)

    protected fun <T> transaction(block: DSLContext.() -> T): T = defaultContext.requireTransaction(block)

    protected fun ensureAffected(expected: Int, actual: Int) = when (actual) {
        0 -> throw NoDataFoundException("No records affected (expected: $expected)")
        expected -> Unit
        else -> throw TooManyRowsException("$actual records affected (expected: $expected)")
    }

    protected fun Query.executeSingle() = ensureAffected(1, execute())
    protected fun Batch.executeBatch() = ensureAffected(size(), execute().sum())

    fun delete(entity: E) = delete(entity.id)

    open fun <T> delete(id: T) {
        context.delete(table)
            .where(idField.eq(id))
            .executeSingle()
    }

    open fun update(entity: E) {
        if (entity is MutableEntity) entity.updatedAt = Instant.now()
        else throw IllegalArgumentException("$entity is not a MutableEntity")

        val record = entity.toRecord()
        context.update(table)
            .set(record)
            .where(idField.eq(entity.id))
            .executeSingle()
    }

    open fun insert(entity: E): E {
        beforeInsert(entity)
        val record = entity.toRecord()
        return transaction {
            val inserted = insertInto(table)
                .columns(*record.fields())
                .values(record.valuesRow())
                .returning()
                .fetchOne()
                .toEntity()

            afterInsert(listOf(entity))
            inserted
        }
    }

    open fun insert(entities: Collection<E>) {
        entities.forEach(::beforeInsert)
        val records = entities.map { it.toRecord() }
        transaction {
            batchInsert(records).executeBatch()
            afterInsert(entities)
        }
    }

    protected open fun beforeInsert(entity: E) {
        entity.insertedAt = Instant.now()
        val current = entity.id as? Number ?: 0L
        if (current == 0L) {
            val id = nextKey()
            if (id != null) entity.id = id
        }
    }

    protected fun nextKey(): Number? = sequence?.let {
        val next = it.nextval()
        context.select(next)
            .fetchOne(next)
    }

    protected open fun afterInsert(entities: Collection<E>) {}
}
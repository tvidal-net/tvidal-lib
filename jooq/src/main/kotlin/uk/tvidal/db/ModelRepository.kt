package uk.tvidal.db

import org.jooq.Condition
import org.jooq.Record
import org.jooq.ResultQuery
import org.jooq.UpdatableRecord
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class ModelRepository<E : Any> : JooqRepository() {

    protected abstract val entityType: KClass<E>

    protected fun E.toRecord(): Record = context.newRecord(table, this)

    protected fun <R : Record> R.toEntity(): E = into(entityType.java)

    operator fun <ID : Any> get(id: ID): E? {
        val condition = primaryKeyFields[0].eq(id)
        return query(condition).one()
    }

    operator fun get(vararg keys: Any): E? {
        var condition: Condition? = null
        for ((i, field) in primaryKeyFields.withIndex()) {
            val value = keys[i]
            condition = if (condition == null) field.eq(value)
            else condition.and(field.eq(value))
        }
        return query(condition!!).one()
    }

    protected fun <R : Record> ResultQuery<R>.one(): E? = fetchOne()?.toEntity()
    protected fun <R : Record> ResultQuery<R>.all(): List<E> = fetchInto(entityType.java)

    protected fun <R : Record> ResultQuery<R>.sequence(): Sequence<E> = sequence {
        fetchLazy().let { cursor ->
            cursor.forEach { yield(it.toEntity()) }
        }
    }

    open fun insert(entity: E) {
        val entities = listOf(entity)
        beforeInsertAll(entities)
        beforeInsertEach(entity)

        val record = entity.toRecord()
        transaction {
            insertInto(table)
                .columns(*record.fields())
                .values(*record.intoArray())
                .executeSingle()

            afterInsertEach(entity)
            afterInsertAll(entities)
        }
    }

    open fun insert(entities: Collection<E>) {
        beforeInsertAll(entities)
        entities.forEach(::beforeInsertEach)

        val records = entities.map {
            it.toRecord() as UpdatableRecord<*>
        }
        transaction {
            batchInsert(records).executeBatch()
            entities.forEach(::afterInsertEach)
            afterInsertAll(entities)
        }
    }

    protected open fun beforeInsertAll(entities: Collection<E>) {}
    protected open fun beforeInsertEach(entity: E) {}
    protected open fun afterInsertEach(entity: E) {}
    protected open fun afterInsertAll(entities: Collection<E>) {}

    open fun update(entity: E) {
        beforeUpdate(entity)

        val record = entity.toRecord()
        val condition = wherePrimaryKey(record)
        transaction {
            update(table)
                .set(record)
                .where(condition)
                .executeSingle()

            afterUpdate(entity)
        }
    }

    protected open fun beforeUpdate(entity: E) {}
    protected open fun afterUpdate(entity: E) {}

    fun delete(entity: E) {
        beforeDelete(entity)

        val record = entity.toRecord()
        val condition = wherePrimaryKey(record)

        transaction {
            deleteFrom(table)
                .where(condition)
                .executeSingle()

            afterDelete(entity)
        }
    }

    protected open fun beforeDelete(entity: E) {}
    protected open fun afterDelete(entity: E) {}
}

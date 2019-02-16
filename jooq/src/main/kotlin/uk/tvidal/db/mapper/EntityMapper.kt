package uk.tvidal.db.mapper

import org.jooq.Record

interface EntityMapper<E : Any> {
    fun fromRecord(record: Record): E
    fun toRecord(entity: E, record: Record)
}
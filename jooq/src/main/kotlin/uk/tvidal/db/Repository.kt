package uk.tvidal.db

import uk.tvidal.model.Entity

interface Repository<E : Entity<*>> {
    operator fun <T> get(id: T): E?
    fun insert(entity: E)
    fun insert(entities: Iterable<E>)
}
package uk.tvidal.db

import org.jooq.Sequence
import uk.tvidal.model.Entity
import uk.tvidal.model.MutableEntity
import java.time.Instant
import kotlin.reflect.KClass

abstract class EntityRepository<E : Entity<out Any?>>(
    override val entityType: KClass<E>
) : ModelRepository<E>() {

    protected open val sequence: Sequence<out Number>?
        get() = null

    protected open fun nextKey(): Any? = sequence?.let {
        val next = it.nextval()
        db.select(next)
            .fetchOne(next)
    }

    override fun beforeInsertAll(entities: Collection<E>) {
        val now = Instant.now()
        entities.forEach { it.created = now }
    }

    @Suppress("UNCHECKED_CAST")
    override fun beforeInsertEach(entity: E) {
        val id = entity.id
        if (id == null || id as? Number == 0) {
            nextKey()?.let {
                (entity as Entity<Any>).id = it
            }
        }
    }

    override fun beforeUpdate(entity: E) {
        if (entity is MutableEntity) {
            entity.modified = Instant.now()
        }
    }
}
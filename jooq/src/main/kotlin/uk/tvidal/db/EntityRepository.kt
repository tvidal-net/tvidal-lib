package uk.tvidal.db

import org.jooq.Sequence
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.value
import uk.tvidal.model.Entity
import uk.tvidal.model.MutableEntity
import java.time.Instant
import kotlin.reflect.KClass

abstract class EntityRepository<E : Entity<out Any?>>(
    override val entityType: KClass<E>
) : ModelRepository<E>() {

    protected open val sequence: Sequence<out Number>?
        get() = null

    protected open fun nextKeys(count: Int): List<Any> = sequence?.let { seq ->
        if (count == 1) {
            return db.select(seq.nextval())
                .fetchInto(Long::class.java)
        }

        val loop = name("loop")
        val lvl = field("lvl", Int::class.java)
        val recursive = select(lvl.plus(1))
            .from(loop)
            .where(lvl.lt(count))

        db.withRecursive(loop, lvl.unqualifiedName).`as`(
            select(value(1).`as`(lvl))
                .unionAll(recursive)
        ).select(seq.nextval())
            .from(loop)
            .fetchInto(Long::class.java)
    } ?: throw IllegalStateException("Sequence is not defined for ${javaClass.name}")

    @Suppress("UNCHECKED_CAST")
    override fun beforeInsertAll(entities: Collection<E>) {
        val now = Instant.now()
        entities.forEach { it.created = now }

        val needKey = entities.filter {
            it.id == null || (it.id as? Number)?.toLong() == 0L
        }
        val keys = nextKeys(needKey.size)
        for ((i, entity) in needKey.withIndex()) {
            val key = keys[i]
            (entity as Entity<Any>).id = key
        }
    }

    override fun beforeUpdate(entity: E) {
        if (entity is MutableEntity) {
            entity.modified = Instant.now()
        }
    }
}
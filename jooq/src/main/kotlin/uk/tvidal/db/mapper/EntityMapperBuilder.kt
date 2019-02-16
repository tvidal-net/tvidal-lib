package uk.tvidal.db.mapper

import uk.tvidal.model.Entity
import kotlin.reflect.KClass

class EntityMapperBuilder {

    private val propertyResolverMap = mutableMapOf<Class<*>, PropertyResolver<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun withResolver(propertyResolver: PropertyResolver<*>) = apply {
        val cls = propertyResolver.propertyType
        propertyResolverMap[cls] = propertyResolver as PropertyResolver<Any>
    }

    fun <E : Entity<*>> forEntity(entityType: KClass<E>): EntityMapper<E> =
        ReflectionMapper(entityType, propertyResolverMap)

    inline fun <reified E : Entity<*>> forEntity() = forEntity(E::class)
}
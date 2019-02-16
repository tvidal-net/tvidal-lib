package uk.tvidal.db.mapper

import uk.tvidal.model.Entity
import kotlin.reflect.KClass

class EntityMapperBuilder {

    private val propertyResolverMap = mutableMapOf<Class<*>, PropertyResolver<*>>()

    fun withResolver(propertyResolver: PropertyResolver<*>) = apply {
        val cls = propertyResolver.propertyType
        propertyResolverMap[cls] = propertyResolver
    }

    fun <E : Entity<*>> forEntity(entityType: KClass<E>): EntityMapper<E> =
        ReflectionMapper(entityType, propertyResolverMap)

    inline fun <reified E : Entity<*>> forEntity() = forEntity(E::class)
}
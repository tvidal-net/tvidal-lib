package uk.tvidal.db.mapper

import org.jooq.Record
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class ReflectionMapper<E : Any>(
    val entityType: KClass<E>,
    val propertyResolverMap: Map<Class<*>, PropertyResolver<*>> = emptyMap()
) : EntityMapper<E> {

    private val primaryConstructor = entityType.constructors
        .asSequence()
        .filter { it.visibility == PUBLIC }
        .maxBy { it.parameters.size }
        ?: throw IllegalArgumentException("Could not find a public constructor in $entityType")

    private val parameterFields: Map<KParameter, String> = primaryConstructor.parameters
        .associateWith { fieldName(it) }

    private val propertyFields: Map<KMutableProperty1<E, Any>, String>

    init {
        val parameterNames = parameterFields.keys
            .map { it.name }
            .toSet()

        propertyFields = entityType.memberProperties
            .asSequence()
            .filterIsInstance<KMutableProperty1<E, Any>>()
            .filterNot { it.name in parameterNames }
            .associateWith { fieldName(it) }
    }

    override fun fromRecord(record: Record): E {
        val args = constructorArgs(record)
        val entity = primaryConstructor.callBy(args)
        record.setMutableProperties(entity)
        return entity
    }

    private fun constructorArgs(record: Record): Map<KParameter, Any?> =
        parameterFields.entries.associate { (param, fieldName) ->
            val databaseValue = record[fieldName]
            val resolvedValue = param.resolvedValue(databaseValue)
            param to resolvedValue
        }

    private fun Record.setMutableProperties(entity: E) {
        for ((property, fieldName) in propertyFields) {
            val databaseValue = get(fieldName)
            databaseValue?.let {
                val resolvedValue = property.resolvedValue(it)
                property.set(entity, resolvedValue)
            }
        }
    }

    private fun KParameter.resolvedValue(databaseValue: Any?): Any? =
        databaseValue?.let { resolvedValue(it, type.jvmErasure) }

    private fun KProperty<*>.resolvedValue(databaseValue: Any): Any =
        resolvedValue(databaseValue, returnType.jvmErasure)

    private fun resolvedValue(originalValue: Any, expectedType: KClass<*>): Any {
        val resolver = propertyResolverMap[expectedType.java]
        return resolver?.fromDatabase(originalValue) ?: originalValue
    }

    override fun toRecord(entity: E, record: Record) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
package uk.tvidal.db.mapper

import org.jooq.Field
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
    val propertyResolverMap: Map<Class<*>, PropertyResolver<Any>> = emptyMap()
) : EntityMapper<E> {

    private val primaryConstructor = entityType.constructors
        .asSequence()
        .filter { it.visibility == PUBLIC }
        .maxBy { it.parameters.size }
        ?: throw IllegalArgumentException("Could not find a public constructor in $entityType")

    private val parameterFields: Map<KParameter, String> = primaryConstructor.parameters
        .associateWith { fieldName(it) }

    private val mutableProperties: Map<KMutableProperty1<E, Any>, String>

    private val fieldProperties: Map<String, KProperty<*>> = entityType.memberProperties
        .associateBy { fieldName(it) }

    init {
        val parameterNames = parameterFields.keys
            .map { it.name }
            .toSet()

        mutableProperties = entityType.memberProperties
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

    override fun toRecord(entity: E, record: Record) {
        for (field in record.fields().filterIsInstance<Field<Any>>()) {
            fieldProperties[field.name]?.run {
                record[field] = databaseValue(entity)
            }
        }
    }

    private fun constructorArgs(record: Record): Map<KParameter, Any?> =
        parameterFields.entries.associate { (param, fieldName) ->
            val databaseValue = record[fieldName]
            val resolvedValue = param.resolvedValue(databaseValue)
            param to resolvedValue
        }

    private fun Record.setMutableProperties(entity: E) {
        for ((property, fieldName) in mutableProperties) {
            val databaseValue = get(fieldName)
            databaseValue?.let {
                val resolvedValue = property.resolvedValue(it)
                property.set(entity, resolvedValue)
            }
        }
    }

    private fun KProperty<*>.databaseValue(entity: E): Any? = call(entity)?.let {
        val propertyType = returnType.jvmErasure
        val resolver = propertyResolverMap[propertyType.java]
        resolver?.toDatabase(it) ?: it
    }

    private fun KProperty<*>.resolvedValue(databaseValue: Any): Any =
        resolvedValue(databaseValue, returnType.jvmErasure)

    private fun KParameter.resolvedValue(databaseValue: Any?): Any? =
        databaseValue?.let { resolvedValue(it, type.jvmErasure) }

    private fun resolvedValue(originalValue: Any, expectedType: KClass<*>): Any {
        val resolver = propertyResolverMap[expectedType.java]
        return resolver?.fromDatabase(originalValue) ?: originalValue
    }
}
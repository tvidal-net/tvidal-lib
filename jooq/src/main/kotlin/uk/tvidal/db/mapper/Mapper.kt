package uk.tvidal.db.mapper

import org.jooq.Field
import org.jooq.Name
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

const val FIELD_NAME_SEPARATOR = '_'

fun propertyName(field: Field<*>) = propertyName(field.name)

fun propertyName(name: Name) = propertyName(name.last())

fun propertyName(fieldName: String) = buildString {
    var sep = false
    for (ch in fieldName) {
        when (ch) {
            FIELD_NAME_SEPARATOR -> sep = true
            else -> if (sep) {
                append(ch.toUpperCase())
                sep = false
            } else append(ch.toLowerCase())
        }
    }
}

fun fieldName(parameter: KParameter) = fieldName(parameter.name!!)

fun fieldName(property: KProperty<*>) = fieldName(property.name)

fun fieldName(propertyName: String) = buildString {
    for (ch in propertyName) {
        if (ch.isUpperCase()) {
            append(FIELD_NAME_SEPARATOR)
        }
        append(ch.toLowerCase())
    }
}
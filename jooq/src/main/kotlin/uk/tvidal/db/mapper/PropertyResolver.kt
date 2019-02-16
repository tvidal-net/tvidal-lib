package uk.tvidal.db.mapper

interface PropertyResolver<T> {
    val propertyType: Class<T>
    fun fromDatabase(databaseValue: Any): T
    fun toDatabase(entityValue: T): Any
}
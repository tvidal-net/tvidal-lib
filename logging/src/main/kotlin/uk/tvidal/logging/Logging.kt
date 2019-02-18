package uk.tvidal.logging

import kotlin.reflect.KClass

internal fun javaClassName(cls: Class<*>): String =
    cls.name.substringBefore('$')

internal fun javaClassName(kClass: KClass<*>): String =
    javaClassName(kClass.java)

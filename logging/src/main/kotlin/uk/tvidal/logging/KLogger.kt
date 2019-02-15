package uk.tvidal.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class KLogger(val logger: Logger) : Logger by logger {

    constructor(logger: String) : this(LoggerFactory.getLogger(logger))

    constructor(cls: Class<*>) : this(javaClassName(cls))

    constructor(cls: KClass<*>) : this(javaClassName(cls))

    constructor(block: () -> Unit) : this(block.javaClass)

    inline fun trace(message: () -> Any?) {
        if (isTraceEnabled) {
            trace(message().toString())
        }
    }

    inline fun debug(message: () -> Any?) {
        if (isDebugEnabled) {
            debug(message().toString())
        }
    }

    inline fun info(message: () -> Any?) {
        info(message().toString())
    }

    inline fun warn(message: () -> Any?) {
        warn(message().toString())
    }

    inline fun error(e: Throwable? = null, message: () -> Any?) {
        error(message().toString(), e)
    }

    fun error(e: Throwable) {
        error("${e::class.simpleName}: ${e.message}", e)
    }

    inline fun <reified E : Exception, T> catch(block: () -> T): T? = try {
        block()
    } catch (e: Throwable) {
        error(e)
        if (e !is E) throw e
        else null
    }
}
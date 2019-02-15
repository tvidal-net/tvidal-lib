package uk.tvidal.logging

abstract class KLogging {
    val log = KLogger(this::class)
}
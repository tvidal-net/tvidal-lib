package uk.tvidal.db

interface DatabaseTransaction {
    val inTransaction: Boolean
    operator fun <T> invoke(block: () -> T): T
}
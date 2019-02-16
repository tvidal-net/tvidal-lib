package uk.tvidal

fun <T> Boolean?.then(value: T?): T? =
    if (this == true) value else null

inline fun <T> Boolean?.then(value: () -> T?): T? =
    if (this == true) value() else null
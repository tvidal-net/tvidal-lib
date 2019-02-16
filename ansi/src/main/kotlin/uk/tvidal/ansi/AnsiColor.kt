package uk.tvidal.ansi

enum class AnsiColor(val id: Number) {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7);

    operator fun invoke(value: Any?, bright: Boolean = false): String {
        val brightPrefix = if (bright) ";1" else ""
        return if (!hasAnsiSupport) value.toString()
        else "$ESC[3$id${brightPrefix}m$value$COLOR_RESET"
    }

    fun print(value: Any?, bright: Boolean = false) {
        System.out.print(this(value, bright))
    }
}
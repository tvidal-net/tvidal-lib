package uk.tvidal.ansi

import java.lang.Thread.sleep
import kotlin.math.min
import kotlin.math.round

const val DEFAULT_DELAY = 333L
const val ESC = 0x1B.toChar()
const val COLOR_RESET = "$ESC[m"

val hasAnsiSupport: Boolean = System.console() != null

fun black(value: Any?) = AnsiColor.BLACK(value)
fun red(value: Any?) = AnsiColor.RED(value)
fun green(value: Any?) = AnsiColor.GREEN(value)
fun yellow(value: Any?) = AnsiColor.YELLOW(value)
fun blue(value: Any?) = AnsiColor.BLUE(value)
fun magenta(value: Any?) = AnsiColor.MAGENTA(value)
fun cyan(value: Any?) = AnsiColor.CYAN(value)
fun white(value: Any?) = AnsiColor.WHITE(value)

fun brightBlack(value: Any?) = AnsiColor.BLACK(value, true)
fun brightRed(value: Any?) = AnsiColor.RED(value, true)
fun brightGreen(value: Any?) = AnsiColor.GREEN(value, true)
fun brightYellow(value: Any?) = AnsiColor.YELLOW(value, true)
fun brightBlue(value: Any?) = AnsiColor.BLUE(value, true)
fun brightMagenta(value: Any?) = AnsiColor.MAGENTA(value, true)
fun brightCyan(value: Any?) = AnsiColor.CYAN(value, true)
fun brightWhite(value: Any?) = AnsiColor.WHITE(value, true)

fun up(lines: Number = 1) = AnsiMove.UP(lines)
fun down(lines: Number = 1) = AnsiMove.DOWN(lines)
fun right(chars: Number = 1) = AnsiMove.RIGHT(chars)
fun left(chars: Number = 1) = AnsiMove.LEFT(chars)
fun next(lines: Number = 1) = AnsiMove.NEXT(lines)
fun prev(lines: Number = 1) = AnsiMove.PREV(lines)
fun col(col: Number = 1) = AnsiMove.COL(col)
fun pos(line: Number = 1, col: Number = 1) = AnsiMove.POS("$line;$col")
fun clearDown() = AnsiMove.CLEAR_DISPLAY(0)
fun clearUp() = AnsiMove.CLEAR_DISPLAY(1)
fun clear() = AnsiMove.CLEAR_DISPLAY(2)
fun clearForward() = AnsiMove.CLEAR_LINE(0)

fun scrollUp(lines: Number = 1) = AnsiMove.SCROLL_UP(lines)
fun scrollDown(lines: Number = 1) = AnsiMove.SCROLL_DOWN(lines)
fun saveCursor() = AnsiMove.SAVE_CURSOR("", "")
fun restoreCursor() = AnsiMove.RESTORE_CURSOR("", "")

fun clearBack(col: Number = 1) {
    AnsiMove.CLEAR_LINE(1)
    col(col)
}

fun clearLine() {
    AnsiMove.CLEAR_LINE(2)
    col()
}

fun replaceLine(value: Any) {
    clearLine()
    print(value)
}

fun printInPlace(delay: Long = DEFAULT_DELAY, block: () -> Any?) {
    do {
        val value = block()
        value?.let {
            replaceLine(it)
            sleep(delay)
        }
    } while (value != null)
}

fun progressBar(
    delay: Long = DEFAULT_DELAY,
    width: Int = 36,
    prefix: String = "[",
    suffix: String = "]",
    progressChar: Char = '#',
    progressProvider: () -> Double
) {
    do {
        val progress = progressProvider()
        val currentCount = min(round(progress * width).toInt(), width)
        print(prefix)
        repeat(currentCount) { print(progressChar) }
        repeat(width - currentCount) { print(' ') }
        print(suffix)
        sleep(delay)
        clearLine()
    } while (progress < 1.0)
}
package uk.tvidal.ansi

import java.lang.System.lineSeparator

enum class AnsiMove(val direction: Char) {
    UP('A'),
    DOWN('B'),
    RIGHT('C'),
    LEFT('D'),
    NEXT('E'),
    PREV('F'),
    COL('G'),
    POS('H'),
    CLEAR_DISPLAY('J'),
    CLEAR_LINE('K'),
    SCROLL_UP('S'),
    SCROLL_DOWN('T'),
    SAVE_CURSOR('s'),
    RESTORE_CURSOR('u');

    operator fun invoke(value: Any, default: String = lineSeparator()) {
        if (hasAnsiSupport) print("$ESC[$value$direction")
        else print(default)
    }
}
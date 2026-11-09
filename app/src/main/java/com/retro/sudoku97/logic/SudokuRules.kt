package com.retro.sudoku97.logic

import com.retro.sudoku97.model.BOARD_SIZE
import com.retro.sudoku97.model.CELL_COUNT
import com.retro.sudoku97.model.FULL_CANDIDATE_MASK

enum class UnitKind {
    ROW,
    COLUMN,
    BOX
}

data class SudokuUnit(
    val kind: UnitKind,
    val number: Int,
    val cells: IntArray
) {
    val label: String
        get() = when (kind) {
            UnitKind.ROW -> "row ${number + 1}"
            UnitKind.COLUMN -> "column ${number + 1}"
            UnitKind.BOX -> "box ${number + 1}"
        }
}

/**
 * Pure rules helpers shared by the game, conflict highlighter and hint engine.
 */
object SudokuRules {

    val ROWS: List<SudokuUnit> = (0 until BOARD_SIZE).map { row ->
        SudokuUnit(
            kind = UnitKind.ROW,
            number = row,
            cells = IntArray(BOARD_SIZE) { column -> row * BOARD_SIZE + column }
        )
    }

    val COLUMNS: List<SudokuUnit> = (0 until BOARD_SIZE).map { column ->
        SudokuUnit(
            kind = UnitKind.COLUMN,
            number = column,
            cells = IntArray(BOARD_SIZE) { row -> row * BOARD_SIZE + column }
        )
    }

    val BOXES: List<SudokuUnit> = (0 until BOARD_SIZE).map { box ->
        val startRow = (box / 3) * 3
        val startColumn = (box % 3) * 3
        SudokuUnit(
            kind = UnitKind.BOX,
            number = box,
            cells = IntArray(BOARD_SIZE) { offset ->
                val row = startRow + offset / 3
                val column = startColumn + offset % 3
                row * BOARD_SIZE + column
            }
        )
    }

    val ALL_UNITS: List<SudokuUnit> = ROWS + COLUMNS + BOXES

    val PEERS: Array<IntArray> = Array(CELL_COUNT) { index ->
        val row = index / BOARD_SIZE
        val column = index % BOARD_SIZE
        val box = (row / 3) * 3 + (column / 3)
        val peers = linkedSetOf<Int>()
        ROWS[row].cells.forEach { peers.add(it) }
        COLUMNS[column].cells.forEach { peers.add(it) }
        BOXES[box].cells.forEach { peers.add(it) }
        peers.remove(index)
        peers.toIntArray()
    }

    fun candidateMask(values: List<Int>, index: Int): Int {
        if (values.size != CELL_COUNT || index !in 0 until CELL_COUNT) return 0
        if (values[index] != 0) return 0
        var used = 0
        for (peer in PEERS[index]) {
            val value = values[peer]
            if (value in 1..9) used = used or (1 shl (value - 1))
        }
        return FULL_CANDIDATE_MASK and used.inv()
    }

    fun candidateMasks(values: List<Int>): IntArray =
        IntArray(CELL_COUNT) { index -> candidateMask(values, index) }

    fun candidateDigits(mask: Int): List<Int> =
        (1..9).filter { mask and (1 shl (it - 1)) != 0 }

    fun findConflicts(values: List<Int>): Set<Int> {
        if (values.size != CELL_COUNT) return emptySet()
        val conflicts = linkedSetOf<Int>()
        for (unit in ALL_UNITS) {
            val positionsByValue = HashMap<Int, MutableList<Int>>()
            for (index in unit.cells) {
                val value = values[index]
                if (value in 1..9) {
                    positionsByValue.getOrPut(value) { mutableListOf() }.add(index)
                }
            }
            for (positions in positionsByValue.values) {
                if (positions.size > 1) conflicts.addAll(positions)
            }
        }
        return conflicts
    }

    fun isSolved(values: List<Int>, solution: List<Int>): Boolean {
        if (values.size != CELL_COUNT || solution.size != CELL_COUNT) return false
        if (values.any { it == 0 }) return false
        return values == solution
    }

    fun isBoardFull(values: List<Int>): Boolean =
        values.size == CELL_COUNT && values.none { it == 0 }

    fun unitForCellAndKind(index: Int, kind: UnitKind): SudokuUnit {
        val row = index / BOARD_SIZE
        val column = index % BOARD_SIZE
        return when (kind) {
            UnitKind.ROW -> ROWS[row]
            UnitKind.COLUMN -> COLUMNS[column]
            UnitKind.BOX -> BOXES[(row / 3) * 3 + column / 3]
        }
    }
}

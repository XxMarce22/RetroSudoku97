package com.retro.sudoku97.logic

import com.retro.sudoku97.model.CELL_COUNT

private fun cellName(index: Int): String {
    val row = index / 9 + 1
    val column = index % 9 + 1
    return "row $row, column $column"
}

sealed interface Hint {
    val message: String
    val highlightCells: Set<Int>

    data class NakedSingle(
        val cell: Int,
        val value: Int
    ) : Hint {
        override val message: String
            get() = "Naked single: ${cellName(cell)} must be $value."
        override val highlightCells: Set<Int>
            get() = setOf(cell)
    }

    data class HiddenSingle(
        val cell: Int,
        val value: Int,
        val unitLabel: String
    ) : Hint {
        override val message: String
            get() = "Hidden single: $value can only go in ${cellName(cell)} in $unitLabel."
        override val highlightCells: Set<Int>
            get() = setOf(cell)
    }

    data class NakedPair(
        val pairCells: Set<Int>,
        val digits: Set<Int>,
        val eliminations: Map<Int, Int>,
        val unitLabel: String
    ) : Hint {
        override val message: String
            get() = "Naked pair (${digits.sorted().joinToString(", ")}) in $unitLabel. " +
                "Those candidates were removed from the highlighted cells."
        override val highlightCells: Set<Int>
            get() = pairCells + eliminations.keys
    }

    data class Reveal(
        val cell: Int,
        val value: Int
    ) : Hint {
        override val message: String
            get() = "No simple pattern found. Revealed ${cellName(cell)} = $value."
        override val highlightCells: Set<Int>
            get() = setOf(cell)
    }
}

/**
 * Finds the next human-style move. It never returns the full solution:
 * - naked / hidden singles (single-candidate logic)
 * - naked pairs with candidate eliminations
 * - a single revealed cell as the last-resort fallback
 */
object HintEngine {

    fun findHint(
        values: List<Int>,
        solution: List<Int>,
        selectedCell: Int? = null,
        userCandidates: List<Int>? = null
    ): Hint? {
        if (values.size != CELL_COUNT || solution.size != CELL_COUNT) return null
        if (values.none { it == 0 }) return null

        val masks = SudokuRules.candidateMasks(values)
        val effectiveMasks = IntArray(CELL_COUNT) { index ->
            if (values[index] != 0) {
                0
            } else {
                val userMask = userCandidates?.getOrNull(index) ?: 0
                if (userMask == 0) masks[index] else masks[index] and userMask
            }
        }

        // Prefer a single in the selected cell.
        if (selectedCell != null &&
            selectedCell in 0 until CELL_COUNT &&
            values[selectedCell] == 0 &&
            Integer.bitCount(masks[selectedCell]) == 1
        ) {
            val value = Integer.numberOfTrailingZeros(masks[selectedCell]) + 1
            if (solution[selectedCell] == value) {
                return Hint.NakedSingle(selectedCell, value)
            }
        }

        // Naked singles.
        for (index in 0 until CELL_COUNT) {
            if (values[index] != 0) continue
            val mask = masks[index]
            if (Integer.bitCount(mask) == 1) {
                val value = Integer.numberOfTrailingZeros(mask) + 1
                if (solution[index] == value) {
                    return Hint.NakedSingle(index, value)
                }
            }
        }

        // Hidden singles.
        for (unit in SudokuRules.ALL_UNITS) {
            val usedValues = unit.cells
                .map { values[it] }
                .filter { it in 1..9 }
                .toSet()
            for (digit in 1..9) {
                if (digit in usedValues) continue
                val bit = 1 shl (digit - 1)
                val possibleCells = unit.cells.filter { index ->
                    values[index] == 0 && masks[index] and bit != 0
                }
                if (possibleCells.size == 1) {
                    val cell = possibleCells.first()
                    if (solution[cell] == digit) {
                        return Hint.HiddenSingle(cell, digit, unit.label)
                    }
                }
            }
        }

        // Naked pairs.
        for (unit in SudokuRules.ALL_UNITS) {
            val twoCandidateCells = unit.cells.filter { index ->
                values[index] == 0 && Integer.bitCount(masks[index]) == 2
            }

            for (firstIndex in twoCandidateCells.indices) {
                val firstCell = twoCandidateCells[firstIndex]
                val pairMask = masks[firstCell]
                for (secondIndex in firstIndex + 1 until twoCandidateCells.size) {
                    val secondCell = twoCandidateCells[secondIndex]
                    if (masks[secondCell] != pairMask) continue

                    val eliminations = linkedMapOf<Int, Int>()
                    for (otherCell in unit.cells) {
                        if (otherCell == firstCell || otherCell == secondCell) continue
                        if (values[otherCell] != 0) continue
                        val removable = effectiveMasks[otherCell] and pairMask
                        if (removable != 0) {
                            eliminations[otherCell] = removable
                        }
                    }

                    if (eliminations.isNotEmpty()) {
                        val digits = SudokuRules.candidateDigits(pairMask).toSet()
                        return Hint.NakedPair(
                            pairCells = setOf(firstCell, secondCell),
                            digits = digits,
                            eliminations = eliminations,
                            unitLabel = unit.label
                        )
                    }
                }
            }
        }

        // Fallback: reveal exactly one correct cell.
        val emptyCells = (0 until CELL_COUNT).filter { values[it] == 0 }
        val fallbackCell = selectedCell
            ?.takeIf { it in 0 until CELL_COUNT && values[it] == 0 }
            ?: emptyCells.firstOrNull()
            ?: return null
        val value = solution[fallbackCell]
        if (value !in 1..9) return null
        return Hint.Reveal(fallbackCell, value)
    }
}

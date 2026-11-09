package com.retro.sudoku97.logic

import com.retro.sudoku97.model.CELL_COUNT
import com.retro.sudoku97.model.Difficulty
import kotlin.random.Random

data class GeneratedPuzzle(
    val difficulty: Difficulty,
    val puzzle: IntArray,
    val solution: IntArray,
    val seed: Long
) {
    init {
        require(puzzle.size == CELL_COUNT)
        require(solution.size == CELL_COUNT)
    }
}

/**
 * Deterministic generator: the same seed always produces the same puzzle.
 *
 * 1. Fill a complete board with randomized backtracking.
 * 2. Remove cells in a shuffled order.
 * 3. After every removal, verify that exactly one solution remains using the
 *    dual-direction backtracking solver. Restore the cell if uniqueness is lost.
 * 4. Stop at a target clue count inside the requested difficulty range.
 */
object SudokuGenerator {

    private const val MAX_ATTEMPTS = 24
    private const val ATTEMPT_SALT = 0x9E3779B97F4A7C15uL

    fun generate(difficulty: Difficulty, seed: Long = System.nanoTime()): GeneratedPuzzle {
        val random = Random(seed)
        repeat(MAX_ATTEMPTS) { attempt ->
            val boardSeed = seed xor (attempt.toLong() * ATTEMPT_SALT.toLong())
            val solution = IntArray(CELL_COUNT)
            if (!fillCompleteBoard(solution, Random(boardSeed))) {
                return@repeat
            }

            val puzzle = solution.copyOf()
            val targetClues = difficulty.clueRange.random(random)
            val removalOrder = (0 until CELL_COUNT).shuffled(random)
            var clueCount = CELL_COUNT

            for (position in removalOrder) {
                if (clueCount <= targetClues) break
                val removedValue = puzzle[position]
                if (removedValue == 0) continue
                puzzle[position] = 0

                if (SudokuSolver.hasUniqueSolution(puzzle)) {
                    clueCount--
                } else {
                    puzzle[position] = removedValue
                }
            }

            if (clueCount !in difficulty.clueRange) return@repeat

            // Final independent checks: uniqueness in both directions and a
            // solution that still matches the complete board we started with.
            if (!SudokuSolver.hasUniqueSolution(puzzle)) return@repeat
            val solved = SudokuSolver.solve(puzzle) ?: return@repeat
            if (!solved.contentEquals(solution)) return@repeat

            return GeneratedPuzzle(
                difficulty = difficulty,
                puzzle = puzzle,
                solution = solution,
                seed = seed
            )
        }

        throw IllegalStateException(
            "Could not generate a ${difficulty.displayName} puzzle. Please try again."
        )
    }

    private fun fillCompleteBoard(board: IntArray, random: Random): Boolean {
        val rows = IntArray(9)
        val columns = IntArray(9)
        val boxes = IntArray(9)

        fun fill(): Boolean {
            var bestIndex = -1
            var bestMask = 0
            var bestCount = 10

            for (index in 0 until CELL_COUNT) {
                if (board[index] != 0) continue
                val row = index / 9
                val column = index % 9
                val box = (row / 3) * 3 + (column / 3)
                val mask = SudokuSolver.FULL_MASK and
                    (rows[row] or columns[column] or boxes[box]).inv()
                val count = Integer.bitCount(mask)
                if (count == 0) return false
                if (count < bestCount) {
                    bestIndex = index
                    bestMask = mask
                    bestCount = count
                    if (count == 1) break
                }
            }

            if (bestIndex == -1) return true

            val row = bestIndex / 9
            val column = bestIndex % 9
            val box = (row / 3) * 3 + (column / 3)
            val candidates = (1..9)
                .filter { bestMask and (1 shl (it - 1)) != 0 }
                .shuffled(random)

            for (digit in candidates) {
                val bit = 1 shl (digit - 1)
                board[bestIndex] = digit
                rows[row] = rows[row] or bit
                columns[column] = columns[column] or bit
                boxes[box] = boxes[box] or bit

                if (fill()) return true

                board[bestIndex] = 0
                rows[row] = rows[row] and bit.inv()
                columns[column] = columns[column] and bit.inv()
                boxes[box] = boxes[box] and bit.inv()
            }

            return false
        }

        return fill()
    }
}

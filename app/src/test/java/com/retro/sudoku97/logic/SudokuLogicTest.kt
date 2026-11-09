package com.retro.sudoku97.logic

import com.retro.sudoku97.model.Difficulty
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuLogicTest {

    private val solved = intArrayOf(
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
    )

    @Test
    fun emptyBoardHasMoreThanOneSolution() {
        val empty = IntArray(81)
        assertEquals(2, SudokuSolver.countSolutions(empty, limit = 2))
        assertFalse(SudokuSolver.hasUniqueSolution(empty))
    }

    @Test
    fun solverSolvesKnownBoard() {
        val puzzle = solved.copyOf()
        puzzle[0] = 0
        puzzle[10] = 0
        puzzle[80] = 0

        val result = SudokuSolver.solve(puzzle)
        assertNotNull(result)
        assertArrayEquals(solved, result)
        assertTrue(SudokuSolver.hasUniqueSolution(puzzle))
    }

    @Test
    fun generatorProducesClueRangeAndUniqueSolutionForEveryDifficulty() {
        Difficulty.entries.forEachIndexed { index, difficulty ->
            val generated = SudokuGenerator.generate(
                difficulty = difficulty,
                seed = 97_000L + index * 1_013L
            )
            val clues = generated.puzzle.count { it != 0 }
            assertTrue(
                "${difficulty.displayName} produced $clues clues",
                clues in difficulty.clueRange
            )
            assertEquals(1, SudokuSolver.countSolutions(generated.puzzle, limit = 2))
            assertTrue(SudokuSolver.hasUniqueSolution(generated.puzzle))

            val solution = SudokuSolver.solve(generated.puzzle)
            assertNotNull(solution)
            assertArrayEquals(generated.solution, solution)
            assertValidCompleteBoard(generated.solution)
            assertTrue(
                generated.puzzle.indices.all {
                    generated.puzzle[it] == 0 || generated.puzzle[it] == generated.solution[it]
                }
            )
        }
    }

    @Test
    fun generatorIsDeterministicForSameSeed() {
        val first = SudokuGenerator.generate(Difficulty.MEDIUM, seed = 424_242L)
        val second = SudokuGenerator.generate(Difficulty.MEDIUM, seed = 424_242L)
        assertArrayEquals(first.puzzle, second.puzzle)
        assertArrayEquals(first.solution, second.solution)
    }

    @Test
    fun hardGeneratorStaysInRangeAcrossManySeeds() {
        repeat(30) { offset ->
            val generated = SudokuGenerator.generate(
                Difficulty.HARD,
                seed = 8_000L + offset
            )
            val clues = generated.puzzle.count { it != 0 }
            assertTrue("seed ${8_000L + offset} produced $clues clues", clues in Difficulty.HARD.clueRange)
            assertTrue(SudokuSolver.hasUniqueSolution(generated.puzzle))
        }
    }

    @Test
    fun conflictDetectionMarksBothDuplicateCells() {
        val values = solved.toMutableList()
        values[1] = 5 // row 1 and the top-left box already contain a 5 at index 0
        val conflicts = SudokuRules.findConflicts(values)
        assertTrue(conflicts.contains(0))
        assertTrue(conflicts.contains(1))
    }

    @Test
    fun nakedSingleHintFindsCorrectCell() {
        val values = solved.toMutableList()
        values[0] = 0
        val hint = HintEngine.findHint(values, solved.toList(), selectedCell = 0)
        assertTrue(hint is Hint.NakedSingle)
        hint as Hint.NakedSingle
        assertEquals(0, hint.cell)
        assertEquals(5, hint.value)
    }

    @Test
    fun hintFallsBackToRevealingOneCellWhenNoSimplePatternExists() {
        val empty = List(81) { 0 }
        val hint = HintEngine.findHint(empty, solved.toList(), selectedCell = null)
        assertTrue(hint is Hint.Reveal)
        hint as Hint.Reveal
        assertEquals(0, hint.cell)
        assertEquals(5, hint.value)
    }

    @Test
    fun nakedPairHintReturnsCandidateEliminations() {
        val board = (
            "896137020" +
                "123400978" +
                "007200136" +
                "234801060" +
                "015300840" +
                "908504010" +
                "002610090" +
                "009740081" +
                "001903057"
            ).map { it - '0' }.toList()
        val solution = SudokuSolver.solve(board.toIntArray())
        assertNotNull(solution)
        val hint = HintEngine.findHint(board, solution!!.toList())
        assertTrue(hint is Hint.NakedPair)
        hint as Hint.NakedPair
        assertEquals(2, hint.digits.size)
        assertTrue(hint.eliminations.isNotEmpty())
        assertTrue(hint.highlightCells.size >= 3)
    }

    @Test
    fun solvedBoardDetectionIsExact() {
        assertTrue(SudokuRules.isSolved(solved.toList(), solved.toList()))
        val wrong = solved.toMutableList()
        wrong[0] = 1
        assertFalse(SudokuRules.isSolved(wrong, solved.toList()))
    }

    private fun assertValidCompleteBoard(board: IntArray) {
        assertEquals(81, board.size)
        assertTrue(board.all { it in 1..9 })
        for (unit in SudokuRules.ALL_UNITS) {
            val values = unit.cells.map { board[it] }.toSet()
            assertEquals(9, values.size)
            assertTrue(values.containsAll((1..9).toSet()))
        }
    }
}

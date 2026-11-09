package com.retro.sudoku97.logic

/**
 * Bit-mask Sudoku solver used by both the generator and the uniqueness verifier.
 *
 * Uniqueness is checked in both scan directions. Each directional pass uses a
 * minimum-remaining-values search (so it stays fast) but breaks ties according to
 * the requested direction and visits candidates in ascending or descending order.
 * A puzzle is accepted only when both passes count exactly one solution.
 */
object SudokuSolver {

    const val SIZE = 9
    const val CELLS = 81
    const val FULL_MASK = 0x1FF

    fun countSolutions(board: IntArray, limit: Int = 2): Int =
        countSolutions(board, limit, reverse = false)

    fun countSolutions(board: IntArray, limit: Int, reverse: Boolean): Int {
        require(board.size == CELLS) { "A Sudoku board must contain 81 cells" }
        val copy = board.copyOf()
        val rows = IntArray(SIZE)
        val columns = IntArray(SIZE)
        val boxes = IntArray(SIZE)

        for (index in 0 until CELLS) {
            val value = copy[index]
            if (value == 0) continue
            if (value !in 1..9) return 0
            val row = index / SIZE
            val column = index % SIZE
            val box = (row / 3) * 3 + (column / 3)
            val bit = 1 shl (value - 1)
            if ((rows[row] or columns[column] or boxes[box]) and bit != 0) return 0
            rows[row] = rows[row] or bit
            columns[column] = columns[column] or bit
            boxes[box] = boxes[box] or bit
        }

        return search(
            board = copy,
            rows = rows,
            columns = columns,
            boxes = boxes,
            limit = limit.coerceAtLeast(1),
            reverse = reverse
        )
    }

    /**
     * Both directional passes must agree that there is exactly one solution.
     */
    fun hasUniqueSolution(board: IntArray): Boolean =
        countSolutions(board, limit = 2, reverse = false) == 1 &&
            countSolutions(board, limit = 2, reverse = true) == 1

    fun solve(board: IntArray): IntArray? {
        require(board.size == CELLS) { "A Sudoku board must contain 81 cells" }
        val copy = board.copyOf()
        val rows = IntArray(SIZE)
        val columns = IntArray(SIZE)
        val boxes = IntArray(SIZE)

        for (index in 0 until CELLS) {
            val value = copy[index]
            if (value == 0) continue
            if (value !in 1..9) return null
            val row = index / SIZE
            val column = index % SIZE
            val box = (row / 3) * 3 + (column / 3)
            val bit = 1 shl (value - 1)
            if ((rows[row] or columns[column] or boxes[box]) and bit != 0) return null
            rows[row] = rows[row] or bit
            columns[column] = columns[column] or bit
            boxes[box] = boxes[box] or bit
        }

        return if (solveInPlace(copy, rows, columns, boxes)) copy else null
    }

    private fun search(
        board: IntArray,
        rows: IntArray,
        columns: IntArray,
        boxes: IntArray,
        limit: Int,
        reverse: Boolean
    ): Int {
        var bestIndex = -1
        var bestMask = 0
        var bestCount = 10

        if (reverse) {
            for (index in CELLS - 1 downTo 0) {
                if (board[index] != 0) continue
                val mask = candidateMask(index, rows, columns, boxes)
                val count = Integer.bitCount(mask)
                if (count == 0) return 0
                if (count < bestCount) {
                    bestIndex = index
                    bestMask = mask
                    bestCount = count
                    if (count == 1) break
                }
            }
        } else {
            for (index in 0 until CELLS) {
                if (board[index] != 0) continue
                val mask = candidateMask(index, rows, columns, boxes)
                val count = Integer.bitCount(mask)
                if (count == 0) return 0
                if (count < bestCount) {
                    bestIndex = index
                    bestMask = mask
                    bestCount = count
                    if (count == 1) break
                }
            }
        }

        if (bestIndex == -1) return 1

        val row = bestIndex / SIZE
        val column = bestIndex % SIZE
        val box = (row / 3) * 3 + (column / 3)
        var remaining = bestMask
        var total = 0

        while (remaining != 0) {
            val bit = if (reverse) Integer.highestOneBit(remaining) else remaining and -remaining
            val digit = Integer.numberOfTrailingZeros(bit) + 1
            board[bestIndex] = digit
            rows[row] = rows[row] or bit
            columns[column] = columns[column] or bit
            boxes[box] = boxes[box] or bit

            total += search(board, rows, columns, boxes, limit, reverse)
            if (total >= limit) {
                board[bestIndex] = 0
                rows[row] = rows[row] and bit.inv()
                columns[column] = columns[column] and bit.inv()
                boxes[box] = boxes[box] and bit.inv()
                return total
            }

            board[bestIndex] = 0
            rows[row] = rows[row] and bit.inv()
            columns[column] = columns[column] and bit.inv()
            boxes[box] = boxes[box] and bit.inv()
            remaining = remaining xor bit
        }

        return total
    }

    private fun solveInPlace(
        board: IntArray,
        rows: IntArray,
        columns: IntArray,
        boxes: IntArray
    ): Boolean {
        var bestIndex = -1
        var bestMask = 0
        var bestCount = 10

        for (index in 0 until CELLS) {
            if (board[index] != 0) continue
            val mask = candidateMask(index, rows, columns, boxes)
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

        val row = bestIndex / SIZE
        val column = bestIndex % SIZE
        val box = (row / 3) * 3 + (column / 3)
        var remaining = bestMask
        while (remaining != 0) {
            val bit = remaining and -remaining
            val digit = Integer.numberOfTrailingZeros(bit) + 1
            board[bestIndex] = digit
            rows[row] = rows[row] or bit
            columns[column] = columns[column] or bit
            boxes[box] = boxes[box] or bit

            if (solveInPlace(board, rows, columns, boxes)) return true

            board[bestIndex] = 0
            rows[row] = rows[row] and bit.inv()
            columns[column] = columns[column] and bit.inv()
            boxes[box] = boxes[box] and bit.inv()
            remaining = remaining xor bit
        }
        return false
    }

    private fun candidateMask(
        index: Int,
        rows: IntArray,
        columns: IntArray,
        boxes: IntArray
    ): Int {
        val row = index / SIZE
        val column = index % SIZE
        val box = (row / 3) * 3 + (column / 3)
        return FULL_MASK and (rows[row] or columns[column] or boxes[box]).inv()
    }
}

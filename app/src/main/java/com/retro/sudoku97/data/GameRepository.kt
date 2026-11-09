package com.retro.sudoku97.data

import android.content.Context
import android.content.SharedPreferences
import com.retro.sudoku97.model.CELL_COUNT
import com.retro.sudoku97.model.Difficulty
import com.retro.sudoku97.model.SavedGame

/**
 * Tiny local persistence layer. SharedPreferences is enough for a single board,
 * three best times and one display preference. No network or third-party storage.
 */
class GameRepository(context: Context) {

    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSavedGame(): SavedGame? {
        if (!preferences.getBoolean(KEY_ACTIVE, false)) return null
        val difficulty = Difficulty.fromStoredName(preferences.getString(KEY_DIFFICULTY, null))
            ?: return null
        val puzzle = decodeBoard(preferences.getString(KEY_PUZZLE, null)) ?: return null
        val solution = decodeBoard(preferences.getString(KEY_SOLUTION, null)) ?: return null
        val values = decodeBoard(preferences.getString(KEY_VALUES, null)) ?: return null
        val candidates = decodeCandidates(preferences.getString(KEY_CANDIDATES, null)) ?: return null
        if (solution.any { it == 0 }) return null
        if (puzzle.indices.any { puzzle[it] != 0 && puzzle[it] != solution[it] }) return null

        return SavedGame(
            difficulty = difficulty,
            puzzle = puzzle,
            solution = solution,
            values = values,
            candidates = candidates,
            elapsedSeconds = preferences.getLong(KEY_ELAPSED, 0L).coerceAtLeast(0L),
            pencilMode = preferences.getBoolean(KEY_PENCIL, false)
        )
    }

    fun saveSavedGame(game: SavedGame, synchronous: Boolean = false) {
        val editor = preferences.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_DIFFICULTY, game.difficulty.name)
            .putString(KEY_PUZZLE, encodeBoard(game.puzzle))
            .putString(KEY_SOLUTION, encodeBoard(game.solution))
            .putString(KEY_VALUES, encodeBoard(game.values))
            .putString(KEY_CANDIDATES, encodeCandidates(game.candidates))
            .putLong(KEY_ELAPSED, game.elapsedSeconds)
            .putBoolean(KEY_PENCIL, game.pencilMode)
        if (synchronous) editor.commit() else editor.apply()
    }

    fun clearSavedGame() {
        preferences.edit()
            .putBoolean(KEY_ACTIVE, false)
            .remove(KEY_DIFFICULTY)
            .remove(KEY_PUZZLE)
            .remove(KEY_SOLUTION)
            .remove(KEY_VALUES)
            .remove(KEY_CANDIDATES)
            .remove(KEY_ELAPSED)
            .remove(KEY_PENCIL)
            .apply()
    }

    fun loadBestTimes(): Map<Difficulty, Long> {
        val result = linkedMapOf<Difficulty, Long>()
        Difficulty.entries.forEach { difficulty ->
            val value = preferences.getLong(KEY_BEST_PREFIX + difficulty.name, 0L)
            if (value > 0L) result[difficulty] = value
        }
        return result
    }

    fun saveBestTime(difficulty: Difficulty, seconds: Long) {
        if (seconds <= 0L) return
        val key = KEY_BEST_PREFIX + difficulty.name
        val previous = preferences.getLong(key, 0L)
        if (previous == 0L || seconds < previous) {
            preferences.edit().putLong(key, seconds).apply()
        }
    }

    fun loadHighlightConflicts(): Boolean =
        preferences.getBoolean(KEY_HIGHLIGHT_CONFLICTS, true)

    fun saveHighlightConflicts(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HIGHLIGHT_CONFLICTS, enabled).apply()
    }

    private fun encodeBoard(board: List<Int>): String {
        require(board.size == CELL_COUNT)
        return board.joinToString(separator = "") { digit ->
            require(digit in 0..9)
            digit.toString()
        }
    }

    private fun decodeBoard(encoded: String?): List<Int>? {
        if (encoded == null || encoded.length != CELL_COUNT) return null
        if (encoded.any { it !in '0'..'9' }) return null
        return encoded.map { it - '0' }
    }

    private fun encodeCandidates(candidates: List<Int>): String {
        require(candidates.size == CELL_COUNT)
        return candidates.joinToString(separator = ",") { mask ->
            require(mask in 0..511)
            mask.toString(16)
        }
    }

    private fun decodeCandidates(encoded: String?): List<Int>? {
        if (encoded == null) return null
        val parts = encoded.split(',')
        if (parts.size != CELL_COUNT) return null
        return parts.map { part ->
            val mask = part.toIntOrNull(16) ?: return null
            if (mask !in 0..511) return null
            mask
        }
    }

    private companion object {
        const val PREFS_NAME = "sudoku97_preferences"
        const val KEY_ACTIVE = "game_active"
        const val KEY_DIFFICULTY = "game_difficulty"
        const val KEY_PUZZLE = "game_puzzle"
        const val KEY_SOLUTION = "game_solution"
        const val KEY_VALUES = "game_values"
        const val KEY_CANDIDATES = "game_candidates"
        const val KEY_ELAPSED = "game_elapsed"
        const val KEY_PENCIL = "game_pencil"
        const val KEY_BEST_PREFIX = "best_"
        const val KEY_HIGHLIGHT_CONFLICTS = "highlight_conflicts"
    }
}

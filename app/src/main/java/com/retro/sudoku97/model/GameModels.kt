package com.retro.sudoku97.model

/**
 * Difficulty is deliberately expressed as an inclusive clue range. The generator
 * stops removing clues at a target inside that range and verifies uniqueness after
 * every removal.
 */
enum class Difficulty(
    val displayName: String,
    val clueRange: IntRange,
    val tagline: String
) {
    EASY("Easy", 36..42, "A gentle warm-up"),
    MEDIUM("Medium", 30..35, "The classic challenge"),
    HARD("Hard", 24..29, "For seasoned solvers");

    val clueSummary: String
        get() = "${clueRange.first}-${clueRange.last} clues"

    companion object {
        fun fromStoredName(name: String?): Difficulty? =
            entries.firstOrNull { it.name == name }
    }
}

enum class AppScreen {
    START,
    PLAYING,
    WON
}

/**
 * Everything the Compose UI needs. Board arrays are immutable lists so StateFlow
 * can safely compare old and new state.
 */
data class SudokuUiState(
    val screen: AppScreen = AppScreen.START,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val puzzle: List<Int> = emptyList(),
    val solution: List<Int> = emptyList(),
    val values: List<Int> = emptyList(),
    val candidates: List<Int> = List(CELL_COUNT) { 0 },
    val selectedCell: Int? = null,
    val pencilMode: Boolean = false,
    val highlightConflicts: Boolean = true,
    val conflicts: Set<Int> = emptySet(),
    val hintCells: Set<Int> = emptySet(),
    val hintMessage: String? = null,
    val elapsedSeconds: Long = 0L,
    val bestTimes: Map<Difficulty, Long> = emptyMap(),
    val isGenerating: Boolean = false,
    val isNewBest: Boolean = false,
    val canResume: Boolean = false
) {
    val clueCount: Int
        get() = puzzle.count { it != 0 }

    val hasBoard: Boolean
        get() = puzzle.size == CELL_COUNT

    val isPencilAvailable: Boolean
        get() = screen == AppScreen.PLAYING && hasBoard
}

data class SavedGame(
    val difficulty: Difficulty,
    val puzzle: List<Int>,
    val solution: List<Int>,
    val values: List<Int>,
    val candidates: List<Int>,
    val elapsedSeconds: Long,
    val pencilMode: Boolean
)

const val CELL_COUNT = 81
const val BOARD_SIZE = 9
const val FULL_CANDIDATE_MASK = 0x1FF

fun formatElapsed(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val secs = safeSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%02d:%02d".format(minutes, secs)
    }
}

fun formatBestTime(seconds: Long?): String =
    if (seconds == null || seconds <= 0L) "--:--" else formatElapsed(seconds)

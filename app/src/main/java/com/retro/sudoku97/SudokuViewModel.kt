package com.retro.sudoku97

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.retro.sudoku97.data.GameRepository
import com.retro.sudoku97.logic.Hint
import com.retro.sudoku97.logic.HintEngine
import com.retro.sudoku97.logic.SudokuGenerator
import com.retro.sudoku97.logic.SudokuRules
import com.retro.sudoku97.model.AppScreen
import com.retro.sudoku97.model.CELL_COUNT
import com.retro.sudoku97.model.Difficulty
import com.retro.sudoku97.model.SavedGame
import com.retro.sudoku97.model.SudokuUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SudokuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var appInForeground = true

    init {
        var bestTimes = repository.loadBestTimes()
        val highlightConflicts = repository.loadHighlightConflicts()
        val savedGame = repository.loadSavedGame()
        val resumableGame = savedGame?.takeUnless {
            SudokuRules.isSolved(it.values, it.solution)
        }

        // Defensive recovery: if the process died after the last cell was filled
        // but before the win handler cleared the saved game, record the time and
        // start cleanly instead of reopening an already-finished board.
        if (savedGame != null && resumableGame == null) {
            repository.saveBestTime(
                savedGame.difficulty,
                savedGame.elapsedSeconds.coerceAtLeast(1L)
            )
            bestTimes = repository.loadBestTimes()
            repository.clearSavedGame()
        }

        if (resumableGame != null) {
            val conflicts = SudokuRules.findConflicts(resumableGame.values)
            _uiState.value = SudokuUiState(
                screen = AppScreen.PLAYING,
                difficulty = resumableGame.difficulty,
                puzzle = resumableGame.puzzle,
                solution = resumableGame.solution,
                values = resumableGame.values,
                candidates = resumableGame.candidates,
                selectedCell = null,
                pencilMode = resumableGame.pencilMode,
                highlightConflicts = highlightConflicts,
                conflicts = conflicts,
                elapsedSeconds = resumableGame.elapsedSeconds,
                bestTimes = bestTimes,
                canResume = true
            )
            startTimer()
        } else {
            _uiState.value = SudokuUiState(
                screen = AppScreen.START,
                difficulty = Difficulty.MEDIUM,
                highlightConflicts = highlightConflicts,
                bestTimes = bestTimes,
                canResume = false
            )
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        if (_uiState.value.isGenerating) return
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                isGenerating = true,
                hintMessage = null,
                hintCells = emptySet(),
                isNewBest = false
            )
        }

        viewModelScope.launch {
            try {
                val generated = withContext(Dispatchers.Default) {
                    SudokuGenerator.generate(difficulty)
                }
                val values = generated.puzzle.toList()
                val newState = _uiState.value.copy(
                    screen = AppScreen.PLAYING,
                    difficulty = difficulty,
                    puzzle = generated.puzzle.toList(),
                    solution = generated.solution.toList(),
                    values = values,
                    candidates = List(CELL_COUNT) { 0 },
                    selectedCell = null,
                    pencilMode = false,
                    conflicts = emptySet(),
                    hintCells = emptySet(),
                    hintMessage = "New ${difficulty.displayName.lowercase()} puzzle. Good luck!",
                    elapsedSeconds = 0L,
                    isGenerating = false,
                    isNewBest = false,
                    canResume = true
                )
                _uiState.value = newState
                persist(newState)
                if (appInForeground) startTimer()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        hintMessage = error.message
                            ?: "Could not generate a puzzle. Please try again."
                    )
                }
                if (appInForeground && _uiState.value.screen == AppScreen.PLAYING) {
                    startTimer()
                }
            }
        }
    }

    fun resumeFromMenu() {
        val state = _uiState.value
        if (state.screen != AppScreen.START || !state.hasBoard || !state.canResume) return
        val resumed = state.copy(
            screen = AppScreen.PLAYING,
            selectedCell = null,
            hintCells = emptySet(),
            hintMessage = "Welcome back. Your timer is running again."
        )
        _uiState.value = resumed
        persist(resumed)
        if (appInForeground) startTimer()
    }

    fun returnToMenu() {
        val state = _uiState.value
        val canResume = state.screen == AppScreen.PLAYING && state.hasBoard
        timerJob?.cancel()
        timerJob = null
        if (canResume) persist(state) else repository.clearSavedGame()
        _uiState.value = state.copy(
            screen = AppScreen.START,
            selectedCell = null,
            hintCells = emptySet(),
            hintMessage = null,
            isNewBest = false,
            canResume = canResume
        )
    }

    fun restartGame() {
        val state = _uiState.value
        if (!state.hasBoard) return
        timerJob?.cancel()
        timerJob = null
        val reset = state.copy(
            screen = AppScreen.PLAYING,
            values = state.puzzle,
            candidates = List(CELL_COUNT) { 0 },
            selectedCell = null,
            conflicts = emptySet(),
            hintCells = emptySet(),
            hintMessage = "Puzzle restarted. The clock is back to zero.",
            elapsedSeconds = 0L,
            isNewBest = false,
            canResume = true
        )
        _uiState.value = reset
        persist(reset)
        if (appInForeground) startTimer()
    }

    fun selectCell(index: Int) {
        val state = _uiState.value
        if (state.screen != AppScreen.PLAYING || index !in 0 until CELL_COUNT) return
        _uiState.value = state.copy(selectedCell = index)
    }

    fun enterNumber(digit: Int) {
        if (digit !in 1..9) return
        val state = _uiState.value
        if (state.screen != AppScreen.PLAYING || !state.hasBoard) return
        val cell = state.selectedCell
        if (cell == null) {
            _uiState.value = state.copy(hintMessage = "Select a cell first, then choose a number.")
            return
        }
        if (state.puzzle[cell] != 0) {
            _uiState.value = state.copy(hintMessage = "That number is a fixed clue and cannot be changed.")
            return
        }

        val values = state.values.toMutableList()
        val candidates = state.candidates.toMutableList()
        val bit = 1 shl (digit - 1)

        if (state.pencilMode) {
            if (values[cell] != 0) {
                _uiState.value = state.copy(hintMessage = "Erase the value before adding pencil notes.")
                return
            }
            candidates[cell] = candidates[cell] xor bit
            val next = state.copy(
                candidates = candidates,
                hintCells = emptySet(),
                hintMessage = null,
                isNewBest = false
            )
            _uiState.value = next
            persist(next)
            return
        }

        values[cell] = digit
        candidates[cell] = 0
        for (peer in SudokuRules.PEERS[cell]) {
            if (values[peer] == 0) {
                candidates[peer] = candidates[peer] and bit.inv()
            }
        }

        var next = state.copy(
            values = values,
            candidates = candidates,
            conflicts = SudokuRules.findConflicts(values),
            hintCells = emptySet(),
            hintMessage = null,
            isNewBest = false
        )

        if (SudokuRules.isSolved(values, state.solution)) {
            next = finishWin(next)
        } else if (SudokuRules.isBoardFull(values)) {
            next = next.copy(
                hintMessage = "The board is full, but it is not correct. Check the red cells."
            )
        }

        _uiState.value = next
        persist(next)
    }

    fun eraseSelectedCell() {
        val state = _uiState.value
        if (state.screen != AppScreen.PLAYING || !state.hasBoard) return
        val cell = state.selectedCell ?: run {
            _uiState.value = state.copy(hintMessage = "Select a cell to erase.")
            return
        }
        if (state.puzzle[cell] != 0) {
            _uiState.value = state.copy(hintMessage = "Fixed clues cannot be erased.")
            return
        }
        if (state.values[cell] == 0 && state.candidates[cell] == 0) return

        val values = state.values.toMutableList()
        val candidates = state.candidates.toMutableList()
        values[cell] = 0
        candidates[cell] = 0

        val next = state.copy(
            values = values,
            candidates = candidates,
            conflicts = SudokuRules.findConflicts(values),
            hintCells = emptySet(),
            hintMessage = null,
            isNewBest = false
        )
        _uiState.value = next
        persist(next)
    }

    fun togglePencilMode() {
        val state = _uiState.value
        if (state.screen != AppScreen.PLAYING) return
        _uiState.value = state.copy(
            pencilMode = !state.pencilMode,
            hintMessage = if (state.pencilMode) {
                "Pencil mode off. Numbers will be placed."
            } else {
                "Pencil mode on. Numbers toggle small candidate notes."
            }
        )
    }

    fun toggleConflictHighlighting() {
        val enabled = !_uiState.value.highlightConflicts
        repository.saveHighlightConflicts(enabled)
        _uiState.value = _uiState.value.copy(highlightConflicts = enabled)
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.screen != AppScreen.PLAYING || !state.hasBoard) return
        if (state.conflicts.isNotEmpty()) {
            _uiState.value = state.copy(
                hintMessage = "Fix the red duplicate numbers before asking for a hint.",
                hintCells = state.conflicts
            )
            return
        }

        val hint = HintEngine.findHint(
            values = state.values,
            solution = state.solution,
            selectedCell = state.selectedCell,
            userCandidates = state.candidates
        )
        if (hint == null) {
            _uiState.value = state.copy(hintMessage = "There are no empty cells left.")
            return
        }

        when (hint) {
            is Hint.NakedSingle -> {
                val next = placeHintValue(state, hint.cell, hint.value, hint.message, hint.highlightCells)
                _uiState.value = next
                persist(next)
            }

            is Hint.HiddenSingle -> {
                val next = placeHintValue(state, hint.cell, hint.value, hint.message, hint.highlightCells)
                _uiState.value = next
                persist(next)
            }

            is Hint.Reveal -> {
                val next = placeHintValue(state, hint.cell, hint.value, hint.message, hint.highlightCells)
                _uiState.value = next
                persist(next)
            }

            is Hint.NakedPair -> {
                val candidates = state.candidates.toMutableList()
                hint.eliminations.forEach { (cell, bits) ->
                    // If the player has not written notes in an affected cell yet,
                    // seed the cell with its logical candidates before applying the
                    // pair elimination. This makes repeated Hint presses advance.
                    val base = if (candidates[cell] == 0) {
                        SudokuRules.candidateMask(state.values, cell)
                    } else {
                        candidates[cell]
                    }
                    candidates[cell] = base and bits.inv()
                }
                val next = state.copy(
                    candidates = candidates,
                    selectedCell = hint.pairCells.firstOrNull(),
                    hintCells = hint.highlightCells,
                    hintMessage = hint.message,
                    isNewBest = false
                )
                _uiState.value = next
                persist(next)
            }
        }
    }

    private fun placeHintValue(
        state: SudokuUiState,
        cell: Int,
        value: Int,
        message: String,
        highlight: Set<Int>
    ): SudokuUiState {
        val values = state.values.toMutableList()
        val candidates = state.candidates.toMutableList()
        val bit = 1 shl (value - 1)
        values[cell] = value
        candidates[cell] = 0
        for (peer in SudokuRules.PEERS[cell]) {
            if (values[peer] == 0) {
                candidates[peer] = candidates[peer] and bit.inv()
            }
        }

        var next = state.copy(
            values = values,
            candidates = candidates,
            selectedCell = cell,
            conflicts = SudokuRules.findConflicts(values),
            hintCells = highlight,
            hintMessage = message,
            isNewBest = false
        )
        if (SudokuRules.isSolved(values, state.solution)) {
            next = finishWin(next)
        }
        return next
    }

    private fun finishWin(state: SudokuUiState): SudokuUiState {
        timerJob?.cancel()
        timerJob = null
        val finishedSeconds = state.elapsedSeconds.coerceAtLeast(1L)
        val previousBest = state.bestTimes[state.difficulty]
        val isNewBest = previousBest == null || finishedSeconds < previousBest
        val bestTimes = if (isNewBest) {
            repository.saveBestTime(state.difficulty, finishedSeconds)
            state.bestTimes + (state.difficulty to finishedSeconds)
        } else {
            state.bestTimes
        }
        repository.clearSavedGame()
        return state.copy(
            screen = AppScreen.WON,
            elapsedSeconds = finishedSeconds,
            bestTimes = bestTimes,
            isNewBest = isNewBest,
            canResume = false,
            selectedCell = null,
            hintMessage = "Puzzle solved!",
            hintCells = emptySet()
        )
    }

    fun pauseGame() {
        appInForeground = false
        timerJob?.cancel()
        timerJob = null
        val state = _uiState.value
        if (state.screen == AppScreen.PLAYING && state.hasBoard) {
            persist(state, synchronous = true)
        }
    }

    fun resumeGame() {
        appInForeground = true
        if (_uiState.value.screen == AppScreen.PLAYING && _uiState.value.hasBoard) {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val state = _uiState.value
                if (state.screen != AppScreen.PLAYING) break
                val next = state.copy(elapsedSeconds = state.elapsedSeconds + 1L)
                _uiState.value = next
                if (next.elapsedSeconds % 15L == 0L) {
                    persist(next)
                }
            }
        }
    }

    private fun persist(state: SudokuUiState, synchronous: Boolean = false) {
        if (state.screen == AppScreen.PLAYING && state.hasBoard) {
            repository.saveSavedGame(
                SavedGame(
                    difficulty = state.difficulty,
                    puzzle = state.puzzle,
                    solution = state.solution,
                    values = state.values,
                    candidates = state.candidates,
                    elapsedSeconds = state.elapsedSeconds,
                    pencilMode = state.pencilMode
                ),
                synchronous = synchronous
            )
        } else if (state.screen == AppScreen.WON) {
            repository.clearSavedGame()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        timerJob = null
        val state = _uiState.value
        if (state.screen == AppScreen.PLAYING && state.hasBoard) {
            persist(state, synchronous = true)
        }
        super.onCleared()
    }
}

package com.retro.sudoku97.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.retro.sudoku97.model.AppScreen
import com.retro.sudoku97.model.Difficulty
import com.retro.sudoku97.model.SudokuUiState
import com.retro.sudoku97.ui.screens.GameScreen
import com.retro.sudoku97.ui.screens.StartScreen

/**
 * Preview-only fixtures. This file lives in the debug source set so none of it
 * ships in a release build. It exists so the UI can be inspected with
 * `android preview render` without an emulator.
 */

private val SamplePuzzle = listOf(
    5, 3, 0, 0, 7, 0, 0, 0, 0,
    6, 0, 0, 1, 9, 5, 0, 0, 0,
    0, 9, 8, 0, 0, 0, 0, 6, 0,
    8, 0, 0, 0, 6, 0, 0, 0, 3,
    4, 0, 0, 8, 0, 3, 0, 0, 1,
    7, 0, 0, 0, 2, 0, 0, 0, 6,
    0, 6, 0, 0, 0, 0, 2, 8, 0,
    0, 0, 0, 4, 1, 9, 0, 0, 5,
    0, 0, 0, 0, 8, 0, 0, 7, 9
)

private val SampleSolution = listOf(
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

private fun sampleValues(): List<Int> = SamplePuzzle.toMutableList().apply {
    this[2] = 4
    this[3] = 6
    this[5] = 8
    this[10] = 7
    this[11] = 2
    this[15] = 3
    this[16] = 4
    this[18] = 1
    this[21] = 3
    this[22] = 4
    this[23] = 2
    this[24] = 5
    this[26] = 7
    this[28] = 6 // deliberately conflicts with R4C5 and R7C2
}

private fun sampleCandidates(): List<Int> = List(81) { 0 }.toMutableList().apply {
    this[6] = 0b000000011 // 1, 2
    this[7] = 0b000000011 // 1, 2
    this[8] = 0b100000011 // 1, 2, 9
    this[29] = 0b000010010 // 2, 5
    this[30] = 0b000010010 // 2, 5
}

private fun startState(canResume: Boolean) = SudokuUiState(
    screen = AppScreen.START,
    difficulty = Difficulty.MEDIUM,
    bestTimes = mapOf(
        Difficulty.EASY to 312L,
        Difficulty.MEDIUM to 745L,
        Difficulty.HARD to 1543L
    ),
    elapsedSeconds = 187L,
    canResume = canResume
)

private fun playingState() = SudokuUiState(
    screen = AppScreen.PLAYING,
    difficulty = Difficulty.MEDIUM,
    puzzle = SamplePuzzle,
    solution = SampleSolution,
    values = sampleValues(),
    candidates = sampleCandidates(),
    selectedCell = 6,
    pencilMode = false,
    highlightConflicts = true,
    conflicts = setOf(28, 31, 55),
    hintCells = setOf(6, 7),
    hintMessage = "Naked pair in row 1: 1 and 2 must go in R1C7 and R1C8.",
    elapsedSeconds = 187L,
    bestTimes = mapOf(Difficulty.MEDIUM to 745L)
)

@Preview(name = "Start screen", widthDp = 420, heightDp = 900, showBackground = true)
@Composable
private fun StartScreenPreview() {
    Sudoku97Theme {
        RetroDesktop {
            StartScreen(
                state = startState(canResume = false),
                onStart = {},
                onResume = {},
                onBestTimes = {},
                onHowToPlay = {},
                onAbout = {},
                onMinimize = {},
                onMaximize = {},
                onClose = {}
            )
        }
    }
}

@Preview(name = "Start screen - saved game", widthDp = 420, heightDp = 900, showBackground = true)
@Composable
private fun StartScreenResumePreview() {
    Sudoku97Theme {
        RetroDesktop {
            StartScreen(
                state = startState(canResume = true),
                onStart = {},
                onResume = {},
                onBestTimes = {},
                onHowToPlay = {},
                onAbout = {},
                onMinimize = {},
                onMaximize = {},
                onClose = {}
            )
        }
    }
}

@Preview(name = "Game screen", widthDp = 420, heightDp = 900, showBackground = true)
@Composable
private fun GameScreenPreview() {
    Sudoku97Theme {
        RetroDesktop {
            GameScreen(
                state = playingState(),
                onSelectCell = {},
                onNumber = {},
                onErase = {},
                onTogglePencil = {},
                onHint = {},
                onRestart = {},
                onMenu = {},
                onToggleConflicts = {},
                onNewGameRequest = {},
                onBestTimesRequest = {},
                onHowToPlayRequest = {},
                onAboutRequest = {},
                onExitRequest = {},
                onMinimize = {},
                onMaximize = {}
            )
        }
    }
}

@Preview(name = "Game screen - landscape", widthDp = 900, heightDp = 420, showBackground = true)
@Composable
private fun GameScreenLandscapePreview() {
    Sudoku97Theme {
        RetroDesktop {
            GameScreen(
                state = playingState(),
                onSelectCell = {},
                onNumber = {},
                onErase = {},
                onTogglePencil = {},
                onHint = {},
                onRestart = {},
                onMenu = {},
                onToggleConflicts = {},
                onNewGameRequest = {},
                onBestTimesRequest = {},
                onHowToPlayRequest = {},
                onAboutRequest = {},
                onExitRequest = {},
                onMinimize = {},
                onMaximize = {}
            )
        }
    }
}

@Preview(name = "Game screen - pencil mode", widthDp = 420, heightDp = 900, showBackground = true)
@Composable
private fun GameScreenPencilPreview() {
    Sudoku97Theme {
        RetroDesktop {
            GameScreen(
                state = playingState().copy(pencilMode = true, selectedCell = 29),
                onSelectCell = {},
                onNumber = {},
                onErase = {},
                onTogglePencil = {},
                onHint = {},
                onRestart = {},
                onMenu = {},
                onToggleConflicts = {},
                onNewGameRequest = {},
                onBestTimesRequest = {},
                onHowToPlayRequest = {},
                onAboutRequest = {},
                onExitRequest = {},
                onMinimize = {},
                onMaximize = {}
            )
        }
    }
}

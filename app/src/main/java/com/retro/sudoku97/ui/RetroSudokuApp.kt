package com.retro.sudoku97.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.retro.sudoku97.SudokuViewModel
import com.retro.sudoku97.model.AppScreen
import com.retro.sudoku97.ui.screens.AboutDialog
import com.retro.sudoku97.ui.screens.BestTimesDialog
import com.retro.sudoku97.ui.screens.ExitDialog
import com.retro.sudoku97.ui.screens.GameScreen
import com.retro.sudoku97.ui.screens.GeneratingDialog
import com.retro.sudoku97.ui.screens.HowToPlayDialog
import com.retro.sudoku97.ui.screens.NewGameDialog
import com.retro.sudoku97.ui.screens.StartScreen
import com.retro.sudoku97.ui.screens.WinDialog

private enum class AppDialog {
    NEW_GAME,
    BEST_TIMES,
    HOW_TO_PLAY,
    ABOUT,
    EXIT
}

@Composable
fun RetroSudokuApp(
    viewModel: SudokuViewModel,
    onExit: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeDialog by remember { mutableStateOf<AppDialog?>(null) }

    RetroDesktop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(3.dp)
        ) {
            when (state.screen) {
                AppScreen.START -> StartScreen(
                    state = state,
                    onStart = { difficulty ->
                        activeDialog = null
                        viewModel.startNewGame(difficulty)
                    },
                    onResume = viewModel::resumeFromMenu,
                    onBestTimes = { activeDialog = AppDialog.BEST_TIMES },
                    onHowToPlay = { activeDialog = AppDialog.HOW_TO_PLAY },
                    onAbout = { activeDialog = AppDialog.ABOUT },
                    onMinimize = onMinimize,
                    onMaximize = onMaximize,
                    onClose = { activeDialog = AppDialog.EXIT }
                )

                AppScreen.PLAYING,
                AppScreen.WON -> GameScreen(
                    state = state,
                    onSelectCell = viewModel::selectCell,
                    onNumber = viewModel::enterNumber,
                    onErase = viewModel::eraseSelectedCell,
                    onTogglePencil = viewModel::togglePencilMode,
                    onHint = viewModel::requestHint,
                    onRestart = viewModel::restartGame,
                    onMenu = viewModel::returnToMenu,
                    onToggleConflicts = viewModel::toggleConflictHighlighting,
                    onNewGameRequest = { activeDialog = AppDialog.NEW_GAME },
                    onBestTimesRequest = { activeDialog = AppDialog.BEST_TIMES },
                    onHowToPlayRequest = { activeDialog = AppDialog.HOW_TO_PLAY },
                    onAboutRequest = { activeDialog = AppDialog.ABOUT },
                    onExitRequest = { activeDialog = AppDialog.EXIT },
                    onMinimize = onMinimize,
                    onMaximize = onMaximize
                )
            }

            if (state.screen == AppScreen.WON &&
                activeDialog == null &&
                !state.isGenerating
            ) {
                WinDialog(
                    state = state,
                    onNewGame = { activeDialog = AppDialog.NEW_GAME },
                    onMenu = {
                        activeDialog = null
                        viewModel.returnToMenu()
                    }
                )
            }

            if (state.isGenerating) {
                GeneratingDialog()
            }

            when (activeDialog) {
                AppDialog.NEW_GAME -> NewGameDialog(
                    initialDifficulty = state.difficulty,
                    hasActiveGame = state.screen == AppScreen.PLAYING && state.hasBoard,
                    onDismiss = { activeDialog = null },
                    onStart = { difficulty ->
                        activeDialog = null
                        viewModel.startNewGame(difficulty)
                    }
                )

                AppDialog.BEST_TIMES -> BestTimesDialog(
                    bestTimes = state.bestTimes,
                    onDismiss = { activeDialog = null }
                )

                AppDialog.HOW_TO_PLAY -> HowToPlayDialog(
                    onDismiss = { activeDialog = null }
                )

                AppDialog.ABOUT -> AboutDialog(
                    onDismiss = { activeDialog = null }
                )

                AppDialog.EXIT -> ExitDialog(
                    onDismiss = { activeDialog = null },
                    onConfirm = {
                        activeDialog = null
                        viewModel.pauseGame()
                        onExit()
                    }
                )

                null -> Unit
            }
        }
    }
}

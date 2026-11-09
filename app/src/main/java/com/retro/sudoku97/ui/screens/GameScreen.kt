package com.retro.sudoku97.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.sudoku97.model.SudokuUiState
import com.retro.sudoku97.model.formatBestTime
import com.retro.sudoku97.model.formatElapsed
import com.retro.sudoku97.ui.Win95
import com.retro.sudoku97.ui.components.RetroButton
import com.retro.sudoku97.ui.components.RetroHorizontalRule
import com.retro.sudoku97.ui.components.RetroMenuBarItem
import com.retro.sudoku97.ui.components.RetroMenuItem
import com.retro.sudoku97.ui.components.RetroMenuSeparator
import com.retro.sudoku97.ui.components.RetroStatPanel
import com.retro.sudoku97.ui.components.RetroWindow
import com.retro.sudoku97.ui.components.SudokuBoardView
import com.retro.sudoku97.ui.components.win95Sunken

@Composable
fun GameScreen(
    state: SudokuUiState,
    onSelectCell: (Int) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onTogglePencil: () -> Unit,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onToggleConflicts: () -> Unit,
    onNewGameRequest: () -> Unit,
    onBestTimesRequest: () -> Unit,
    onHowToPlayRequest: () -> Unit,
    onAboutRequest: () -> Unit,
    onExitRequest: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameMenuOpen by remember { mutableStateOf(false) }
    var optionsMenuOpen by remember { mutableStateOf(false) }
    var helpMenuOpen by remember { mutableStateOf(false) }

    RetroWindow(
        title = "Sudoku 97 - ${state.difficulty.displayName}",
        modifier = modifier.fillMaxSize(),
        fillSize = true,
        onMinimize = onMinimize,
        onMaximize = onMaximize,
        onClose = onExitRequest
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .background(Win95.Gray)
                    .padding(horizontal = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetroMenuBarItem(
                    label = "Game",
                    expanded = gameMenuOpen,
                    onExpandedChange = { gameMenuOpen = it }
                ) {
                    RetroMenuItem(
                        label = "New Game...",
                        onClick = {
                            gameMenuOpen = false
                            onNewGameRequest()
                        }
                    )
                    RetroMenuItem(
                        label = "Restart Puzzle",
                        onClick = {
                            gameMenuOpen = false
                            onRestart()
                        }
                    )
                    RetroMenuItem(
                        label = "Best Times...",
                        onClick = {
                            gameMenuOpen = false
                            onBestTimesRequest()
                        }
                    )
                    RetroMenuSeparator()
                    RetroMenuItem(
                        label = "Main Menu",
                        onClick = {
                            gameMenuOpen = false
                            onMenu()
                        }
                    )
                    RetroMenuItem(
                        label = "Exit",
                        onClick = {
                            gameMenuOpen = false
                            onExitRequest()
                        }
                    )
                }
                RetroMenuBarItem(
                    label = "Options",
                    expanded = optionsMenuOpen,
                    onExpandedChange = { optionsMenuOpen = it }
                ) {
                    RetroMenuItem(
                        label = "Pencil Mode",
                        checked = state.pencilMode,
                        onClick = {
                            optionsMenuOpen = false
                            onTogglePencil()
                        }
                    )
                    RetroMenuItem(
                        label = "Highlight Conflicts",
                        checked = state.highlightConflicts,
                        onClick = {
                            optionsMenuOpen = false
                            onToggleConflicts()
                        }
                    )
                }
                RetroMenuBarItem(
                    label = "Help",
                    expanded = helpMenuOpen,
                    onExpandedChange = { helpMenuOpen = it }
                ) {
                    RetroMenuItem(
                        label = "How to Play...",
                        onClick = {
                            helpMenuOpen = false
                            onHowToPlayRequest()
                        }
                    )
                    RetroMenuItem(
                        label = "About Sudoku 97...",
                        onClick = {
                            helpMenuOpen = false
                            onAboutRequest()
                        }
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "OFFLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Win95.DarkGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
                Spacer(Modifier.width(5.dp))
            }
            RetroHorizontalRule()

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val wide = maxWidth > maxHeight * 1.05f
                if (wide) {
                    WideGameLayout(
                        state = state,
                        onSelectCell = onSelectCell,
                        onNumber = onNumber,
                        onErase = onErase,
                        onTogglePencil = onTogglePencil,
                        onHint = onHint
                    )
                } else {
                    PortraitGameLayout(
                        state = state,
                        onSelectCell = onSelectCell,
                        onNumber = onNumber,
                        onErase = onErase,
                        onTogglePencil = onTogglePencil,
                        onHint = onHint
                    )
                }
            }
        }
    }
}

@Composable
private fun PortraitGameLayout(
    state: SudokuUiState,
    onSelectCell: (Int) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onTogglePencil: () -> Unit,
    onHint: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameStatusRow(state)
        Spacer(Modifier.height(6.dp))
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            GameBoard(
                state = state,
                onSelectCell = onSelectCell,
                modifier = Modifier.size(boardSize)
            )
        }
        Spacer(Modifier.height(6.dp))
        HintPanel(state)
        Spacer(Modifier.height(7.dp))
        GameControls(
            state = state,
            onNumber = onNumber,
            onErase = onErase,
            onTogglePencil = onTogglePencil,
            onHint = onHint
        )
    }
}

@Composable
private fun WideGameLayout(
    state: SudokuUiState,
    onSelectCell: (Int) -> Unit,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onTogglePencil: () -> Unit,
    onHint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val boardSize = minOf(maxWidth, maxHeight)
            GameBoard(
                state = state,
                onSelectCell = onSelectCell,
                modifier = Modifier.size(boardSize)
            )
        }
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
        ) {
            GameStatusRow(state)
            Spacer(Modifier.height(6.dp))
            HintPanel(state)
            Spacer(Modifier.weight(1f))
            GameControls(
                state = state,
                onNumber = onNumber,
                onErase = onErase,
                onTogglePencil = onTogglePencil,
                onHint = onHint
            )
        }
    }
}

@Composable
private fun GameBoard(
    state: SudokuUiState,
    onSelectCell: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SudokuBoardView(
        values = state.values,
        puzzle = state.puzzle,
        candidates = state.candidates,
        selectedCell = state.selectedCell,
        conflicts = state.conflicts,
        highlightConflicts = state.highlightConflicts,
        hintCells = state.hintCells,
        onCellClick = onSelectCell,
        modifier = modifier
    )
}

@Composable
private fun GameStatusRow(state: SudokuUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        RetroStatPanel(
            label = "Difficulty",
            value = state.difficulty.displayName,
            modifier = Modifier.weight(1f)
        )
        RetroStatPanel(
            label = "Time",
            value = formatElapsed(state.elapsedSeconds),
            modifier = Modifier.weight(1f)
        )
        RetroStatPanel(
            label = "Best",
            value = formatBestTime(state.bestTimes[state.difficulty]),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HintPanel(state: SudokuUiState) {
    val hasConflict = state.conflicts.isNotEmpty() && state.highlightConflicts
    val message = state.hintMessage ?: when {
        hasConflict -> "Invalid move: duplicate numbers are highlighted in red."
        state.pencilMode -> "Pencil mode: tap 1-9 to toggle small candidate notes."
        else -> "Select a cell, then use the number pad. Hint gives a logical nudge."
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp)
            .background(Win95.Gray)
            .win95Sunken(2.dp)
            .padding(horizontal = 7.dp, vertical = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (hasConflict) Win95.Red else Win95.Black,
            maxLines = 2
        )
    }
}

@Composable
private fun GameControls(
    state: SudokuUiState,
    onNumber: (Int) -> Unit,
    onErase: () -> Unit,
    onTogglePencil: () -> Unit,
    onHint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        NumberPad(
            modifier = Modifier.weight(1f),
            enabled = state.screen == com.retro.sudoku97.model.AppScreen.PLAYING,
            onNumber = onNumber
        )
        Column(
            modifier = Modifier.width(116.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RetroButton(
                text = "Erase",
                onClick = onErase,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 44.dp,
                enabled = state.screen == com.retro.sudoku97.model.AppScreen.PLAYING
            )
            RetroButton(
                text = if (state.pencilMode) "Pencil: ON" else "Pencil",
                onClick = onTogglePencil,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 44.dp,
                active = state.pencilMode,
                enabled = state.screen == com.retro.sudoku97.model.AppScreen.PLAYING,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 4.dp,
                    vertical = 5.dp
                )
            )
            RetroButton(
                text = "Hint",
                onClick = onHint,
                modifier = Modifier.fillMaxWidth(),
                minHeight = 44.dp,
                enabled = state.screen == com.retro.sudoku97.model.AppScreen.PLAYING
            )
        }
    }
}

@Composable
private fun NumberPad(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onNumber: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (column in 0..2) {
                    val number = row * 3 + column + 1
                    RetroButton(
                        text = number.toString(),
                        onClick = { onNumber(number) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        minHeight = 44.dp,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

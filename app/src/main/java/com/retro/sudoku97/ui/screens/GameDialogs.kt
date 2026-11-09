package com.retro.sudoku97.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retro.sudoku97.model.Difficulty
import com.retro.sudoku97.model.SudokuUiState
import com.retro.sudoku97.model.formatBestTime
import com.retro.sudoku97.model.formatElapsed
import com.retro.sudoku97.ui.Win95
import com.retro.sudoku97.ui.components.RetroButton
import com.retro.sudoku97.ui.components.RetroButtonRow
import com.retro.sudoku97.ui.components.RetroDialogWindow
import com.retro.sudoku97.ui.components.RetroHorizontalRule
import com.retro.sudoku97.ui.components.RetroKeyValueRow
import com.retro.sudoku97.ui.components.RetroPanel
import com.retro.sudoku97.ui.components.RetroProgressBar
import com.retro.sudoku97.ui.components.RetroRadioOption
import com.retro.sudoku97.ui.components.RetroWindow

@Composable
fun NewGameDialog(
    initialDifficulty: Difficulty,
    hasActiveGame: Boolean,
    onDismiss: () -> Unit,
    onStart: (Difficulty) -> Unit
) {
    var selectedName by rememberSaveable {
        mutableStateOf(initialDifficulty.name)
    }
    val selected = Difficulty.fromStoredName(selectedName) ?: Difficulty.MEDIUM

    RetroDialogWindow(
        title = "New Game - Select Difficulty",
        onDismiss = onDismiss
    ) {
        if (hasActiveGame) {
            Text(
                text = "Starting a new puzzle will replace the game in progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = Win95.Red
            )
            Spacer(Modifier.height(10.dp))
        }
        Text(
            text = "Choose a difficulty:",
            style = MaterialTheme.typography.titleMedium,
            color = Win95.Black
        )
        Spacer(Modifier.height(6.dp))
        RetroPanel(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                RetroRadioOption(
                    selected = selected == difficulty,
                    onClick = { selectedName = difficulty.name },
                    title = difficulty.displayName,
                    subtitle = "${difficulty.clueSummary}  •  ${difficulty.tagline}"
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        RetroButtonRow {
            RetroButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                minHeight = 36.dp
            )
            RetroButton(
                text = "Start Game",
                onClick = { onStart(selected) },
                modifier = Modifier.weight(1f),
                minHeight = 36.dp
            )
        }
    }
}

@Composable
fun BestTimesDialog(
    bestTimes: Map<Difficulty, Long>,
    onDismiss: () -> Unit
) {
    RetroDialogWindow(
        title = "Sudoku 97 - Best Times",
        onDismiss = onDismiss
    ) {
        Text(
            text = "Fastest completed puzzle for each difficulty:",
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.Black
        )
        Spacer(Modifier.height(8.dp))
        RetroPanel(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                RetroKeyValueRow(
                    key = "${difficulty.displayName}  (${difficulty.clueSummary})",
                    value = formatBestTime(bestTimes[difficulty]),
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        RetroButton(
            text = "Close",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 36.dp
        )
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    RetroDialogWindow(
        title = "How to Play",
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DialogHeading("The goal")
            DialogParagraph(
                "Fill every empty cell so that each row, each column and each 3x3 box " +
                    "contains the digits 1 through 9 exactly once."
            )
            Spacer(Modifier.height(9.dp))
            DialogHeading("Controls")
            DialogBullet("Tap a cell, then tap a number from the keypad.")
            DialogBullet("Pencil toggles small candidate notes inside an empty cell.")
            DialogBullet("Erase removes the selected value and its pencil marks.")
            DialogBullet("Hint finds a naked single, hidden single or naked pair. If no simple pattern exists, it reveals one correct cell.")
            DialogBullet("Options > Highlight Conflicts marks duplicate numbers in red.")
            Spacer(Modifier.height(9.dp))
            DialogHeading("Difficulty")
            DialogBullet("Easy: ${Difficulty.EASY.clueSummary}")
            DialogBullet("Medium: ${Difficulty.MEDIUM.clueSummary}")
            DialogBullet("Hard: ${Difficulty.HARD.clueSummary}")
            Spacer(Modifier.height(9.dp))
            DialogHeading("Saving")
            DialogParagraph(
                "Your board, pencil notes and elapsed time are saved locally whenever " +
                    "the app is paused or stopped. Reopen the app to continue exactly where you left off."
            )
        }
        Spacer(Modifier.height(12.dp))
        RetroButton(
            text = "Close",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 36.dp
        )
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    RetroDialogWindow(
        title = "About Sudoku 97",
        onDismiss = onDismiss
    ) {
        Text(
            text = "SUDOKU 97",
            style = MaterialTheme.typography.headlineMedium,
            color = Win95.Navy
        )
        Text(
            text = "Version 1.0  •  Windows 97 edition",
            style = MaterialTheme.typography.bodySmall,
            color = Win95.DarkGray
        )
        Spacer(Modifier.height(10.dp))
        RetroHorizontalRule()
        Spacer(Modifier.height(10.dp))
        DialogParagraph(
            "A classic 9x9 Sudoku game with a hand-built Windows 95/97 interface. " +
                "Every puzzle is generated on-device with backtracking and checked for exactly one solution."
        )
        Spacer(Modifier.height(8.dp))
        RetroPanel(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
        ) {
            RetroKeyValueRow("Internet permission", "None")
            RetroKeyValueRow("Ads / trackers", "None")
            RetroKeyValueRow("Analytics", "None")
            RetroKeyValueRow("Storage", "Local only")
        }
        Spacer(Modifier.height(12.dp))
        RetroButton(
            text = "Close",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            minHeight = 36.dp
        )
    }
}

@Composable
fun ExitDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    RetroDialogWindow(
        title = "Exit Sudoku 97",
        onDismiss = onDismiss
    ) {
        Text(
            text = "Exit Sudoku 97?",
            style = MaterialTheme.typography.titleMedium,
            color = Win95.Black
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Your active game, pencil notes and timer are saved automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.DarkGray
        )
        Spacer(Modifier.height(12.dp))
        RetroButtonRow {
            RetroButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                minHeight = 36.dp
            )
            RetroButton(
                text = "Exit",
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                minHeight = 36.dp
            )
        }
    }
}

@Composable
fun GeneratingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            RetroWindow(
                title = "Sudoku 97",
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
                onMinimize = null,
                onMaximize = null,
                onClose = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Generating puzzle...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Win95.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Building a complete board, then removing clues while checking that exactly one solution remains.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Win95.DarkGray
                    )
                    Spacer(Modifier.height(12.dp))
                    RetroProgressBar(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Dual-direction uniqueness check in progress...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Win95.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun WinDialog(
    state: SudokuUiState,
    onNewGame: () -> Unit,
    onMenu: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            RetroWindow(
                title = "Sudoku 97 - Congratulations",
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
                    .heightIn(max = maxHeight),
                onMinimize = null,
                onMaximize = null,
                onClose = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PUZZLE SOLVED!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Win95.Navy,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "You completed the ${state.difficulty.displayName.lowercase()} puzzle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Win95.Black
                    )
                    Spacer(Modifier.height(12.dp))
                    RetroPanel(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
                    ) {
                        RetroKeyValueRow(
                            key = "Difficulty",
                            value = state.difficulty.displayName
                        )
                        RetroKeyValueRow(
                            key = "Your time",
                            value = formatElapsed(state.elapsedSeconds)
                        )
                        RetroKeyValueRow(
                            key = "Best time",
                            value = formatBestTime(state.bestTimes[state.difficulty])
                        )
                    }
                    if (state.isNewBest) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "★ NEW BEST TIME! ★",
                            style = MaterialTheme.typography.labelLarge,
                            color = Win95.Green,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroButton(
                            text = "New Game",
                            onClick = onNewGame,
                            modifier = Modifier.weight(1f),
                            minHeight = 38.dp
                        )
                        RetroButton(
                            text = "Main Menu",
                            onClick = onMenu,
                            modifier = Modifier.weight(1f),
                            minHeight = 38.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = Win95.Navy
    )
}

@Composable
private fun DialogParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Win95.Black
    )
}

@Composable
private fun DialogBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.Navy,
            modifier = Modifier.width(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.Black
        )
    }
}

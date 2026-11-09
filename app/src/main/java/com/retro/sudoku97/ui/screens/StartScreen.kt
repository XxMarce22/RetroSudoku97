package com.retro.sudoku97.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.sudoku97.model.Difficulty
import com.retro.sudoku97.model.SudokuUiState
import com.retro.sudoku97.model.formatBestTime
import com.retro.sudoku97.model.formatElapsed
import com.retro.sudoku97.ui.Win95
import com.retro.sudoku97.ui.components.RetroButton
import com.retro.sudoku97.ui.components.RetroButtonRow
import com.retro.sudoku97.ui.components.RetroHorizontalRule
import com.retro.sudoku97.ui.components.RetroKeyValueRow
import com.retro.sudoku97.ui.components.RetroPanel
import com.retro.sudoku97.ui.components.RetroRadioOption
import com.retro.sudoku97.ui.components.RetroWindow

@Composable
fun StartScreen(
    state: SudokuUiState,
    onStart: (Difficulty) -> Unit,
    onResume: () -> Unit,
    onBestTimes: () -> Unit,
    onHowToPlay: () -> Unit,
    onAbout: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficultyName by rememberSaveable {
        mutableStateOf(state.difficulty.name)
    }
    val selectedDifficulty = Difficulty.fromStoredName(selectedDifficultyName) ?: Difficulty.MEDIUM

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        RetroWindow(
            title = "Sudoku 97 - New Game",
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .heightIn(max = maxHeight),
            onMinimize = onMinimize,
            onMaximize = onMaximize,
            onClose = onClose
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SudokuLogo(Modifier.size(56.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "SUDOKU 97",
                            style = MaterialTheme.typography.displayLarge,
                            color = Win95.Navy
                        )
                        Text(
                            text = "Classic 9x9  •  Windows 97 edition",
                            style = MaterialTheme.typography.bodySmall,
                            color = Win95.DarkGray
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                RetroHorizontalRule()
                Spacer(Modifier.height(12.dp))

                if (state.canResume) {
                    RetroPanel(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
                    ) {
                        Text(
                            text = "Saved game found",
                            style = MaterialTheme.typography.titleMedium,
                            color = Win95.Black
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${state.difficulty.displayName}  •  ${formatElapsed(state.elapsedSeconds)} elapsed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Win95.DarkGray
                        )
                        Spacer(Modifier.height(8.dp))
                        RetroButton(
                            text = "Resume Game",
                            onClick = onResume,
                            modifier = Modifier.fillMaxWidth(),
                            minHeight = 38.dp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                RetroPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
                ) {
                    Text(
                        text = "Choose a difficulty",
                        style = MaterialTheme.typography.titleMedium,
                        color = Win95.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Difficulty.entries.forEach { difficulty ->
                        RetroRadioOption(
                            selected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficultyName = difficulty.name },
                            title = difficulty.displayName,
                            subtitle = "${difficulty.clueSummary}  •  ${difficulty.tagline}"
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                RetroButtonRow {
                    RetroButton(
                        text = "Start Game",
                        onClick = { onStart(selectedDifficulty) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isGenerating,
                        minHeight = 40.dp
                    )
                    RetroButton(
                        text = "Best Times",
                        onClick = onBestTimes,
                        modifier = Modifier.weight(1f),
                        minHeight = 40.dp
                    )
                }

                Spacer(Modifier.height(12.dp))

                RetroPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
                ) {
                    Text(
                        text = "Best times",
                        style = MaterialTheme.typography.titleMedium,
                        color = Win95.Black
                    )
                    Spacer(Modifier.height(5.dp))
                    Difficulty.entries.forEach { difficulty ->
                        RetroKeyValueRow(
                            key = difficulty.displayName,
                            value = formatBestTime(state.bestTimes[difficulty]),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                state.hintMessage?.let { message ->
                    Spacer(Modifier.height(12.dp))
                    RetroPanel(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Win95.Red
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                RetroHorizontalRule()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "100% offline  •  zero ads  •  no trackers",
                    style = MaterialTheme.typography.bodySmall,
                    color = Win95.DarkGray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    RetroButton(
                        text = "How to Play",
                        onClick = onHowToPlay,
                        minHeight = 30.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    RetroButton(
                        text = "About",
                        onClick = onAbout,
                        minHeight = 30.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SudokuLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val one = 1.dp.toPx()
        drawRect(Color.Black)
        drawRect(Win95.White, topLeft = Offset(one, one), size = Size(size.width - 2 * one, size.height - 2 * one))
        drawRect(
            Win95.Gray,
            topLeft = Offset(2 * one, 2 * one),
            size = Size(size.width - 4 * one, size.height - 4 * one)
        )
        drawRect(
            Win95.Navy,
            topLeft = Offset(3 * one, 3 * one),
            size = Size(size.width - 6 * one, 11.dp.toPx())
        )
        drawRect(
            Win95.White,
            topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
            size = Size(22.dp.toPx(), 2.dp.toPx())
        )

        val gridTop = 17.dp.toPx()
        val gridLeft = 6.dp.toPx()
        val gridSize = size.width - 12.dp.toPx()
        drawRect(Win95.White, topLeft = Offset(gridLeft, gridTop), size = Size(gridSize, size.height - gridTop - 5.dp.toPx()))
        val cell = gridSize / 9f
        for (i in 1..8) {
            val lineWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
            drawLine(
                color = if (i % 3 == 0) Win95.Black else Win95.MidGray,
                start = Offset(gridLeft + i * cell, gridTop),
                end = Offset(gridLeft + i * cell, gridTop + 9 * cell),
                strokeWidth = lineWidth
            )
            drawLine(
                color = if (i % 3 == 0) Win95.Black else Win95.MidGray,
                start = Offset(gridLeft, gridTop + i * cell),
                end = Offset(gridLeft + 9 * cell, gridTop + i * cell),
                strokeWidth = lineWidth
            )
        }
        val values = listOf(
            0 to Win95.Navy,
            4 to Win95.Red,
            8 to Win95.Navy,
            1 to Win95.Navy,
            5 to Win95.Navy,
            2 to Win95.Red,
            3 to Win95.Navy,
            7 to Win95.Navy,
            6 to Win95.Navy
        )
        values.forEachIndexed { position, (value, color) ->
            val row = position / 3
            val column = position % 3
            val blockSize = cell * 0.52f
            drawRect(
                color = color,
                topLeft = Offset(
                    gridLeft + column * 3 * cell + cell * 0.24f,
                    gridTop + row * 3 * cell + cell * 0.24f
                ),
                size = Size(blockSize, blockSize)
            )
        }
    }
}

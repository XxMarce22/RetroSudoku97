package com.retro.sudoku97.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retro.sudoku97.logic.SudokuRules
import com.retro.sudoku97.model.CELL_COUNT
import com.retro.sudoku97.ui.Win95

/**
 * The board is drawn as 81 beveled cells. Thick black rules mark the 3x3 boxes,
 * and pencil marks are rendered in a 3x3 mini-grid inside each empty cell.
 */
@Composable
fun SudokuBoardView(
    values: List<Int>,
    puzzle: List<Int>,
    candidates: List<Int>,
    selectedCell: Int?,
    conflicts: Set<Int>,
    highlightConflicts: Boolean,
    hintCells: Set<Int>,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (values.size != CELL_COUNT || puzzle.size != CELL_COUNT || candidates.size != CELL_COUNT) {
        return
    }

    val selectedValue = selectedCell?.let { values.getOrNull(it) } ?: 0
    val peerCells = remember(selectedCell) {
        selectedCell?.let { SudokuRules.PEERS[it].toSet() } ?: emptySet()
    }
    val sameValueCells = remember(selectedCell, selectedValue, values) {
        if (selectedValue !in 1..9) {
            emptySet()
        } else {
            values.indices.filter { index ->
                index != selectedCell && values[index] == selectedValue
            }.toSet()
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val innerSize = (maxWidth - 6.dp).coerceAtLeast(0.dp)
        val cellSize = innerSize / 9f
        val numberSize = (cellSize.value * 0.54f).sp
        val noteSize = (cellSize.value * 0.19f).sp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Win95.Gray)
                .win95Raised(2.dp)
                .padding(3.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                for (row in 0 until 9) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        for (column in 0 until 9) {
                            val index = row * 9 + column
                            val value = values[index]
                            val given = puzzle[index] != 0
                            val isConflict = highlightConflicts && index in conflicts
                            val isSelected = selectedCell == index
                            val isHinted = index in hintCells
                            val isPeer = index in peerCells
                            val isSameValue = index in sameValueCells

                            val background = when {
                                isConflict -> Win95.ConflictCell
                                isHinted -> Win95.HintCell
                                isSelected -> Win95.SelectedCell
                                isSameValue -> Color(0xFFC4D9EC)
                                isPeer -> Win95.PeerCell
                                given -> Win95.GivenCell
                                else -> Win95.EditableCell
                            }
                            val textColor = when {
                                isConflict -> Win95.Red
                                given -> Win95.Black
                                else -> Win95.Navy
                            }

                            val interactionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(background)
                                    .drawBehind {
                                        val thin = 1.dp.toPx()
                                        val thick = 2.dp.toPx()
                                        drawLine(
                                            Win95.White,
                                            Offset(0f, thin / 2f),
                                            Offset(size.width, thin / 2f),
                                            thin
                                        )
                                        drawLine(
                                            Win95.White,
                                            Offset(thin / 2f, 0f),
                                            Offset(thin / 2f, size.height),
                                            thin
                                        )
                                        drawLine(
                                            Win95.MidGray,
                                            Offset(0f, size.height - thin / 2f),
                                            Offset(size.width, size.height - thin / 2f),
                                            thin
                                        )
                                        drawLine(
                                            Win95.MidGray,
                                            Offset(size.width - thin / 2f, 0f),
                                            Offset(size.width - thin / 2f, size.height),
                                            thin
                                        )

                                        if (column % 3 == 2 && column != 8) {
                                            drawLine(
                                                Win95.Black,
                                                Offset(size.width - thick / 2f, 0f),
                                                Offset(size.width - thick / 2f, size.height),
                                                thick
                                            )
                                        }
                                        if (row % 3 == 2 && row != 8) {
                                            drawLine(
                                                Win95.Black,
                                                Offset(0f, size.height - thick / 2f),
                                                Offset(size.width, size.height - thick / 2f),
                                                thick
                                            )
                                        }

                                        if (isHinted) {
                                            drawRect(
                                                color = Color(0xFF806000),
                                                style = Stroke(width = 2.dp.toPx()),
                                                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                                size = androidx.compose.ui.geometry.Size(
                                                    size.width - 4.dp.toPx(),
                                                    size.height - 4.dp.toPx()
                                                )
                                            )
                                        }
                                        if (isSelected) {
                                            drawRect(
                                                color = Win95.Navy,
                                                style = Stroke(width = 2.dp.toPx()),
                                                topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                                                size = androidx.compose.ui.geometry.Size(
                                                    size.width - 2.dp.toPx(),
                                                    size.height - 2.dp.toPx()
                                                )
                                            )
                                        }
                                    }
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        onCellClick(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = value.toString(),
                                        color = textColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = numberSize,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                } else if (candidates[index] != 0) {
                                    PencilMarks(
                                        mask = candidates[index],
                                        noteSize = noteSize,
                                        color = if (isConflict) Win95.Red else Win95.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PencilMarks(
    mask: Int,
    noteSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (column in 0..2) {
                    val digit = row * 3 + column + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .wrapContentSize(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mask and (1 shl (digit - 1)) != 0) {
                            Text(
                                text = digit.toString(),
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = noteSize,
                                lineHeight = noteSize,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

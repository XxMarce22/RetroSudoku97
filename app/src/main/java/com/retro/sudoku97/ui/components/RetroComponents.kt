package com.retro.sudoku97.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retro.sudoku97.ui.Win95

/**
 * Classic two-pixel raised bevel. The background should already be Win95.Gray.
 */
fun Modifier.win95Raised(bevel: Dp = 2.dp): Modifier = drawBehind {
    val width = bevel.toPx().coerceAtLeast(1f)
    drawLine(
        color = Win95.White,
        start = Offset(0f, width / 2f),
        end = Offset(size.width, width / 2f),
        strokeWidth = width
    )
    drawLine(
        color = Win95.White,
        start = Offset(width / 2f, 0f),
        end = Offset(width / 2f, size.height),
        strokeWidth = width
    )
    drawLine(
        color = Win95.Black,
        start = Offset(0f, size.height - width / 2f),
        end = Offset(size.width, size.height - width / 2f),
        strokeWidth = width
    )
    drawLine(
        color = Win95.Black,
        start = Offset(size.width - width / 2f, 0f),
        end = Offset(size.width - width / 2f, size.height),
        strokeWidth = width
    )
}

/**
 * Classic pressed / inset bevel used by text fields, panels and toggled buttons.
 */
fun Modifier.win95Sunken(bevel: Dp = 2.dp): Modifier = drawBehind {
    val width = bevel.toPx().coerceAtLeast(1f)
    drawLine(
        color = Win95.MidGray,
        start = Offset(0f, width / 2f),
        end = Offset(size.width, width / 2f),
        strokeWidth = width
    )
    drawLine(
        color = Win95.MidGray,
        start = Offset(width / 2f, 0f),
        end = Offset(width / 2f, size.height),
        strokeWidth = width
    )
    drawLine(
        color = Win95.White,
        start = Offset(0f, size.height - width / 2f),
        end = Offset(size.width, size.height - width / 2f),
        strokeWidth = width
    )
    drawLine(
        color = Win95.White,
        start = Offset(size.width - width / 2f, 0f),
        end = Offset(size.width - width / 2f, size.height),
        strokeWidth = width
    )
}

fun Modifier.win95WindowBorder(): Modifier = drawBehind {
    val one = 1.dp.toPx()
    drawLine(Win95.White, Offset(0f, one / 2f), Offset(size.width, one / 2f), one)
    drawLine(Win95.White, Offset(one / 2f, 0f), Offset(one / 2f, size.height), one)
    drawLine(Win95.Black, Offset(0f, size.height - one / 2f), Offset(size.width, size.height - one / 2f), one)
    drawLine(Win95.Black, Offset(size.width - one / 2f, 0f), Offset(size.width - one / 2f, size.height), one)

    val inset = one * 1.5f
    drawLine(
        Win95.LightGray,
        Offset(inset, inset),
        Offset(size.width - inset, inset),
        one
    )
    drawLine(
        Win95.LightGray,
        Offset(inset, inset),
        Offset(inset, size.height - inset),
        one
    )
    drawLine(
        Win95.MidGray,
        Offset(inset, size.height - inset),
        Offset(size.width - inset, size.height - inset),
        one
    )
    drawLine(
        Win95.MidGray,
        Offset(size.width - inset, inset),
        Offset(size.width - inset, size.height - inset),
        one
    )
}

@Composable
fun RetroWindow(
    title: String,
    modifier: Modifier = Modifier,
    fillSize: Boolean = false,
    onMinimize: (() -> Unit)? = null,
    onMaximize: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val innerSize = if (fillSize) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black.copy(alpha = 0.30f))
        )
        Column(
            modifier = innerSize
                .background(Win95.Gray)
                .win95WindowBorder()
                .padding(3.dp)
        ) {
            RetroTitleBar(
                title = title,
                onMinimize = onMinimize,
                onMaximize = onMaximize,
                onClose = onClose
            )
            content()
        }
    }
}

@Composable
fun RetroTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    onMinimize: (() -> Unit)? = null,
    onMaximize: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(27.dp)
            .background(Win95.Navy)
            .padding(start = 4.dp, end = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroWindowIcon()
        Spacer(Modifier.width(5.dp))
        Text(
            text = title,
            color = Win95.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(4.dp))
        TitleBarButton(symbol = "_", onClick = onMinimize)
        Spacer(Modifier.width(2.dp))
        TitleBarButton(symbol = "□", onClick = onMaximize)
        Spacer(Modifier.width(2.dp))
        TitleBarButton(symbol = "X", onClick = onClose)
    }
}

@Composable
private fun TitleBarButton(
    symbol: String,
    onClick: (() -> Unit)?
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .size(width = 19.dp, height = 17.dp)
            .background(Win95.Gray)
            .then(if (pressed) Modifier.win95Sunken(1.dp) else Modifier.win95Raised(1.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Win95.Black,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RetroWindowIcon() {
    Canvas(Modifier.size(14.dp)) {
        drawRect(Win95.Teal)
        drawRect(Win95.White, topLeft = Offset(1.dp.toPx(), 1.dp.toPx()), size = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()))
        drawRect(
            Win95.Gray,
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx())
        )
        val cell = (size.width - 4.dp.toPx()) / 3f
        for (row in 0..2) {
            for (column in 0..2) {
                if ((row + column) % 2 == 0) {
                    drawRect(
                        Win95.Navy,
                        topLeft = Offset(
                            2.dp.toPx() + column * cell,
                            2.dp.toPx() + row * cell
                        ),
                        size = Size(cell, cell)
                    )
                }
            }
        }
    }
}

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    minHeight: Dp = 34.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isDown = pressed || active
    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .background(Win95.Gray)
            .then(if (isDown) Modifier.win95Sunken(2.dp) else Modifier.win95Raised(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Win95.Black else Win95.MidGray,
            style = textStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.offset(
                x = if (pressed) 1.dp else 0.dp,
                y = if (pressed) 1.dp else 0.dp
            )
        )
    }
}

@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    sunken: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Win95.Gray)
            .then(if (sunken) Modifier.win95Sunken(2.dp) else Modifier.win95Raised(2.dp))
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun RetroStatPanel(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Win95.Navy
) {
    Column(
        modifier = modifier
            .background(Win95.Gray)
            .win95Sunken(2.dp)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Win95.DarkGray,
            maxLines = 1
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
fun RetroRadioOption(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RetroRadioMark(selected = selected)
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Win95.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Win95.DarkGray
            )
        }
    }
}

@Composable
fun RetroRadioMark(selected: Boolean) {
    Canvas(Modifier.size(14.dp)) {
        val stroke = 1.dp.toPx()
        drawCircle(color = Win95.White, radius = size.minDimension / 2f - stroke / 2f)
        drawCircle(
            color = Win95.Black,
            radius = size.minDimension / 2f - stroke / 2f,
            style = Stroke(stroke)
        )
        if (selected) {
            drawCircle(color = Win95.Navy, radius = size.minDimension * 0.23f)
        }
    }
}

@Composable
fun RetroCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(Win95.White)
                .win95Sunken(1.dp),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Win95.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.Black
        )
    }
}

@Composable
fun RetroMenuBarItem(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Win95.Black,
            modifier = Modifier
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 7.dp, vertical = 3.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(220.dp)
                .background(Win95.Gray)
                .win95Raised(2.dp),
            offset = DpOffset(0.dp, 3.dp),
            shape = RectangleShape,
            containerColor = Win95.Gray,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            content()
        }
    }
}

@Composable
fun RetroMenuItem(
    label: String,
    onClick: () -> Unit,
    checked: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(18.dp)) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Win95.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) Win95.Black else Win95.MidGray
        )
    }
}

@Composable
fun RetroMenuSeparator() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .height(2.dp)
            .background(Win95.MidGray)
            .win95Sunken(1.dp)
    )
}

@Composable
fun RetroDialogWindow(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            RetroWindow(
                title = title,
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .heightIn(max = maxHeight),
                onMinimize = null,
                onMaximize = null,
                onClose = onDismiss
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun RetroProgressBar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "retro-progress")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "retro-progress-position"
    )

    Box(
        modifier = modifier
            .height(18.dp)
            .background(Win95.Gray)
            .win95Sunken(2.dp)
            .padding(3.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val segment = 11.dp.toPx()
            val gap = 3.dp.toPx()
            val travel = size.width + segment
            var x = progress * travel - segment
            while (x < size.width) {
                val left = x.coerceAtLeast(0f)
                val right = (x + segment).coerceAtMost(size.width)
                if (right > left) {
                    drawRect(
                        color = Win95.Navy,
                        topLeft = Offset(left, 0f),
                        size = Size(right - left, size.height)
                    )
                }
                x += segment + gap
            }
        }
    }
}

@Composable
fun RetroHorizontalRule(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Win95.MidGray)
            .win95Sunken(1.dp)
    )
}

@Composable
fun RetroKeyValueRow(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Win95.Navy
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = key, style = MaterialTheme.typography.bodyMedium, color = Win95.Black)
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = valueColor
        )
    }
}

@Composable
fun RetroButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

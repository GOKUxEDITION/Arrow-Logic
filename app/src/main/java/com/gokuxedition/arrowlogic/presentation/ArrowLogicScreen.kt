package com.gokuxedition.arrowlogic.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gokuxedition.arrowlogic.domain.Board
import com.gokuxedition.arrowlogic.domain.Direction
import com.gokuxedition.arrowlogic.domain.EscapeRules
import com.gokuxedition.arrowlogic.R

private val PageBackground = Color(0xFF090B1D)
private val PanelBackground = Color(0xFF151A38)
private val BoardBackground = Color(0xFF11162F)
private val GridColor = Color(0xFF30395F)
private val AvailableArrowColor = Color(0xFF00DDB4)
private val BlockedArrowColor = Color(0xFF756AAB)
private val GoldAccent = Color(0xFFFFC857)
private val MainText = Color(0xFFF4F5FF)
private val MutedText = Color(0xFFB9C2E4)
private val CellSize = 48.dp

private val ArrowLogicDarkColors = darkColorScheme(
    primary = AvailableArrowColor,
    onPrimary = Color(0xFF06231D),
    secondary = Color(0xFFC0A4FF),
    onSecondary = Color(0xFF20163B),
    background = PageBackground,
    onBackground = MainText,
    surface = PanelBackground,
    onSurface = MainText,
    surfaceVariant = Color(0xFF252B4D),
    onSurfaceVariant = MutedText,
    outline = Color(0xFF505A83)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrowLogicScreen(viewModel: ArrowLogicViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var isPaused by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = ArrowLogicDarkColors) {
        Scaffold(
            containerColor = PageBackground,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PageBackground,
                        titleContentColor = MainText
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val game = state.gameState
                val totalArrows = game?.originalBoard?.arrows?.size ?: 0
                val remainingArrows = game?.remainingArrows?.size ?: 0
                val moves = game?.moveCount ?: 0
                val progress = if (totalArrows == 0) 0f
                    else (totalArrows - remainingArrows).toFloat() / totalArrows.toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.level_label, state.level.decimal), style = MaterialTheme.typography.titleMedium, color = MainText)
                        Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodySmall, color = MutedText)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.arrows_count, remainingArrows, totalArrows), style = MaterialTheme.typography.labelLarge, color = MainText)
                        Text(stringResource(R.string.moves_count, moves), style = MaterialTheme.typography.labelMedium, color = MutedText)
                    }
                    if (game != null && !game.isComplete && !state.isGenerating) {
                        OutlinedButton(onClick = { isPaused = !isPaused }) {
                            Text(stringResource(if (isPaused) R.string.resume else R.string.pause))
                        }
                    }
                }

                if (game != null) {
                    Canvas(Modifier.fillMaxWidth().height(5.dp)) {
                        val radius = CornerRadius(size.height / 2f)
                        drawRoundRect(GridColor, cornerRadius = radius)
                        if (progress > 0f) {
                            drawRoundRect(
                                color = if (game.isComplete) GoldAccent else AvailableArrowColor,
                                size = Size(size.width * progress, size.height),
                                cornerRadius = radius
                            )
                        }
                    }
                }

                when {
                    state.isGenerating -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AvailableArrowColor)
                                Text(messageText(state.message), modifier = Modifier.padding(top = 16.dp), color = MainText)
                            }
                        }
                    }
                    game == null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = CardDefaults.cardColors(containerColor = PanelBackground)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(stringResource(R.string.puzzle_unavailable), style = MaterialTheme.typography.titleLarge, color = MainText)
                                Text(messageText(state.message), color = MutedText)
                                state.generationError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MutedText) }
                                Text(stringResource(R.string.unverified_level_warning), color = MainText)
                            }
                        }
                    }
                    isPaused -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            colors = CardDefaults.cardColors(containerColor = PanelBackground)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(stringResource(R.string.paused), style = MaterialTheme.typography.headlineMedium, color = GoldAccent)
                                Text(stringResource(R.string.session_unchanged), modifier = Modifier.padding(top = 8.dp), color = MutedText)
                                Button(
                                    onClick = { isPaused = false },
                                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                                ) { Text(stringResource(R.string.resume)) }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.resetLevel()
                                        isPaused = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.restart_level)) }
                            }
                        }
                    }
                    else -> {
                        val board = game.currentBoard()
                        BoardViewport(
                            board = board,
                            onArrowTapped = viewModel::onArrowTapped,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        Text(
                            messageText(state.message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (game.isComplete) GoldAccent else MainText,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::resetLevel,
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.restart)) }
                            Button(
                                onClick = {
                                    isPaused = false
                                    viewModel.nextLevel()
                                },
                                enabled = game.isComplete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldAccent,
                                    contentColor = Color(0xFF322000),
                                    disabledContainerColor = Color(0xFF3C4055),
                                    disabledContentColor = Color(0xFF9299B3)
                                )
                            ) { Text(stringResource(R.string.next_level)) }
                        }
                        Text(
                            stringResource(R.string.board_instruction),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun messageText(message: GameplayMessage): String = when (message) {
    GameplayMessage.Preparing -> stringResource(R.string.message_preparing)
    is GameplayMessage.Generating -> stringResource(R.string.message_generating, message.level)
    GameplayMessage.Verified -> stringResource(R.string.message_verified)
    GameplayMessage.GenerationFailed -> stringResource(R.string.message_generation_failed)
    is GameplayMessage.GenerationRejected -> stringResource(R.string.message_generation_rejected)
    is GameplayMessage.Complete -> stringResource(R.string.message_completed, message.moves)
    is GameplayMessage.ArrowEscaped -> stringResource(R.string.message_arrow_escaped, message.remaining)
    GameplayMessage.ArrowBlocked -> stringResource(R.string.message_arrow_blocked)
    GameplayMessage.ArrowUnknown -> stringResource(R.string.message_arrow_unknown)
    GameplayMessage.Reset -> stringResource(R.string.message_reset)
}

@Composable
private fun BoardViewport(
    board: Board,
    onArrowTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val horizontalState = rememberScrollState()
    val verticalState = rememberScrollState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BoardBackground)
            .horizontalScroll(horizontalState)
            .verticalScroll(verticalState),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .width(CellSize * board.width.toFloat())
                .height(CellSize * board.height.toFloat())
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val cellWidth = size.width / board.width
                val cellHeight = size.height / board.height
                for (column in 0..board.width) {
                    val x = column * cellWidth
                    drawLine(GridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                }
                for (row in 0..board.height) {
                    val y = row * cellHeight
                    drawLine(GridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            }

            board.arrows.forEach { arrow ->
                val isClear = EscapeRules.isClear(board, arrow.id)
                val horizontal = arrow.direction == Direction.LEFT || arrow.direction == Direction.RIGHT
                val arrowWidth = if (horizontal) CellSize * arrow.length.toFloat() else CellSize
                val arrowHeight = if (horizontal) CellSize else CellSize * arrow.length.toFloat()
                val arrowColor = if (isClear) AvailableArrowColor else BlockedArrowColor
                val cells = arrow.occupiedCells()
                val originX = cells.minOf { it.x }
                val originY = cells.minOf { it.y }
                val directionName = stringResource(
                    when (arrow.direction) {
                        Direction.UP -> R.string.direction_up
                        Direction.DOWN -> R.string.direction_down
                        Direction.LEFT -> R.string.direction_left
                        Direction.RIGHT -> R.string.direction_right
                    }
                )
                val arrowStateDescription = stringResource(
                    if (isClear) R.string.arrow_path_clear else R.string.arrow_blocked
                )

                Box(
                    modifier = Modifier
                        .offset(x = CellSize * originX.toFloat(), y = CellSize * originY.toFloat())
                        .size(width = arrowWidth, height = arrowHeight)
                        .clip(RoundedCornerShape(9.dp))
                        .background(arrowColor)
                        .clickable { onArrowTapped(arrow.id) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = stringResource(
                                R.string.arrow_content_description,
                                arrow.id,
                                directionName,
                                arrow.length,
                                arrowStateDescription
                            )
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize().padding(6.dp)) {
                        drawDirectionArrow(arrow.direction, Color.White)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDirectionArrow(direction: Direction, color: Color) {
    val strokeWidth = size.minDimension * 0.14f
    val halfHead = strokeWidth * 1.8f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val path = Path()

    when (direction) {
        Direction.RIGHT -> {
            val tipX = size.width - strokeWidth * 0.6f
            val baseX = tipX - halfHead * 1.7f
            drawLine(color, Offset(strokeWidth, centerY), Offset(baseX, centerY), strokeWidth, cap = StrokeCap.Round)
            path.moveTo(baseX, centerY - halfHead)
            path.lineTo(tipX, centerY)
            path.lineTo(baseX, centerY + halfHead)
        }
        Direction.LEFT -> {
            val tipX = strokeWidth * 0.6f
            val baseX = tipX + halfHead * 1.7f
            drawLine(color, Offset(size.width - strokeWidth, centerY), Offset(baseX, centerY), strokeWidth, cap = StrokeCap.Round)
            path.moveTo(baseX, centerY - halfHead)
            path.lineTo(tipX, centerY)
            path.lineTo(baseX, centerY + halfHead)
        }
        Direction.DOWN -> {
            val tipY = size.height - strokeWidth * 0.6f
            val baseY = tipY - halfHead * 1.7f
            drawLine(color, Offset(centerX, strokeWidth), Offset(centerX, baseY), strokeWidth, cap = StrokeCap.Round)
            path.moveTo(centerX - halfHead, baseY)
            path.lineTo(centerX, tipY)
            path.lineTo(centerX + halfHead, baseY)
        }
        Direction.UP -> {
            val tipY = strokeWidth * 0.6f
            val baseY = tipY + halfHead * 1.7f
            drawLine(color, Offset(centerX, size.height - strokeWidth), Offset(centerX, baseY), strokeWidth, cap = StrokeCap.Round)
            path.moveTo(centerX - halfHead, baseY)
            path.lineTo(centerX, tipY)
            path.lineTo(centerX + halfHead, baseY)
        }
    }
    path.close()
    drawPath(path, color)
}

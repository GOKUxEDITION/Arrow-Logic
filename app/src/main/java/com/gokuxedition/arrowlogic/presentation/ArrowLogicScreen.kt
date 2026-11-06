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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.mergeDescendants
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gokuxedition.arrowlogic.domain.Board
import com.gokuxedition.arrowlogic.domain.Direction
import com.gokuxedition.arrowlogic.domain.EscapeRules

private val PageBackground = Color(0xFFF5F8F6)
private val BoardBackground = Color(0xFFFFFFFF)
private val GridColor = Color(0xFFDDE6E0)
private val AvailableArrowColor = Color(0xFF0B7A4B)
private val BlockedArrowColor = Color(0xFF60736A)
private val CellSize = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrowLogicScreen(viewModel: ArrowLogicViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(title = {
                Text("ARROW LOGIC", style = MaterialTheme.typography.titleLarge)
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LEVEL ${state.level.decimal}", style = MaterialTheme.typography.titleMedium)
                    Text("Think ahead. Clear the path.", style = MaterialTheme.typography.bodySmall)
                }
                val moves = state.gameState?.moveCount ?: 0
                Text("$moves MOVES", style = MaterialTheme.typography.labelLarge)
            }

            when {
                state.isGenerating -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(state.message, modifier = Modifier.padding(top = 16.dp))
                        }
                    }
                }
                state.gameState == null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Puzzle unavailable", style = MaterialTheme.typography.titleLarge)
                            Text(state.message)
                            state.generationError?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            Text("The game will not display an unverified generated level.")
                        }
                    }
                }
                else -> {
                    val game = requireNotNull(state.gameState)
                    val board = game.currentBoard()
                    BoardViewport(
                        board = board,
                        onArrowTapped = viewModel::onArrowTapped,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::resetLevel,
                            modifier = Modifier.weight(1f)
                        ) { Text("Restart") }
                        Button(
                            onClick = viewModel::nextLevel,
                            enabled = game.isComplete,
                            modifier = Modifier.weight(1f)
                        ) { Text("Next level") }
                    }
                    Text(
                        "Tap any arrow segment. Blocked arrows stay in place until their path is clear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
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
                val directionName = arrow.direction.name.lowercase()

                Box(
                    modifier = Modifier
                        .offset(x = CellSize * arrow.tail.x.toFloat(), y = CellSize * arrow.tail.y.toFloat())
                        .size(width = arrowWidth, height = arrowHeight)
                        .clip(RoundedCornerShape(9.dp))
                        .background(arrowColor)
                        .clickable { onArrowTapped(arrow.id) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Arrow ${arrow.id}, points $directionName, length ${arrow.length}, ${if (isClear) "path clear" else "blocked"}"
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

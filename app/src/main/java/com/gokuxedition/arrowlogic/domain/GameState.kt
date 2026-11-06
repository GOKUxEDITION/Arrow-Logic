package com.gokuxedition.arrowlogic.domain

data class GameState(
    val originalBoard: Board,
    val remainingArrows: List<Arrow>,
    val moveCount: Int = 0
) {
    val isComplete: Boolean get() = remainingArrows.isEmpty()
    fun currentBoard(): Board = originalBoard.copy(arrows = remainingArrows)
}

sealed interface MoveResult {
    data class Escaped(val state: GameState) : MoveResult
    data class Blocked(val arrowId: String) : MoveResult
    data object UnknownArrow : MoveResult
}

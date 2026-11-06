package com.gokuxedition.arrowlogic.domain

object GameEngine {
    fun initialState(board: Board): GameState {
        val errors = EscapeRules.validateBoard(board)
        require(errors.isEmpty()) { errors.joinToString("; ") }
        return GameState(originalBoard = board, remainingArrows = board.arrows)
    }

    /** Rechecks legality against current state; a blocked/unknown move never mutates state. */
    fun tap(state: GameState, arrowId: String): MoveResult {
        val arrow = state.remainingArrows.firstOrNull { it.id == arrowId }
            ?: return MoveResult.UnknownArrow
        val current = state.currentBoard()
        if (!EscapeRules.isClear(current, arrow.id)) return MoveResult.Blocked(arrow.id)
        return MoveResult.Escaped(
            state.copy(
                remainingArrows = state.remainingArrows.filterNot { it.id == arrow.id },
                moveCount = state.moveCount + 1
            )
        )
    }
}

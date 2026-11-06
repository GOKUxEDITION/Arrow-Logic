package com.gokuxedition.arrowlogic.domain

data class ReplayValidation(val valid: Boolean, val reason: String? = null)

object SolutionReplayValidator {
    fun validate(board: Board, solution: List<String>): ReplayValidation {
        val structuralErrors = EscapeRules.validateBoard(board)
        if (structuralErrors.isNotEmpty()) return ReplayValidation(false, structuralErrors.joinToString("; "))
        if (solution.size != board.arrows.size || solution.toSet().size != solution.size) {
            return ReplayValidation(false, "Solution must contain every arrow exactly once")
        }
        var state = GameEngine.initialState(board)
        solution.forEachIndexed { index, arrowId ->
            when (val result = GameEngine.tap(state, arrowId)) {
                is MoveResult.Escaped -> state = result.state
                is MoveResult.Blocked -> return ReplayValidation(false, "Step $index is blocked: $arrowId")
                MoveResult.UnknownArrow -> return ReplayValidation(false, "Step $index references unknown arrow: $arrowId")
            }
        }
        return if (state.isComplete) ReplayValidation(true)
        else ReplayValidation(false, "Replay ended with ${state.remainingArrows.size} arrows remaining")
    }
}

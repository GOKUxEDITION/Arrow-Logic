package com.gokuxedition.arrowlogic.domain

sealed interface SolveResult {
    data class Solved(val arrowIds: List<String>) : SolveResult
    data object Unsolvable : SolveResult
    data object BudgetExceeded : SolveResult
}

/** Independent depth-first search over immutable active-arrow sets. */
object PuzzleSolver {
    fun solve(board: Board, nodeBudget: Long = 250_000): SolveResult {
        require(nodeBudget > 0)
        if (EscapeRules.validateBoard(board).isNotEmpty()) return SolveResult.Unsolvable
        val byId = board.arrows.associateBy { it.id }
        val failed = HashSet<Set<String>>()
        var visited = 0L
        var exhausted = false

        fun search(activeIds: Set<String>): List<String>? {
            if (activeIds.isEmpty()) return emptyList()
            if (activeIds in failed) return null
            visited++
            if (visited > nodeBudget) {
                exhausted = true
                return null
            }
            val activeBoard = board.copy(arrows = activeIds.mapNotNull(byId::get))
            for (id in EscapeRules.legalArrowIds(activeBoard)) {
                val suffix = search(activeIds - id)
                if (suffix != null) return listOf(id) + suffix
                if (exhausted) return null
            }
            failed += activeIds
            return null
        }

        val answer = search(board.arrows.map { it.id }.toSet())
        return when {
            answer != null -> SolveResult.Solved(answer)
            exhausted -> SolveResult.BudgetExceeded
            else -> SolveResult.Unsolvable
        }
    }
}

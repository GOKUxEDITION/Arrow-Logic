package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class PuzzleSolverTest {
    @Test fun solvesSingleArrowBoard() {
        val board = Board(3, 2, listOf(Arrow("a", Cell(0, 0), Direction.RIGHT, 2)))
        val result = PuzzleSolver.solve(board)
        assertEquals(SolveResult.Solved(listOf("a")), result)
    }

    @Test fun reportsBudgetExceededSeparately() {
        val board = Board(4, 4, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 1),
            Arrow("b", Cell(1, 0), Direction.DOWN, 1),
            Arrow("c", Cell(2, 0), Direction.LEFT, 1)
        ))
        assertEquals(SolveResult.BudgetExceeded, PuzzleSolver.solve(board, nodeBudget = 1))
    }
}

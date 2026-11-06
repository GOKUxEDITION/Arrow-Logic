package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    @Test fun blockedMoveDoesNotChangeState() {
        val board = Board(5, 2, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(3, 0), Direction.UP, 1)
        ))
        val state = GameEngine.initialState(board)
        assertEquals(MoveResult.Blocked("a"), GameEngine.tap(state, "a"))
        assertEquals(2, state.remainingArrows.size)
        assertEquals(0, state.moveCount)
    }

    @Test fun successfulMoveRemovesExactlyOneArrow() {
        val board = Board(3, 2, listOf(Arrow("a", Cell(0, 0), Direction.RIGHT, 2)))
        val state = GameEngine.initialState(board)
        val result = GameEngine.tap(state, "a") as MoveResult.Escaped
        assertTrue(result.state.isComplete)
        assertEquals(1, result.state.moveCount)
    }
}

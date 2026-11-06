package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class EscapeRulesTest {
    @Test fun clearArrowCanEscape() {
        val board = Board(4, 3, listOf(Arrow("a", Cell(0, 1), Direction.RIGHT, 2)))
        assertTrue(EscapeRules.isClear(board, "a"))
    }

    @Test fun arrowBlockedByCellAhead() {
        val board = Board(5, 2, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(3, 0), Direction.UP, 1)
        ))
        assertFalse(EscapeRules.isClear(board, "a"))
    }

    @Test fun ownOccupiedCellsAreNotBlockers() {
        val board = Board(4, 1, listOf(Arrow("a", Cell(0, 0), Direction.RIGHT, 3)))
        assertTrue(EscapeRules.isClear(board, "a"))
    }

    @Test fun detectsOverlapAndOutOfBounds() {
        val board = Board(2, 2, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(1, 0), Direction.DOWN, 2)
        ))
        assertTrue(EscapeRules.validateBoard(board).isNotEmpty())
    }
}

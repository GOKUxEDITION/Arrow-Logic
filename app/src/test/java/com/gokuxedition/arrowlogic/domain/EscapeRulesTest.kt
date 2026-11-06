package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class EscapeRulesTest {
    @Test fun clearArrowCanEscape() {
        val board = Board(4, 3, listOf(Arrow("a", Cell(1, 1), Direction.RIGHT, 2)))
        assertTrue(EscapeRules.isClear(board, "a"))
    }

    @Test fun arrowBlockedByCellAhead() {
        val board = Board(5, 2, listOf(
            Arrow("a", Cell(1, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(3, 0), Direction.UP, 1)
        ))
        assertFalse(EscapeRules.isClear(board, "a"))
    }

    @Test fun occupiedCellsExtendBackwardFromHead() {
        val right = Arrow("right", Cell(2, 1), Direction.RIGHT, 3)
        val left = Arrow("left", Cell(2, 2), Direction.LEFT, 3)
        assertEquals(listOf(Cell(2, 1), Cell(1, 1), Cell(0, 1)), right.occupiedCells())
        assertEquals(listOf(Cell(2, 2), Cell(3, 2), Cell(4, 2)), left.occupiedCells())
    }

    @Test fun ownOccupiedCellsAreNotBlockers() {
        val board = Board(4, 1, listOf(Arrow("a", Cell(2, 0), Direction.RIGHT, 3)))
        assertTrue(EscapeRules.isClear(board, "a"))
    }

    @Test fun detectsOverlapAndOutOfBounds() {
        val overlap = Board(2, 2, listOf(
            Arrow("a", Cell(1, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(1, 1), Direction.DOWN, 2)
        ))
        assertTrue(EscapeRules.validateBoard(overlap).isNotEmpty())

        val outOfBounds = Board(2, 2, listOf(Arrow("c", Cell(0, 0), Direction.LEFT, 2)))
        assertTrue(EscapeRules.validateBoard(outOfBounds).isNotEmpty())
    }
}

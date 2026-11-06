package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class SolutionReplayValidatorTest {
    @Test fun acceptsACompleteLegalWitness() {
        val board = Board(4, 2, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(0, 1), Direction.LEFT, 2)
        ))
        assertTrue(SolutionReplayValidator.validate(board, listOf("a", "b")).valid)
    }

    @Test fun rejectsBlockedWitnessStep() {
        val board = Board(5, 2, listOf(
            Arrow("a", Cell(0, 0), Direction.RIGHT, 2),
            Arrow("b", Cell(3, 0), Direction.UP, 1)
        ))
        assertFalse(SolutionReplayValidator.validate(board, listOf("a", "b")).valid)
    }
}

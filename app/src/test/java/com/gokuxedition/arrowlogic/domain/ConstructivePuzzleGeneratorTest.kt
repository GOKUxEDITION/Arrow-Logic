package com.gokuxedition.arrowlogic.domain

import org.junit.Assert.*
import org.junit.Test

class ConstructivePuzzleGeneratorTest {
    private val profile = DifficultyProfile(
        boardWidth = 5,
        boardHeight = 5,
        targetArrowCount = 5,
        minArrowLength = 1,
        maxArrowLength = 3,
        maxBoardAttempts = 16,
        solverNodeBudget = 100_000
    )

    @Test fun sameLevelAndSeedProduceSameBoardFingerprint() {
        val level = LevelNumber.parse("37")
        val first = ConstructivePuzzleGenerator.generate(level, "test-root", profile)
        val second = ConstructivePuzzleGenerator.generate(level, "test-root", profile)
        assertTrue("First generation failed: $first", first is GenerationResult.Generated)
        assertTrue("Second generation failed: $second", second is GenerationResult.Generated)
        assertEquals(
            (first as GenerationResult.Generated).fingerprint,
            (second as GenerationResult.Generated).fingerprint
        )
    }

    @Test fun generatedPuzzlePassesBothSolutionReplayGates() {
        val result = ConstructivePuzzleGenerator.generate(
            LevelNumber.parse("101"),
            "qa-seed",
            profile
        )
        assertTrue("Generation failed: $result", result is GenerationResult.Generated)
        val generated = result as GenerationResult.Generated
        assertTrue(EscapeRules.validateBoard(generated.board).isEmpty())
        assertTrue(
            SolutionReplayValidator.validate(generated.board, generated.constructiveSolution).valid
        )
        assertTrue(
            SolutionReplayValidator.validate(generated.board, generated.solverSolution).valid
        )
        assertEquals(profile.targetArrowCount, generated.board.arrows.size)
    }

    @Test fun hugeLevelNumberDoesNotOverflowDifficultyCalculation() {
        val level = LevelNumber.parse("999999999999999999999999999999999999999999999999")
        val difficulty = DifficultyProfile.forLevel(level)
        assertTrue(difficulty.boardWidth in 3..16)
        assertTrue(difficulty.targetArrowCount in 1..64)
    }

    @Test fun invalidGenerationProfilesAreRejected() {
        try {
            DifficultyProfile(
                boardWidth = 2,
                boardHeight = 5,
                targetArrowCount = 3
            )
            fail("Invalid board profile should be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected validation.
        }
    }
}

package com.gokuxedition.arrowlogic.domain

import java.math.BigInteger

/**
 * Bounded resource profile for one on-demand level. Level count itself is unbounded;
 * per-level board size and solver work remain deliberately bounded for mobile devices.
 */
data class DifficultyProfile(
    val boardWidth: Int,
    val boardHeight: Int,
    val targetArrowCount: Int,
    val minArrowLength: Int = 1,
    val maxArrowLength: Int = 3,
    val maxBoardAttempts: Int = 20,
    val solverNodeBudget: Long = 250_000
) {
    init {
        require(boardWidth in 3..16) { "Board width must be in 3..16" }
        require(boardHeight in 3..16) { "Board height must be in 3..16" }
        require(targetArrowCount in 1..64) { "Arrow count must be in 1..64" }
        require(minArrowLength in 1..minOf(boardWidth, boardHeight))
        require(maxArrowLength in minArrowLength..minOf(boardWidth, boardHeight))
        require(maxBoardAttempts in 1..100)
        require(solverNodeBudget > 0)
    }

    companion object {
        /** Monotonic, capped per-level difficulty; never converts a huge level ID to Int. */
        fun forLevel(level: LevelNumber): DifficultyProfile {
            val ordinal = level.asBigInteger
            val band = ((ordinal.bitLength() - 1) / 4).coerceIn(0, 8)
            val side = 5 + band
            return DifficultyProfile(
                boardWidth = side,
                boardHeight = side,
                targetArrowCount = 5 + band * 2,
                minArrowLength = 1,
                maxArrowLength = (3 + band / 2).coerceAtMost(side),
                maxBoardAttempts = 24,
                solverNodeBudget = 300_000
            )
        }
    }
}

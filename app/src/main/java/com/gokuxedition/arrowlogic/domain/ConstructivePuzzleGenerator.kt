package com.gokuxedition.arrowlogic.domain

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

enum class GenerationFailureReason {
    NO_VALID_PLACEMENT,
    CONSTRUCTIVE_WITNESS_REJECTED,
    SOLVER_PROVED_UNSOLVABLE,
    SOLVER_BUDGET_EXCEEDED
}

sealed interface GenerationResult {
    data class Generated(
        val level: LevelNumber,
        val board: Board,
        /** Reverse construction order; retained as a constructive solution witness. */
        val constructiveSolution: List<String>,
        /** Independently discovered solution from PuzzleSolver. */
        val solverSolution: List<String>,
        val fingerprint: String,
        val attemptsUsed: Int
    ) : GenerationResult

    data class Failed(
        val level: LevelNumber,
        val reason: GenerationFailureReason,
        val attemptsUsed: Int
    ) : GenerationResult
}

/**
 * Builds a puzzle by inserting each new arrow only when that arrow can escape immediately.
 * Therefore, removing arrows in reverse insertion order is always a constructive witness.
 * An independent solver and production-engine replay are still mandatory publication gates.
 */
object ConstructivePuzzleGenerator {
    const val GENERATOR_VERSION: Int = 1
    private const val MAX_EQUAL_SCORE_RESERVOIR = Int.MAX_VALUE

    fun generate(level: LevelNumber, rootSeed: String): GenerationResult =
        generate(level, rootSeed, DifficultyProfile.forLevel(level))

    fun generate(
        level: LevelNumber,
        rootSeed: String,
        profile: DifficultyProfile
    ): GenerationResult {
        require(rootSeed.isNotBlank()) { "Root seed must not be blank" }

        val random = DeterministicRandom(
            LevelSeed.derive(rootSeed, GENERATOR_VERSION, level)
        )
        var lastFailure = GenerationFailureReason.NO_VALID_PLACEMENT

        repeat(profile.maxBoardAttempts) { attemptIndex ->
            val inserted = mutableListOf<Arrow>()
            var constructionCompleted = true

            while (inserted.size < profile.targetArrowCount) {
                val candidate = chooseNextArrow(
                    width = profile.boardWidth,
                    height = profile.boardHeight,
                    existing = inserted,
                    nextIndex = inserted.size,
                    profile = profile,
                    random = random
                )
                if (candidate == null) {
                    constructionCompleted = false
                    lastFailure = GenerationFailureReason.NO_VALID_PLACEMENT
                    break
                }
                inserted += candidate
            }

            if (!constructionCompleted) return@repeat

            val board = Board(profile.boardWidth, profile.boardHeight, inserted.toList())
            if (EscapeRules.validateBoard(board).isNotEmpty()) {
                lastFailure = GenerationFailureReason.CONSTRUCTIVE_WITNESS_REJECTED
                return@repeat
            }

            val witness = inserted.asReversed().map { it.id }
            if (!SolutionReplayValidator.validate(board, witness).valid) {
                lastFailure = GenerationFailureReason.CONSTRUCTIVE_WITNESS_REJECTED
                return@repeat
            }

            when (val solver = PuzzleSolver.solve(board, profile.solverNodeBudget)) {
                is SolveResult.Solved -> {
                    val solverReplay = SolutionReplayValidator.validate(board, solver.arrowIds)
                    if (!solverReplay.valid) {
                        lastFailure = GenerationFailureReason.CONSTRUCTIVE_WITNESS_REJECTED
                        return@repeat
                    }
                    return GenerationResult.Generated(
                        level = level,
                        board = board,
                        constructiveSolution = witness,
                        solverSolution = solver.arrowIds,
                        fingerprint = fingerprint(board),
                        attemptsUsed = attemptIndex + 1
                    )
                }
                SolveResult.Unsolvable -> lastFailure = GenerationFailureReason.SOLVER_PROVED_UNSOLVABLE
                SolveResult.BudgetExceeded -> lastFailure = GenerationFailureReason.SOLVER_BUDGET_EXCEEDED
            }
        }

        return GenerationResult.Failed(
            level = level,
            reason = lastFailure,
            attemptsUsed = profile.maxBoardAttempts
        )
    }

    private fun chooseNextArrow(
        width: Int,
        height: Int,
        existing: List<Arrow>,
        nextIndex: Int,
        profile: DifficultyProfile,
        random: DeterministicRandom
    ): Arrow? {
        val currentBoard = Board(width, height, existing)
        val currentlyLegal = EscapeRules.legalArrowIds(currentBoard).toHashSet()
        val occupied = existing.asSequence().flatMap { it.occupiedCells().asSequence() }.toHashSet()

        var bestScore = -1
        var bestCandidate: Arrow? = null
        var equalBestCount = 0

        for (direction in Direction.entries) {
            for (length in profile.minArrowLength..profile.maxArrowLength) {
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val candidate = Arrow(
                            id = "A${(nextIndex + 1).toString().padStart(3, '0')}",
                            tail = Cell(x, y),
                            direction = direction,
                            length = length
                        )
                        val cells = candidate.occupiedCells()
                        if (cells.any { it.x !in 0 until width || it.y !in 0 until height }) continue
                        if (cells.any { it in occupied }) continue

                        val candidateBoard = Board(width, height, existing + candidate)
                        if (!EscapeRules.isClear(candidateBoard, candidate.id)) continue

                        // Prefer insertions that create new dependencies for earlier arrows.
                        val legalAfter = EscapeRules.legalArrowIds(candidateBoard).toHashSet()
                        val newlyBlocked = currentlyLegal.count { it !in legalAfter }
                        when {
                            newlyBlocked > bestScore -> {
                                bestScore = newlyBlocked
                                bestCandidate = candidate
                                equalBestCount = 1
                            }
                            newlyBlocked == bestScore -> {
                                equalBestCount++
                                // Reservoir sampling keeps selection unbiased among equal scores.
                                if (random.nextInt(equalBestCount) == 0) bestCandidate = candidate
                            }
                        }
                    }
                }
            }
        }
        return bestCandidate
    }

    private fun fingerprint(board: Board): String {
        val canonical = buildString {
            append(board.width).append('x').append(board.height).append('\n')
            board.arrows.sortedBy { it.id }.forEach { arrow ->
                append(arrow.id).append('|')
                    .append(arrow.tail.x).append(',').append(arrow.tail.y).append('|')
                    .append(arrow.direction.name).append('|')
                    .append(arrow.length).append('\n')
            }
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}

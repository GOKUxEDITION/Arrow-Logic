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
        val constructiveSolution: List<String>,
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
 * Inserts an arrow only if it is immediately escapable against all arrows already placed.
 * Reverse insertion order is therefore a constructive solution witness. An independent
 * solver and production-engine replay remain mandatory publication gates.
 */
object ConstructivePuzzleGenerator {
    const val GENERATOR_VERSION: Int = 1

    fun generate(level: LevelNumber, rootSeed: String): GenerationResult =
        generate(level, rootSeed, DifficultyProfile.forLevel(level))

    fun generate(
        level: LevelNumber,
        rootSeed: String,
        profile: DifficultyProfile
    ): GenerationResult {
        require(rootSeed.isNotBlank()) { "Root seed must not be blank" }
        val random = DeterministicRandom(LevelSeed.derive(rootSeed, GENERATOR_VERSION, level))
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
                    if (!SolutionReplayValidator.validate(board, solver.arrowIds).valid) {
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
        return GenerationResult.Failed(level, lastFailure, profile.maxBoardAttempts)
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
        val legalRays = existing.asSequence()
            .filter { EscapeRules.isClear(currentBoard, it.id) }
            .map { EscapeRules.forwardRayCells(width, height, it).toHashSet() }
            .toList()
        val occupied = existing.asSequence()
            .flatMap { it.occupiedCells().asSequence() }
            .toHashSet()

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
                        if (EscapeRules.forwardRayCells(width, height, candidate).any { it in occupied }) continue

                        val candidateCells = cells.toHashSet()
                        val newlyBlocked = legalRays.count { ray -> ray.any { it in candidateCells } }
                        when {
                            newlyBlocked > bestScore -> {
                                bestScore = newlyBlocked
                                bestCandidate = candidate
                                equalBestCount = 1
                            }
                            newlyBlocked == bestScore -> {
                                equalBestCount++
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
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}

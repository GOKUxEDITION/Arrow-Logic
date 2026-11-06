package com.gokuxedition.arrowlogic.data.repository

import com.gokuxedition.arrowlogic.data.local.CompletedLevelEntity
import com.gokuxedition.arrowlogic.data.local.GameDataDao
import com.gokuxedition.arrowlogic.data.local.PlayerProgressEntity
import com.gokuxedition.arrowlogic.data.local.PuzzleArrowEntity
import com.gokuxedition.arrowlogic.data.local.PuzzleDefinitionEntity
import com.gokuxedition.arrowlogic.data.local.PuzzleDefinitionWithArrows
import com.gokuxedition.arrowlogic.data.local.RecentPuzzleFingerprintEntity
import com.gokuxedition.arrowlogic.data.local.SessionActiveArrowEntity
import com.gokuxedition.arrowlogic.data.local.SessionSnapshotEntity
import com.gokuxedition.arrowlogic.data.local.SessionSnapshotWithActiveArrows
import com.gokuxedition.arrowlogic.domain.Arrow
import com.gokuxedition.arrowlogic.domain.Board
import com.gokuxedition.arrowlogic.domain.ConstructivePuzzleGenerator
import com.gokuxedition.arrowlogic.domain.DifficultyProfile
import com.gokuxedition.arrowlogic.domain.Direction
import com.gokuxedition.arrowlogic.domain.EscapeRules
import com.gokuxedition.arrowlogic.domain.GameState
import com.gokuxedition.arrowlogic.domain.LevelNumber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class RestoredGameSession(
    val puzzleId: String,
    val level: LevelNumber,
    val board: Board,
    val activeArrowIds: List<String>,
    val moveCount: Int,
    val elapsedActiveMs: Long,
    val hintsUsed: Int
) {
    fun toGameState(): GameState {
        val active = activeArrowIds.toSet()
        return GameState(
            originalBoard = board,
            remainingArrows = board.arrows.filter { it.id in active },
            moveCount = moveCount
        )
    }
}

data class CompletedLevelSummary(
    val levelNumber: String,
    val completedAtEpochMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val stars: Int
)

class ArrowLogicRepository(
    private val dao: GameDataDao,
    private val recentFingerprintLimit: Int = 500
) {
    init {
        require(recentFingerprintLimit in 1..5_000)
    }

    suspend fun getCurrentLevel(): LevelNumber =
        dao.getPlayerProgress()?.let { LevelNumber.parse(it.currentLevelNumber) } ?: LevelNumber.FIRST

    suspend fun loadPuzzle(level: LevelNumber): StoredPuzzle? {
        val definition = dao.getPuzzleByLevelAndVersion(
            levelNumber = level.decimal,
            generatorVersion = ConstructivePuzzleGenerator.GENERATOR_VERSION
        ) ?: return null
        return definition.toStoredPuzzleOrNull()
    }

    suspend fun persistGeneratedPuzzle(
        level: LevelNumber,
        profile: DifficultyProfile,
        board: Board,
        fingerprint: String,
        randomSeed: Long,
        seedRetryCounter: Int
    ): String? {
        require(seedRetryCounter >= 0)
        val puzzleId = stablePuzzleId(level, randomSeed, fingerprint)
        val now = System.currentTimeMillis()
        val definition = PuzzleDefinitionEntity(
            puzzleId = puzzleId,
            levelNumber = level.decimal,
            generatorVersion = ConstructivePuzzleGenerator.GENERATOR_VERSION,
            randomSeed = randomSeed,
            seedRetryCounter = seedRetryCounter,
            boardWidth = board.width,
            boardHeight = board.height,
            targetArrowCount = board.arrows.size,
            maxArrowLength = profile.maxArrowLength,
            solverNodeBudget = profile.solverNodeBudget,
            contentHash = fingerprint,
            createdAtEpochMs = now
        )
        val arrowRows = board.arrows.mapIndexed { index, arrow ->
            PuzzleArrowEntity(
                puzzleId = puzzleId,
                arrowId = arrow.id,
                orderIndex = index,
                headX = arrow.head.x,
                headY = arrow.head.y,
                direction = arrow.direction.name,
                length = arrow.length
            )
        }

        require(EscapeRules.validateBoard(board).isEmpty()) {
            "Refusing to persist an invalid board"
        }
        val fingerprintEntity = RecentPuzzleFingerprintEntity(
            fingerprint = fingerprint,
            levelNumber = level.decimal,
            generatorVersion = ConstructivePuzzleGenerator.GENERATOR_VERSION,
            createdAtEpochMs = now
        )
        val saved = dao.savePuzzleIfFingerprintNovel(
            definition = definition,
            arrows = arrowRows,
            fingerprint = fingerprintEntity,
            maximumRecentEntries = recentFingerprintLimit
        )
        if (!saved) return null
        updateCurrentProgress(level, puzzleId, now)
        return puzzleId
    }

    suspend fun restoreLatestSession(): RestoredGameSession? {
        val snapshot = dao.getLatestSession() ?: return null
        val restored = restoreSession(snapshot)
        if (restored == null) {
            dao.deleteSessionSnapshot(snapshot.snapshot.puzzleId)
            return null
        }
        return restored
    }

    suspend fun saveSession(
        puzzleId: String,
        level: LevelNumber,
        gameState: GameState,
        elapsedActiveMs: Long = 0L,
        hintsUsed: Int = 0
    ) {
        require(elapsedActiveMs >= 0)
        require(hintsUsed >= 0)
        val boardIds = gameState.originalBoard.arrows.map { it.id }.toSet()
        val activeIds = gameState.remainingArrows.map { it.id }
        require(activeIds.distinct().size == activeIds.size)
        require(activeIds.all { it in boardIds })

        val now = System.currentTimeMillis()
        val snapshot = SessionSnapshotEntity(
            puzzleId = puzzleId,
            levelNumber = level.decimal,
            moveCount = gameState.moveCount,
            elapsedActiveMs = elapsedActiveMs,
            hintsUsed = hintsUsed,
            updatedAtEpochMs = now
        )
        val activeRows = activeIds.mapIndexed { index, id ->
            SessionActiveArrowEntity(puzzleId, id, index)
        }
        dao.saveSession(snapshot, activeRows)
        updateCurrentProgress(level, puzzleId, now)
    }

    suspend fun completeLevel(
        puzzleId: String,
        level: LevelNumber,
        moveCount: Int,
        hintsUsed: Int
    ) {
        require(moveCount >= 0)
        require(hintsUsed >= 0)
        val now = System.currentTimeMillis()
        val nextLevel = level.next()
        val previous = dao.getPlayerProgress()
        val previousHighest = previous?.let {
            LevelNumber.parse(it.highestUnlockedLevelNumber)
        }
        val highest = if (previousHighest != null && previousHighest >= nextLevel) {
            previousHighest
        } else {
            nextLevel
        }

        val completed = CompletedLevelEntity(
            levelNumber = level.decimal,
            puzzleId = puzzleId,
            generatorVersion = ConstructivePuzzleGenerator.GENERATOR_VERSION,
            completedAtEpochMs = now,
            moveCount = moveCount,
            hintsUsed = hintsUsed,
            stars = 3
        )
        val progress = PlayerProgressEntity(
            currentLevelNumber = nextLevel.decimal,
            highestUnlockedLevelNumber = highest.decimal,
            currentPuzzleId = null,
            updatedAtEpochMs = now
        )
        dao.completeLevelAndAdvance(completed, progress, puzzleId)
    }

    suspend fun getCompletedLevels(limit: Int = 50, offset: Int = 0): List<CompletedLevelSummary> {
        require(limit in 1..200)
        require(offset >= 0)
        return dao.getCompletedLevels(limit, offset).map {
            CompletedLevelSummary(
                levelNumber = it.levelNumber,
                completedAtEpochMs = it.completedAtEpochMs,
                moveCount = it.moveCount,
                hintsUsed = it.hintsUsed,
                stars = it.stars
            )
        }
    }

    suspend fun isRecentDuplicate(fingerprint: String): Boolean =
        dao.hasRecentFingerprint(fingerprint)

    suspend fun getRecentFingerprints(limit: Int = 50, offset: Int = 0): List<String> {
        require(limit in 1..200)
        require(offset >= 0)
        return dao.getRecentFingerprints(limit, offset).map { it.fingerprint }
    }

    private suspend fun updateCurrentProgress(level: LevelNumber, puzzleId: String, now: Long) {
        val existing = dao.getPlayerProgress()
        val previousHighest = existing?.let {
            LevelNumber.parse(it.highestUnlockedLevelNumber)
        }
        val highest = if (previousHighest == null || level > previousHighest) level else previousHighest
        dao.upsertPlayerProgress(
            PlayerProgressEntity(
                currentLevelNumber = level.decimal,
                highestUnlockedLevelNumber = highest.decimal,
                currentPuzzleId = puzzleId,
                updatedAtEpochMs = now
            )
        )
    }

    private suspend fun restoreSession(
        relation: SessionSnapshotWithActiveArrows
    ): RestoredGameSession? {
        val session = relation.snapshot
        if (session.moveCount < 0 || session.elapsedActiveMs < 0 || session.hintsUsed < 0) return null
        val puzzle = dao.getPuzzleById(session.puzzleId)?.toStoredPuzzleOrNull() ?: return null
        val activeIds = relation.activeArrows.sortedBy { it.orderIndex }.map { it.arrowId }
        val allIds = puzzle.board.arrows.map { it.id }.toSet()
        if (activeIds.size != activeIds.distinct().size || activeIds.any { it !in allIds }) return null
        val level = runCatching { LevelNumber.parse(session.levelNumber) }.getOrNull() ?: return null
        if (puzzle.level != level) return null

        return RestoredGameSession(
            puzzleId = session.puzzleId,
            level = level,
            board = puzzle.board,
            activeArrowIds = activeIds,
            moveCount = session.moveCount,
            elapsedActiveMs = session.elapsedActiveMs,
            hintsUsed = session.hintsUsed
        )
    }

    private fun stablePuzzleId(level: LevelNumber, seed: Long, fingerprint: String): String {
        val identity = "${ConstructivePuzzleGenerator.GENERATOR_VERSION}|${level.decimal}|$seed|$fingerprint"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(identity.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}

data class StoredPuzzle(
    val puzzleId: String,
    val level: LevelNumber,
    val seed: Long,
    val seedRetryCounter: Int,
    val board: Board,
    val contentHash: String
)

private fun PuzzleDefinitionWithArrows.toStoredPuzzleOrNull(): StoredPuzzle? {
    val level = runCatching { LevelNumber.parse(definition.levelNumber) }.getOrNull() ?: return null
    val ordered = arrows.sortedBy { it.orderIndex }
    val domainArrows = ordered.map { row ->
        val direction = runCatching { Direction.valueOf(row.direction) }.getOrNull() ?: return null
        if (row.length <= 0) return null
        Arrow(
            id = row.arrowId,
            head = com.gokuxedition.arrowlogic.domain.Cell(row.headX, row.headY),
            direction = direction,
            length = row.length
        )
    }
    val board = runCatching {
        Board(definition.boardWidth, definition.boardHeight, domainArrows)
    }.getOrNull() ?: return null
    if (EscapeRules.validateBoard(board).isNotEmpty()) return null
    return StoredPuzzle(
        puzzleId = definition.puzzleId,
        level = level,
        seed = definition.randomSeed,
        seedRetryCounter = definition.seedRetryCounter,
        board = board,
        contentHash = definition.contentHash
    )
}

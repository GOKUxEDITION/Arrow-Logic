package com.gokuxedition.arrowlogic.data.repository

import androidx.room.Room
import com.gokuxedition.arrowlogic.data.local.ArrowLogicDatabase
import com.gokuxedition.arrowlogic.domain.Arrow
import com.gokuxedition.arrowlogic.domain.Board
import com.gokuxedition.arrowlogic.domain.Cell
import com.gokuxedition.arrowlogic.domain.DifficultyProfile
import com.gokuxedition.arrowlogic.domain.Direction
import com.gokuxedition.arrowlogic.domain.GameEngine
import com.gokuxedition.arrowlogic.domain.LevelNumber
import com.gokuxedition.arrowlogic.domain.MoveResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ArrowLogicRepositoryTest {
    private lateinit var database: ArrowLogicDatabase
    private lateinit var repository: ArrowLogicRepository
    private lateinit var dao: com.gokuxedition.arrowlogic.data.local.GameDataDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication<android.app.Application>()
        database = Room.inMemoryDatabaseBuilder(context, ArrowLogicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.gameDataDao()
        repository = ArrowLogicRepository(dao)
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
    }

    @Test
    fun savesPuzzleAndRestoresExactPartiallyPlayedSession() = runBlocking {
        val level = LevelNumber.parse("999999999999999999999999")
        val board = validBoard()
        val puzzleId = repository.persistGeneratedPuzzle(
            level = level,
            profile = profile(),
            board = board,
            fingerprint = "repository-roundtrip-fingerprint",
            randomSeed = 456L,
            seedRetryCounter = 2
        )
        assertNotNull(puzzleId)

        val initial = GameEngine.initialState(board)
        val firstMove = GameEngine.tap(initial, "A001") as MoveResult.Escaped
        repository.saveSession(requireNotNull(puzzleId), level, firstMove.state)

        val restored = repository.restoreLatestSession()
        assertNotNull(restored)
        assertEquals(level, restored!!.level)
        assertEquals(puzzleId, restored.puzzleId)
        assertEquals(1, restored.moveCount)
        assertEquals(listOf("A002"), restored.activeArrowIds)
        assertEquals(firstMove.state, restored.toGameState())
    }

    @Test
    fun recentDuplicateIsNotPersistedAndCanBeRetriedWithNewFingerprint() = runBlocking {
        val first = repository.persistGeneratedPuzzle(
            level = LevelNumber.parse("1"),
            profile = profile(),
            board = validBoard(),
            fingerprint = "duplicate-fingerprint",
            randomSeed = 100L,
            seedRetryCounter = 0
        )
        assertNotNull(first)

        val duplicate = repository.persistGeneratedPuzzle(
            level = LevelNumber.parse("2"),
            profile = profile(),
            board = validBoard(),
            fingerprint = "duplicate-fingerprint",
            randomSeed = 200L,
            seedRetryCounter = 1
        )
        assertNull(duplicate)

        val distinct = repository.persistGeneratedPuzzle(
            level = LevelNumber.parse("2"),
            profile = profile(),
            board = validBoard(),
            fingerprint = "different-fingerprint",
            randomSeed = 201L,
            seedRetryCounter = 2
        )
        assertNotNull(distinct)
        assertTrue(repository.isRecentDuplicate("duplicate-fingerprint"))
        assertTrue(repository.isRecentDuplicate("different-fingerprint"))
        assertEquals(LevelNumber.parse("2"), repository.getCurrentLevel())
    }

    @Test
    fun completedPuzzleAdvancesArbitraryPrecisionCampaignAtomically() = runBlocking {
        val level = LevelNumber.parse("9223372036854775808")
        val board = validBoard()
        val puzzleId = requireNotNull(
            repository.persistGeneratedPuzzle(
                level = level,
                profile = profile(),
                board = board,
                fingerprint = "complete-level-fingerprint",
                randomSeed = 900L,
                seedRetryCounter = 0
            )
        )
        repository.completeLevel(
            puzzleId = puzzleId,
            level = level,
            moveCount = board.arrows.size,
            hintsUsed = 0
        )

        assertEquals("9223372036854775809", repository.getCurrentLevel().decimal)
        assertNull(repository.restoreLatestSession())
        val completed = repository.getCompletedLevels(limit = 10)
        assertEquals(1, completed.size)
        assertEquals(level.decimal, completed.single().levelNumber)
    }

    @Test
    fun invalidSessionIsDiscardedInsteadOfRestored() = runBlocking {
        val level = LevelNumber.parse("3")
        val board = validBoard()
        val puzzleId = requireNotNull(
            repository.persistGeneratedPuzzle(
                level = level,
                profile = profile(),
                board = board,
                fingerprint = "invalid-session-fingerprint",
                randomSeed = 303L,
                seedRetryCounter = 0
            )
        )
        // Two arrows remain, so a zero move count is consistent only with both arrows active.
        // The corrupt snapshot omits both active IDs and must be discarded on restore.
        dao.saveSession(
            com.gokuxedition.arrowlogic.data.local.SessionSnapshotEntity(
                puzzleId = puzzleId,
                levelNumber = level.decimal,
                moveCount = 0,
                elapsedActiveMs = 0L,
                hintsUsed = 0,
                updatedAtEpochMs = 500L
            ),
            emptyList()
        )

        assertNull(repository.restoreLatestSession())
        assertNull(dao.getLatestSession())
        assertEquals(level, repository.getCurrentLevel())
    }

    @Test
    fun completingHistoricalLevelDoesNotRegressCurrentCampaignPointer() = runBlocking {
        val board = validBoard()
        val historicalPuzzle = requireNotNull(
            repository.persistGeneratedPuzzle(
                level = LevelNumber.parse("5"),
                profile = profile(),
                board = board,
                fingerprint = "historical-level-layout",
                randomSeed = 505L,
                seedRetryCounter = 0
            )
        )
        repository.persistGeneratedPuzzle(
            level = LevelNumber.parse("10"),
            profile = profile(),
            board = board,
            fingerprint = "current-level-layout",
            randomSeed = 1010L,
            seedRetryCounter = 0
        )

        repository.completeLevel(
            puzzleId = historicalPuzzle,
            level = LevelNumber.parse("5"),
            moveCount = board.arrows.size,
            hintsUsed = 0
        )

        assertEquals("10", repository.getCurrentLevel().decimal)
    }

    private fun profile() = DifficultyProfile(
        boardWidth = 4,
        boardHeight = 3,
        targetArrowCount = 2,
        minArrowLength = 1,
        maxArrowLength = 2,
        maxBoardAttempts = 4,
        solverNodeBudget = 10_000
    )

    private fun validBoard() = Board(
        width = 4,
        height = 3,
        arrows = listOf(
            Arrow("A001", Cell(1, 0), Direction.RIGHT, 2),
            Arrow("A002", Cell(2, 2), Direction.LEFT, 2)
        )
    )
}

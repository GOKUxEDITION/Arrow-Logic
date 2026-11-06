package com.gokuxedition.arrowlogic.data.local

import android.content.Context
import androidx.room.Room
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
class GameDataDaoTest {
    private lateinit var database: ArrowLogicDatabase
    private lateinit var dao: GameDataDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, ArrowLogicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.gameDataDao()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
    }

    @Test
    fun puzzleAndSessionRoundTripPreservesArbitraryPrecisionLevelText() = runBlocking {
        val level = "999999999999999999999999999999999999999"
        val puzzle = puzzleDefinition(levelNumber = level)
        val arrows = listOf(
            PuzzleArrowEntity(
                puzzleId = puzzle.puzzleId,
                arrowId = "A001",
                orderIndex = 0,
                headX = 1,
                headY = 0,
                direction = "RIGHT",
                length = 2
            ),
            PuzzleArrowEntity(
                puzzleId = puzzle.puzzleId,
                arrowId = "A002",
                orderIndex = 1,
                headX = 1,
                headY = 2,
                direction = "LEFT",
                length = 2
            )
        )
        dao.savePuzzle(puzzle, arrows)

        val session = SessionSnapshotEntity(
            puzzleId = puzzle.puzzleId,
            levelNumber = level,
            moveCount = 1,
            elapsedActiveMs = 12_345L,
            hintsUsed = 0,
            updatedAtEpochMs = 100L
        )
        dao.saveSession(
            session,
            listOf(SessionActiveArrowEntity(puzzle.puzzleId, "A002", 0))
        )

        val loadedPuzzle = dao.getPuzzleByLevelAndVersion(level, 1)
        val loadedSession = dao.getLatestSession()

        assertNotNull(loadedPuzzle)
        assertEquals(level, loadedPuzzle!!.definition.levelNumber)
        assertEquals(listOf("A001", "A002"), loadedPuzzle.arrows.sortedBy { it.orderIndex }.map { it.arrowId })
        assertNotNull(loadedSession)
        assertEquals(1, loadedSession!!.snapshot.moveCount)
        assertEquals(12_345L, loadedSession.snapshot.elapsedActiveMs)
        assertEquals(listOf("A002"), loadedSession.activeArrows.sortedBy { it.orderIndex }.map { it.arrowId })
    }

    @Test
    fun completionAndProgressAdvanceAreAtomic() = runBlocking {
        val puzzle = puzzleDefinition()
        dao.savePuzzle(
            puzzle,
            listOf(
                PuzzleArrowEntity(puzzle.puzzleId, "A001", 0, 1, 0, "RIGHT", 2)
            )
        )
        dao.saveSession(
            SessionSnapshotEntity(puzzle.puzzleId, "8", 1, 500L, 0, 50L),
            emptyList()
        )

        val completed = CompletedLevelEntity(
            levelNumber = "8",
            puzzleId = puzzle.puzzleId,
            generatorVersion = 1,
            completedAtEpochMs = 75L,
            moveCount = 1,
            hintsUsed = 0,
            stars = 3
        )
        val progress = PlayerProgressEntity(
            currentLevelNumber = "9",
            highestUnlockedLevelNumber = "9",
            currentPuzzleId = null,
            updatedAtEpochMs = 75L
        )

        dao.completeLevelAndAdvance(completed, progress, puzzle.puzzleId)

        assertNull(dao.getLatestSession())
        assertEquals("9", dao.getPlayerProgress()!!.currentLevelNumber)
        assertEquals(3, dao.getCompletedLevel("8")!!.stars)
    }

    @Test
    fun recentFingerprintHistoryIsBoundedAndKeepsNewestEntries() = runBlocking {
        (1..4).forEach { index ->
            dao.insertRecentFingerprint(
                RecentPuzzleFingerprintEntity(
                    fingerprint = "hash-$index",
                    levelNumber = index.toString(),
                    generatorVersion = 1,
                    createdAtEpochMs = index.toLong()
                )
            )
        }

        dao.trimRecentFingerprints(2)
        val recent = dao.getRecentFingerprints(limit = 10, offset = 0)

        assertEquals(listOf("hash-4", "hash-3"), recent.map { it.fingerprint })
        assertFalse(dao.hasRecentFingerprint("hash-1"))
        assertTrue(dao.hasRecentFingerprint("hash-4"))
    }

    @Test
    fun atomicPuzzleInsertRejectsRecentDuplicateFingerprints() = runBlocking {
        val first = puzzleDefinition(levelNumber = "10")
        val firstFingerprint = RecentPuzzleFingerprintEntity(
            fingerprint = "same-layout-hash",
            levelNumber = "10",
            generatorVersion = 1,
            createdAtEpochMs = 10L
        )
        val firstArrow = PuzzleArrowEntity(
            puzzleId = first.puzzleId,
            arrowId = "A001",
            orderIndex = 0,
            headX = 1,
            headY = 0,
            direction = "RIGHT",
            length = 2
        )
        assertTrue(
            dao.savePuzzleIfFingerprintNovel(first, listOf(firstArrow), firstFingerprint, 500)
        )

        val second = puzzleDefinition(levelNumber = "11").copy(puzzleId = "puzzle-11")
        val secondFingerprint = firstFingerprint.copy(levelNumber = "11", createdAtEpochMs = 11L)
        val secondArrow = firstArrow.copy(puzzleId = second.puzzleId)
        assertFalse(
            dao.savePuzzleIfFingerprintNovel(second, listOf(secondArrow), secondFingerprint, 500)
        )
        assertNull(dao.getPuzzleByLevelAndVersion("11", 1))
        assertTrue(dao.hasRecentFingerprint("same-layout-hash"))
    }

    private fun puzzleDefinition(levelNumber: String = "7") = PuzzleDefinitionEntity(
        puzzleId = "puzzle-$levelNumber",
        levelNumber = levelNumber,
        generatorVersion = 1,
        randomSeed = 123L,
        seedRetryCounter = 0,
        boardWidth = 4,
        boardHeight = 4,
        targetArrowCount = 2,
        maxArrowLength = 2,
        solverNodeBudget = 50_000L,
        contentHash = "content-$levelNumber",
        createdAtEpochMs = 10L
    )
}

package com.gokuxedition.arrowlogic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface GameDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPuzzleDefinition(entity: PuzzleDefinitionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzleArrows(entities: List<PuzzleArrowEntity>)

    @Query("DELETE FROM puzzle_arrow WHERE puzzle_id = :puzzleId")
    suspend fun deletePuzzleArrows(puzzleId: String)

    @Transaction
    suspend fun savePuzzle(definition: PuzzleDefinitionEntity, arrows: List<PuzzleArrowEntity>) {
        upsertPuzzleDefinition(definition)
        deletePuzzleArrows(definition.puzzleId)
        insertPuzzleArrows(arrows)
    }

    @Transaction
    @Query(
        """
        SELECT * FROM puzzle_definition
        WHERE level_number = :levelNumber
          AND generator_version = :generatorVersion
          AND random_seed = :randomSeed
        LIMIT 1
        """
    )
    suspend fun getPuzzleByIdentity(
        levelNumber: String,
        generatorVersion: Int,
        randomSeed: Long
    ): PuzzleDefinitionWithArrows?

    @Transaction
    @Query("SELECT * FROM puzzle_definition WHERE puzzle_id = :puzzleId LIMIT 1")
    suspend fun getPuzzleById(puzzleId: String): PuzzleDefinitionWithArrows?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerProgress(entity: PlayerProgressEntity)

    @Query("SELECT * FROM player_progress WHERE singleton_id = 1 LIMIT 1")
    suspend fun getPlayerProgress(): PlayerProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletedLevel(entity: CompletedLevelEntity)

    @Query(
        """
        SELECT * FROM completed_level
        ORDER BY completed_at_epoch_ms DESC, level_number DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getCompletedLevels(limit: Int, offset: Int): List<CompletedLevelEntity>

    @Query("SELECT * FROM completed_level WHERE level_number = :levelNumber LIMIT 1")
    suspend fun getCompletedLevel(levelNumber: String): CompletedLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessionSnapshot(entity: SessionSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionActiveArrows(entities: List<SessionActiveArrowEntity>)

    @Query("DELETE FROM session_active_arrow WHERE puzzle_id = :puzzleId")
    suspend fun deleteSessionActiveArrows(puzzleId: String)

    @Query("DELETE FROM session_snapshot WHERE puzzle_id = :puzzleId")
    suspend fun deleteSessionSnapshot(puzzleId: String)

    @Transaction
    suspend fun saveSession(
        snapshot: SessionSnapshotEntity,
        activeArrows: List<SessionActiveArrowEntity>
    ) {
        upsertSessionSnapshot(snapshot)
        deleteSessionActiveArrows(snapshot.puzzleId)
        insertSessionActiveArrows(activeArrows)
    }

    @Transaction
    @Query(
        """
        SELECT * FROM session_snapshot
        ORDER BY updated_at_epoch_ms DESC
        LIMIT 1
        """
    )
    suspend fun getLatestSession(): SessionSnapshotWithActiveArrows?

    @Transaction
    suspend fun completeLevelAndAdvance(
        completedLevel: CompletedLevelEntity,
        progress: PlayerProgressEntity,
        puzzleId: String
    ) {
        upsertCompletedLevel(completedLevel)
        deleteSessionSnapshot(puzzleId)
        upsertPlayerProgress(progress)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecentFingerprint(entity: RecentPuzzleFingerprintEntity): Long

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM recent_puzzle_fingerprint
            WHERE fingerprint = :fingerprint
        )
        """
    )
    suspend fun hasRecentFingerprint(fingerprint: String): Boolean

    @Query(
        """
        DELETE FROM recent_puzzle_fingerprint
        WHERE fingerprint NOT IN (
            SELECT fingerprint FROM recent_puzzle_fingerprint
            ORDER BY created_at_epoch_ms DESC, fingerprint ASC
            LIMIT :maximumEntries
        )
        """
    )
    suspend fun trimRecentFingerprints(maximumEntries: Int)

    @Query(
        """
        SELECT * FROM recent_puzzle_fingerprint
        ORDER BY created_at_epoch_ms DESC, fingerprint ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getRecentFingerprints(limit: Int, offset: Int): List<RecentPuzzleFingerprintEntity>
}

package com.gokuxedition.arrowlogic.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "puzzle_definition",
    indices = [
        Index(value = ["level_number", "generator_version"], unique = true),
        Index(value = ["content_hash"])
    ]
)
data class PuzzleDefinitionEntity(
    @PrimaryKey
    @ColumnInfo(name = "puzzle_id")
    val puzzleId: String,
    @ColumnInfo(name = "level_number")
    val levelNumber: String,
    @ColumnInfo(name = "generator_version")
    val generatorVersion: Int,
    @ColumnInfo(name = "random_seed")
    val randomSeed: Long,
    @ColumnInfo(name = "seed_retry_counter")
    val seedRetryCounter: Int,
    @ColumnInfo(name = "board_width")
    val boardWidth: Int,
    @ColumnInfo(name = "board_height")
    val boardHeight: Int,
    @ColumnInfo(name = "target_arrow_count")
    val targetArrowCount: Int,
    @ColumnInfo(name = "max_arrow_length")
    val maxArrowLength: Int,
    @ColumnInfo(name = "solver_node_budget")
    val solverNodeBudget: Long,
    @ColumnInfo(name = "content_hash")
    val contentHash: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long
)

@Entity(
    tableName = "puzzle_arrow",
    primaryKeys = ["puzzle_id", "arrow_id"],
    foreignKeys = [
        ForeignKey(
            entity = PuzzleDefinitionEntity::class,
            parentColumns = ["puzzle_id"],
            childColumns = ["puzzle_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["puzzle_id"])]
)
data class PuzzleArrowEntity(
    @ColumnInfo(name = "puzzle_id")
    val puzzleId: String,
    @ColumnInfo(name = "arrow_id")
    val arrowId: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    @ColumnInfo(name = "head_x")
    val headX: Int,
    @ColumnInfo(name = "head_y")
    val headY: Int,
    val direction: String,
    val length: Int
)

data class PuzzleDefinitionWithArrows(
    @Embedded
    val definition: PuzzleDefinitionEntity,
    @Relation(
        parentColumn = "puzzle_id",
        entityColumn = "puzzle_id"
    )
    val arrows: List<PuzzleArrowEntity>
)

@Entity(
    tableName = "player_progress"
)
data class PlayerProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "singleton_id")
    val singletonId: Int = 1,
    @ColumnInfo(name = "current_level_number")
    val currentLevelNumber: String,
    @ColumnInfo(name = "highest_unlocked_level_number")
    val highestUnlockedLevelNumber: String,
    @ColumnInfo(name = "current_puzzle_id")
    val currentPuzzleId: String?,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "completed_level",
    indices = [Index(value = ["completed_at_epoch_ms"])]
)
data class CompletedLevelEntity(
    @PrimaryKey
    @ColumnInfo(name = "level_number")
    val levelNumber: String,
    @ColumnInfo(name = "puzzle_id")
    val puzzleId: String,
    @ColumnInfo(name = "generator_version")
    val generatorVersion: Int,
    @ColumnInfo(name = "completed_at_epoch_ms")
    val completedAtEpochMs: Long,
    @ColumnInfo(name = "move_count")
    val moveCount: Int,
    @ColumnInfo(name = "hints_used")
    val hintsUsed: Int,
    val stars: Int
)

@Entity(
    tableName = "session_snapshot",
    foreignKeys = [
        ForeignKey(
            entity = PuzzleDefinitionEntity::class,
            parentColumns = ["puzzle_id"],
            childColumns = ["puzzle_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["puzzle_id"], unique = true),
        Index(value = ["updated_at_epoch_ms"])
    ]
)
data class SessionSnapshotEntity(
    @PrimaryKey
    @ColumnInfo(name = "puzzle_id")
    val puzzleId: String,
    @ColumnInfo(name = "level_number")
    val levelNumber: String,
    @ColumnInfo(name = "move_count")
    val moveCount: Int,
    @ColumnInfo(name = "elapsed_active_ms")
    val elapsedActiveMs: Long,
    @ColumnInfo(name = "hints_used")
    val hintsUsed: Int,
    @ColumnInfo(name = "updated_at_epoch_ms")
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "session_active_arrow",
    primaryKeys = ["puzzle_id", "arrow_id"],
    foreignKeys = [
        ForeignKey(
            entity = SessionSnapshotEntity::class,
            parentColumns = ["puzzle_id"],
            childColumns = ["puzzle_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["puzzle_id"])]
)
data class SessionActiveArrowEntity(
    @ColumnInfo(name = "puzzle_id")
    val puzzleId: String,
    @ColumnInfo(name = "arrow_id")
    val arrowId: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)

data class SessionSnapshotWithActiveArrows(
    @Embedded
    val snapshot: SessionSnapshotEntity,
    @Relation(
        parentColumn = "puzzle_id",
        entityColumn = "puzzle_id"
    )
    val activeArrows: List<SessionActiveArrowEntity>
)

@Entity(
    tableName = "recent_puzzle_fingerprint",
    indices = [Index(value = ["created_at_epoch_ms"])]
)
data class RecentPuzzleFingerprintEntity(
    @PrimaryKey
    val fingerprint: String,
    @ColumnInfo(name = "level_number")
    val levelNumber: String,
    @ColumnInfo(name = "generator_version")
    val generatorVersion: Int,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long
)

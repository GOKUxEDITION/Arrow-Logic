package com.gokuxedition.arrowlogic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PuzzleDefinitionEntity::class,
        PuzzleArrowEntity::class,
        PlayerProgressEntity::class,
        CompletedLevelEntity::class,
        SessionSnapshotEntity::class,
        SessionActiveArrowEntity::class,
        RecentPuzzleFingerprintEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class ArrowLogicDatabase : RoomDatabase() {
    abstract fun gameDataDao(): GameDataDao

    companion object {
        @Volatile
        private var instance: ArrowLogicDatabase? = null

        fun getInstance(context: Context): ArrowLogicDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArrowLogicDatabase::class.java,
                    "arrow_logic.db"
                ).build().also { instance = it }
            }
    }
}

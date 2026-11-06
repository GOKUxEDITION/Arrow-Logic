package com.gokuxedition.arrowlogic.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.arrowLogicPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "arrow_logic_preferences"
)

data class UserGamePreferences(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val reducedMotionEnabled: Boolean = false,
    val tutorialCompleted: Boolean = false
)

class UserPreferencesRepository(context: Context) {
    private val store = context.applicationContext.arrowLogicPreferences

    val preferences: Flow<UserGamePreferences> = store.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { values ->
            UserGamePreferences(
                soundEnabled = values[Keys.soundEnabled] ?: true,
                musicEnabled = values[Keys.musicEnabled] ?: false,
                vibrationEnabled = values[Keys.vibrationEnabled] ?: true,
                reducedMotionEnabled = values[Keys.reducedMotionEnabled] ?: false,
                tutorialCompleted = values[Keys.tutorialCompleted] ?: false
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        store.edit { it[Keys.soundEnabled] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        store.edit { it[Keys.musicEnabled] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        store.edit { it[Keys.vibrationEnabled] = enabled }
    }

    suspend fun setReducedMotionEnabled(enabled: Boolean) {
        store.edit { it[Keys.reducedMotionEnabled] = enabled }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        store.edit { it[Keys.tutorialCompleted] = completed }
    }

    private object Keys {
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val musicEnabled = booleanPreferencesKey("music_enabled")
        val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
        val reducedMotionEnabled = booleanPreferencesKey("reduced_motion_enabled")
        val tutorialCompleted = booleanPreferencesKey("tutorial_completed")
    }
}

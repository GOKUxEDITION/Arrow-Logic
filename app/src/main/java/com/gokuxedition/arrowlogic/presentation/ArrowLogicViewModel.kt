package com.gokuxedition.arrowlogic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokuxedition.arrowlogic.data.repository.ArrowLogicRepository
import com.gokuxedition.arrowlogic.domain.ConstructivePuzzleGenerator
import com.gokuxedition.arrowlogic.domain.DifficultyProfile
import com.gokuxedition.arrowlogic.domain.GameEngine
import com.gokuxedition.arrowlogic.domain.GameState
import com.gokuxedition.arrowlogic.domain.GenerationResult
import com.gokuxedition.arrowlogic.domain.LevelNumber
import com.gokuxedition.arrowlogic.domain.MoveResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GameplayMessage {
    data object Preparing : GameplayMessage
    data class Generating(val level: String) : GameplayMessage
    data object Verified : GameplayMessage
    data object GenerationFailed : GameplayMessage
    data class GenerationRejected(val details: String) : GameplayMessage
    data class Complete(val moves: Int) : GameplayMessage
    data class ArrowEscaped(val remaining: Int) : GameplayMessage
    data object ArrowBlocked : GameplayMessage
    data object ArrowUnknown : GameplayMessage
    data object Reset : GameplayMessage
    data object PersistenceWarning : GameplayMessage
}

data class ArrowLogicUiState(
    val level: LevelNumber = LevelNumber.FIRST,
    val puzzleId: String? = null,
    val isGenerating: Boolean = true,
    val isPersisting: Boolean = false,
    val gameState: GameState? = null,
    val message: GameplayMessage = GameplayMessage.Preparing,
    val generationError: String? = null
)

class ArrowLogicViewModel(
    private val repository: ArrowLogicRepository
) : ViewModel() {
    private val campaignSeed = "arrow-logic-offline-campaign-v1"
    private val mutableUiState = MutableStateFlow(ArrowLogicUiState())
    val uiState: StateFlow<ArrowLogicUiState> = mutableUiState.asStateFlow()

    private var generationJob: Job? = null
    private var sessionWriteJob: Job? = null

    init {
        restoreOrStart()
    }

    private fun restoreOrStart() {
        viewModelScope.launch {
            try {
                val restored = withContext(Dispatchers.IO) {
                    repository.restoreLatestSession()
                }
                if (restored != null) {
                    val game = restored.toGameState()
                    mutableUiState.value = ArrowLogicUiState(
                        level = restored.level,
                        puzzleId = restored.puzzleId,
                        isGenerating = false,
                        gameState = game,
                        message = if (game.isComplete) {
                            GameplayMessage.Complete(game.moveCount)
                        } else {
                            GameplayMessage.Verified
                        }
                    )
                    if (game.isComplete) {
                        persistSession(restored.puzzleId, restored.level, game)
                    }
                } else {
                    val currentLevel = withContext(Dispatchers.IO) {
                        repository.getCurrentLevel()
                    }
                    loadLevel(currentLevel)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isGenerating = false,
                    message = GameplayMessage.GenerationFailed,
                    generationError = error.message ?: error.javaClass.simpleName
                )
            }
        }
    }

    fun loadLevel(level: LevelNumber) {
        generationJob?.cancel()
        sessionWriteJob?.cancel()
        mutableUiState.value = ArrowLogicUiState(
            level = level,
            isGenerating = true,
            message = GameplayMessage.Generating(level.decimal)
        )
        generationJob = viewModelScope.launch {
            try {
                val stored = withContext(Dispatchers.IO) {
                    repository.loadPuzzle(level)
                }
                if (stored != null) {
                    val game = GameEngine.initialState(stored.board)
                    withContext(Dispatchers.IO) {
                        repository.saveSession(stored.puzzleId, level, game)
                    }
                    mutableUiState.value = ArrowLogicUiState(
                        level = level,
                        puzzleId = stored.puzzleId,
                        isGenerating = false,
                        gameState = game,
                        message = GameplayMessage.Verified
                    )
                    return@launch
                }

                val profile = DifficultyProfile.forLevel(level)
                var lastFailure: GenerationResult.Failed? = null
                repeat(MAX_SEED_RETRIES) { retryCounter ->
                    val result = withContext(Dispatchers.Default) {
                        ConstructivePuzzleGenerator.generate(
                            level = level,
                            rootSeed = campaignSeed,
                            profile = profile,
                            seedRetryCounter = retryCounter
                        )
                    }
                    when (result) {
                        is GenerationResult.Failed -> {
                            lastFailure = result
                        }
                        is GenerationResult.Generated -> {
                            val puzzleId = withContext(Dispatchers.IO) {
                                repository.persistGeneratedPuzzle(
                                    level = level,
                                    profile = profile,
                                    board = result.board,
                                    fingerprint = result.fingerprint,
                                    randomSeed = result.seed,
                                    seedRetryCounter = result.seedRetryCounter
                                )
                            }
                            // A null ID means the atomic database gate rejected a recent duplicate.
                            if (puzzleId == null) return@repeat

                            val game = GameEngine.initialState(result.board)
                            withContext(Dispatchers.IO) {
                                repository.saveSession(puzzleId, level, game)
                            }
                            mutableUiState.value = ArrowLogicUiState(
                                level = level,
                                puzzleId = puzzleId,
                                isGenerating = false,
                                gameState = game,
                                message = GameplayMessage.Verified
                            )
                            return@launch
                        }
                    }
                }

                val details = lastFailure?.let {
                    "No candidate passed all verification gates. Last result: ${it.reason} after ${it.attemptsUsed} board attempts across $MAX_SEED_RETRIES seed variants."
                } ?: "Recent puzzle fingerprints repeated across $MAX_SEED_RETRIES deterministic seed variants."
                mutableUiState.value = mutableUiState.value.copy(
                    isGenerating = false,
                    message = GameplayMessage.GenerationRejected(details)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isGenerating = false,
                    message = GameplayMessage.GenerationFailed,
                    generationError = error.message ?: error.javaClass.simpleName
                )
            }
        }
    }

    fun onArrowTapped(arrowId: String) {
        val current = mutableUiState.value
        if (current.isPersisting) return
        val game = current.gameState ?: return
        val puzzleId = current.puzzleId ?: return
        when (val result = GameEngine.tap(game, arrowId)) {
            is MoveResult.Escaped -> {
                mutableUiState.value = current.copy(
                    gameState = result.state,
                    message = if (result.state.isComplete) {
                        GameplayMessage.Complete(result.state.moveCount)
                    } else {
                        GameplayMessage.ArrowEscaped(result.state.remainingArrows.size)
                    }
                )
                persistSession(puzzleId, current.level, result.state)
            }
            is MoveResult.Blocked -> {
                mutableUiState.value = current.copy(message = GameplayMessage.ArrowBlocked)
            }
            MoveResult.UnknownArrow -> {
                mutableUiState.value = current.copy(message = GameplayMessage.ArrowUnknown)
            }
        }
    }

    fun resetLevel() {
        val current = mutableUiState.value
        if (current.isPersisting) return
        val board = current.gameState?.originalBoard ?: return
        val puzzleId = current.puzzleId ?: return
        val reset = GameEngine.initialState(board)
        mutableUiState.value = current.copy(
            gameState = reset,
            message = GameplayMessage.Reset
        )
        persistSession(puzzleId, current.level, reset)
    }

    fun nextLevel() {
        val current = mutableUiState.value
        if (current.isGenerating || current.isPersisting ||
            current.message == GameplayMessage.PersistenceWarning ||
            current.gameState?.isComplete != true
        ) return
        loadLevel(current.level.next())
    }

    fun retrySave() {
        val current = mutableUiState.value
        if (current.isPersisting || current.message != GameplayMessage.PersistenceWarning) return
        val puzzleId = current.puzzleId ?: return
        val game = current.gameState ?: return
        persistSession(puzzleId, current.level, game)
    }

    private fun persistSession(puzzleId: String, level: LevelNumber, game: GameState) {
        sessionWriteJob?.cancel()
        val before = mutableUiState.value
        if (before.puzzleId == puzzleId) {
            mutableUiState.value = before.copy(isPersisting = true)
        }
        sessionWriteJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.saveSession(puzzleId, level, game)
                    if (game.isComplete) {
                        repository.completeLevel(
                            puzzleId = puzzleId,
                            level = level,
                            moveCount = game.moveCount,
                            hintsUsed = 0
                        )
                    }
                }
                val latest = mutableUiState.value
                if (latest.puzzleId == puzzleId && latest.gameState == game) {
                    mutableUiState.value = latest.copy(isPersisting = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                val latest = mutableUiState.value
                if (latest.puzzleId == puzzleId && latest.gameState == game) {
                    mutableUiState.value = latest.copy(
                        isPersisting = false,
                        message = GameplayMessage.PersistenceWarning
                    )
                }
            }
        }
    }

    private companion object {
        const val MAX_SEED_RETRIES = 8
    }

}

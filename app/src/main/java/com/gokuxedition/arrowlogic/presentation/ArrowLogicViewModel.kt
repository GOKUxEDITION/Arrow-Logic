package com.gokuxedition.arrowlogic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokuxedition.arrowlogic.domain.ConstructivePuzzleGenerator
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
}

data class ArrowLogicUiState(
    val level: LevelNumber = LevelNumber.FIRST,
    val isGenerating: Boolean = true,
    val gameState: GameState? = null,
    val message: GameplayMessage = GameplayMessage.Preparing,
    val generationError: String? = null
)

class ArrowLogicViewModel : ViewModel() {
    private val campaignSeed = "arrow-logic-offline-campaign-v1"
    private val mutableUiState = MutableStateFlow(ArrowLogicUiState())
    val uiState: StateFlow<ArrowLogicUiState> = mutableUiState.asStateFlow()
    private var generationJob: Job? = null

    init {
        loadLevel(LevelNumber.FIRST)
    }

    fun loadLevel(level: LevelNumber) {
        generationJob?.cancel()
        mutableUiState.value = ArrowLogicUiState(
            level = level,
            isGenerating = true,
            message = GameplayMessage.Generating(level.decimal)
        )
        generationJob = viewModelScope.launch {
            val result = try {
                withContext(Dispatchers.Default) {
                    ConstructivePuzzleGenerator.generate(level, campaignSeed)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableUiState.value = mutableUiState.value.copy(
                    isGenerating = false,
                    message = GameplayMessage.GenerationFailed,
                    generationError = error.message ?: error.javaClass.simpleName
                )
                return@launch
            }

            when (result) {
                is GenerationResult.Generated -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isGenerating = false,
                        gameState = GameEngine.initialState(result.board),
                        message = GameplayMessage.Verified
                    )
                }
                is GenerationResult.Failed -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isGenerating = false,
                        message = GameplayMessage.GenerationRejected(
                            "${result.reason} after ${result.attemptsUsed} attempts"
                        )
                    )
                }
            }
        }
    }

    fun onArrowTapped(arrowId: String) {
        val current = mutableUiState.value
        val game = current.gameState ?: return
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
        val board = current.gameState?.originalBoard ?: return
        mutableUiState.value = current.copy(
            gameState = GameEngine.initialState(board),
            message = GameplayMessage.Reset
        )
    }

    fun nextLevel() {
        val current = mutableUiState.value
        if (current.isGenerating || current.gameState?.isComplete != true) return
        loadLevel(current.level.next())
    }
}

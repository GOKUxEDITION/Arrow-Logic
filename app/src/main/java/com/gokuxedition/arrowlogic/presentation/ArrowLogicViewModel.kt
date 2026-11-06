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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ArrowLogicUiState(
    val level: LevelNumber = LevelNumber.FIRST,
    val isGenerating: Boolean = true,
    val gameState: GameState? = null,
    val message: String = "Preparing your puzzle",
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
            message = "Generating and verifying level ${level.decimal}"
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
                    message = "Puzzle generation failed safely.",
                    generationError = error.message ?: error.javaClass.simpleName
                )
                return@launch
            }

            when (result) {
                is GenerationResult.Generated -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isGenerating = false,
                        gameState = GameEngine.initialState(result.board),
                        message = "Level verified. Tap an arrow whose path to the edge is clear."
                    )
                }
                is GenerationResult.Failed -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isGenerating = false,
                        message = "This level could not pass every verification gate.",
                        generationError = "${result.reason} after ${result.attemptsUsed} attempts"
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
                        "Puzzle complete. You cleared every arrow in ${result.state.moveCount} moves."
                    } else {
                        "Arrow escaped. ${result.state.remainingArrows.size} remaining."
                    }
                )
            }
            is MoveResult.Blocked -> {
                mutableUiState.value = current.copy(
                    message = "That arrow is blocked. Clear the arrow in its forward path first."
                )
            }
            MoveResult.UnknownArrow -> {
                mutableUiState.value = current.copy(message = "That arrow is no longer on the board.")
            }
        }
    }

    fun resetLevel() {
        val current = mutableUiState.value
        val board = current.gameState?.originalBoard ?: return
        mutableUiState.value = current.copy(
            gameState = GameEngine.initialState(board),
            message = "Level reset. Find a clear escape path."
        )
    }

    fun nextLevel() {
        val current = mutableUiState.value
        if (current.isGenerating || current.gameState?.isComplete != true) return
        loadLevel(current.level.next())
    }
}

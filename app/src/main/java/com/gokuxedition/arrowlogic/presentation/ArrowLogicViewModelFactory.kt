package com.gokuxedition.arrowlogic.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gokuxedition.arrowlogic.data.repository.ArrowLogicRepository

class ArrowLogicViewModelFactory(
    private val repository: ArrowLogicRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ArrowLogicViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ArrowLogicViewModel(repository) as T
    }
}

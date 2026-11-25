package com.ortiz.avisoscampus.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ortiz.avisoscampus.data.AvisosRepositoryMemoria

// Crea el AvisosViewModel con su Repository (el ViewModel solo ve la interface)
val AvisosViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        AvisosViewModel(AvisosRepositoryMemoria())
    }
}

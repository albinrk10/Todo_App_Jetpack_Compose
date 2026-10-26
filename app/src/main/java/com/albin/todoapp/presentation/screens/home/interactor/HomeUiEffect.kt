package com.albin.todoapp.presentation.screens.home.interactor

import com.albin.todoapp.core.mvi.UiEffect

sealed interface HomeUiEffect : UiEffect {
    data object UpdatedTask : HomeUiEffect
    data object DeletedTask : HomeUiEffect
    data object AllTaskDeleted : HomeUiEffect
}

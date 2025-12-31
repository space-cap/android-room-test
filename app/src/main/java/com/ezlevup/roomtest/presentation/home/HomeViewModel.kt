package com.ezlevup.roomtest.presentation.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(): ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()


}


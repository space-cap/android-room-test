package com.ezlevup.roomtest.presentation.home

import com.ezlevup.roomtest.domain.User

data class HomeState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
)

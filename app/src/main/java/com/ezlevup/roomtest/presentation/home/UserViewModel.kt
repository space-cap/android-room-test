package com.ezlevup.roomtest.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ezlevup.roomtest.RoomTestApplication
import com.ezlevup.roomtest.domain.User
import com.ezlevup.roomtest.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _state.update { it.copy(users = users) }
            }
        }
    }

    fun addUser(name: String, age: Int) {
        viewModelScope.launch { userRepository.insertUser(User(name = name, age = age)) }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch { userRepository.deleteUser(user) }
    }

    fun updateUser(user: User) {
        viewModelScope.launch { userRepository.updateUser(user) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as RoomTestApplication)
                UserViewModel(application.userRepository)
            }
        }
    }
}

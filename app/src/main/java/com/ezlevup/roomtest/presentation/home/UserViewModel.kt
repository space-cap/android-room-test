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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    val users: StateFlow<List<User>> =
            userRepository
                    .getAllUsers()
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

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

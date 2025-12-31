package com.ezlevup.roomtest.domain

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(id: Int): User?
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun updateUser(user: User)
}

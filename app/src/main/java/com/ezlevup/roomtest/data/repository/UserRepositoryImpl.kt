package com.ezlevup.roomtest.data.repository

import com.ezlevup.roomtest.data.local.UserDao
import com.ezlevup.roomtest.data.local.toDomain
import com.ezlevup.roomtest.data.local.toEntity
import com.ezlevup.roomtest.domain.User
import com.ezlevup.roomtest.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun deleteUser(user: User) {
        userDao.deleteUser(user.toEntity())
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
    }
}

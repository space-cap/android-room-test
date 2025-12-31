package com.ezlevup.roomtest

import android.app.Application
import androidx.room.Room
import com.ezlevup.roomtest.data.local.AppDatabase
import com.ezlevup.roomtest.data.repository.UserRepositoryImpl
import com.ezlevup.roomtest.domain.UserRepository

class RoomTestApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "room_test_db").build()
    }

    val userRepository: UserRepository by lazy { UserRepositoryImpl(database.userDao()) }
}

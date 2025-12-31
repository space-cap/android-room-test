package com.ezlevup.roomtest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ezlevup.roomtest.domain.User

@Entity(tableName = "users")
data class UserEntity(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val name: String,
        val age: Int
)

// Extension functions for mapping
fun UserEntity.toDomain(): User {
    return User(id = id, name = name, age = age)
}

fun User.toEntity(): UserEntity {
    return UserEntity(id = id, name = name, age = age)
}

# Room Library를 이용한 안드로이드 CRUD 완벽 가이드

안드로이드 앱에서 데이터를 로컬에 저장하고 관리하기 위해 사용하는 Room 라이브러리의 CRUD(Create, Read, Update, Delete) 구현 방법을 단계별로 설명합니다. 초보자도 쉽게 따라 할 수 있도록 구성했습니다.

## 목차
1. [설정 (Dependencies)](#1-설정-dependencies)
2. [Data Layer: Entity (테이블 정의)](#2-data-layer-entity-테이블-정의)
3. [Data Layer: DAO (데이터 접근 객체)](#3-data-layer-dao-데이터-접근-객체)
4. [Data Layer: Database (데이터베이스 설정)](#4-data-layer-database-데이터베이스-설정)
5. [Repository Layer (데이터 관리)](#5-repository-layer-데이터-관리)
6. [DI & Application (의존성 주입 및 초기화)](#6-di--application-의존성-주입-및-초기화)
7. [UI Layer: ViewModel (데이터 연결)](#7-ui-layer-viewmodel-데이터-연결)

---

## 1. 설정 (Dependencies)

먼저 `app/build.gradle.kts` 파일에 Room 라이브러리와 KSP(Kotlin Symbol Processing) 플러그인을 추가해야 합니다.

### `libs.versions.toml` (권장)
버전 관리 파일에 다음과 같이 정의되어 있다고 가정합니다 (최신 버전 확인 필요).
```toml
[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

### `app/build.gradle.kts`
```kotlin
plugins {
    // ... 기존 플러그인들
    alias(libs.plugins.ksp) // KSP 플러그인 추가
}

dependencies {
    // ...
    // Room 라이브러리 추가
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Kotlin 확장 기능 (Coroutines, Flow 등 지원)
    ksp(libs.androidx.room.compiler) // 컴파일러 (어노테이션 처리용)
}
```

---

## 2. Data Layer: Entity (테이블 정의)

데이터베이스에 저장될 테이블 구조를 정의하는 클래스입니다. `@Entity` 어노테이션을 사용합니다.

**`data/local/UserEntity.kt`**
```kotlin
package com.ezlevup.roomtest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ezlevup.roomtest.domain.User

// @Entity: 이 클래스가 데이터베이스의 테이블임을 나타냅니다.
// tableName = "users": 실제 DB에 생성될 테이블 이름을 "users"로 지정합니다.
@Entity(tableName = "users")
data class UserEntity(
    // @PrimaryKey: 각 행(row)을 구분하는 고유한 키(ID)입니다.
    // autoGenerate = true: ID를 직접 넣지 않아도 DB가 자동으로 1씩 증가시켜 생성해줍니다.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // 테이블의 컬럼(Column)들이 됩니다.
    val name: String,
    val age: Int
)

// Tip: Clean Architecture를 위해 DB용 Entity와 앱 내부용 Domain Model을 분리하여 사용하는 것이 좋습니다.
// 아래 함수들은 그 변환을 담당합니다.

fun UserEntity.toDomain(): User {
    return User(id = id, name = name, age = age)
}

fun User.toEntity(): UserEntity {
    return UserEntity(id = id, name = name, age = age)
}
```

---

## 3. Data Layer: DAO (데이터 접근 객체)

실제 데이터베이스에 접근하여 CRUD 작업을 수행하는 메서드들을 정의하는 인터페이스입니다. `@Dao` 어노테이션을 사용합니다.

**`data/local/UserDao.kt`**
```kotlin
package com.ezlevup.roomtest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // C: Create (데이터 추가)
    // OnConflictStrategy.REPLACE: 만약 같은 ID의 데이터가 이미 있다면 덮어씌웁니다.
    // suspend 키워드: 데이터베이스 작업은 오래 걸릴 수 있으므로 코루틴 내에서 비동기로 실행되어야 합니다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // R: Read (데이터 조회 - 전체)
    // Flow<List<UserEntity>>: 데이터가 변경될 때마다 자동으로 새로운 리스트를 방출(emit)해줍니다. 실시간 업데이트에 유용합니다.
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // R: Read (데이터 조회 - 단건)
    // :id 와 같이 콜론(:)을 사용하여 함수의 파라미터를 SQL 쿼리에 바인딩할 수 있습니다.
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    // U: Update (데이터 수정)
    // 전달받은 객체의 @PrimaryKey(id)와 일치하는 행을 찾아 나머지 데이터를 수정합니다.
    @Update
    suspend fun updateUser(user: UserEntity)

    // D: Delete (데이터 삭제)
    // 전달받은 객체와 일치하는 행을 삭제합니다.
    @Delete
    suspend fun deleteUser(user: UserEntity)
}
```

---

## 4. Data Layer: Database (데이터베이스 설정)

Room 데이터베이스의 메인 진입점입니다. `RoomDatabase`를 상속받는 추상 클래스로 정의합니다.

**`data/local/AppDatabase.kt`**
```kotlin
package com.ezlevup.roomtest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// @Database: 데이터베이스 설정을 정의합니다.
// entities: 이 데이터베이스에 포함될 Entity 목록입니다.
// version: DB 구조가 바뀔 때마다 올려줘야 하는 버전 번호입니다.
// exportSchema = false: 스키마 버전 히스토리 백업 기능을 끕니다 (테스트용이므로 false).
@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // DAO를 반환하는 추상 메서드를 정의합니다. Room이 자동으로 구현체를 만들어줍니다.
    abstract fun userDao(): UserDao
}
```

---

## 5. Repository Layer (데이터 관리)

앱의 나머지 부분(UI 등)이 데이터의 출처(DB, 네트워크 등)를 모르도록 추상화하는 계층입니다.

**`domain/UserRepository.kt` (Interface)**
```kotlin
package com.ezlevup.roomtest.domain

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(id: Int): User?
    suspend fun insertUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun updateUser(user: User)
}
```

**`data/repository/UserRepositoryImpl.kt` (Implementation)**
```kotlin
package com.ezlevup.roomtest.data.repository

import com.ezlevup.roomtest.data.local.UserDao
import com.ezlevup.roomtest.data.local.toDomain
import com.ezlevup.roomtest.data.local.toEntity
import com.ezlevup.roomtest.domain.User
import com.ezlevup.roomtest.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DAO를 주입받아 실제 DB 작업을 수행하고, Entity <-> Domain Model 변환을 담당합니다.
class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    
    // Flow의 map 기능을 이용해 DB의 Entity 리스트를 도메인 모델 리스트로 변환하여 내보냅니다.
    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities -> 
            entities.map { it.toDomain() } 
        }
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
```

---

## 6. DI & Application (의존성 주입 및 초기화)

앱 전체에서 데이터베이스 인스턴스는 하나만 있어야 하므로(Singleton), `Application` 클래스에서 생성하여 관리합니다.

**`RoomTestApplication.kt`**
```kotlin
package com.ezlevup.roomtest

import android.app.Application
import androidx.room.Room
import com.ezlevup.roomtest.data.local.AppDatabase
import com.ezlevup.roomtest.data.repository.UserRepositoryImpl
import com.ezlevup.roomtest.domain.UserRepository

class RoomTestApplication : Application() {
    // by lazy: 실제로 처음 사용할 때 초기화합니다.
    
    // 데이터베이스 빌더를 이용해 DB 인스턴스를 생성합니다.
    // "room_test_db": 디바이스에 저장될 실제 파일 이름입니다.
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext, 
            AppDatabase::class.java, 
            "room_test_db"
        ).build()
    }

    // Repository 인스턴스도 여기서 생성하여 앱 전역에서 사용할 수 있게 합니다.
    val userRepository: UserRepository by lazy { 
        UserRepositoryImpl(database.userDao()) 
    }
}
```
*주의: `AndroidManifest.xml`의 `<application>` 태그에 `android:name=".RoomTestApplication"` 속성을 꼭 추가해야 이 클래스가 실행됩니다.*

---

## 7. UI Layer: ViewModel (데이터 연결)

UI와 데이터를 연결해주는 ViewModel입니다. Repository를 통해 데이터를 요청합니다.

**`presentation/home/UserViewModel.kt`**
```kotlin
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
    
    // UI 상태를 관리하는 StateFlow입니다.
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        // ViewModel이 생성될 때 데이터를 관찰(Observe)하기 시작합니다.
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                // 새로운 유저 리스트가 올 때마다 UI 상태를 업데이트합니다.
                _state.update { it.copy(users = users) }
            }
        }
    }

    // 데이터 추가 (Create)
    fun addUser(name: String, age: Int) {
        viewModelScope.launch {
            userRepository.insertUser(User(name = name, age = age))
        }
    }

    // 데이터 삭제 (Delete)
    fun deleteUser(user: User) {
        viewModelScope.launch {
            userRepository.deleteUser(user)
        }
    }

    // 데이터 수정 (Update)
    fun updateUser(user: User) {
        viewModelScope.launch {
            userRepository.updateUser(user)
        }
    }

    // ViewModel Factory 정의
    // Application 클래스에서 생성해둔 repository를 주입받아 ViewModel을 생성합니다.
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as RoomTestApplication)
                UserViewModel(application.userRepository)
            }
        }
    }
}
```

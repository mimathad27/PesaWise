package com.pesawise.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserAccount)
}

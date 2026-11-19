package com.pesawise.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY dateMillis DESC, id DESC")
    fun observeForUser(userId: String): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(expense: ExpenseEntity)

    @Query("UPDATE expenses SET title = :title, amount = :amount, category = :category, currencyCode = :currencyCode WHERE id = :id AND userId = :userId")
    suspend fun updateForUser(
        id: Long,
        userId: String,
        title: String,
        amount: Double,
        category: String,
        currencyCode: String
    ): Int

    @Query("DELETE FROM expenses WHERE id = :id AND userId = :userId")
    suspend fun deleteForUser(id: Long, userId: String): Int
}

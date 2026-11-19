package com.pesawise.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM incomes WHERE userId = :userId ORDER BY dateMillis DESC, id DESC")
    fun observeIncomes(userId: String): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id AND userId = :userId")
    suspend fun deleteIncome(id: Long, userId: String)

    @Query("SELECT * FROM subscriptions WHERE userId = :userId ORDER BY active DESC, nextChargeMillis ASC")
    fun observeSubscriptions(userId: String): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET active = :active WHERE id = :id AND userId = :userId")
    suspend fun setSubscriptionActive(id: Long, userId: String, active: Boolean)

    @Query("DELETE FROM subscriptions WHERE id = :id AND userId = :userId")
    suspend fun deleteSubscription(id: Long, userId: String)
}

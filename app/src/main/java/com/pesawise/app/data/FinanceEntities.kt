package com.pesawise.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "incomes", indices = [Index(value = ["userId"])])
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val dateMillis: Long,
    val currencyCode: String
)

@Entity(tableName = "subscriptions", indices = [Index(value = ["userId"])])
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val currencyCode: String,
    val billingCycle: String,
    val nextChargeMillis: Long,
    val active: Boolean = true
)

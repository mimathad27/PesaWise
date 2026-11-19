package com.pesawise.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses", indices = [Index(value = ["userId"])])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long,
    val currencyCode: String
) {
    fun toExpense() = Expense(
        id = id,
        title = title,
        amount = amount,
        category = runCatching { ExpenseCategory.valueOf(category) }.getOrDefault(ExpenseCategory.TRANSFERS),
        dateMillis = dateMillis,
        currencyCode = currencyCode
    )

    companion object {
        fun from(userId: String, expense: Expense) = ExpenseEntity(
            id = expense.id,
            userId = userId,
            title = expense.title,
            amount = expense.amount,
            category = expense.category.name,
            dateMillis = expense.dateMillis,
            currencyCode = expense.currencyCode
        )
    }
}

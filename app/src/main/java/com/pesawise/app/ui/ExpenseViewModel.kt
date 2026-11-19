package com.pesawise.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pesawise.app.data.Expense
import com.pesawise.app.data.ExpenseCategory
import com.pesawise.app.data.ExpenseDao
import com.pesawise.app.data.ExpenseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseDao: ExpenseDao,
    private val userId: String
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = expenseDao.observeForUser(userId)
        .map { rows -> rows.map(ExpenseEntity::toExpense) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _filter = MutableStateFlow<ExpenseCategory?>(null)
    val filter: StateFlow<ExpenseCategory?> = _filter.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val visibleExpenses: StateFlow<List<Expense>> =
        combine(expenses, _filter) { list, filter ->
            list.filter { filter == null || it.category == filter }
                .sortedByDescending { it.dateMillis }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(category: ExpenseCategory?) { _filter.value = category }
    fun clearMessage() { _message.value = null }

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, currencyCode: String) {
        viewModelScope.launch {
            runCatching {
                expenseDao.insert(
                    ExpenseEntity.from(
                        userId,
                        Expense(0, title.trim(), amount, category, currencyCode = currencyCode)
                    )
                )
            }.onFailure { _message.value = "Could not save this expense. Please try again." }
        }
    }

    fun updateExpense(expense: Expense, title: String, amount: Double, category: ExpenseCategory, currencyCode: String) {
        viewModelScope.launch {
            runCatching {
                expenseDao.updateForUser(
                    id = expense.id,
                    userId = userId,
                    title = title.trim(),
                    amount = amount,
                    category = category.name,
                    currencyCode = currencyCode
                )
            }.onSuccess { updated ->
                if (updated == 0) _message.value = "This expense is no longer available."
            }.onFailure { _message.value = "Could not update this expense. Please try again." }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            runCatching { expenseDao.deleteForUser(expense.id, userId) }
                .onSuccess { deleted ->
                    if (deleted == 0) _message.value = "This expense is no longer available."
                }
                .onFailure { _message.value = "Could not delete this expense. Please try again." }
        }
    }
}

class ExpenseViewModelFactory(
    private val expenseDao: ExpenseDao,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ExpenseViewModel(expenseDao, userId) as T
}

package com.pesawise.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesawise.app.data.Expense
import com.pesawise.app.data.ExpenseCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {

    private val day = 24L * 60 * 60 * 1000
    private var nextId = 100L

    private val _expenses = MutableStateFlow(
        listOf(
            Expense(1, "Monthly Rent", 25000.0, ExpenseCategory.HOUSING, System.currentTimeMillis() - 5 * day),
            Expense(2, "Naivas Groceries", 3200.0, ExpenseCategory.FOOD, System.currentTimeMillis() - 3 * day),
            Expense(3, "KPLC Tokens", 1500.0, ExpenseCategory.UTILITIES, System.currentTimeMillis() - 2 * day),
            Expense(4, "Uber to Work", 650.0, ExpenseCategory.TRANSPORT, System.currentTimeMillis() - day)
        )
    )
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // null = "All"
    private val _filter = MutableStateFlow<ExpenseCategory?>(null)
    val filter: StateFlow<ExpenseCategory?> = _filter.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    val visibleExpenses: StateFlow<List<Expense>> =
        combine(_expenses, _filter) { list, filter ->
            list.filter { filter == null || it.category == filter }
                .sortedByDescending { it.dateMillis }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(category: ExpenseCategory?) { _filter.value = category }

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, currencyCode: String = "KES") {
        _expenses.value = _expenses.value + Expense(nextId++, title.trim(), amount, category, currencyCode = currencyCode)
    }

    /** Simulates importing a bank / M-Pesa statement and auto-categorizing it. */
    fun importStatement() {
        if (_isImporting.value) return
        viewModelScope.launch {
            _isImporting.value = true
            delay(1500) // pretend we're parsing a file
            val raw = listOf(
                "KPLC PREPAID" to 2000.0,
                "QUICKMART SUPERMARKET" to 4150.0,
                "UBER TRIP" to 780.0,
                "NETFLIX SUBSCRIPTION" to 1100.0,
                "SENT TO JOHN KAMAU" to 3000.0,
                "RENT PAYMENT" to 18000.0
            )
            val now = System.currentTimeMillis()
            val imported = raw.mapIndexed { i, (title, amount) ->
                Expense(nextId++, title.toDisplayName(), amount, categorize(title), now - i * day)
            }
            _expenses.value = _expenses.value + imported
            _isImporting.value = false
        }
    }

    private fun categorize(title: String): ExpenseCategory {
        val t = title.lowercase()
        return when {
            listOf("rent", "mortgage", "landlord").any { it in t } -> ExpenseCategory.HOUSING
            listOf("supermarket", "naivas", "quickmart", "carrefour", "restaurant", "kfc").any { it in t } -> ExpenseCategory.FOOD
            listOf("kplc", "water", "token", "safaricom", "wifi", "zuku").any { it in t } -> ExpenseCategory.UTILITIES
            listOf("uber", "bolt", "matatu", "fuel", "petrol").any { it in t } -> ExpenseCategory.TRANSPORT
            listOf("netflix", "showmax", "cinema", "spotify").any { it in t } -> ExpenseCategory.ENTERTAINMENT
            listOf("sent to", "transfer", "withdraw").any { it in t } -> ExpenseCategory.TRANSFERS
            else -> ExpenseCategory.TRANSFERS
        }
    }

    private fun String.toDisplayName() =
        lowercase().split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
}

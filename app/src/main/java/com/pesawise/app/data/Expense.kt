package com.pesawise.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    HOUSING("Housing", Icons.Filled.Home, Color(0xFF767CB2)),
    FOOD("Food & Groceries", Icons.Filled.ShoppingCart, Color(0xFF789579)),
    UTILITIES("Utilities", Icons.Filled.Bolt, Color(0xFFD09A3E)),
    TRANSPORT("Transport", Icons.Filled.DirectionsCar, Color(0xFF6C8795)),
    ENTERTAINMENT("Entertainment", Icons.Filled.Movie, Color(0xFFC97B5C)),
    TRANSFERS("Transfers", Icons.Filled.SwapHoriz, Color(0xFF5E6472))
}

data class Expense(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val dateMillis: Long = System.currentTimeMillis(),
    val currencyCode: String = "KES"
)

package com.pesawise.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesawise.app.data.Expense
import com.pesawise.app.data.ExpenseCategory
import com.pesawise.app.data.CurrencyRatesRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun currencySymbol(code: String) = when (code) {
    "KES" -> "KSh"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "UGX" -> "USh"
    "TZS" -> "TSh"
    else -> code
}
private fun formatCurrency(amount: Double, currencyCode: String) = "${currencySymbol(currencyCode)} " + String.format(Locale.US, "%,.0f", amount)
private fun visibleAmount(amount: Double, currencyCode: String, hidden: Boolean) = if (hidden) "••••••" else formatCurrency(amount, currencyCode)
private fun formatDate(millis: Long) =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

@Composable
fun ExpenseTrackingScreen(
    currencyCode: String = "KES",
    hideAmounts: Boolean = false,
    viewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val ratesRepository = remember { CurrencyRatesRepository(context) }
    val allExpenses by viewModel.expenses.collectAsState()
    val visible by viewModel.visibleExpenses.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val importing by viewModel.isImporting.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var rates by remember(currencyCode) { mutableStateOf(mapOf(currencyCode to 1.0)) }
    var loadingRates by remember { mutableStateOf(false) }
    var missingRates by remember { mutableStateOf(false) }
    val sourceCurrencies = allExpenses.map { it.currencyCode }.distinct().sorted()

    LaunchedEffect(currencyCode, sourceCurrencies) {
        loadingRates = true
        val latestRates = mutableMapOf(currencyCode to 1.0)
        sourceCurrencies.filter { it != currencyCode }.forEach { sourceCode ->
            ratesRepository.rate(sourceCode, currencyCode)?.let { latestRates[sourceCode] = it }
        }
        rates = latestRates
        missingRates = sourceCurrencies.any { it !in latestRates }
        loadingRates = false
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Expense") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header
            item {
                Text(
                    "Expense Tracking",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Upload card
            item { UploadCard(importing = importing, onClick = viewModel::importStatement) }

            // 3. Total card (all expenses; swap allExpenses -> visible to follow the filter)
            item {
                if (loadingRates) {
                    Text("Updating currency conversions…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else if (missingRates) {
                    Text("Some exchange rates are unavailable. Those entries are shown in their original currencies.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else if (sourceCurrencies.any { it != currencyCode }) {
                    Text("Converted to $currencyCode using the latest reference rates.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Total amount is converted to the selected display currency when rates are available.
            item {
                val hasAllRates = allExpenses.all { it.currencyCode in rates }
                TotalCard(
                    totals = if (hasAllRates) mapOf(currencyCode to allExpenses.sumOf { it.amount * (rates[it.currencyCode] ?: 1.0) })
                    else allExpenses.groupBy { it.currencyCode }.mapValues { (_, items) -> items.sumOf { it.amount } },
                    count = allExpenses.size,
                    hideAmounts = hideAmounts,
                    showingSelectedCurrency = hasAllRates
                )
            }

            // 4. Filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("All") }
                        )
                    }
                    items(ExpenseCategory.values().toList()) { cat ->
                        FilterChip(
                            selected = filter == cat,
                            onClick = { viewModel.setFilter(cat) },
                            label = { Text(cat.label) }
                        )
                    }
                }
            }

            // 6. Transactions list
            if (visible.isEmpty()) {
                item {
                    Text(
                        "No expenses in this category yet.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(visible, key = { it.id }) { expense ->
                    val rate = rates[expense.currencyCode]
                    ExpenseRow(expense, hideAmounts, currencyCode, rate)
                }
            }

            item { Spacer(Modifier.height(72.dp)) } // room for the FAB
        }
    }

    // 5. Add expense dialog
    if (showDialog) {
        AddExpenseDialog(
            onDismiss = { showDialog = false },
            currencyCode = currencyCode,
            onSave = { title, amount, category, code ->
                viewModel.addExpense(title, amount, category, code)
                showDialog = false
            }
        )
    }
}

@Composable
private fun UploadCard(importing: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !importing, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (importing) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            } else {
                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(32.dp))
            }
            Column {
                Text(
                    if (importing) "Importing & categorizing…" else "Upload Bank / M-Pesa Statement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Tap to import and auto-categorize transactions",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TotalCard(totals: Map<String, Double>, count: Int, hideAmounts: Boolean, showingSelectedCurrency: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(if (!showingSelectedCurrency && totals.size > 1) "Tracked expenses by currency" else "Total Tracked Expense", color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(4.dp))
            if (hideAmounts) {
                Text("••••••", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                totals.toSortedMap().forEach { (code, total) ->
                    Text(visibleAmount(total, code, false), style = if (totals.size == 1) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(
                "$count ${if (count == 1) "expense" else "expenses"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, hideAmounts: Boolean, displayCurrencyCode: String, rate: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(expense.category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(expense.category.icon, contentDescription = expense.category.label, tint = expense.category.color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "${expense.category.label} • ${formatDate(expense.dateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val shownCode = if (rate == null) expense.currencyCode else displayCurrencyCode
            val shownAmount = if (rate == null) expense.amount else expense.amount * rate
            Text("- ${visibleAmount(shownAmount, shownCode, hideAmounts)}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    currencyCode: String,
    onSave: (String, Double, ExpenseCategory, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var menuOpen by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull()
    val valid = title.isNotBlank() && amount != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Dinner at Restaurant)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) amountText = input
                    },
                    label = { Text("Amount ($currencyCode)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(category.icon, null, tint = category.color)
                        Spacer(Modifier.width(8.dp))
                        Text(category.label, Modifier.weight(1f))
                        Icon(Icons.Filled.ArrowDropDown, null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                leadingIcon = { Icon(cat.icon, null, tint = cat.color) },
                                onClick = { category = cat; menuOpen = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSave(title, amount!!, category, currencyCode) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

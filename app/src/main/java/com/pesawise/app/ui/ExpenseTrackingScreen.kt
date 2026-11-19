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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pesawise.app.data.IncomeEntity
import com.pesawise.app.data.CurrencyRatesRepository
import com.pesawise.app.data.PesaWiseDatabase
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

private data class ActivityEntry(val dateMillis: Long, val key: String, val expense: Expense? = null, val income: IncomeEntity? = null)

@Composable
fun ExpenseTrackingScreen(
    userId: String,
    currencyCode: String = "KES",
    hideAmounts: Boolean = false,
    incomes: List<IncomeEntity> = emptyList(),
    onAddIncome: (String, Double, Long) -> Unit = { _, _, _ -> },
    onDeleteIncome: (IncomeEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val ratesRepository = remember { CurrencyRatesRepository(context) }
    val database = remember(context) { PesaWiseDatabase.getInstance(context) }
    val factory = remember(database, userId) { ExpenseViewModelFactory(database.expenseDao(), userId) }
    val viewModel: ExpenseViewModel = viewModel(key = "expenses-$userId", factory = factory)
    val allExpenses by viewModel.expenses.collectAsState()
    val visible by viewModel.visibleExpenses.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val message by viewModel.message.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    var rates by remember(currencyCode) { mutableStateOf(mapOf(currencyCode to 1.0)) }
    var loadingRates by remember { mutableStateOf(false) }
    var missingRates by remember { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var selectedPeriod by rememberSaveable { mutableStateOf("This month") }
    var showIncomeDialog by remember { mutableStateOf(false) }
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
                onClick = { if (selectedType == "Income") showIncomeDialog = true else showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(if (selectedType == "Income") "Add income" else "Add expense") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text("Transactions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Everything coming in and going out", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("All", "Income", "Expenses").forEach { type ->
                        val selected = selectedType == type
                        Surface(onClick = { selectedType = type }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent) {
                            Text(type, Modifier.padding(vertical = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (selectedType != "Expenses") item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showIncomeDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("Add income")
                    }
                }
            }

            if (message != null) item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message.orEmpty(), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = viewModel::clearMessage) { Text("Dismiss") }
                    }
                }
            }

            // Total card includes every saved expense.
            item {
                if (loadingRates) {
                    Text("Updating currency conversions…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else if (missingRates) {
                    Text("Some exchange rates are unavailable. Those entries are shown in their original currencies.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else if (sourceCurrencies.any { it != currencyCode }) {
                    Text("Converted to $currencyCode using the latest reference rates.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (selectedType != "Income") item {
                val hasAllRates = allExpenses.all { it.currencyCode in rates }
                TotalCard(
                    totals = if (hasAllRates) mapOf(currencyCode to allExpenses.sumOf { it.amount * (rates[it.currencyCode] ?: 1.0) })
                    else allExpenses.groupBy { it.currencyCode }.mapValues { (_, items) -> items.sumOf { it.amount } },
                    count = allExpenses.size,
                    hideAmounts = hideAmounts,
                    showingSelectedCurrency = hasAllRates
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("This month", "3 months", "All time")) { period ->
                        FilterChip(selectedPeriod == period, { selectedPeriod = period }, label = { Text(period) })
                    }
                }
            }

            if (selectedType != "Income") item {
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

            val lowerBound = when (selectedPeriod) {
                "This month" -> java.util.Calendar.getInstance().apply { set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
                "3 months" -> java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -3) }.timeInMillis
                else -> Long.MIN_VALUE
            }
            val periodExpenses = allExpenses.filter { it.dateMillis >= lowerBound && (filter == null || it.category == filter) }
            val periodIncomes = incomes.filter { it.dateMillis >= lowerBound }
            val showExpenses = selectedType != "Income"
            val showIncomes = selectedType != "Expenses"
            val entries = buildList {
                if (showExpenses) periodExpenses.forEach { add(ActivityEntry(it.dateMillis, "expense-${it.id}", expense = it)) }
                if (showIncomes) periodIncomes.forEach { add(ActivityEntry(it.dateMillis, "income-${it.id}", income = it)) }
            }.sortedByDescending { it.dateMillis }
            if (entries.isEmpty()) {
                item {
                    Text(
                        "Nothing to show for this period yet. Add an income or expense to get started.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(entries, key = { it.key }) { entry ->
                    entry.income?.let { income -> IncomeRow(income, hideAmounts, onDelete = { onDeleteIncome(income) }) }
                    entry.expense?.let { expense ->
                        ExpenseRow(
                            expense = expense,
                            hideAmounts = hideAmounts,
                            displayCurrencyCode = currencyCode,
                            rate = rates[expense.currencyCode],
                            onEdit = { editingExpense = expense },
                            onDelete = { deletingExpense = expense }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

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

    if (showIncomeDialog) {
        AddIncomeDialog(currencyCode, onDismiss = { showIncomeDialog = false }, onSave = { title, amount -> onAddIncome(title, amount, System.currentTimeMillis()); showIncomeDialog = false })
    }

    editingExpense?.let { expense ->
        AddExpenseDialog(
            initialExpense = expense,
            onDismiss = { editingExpense = null },
            currencyCode = expense.currencyCode,
            onSave = { title, amount, category, code ->
                viewModel.updateExpense(expense, title, amount, category, code)
                editingExpense = null
            }
        )
    }

    deletingExpense?.let { expense ->
        AlertDialog(
            onDismissRequest = { deletingExpense = null },
            title = { Text("Delete expense?") },
            text = { Text("\"${expense.title}\" will be removed from your expense history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(expense)
                    deletingExpense = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingExpense = null }) { Text("Cancel") } }
        )
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
private fun ExpenseRow(
    expense: Expense,
    hideAmounts: Boolean,
    displayCurrencyCode: String,
    rate: Double?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            var menuExpanded by remember(expense.id) { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Expense options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeRow(income: IncomeEntity, hideAmounts: Boolean, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF8DA38B).copy(alpha = .16f)), contentAlignment = Alignment.Center) {
                Text("+", color = Color(0xFF648264), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(income.title, fontWeight = FontWeight.SemiBold)
                Text("Income · ${formatDate(income.dateMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("+ ${visibleAmount(income.amount, income.currencyCode, hideAmounts)}", color = Color(0xFF648264), fontWeight = FontWeight.Bold)
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete income", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun AddIncomeDialog(currencyCode: String, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add income") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Source (e.g. Salary)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amountText, { input -> if (input.all { it.isDigit() || it == '.' }) amountText = input }, label = { Text("Amount ($currencyCode)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank() && amount != null && amount > 0, onClick = { onSave(title.trim(), amount!!) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddExpenseDialog(
    initialExpense: Expense? = null,
    onDismiss: () -> Unit,
    currencyCode: String,
    onSave: (String, Double, ExpenseCategory, String) -> Unit
) {
    var title by remember(initialExpense?.id) { mutableStateOf(initialExpense?.title.orEmpty()) }
    var amountText by remember(initialExpense?.id) { mutableStateOf(initialExpense?.amount?.toString().orEmpty()) }
    var category by remember(initialExpense?.id) { mutableStateOf(initialExpense?.category ?: ExpenseCategory.FOOD) }
    var menuOpen by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull()
    val valid = title.isNotBlank() && amount != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExpense == null) "Add Expense" else "Edit Expense") },
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
            TextButton(enabled = valid, onClick = { onSave(title, amount!!, category, currencyCode) }) { Text(if (initialExpense == null) "Save" else "Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

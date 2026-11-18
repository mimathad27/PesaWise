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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesawise.app.data.Expense
import com.pesawise.app.data.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatKsh(amount: Double) = "KSh " + String.format(Locale.US, "%,.0f", amount)
private fun formatDate(millis: Long) =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

@Composable
fun ExpenseTrackingScreen(viewModel: ExpenseViewModel = viewModel()) {
    val allExpenses by viewModel.expenses.collectAsState()
    val visible by viewModel.visibleExpenses.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val importing by viewModel.isImporting.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

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
                TotalCard(
                    total = allExpenses.sumOf { it.amount },
                    count = allExpenses.size
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
                items(visible, key = { it.id }) { ExpenseRow(it) }
            }

            item { Spacer(Modifier.height(72.dp)) } // room for the FAB
        }
    }

    // 5. Add expense dialog
    if (showDialog) {
        AddExpenseDialog(
            onDismiss = { showDialog = false },
            onSave = { title, amount, category ->
                viewModel.addExpense(title, amount, category)
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
private fun TotalCard(total: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Total Tracked Expense", color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                formatKsh(total),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "$count ${if (count == 1) "expense" else "expenses"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
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
            Text("- ${formatKsh(expense.amount)}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, ExpenseCategory) -> Unit
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
                    label = { Text("Amount (KSh)") },
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
            TextButton(enabled = valid, onClick = { onSave(title, amount!!, category) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

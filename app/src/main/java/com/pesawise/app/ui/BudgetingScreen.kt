package com.pesawise.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.pesawise.app.data.CurrencyRatesRepository

private data class BudgetBucket(
    val name: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

private val budgetBuckets = listOf(
    BudgetBucket("Needs", "Housing, food, bills & transport", Icons.Filled.AccountBalanceWallet, Color(0xFF238A70)),
    BudgetBucket("Wants", "Leisure, shopping & subscriptions", Icons.Filled.ShoppingBag, Color(0xFF6C63B5)),
    BudgetBucket("Savings", "Emergency fund & future goals", Icons.Filled.Savings, Color(0xFFE39A32))
)

private val allocationPresets = mapOf(
    "50 / 30 / 20" to listOf(50, 30, 20),
    "60 / 20 / 20" to listOf(60, 20, 20)
)

private fun formatBudgetCurrency(amount: Double, currencyCode: String): String =
    "${budgetCurrencySymbol(currencyCode)} " + NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 2 }.format(amount)

private fun budgetCurrencySymbol(code: String) = when (code) {
    "KES" -> "KSh"
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "UGX" -> "USh"
    "TZS" -> "TSh"
    else -> code
}

@Composable
fun BudgetingScreen(currencyCode: String = "KES", budgetCycle: String = "Monthly", hideAmounts: Boolean = false) {
    val context = LocalContext.current
    val ratesRepository = remember { CurrencyRatesRepository(context) }
    var incomeText by rememberSaveable { mutableStateOf("") }
    var incomeCurrencyCode by rememberSaveable { mutableStateOf(currencyCode) }
    var incomeRateUnavailable by remember { mutableStateOf(false) }
    var incomeWasConverted by remember { mutableStateOf(false) }
    var selectedPreset by rememberSaveable { mutableStateOf("50 / 30 / 20") }
    var customNeeds by rememberSaveable { mutableStateOf("50") }
    var customWants by rememberSaveable { mutableStateOf("30") }
    var customSavings by rememberSaveable { mutableStateOf("20") }

    val income = incomeText.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 0.0
    val customValues = listOf(customNeeds, customWants, customSavings).map { it.toIntOrNull() ?: 0 }
    val allocation = if (selectedPreset == "Custom") customValues else allocationPresets[selectedPreset] ?: allocationPresets.getValue("50 / 30 / 20")
    val allocationTotal = allocation.sum()
    val allocationValid = allocation.all { it in 0..100 } && allocationTotal == 100
    val budgetCurrencyCode = if (incomeCurrencyCode != currencyCode || incomeRateUnavailable) incomeCurrencyCode else currencyCode

    LaunchedEffect(currencyCode) {
        val sourceCode = incomeCurrencyCode
        if (sourceCode == currencyCode) {
            incomeRateUnavailable = false
            incomeWasConverted = false
        } else {
            incomeRateUnavailable = false
            incomeWasConverted = false
            val rate = ratesRepository.rate(sourceCode, currencyCode)
            if (rate == null) {
                incomeRateUnavailable = true
            } else {
                incomeText.toDoubleOrNull()?.let { previousIncome ->
                    incomeText = String.format(Locale.US, "%.2f", previousIncome * rate).trimEnd('0').trimEnd('.')
                }
                incomeCurrencyCode = currencyCode
                incomeWasConverted = true
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("My budget", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Plan income for your selected cycle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Text("${budgetCycle.uppercase()} TAKE-HOME INCOME", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = incomeText,
                        onValueChange = { input ->
                            if (input.length <= 12 && input.all { it.isDigit() || it == '.' }) {
                                incomeText = input
                                incomeWasConverted = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 85000", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .5f)) },
                        prefix = { Text("${budgetCurrencySymbol(incomeCurrencyCode)} ", color = MaterialTheme.colorScheme.onSurface) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = if (hideAmounts) PasswordVisualTransformation() else VisualTransformation.None,
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Enter your income after tax and deductions", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall)
                    if (incomeWasConverted) Text("Income converted using the latest available reference rate.", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall)
                    if (incomeRateUnavailable) Text("Exchange rate unavailable. This budget remains in $incomeCurrencyCode.", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose an allocation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Text("A starting point you can adjust to fit your life.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (allocationPresets.keys + "Custom").forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { selectedPreset = preset },
                            label = { Text(preset) },
                            leadingIcon = if (selectedPreset == preset) ({ Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }) else null
                        )
                    }
                }
                if (selectedPreset == "Custom") {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Set your percentages", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AllocationInput("Needs", customNeeds, { customNeeds = it }, Modifier.weight(1f))
                                AllocationInput("Wants", customWants, { customWants = it }, Modifier.weight(1f))
                                AllocationInput("Savings", customSavings, { customSavings = it }, Modifier.weight(1f))
                            }
                            Text(
                                if (allocationValid) "Adds up to 100%" else "Percentages must total 100% (currently $allocationTotal%).",
                                color = if (allocationValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your $budgetCycle plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFFE39A32), modifier = Modifier.size(18.dp))
            }
        }

        if (!allocationValid) {
            item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text("Adjust your custom percentages to total 100% to see your budget breakdown.", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(budgetBuckets.size) { index ->
                val bucket = budgetBuckets[index]
                val percent = allocation[index]
                BudgetBucketCard(bucket, percent, income * percent / 100.0, budgetCurrencyCode, budgetCycle, hideAmounts)
            }
        }

        item {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.TipsAndUpdates, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("A flexible guide", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("The 50/30/20 rule is a starting point. Adjust it for your needs, goals and cost of living.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AllocationInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.length <= 3 && input.all(Char::isDigit)) onValueChange(input) },
        modifier = modifier,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Clip) },
        suffix = { Text("%") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun BudgetBucketCard(bucket: BudgetBucket, percent: Int, amount: Double, currencyCode: String, budgetCycle: String, hideAmounts: Boolean) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(bucket.color.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Icon(bucket.icon, contentDescription = null, tint = bucket.color)
                }
                Column(Modifier.weight(1f)) {
                    Text(bucket.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(bucket.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Text("$percent%", color = bucket.color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = bucket.color,
                trackColor = bucket.color.copy(alpha = .12f)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Amount per $budgetCycle", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(if (hideAmounts) "••••••" else formatBudgetCurrency(amount, currencyCode), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

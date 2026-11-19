package com.pesawise.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pesawise.app.data.Expense
import com.pesawise.app.data.ExpenseCategory
import com.pesawise.app.data.IncomeEntity
import com.pesawise.app.data.SubscriptionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val WarmInk = Color(0xFF293040)
private val MutedInk = Color(0xFF707781)
private val Sage = Color(0xFF8DA38B)
private val Terracotta = Color(0xFFC97B5C)
private val Gold = Color(0xFFF4D58D)
private val WarmCanvas = Color(0xFFF7F5F0)
private val CategoryPalette = listOf(Sage, Color(0xFF5E6472), Gold, Terracotta, Color(0xFF8DA38B), Color(0xFFB8A895))

private fun amountText(amount: Double, code: String, hidden: Boolean = false): String {
    if (hidden) return "••••••"
    val symbol = when (code) { "KES" -> "KSh"; "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; "UGX" -> "USh"; "TZS" -> "TSh"; else -> code }
    return "$symbol " + NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(amount)
}

private fun monthBounds(offset: Int = 0): Pair<Long, Long> {
    val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, offset); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    return start to calendar.timeInMillis
}

private fun shortDate(millis: Long): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))

@Composable
fun OverviewScreen(
    expenses: List<Expense>, incomes: List<IncomeEntity>, currencyCode: String, hideAmounts: Boolean,
    onOpenTransactions: () -> Unit, onOpenInsights: () -> Unit
) {
    val (start, end) = remember { monthBounds() }
    val monthExpenses = expenses.filter { it.dateMillis in start until end && it.currencyCode == currencyCode }
    val monthIncome = incomes.filter { it.dateMillis in start until end && it.currencyCode == currencyCode }
    val spent = monthExpenses.sumOf { it.amount }
    val earned = monthIncome.sumOf { it.amount }
    val categorySpend = ExpenseCategory.values().map { category -> category to monthExpenses.filter { it.category == category }.sumOf { it.amount } }.filter { it.second > 0 }.sortedByDescending { it.second }
    val months = (5 downTo 0).map { offset ->
        val (from, to) = monthBounds(-offset)
        expenses.filter { it.dateMillis in from until to && it.currencyCode == currencyCode }.sumOf { it.amount }
    }
    LazyColumn(Modifier.fillMaxSize().background(WarmCanvas), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PesaWise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = WarmInk)
                    Text(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.bodyMedium, color = MutedInk)
                }
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Current month", tint = MutedInk)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).background(Color(0xFF0D8F72)).padding(22.dp)) {
                Text("Spent this month", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(7.dp))
                Text(amountText(spent, currencyCode, hideAmounts), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(17.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) { Text("INCOME", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelSmall); Text(amountText(earned, currencyCode, hideAmounts), color = Color.White, fontWeight = FontWeight.SemiBold) }
                    Column(Modifier.weight(1f)) { Text("EXPENSES", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelSmall); Text("${monthExpenses.size} entries", color = Color.White, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Spending trend", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = WarmInk, modifier = Modifier.weight(1f))
                    Text("6 months", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(14.dp))
                SpendingChart(months, Modifier.fillMaxWidth().height(108.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    (5 downTo 0).forEach { offset -> Text(SimpleDateFormat("MMM", Locale.getDefault()).format(Date(monthBounds(-offset).first)), color = MutedInk, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Where it went", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = WarmInk, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenInsights) { Text("Insights"); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp)) }
            }
        }
        if (categorySpend.isEmpty()) item { QuietEmpty("Add expenses to see your monthly breakdown.") }
        else item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                    var x = 0f
                    categorySpend.forEachIndexed { index, pair ->
                        val width = size.width * (pair.second / spent.coerceAtLeast(1.0)).toFloat()
                        drawRect(CategoryPalette[index % CategoryPalette.size], topLeft = androidx.compose.ui.geometry.Offset(x, 0f), size = androidx.compose.ui.geometry.Size(width, size.height))
                        x += width
                    }
                }
                categorySpend.take(4).forEachIndexed { index, (category, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(CategoryPalette[index % CategoryPalette.size]))
                        Spacer(Modifier.width(9.dp))
                        Text(category.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = WarmInk)
                        Text("${(amount / spent * 100).toInt()}%", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(12.dp))
                        Text(amountText(amount, currencyCode, hideAmounts), fontWeight = FontWeight.Medium, color = WarmInk)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = WarmInk, modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenTransactions) { Text("See all"); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp)) }
            }
        }
        val recent = expenses.sortedByDescending { it.dateMillis }.take(3)
        if (recent.isEmpty()) item { QuietEmpty("Your recent expenses will appear here.") }
        else items(recent.size) { index ->
            val expense = recent[index]
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(CategoryPalette[index % CategoryPalette.size].copy(alpha = .16f)), contentAlignment = Alignment.Center) { Icon(expense.category.icon, null, tint = CategoryPalette[index % CategoryPalette.size]) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(expense.title, fontWeight = FontWeight.SemiBold, color = WarmInk); Text("${expense.category.label} · ${shortDate(expense.dateMillis)}", style = MaterialTheme.typography.bodySmall, color = MutedInk) }
                Text("− ${amountText(expense.amount, expense.currencyCode, hideAmounts)}", fontWeight = FontWeight.SemiBold, color = WarmInk)
            }
        }
        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
fun InsightsScreen(expenses: List<Expense>, currencyCode: String, hideAmounts: Boolean) {
    val (thisStart, thisEnd) = remember { monthBounds() }
    val (previousStart, _) = remember { monthBounds(-1) }
    val current = expenses.filter { it.dateMillis in thisStart until thisEnd && it.currencyCode == currencyCode }
    val previous = expenses.filter { it.dateMillis in previousStart until thisStart && it.currencyCode == currencyCode }
    val total = current.sumOf { it.amount }
    val previousTotal = previous.sumOf { it.amount }
    val categories = ExpenseCategory.values().map { category ->
        val now = current.filter { it.category == category }.sumOf { it.amount }
        val before = previous.filter { it.category == category }.sumOf { it.amount }
        Triple(category, now, before)
    }.filter { it.second > 0 || it.third > 0 }.sortedByDescending { it.second }
    LazyColumn(Modifier.fillMaxSize().background(WarmCanvas), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Column { Text("Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = WarmInk); Text("A closer look at your spending", color = MutedInk) } }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(19.dp)) {
                Text("This month", style = MaterialTheme.typography.titleMedium, color = MutedInk)
                Text(amountText(total, currencyCode, hideAmounts), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = WarmInk)
                Spacer(Modifier.height(8.dp))
                val change = if (previousTotal == 0.0) null else ((total - previousTotal) / previousTotal * 100).toInt()
                Text(change?.let { "${kotlin.math.abs(it)}% ${if (it > 0) "more" else "less"} than last month" } ?: "Add last month's expenses to compare periods", color = if ((change ?: 0) > 0) Terracotta else Sage, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item { Text("Category patterns", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = WarmInk) }
        if (categories.isEmpty()) item { QuietEmpty("Your category patterns will appear once you add expenses.") }
        else items(categories.size) { index ->
            val (category, now, before) = categories[index]
            val difference = if (before > 0) ((now - before) / before * 100).toInt() else null
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(CategoryPalette[index % CategoryPalette.size].copy(alpha = .18f)), contentAlignment = Alignment.Center) { Icon(category.icon, null, tint = CategoryPalette[index % CategoryPalette.size], modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Text(category.label, Modifier.weight(1f), color = WarmInk, fontWeight = FontWeight.SemiBold)
                    Text(amountText(now, currencyCode, hideAmounts), color = WarmInk, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(progress = { if (total > 0) (now / total).toFloat() else 0f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = CategoryPalette[index % CategoryPalette.size], trackColor = CategoryPalette[index % CategoryPalette.size].copy(alpha = .14f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${if (total > 0) (now / total * 100).toInt() else 0}% of this month's spending", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    Text(difference?.let { "${if (it > 0) "+" else ""}$it% vs last month" } ?: "New this month", color = if ((difference ?: 0) > 0) Terracotta else Sage, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text("Previous month: ${amountText(previousTotal, currencyCode, hideAmounts)}", color = MutedInk, style = MaterialTheme.typography.bodySmall) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionEntity>, currencyCode: String, hideAmounts: Boolean,
    onAdd: (String, Double, String, String, Long) -> Unit,
    onToggle: (SubscriptionEntity, Boolean) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf<SubscriptionEntity?>(null) }
    val active = subscriptions.filter { it.active }
    val monthlyByCurrency = active.groupBy { it.currencyCode }.mapValues { (_, items) ->
        items.sumOf { item -> item.amount * when (item.billingCycle) { "Weekly" -> 4.33; "Yearly" -> 1.0 / 12; else -> 1.0 } }
    }
    LazyColumn(Modifier.fillMaxSize().background(WarmCanvas), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Subscriptions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = WarmInk); Text("Keep recurring costs in view", color = MutedInk) }; FilledTonalIconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, "Add subscription") } } }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFF293040)).padding(19.dp)) {
                Text("ESTIMATED MONTHLY TOTAL", color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.labelMedium)
                if (monthlyByCurrency.isEmpty()) Text(amountText(0.0, currencyCode, hideAmounts), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                else monthlyByCurrency.toSortedMap().forEach { (code, amount) -> Text(amountText(amount, code, hideAmounts), color = Color.White, style = if (monthlyByCurrency.size == 1) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                Text("${active.size} active ${if (active.size == 1) "subscription" else "subscriptions"}", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (subscriptions.isEmpty()) item { QuietEmpty("Add subscriptions manually to keep recurring costs visible.") }
        else items(subscriptions.size, key = { subscriptions[it].id }) { index ->
            val item = subscriptions[index]
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(if (item.active) Sage.copy(alpha = .17f) else MutedInk.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Subscriptions, null, tint = if (item.active) Sage else MutedInk) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(item.title, color = WarmInk, fontWeight = FontWeight.SemiBold); Text("${item.billingCycle} · ${if (item.active) "Next ${shortDate(item.nextChargeMillis)}" else "Paused"}", color = MutedInk, style = MaterialTheme.typography.bodySmall) }
                Column(horizontalAlignment = Alignment.End) { Text(amountText(item.amount, item.currencyCode, hideAmounts), color = WarmInk, fontWeight = FontWeight.SemiBold); TextButton(onClick = { onToggle(item, !item.active) }, contentPadding = PaddingValues(horizontal = 4.dp)) { Text(if (item.active) "Pause" else "Resume") } }
                IconButton(onClick = { deleteItem = item }) { Icon(Icons.Filled.DeleteOutline, "Delete ${item.title}", tint = MutedInk) }
            }
        }
        item { Text("Entries are added by you and stay on this device. Monthly totals are grouped by currency.", color = MutedInk, style = MaterialTheme.typography.bodySmall) }
    }
    if (showAdd) SubscriptionDialog(currencyCode, onDismiss = { showAdd = false }, onSave = { title, amount, cycle, date -> onAdd(title, amount, currencyCode, cycle, date); showAdd = false })
    deleteItem?.let { item -> AlertDialog(onDismissRequest = { deleteItem = null }, title = { Text("Remove subscription?") }, text = { Text("${item.title} will be removed from your list.") }, confirmButton = { TextButton(onClick = { onDelete(item); deleteItem = null }) { Text("Remove") } }, dismissButton = { TextButton(onClick = { deleteItem = null }) { Text("Cancel") } }) }
}

@Composable
private fun SubscriptionDialog(currencyCode: String, onDismiss: () -> Unit, onSave: (String, Double, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }; var amountText by remember { mutableStateOf("") }; var cycle by remember { mutableStateOf("Monthly") }
    val amount = amountText.toDoubleOrNull()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add subscription") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Service name") }, singleLine = true)
            OutlinedTextField(amountText, { input -> if (input.all { it.isDigit() || it == '.' }) amountText = input }, label = { Text("Amount ($currencyCode)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Weekly", "Monthly", "Yearly").forEach { FilterChip(cycle == it, { cycle = it }, label = { Text(it) }) } }
            Text("Next charge: one ${cycle.lowercase()} from today", color = MutedInk, style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(enabled = title.isNotBlank() && amount != null && amount > 0, onClick = {
        val calendar = Calendar.getInstance().apply { when (cycle) { "Weekly" -> add(Calendar.DAY_OF_YEAR, 7); "Yearly" -> add(Calendar.YEAR, 1); else -> add(Calendar.MONTH, 1) } }
        onSave(title.trim(), amount!!, cycle, calendar.timeInMillis)
    }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun SpendingChart(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val max = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        val points = values.mapIndexed { index, value -> androidx.compose.ui.geometry.Offset(size.width * index / (values.size - 1), size.height - (value / max).toFloat() * (size.height - 8f) - 4f) }
        val path = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }
        drawPath(path, Sage, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        points.forEach { drawCircle(Terracotta, radius = 4f, center = it) }
    }
}

@Composable
private fun QuietEmpty(message: String) {
    Text(message, Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(18.dp), color = MutedInk, style = MaterialTheme.typography.bodyMedium)
}

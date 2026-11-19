package com.pesawise.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesawise.app.data.AuthRepository
import com.pesawise.app.data.PesaWiseDatabase
import kotlinx.coroutines.launch

@Composable
fun SignedInScreen(authViewModel: AuthViewModel, authRepository: AuthRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val userId = authRepository.currentUserId()
    val stateHolder = rememberSaveableStateHolder()
    val context = LocalContext.current
    val appSettings = remember { AppSettingsStore(context) }
    val settings by appSettings.state
    val database = remember(context) { PesaWiseDatabase.getInstance(context) }
    val expenses by remember(userId) { userId?.let(database.expenseDao()::observeForUser) ?: kotlinx.coroutines.flow.flowOf(emptyList<com.pesawise.app.data.ExpenseEntity>()) }.collectAsState(initial = emptyList())
    val incomes by remember(userId) { userId?.let(database.financeDao()::observeIncomes) ?: kotlinx.coroutines.flow.flowOf(emptyList<com.pesawise.app.data.IncomeEntity>()) }.collectAsState(initial = emptyList())
    val subscriptions by remember(userId) { userId?.let(database.financeDao()::observeSubscriptions) ?: kotlinx.coroutines.flow.flowOf(emptyList<com.pesawise.app.data.SubscriptionEntity>()) }.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color(0xFFF1ECEF)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home", maxLines = 1) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                    label = { Text("Insights", maxLines = 1) }
                )
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }, label = { Text("Activity", maxLines = 1) })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Filled.Subscriptions, null) }, label = { Text("Subs", maxLines = 1) })
                NavigationBarItem(selected = selectedTab == 4, onClick = { selectedTab = 4 }, icon = { Icon(Icons.Filled.AccountBalanceWallet, null) }, label = { Text("Budget", maxLines = 1) })
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    label = { Text("Account", maxLines = 1) }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            stateHolder.SaveableStateProvider(key = selectedTab) {
                when (selectedTab) {
                    0 -> OverviewScreen(
                        expenses = expenses.map(com.pesawise.app.data.ExpenseEntity::toExpense), incomes = incomes,
                        currencyCode = settings.currencyCode, hideAmounts = settings.privacyMode,
                        onOpenTransactions = { selectedTab = 2 }, onOpenInsights = { selectedTab = 1 }
                    )
                    1 -> InsightsScreen(expenses.map(com.pesawise.app.data.ExpenseEntity::toExpense), settings.currencyCode, settings.privacyMode)
                    2 -> if (userId != null) {
                        ExpenseTrackingScreen(userId = userId, currencyCode = settings.currencyCode, hideAmounts = settings.privacyMode, incomes = incomes,
                            onAddIncome = { title, amount, date -> scope.launch { database.financeDao().addIncome(com.pesawise.app.data.IncomeEntity(userId = userId, title = title, amount = amount, dateMillis = date, currencyCode = settings.currencyCode)) } },
                            onDeleteIncome = { income -> scope.launch { database.financeDao().deleteIncome(income.id, userId) } })
                    } else {
                        Text("Your session has expired. Please sign in again.", modifier = Modifier.padding(24.dp))
                    }
                    3 -> if (userId != null) SubscriptionsScreen(
                        subscriptions, settings.currencyCode, settings.privacyMode,
                        onAdd = { title, amount, code, cycle, nextDate -> scope.launch { database.financeDao().addSubscription(com.pesawise.app.data.SubscriptionEntity(userId = userId, title = title, amount = amount, currencyCode = code, billingCycle = cycle, nextChargeMillis = nextDate)) } },
                        onToggle = { item, active -> scope.launch { database.financeDao().setSubscriptionActive(item.id, userId, active) } },
                        onDelete = { item -> scope.launch { database.financeDao().deleteSubscription(item.id, userId) } }
                    ) else Text("Your session has expired. Please sign in again.", modifier = Modifier.padding(24.dp))
                    4 -> BudgetingScreen(currencyCode = settings.currencyCode, budgetCycle = settings.budgetCycle, hideAmounts = settings.privacyMode, expenses = expenses.map(com.pesawise.app.data.ExpenseEntity::toExpense))
                    else -> AccountScreen(
                        authRepository = authRepository,
                        appSettings = appSettings,
                        settings = settings,
                        onLogout = {
                            authRepository.signOut()
                            authViewModel.update { it.copy(loggedIn = false, message = null, isError = false) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountScreen(
    authRepository: AuthRepository,
    appSettings: AppSettingsStore,
    settings: AppSettings,
    onLogout: () -> Unit
) {
    val profile = authRepository.currentProfile()
    val scope = rememberCoroutineScope()
    var sendingReset by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showNameDialog by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }
    val displayName = profile?.fullName?.takeIf { it.isNotBlank() } ?: "PesaWise member"
    val email = profile?.email.orEmpty()
    val initial = displayName.firstOrNull()?.uppercase() ?: "P"

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(58.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { nameDraft = displayName; showNameDialog = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit profile name", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        SettingsGroup {
            PreferencePicker(
                title = "Display currency",
                value = settings.currencyCode,
                icon = Icons.Filled.CurrencyExchange,
                options = listOf("KES", "USD", "EUR", "GBP", "UGX", "TZS"),
                onSelect = { code -> appSettings.update { it.copy(currencyCode = code) } }
            )
            PreferenceDivider()
            PreferencePicker(
                title = "Budget cycle",
                value = settings.budgetCycle,
                icon = Icons.Filled.CalendarMonth,
                options = listOf("Monthly", "Weekly", "Every 2 weeks"),
                onSelect = { cycle -> appSettings.update { it.copy(budgetCycle = cycle) } }
            )
            PreferenceDivider()
            PreferencePicker(
                title = "Reset email language",
                value = if (settings.languageCode == "sw") "Kiswahili" else "English",
                icon = Icons.Filled.Language,
                options = listOf("English", "Kiswahili"),
                onSelect = { language ->
                    val code = if (language == "Kiswahili") "sw" else "en"
                    appSettings.update { it.copy(languageCode = code) }
                    authRepository.setLanguageCode(code)
                }
            )
            PreferenceDivider()
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 78.dp).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Filled.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Privacy mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text("Hide balances and amounts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.privacyMode, onCheckedChange = { enabled -> appSettings.update { it.copy(privacyMode = enabled) } })
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        SettingsGroup {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 78.dp).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Filled.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Password reset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text("Send a secure link to your email", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(
                    enabled = !sendingReset && email.isNotBlank(),
                    onClick = {
                        scope.launch {
                            sendingReset = true
                            feedback = authRepository.sendPasswordReset(email).fold(
                                onSuccess = { it },
                                onFailure = { it.message ?: "Could not send the reset email. Try again." }
                            )
                            sendingReset = false
                        }
                    }
                ) { Text(if (sendingReset) "Sending…" else "Reset") }
            }
            feedback?.let {
                PreferenceDivider()
                Text(it, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(4.dp))
        SettingsGroup {
            Row(
                modifier = Modifier.fillMaxWidth().height(62.dp).clickable(onClick = onLogout).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Log out", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showNameDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit profile name") },
            text = {
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, label = { Text("Full name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(enabled = nameDraft.isNotBlank() && !sendingReset, onClick = {
                    scope.launch {
                        sendingReset = true
                        feedback = authRepository.updateDisplayName(nameDraft).fold({ it }, { it.message ?: "Could not update your name." })
                        sendingReset = false
                        showNameDialog = false
                    }
                }) { Text(if (sendingReset) "Saving..." else "Save") }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun PreferenceDivider() {
    HorizontalDivider(Modifier.padding(start = 54.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f))
}

@Composable
private fun PreferencePicker(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).clickable { expanded = true }.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ExpandMore, contentDescription = "Choose $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

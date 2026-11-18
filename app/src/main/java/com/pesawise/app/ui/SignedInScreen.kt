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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesawise.app.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun SignedInScreen(authViewModel: AuthViewModel, authRepository: AuthRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val stateHolder = rememberSaveableStateHolder()
    val context = LocalContext.current
    val appSettings = remember { AppSettingsStore(context) }
    val settings by appSettings.state

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                    label = { Text("Expenses") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.PieChart, contentDescription = null) },
                    label = { Text("Budget") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    label = { Text("Account") }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            stateHolder.SaveableStateProvider(key = selectedTab) {
                when (selectedTab) {
                    0 -> ExpenseTrackingScreen(currencyCode = settings.currencyCode, hideAmounts = settings.privacyMode)
                    1 -> BudgetingScreen(currencyCode = settings.currencyCode, budgetCycle = settings.budgetCycle, hideAmounts = settings.privacyMode)
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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text("Account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Manage your profile and security.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(58.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = { nameDraft = displayName; showNameDialog = true }) { Text("Edit name") }

        Spacer(Modifier.height(18.dp))
        Text("Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        PreferencePicker(
            title = "Display currency",
            value = settings.currencyCode,
            options = listOf("KES", "USD", "EUR", "GBP", "UGX", "TZS"),
            onSelect = { code -> appSettings.update { it.copy(currencyCode = code) } }
        )
        PreferencePicker(
            title = "Language for Firebase emails",
            value = if (settings.languageCode == "sw") "Kiswahili" else "English",
            options = listOf("English", "Kiswahili"),
            onSelect = { language ->
                val code = if (language == "Kiswahili") "sw" else "en"
                appSettings.update { it.copy(languageCode = code) }
                authRepository.setLanguageCode(code)
            }
        )
        PreferencePicker(
            title = "Budget cycle",
            value = settings.budgetCycle,
            options = listOf("Monthly", "Weekly", "Every 2 weeks"),
            onSelect = { cycle -> appSettings.update { it.copy(budgetCycle = cycle) } }
        )
        ListItem(
            headlineContent = { Text("Privacy mode") },
            supportingContent = { Text("Hide balances and amounts on this device") },
            trailingContent = {
                Switch(checked = settings.privacyMode, onCheckedChange = { enabled -> appSettings.update { it.copy(privacyMode = enabled) } })
            }
        )
        Text("Budgets and expense entries display in this currency. Existing records are converted with the latest available reference rate; saved amounts stay in their original currency.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(18.dp))
        Text("Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        ListItem(
            headlineContent = { Text("Password & security") },
            supportingContent = { Text("Email a secure password reset link") },
            leadingContent = { Icon(Icons.Filled.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = {
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
        )
        feedback?.let {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                Text(it, modifier = Modifier.fillMaxWidth().padding(14.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text("Log out", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
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
private fun PreferencePicker(title: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        trailingContent = {
            Box {
                TextButton(onClick = { expanded = true }) { Text("Change") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                    }
                }
            }
        }
    )
}

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
import androidx.compose.material.icons.filled.CurrencyExchange
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesawise.app.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun SignedInScreen(authViewModel: AuthViewModel, authRepository: AuthRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val stateHolder = rememberSaveableStateHolder()

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
                    0 -> ExpenseTrackingScreen()
                    1 -> BudgetingScreen()
                    else -> AccountScreen(
                        authRepository = authRepository,
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
private fun AccountScreen(authRepository: AuthRepository, onLogout: () -> Unit) {
    val profile = authRepository.currentProfile()
    val scope = rememberCoroutineScope()
    var sendingReset by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
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

        Spacer(Modifier.height(18.dp))
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
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
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Currency") },
            supportingContent = { Text("Kenyan shilling (KSh)") },
            leadingContent = { Icon(Icons.Filled.CurrencyExchange, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
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
}

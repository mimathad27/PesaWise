package com.pesawise.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Navy = Color(0xFF102A43)
private val Green = Color(0xFF0B8F70)
private val Canvas = Color(0xFFF5F8FA)

@Composable
fun PesaWiseApp(viewModel: AuthViewModel) {
    val state by viewModel.state

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))
            Box(Modifier.size(58.dp).background(Green, RoundedCornerShape(18.dp)), Alignment.Center) {
                Text("P", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text("PesaWise", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("Your smart financial journey", color = Color(0xFF627D98))
            Spacer(Modifier.height(36.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(if (state.isForgotPassword) "Reset your password" else if (state.isSignUp) "Create your account" else "Welcome back", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(6.dp))
                    Text(if (state.isForgotPassword) "Enter your account email to receive a secure reset link." else if (state.isSignUp) "Start building healthier money habits." else "Sign in to continue your journey.", color = Color(0xFF627D98))
                    Spacer(Modifier.height(22.dp))
                    if (!state.isForgotPassword) Row(Modifier.fillMaxWidth().background(Canvas, RoundedCornerShape(12.dp)).padding(4.dp)) {
                        AuthTab("Log in", !state.isSignUp, Modifier.weight(1f)) { viewModel.update { it.copy(isSignUp = false, message = null) } }
                        AuthTab("Sign up", state.isSignUp, Modifier.weight(1f)) { viewModel.update { it.copy(isSignUp = true, message = null) } }
                    }
                    Spacer(Modifier.height(20.dp))
                    if (state.isSignUp && !state.isForgotPassword) AuthField("Full name", state.fullName, { value -> viewModel.update { it.copy(fullName = value) } })
                    AuthField("Email address", state.email, { value -> viewModel.update { it.copy(email = value) } }, KeyboardType.Email)
                    if (!state.isForgotPassword) AuthField("Password", state.password, { value -> viewModel.update { it.copy(password = value) } }, secret = true)
                    if (state.isSignUp && !state.isForgotPassword) AuthField("Confirm password", state.confirmPassword, { value -> viewModel.update { it.copy(confirmPassword = value) } }, secret = true)
                    if (!state.isSignUp && !state.isForgotPassword) TextButton(onClick = { viewModel.update { it.copy(isForgotPassword = true, message = null) } }, modifier = Modifier.align(Alignment.End)) { Text("Forgot password?") }
                    if (state.isForgotPassword) TextButton(onClick = { viewModel.update { it.copy(isForgotPassword = false, message = null) } }, modifier = Modifier.align(Alignment.End)) { Text("Back to log in") }
                    state.message?.let { Text(it, color = if (state.isError) MaterialTheme.colorScheme.error else Green, modifier = Modifier.padding(vertical = 8.dp)) }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                        Text(if (state.busy) "Please wait..." else if (state.isForgotPassword) "Send reset link" else if (state.isSignUp) "Create account" else "Log in", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!state.isForgotPassword) Text("Your account is securely managed by Firebase.", color = Color(0xFF829AB1), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("By continuing, you agree to our Terms of Service and Privacy Policy.", color = Color(0xFF829AB1), fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AuthTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = modifier, colors = ButtonDefaults.textButtonColors(contentColor = if (selected) Navy else Color(0xFF627D98))) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    secret: Boolean = false
) {
    var show by remember { mutableStateOf(false) }
    Column(Modifier.padding(bottom = 14.dp)) {
        Text(label, fontWeight = FontWeight.Medium, color = Navy)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.Password else keyboardType),
            visualTransformation = if (secret && !show) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (secret) ({ TextButton(onClick = { show = !show }) { Text(if (show) "Hide" else "Show") } }) else null
        )
    }
}

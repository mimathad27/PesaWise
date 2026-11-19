package com.pesawise.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.pesawise.app.ui.theme.PesaInterFontFamily

private val Navy = Color(0xFF142B42)
private val Green = Color(0xFF0AAD62)
private val Canvas = Color(0xFFF4F6F8)
private val Slate = Color(0xFF71869A)

@Composable
fun PesaWiseApp(viewModel: AuthViewModel) {
    val state by viewModel.state
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var googleBusy by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(38.dp))
            Text("PesaWise", fontSize = 25.sp, fontWeight = FontWeight.Medium, color = Navy, fontFamily = PesaInterFontFamily)
            Spacer(Modifier.height(135.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 26.dp)) {
                    Text(
                        if (state.isForgotPassword) "Forgot password?" else if (state.isSignUp) "Create an Account?" else "Welcome to PesaWise\nLog in now!",
                        modifier = Modifier.fillMaxWidth(), fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold, color = Navy, textAlign = TextAlign.Center, fontFamily = PesaInterFontFamily
                    )
                    Spacer(Modifier.height(30.dp))
                    if (!state.isForgotPassword) Spacer(Modifier.height(4.dp))
                    if (state.isSignUp && !state.isForgotPassword) AuthField("Full name", state.fullName, { value -> viewModel.update { it.copy(fullName = value) } })
                    AuthField("Email address", state.email, { value -> viewModel.update { it.copy(email = value) } }, KeyboardType.Email)
                    if (!state.isForgotPassword) AuthField("Password", state.password, { value -> viewModel.update { it.copy(password = value) } }, secret = true)
                    if (state.isSignUp && !state.isForgotPassword) AuthField("Confirm password", state.confirmPassword, { value -> viewModel.update { it.copy(confirmPassword = value) } }, secret = true)
                    if (!state.isSignUp && !state.isForgotPassword) TextButton(onClick = { viewModel.update { it.copy(isForgotPassword = true, message = null) } }, modifier = Modifier.align(Alignment.End)) { Text("Forgot password?") }
                    if (state.isForgotPassword) TextButton(onClick = { viewModel.update { it.copy(isForgotPassword = false, message = null) } }, modifier = Modifier.align(Alignment.End)) { Text("Back to log in") }
                    state.message?.let { Text(it, color = if (state.isError) MaterialTheme.colorScheme.error else Green, modifier = Modifier.padding(vertical = 8.dp)) }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                        Text(if (state.busy) "Please wait..." else if (state.isForgotPassword) "Send reset link" else if (state.isSignUp) "Sign up" else "Log in", fontWeight = FontWeight.Medium)
                    }
                    if (!state.isForgotPassword) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(Modifier.weight(1f))
                            Text("  OR  ", color = Color(0xFF829AB1), fontSize = 12.sp)
                            HorizontalDivider(Modifier.weight(1f))
                        }
                        OutlinedButton(
                            onClick = {
                                val activity = context.findActivity()
                                if (activity == null) {
                                    viewModel.update { it.copy(message = "Google sign-in isn't available right now. Please try again.", isError = true) }
                                } else {
                                    val clientIdResource = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
                                    if (clientIdResource == 0) {
                                        viewModel.update { it.copy(message = "Google sign-in isn't available right now. Please use your email and password.", isError = true) }
                                    } else {
                                        googleBusy = true
                                        viewModel.update { it.copy(message = null, isError = false) }
                                        scope.launch {
                                            try {
                                                val googleSignInOption = GetSignInWithGoogleOption.Builder(
                                                    activity.getString(clientIdResource)
                                                ).build()
                                                val request = GetCredentialRequest.Builder()
                                                    .addCredentialOption(googleSignInOption)
                                                    .build()
                                                val result = CredentialManager.create(activity).getCredential(activity, request)
                                                val credential = result.credential
                                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                    viewModel.signInWithGoogle(googleCredential.idToken)
                                                } else {
                                                    viewModel.update { it.copy(message = "Google couldn't complete sign in. Try another account.", isError = true) }
                                                }
                                            } catch (error: GetCredentialException) {
                                                if (error !is GetCredentialCancellationException) {
                                                    viewModel.update { it.copy(message = error.localizedMessage ?: "Google sign-in failed. Please try again.", isError = true) }
                                                }
                                            } catch (error: Exception) {
                                                viewModel.update { it.copy(message = error.localizedMessage ?: "Google sign-in failed. Please try again.", isError = true) }
                                            } finally {
                                                googleBusy = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !state.busy && !googleBusy,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFFE3E7EA)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                        ) {
                            if (googleBusy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(if (googleBusy) "Connecting to Google..." else "Continue with Google", color = Navy, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (!state.isForgotPassword) TextButton(
                        onClick = { viewModel.update { it.copy(isSignUp = !it.isSignUp, message = null) } },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(
                            if (state.isSignUp) "Already have an account? Log in" else "New to PesaWise? Create an account",
                            color = Slate, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
    Column(Modifier.padding(bottom = 13.dp)) {
        Text(label, fontWeight = FontWeight.Medium, color = Navy, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp, bottom = 5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text(if (secret) "Enter your password" else "", color = Color(0xFF9AA8B4)) },
            leadingIcon = {
                Icon(
                    imageVector = when {
                        secret -> Icons.Outlined.Lock
                        keyboardType == KeyboardType.Email -> Icons.Outlined.Email
                        else -> Icons.Outlined.Person
                    },
                    contentDescription = null,
                    tint = Color(0xFF788995),
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                unfocusedBorderColor = Color(0xFFE1E5E8),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Green
            ),
            keyboardOptions = KeyboardOptions(keyboardType = if (secret) KeyboardType.Password else keyboardType),
            visualTransformation = if (secret && !show) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (secret) ({
                IconButton(onClick = { show = !show }) {
                    Icon(if (show) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, contentDescription = if (show) "Hide password" else "Show password", tint = Color(0xFF788995))
                }
            }) else null
        )
    }
}

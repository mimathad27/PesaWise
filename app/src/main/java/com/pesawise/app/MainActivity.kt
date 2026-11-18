package com.pesawise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesawise.app.ui.AuthViewModel
import com.pesawise.app.ui.AuthViewModelFactory
import com.pesawise.app.ui.OnboardingScreen
import com.pesawise.app.ui.PesaWiseApp
import com.pesawise.app.ui.SignedInScreen
import com.pesawise.app.ui.theme.PesaWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PesaWiseApplication
        val onboardingPrefs = getSharedPreferences("pesawise_onboarding", MODE_PRIVATE)
        setContent {
            PesaWiseTheme {
                val onboardingComplete = remember {
                    mutableStateOf(onboardingPrefs.getBoolean("complete", false))
                }
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(app.authRepository)
                )
                if (authViewModel.state.value.loggedIn) {
                    SignedInScreen(authViewModel, app.authRepository)
                } else if (!onboardingComplete.value) {
                    OnboardingScreen(
                        onComplete = {
                            onboardingPrefs.edit().putBoolean("complete", true).apply()
                            onboardingComplete.value = true
                        }
                    )
                } else {
                    PesaWiseApp(authViewModel)
                }
            }
        }
    }
}

package com.pesawise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesawise.app.ui.AuthViewModel
import com.pesawise.app.ui.AuthViewModelFactory
import com.pesawise.app.ui.ExpenseTrackingScreen
import com.pesawise.app.ui.PesaWiseApp
import com.pesawise.app.ui.theme.PesaWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as PesaWiseApplication).authRepository
        setContent {
            PesaWiseTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(repository)
                )

                if (authViewModel.state.value.loggedIn) {
                    ExpenseTrackingScreen()
                } else {
                    PesaWiseApp(authViewModel)
                }
            }
        }
    }
}
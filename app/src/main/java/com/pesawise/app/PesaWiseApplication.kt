package com.pesawise.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.pesawise.app.data.AuthRepository

class PesaWiseApplication : Application() {
    val authRepository: AuthRepository by lazy {
        AuthRepository(FirebaseAuth.getInstance())
    }
}

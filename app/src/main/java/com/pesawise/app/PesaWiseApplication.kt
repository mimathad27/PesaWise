package com.pesawise.app

import android.app.Application
import com.pesawise.app.data.AuthRepository
import com.pesawise.app.data.PesaWiseDatabase

class PesaWiseApplication : Application() {
    val authRepository: AuthRepository by lazy {
        AuthRepository(PesaWiseDatabase.getInstance(this).userDao())
    }
}

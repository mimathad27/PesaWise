package com.pesawise.app.ui

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class AppSettings(
    val currencyCode: String = "KES",
    val languageCode: String = "en",
    val budgetCycle: String = "Monthly",
    val privacyMode: Boolean = false
)

class AppSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    val state: MutableState<AppSettings> = mutableStateOf(load())

    fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(state.value)
        state.value = updated
        preferences.edit()
            .putString("currency_code", updated.currencyCode)
            .putString("language_code", updated.languageCode)
            .putString("budget_cycle", updated.budgetCycle)
            .putBoolean("privacy_mode", updated.privacyMode)
            .apply()
    }

    private fun load() = AppSettings(
        currencyCode = preferences.getString("currency_code", "KES") ?: "KES",
        languageCode = preferences.getString("language_code", "en") ?: "en",
        budgetCycle = preferences.getString("budget_cycle", "Monthly") ?: "Monthly",
        privacyMode = preferences.getBoolean("privacy_mode", false)
    )
}

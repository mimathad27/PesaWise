package com.pesawise.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Fetches daily reference rates and caches them for offline use. */
class CurrencyRatesRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("currency_rates", Context.MODE_PRIVATE)

    suspend fun rate(fromCode: String, toCode: String): Double? = withContext(Dispatchers.IO) {
        val from = fromCode.uppercase()
        val to = toCode.uppercase()
        if (from == to) return@withContext 1.0

        val key = "${from}_$to"
        val cachedRate = preferences.getFloat("${key}_rate", 0f).takeIf { it > 0f }?.toDouble()
        val cachedAt = preferences.getLong("${key}_time", 0L)
        val cacheFresh = System.currentTimeMillis() - cachedAt < 12 * 60 * 60 * 1000L
        if (cachedRate != null && cacheFresh) return@withContext cachedRate

        try {
            val connection = (URL("https://api.frankfurter.dev/v2/rate/${from.lowercase()}/${to.lowercase()}").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8_000
                readTimeout = 8_000
            }
            try {
                if (connection.responseCode !in 200..299) error("Exchange-rate service returned ${connection.responseCode}.")
                val payload = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(payload).getDouble("rate").takeIf { it > 0.0 }?.also { rate ->
                    preferences.edit()
                        .putFloat("${key}_rate", rate.toFloat())
                        .putLong("${key}_time", System.currentTimeMillis())
                        .apply()
                }
            } finally {
                connection.disconnect()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // A last-known rate is better than losing the selected display currency offline.
            cachedRate
        }
    }
}

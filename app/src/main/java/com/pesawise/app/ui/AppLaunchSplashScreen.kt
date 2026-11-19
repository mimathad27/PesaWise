package com.pesawise.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pesawise.app.R
import kotlinx.coroutines.delay

@Composable
fun AppLaunchSplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_300)
        onFinished()
    }

    Image(
        painter = painterResource(R.drawable.wallet_first_launch),
        contentDescription = "PesaWise wallet",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OnboardingNavy = Color(0xFF102A43)
private val OnboardingGreen = Color(0xFF0B8F70)
private val OnboardingCanvas = Color(0xFFF5F8FA)
private val OnboardingSlate = Color(0xFF627D98)

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val amount: String,
    val caption: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        "Know where your money goes",
        "Record your expenses and see your spending clearly, all in one place.",
        Icons.Filled.ReceiptLong,
        "KSh 2,450",
        "Tracked this week"
    ),
    OnboardingPage(
        "Build better money habits",
        "Notice your spending patterns and make choices that support your goals.",
        Icons.Filled.Insights,
        "Your progress",
        "Small steps add up"
    ),
    OnboardingPage(
        "Stay in control",
        "Keep your finances organized and feel more confident about what comes next.",
        Icons.Filled.Savings,
        "One clear view",
        "Your money, organized"
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var pageIndex by remember { mutableIntStateOf(-1) }
    val isWelcome = pageIndex == -1
    val page = onboardingPages.getOrNull(pageIndex)

    Surface(modifier = Modifier.fillMaxSize(), color = OnboardingCanvas) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(OnboardingGreen), Alignment.Center) {
                        Text("P", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("  PesaWise", color = OnboardingNavy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                if (!isWelcome) {
                    TextButton(onClick = onComplete) { Text("Skip", color = OnboardingSlate) }
                }
            }

            Spacer(Modifier.weight(1f))
            if (isWelcome) {
                WelcomeIllustration()
                Spacer(Modifier.height(38.dp))
                Text("Make your money\nwork smarter.", color = OnboardingNavy, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text("Track your spending, build better habits, and stay on top of your finances.", color = OnboardingSlate, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
            } else if (page != null) {
                FeatureIllustration(page)
                Spacer(Modifier.height(38.dp))
                Text(page.title, color = OnboardingNavy, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text(page.description, color = OnboardingSlate, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f))

            if (!isWelcome) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                    onboardingPages.indices.forEach { index ->
                        Box(
                            Modifier
                                .size(width = if (index == pageIndex) 22.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (index == pageIndex) OnboardingGreen else Color(0xFFD1DCE5))
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (isWelcome) pageIndex = 0
                    else if (pageIndex < onboardingPages.lastIndex) pageIndex++
                    else onComplete()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OnboardingGreen)
            ) {
                Text(if (isWelcome) "Get started" else if (pageIndex == onboardingPages.lastIndex) "Create account" else "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onComplete, modifier = Modifier.padding(top = 4.dp)) {
                Text(if (isWelcome) "I already have an account · Log in" else "Log in", color = OnboardingNavy, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun WelcomeIllustration() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        Box(Modifier.size(230.dp).clip(CircleShape).background(Color(0xFFE4F3EF)))
        Card(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 34.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFD9F2EA)), Alignment.Center) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = OnboardingGreen, modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Your balance", color = OnboardingSlate, fontSize = 12.sp)
                        Text("KSh 24,800", color = OnboardingNavy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(54.dp).fillMaxWidth()) {
                    listOf(0.45f, 0.72f, 0.55f, 1f, 0.67f, 0.84f, 0.58f).forEachIndexed { index, height ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height((46 * height).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (index == 3) OnboardingGreen else Color(0xFFB8E2D5))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureIllustration(page: OnboardingPage) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        Box(Modifier.size(230.dp).clip(CircleShape).background(Color(0xFFE4F3EF)))
        Card(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 36.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFD9F2EA)), Alignment.Center) {
                    Icon(page.icon, contentDescription = null, tint = OnboardingGreen, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(page.amount, color = OnboardingNavy, fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(page.caption, color = OnboardingSlate, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.DriverAccount
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEarningsScreen(
    driverAccount: DriverAccount,
    currentLanguage: AppLanguage,
    onInstantCashOut: (Double) -> Unit,
    onBack: () -> Unit
) {
    var showCashOutDialog by remember { mutableStateOf(false) }

    if (showCashOutDialog) {
        AlertDialog(
            onDismissRequest = { showCashOutDialog = false },
            title = { Text("Instant Bank Payout", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transfer full available balance to Chase Checking (•••• 7182)?",
                        color = Slate300,
                        fontSize = 13.sp
                    )
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Transfer Amount", fontSize = 11.sp, color = Slate400)
                            Text(
                                text = "$${String.format("%.2f", driverAccount.walletBalance)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                            Text(text = "Fee: $0.00 • Instant delivery in 60s", fontSize = 11.sp, color = Slate400)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInstantCashOut(driverAccount.walletBalance)
                        showCashOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("confirm_payout_button")
                ) {
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashOutDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings & Performance", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("earnings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Balance and Cash Out Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Available Balance", fontSize = 13.sp, color = Slate400)
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Active Courier",
                                    color = EmeraldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format("%.2f", driverAccount.walletBalance)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showCashOutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("cash_out_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LocalizedStrings.tr("cash_out", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Today Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Slate900
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Today's Earnings", fontSize = 11.sp, color = Slate400)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$${String.format("%.2f", driverAccount.todayEarnings)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Slate900
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Completed Trips", fontSize = 11.sp, color = Slate400)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${driverAccount.todayTripsCount}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }

            item {
                // Weekly Earnings Visual Bar Representation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(text = "Weekly Activity", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))

                        val weeklyStats = listOf(
                            Pair("Mon", 0.65f),
                            Pair("Tue", 0.85f),
                            Pair("Wed", 0.45f),
                            Pair("Thu", 0.90f),
                            Pair("Fri", 1.0f),
                            Pair("Sat", 0.75f),
                            Pair("Sun", 0.40f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyStats.forEach { (day, fraction) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.height(110.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height((70 * fraction).dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (day == "Fri") EmeraldPrimary else ElectricBlue.copy(alpha = 0.6f))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = day, fontSize = 10.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Performance Scores
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(text = "Performance Metrics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Acceptance Rate", fontSize = 11.sp, color = Slate400)
                                Text(text = "98%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldPrimary)
                            }
                            Column {
                                Text(text = "Customer Rating", fontSize = 11.sp, color = Slate400)
                                Text(text = "${driverAccount.rating} ★", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AmberWarning)
                            }
                            Column {
                                Text(text = "Cancellation Rate", fontSize = 11.sp, color = Slate400)
                                Text(text = "0.8%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate200)
                            }
                        }
                    }
                }
            }

            item {
                Text(text = "Recent Bank Payouts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }

            items(driverAccount.recentPayouts) { payout ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(text = "Checking •••• 7182", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                                Text(text = payout.date, fontSize = 11.sp, color = Slate400)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format("%.2f", payout.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(text = payout.status, fontSize = 11.sp, color = EmeraldPrimary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

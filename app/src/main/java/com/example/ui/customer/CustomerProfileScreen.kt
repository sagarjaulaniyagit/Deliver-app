package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.CustomerAccount
import com.example.model.DeliveryOrder
import com.example.ui.components.TopUpWalletDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    customerAccount: CustomerAccount,
    orderHistory: List<DeliveryOrder>,
    currentLanguage: AppLanguage,
    onAddFunds: (Double) -> Unit,
    onOpenLanguage: () -> Unit,
    onSwitchToDriver: () -> Unit,
    onBack: () -> Unit
) {
    var showTopUpDialog by remember { mutableStateOf(false) }

    if (showTopUpDialog) {
        TopUpWalletDialog(
            currentBalance = customerAccount.walletBalance,
            onAddFunds = onAddFunds,
            onDismiss = { showTopUpDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & Wallet", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenLanguage) {
                        Text(currentLanguage.flag, fontSize = 20.sp)
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
                Spacer(modifier = Modifier.height(8.dp))
                // User info card
                Surface(
                    color = Slate900,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(EmeraldPrimary, ElectricBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customerAccount.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customerAccount.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = customerAccount.email,
                                fontSize = 12.sp,
                                color = Slate400
                            )
                            Text(
                                text = customerAccount.phone,
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }

            item {
                // SwiftPay Wallet Card (Uber Cash style)
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldPrimary)
                                Text(
                                    text = LocalizedStrings.tr("wallet", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = { showTopUpDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                modifier = Modifier.testTag("topup_funds_button")
                            ) {
                                Text(LocalizedStrings.tr("add_funds", currentLanguage), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "$${String.format("%.2f", customerAccount.walletBalance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Auto-refills from linked Visa card when balance is low",
                            fontSize = 11.sp,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Slate800)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Saved Cards
                        Text(
                            text = "Payment Methods",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        customerAccount.savedCards.forEach { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                    Text(text = "${card.brand} ${card.last4}", fontSize = 13.sp, color = Color.White)
                                }
                                Text(text = card.expiry, fontSize = 11.sp, color = Slate400)
                            }
                        }
                    }
                }
            }

            item {
                // Saved Addresses section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Saved Delivery Locations",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        customerAccount.savedAddresses.forEach { addr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (addr.tag) {
                                            "WORK" -> Icons.Default.Work
                                            "HOME" -> Icons.Default.Home
                                            else -> Icons.Default.Warehouse
                                        },
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(text = addr.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
                                    Text(text = addr.fullAddress, fontSize = 11.sp, color = Slate400)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Switch to Driver Mode Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSwitchToDriver() }
                        .testTag("switch_to_driver_banner"),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = Color.Black)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Become a Courier Partner",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Earn money on your own schedule. Switch to Driver Mode.",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldPrimary)
                    }
                }
            }

            item {
                Text(
                    text = LocalizedStrings.tr("order_history", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(orderHistory) { order ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate900
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Text(text = order.trackingCode, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Text(
                                text = "$${String.format("%.2f", order.fare)}",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "From: ${order.pickupAddress}", fontSize = 11.sp, color = Slate400)
                        Text(text = "To: ${order.dropoffAddress}", fontSize = 11.sp, color = Slate400)

                        if (order.rating != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(12.dp))
                                Text(text = "${order.rating.toInt()} Stars • Rated", fontSize = 11.sp, color = AmberWarning)
                            }
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

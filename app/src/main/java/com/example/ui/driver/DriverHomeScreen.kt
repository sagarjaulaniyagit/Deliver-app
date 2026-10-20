package com.example.ui.driver

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.DeliveryOrder
import com.example.model.DeliveryStatus
import com.example.model.DriverAccount
import com.example.ui.components.InteractiveCourierMap
import com.example.ui.theme.*

@Composable
fun DriverHomeScreen(
    driverAccount: DriverAccount,
    activeOrder: DeliveryOrder?,
    incomingRequests: List<DeliveryOrder>,
    currentLanguage: AppLanguage,
    unreadNotificationsCount: Int,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onAdvanceTrip: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenCall: () -> Unit,
    onOpenEarnings: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSwitchToCustomer: () -> Unit
) {
    val incoming = incomingRequests.firstOrNull()

    Box(modifier = Modifier.fillMaxSize().background(Slate950)) {
        // Background interactive Map
        InteractiveCourierMap(
            order = activeOrder ?: incoming,
            isDriverView = true,
            modifier = Modifier.fillMaxSize()
        )

        // Top Navigation Floating Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Slate900.copy(alpha = 0.95f),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Driver Role & Switch Back
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onSwitchToCustomer() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = LocalizedStrings.tr("driver_mode", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LocalizedStrings.tr("switch_to_customer", currentLanguage),
                                fontSize = 11.sp,
                                color = EmeraldPrimary
                            )
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Online/Offline Status Switch & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Online Toggle Switch
                    FilterChip(
                        selected = driverAccount.isOnline,
                        onClick = { onToggleOnline(!driverAccount.isOnline) },
                        label = {
                            Text(
                                text = if (driverAccount.isOnline) "ONLINE" else "OFFLINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (driverAccount.isOnline) EmeraldPrimary else RoseError)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldPrimary,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        modifier = Modifier.testTag("driver_online_toggle")
                    )

                    IconButton(onClick = onOpenLanguage, modifier = Modifier.size(36.dp)) {
                        Text(text = currentLanguage.flag, fontSize = 18.sp)
                    }

                    IconButton(onClick = onOpenNotifications, modifier = Modifier.size(36.dp)) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = RoseError, contentColor = Color.White) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    IconButton(onClick = onOpenEarnings, modifier = Modifier.size(36.dp).testTag("driver_earnings_icon")) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Earnings", tint = AmberWarning, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        // Floating Stats HUD Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Slate900.copy(alpha = 0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Today", fontSize = 10.sp, color = Slate400)
                    Text(
                        text = "$${String.format("%.2f", driverAccount.todayEarnings)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Trips", fontSize = 10.sp, color = Slate400)
                    Text(
                        text = "${driverAccount.todayTripsCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Rating", fontSize = 10.sp, color = Slate400)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${driverAccount.rating}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Cockpit Panel: INCOMING REQUEST vs ACTIVE TRIP vs IDLE ONLINE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            if (incoming != null && activeOrder == null) {
                // INCOMING DISPATCH POPUP
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Slate900,
                    border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                )
                                Text(
                                    text = LocalizedStrings.tr("new_order_request", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", incoming.fare)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                        }

                        // Trip details
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TripOrigin, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = incoming.pickupAddress, fontSize = 12.sp, color = Color.White)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = incoming.dropoffAddress, fontSize = 12.sp, color = Color.White)
                                }
                                Divider(color = Slate700)
                                Text(
                                    text = "Package: ${incoming.packageCategory.title} (${incoming.packageWeightKg} kg) • Dist: ${incoming.distanceKm} km",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }

                        // Accept / Decline Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDeclineRequest(incoming.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("driver_decline_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                            ) {
                                Text(LocalizedStrings.tr("decline_request", currentLanguage))
                            }

                            Button(
                                onClick = { onAcceptRequest(incoming.id) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("driver_accept_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                            ) {
                                Text(
                                    text = LocalizedStrings.tr("accept_request", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            } else if (activeOrder != null) {
                // ACTIVE TRIP COCKPIT FOR DRIVER
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Slate900,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val missionTitle = when (activeOrder.status) {
                                    DeliveryStatus.DRIVER_ACCEPTED, DeliveryStatus.HEADING_TO_PICKUP -> "Heading to Pickup"
                                    DeliveryStatus.ARRIVED_AT_PICKUP -> "At Pickup Location"
                                    DeliveryStatus.IN_TRANSIT -> "In Transit to Recipient"
                                    DeliveryStatus.ARRIVED_AT_DROPOFF -> "At Delivery Dropoff"
                                    DeliveryStatus.DELIVERED -> "Delivery Completed"
                                    else -> "Active Mission"
                                }
                                Text(
                                    text = missionTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Payout: $${String.format("%.2f", activeOrder.fare)} • PIN: ${activeOrder.securityPin}",
                                    fontSize = 12.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Chat & Call buttons for driver
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilledTonalIconButton(
                                    onClick = onOpenChat,
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                        contentColor = EmeraldPrimary
                                    ),
                                    modifier = Modifier.testTag("driver_chat_button")
                                ) {
                                    Icon(Icons.Default.ChatBubble, contentDescription = "Chat", modifier = Modifier.size(18.dp))
                                }

                                FilledTonalIconButton(
                                    onClick = onOpenCall,
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Slate800,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("driver_call_button")
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Navigation target banner
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = ElectricBlue)
                                Column {
                                    val isHeadingPickup = activeOrder.status == DeliveryStatus.DRIVER_ACCEPTED || activeOrder.status == DeliveryStatus.HEADING_TO_PICKUP || activeOrder.status == DeliveryStatus.ARRIVED_AT_PICKUP
                                    Text(
                                        text = if (isHeadingPickup) "Pickup: ${activeOrder.senderName}" else "Dropoff: ${activeOrder.recipientName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isHeadingPickup) activeOrder.pickupAddress else activeOrder.dropoffAddress,
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }
                            }
                        }

                        // Driver Action Advance Button
                        val buttonLabel = when (activeOrder.status) {
                            DeliveryStatus.DRIVER_ACCEPTED, DeliveryStatus.HEADING_TO_PICKUP -> "I Arrived at Pickup"
                            DeliveryStatus.ARRIVED_AT_PICKUP -> LocalizedStrings.tr("mark_picked_up", currentLanguage)
                            DeliveryStatus.IN_TRANSIT -> "I Arrived at Dropoff Destination"
                            DeliveryStatus.ARRIVED_AT_DROPOFF -> LocalizedStrings.tr("mark_delivered", currentLanguage)
                            else -> "Trip Completed"
                        }

                        if (activeOrder.status != DeliveryStatus.DELIVERED) {
                            Button(
                                onClick = onAdvanceTrip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("driver_advance_trip_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                            ) {
                                Text(
                                    text = buttonLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // IDLE STATUS CARD
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (driverAccount.isOnline) EmeraldPrimary else Slate600)
                            )
                            Column {
                                Text(
                                    text = if (driverAccount.isOnline) LocalizedStrings.tr("driver_online", currentLanguage) else LocalizedStrings.tr("driver_offline", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (driverAccount.isOnline) "Scanning for nearby courier dispatches..." else "Turn online to start receiving orders",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }

                        Button(
                            onClick = { onToggleOnline(!driverAccount.isOnline) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (driverAccount.isOnline) Slate800 else EmeraldPrimary,
                                contentColor = if (driverAccount.isOnline) Slate300 else Color.Black
                            )
                        ) {
                            Text(
                                text = if (driverAccount.isOnline) "Go Offline" else "Go Online",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

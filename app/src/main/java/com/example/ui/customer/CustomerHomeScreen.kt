package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.*
import com.example.ui.components.InteractiveCourierMap
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    customerAccount: CustomerAccount,
    activeOrder: DeliveryOrder?,
    currentLanguage: AppLanguage,
    unreadNotificationsCount: Int,
    onBookCourier: (pickup: String, dropoff: String, vehicle: VehicleType, category: PackageCategory, weight: Double, notes: String, payment: PaymentType, promo: Boolean) -> Unit,
    onCancelOrder: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenCall: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onSwitchToDriver: () -> Unit
) {
    var pickupAddress by remember { mutableStateOf("450 Lexington Ave, Suite 12B, Manhattan") }
    var dropoffAddress by remember { mutableStateOf("88 Pine St, Financial Center, NYC") }
    var selectedVehicle by remember { mutableStateOf(VehicleType.BIKE) }
    var selectedCategory by remember { mutableStateOf(PackageCategory.DOCUMENTS) }
    var packageWeight by remember { mutableDoubleStateOf(1.5) }
    var instructions by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf(PaymentType.WALLET) }
    var promoCode by remember { mutableStateOf("") }
    var promoApplied by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Slate950)) {
        // Background Interactive Vector Map
        InteractiveCourierMap(
            order = activeOrder,
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
                // App Logo and Role Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onSwitchToDriver() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = LocalizedStrings.tr("app_title", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LocalizedStrings.tr("customer_mode", currentLanguage),
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Switch",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Action icons: Language, Notifications, Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Language button
                    IconButton(
                        onClick = onOpenLanguage,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("customer_lang_button")
                    ) {
                        Text(text = currentLanguage.flag, fontSize = 18.sp)
                    }

                    // Notifications button with badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("customer_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = RoseError,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Profile avatar button
                    IconButton(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("customer_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Slate700),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customerAccount.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheet / Card overlay: EITHER Active Tracking OR Booking Form
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            if (activeOrder == null) {
                // BOOKING BOTTOM SHEET
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Slate900,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Slate700)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = LocalizedStrings.tr("book_courier", currentLanguage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Pickup & Dropoff Address Section
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Pickup row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = LocalizedStrings.tr("pickup_location", currentLanguage),
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                        Text(
                                            text = pickupAddress,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Slate700
                                )

                                // Dropoff row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(RoseError)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = LocalizedStrings.tr("dropoff_location", currentLanguage),
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                        Text(
                                            text = dropoffAddress,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Saved address quick chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(customerAccount.savedAddresses) { addr ->
                                FilterChip(
                                    selected = dropoffAddress == addr.fullAddress,
                                    onClick = { dropoffAddress = addr.fullAddress },
                                    label = { Text(addr.title, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (addr.tag) {
                                                "WORK" -> Icons.Default.Work
                                                "HOME" -> Icons.Default.Home
                                                else -> Icons.Default.Warehouse
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = EmeraldPrimary,
                                        selectedLeadingIconColor = EmeraldPrimary
                                    )
                                )
                            }
                        }

                        // Package category selector
                        Text(
                            text = LocalizedStrings.tr("package_details", currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PackageCategory.entries) { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                        .testTag("cat_chip_${cat.name}"),
                                    color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Slate800,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary) else null
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = cat.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldPrimary else Color.White
                                        )
                                        Text(
                                            text = cat.weightEstimate,
                                            fontSize = 10.sp,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }

                        // Vehicle Fleet Selector (Uber-style)
                        Text(
                            text = LocalizedStrings.tr("choose_vehicle", currentLanguage),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate300
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            VehicleType.entries.forEach { vehicle ->
                                val isSelected = selectedVehicle == vehicle
                                val estimatedDist = 4.8
                                val fare = vehicle.baseFare + (estimatedDist * vehicle.perKmRate)
                                val finalFare = if (promoApplied) fare * 0.5 else fare

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedVehicle = vehicle }
                                        .testTag("vehicle_option_${vehicle.name}"),
                                    color = if (isSelected) Slate800 else Slate800.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Slate700),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (vehicle) {
                                                        VehicleType.BIKE -> Icons.Default.TwoWheeler
                                                        VehicleType.SEDAN -> Icons.Default.DirectionsCar
                                                        VehicleType.VAN -> Icons.Default.AirportShuttle
                                                        VehicleType.TRUCK -> Icons.Default.LocalShipping
                                                    },
                                                    contentDescription = vehicle.title,
                                                    tint = if (isSelected) EmeraldPrimary else Color.White
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = vehicle.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Max ${vehicle.maxWeightKg} kg • 12-15 min",
                                                    fontSize = 11.sp,
                                                    color = Slate400
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$${String.format("%.2f", finalFare)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isSelected) EmeraldPrimary else Color.White
                                            )
                                            if (promoApplied) {
                                                Text(
                                                    text = "$${String.format("%.2f", fare)}",
                                                    fontSize = 11.sp,
                                                    color = Slate500,
                                                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Promo code input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = promoCode,
                                onValueChange = { promoCode = it.uppercase() },
                                placeholder = { Text(LocalizedStrings.tr("enter_promo", currentLanguage), fontSize = 12.sp, color = Slate400) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("promo_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate800,
                                    unfocusedContainerColor = Slate800,
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    if (promoCode == "SWIFT50" || promoCode == "PROMO") {
                                        promoApplied = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate700, contentColor = Color.White),
                                modifier = Modifier.testTag("apply_promo_button")
                            ) {
                                Text(LocalizedStrings.tr("apply", currentLanguage))
                            }
                        }
                        if (promoApplied) {
                            Text(
                                text = LocalizedStrings.tr("promo_applied", currentLanguage),
                                color = EmeraldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Payment selector
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Slate800,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = EmeraldPrimary)
                                    Column {
                                        Text(
                                            text = selectedPayment.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Wallet Balance: $${String.format("%.2f", customerAccount.walletBalance)}",
                                            fontSize = 11.sp,
                                            color = Slate400
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        // cycle through payment options
                                        val nextIdx = (selectedPayment.ordinal + 1) % PaymentType.entries.size
                                        selectedPayment = PaymentType.entries[nextIdx]
                                    },
                                    modifier = Modifier.testTag("change_payment_button")
                                ) {
                                    Text("Change", color = EmeraldPrimary, fontSize = 12.sp)
                                }
                            }
                        }

                        // Primary Booking Button
                        Button(
                            onClick = {
                                onBookCourier(
                                    pickupAddress,
                                    dropoffAddress,
                                    selectedVehicle,
                                    selectedCategory,
                                    packageWeight,
                                    instructions,
                                    selectedPayment,
                                    promoApplied
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_dispatch_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = LocalizedStrings.tr("book_courier", currentLanguage),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // ACTIVE ORDER TRACKING SHEET
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Slate900,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Slate700)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Status Badge & Live Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val statusText = when (activeOrder.status) {
                                    DeliveryStatus.PENDING_SEARCH -> LocalizedStrings.tr("searching_courier", currentLanguage)
                                    DeliveryStatus.DRIVER_ACCEPTED -> LocalizedStrings.tr("driver_assigned", currentLanguage)
                                    DeliveryStatus.HEADING_TO_PICKUP -> LocalizedStrings.tr("heading_to_pickup", currentLanguage)
                                    DeliveryStatus.ARRIVED_AT_PICKUP -> LocalizedStrings.tr("arrived_at_pickup", currentLanguage)
                                    DeliveryStatus.IN_TRANSIT -> LocalizedStrings.tr("package_picked_up", currentLanguage)
                                    DeliveryStatus.ARRIVED_AT_DROPOFF -> LocalizedStrings.tr("arrived_at_dropoff", currentLanguage)
                                    DeliveryStatus.DELIVERED -> LocalizedStrings.tr("delivered", currentLanguage)
                                    DeliveryStatus.CANCELLED -> "Cancelled"
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tracking: ${activeOrder.trackingCode} • ${activeOrder.packageCategory.title}",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }

                            // Security Delivery PIN
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = LocalizedStrings.tr("delivery_code", currentLanguage),
                                        fontSize = 9.sp,
                                        color = Slate300
                                    )
                                    Text(
                                        text = activeOrder.securityPin,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }

                        // Linear progress indicator
                        val progressFraction = when (activeOrder.status) {
                            DeliveryStatus.PENDING_SEARCH -> 0.1f
                            DeliveryStatus.DRIVER_ACCEPTED -> 0.25f
                            DeliveryStatus.HEADING_TO_PICKUP -> 0.45f
                            DeliveryStatus.ARRIVED_AT_PICKUP -> 0.60f
                            DeliveryStatus.IN_TRANSIT -> 0.80f
                            DeliveryStatus.ARRIVED_AT_DROPOFF -> 0.95f
                            DeliveryStatus.DELIVERED -> 1.0f
                            DeliveryStatus.CANCELLED -> 0.0f
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = EmeraldPrimary,
                            trackColor = Slate800
                        )

                        // Assigned Driver Card
                        if (activeOrder.driver != null) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = activeOrder.driver.avatarInitials,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = activeOrder.driver.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Icon(Icons.Default.Star, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "${activeOrder.driver.rating}",
                                                    fontSize = 12.sp,
                                                    color = AmberWarning,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "${activeOrder.driver.vehicleModel} • ${activeOrder.driver.vehiclePlate}",
                                                fontSize = 12.sp,
                                                color = Slate400
                                            )
                                        }
                                    }

                                    // Chat & Call buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilledTonalIconButton(
                                            onClick = onOpenChat,
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = EmeraldPrimary.copy(alpha = 0.2f),
                                                contentColor = EmeraldPrimary
                                            ),
                                            modifier = Modifier.testTag("open_chat_button")
                                        ) {
                                            Icon(Icons.Default.ChatBubble, contentDescription = "Chat", modifier = Modifier.size(20.dp))
                                        }

                                        FilledTonalIconButton(
                                            onClick = onOpenCall,
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = Slate700,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("open_call_button")
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Order Actions (Cancel or Status details)
                        if (activeOrder.status != DeliveryStatus.DELIVERED) {
                            OutlinedButton(
                                onClick = onCancelOrder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cancel_active_order_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(LocalizedStrings.tr("cancel_delivery", currentLanguage))
                            }
                        }
                    }
                }
            }
        }
    }
}

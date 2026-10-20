package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.DeliveryRepository
import com.example.model.DeliveryStatus
import com.example.model.UserRole
import com.example.ui.chat.ChatScreen
import com.example.ui.components.CallSimulationDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.RatingAndReviewSheet
import com.example.ui.customer.CustomerHomeScreen
import com.example.ui.customer.CustomerProfileScreen
import com.example.ui.driver.DriverEarningsScreen
import com.example.ui.driver.DriverHomeScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.delay

object AppRoutes {
    const val CUSTOMER_DASHBOARD = "customer_dashboard"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val DRIVER_DASHBOARD = "driver_dashboard"
    const val DRIVER_EARNINGS = "driver_earnings"
    const val CHAT = "chat"
}

@Composable
fun AppNavigation(
    repository: DeliveryRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val currentRole by repository.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by repository.currentLanguage.collectAsStateWithLifecycle()
    val customerAccount by repository.customerAccount.collectAsStateWithLifecycle()
    val driverAccount by repository.driverAccount.collectAsStateWithLifecycle()
    val activeOrder by repository.activeOrder.collectAsStateWithLifecycle()
    val orderHistory by repository.orderHistory.collectAsStateWithLifecycle()
    val incomingRequests by repository.incomingDriverRequests.collectAsStateWithLifecycle()
    val chatMessages by repository.chatMessages.collectAsStateWithLifecycle()
    val notifications by repository.notifications.collectAsStateWithLifecycle()
    val isCallActive by repository.isCallActive.collectAsStateWithLifecycle()

    val unreadCount = notifications.count { !it.isRead }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var bannerNotificationText by remember { mutableStateOf<String?>(null) }

    // Start destination opens directly into the role dashboard without any login barrier
    val startDestination = remember(currentRole) {
        if (currentRole == UserRole.CUSTOMER) {
            AppRoutes.CUSTOMER_DASHBOARD
        } else {
            AppRoutes.DRIVER_DASHBOARD
        }
    }

    // Role switching reaction
    LaunchedEffect(currentRole) {
        val destination = if (currentRole == UserRole.CUSTOMER) {
            AppRoutes.CUSTOMER_DASHBOARD
        } else {
            AppRoutes.DRIVER_DASHBOARD
        }
        navController.navigate(destination) {
            popUpTo(navController.graph.id) { inclusive = false }
            launchSingleTop = true
        }
    }

    // Pop-in notification banner
    LaunchedEffect(notifications.size) {
        val latest = notifications.firstOrNull()
        if (latest != null && !latest.isRead) {
            bannerNotificationText = "${latest.title}: ${latest.message}"
            delay(4000)
            bannerNotificationText = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Customer Dashboard Route
            composable(AppRoutes.CUSTOMER_DASHBOARD) {
                CustomerHomeScreen(
                    customerAccount = customerAccount,
                    activeOrder = activeOrder,
                    currentLanguage = currentLanguage,
                    unreadNotificationsCount = unreadCount,
                    onBookCourier = { pickup, dropoff, vehicle, category, weight, notes, payment, promo ->
                        repository.bookCourier(pickup, dropoff, vehicle, category, weight, notes, payment, promo)
                    },
                    onCancelOrder = { repository.cancelActiveOrder() },
                    onOpenChat = { navController.navigate(AppRoutes.CHAT) },
                    onOpenCall = { repository.toggleCall(true) },
                    onOpenLanguage = { showLanguageDialog = true },
                    onOpenNotifications = { showNotificationsDialog = true },
                    onOpenProfile = { navController.navigate(AppRoutes.CUSTOMER_PROFILE) },
                    onSwitchToDriver = { repository.switchRole(UserRole.DRIVER) }
                )
            }

            // Customer Profile Sub-Route
            composable(AppRoutes.CUSTOMER_PROFILE) {
                CustomerProfileScreen(
                    customerAccount = customerAccount,
                    orderHistory = orderHistory,
                    currentLanguage = currentLanguage,
                    onAddFunds = { amt -> repository.addCustomerWalletFunds(amt) },
                    onOpenLanguage = { showLanguageDialog = true },
                    onSwitchToDriver = { repository.switchRole(UserRole.DRIVER) },
                    onBack = { navController.popBackStack() }
                )
            }

            // 2. Driver Dashboard Route
            composable(AppRoutes.DRIVER_DASHBOARD) {
                DriverHomeScreen(
                    driverAccount = driverAccount,
                    activeOrder = activeOrder,
                    incomingRequests = incomingRequests,
                    currentLanguage = currentLanguage,
                    unreadNotificationsCount = unreadCount,
                    onToggleOnline = { repository.driverToggleOnline(it) },
                    onAcceptRequest = { repository.driverAcceptRequest(it) },
                    onDeclineRequest = { repository.driverDeclineRequest(it) },
                    onAdvanceTrip = { repository.driverAdvanceTrip() },
                    onOpenChat = { navController.navigate(AppRoutes.CHAT) },
                    onOpenCall = { repository.toggleCall(true) },
                    onOpenEarnings = { navController.navigate(AppRoutes.DRIVER_EARNINGS) },
                    onOpenLanguage = { showLanguageDialog = true },
                    onOpenNotifications = { showNotificationsDialog = true },
                    onSwitchToCustomer = { repository.switchRole(UserRole.CUSTOMER) }
                )
            }

            // Driver Earnings Sub-Route
            composable(AppRoutes.DRIVER_EARNINGS) {
                DriverEarningsScreen(
                    driverAccount = driverAccount,
                    currentLanguage = currentLanguage,
                    onInstantCashOut = { amt -> repository.driverInstantCashOut(amt) },
                    onBack = { navController.popBackStack() }
                )
            }

            // 3. In-App Chat Route
            composable(AppRoutes.CHAT) {
                val contactName = if (currentRole == UserRole.CUSTOMER) {
                    activeOrder?.driver?.name ?: "Marcus Vance"
                } else {
                    activeOrder?.senderName ?: "Elena Rostova"
                }
                val contactSub = if (currentRole == UserRole.CUSTOMER) "Courier Partner" else "Customer"

                ChatScreen(
                    orderTrackingCode = activeOrder?.trackingCode ?: "SC-DELIVERY",
                    contactName = contactName,
                    contactSub = contactSub,
                    isCurrentCustomer = currentRole == UserRole.CUSTOMER,
                    messages = chatMessages,
                    currentLanguage = currentLanguage,
                    onSendMessage = { text ->
                        repository.sendChatMessage(text, currentRole == UserRole.CUSTOMER)
                    },
                    onCallClick = { repository.toggleCall(true) },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // In-App Notification Banner Pop-in
        AnimatedVisibility(
            visible = bannerNotificationText != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate900.copy(alpha = 0.98f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = bannerNotificationText ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Rating and Review Sheet automatically pops up upon order delivery for customer
        if (currentRole == UserRole.CUSTOMER && activeOrder?.status == DeliveryStatus.DELIVERED && activeOrder?.rating == null) {
            RatingAndReviewSheet(
                orderTrackingCode = activeOrder?.trackingCode ?: "",
                driverName = activeOrder?.driver?.name ?: "Courier",
                onSubmit = { stars, compliments, feedback, tip ->
                    repository.submitOrderRating(stars, compliments, feedback, tip)
                },
                onDismiss = {
                    repository.submitOrderRating(5.0, emptyList(), "", 0.0)
                }
            )
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onSelectLanguage = { repository.setLanguage(it) },
                onDismiss = { showLanguageDialog = false }
            )
        }

        // Notifications Center Dialog
        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                onMarkAllRead = { repository.markNotificationsRead() },
                onDismiss = { showNotificationsDialog = false },
                currentLanguage = currentLanguage
            )
        }

        // Call Simulation Dialog
        if (isCallActive) {
            val contactName = if (currentRole == UserRole.CUSTOMER) {
                activeOrder?.driver?.name ?: "Marcus Vance"
            } else {
                activeOrder?.senderName ?: "Elena Rostova"
            }
            val contactRole = if (currentRole == UserRole.CUSTOMER) "Courier Partner" else "Customer"

            CallSimulationDialog(
                contactName = contactName,
                contactRole = contactRole,
                onEndCall = { repository.toggleCall(false) }
            )
        }
    }
}

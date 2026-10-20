package com.example.data

import androidx.compose.ui.geometry.Offset
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.atan2

class DeliveryRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    // Current active role
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Current app language
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Customer profile & wallet
    private val _customerAccount = MutableStateFlow(CustomerAccount())
    val customerAccount: StateFlow<CustomerAccount> = _customerAccount.asStateFlow()

    // Driver profile & earnings
    private val _driverAccount = MutableStateFlow(DriverAccount())
    val driverAccount: StateFlow<DriverAccount> = _driverAccount.asStateFlow()

    // Active live order
    private val _activeOrder = MutableStateFlow<DeliveryOrder?>(null)
    val activeOrder: StateFlow<DeliveryOrder?> = _activeOrder.asStateFlow()

    // Order history
    private val _orderHistory = MutableStateFlow<List<DeliveryOrder>>(generateInitialHistory())
    val orderHistory: StateFlow<List<DeliveryOrder>> = _orderHistory.asStateFlow()

    // Driver incoming dispatch requests
    private val _incomingDriverRequests = MutableStateFlow<List<DeliveryOrder>>(emptyList())
    val incomingDriverRequests: StateFlow<List<DeliveryOrder>> = _incomingDriverRequests.asStateFlow()

    // Live in-app chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // In-app Push Notifications
    private val _notifications = MutableStateFlow<List<PushNotification>>(generateInitialNotifications())
    val notifications: StateFlow<List<PushNotification>> = _notifications.asStateFlow()

    // In-app call simulation dialog
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    // Background tracking simulation job
    private var simulationJob: Job? = null

    init {
        // Start simulated periodic driver dispatch if online
        startDriverRequestsSimulation()
    }

    fun setAuthenticatedUser(userId: String, displayName: String?, email: String?) {
        val resolvedName = displayName?.takeIf { it.isNotBlank() } ?: "User"
        val resolvedEmail = email ?: "user@example.com"
        _customerAccount.update { it.copy(id = userId, name = resolvedName, email = resolvedEmail) }
        _driverAccount.update { it.copy(id = userId, name = resolvedName, email = resolvedEmail) }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        val lang = _currentLanguage.value
        val roleName = if (role == UserRole.CUSTOMER) LocalizedStrings.tr("customer_mode", lang) else LocalizedStrings.tr("driver_mode", lang)
        addNotification("Switched to $roleName", "Active workspace changed successfully.", "ROLE")
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun bookCourier(
        pickupAddress: String,
        dropoffAddress: String,
        vehicleType: VehicleType,
        packageCategory: PackageCategory,
        weightKg: Double,
        instructions: String,
        paymentType: PaymentType,
        hasPromo: Boolean
    ) {
        val distance = 4.8
        var fareCalc = vehicleType.baseFare + (distance * vehicleType.perKmRate)
        if (hasPromo) fareCalc *= 0.5

        val pickup = Offset(180f, 260f)
        val dropoff = Offset(440f, 540f)
        val courierStart = Offset(120f, 180f)

        val newOrder = DeliveryOrder(
            id = "ORD-${System.currentTimeMillis() % 100000}",
            trackingCode = "SC-${(1000..9999).random()}",
            senderName = _customerAccount.value.name,
            senderPhone = _customerAccount.value.phone,
            pickupAddress = pickupAddress,
            pickupCoords = pickup,
            recipientName = "Sarah Jenkins",
            recipientPhone = "+1 (555) 302-8877",
            dropoffAddress = dropoffAddress,
            dropoffCoords = dropoff,
            vehicleType = vehicleType,
            packageCategory = packageCategory,
            packageWeightKg = weightKg,
            instructions = instructions.ifBlank { "Please handle carefully" },
            fare = fareCalc,
            paymentType = paymentType,
            status = DeliveryStatus.PENDING_SEARCH,
            driver = null,
            courierCoords = courierStart,
            courierBearingDegrees = 45f,
            estimatedMinutes = 14,
            distanceKm = distance,
            securityPin = "${(1000..9999).random()}",
            createdAtMillis = System.currentTimeMillis()
        )

        _activeOrder.value = newOrder

        // Deduct from wallet if wallet payment used
        if (paymentType == PaymentType.WALLET) {
            _customerAccount.update { it.copy(walletBalance = (it.walletBalance - fareCalc).coerceAtLeast(0.0)) }
        }

        // Initialize chat with welcome note
        _chatMessages.value = listOf(
            ChatMessage(
                id = "m_sys",
                orderId = newOrder.id,
                senderName = "SwiftCourier Dispatch",
                text = "Order placed! We are matching you with the nearest courier.",
                timestamp = "Just now",
                isFromCustomer = false
            )
        )

        addNotification(
            title = "Order Dispatched (${newOrder.trackingCode})",
            message = "Looking for nearby ${vehicleType.title} courier...",
            iconType = "ORDER"
        )

        // Launch simulation: Match courier after 2.5s, then advance
        scope.launch {
            delay(2500)
            val assignedDriver = DriverInfo()
            _activeOrder.update { order ->
                order?.copy(
                    status = DeliveryStatus.DRIVER_ACCEPTED,
                    driver = assignedDriver
                )
            }
            addNotification(
                title = "Courier Assigned!",
                message = "${assignedDriver.name} (${assignedDriver.vehicleModel}) is on the way.",
                iconType = "DRIVER"
            )

            // Add driver greeting in chat
            delay(1500)
            _chatMessages.update { list ->
                list + ChatMessage(
                    id = "m_drv_greet",
                    orderId = newOrder.id,
                    senderName = assignedDriver.name,
                    text = "Hello! I accepted your delivery order. Heading to pickup now.",
                    timestamp = "Just now",
                    isFromCustomer = false
                )
            }

            // Start movement simulation towards pickup then dropoff
            startCourierMovementSimulation()
        }
    }

    private fun startCourierMovementSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            val order = _activeOrder.value ?: return@launch
            val pickup = order.pickupCoords
            val dropoff = order.dropoffCoords

            // Phase 1: Move towards pickup
            _activeOrder.update { it?.copy(status = DeliveryStatus.HEADING_TO_PICKUP) }
            val stepsToPickup = 12
            for (i in 1..stepsToPickup) {
                delay(1000)
                if (_activeOrder.value?.status == DeliveryStatus.CANCELLED) return@launch
                val current = _activeOrder.value ?: return@launch
                val fraction = i / stepsToPickup.toFloat()
                val newX = current.courierCoords.x + (pickup.x - current.courierCoords.x) * 0.25f
                val newY = current.courierCoords.y + (pickup.y - current.courierCoords.y) * 0.25f
                val bearing = calculateBearing(current.courierCoords, Offset(newX, newY))
                val remainingMins = (14 * (1f - fraction * 0.35f)).toInt().coerceAtLeast(1)

                _activeOrder.update {
                    it?.copy(
                        courierCoords = Offset(newX, newY),
                        courierBearingDegrees = bearing,
                        estimatedMinutes = remainingMins
                    )
                }
            }

            // Phase 2: Arrived at pickup
            _activeOrder.update {
                it?.copy(
                    status = DeliveryStatus.ARRIVED_AT_PICKUP,
                    courierCoords = pickup,
                    estimatedMinutes = 10
                )
            }
            addNotification("Driver at Pickup", "Courier arrived at ${order.pickupAddress}", "STATUS")

            delay(3000)
            // Phase 3: Package Picked Up & In Transit
            _activeOrder.update { it?.copy(status = DeliveryStatus.IN_TRANSIT) }
            addNotification("Package Picked Up", "Courier is now heading to destination", "STATUS")

            // Phase 4: Move towards dropoff
            val stepsToDropoff = 20
            for (i in 1..stepsToDropoff) {
                delay(1200)
                if (_activeOrder.value?.status == DeliveryStatus.CANCELLED) return@launch
                val current = _activeOrder.value ?: return@launch
                val fraction = i / stepsToDropoff.toFloat()
                val targetX = pickup.x + (dropoff.x - pickup.x) * fraction
                val targetY = pickup.y + (dropoff.y - pickup.y) * fraction
                val bearing = calculateBearing(current.courierCoords, Offset(targetX, targetY))
                val remainingKm = (current.distanceKm * (1f - fraction)).coerceAtLeast(0.1)
                val remainingMins = (10 * (1f - fraction)).toInt().coerceAtLeast(1)

                _activeOrder.update {
                    it?.copy(
                        courierCoords = Offset(targetX, targetY),
                        courierBearingDegrees = bearing,
                        distanceKm = remainingKm,
                        estimatedMinutes = remainingMins
                    )
                }
            }

            // Phase 5: Arrived at dropoff
            _activeOrder.update {
                it?.copy(
                    status = DeliveryStatus.ARRIVED_AT_DROPOFF,
                    courierCoords = dropoff,
                    estimatedMinutes = 0
                )
            }
            addNotification(
                title = "Courier Arrived at Destination",
                message = "Please share security PIN (${order.securityPin}) with driver",
                iconType = "PIN"
            )

            delay(4000)
            // Phase 6: Delivered!
            completeDelivery()
        }
    }

    fun completeDelivery() {
        val current = _activeOrder.value ?: return
        val completed = current.copy(
            status = DeliveryStatus.DELIVERED,
            completedAtMillis = System.currentTimeMillis()
        )
        _activeOrder.value = completed
        _orderHistory.update { listOf(completed) + it }

        // Credit to Driver account
        _driverAccount.update {
            it.copy(
                todayEarnings = it.todayEarnings + current.fare,
                walletBalance = it.walletBalance + current.fare,
                todayTripsCount = it.todayTripsCount + 1,
                totalTrips = it.totalTrips + 1
            )
        }

        addNotification(
            title = "Delivery Completed! 📦",
            message = "Package handed over safely. Receipt is ready in your account.",
            iconType = "SUCCESS"
        )
    }

    fun cancelActiveOrder() {
        simulationJob?.cancel()
        _activeOrder.update { it?.copy(status = DeliveryStatus.CANCELLED) }
        addNotification("Order Cancelled", "Your courier request was cancelled.", "CANCEL")
        scope.launch {
            delay(1500)
            _activeOrder.value = null
        }
    }

    fun submitOrderRating(stars: Double, compliments: List<String>, feedback: String, tip: Double) {
        _activeOrder.update {
            it?.copy(
                rating = stars,
                reviewCompliments = compliments,
                reviewFeedback = feedback,
                driverTip = tip
            )
        }

        if (tip > 0) {
            _customerAccount.update { it.copy(walletBalance = (it.walletBalance - tip).coerceAtLeast(0.0)) }
            _driverAccount.update { it.copy(walletBalance = it.walletBalance + tip, todayEarnings = it.todayEarnings + tip) }
        }

        addNotification(
            title = "Thank You for Your Feedback!",
            message = "Rating submitted (${stars.toInt()} ★). ${if (tip > 0) "Tip of $$tip added." else ""}",
            iconType = "RATING"
        )
        // Reset active order after rating
        _activeOrder.value = null
    }

    fun sendChatMessage(text: String, isCustomer: Boolean) {
        val order = _activeOrder.value ?: return
        val sender = if (isCustomer) _customerAccount.value.name else (order.driver?.name ?: "Courier")
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            orderId = order.id,
            senderName = sender,
            text = text,
            timestamp = "Just now",
            isFromCustomer = isCustomer
        )
        _chatMessages.update { it + newMsg }

        // Simulate reply if customer sent message
        if (isCustomer) {
            scope.launch {
                delay(2000)
                val replyText = when {
                    text.contains("outside", true) || text.contains("here", true) -> "Got it! I see you, pulling up in 30 seconds."
                    text.contains("call", true) -> "Sure thing, calling you shortly."
                    text.contains("reception", true) -> "Understood, headed to the front desk now."
                    else -> "Message received, on my way safely!"
                }
                _chatMessages.update {
                    it + ChatMessage(
                        id = "msg_${System.currentTimeMillis()}",
                        orderId = order.id,
                        senderName = order.driver?.name ?: "Marcus Vance",
                        text = replyText,
                        timestamp = "Just now",
                        isFromCustomer = false
                    )
                }
                addNotification("New Message from Courier", replyText, "CHAT")
            }
        }
    }

    fun toggleCall(active: Boolean) {
        _isCallActive.value = active
    }

    // Driver specific actions
    fun driverToggleOnline(online: Boolean) {
        _driverAccount.update { it.copy(isOnline = online) }
        if (online) {
            addNotification("Online", "Ready to receive dispatch requests.", "STATUS")
        } else {
            addNotification("Offline", "You are now offline.", "STATUS")
            _incomingDriverRequests.value = emptyList()
        }
    }

    fun driverAcceptRequest(orderId: String) {
        val req = _incomingDriverRequests.value.firstOrNull { it.id == orderId } ?: return
        _incomingDriverRequests.value = emptyList()

        val accepted = req.copy(
            status = DeliveryStatus.DRIVER_ACCEPTED,
            driver = DriverInfo(
                id = _driverAccount.value.id,
                name = _driverAccount.value.name,
                phone = _driverAccount.value.phone,
                vehicleModel = _driverAccount.value.vehicleModel,
                vehiclePlate = _driverAccount.value.vehiclePlate
            )
        )
        _activeOrder.value = accepted
        addNotification("Delivery Accepted", "Navigate to pickup at ${accepted.pickupAddress}", "DRIVER")
    }

    fun driverDeclineRequest(orderId: String) {
        _incomingDriverRequests.update { list -> list.filterNot { it.id == orderId } }
    }

    fun driverAdvanceTrip() {
        val current = _activeOrder.value ?: return
        when (current.status) {
            DeliveryStatus.DRIVER_ACCEPTED -> {
                _activeOrder.update { it?.copy(status = DeliveryStatus.ARRIVED_AT_PICKUP) }
                addNotification("Arrived at Pickup", "Sender notified of your arrival", "STATUS")
            }
            DeliveryStatus.ARRIVED_AT_PICKUP -> {
                _activeOrder.update { it?.copy(status = DeliveryStatus.IN_TRANSIT) }
                addNotification("Trip Started", "Heading to ${current.dropoffAddress}", "STATUS")
            }
            DeliveryStatus.IN_TRANSIT -> {
                _activeOrder.update { it?.copy(status = DeliveryStatus.ARRIVED_AT_DROPOFF) }
                addNotification("Arrived at Dropoff", "Ready for handover & PIN confirmation", "STATUS")
            }
            DeliveryStatus.ARRIVED_AT_DROPOFF -> {
                completeDelivery()
            }
            else -> {}
        }
    }

    fun driverInstantCashOut(amount: Double) {
        val currentBal = _driverAccount.value.walletBalance
        if (amount <= currentBal && amount > 0) {
            _driverAccount.update {
                it.copy(
                    walletBalance = it.walletBalance - amount,
                    recentPayouts = listOf(
                        PayoutRecord("p_${System.currentTimeMillis()}", amount, "Just now", "Transferred")
                    ) + it.recentPayouts
                )
            }
            addNotification(
                title = "Bank Payout Processed",
                message = "$${String.format("%.2f", amount)} sent instantly to linked checking account.",
                iconType = "PAYOUT"
            )
        }
    }

    fun addCustomerWalletFunds(amount: Double) {
        _customerAccount.update { it.copy(walletBalance = it.walletBalance + amount) }
        addNotification("Wallet Top-Up", "$${String.format("%.2f", amount)} added to SwiftPay balance.", "WALLET")
    }

    fun addNotification(title: String, message: String, iconType: String = "STATUS") {
        val newNotif = PushNotification(
            id = "notif_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = "Just now",
            isRead = false,
            iconType = iconType
        )
        _notifications.update { listOf(newNotif) + it }
    }

    fun markNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    private fun startDriverRequestsSimulation() {
        scope.launch {
            while (isActive) {
                delay(16000)
                if (_driverAccount.value.isOnline && _activeOrder.value == null && _incomingDriverRequests.value.isEmpty()) {
                    // Generate dispatch request for driver
                    val newReq = DeliveryOrder(
                        id = "REQ-${(100..999).random()}",
                        trackingCode = "SC-${(1000..9999).random()}",
                        senderName = "Tech Corp Logistics",
                        senderPhone = "+1 (555) 890-1122",
                        pickupAddress = "72 Wall Street, Financial District",
                        pickupCoords = Offset(210f, 290f),
                        recipientName = "David Chen",
                        recipientPhone = "+1 (555) 441-9988",
                        dropoffAddress = "350 Hudson St, Tribeca",
                        dropoffCoords = Offset(410f, 490f),
                        vehicleType = _driverAccount.value.vehicleType,
                        packageCategory = PackageCategory.PARCEL,
                        packageWeightKg = 4.2,
                        instructions = "Deliver to 4th floor reception",
                        fare = 18.75,
                        paymentType = PaymentType.WALLET,
                        status = DeliveryStatus.PENDING_SEARCH,
                        driver = null,
                        courierCoords = Offset(160f, 220f),
                        courierBearingDegrees = 30f,
                        estimatedMinutes = 12,
                        distanceKm = 3.6,
                        securityPin = "7419",
                        createdAtMillis = System.currentTimeMillis()
                    )
                    _incomingDriverRequests.value = listOf(newReq)
                    addNotification("New Delivery Dispatch!", "$18.75 payout • 3.6 km distance", "DISPATCH")
                }
            }
        }
    }

    private fun calculateBearing(start: Offset, end: Offset): Float {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        return (angle + 90f + 360f) % 360f
    }

    private fun generateInitialHistory(): List<DeliveryOrder> {
        return listOf(
            DeliveryOrder(
                id = "ORD-94812",
                trackingCode = "SC-8421",
                senderName = "Elena Rostova",
                senderPhone = "+1 (555) 839-4412",
                pickupAddress = "450 Lexington Ave, Suite 12B",
                pickupCoords = Offset(200f, 250f),
                recipientName = "Liam Vance",
                recipientPhone = "+1 (555) 991-0021",
                dropoffAddress = "88 Pine St, Financial Center",
                dropoffCoords = Offset(430f, 520f),
                vehicleType = VehicleType.BIKE,
                packageCategory = PackageCategory.DOCUMENTS,
                packageWeightKg = 0.8,
                instructions = "Leave with front desk",
                fare = 12.50,
                paymentType = PaymentType.WALLET,
                status = DeliveryStatus.DELIVERED,
                driver = DriverInfo(name = "Marcus Vance", vehicleModel = "Honda Super Cub 125"),
                courierCoords = Offset(430f, 520f),
                courierBearingDegrees = 180f,
                estimatedMinutes = 0,
                distanceKm = 4.2,
                securityPin = "5821",
                createdAtMillis = System.currentTimeMillis() - 86400000L,
                completedAtMillis = System.currentTimeMillis() - 84000000L,
                rating = 5.0,
                reviewCompliments = listOf("Super Fast", "Polite Courier"),
                driverTip = 3.0
            ),
            DeliveryOrder(
                id = "ORD-91204",
                trackingCode = "SC-7109",
                senderName = "Elena Rostova",
                senderPhone = "+1 (555) 839-4412",
                pickupAddress = "78 Grand St, Apt 4F",
                pickupCoords = Offset(180f, 300f),
                recipientName = "Metro Medical Lab",
                recipientPhone = "+1 (555) 330-9941",
                dropoffAddress = "520 1st Ave, Medical District",
                dropoffCoords = Offset(410f, 470f),
                vehicleType = VehicleType.SEDAN,
                packageCategory = PackageCategory.FRAGILE,
                packageWeightKg = 3.2,
                instructions = "Keep upright, temperature sensitive",
                fare = 21.00,
                paymentType = PaymentType.CREDIT_CARD,
                status = DeliveryStatus.DELIVERED,
                driver = DriverInfo(name = "Carlos Ramirez", vehicleModel = "Toyota Prius"),
                courierCoords = Offset(410f, 470f),
                courierBearingDegrees = 120f,
                estimatedMinutes = 0,
                distanceKm = 6.1,
                securityPin = "9102",
                createdAtMillis = System.currentTimeMillis() - (86400000L * 3),
                completedAtMillis = System.currentTimeMillis() - (86400000L * 3 - 3600000L),
                rating = 5.0,
                reviewCompliments = listOf("Careful Handling", "On Time"),
                driverTip = 4.0
            )
        )
    }

    private fun generateInitialNotifications(): List<PushNotification> {
        return listOf(
            PushNotification(
                id = "n_1",
                title = "Welcome to SwiftCourier! 🚀",
                message = "Use code SWIFT50 on your booking to get 50% discount.",
                timestamp = "10m ago",
                isRead = false,
                iconType = "PROMO"
            ),
            PushNotification(
                id = "n_2",
                title = "Receipt for Trip SC-8421",
                message = "Payment of $12.50 processed successfully via SwiftPay.",
                timestamp = "Yesterday",
                isRead = true,
                iconType = "PAYMENT"
            )
        )
    }
}

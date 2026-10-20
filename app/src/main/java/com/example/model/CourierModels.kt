package com.example.model

import androidx.compose.ui.geometry.Offset

enum class UserRole {
    CUSTOMER,
    DRIVER
}

enum class VehicleType(
    val title: String,
    val description: String,
    val maxWeightKg: Int,
    val baseFare: Double,
    val perKmRate: Double,
    val speedMultiplier: Double
) {
    BIKE("Courier Express", "Best for docs & envelopes up to 5kg", 5, 4.50, 1.25, 1.2),
    SEDAN("Standard Car", "Small to medium boxes, protected", 25, 7.50, 1.75, 1.0),
    VAN("Cargo Van", "Large boxes, multiple parcels & gear", 150, 16.00, 3.20, 0.85),
    TRUCK("Heavy Hauler", "Bulky furniture, pallets up to 800kg", 800, 32.00, 4.80, 0.70)
}

enum class PackageCategory(val title: String, val weightEstimate: String) {
    DOCUMENTS("Documents / Letter", "0.5 kg"),
    ELECTRONICS("Gadget / Electronics", "2.0 kg"),
    PARCEL("Standard Box", "5.0 kg"),
    FRAGILE("Fragile / Glassware", "3.0 kg"),
    HEAVY("Heavy Hardware", "25.0 kg"),
    FOOD("Food / Grocery", "4.0 kg")
}

enum class PaymentType(val title: String, val subtitle: String) {
    WALLET("SwiftPay Wallet", "Instant payment with zero fees"),
    CREDIT_CARD("Visa •••• 4242", "Expires 08/28"),
    UPI("Instant UPI / Bank", "Fast direct bank transfer"),
    CASH("Cash on Delivery", "Pay directly to courier upon arrival")
}

enum class DeliveryStatus {
    PENDING_SEARCH,
    DRIVER_ACCEPTED,
    HEADING_TO_PICKUP,
    ARRIVED_AT_PICKUP,
    IN_TRANSIT,
    ARRIVED_AT_DROPOFF,
    DELIVERED,
    CANCELLED
}

data class DriverInfo(
    val id: String = "drv_101",
    val name: String = "Marcus Vance",
    val phone: String = "+1 (555) 234-8921",
    val rating: Double = 4.94,
    val totalDeliveries: Int = 1420,
    val vehicleModel: String = "Honda Super Cub 125",
    val vehiclePlate: String = "NYC • 7K29B",
    val avatarInitials: String = "MV",
    val acceptanceRate: Int = 98
)

data class DeliveryOrder(
    val id: String,
    val trackingCode: String,
    val senderName: String,
    val senderPhone: String,
    val pickupAddress: String,
    val pickupCoords: Offset,
    val recipientName: String,
    val recipientPhone: String,
    val dropoffAddress: String,
    val dropoffCoords: Offset,
    val vehicleType: VehicleType,
    val packageCategory: PackageCategory,
    val packageWeightKg: Double,
    val instructions: String,
    val fare: Double,
    val paymentType: PaymentType,
    val status: DeliveryStatus,
    val driver: DriverInfo?,
    val courierCoords: Offset,
    val courierBearingDegrees: Float,
    val estimatedMinutes: Int,
    val distanceKm: Double,
    val securityPin: String,
    val createdAtMillis: Long,
    val completedAtMillis: Long? = null,
    val rating: Double? = null,
    val reviewCompliments: List<String> = emptyList(),
    val reviewFeedback: String? = null,
    val driverTip: Double = 0.0
)

data class CustomerAccount(
    val id: String = "cust_001",
    val name: String = "Elena Rostova",
    val email: String = "elena.r@example.com",
    val phone: String = "+1 (555) 839-4412",
    val walletBalance: Double = 84.50,
    val savedCards: List<SavedCard> = listOf(
        SavedCard("c_1", "Visa", "•••• 4242", "08/28", true),
        SavedCard("c_2", "Mastercard", "•••• 8910", "11/27", false)
    ),
    val savedAddresses: List<SavedAddress> = listOf(
        SavedAddress("a_1", "Office / Studio", "450 Lexington Ave, Suite 12B, Manhattan", "WORK"),
        SavedAddress("a_2", "Home Loft", "78 Grand St, Apt 4F, SoHo", "HOME"),
        SavedAddress("a_3", "Central Warehouse", "1200 Dockside Way, Pier 17", "WAREHOUSE")
    )
)

data class SavedCard(
    val id: String,
    val brand: String,
    val last4: String,
    val expiry: String,
    val isDefault: Boolean
)

data class SavedAddress(
    val id: String,
    val title: String,
    val fullAddress: String,
    val tag: String
)

data class DriverAccount(
    val id: String = "drv_101",
    val name: String = "Marcus Vance",
    val email: String = "marcus.courier@example.com",
    val phone: String = "+1 (555) 234-8921",
    val vehicleType: VehicleType = VehicleType.BIKE,
    val vehicleModel: String = "Honda Super Cub 125",
    val vehiclePlate: String = "NYC • 7K29B",
    val isOnline: Boolean = true,
    val rating: Double = 4.94,
    val totalTrips: Int = 1420,
    val todayTripsCount: Int = 8,
    val todayEarnings: Double = 138.50,
    val walletBalance: Double = 422.90,
    val recentPayouts: List<PayoutRecord> = listOf(
        PayoutRecord("p_1", 210.00, "Yesterday, 6:00 PM", "Completed"),
        PayoutRecord("p_2", 185.50, "3 days ago", "Completed")
    )
)

data class PayoutRecord(
    val id: String,
    val amount: Double,
    val date: String,
    val status: String
)


data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val iconType: String = "STATUS"
)

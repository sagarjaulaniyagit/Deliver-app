package com.example.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing an authenticated user (Customer or Courier Partner)
 * configured for Kotlin Serialization and Cloud Firestore.
 */
@Keep
@Serializable
@IgnoreExtraProperties
data class User(
    @DocumentId
    @SerialName("id")
    val id: String = "",

    @PropertyName("userId")
    @SerialName("userId")
    val userId: String = "",

    @PropertyName("name")
    @SerialName("name")
    val name: String = "",

    @PropertyName("email")
    @SerialName("email")
    val email: String = "",

    @PropertyName("phone")
    @SerialName("phone")
    val phone: String = "",

    @PropertyName("role")
    @SerialName("role")
    val role: String = "CUSTOMER", // "CUSTOMER" or "DRIVER"

    @PropertyName("walletBalance")
    @SerialName("walletBalance")
    val walletBalance: Double = 0.0,

    @PropertyName("vehicleType")
    @SerialName("vehicleType")
    val vehicleType: String? = null,

    @PropertyName("vehicleModel")
    @SerialName("vehicleModel")
    val vehicleModel: String? = null,

    @PropertyName("vehiclePlate")
    @SerialName("vehiclePlate")
    val vehiclePlate: String? = null,

    @PropertyName("rating")
    @SerialName("rating")
    val rating: Double = 5.0,

    @PropertyName("totalTrips")
    @SerialName("totalTrips")
    val totalTrips: Int = 0,

    @PropertyName("isOnline")
    @SerialName("isOnline")
    val isOnline: Boolean = false,

    @PropertyName("createdAtMillis")
    @SerialName("createdAtMillis")
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Data model representing a local courier delivery order
 * configured for Kotlin Serialization and Cloud Firestore.
 */
@Keep
@Serializable
@IgnoreExtraProperties
data class CourierOrder(
    @DocumentId
    @SerialName("id")
    val id: String = "",

    @PropertyName("trackingCode")
    @SerialName("trackingCode")
    val trackingCode: String = "",

    @PropertyName("userId")
    @SerialName("userId")
    val userId: String = "",

    @PropertyName("senderName")
    @SerialName("senderName")
    val senderName: String = "",

    @PropertyName("senderPhone")
    @SerialName("senderPhone")
    val senderPhone: String = "",

    @PropertyName("pickupAddress")
    @SerialName("pickupAddress")
    val pickupAddress: String = "",

    @PropertyName("recipientName")
    @SerialName("recipientName")
    val recipientName: String = "",

    @PropertyName("recipientPhone")
    @SerialName("recipientPhone")
    val recipientPhone: String = "",

    @PropertyName("dropoffAddress")
    @SerialName("dropoffAddress")
    val dropoffAddress: String = "",

    @PropertyName("vehicleType")
    @SerialName("vehicleType")
    val vehicleType: String = "BIKE",

    @PropertyName("packageCategory")
    @SerialName("packageCategory")
    val packageCategory: String = "PARCEL",

    @PropertyName("packageWeightKg")
    @SerialName("packageWeightKg")
    val packageWeightKg: Double = 1.0,

    @PropertyName("instructions")
    @SerialName("instructions")
    val instructions: String = "",

    @PropertyName("fare")
    @SerialName("fare")
    val fare: Double = 0.0,

    @PropertyName("paymentType")
    @SerialName("paymentType")
    val paymentType: String = "WALLET",

    @PropertyName("status")
    @SerialName("status")
    val status: String = "PENDING_SEARCH",

    @PropertyName("driverId")
    @SerialName("driverId")
    val driverId: String? = null,

    @PropertyName("driverName")
    @SerialName("driverName")
    val driverName: String? = null,

    @PropertyName("driverPhone")
    @SerialName("driverPhone")
    val driverPhone: String? = null,

    @PropertyName("securityPin")
    @SerialName("securityPin")
    val securityPin: String = "",

    @PropertyName("estimatedMinutes")
    @SerialName("estimatedMinutes")
    val estimatedMinutes: Int = 15,

    @PropertyName("distanceKm")
    @SerialName("distanceKm")
    val distanceKm: Double = 0.0,

    @PropertyName("rating")
    @SerialName("rating")
    val rating: Double? = null,

    @PropertyName("reviewFeedback")
    @SerialName("reviewFeedback")
    val reviewFeedback: String? = null,

    @PropertyName("driverTip")
    @SerialName("driverTip")
    val driverTip: Double = 0.0,

    @PropertyName("createdAtMillis")
    @SerialName("createdAtMillis")
    val createdAtMillis: Long = System.currentTimeMillis(),

    @PropertyName("completedAtMillis")
    @SerialName("completedAtMillis")
    val completedAtMillis: Long? = null
)

/**
 * Data model representing an in-app chat message between customer and driver
 * configured for Kotlin Serialization and Cloud Firestore.
 */
@Keep
@Serializable
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId
    @SerialName("id")
    val id: String = "",

    @PropertyName("orderId")
    @SerialName("orderId")
    val orderId: String = "",

    @PropertyName("senderId")
    @SerialName("senderId")
    val senderId: String = "",

    @PropertyName("senderName")
    @SerialName("senderName")
    val senderName: String = "",

    @PropertyName("text")
    @SerialName("text")
    val text: String = "",

    @PropertyName("timestamp")
    @SerialName("timestamp")
    val timestamp: String = "Just now",

    @PropertyName("isFromCustomer")
    @SerialName("isFromCustomer")
    val isFromCustomer: Boolean = true,

    @PropertyName("isDelivered")
    @SerialName("isDelivered")
    val isDelivered: Boolean = true,

    @PropertyName("timestampMillis")
    @SerialName("timestampMillis")
    val timestampMillis: Long = System.currentTimeMillis()
)

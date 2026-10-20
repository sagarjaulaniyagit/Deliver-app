package com.example

import com.example.data.DeliveryRepository
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLanguageTranslations() {
        assertEquals("SwiftCourier", LocalizedStrings.tr("app_title", AppLanguage.ENGLISH))
        assertEquals("Modo Cliente", LocalizedStrings.tr("customer_mode", AppLanguage.SPANISH))
        assertEquals("Mode Client", LocalizedStrings.tr("customer_mode", AppLanguage.FRENCH))
        assertEquals("Kunden-Modus", LocalizedStrings.tr("customer_mode", AppLanguage.GERMAN))
        assertEquals("ग्राहक मोड", LocalizedStrings.tr("customer_mode", AppLanguage.HINDI))
        assertEquals("وضع العميل", LocalizedStrings.tr("customer_mode", AppLanguage.ARABIC))
    }

    @Test
    fun testVehicleTypeFareCalculations() {
        val bike = VehicleType.BIKE
        val distance = 5.0
        val baseFare = bike.baseFare + (distance * bike.perKmRate)
        assertTrue(baseFare > 10.0)

        val van = VehicleType.VAN
        val vanFare = van.baseFare + (distance * van.perKmRate)
        assertTrue(vanFare > baseFare)
    }

    @Test
    fun testRoleSwitchingAndWallet() {
        val repository = DeliveryRepository()
        assertEquals(UserRole.CUSTOMER, repository.currentRole.value)

        repository.switchRole(UserRole.DRIVER)
        assertEquals(UserRole.DRIVER, repository.currentRole.value)

        val initialBal = repository.customerAccount.value.walletBalance
        repository.addCustomerWalletFunds(50.0)
        assertEquals(initialBal + 50.0, repository.customerAccount.value.walletBalance, 0.01)
    }

    @Test
    fun testUserDataSerialization() {
        val user = User(
            id = "u_1",
            userId = "usr_100",
            name = "Alex Smith",
            email = "alex@example.com",
            role = "CUSTOMER",
            walletBalance = 55.0
        )
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val jsonStr = json.encodeToString(User.serializer(), user)
        assertTrue(jsonStr.contains("Alex Smith"))
        val decodedUser = json.decodeFromString(User.serializer(), jsonStr)
        assertEquals(user.name, decodedUser.name)

        val order = CourierOrder(
            id = "ord_1",
            trackingCode = "SC-9900",
            pickupAddress = "100 Broadway",
            dropoffAddress = "200 5th Ave",
            fare = 22.50
        )
        val orderJson = json.encodeToString(CourierOrder.serializer(), order)
        val decodedOrder = json.decodeFromString(CourierOrder.serializer(), orderJson)
        assertEquals(order.trackingCode, decodedOrder.trackingCode)

        val chat = ChatMessage(
            id = "m_1",
            orderId = "ord_1",
            text = "I have arrived"
        )
        val chatJson = json.encodeToString(ChatMessage.serializer(), chat)
        val decodedChat = json.decodeFromString(ChatMessage.serializer(), chatJson)
        assertEquals(chat.text, decodedChat.text)
    }
}

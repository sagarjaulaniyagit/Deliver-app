package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.DeliveryRepository
import com.example.navigation.AppNavigation
import com.example.ui.theme.SwiftCourierTheme

class MainActivity : ComponentActivity() {
    private val repository = DeliveryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SwiftCourierTheme {
                AppNavigation(repository = repository)
            }
        }
    }
}

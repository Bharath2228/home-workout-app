package com.bharath.homeforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bharath.homeforge.ui.HomeForgeApp
import com.bharath.homeforge.ui.theme.HomeForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeForgeTheme {
                HomeForgeApp()
            }
        }
    }
}

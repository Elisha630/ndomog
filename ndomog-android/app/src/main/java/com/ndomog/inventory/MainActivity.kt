package com.ndomog.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ndomog.inventory.presentation.AppNavigation
import com.ndomog.inventory.presentation.theme.NdomogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NdomogApplication
        setContent {
            NdomogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(authRepository = app.authRepository, database = app.database)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NdomogTheme {
        // AppNavigation(authRepository = AuthRepository()) // Provide a mock for preview if needed
        // Just show a simple text for preview
        androidx.compose.material3.Text("Ndomog Inventory Preview")
    }
}

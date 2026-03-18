package com.mbm.superapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mbm.superapp.core.theme.MBMSuperTheme
import com.mbm.superapp.core.theme.ThemeEngine
import com.mbm.superapp.core.navigation.AppNavigation
import com.mbm.superapp.data.api.BackendConfig
import com.mbm.superapp.data.api.SupabaseClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Supabase if configured
        if (BackendConfig.isConfigured) {
            SupabaseClient.init(BackendConfig.SUPABASE_URL, BackendConfig.SUPABASE_ANON_KEY)
        }

        enableEdgeToEdge()
        setContent {
            val themeEngine: ThemeEngine = viewModel(
                factory = ThemeEngine.Factory(applicationContext)
            )
            val themeState by themeEngine.themeState.collectAsState()

            MBMSuperTheme(themeState = themeState) {
                AppNavigation(themeEngine = themeEngine)
            }
        }
    }
}

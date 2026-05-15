package com.example.projectmatrix

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.projectmatrix.data.local.AppSettingsRepository
import com.example.projectmatrix.presentation.navigation.MinNavHost
import com.example.projectmatrix.ui.theme.ProjectMatrixTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
        }
        enableEdgeToEdge()
        setContent {
            val settings by appSettingsRepository.settings.collectAsState()
            ProjectMatrixTheme(themeMode = settings.themeMode) {
                MinNavHost()
            }
        }
    }
}

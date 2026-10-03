package com.khsuiti.knowhow.presentation.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.khsuiti.knowhow.presentation.common.ui.theme.AlfaDemobCalendarTheme
import com.khsuiti.knowhow.presentation.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			val navController = rememberNavController()
			val settingsViewModel: SettingsViewModel = hiltViewModel()
			val mainViewModel: MainViewModel = hiltViewModel()

			val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
			val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

			AlfaDemobCalendarTheme(
				themeMode = settingsState.themeMode
			) {
				if (startDestination != null) {
					MainScreen(
						navController = navController,
						startDestination = startDestination!!,
						settingsViewModel = settingsViewModel
					)
				}
			}
		}
	}
}

package com.khsuiti.knowhow.presentation.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
			val settingsViewModel: SettingsViewModel = hiltViewModel()
			val mainViewModel: MainViewModel = hiltViewModel()

			val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
			val appAuthState by mainViewModel.appAuthState.collectAsStateWithLifecycle()

			AlfaDemobCalendarTheme(
				themeMode = settingsState.themeMode
			) {
				if (!appAuthState.isLoading) {
					RootAppNavigation(
						appAuthState = appAuthState,
						settingsViewModel = settingsViewModel
					)
				}
			}
		}
	}
}

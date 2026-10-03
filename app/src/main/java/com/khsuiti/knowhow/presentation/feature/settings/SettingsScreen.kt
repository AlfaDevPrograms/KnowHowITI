package com.khsuiti.knowhow.presentation.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.PhotoCameraBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.presentation.common.CircleImagePicker
import com.khsuiti.knowhow.presentation.common.MySlider
import com.khsuiti.knowhow.presentation.common.MySwitch
import com.khsuiti.knowhow.presentation.common.MyText
import com.khsuiti.knowhow.presentation.common.ThemeSelector
import com.khsuiti.knowhow.presentation.common.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	viewModel: SettingsViewModel,
	onNavigateBack: () -> Unit = {}
) {
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current
	var isBackButtonDisabled by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			TopAppBar(
				modifier = Modifier.fillMaxWidth(),
				title = {
					MyText(
						modifier = Modifier,
						text = stringResource(R.string.settings),
						textColor = MaterialTheme.colorScheme.onBackground,
						maxTextSize = 28.sp,
						textStyle = Typography.titleSmall
					)
				},
				navigationIcon = {
					IconButton(
						onClick = {
							if (!isBackButtonDisabled) {
								isBackButtonDisabled = true
								onNavigateBack()
							}
						},
						enabled = !isBackButtonDisabled
					) {
						Icon(
							imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
							contentDescription = stringResource(R.string.back),
							tint = com.khsuiti.knowhow.presentation.common.ui.theme.color2
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color.Transparent
				),
			)
		},
		containerColor = Color.Transparent
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(4.dp),
		) {
			Spacer(modifier = Modifier.height(16.dp))
			MySlider(
				text = stringResource(R.string.background_dimming),
				value = state.dimmingLevel,
				update = { level -> SettingsIntent.UpdateDimmingLevel(level) },
				viewModel = viewModel
			)

			val editPhotoText = stringResource(R.string.edit_photo)
			val deletePhotoText = stringResource(R.string.delete_photo)

			CircleImagePicker(
				modifier = Modifier,
				icon = Icons.Rounded.PhotoCameraBack,
				text = stringResource(R.string.backgroung_select),
				onImageSelected = { uri ->
					viewModel.saveBackgroundImage(uri)
					Toast.makeText(context, editPhotoText, Toast.LENGTH_SHORT).show()
				},
				onImageDelete = {
					viewModel.deleteBackgroundImage()
					Toast.makeText(context, deletePhotoText, Toast.LENGTH_SHORT).show()
				}
			)

			val onAnimationText = stringResource(R.string.background_animation_on)
			val offAnimationText = stringResource(R.string.background_animation_off)

			MySwitch(
				modifier = Modifier.padding(horizontal = 16.dp),
				checked = state.isBackgroundAnimationEnabled,
				onCheckedChange = { enabled ->
					viewModel.handleIntent(SettingsIntent.ToggleBackgroundAnimation(enabled))
					val message = if (enabled) onAnimationText else offAnimationText
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
				},
				label = stringResource(R.string.background_animation),
				icon = Icons.Rounded.Animation
			)
			Spacer(modifier = Modifier.height(16.dp))
			val light = stringResource(R.string.theme_light)
			val dark = stringResource(R.string.theme_dark)
			val system = stringResource(R.string.theme_system)
			ThemeSelector(
				currentTheme = state.themeMode,
				onThemeSelected = { mode ->
					viewModel.handleIntent(SettingsIntent.SetThemeMode(mode))
					val message = when (mode) {
						ThemeMode.Light -> light
						ThemeMode.Dark -> dark
						ThemeMode.System -> system
					}
					Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
				}
			)
		}
	}
}

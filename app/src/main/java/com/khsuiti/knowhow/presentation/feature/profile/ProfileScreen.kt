package com.khsuiti.knowhow.presentation.feature.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.presentation.common.ui.theme.color1

@Composable
fun ProfileScreen(
	viewModel: ProfileViewModel,
	modifier: Modifier = Modifier,
	onNavigateToSettings: () -> Unit = {},
	onNavigateToAbout: () -> Unit = {},
	onLogoutSuccess: () -> Unit = {}
) {
	val state by viewModel.state.collectAsState()
	val logoutSuccess by viewModel.logoutSuccess.collectAsState()
	val context = LocalContext.current
	val shareMessage = stringResource(R.string.share)
	val loggedOutMessage = stringResource(R.string.logged_out)

	val systemDark = isSystemInDarkTheme()
	val themeMode by viewModel.themeMode.collectAsState()

	val isDark = when (themeMode) {
		ThemeMode.System -> systemDark
		ThemeMode.Light  -> false
		ThemeMode.Dark   -> true
	}
	val grayTextColor = if (isDark) Color.Gray else color1
	val profileIconColor = if (isDark) MaterialTheme.colorScheme.secondary else color1

	LaunchedEffect(Unit) {
		viewModel.handleIntent(ProfileIntent.LoadProfile)
	}

	LaunchedEffect(logoutSuccess) {
		if (logoutSuccess) {
			onLogoutSuccess()
			viewModel.resetLogoutState()
		}
	}

	Scaffold(
		containerColor = Color.Transparent
	) { innerPadding ->
		Column(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp)
		) {
			Spacer(modifier = Modifier.height(12.dp))

			// Header Title & Share Icon
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = stringResource(R.string.profile_title),
					style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.onBackground
				)
				IconButton(onClick = {
					Toast.makeText(context, shareMessage, Toast.LENGTH_SHORT).show()
				}) {
					Icon(
						imageVector = Icons.Rounded.Share,
						contentDescription = "Share",
						tint = profileIconColor
					)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			if (state.isLoading && state.accountInfo == null) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
				}
			} else {
				val account = state.accountInfo
				// Main Profile Card
				Card(
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
					elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column(modifier = Modifier.padding(20.dp)) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							AsyncImage(
								model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
								contentDescription = null,
								modifier = Modifier
									.size(64.dp)
									.clip(CircleShape),
								contentScale = ContentScale.Crop,
								error = painterResource(id = R.drawable.logo)
							)

							Spacer(modifier = Modifier.width(16.dp))

							Column {
								Text(
									text = if (account != null) {
										"${account.firstName} ${account.lastName}"
									} else if (state.isLoading) {
										"Загрузка профиля..."
									} else {
										state.error ?: "Профиль не загружен"
									},
									style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.onSurface,
								)
								Spacer(modifier = Modifier.height(2.dp))
								Text(
									text = account?.roleName ?: account?.email ?: "",
									style = MaterialTheme.typography.bodyMedium,
									color = grayTextColor
								)
								Text(
									text = account?.login?.let { "$it" } ?: "",
									style = MaterialTheme.typography.bodySmall,
									color = grayTextColor
								)
							}
						}

						Spacer(modifier = Modifier.height(20.dp))
						HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
						Spacer(modifier = Modifier.height(16.dp))

						// Stats Row
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Column(horizontalAlignment = Alignment.Start) {
								Text(
									text = stringResource(R.string.lessons_passed),
									style = MaterialTheme.typography.labelSmall,
									color = grayTextColor
								)
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = stringResource(R.string.lessons_count_format, state.completedLessonsCount),
									style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.secondary
								)
							}

							Column(horizontalAlignment = Alignment.Start) {
								Text(
									text = stringResource(R.string.balance_label),
									style = MaterialTheme.typography.labelSmall,
									color = grayTextColor
								)
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = stringResource(R.string.balance_count_format, state.balanceLessonsCount),
									style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.onSurface
								)
							}

							Column(horizontalAlignment = Alignment.Start) {
								Text(
									text = stringResource(R.string.active_courses),
									style = MaterialTheme.typography.labelSmall,
									color = grayTextColor
								)
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = stringResource(R.string.courses_count_format, state.activeCoursesCount),
									style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.onSurface
								)
							}
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(20.dp))

			// Settings Options Card
			Card(
				shape = RoundedCornerShape(24.dp),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
				elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
				modifier = Modifier.fillMaxWidth()
			) {
				Column(modifier = Modifier.padding(vertical = 8.dp)) {
					ProfileMenuItem(
						icon = Icons.Rounded.CreditCard,
						title = stringResource(R.string.payment_methods),
						trailingText = state.paymentMethod,
						iconColor = profileIconColor,
						grayTextColor = grayTextColor,
						onClick = {}
					)
					HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color.LightGray.copy(alpha = 0.2f))

					ProfileMenuItemWithSwitch(
						icon = Icons.Rounded.Notifications,
						title = stringResource(R.string.notifications),
						checked = state.notificationsEnabled,
						iconColor = profileIconColor,
						onCheckedChange = { viewModel.handleIntent(ProfileIntent.ToggleNotifications(it)) }
					)
					HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color.LightGray.copy(alpha = 0.2f))

					ProfileMenuItem(
						icon = Icons.Rounded.Settings,
						title = stringResource(R.string.account_settings),
						iconColor = profileIconColor,
						grayTextColor = grayTextColor,
						onClick = onNavigateToSettings
					)
					HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color.LightGray.copy(alpha = 0.2f))

					ProfileMenuItem(
						icon = Icons.AutoMirrored.Rounded.HelpOutline,
						title = stringResource(R.string.help_support),
						iconColor = profileIconColor,
						grayTextColor = grayTextColor,
						onClick = onNavigateToAbout
					)
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			// Logout Button
			Button(
				onClick = {
					viewModel.handleIntent(ProfileIntent.Logout)
					Toast.makeText(context, loggedOutMessage, Toast.LENGTH_SHORT).show()
				},
				shape = RoundedCornerShape(16.dp),
				colors = ButtonDefaults.buttonColors(
					containerColor = Color(0xFFFFF0F2),
					contentColor = Color(0xFFE53935)
				),
				modifier = Modifier
					.fillMaxWidth()
					.height(52.dp)
			) {
				Text(
					text = stringResource(R.string.logout),
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp
				)
			}

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@Composable
fun ProfileMenuItem(
	icon: ImageVector,
	title: String,
	trailingText: String? = null,
	iconColor: Color = MaterialTheme.colorScheme.secondary,
	grayTextColor: Color = Color.Gray,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 20.dp, vertical = 16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = iconColor,
			modifier = Modifier.size(22.dp)
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		if (trailingText != null) {
			Text(
				text = trailingText,
				style = MaterialTheme.typography.bodyMedium,
				color = grayTextColor
			)
		} else {
			Icon(
				imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
				contentDescription = null,
				tint = grayTextColor,
				modifier = Modifier.size(20.dp)
			)
		}
	}
}

@Composable
fun ProfileMenuItemWithSwitch(
	icon: ImageVector,
	title: String,
	checked: Boolean,
	iconColor: Color = MaterialTheme.colorScheme.secondary,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 20.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = iconColor,
			modifier = Modifier.size(22.dp)
		)
		Spacer(modifier = Modifier.width(16.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange,
			colors = SwitchDefaults.colors(
				checkedThumbColor = Color.White,
				checkedTrackColor = Color(0xFF00C853)
			)
		)
	}
}

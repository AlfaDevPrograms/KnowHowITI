package com.khsuiti.knowhow.presentation.feature.home

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khsuiti.knowhow.MainApplication
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.data.local.ThemeMode
import com.khsuiti.knowhow.presentation.common.MyText
import com.khsuiti.knowhow.presentation.common.OnboardingScreen
import com.khsuiti.knowhow.presentation.common.ui.theme.Typography
import com.khsuiti.knowhow.presentation.common.ui.theme.color1
import com.khsuiti.knowhow.responsesData.ServiceResponse
import com.khsuiti.knowhow.responsesData.TutorProfileWithUserResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalLayoutApi::class)
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeScreen(
	viewModel: HomeViewModel,
	modifier: Modifier = Modifier,
	onNavigateToTutorDetail: (String) -> Unit = {}
) {
	val state by viewModel.state.collectAsState()
	val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
	val context = LocalContext.current
	val focusManager = LocalFocusManager.current
	val scope = rememberCoroutineScope()

	val systemDark = isSystemInDarkTheme()
	val themeMode by viewModel.themeMode.collectAsState()

	val isDark = when (themeMode) {
		ThemeMode.System -> systemDark
		ThemeMode.Light  -> false
		ThemeMode.Dark   -> true
	}
	val grayTextColor = if (isDark) Color.Gray else color1

	// Splash Logic
	val hasShownSplash by viewModel.hasShownSplash
	val isSplashVisible = !hasShownSplash
	val splashAlpha by animateFloatAsState(
		targetValue = if (isSplashVisible) 1f else 0f,
		animationSpec = tween(durationMillis = 1000),
		label = "splashAlpha"
	)

	LaunchedEffect(Unit) {
		viewModel.handleIntent(HomeIntent.LoadData)
		if (!hasShownSplash) {
			delay(1000.milliseconds)
			viewModel.markSplashAsShown()
		}
	}

	if (!isOnboardingCompleted) {
		val application = context.applicationContext as MainApplication
		OnboardingScreen(
			context = context,
			onFinished = {
				scope.launch {
					viewModel.markOnboardingCompleted()
				}
			}
		)
	} else {
		Box(modifier = Modifier.fillMaxSize()) {
			Scaffold(
				containerColor = Color.Transparent
			) { innerPadding ->
				Column(
					modifier = modifier
						.fillMaxSize()
						.padding(innerPadding)
						.alpha(1f - splashAlpha)
						.padding(horizontal = 16.dp)
				) {
					Spacer(modifier = Modifier.height(8.dp))

					// Search Bar
					OutlinedTextField(
						value = state.searchQuery,
						onValueChange = { viewModel.handleIntent(HomeIntent.SearchQueryChanged(it)) },
						placeholder = {
							Text(
								text = stringResource(R.string.search_tutors_placeholder),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
							)
						},
						leadingIcon = {
							Icon(
								imageVector = Icons.Rounded.Search,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.secondary
							)
						},
						trailingIcon = {
							IconButton(onClick = {}) {
								Icon(
									imageVector = Icons.Rounded.FilterList,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.secondary
								)
							}
						},
						singleLine = true,
						keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
						keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
						shape = RoundedCornerShape(20.dp),
						colors = OutlinedTextFieldDefaults.colors(
							focusedBorderColor = MaterialTheme.colorScheme.secondary,
							unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
							focusedContainerColor = MaterialTheme.colorScheme.surface,
							unfocusedContainerColor = MaterialTheme.colorScheme.surface
						),
						modifier = Modifier.fillMaxWidth()
					)

					Spacer(modifier = Modifier.height(16.dp))

					// Popular subjects section
					Text(
						text = stringResource(R.string.popular_subjects),
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onBackground
					)

					Spacer(modifier = Modifier.height(8.dp))

					// Chips
					LazyRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						items(state.popularSubjects) { subject ->
							val isSelected = (subject == "Все эксперты" && state.selectedSubject == null) ||
									(subject == state.selectedSubject)
							FilterChip(
								selected = isSelected,
								onClick = { viewModel.handleIntent(HomeIntent.SelectSubjectFilter(subject)) },
								label = { Text(text = subject, fontSize = 13.sp) },
								shape = RoundedCornerShape(16.dp),
								colors = FilterChipDefaults.filterChipColors(
									selectedContainerColor = MaterialTheme.colorScheme.secondary,
									selectedLabelColor = Color.White,
									containerColor = MaterialTheme.colorScheme.surface,
									labelColor = MaterialTheme.colorScheme.onSurface
								)
							)
						}
					}

					Spacer(modifier = Modifier.height(16.dp))

					// Tutors List
					val filteredTutors = state.tutors.filter { tutor ->
						val matchesSearch = state.searchQuery.isEmpty() ||
								"${tutor.user.firstName} ${tutor.user.lastName}".contains(state.searchQuery, ignoreCase = true) ||
								(tutor.bio?.contains(state.searchQuery, ignoreCase = true) == true)
						matchesSearch
					}

					LazyColumn(
						verticalArrangement = Arrangement.spacedBy(16.dp),
						modifier = Modifier.fillMaxSize()
					) {
						items(filteredTutors) { tutor ->
							val service = state.services.find { it.tutor.idTutorProfile == tutor.idTutorProfile }
							TutorCardItem(
								tutor = tutor,
								service = service,
								grayTextColor = grayTextColor,
								onClick = { onNavigateToTutorDetail(tutor.idTutorProfile) }
							)
						}
					}
				}
			}
		}
	}

	// Splash Overlay
	if (splashAlpha > 0.01f) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer(alpha = splashAlpha)
				.background(MaterialTheme.colorScheme.onSecondary),
			contentAlignment = Alignment.Center
		) {
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Image(
					painter = painterResource(id = R.drawable.logo),
					contentDescription = null,
					modifier = Modifier
						.size(120.dp)
						.clip(RoundedCornerShape(24.dp)),
					contentScale = ContentScale.Crop
				)
				Spacer(modifier = Modifier.height(16.dp))
				MyText(
					text = stringResource(R.string.app_name),
					textStyle = Typography.bodyLarge,
					textColor = MaterialTheme.colorScheme.onPrimary,
					textAlign = TextAlign.Center,
					maxTextSize = 24.sp
				)
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorCardItem(
	tutor: TutorProfileWithUserResponse,
	service: ServiceResponse?,
	grayTextColor: Color,
	onClick: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box {
					AsyncImage(
						model = tutor.photoURL,
						contentDescription = null,
						modifier = Modifier
							.size(60.dp)
							.clip(CircleShape),
						contentScale = ContentScale.Crop,
						error = painterResource(id = R.drawable.logo)
					)
					Box(
						modifier = Modifier
							.size(12.dp)
							.clip(CircleShape)
							.background(Color(0xFF00C853))
							.align(Alignment.BottomEnd)
					)
				}

				Spacer(modifier = Modifier.width(12.dp))

				Column(modifier = Modifier.weight(1f)) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Text(
							text = "${tutor.user.firstName} ${tutor.user.lastName}",
							style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
							color = MaterialTheme.colorScheme.onSurface
						)
						if (tutor.isVerified) {
							Spacer(modifier = Modifier.width(4.dp))
							Icon(
								imageVector = Icons.Rounded.CheckCircle,
								contentDescription = "Verified",
								tint = MaterialTheme.colorScheme.secondary,
								modifier = Modifier.size(18.dp)
							)
						}
					}

					Text(
						text = tutor.bio?.take(35) ?: tutor.education.educationName,
						style = MaterialTheme.typography.bodySmall,
						color = grayTextColor
					)

					Row(verticalAlignment = Alignment.CenterVertically) {
						repeat(5) {
							Icon(
								imageVector = Icons.Rounded.Star,
								contentDescription = null,
								tint = Color(0xFFFFC107),
								modifier = Modifier.size(14.dp)
							)
						}
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "${tutor.averageRating ?: 5.0}",
							style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "(${tutor.reviewCount} reviews)",
							style = MaterialTheme.typography.bodySmall,
							color = grayTextColor
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			// Tags
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				val tags = service?.description?.split(",")?.map { it.trim() }
					?: listOf("Mathematics", "Physics", "Calculus")
				tags.forEach { tag ->
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = MaterialTheme.colorScheme.surfaceVariant
					) {
						Text(
							text = tag,
							style = MaterialTheme.typography.labelMedium,
							color = MaterialTheme.colorScheme.secondary,
							modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Bottom Rate & Action
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Column {
					Text(
						text = stringResource(R.string.hourly_rate),
						style = MaterialTheme.typography.labelSmall,
						color = grayTextColor
					)
					Text(
						text = "$${service?.priceInDollars ?: "39.30"}/hr",
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.secondary
					)
				}

				Column {
					Text(
						text = stringResource(R.string.experience_years),
						style = MaterialTheme.typography.labelSmall,
						color = grayTextColor
					)
					Text(
						text = "${tutor.experienceYear}+ Years",
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)
				}

				Button(
					onClick = onClick,
					shape = RoundedCornerShape(16.dp),
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
				) {
					Text(
						text = stringResource(R.string.contact_tutor),
						color = Color.White,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}
}

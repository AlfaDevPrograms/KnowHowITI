package com.khsuiti.knowhow.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.presentation.feature.about.AboutScreen
import com.khsuiti.knowhow.presentation.feature.auth.AuthScreen
import com.khsuiti.knowhow.presentation.feature.auth.AuthViewModel
import com.khsuiti.knowhow.presentation.feature.catalog.CatalogScreen
import com.khsuiti.knowhow.presentation.feature.catalog.CatalogViewModel
import com.khsuiti.knowhow.presentation.feature.home.HomeScreen
import com.khsuiti.knowhow.presentation.feature.home.HomeViewModel
import com.khsuiti.knowhow.presentation.feature.profile.ProfileScreen
import com.khsuiti.knowhow.presentation.feature.profile.ProfileViewModel
import com.khsuiti.knowhow.presentation.feature.settings.SettingsScreen
import com.khsuiti.knowhow.presentation.feature.settings.SettingsViewModel
import com.khsuiti.knowhow.presentation.feature.tutorschedule.TutorScheduleScreen
import com.khsuiti.knowhow.presentation.feature.tutorschedule.TutorScheduleViewModel
import com.khsuiti.knowhow.presentation.feature.tutorservices.TutorServicesScreen
import com.khsuiti.knowhow.presentation.feature.tutorservices.TutorServicesViewModel
import com.khsuiti.knowhow.presentation.feature.tutordetail.TutorDetailIntent
import com.khsuiti.knowhow.presentation.feature.tutordetail.TutorDetailScreen
import com.khsuiti.knowhow.presentation.feature.tutordetail.TutorDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

sealed class BottomNavItem(
	val route: String,
	val titleRes: Int,
	val icon: ImageVector
) {
	data object Home : BottomNavItem("home", R.string.nav_home, Icons.Rounded.Home)
	data object Catalog : BottomNavItem("catalog", R.string.nav_catalog, Icons.Rounded.Search)
	data object Lessons : BottomNavItem("lessons", R.string.nav_lessons, Icons.Rounded.CalendarMonth)
	data object Profile : BottomNavItem("profile", R.string.nav_profile, Icons.Rounded.Person)

	data object TutorServices : BottomNavItem("tutor_services", R.string.nav_services, Icons.Rounded.School)
	data object TutorSchedule : BottomNavItem("tutor_schedule", R.string.nav_schedule, Icons.Rounded.CalendarMonth)
}

@OptIn(ExperimentalTime::class)
@Composable
fun AppBackground(
	homeViewModel: HomeViewModel,
	settingsViewModel: SettingsViewModel,
	content: @Composable () -> Unit
) {
	val backgroundImageUri by homeViewModel.backgroundImageUri.collectAsState(initial = null)
	val settingsState by settingsViewModel.state.collectAsState()

	val configuration = LocalConfiguration.current
	val screenWidthDp = configuration.screenWidthDp.toFloat()
	val screenHeightDp = configuration.screenHeightDp.toFloat()

	val offsetX = remember { Animatable(0f) }
	val offsetY = remember { Animatable(0f) }
	val scale = remember { Animatable(1f) }

	LaunchedEffect(settingsState.isBackgroundAnimationEnabled) {
		if (!settingsState.isBackgroundAnimationEnabled) {
			offsetX.snapTo(0f)
			offsetY.snapTo(0f)
			scale.snapTo(1f)
			return@LaunchedEffect
		}

		val random = Random
		val duration = 20000
		while (true) {
			val newScale = 1f + random.nextFloat()
			val maxOffsetX = (newScale - 1f) * screenWidthDp / 2f
			val maxOffsetY = (newScale - 1f) * screenHeightDp / 2f
			val newOffsetX = (random.nextFloat() - 0.5f) * 2 * maxOffsetX
			val newOffsetY = (random.nextFloat() - 0.5f) * 2 * maxOffsetY

			launch { offsetX.animateTo(newOffsetX, tween(duration, easing = FastOutSlowInEasing)) }
			launch { offsetY.animateTo(newOffsetY, tween(duration, easing = FastOutSlowInEasing)) }
			launch { scale.animateTo(newScale, tween(duration, easing = FastOutSlowInEasing)) }

			delay(duration.milliseconds)
		}
	}

	Box(modifier = Modifier.fillMaxSize()) {
		backgroundImageUri?.let { uri ->
			AsyncImage(
				model = uri,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxSize()
					.graphicsLayer {
						translationX = offsetX.value
						translationY = offsetY.value
						scaleX = scale.value
						scaleY = scale.value
					}
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background.copy(alpha = settingsState.dimmingLevel))
		)

		content()
	}
}

@Composable
fun RootAppNavigation(
	appAuthState: AppAuthState,
	settingsViewModel: SettingsViewModel,
	authViewModel: AuthViewModel = hiltViewModel(),
	homeViewModel: HomeViewModel = hiltViewModel(),
	catalogViewModel: CatalogViewModel = hiltViewModel(),
	profileViewModel: ProfileViewModel = hiltViewModel(),
	tutorDetailViewModel: TutorDetailViewModel = hiltViewModel(),
	tutorServicesViewModel: TutorServicesViewModel = hiltViewModel(),
	tutorScheduleViewModel: TutorScheduleViewModel = hiltViewModel()
) {
	if (!appAuthState.isAuthenticated) {
		AuthScreen(
			viewModel = authViewModel,
			onAuthSuccess = {}
		)
	} else if (appAuthState.isTutor) {
		TutorMainScreen(
			settingsViewModel = settingsViewModel,
			homeViewModel = homeViewModel,
			profileViewModel = profileViewModel,
			tutorServicesViewModel = tutorServicesViewModel,
			tutorScheduleViewModel = tutorScheduleViewModel
		)
	} else {
		StudentMainScreen(
			settingsViewModel = settingsViewModel,
			homeViewModel = homeViewModel,
			catalogViewModel = catalogViewModel,
			profileViewModel = profileViewModel,
			tutorDetailViewModel = tutorDetailViewModel
		)
	}
}

@Composable
fun StudentMainScreen(
	settingsViewModel: SettingsViewModel,
	homeViewModel: HomeViewModel,
	catalogViewModel: CatalogViewModel,
	profileViewModel: ProfileViewModel,
	tutorDetailViewModel: TutorDetailViewModel
) {
	val navController = rememberNavController()
	val bottomNavItems = listOf(
		BottomNavItem.Home,
		BottomNavItem.Catalog,
		BottomNavItem.Lessons,
		BottomNavItem.Profile
	)

	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Home.route

	AppBackground(
		homeViewModel = homeViewModel,
		settingsViewModel = settingsViewModel
	) {
		Scaffold(
			containerColor = Color.Transparent,
			bottomBar = {
				NavigationBar(
					containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
					tonalElevation = 8.dp,
					modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
				) {
					bottomNavItems.forEach { item ->
						val isSelected = currentRoute == item.route
						NavigationBarItem(
							selected = isSelected,
							onClick = {
								if (currentRoute != item.route) {
									navController.navigate(item.route) {
										popUpTo(navController.graph.findStartDestination().id) {
											saveState = true
										}
										launchSingleTop = true
										restoreState = true
									}
								}
							},
							icon = {
								Icon(
									imageVector = item.icon,
									contentDescription = stringResource(item.titleRes),
									modifier = Modifier.size(24.dp)
								)
							},
							label = {
								Text(
									text = stringResource(item.titleRes),
									fontSize = 11.sp,
									fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
								)
							},
							colors = NavigationBarItemDefaults.colors(
								selectedIconColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1,
								selectedTextColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1,
								indicatorColor = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
								unselectedIconColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1.copy(alpha = 0.6f),
								unselectedTextColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1.copy(alpha = 0.6f)
							)
						)
					}
				}
			}
		) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = BottomNavItem.Home.route,
				modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
				enterTransition = { slideInHorizontally(tween(300, easing = EaseOutQuad)) { it } + fadeIn(tween(300)) },
				exitTransition = { slideOutHorizontally(tween(250, easing = EaseInQuad)) { -it / 3 } + fadeOut(tween(250)) },
				popEnterTransition = { slideInHorizontally(tween(300, easing = EaseOutQuad)) { -it } + fadeIn(tween(300)) },
				popExitTransition = { slideOutHorizontally(tween(250, easing = EaseInQuad)) { it / 3 } + fadeOut(tween(250)) }
			) {
				composable(BottomNavItem.Home.route) {
					HomeScreen(
						viewModel = homeViewModel,
						onNavigateToTutorDetail = { tutorId ->
							tutorDetailViewModel.handleIntent(TutorDetailIntent.LoadTutorDetail(tutorId))
							navController.navigate("tutor_detail/$tutorId")
						}
					)
				}

				composable(BottomNavItem.Catalog.route) {
					CatalogScreen(
						viewModel = catalogViewModel,
						onSelectSubject = { subjectId ->
							navController.navigate(BottomNavItem.Home.route) {
								popUpTo(navController.graph.findStartDestination().id) {
									saveState = true
								}
								launchSingleTop = true
							}
						}
					)
				}

				composable(BottomNavItem.Lessons.route) {
					LessonsScreen()
				}

				composable(BottomNavItem.Profile.route) {
					ProfileScreen(
						viewModel = profileViewModel,
						onNavigateToSettings = { navController.navigate("settings") },
						onNavigateToAbout = { navController.navigate("about") },
						onLogoutSuccess = {
							homeViewModel.clearState()
							catalogViewModel.clearState()
							profileViewModel.clearState()
						}
					)
				}

				composable(
					route = "tutor_detail/{tutorId}",
					arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
				) {
					TutorDetailScreen(
						viewModel = tutorDetailViewModel,
						onNavigateBack = { navController.popBackStack() }
					)
				}

				composable("settings") {
					SettingsScreen(
						viewModel = settingsViewModel,
						onNavigateBack = { navController.popBackStack() }
					)
				}

				composable("about") {
					AboutScreen(
						onNavigateBack = { navController.popBackStack() }
					)
				}
			}
		}
	}
}

@Composable
fun TutorMainScreen(
	settingsViewModel: SettingsViewModel,
	homeViewModel: HomeViewModel,
	profileViewModel: ProfileViewModel,
	tutorServicesViewModel: TutorServicesViewModel,
	tutorScheduleViewModel: TutorScheduleViewModel
) {
	val navController = rememberNavController()
	val bottomNavItems = listOf(
		BottomNavItem.TutorServices,
		BottomNavItem.TutorSchedule,
		BottomNavItem.Profile
	)

	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.TutorServices.route

	AppBackground(
		homeViewModel = homeViewModel,
		settingsViewModel = settingsViewModel
	) {
		Scaffold(
			containerColor = Color.Transparent,
			bottomBar = {
				NavigationBar(
					containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
					tonalElevation = 8.dp,
					modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
				) {
					bottomNavItems.forEach { item ->
						val isSelected = currentRoute == item.route
						NavigationBarItem(
							selected = isSelected,
							onClick = {
								if (currentRoute != item.route) {
									navController.navigate(item.route) {
										popUpTo(navController.graph.findStartDestination().id) {
											saveState = true
										}
										launchSingleTop = true
										restoreState = true
									}
								}
							},
							icon = {
								Icon(
									imageVector = item.icon,
									contentDescription = stringResource(item.titleRes),
									modifier = Modifier.size(24.dp)
								)
							},
							label = {
								Text(
									text = stringResource(item.titleRes),
									fontSize = 11.sp,
									fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
								)
							},
							colors = NavigationBarItemDefaults.colors(
								selectedIconColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1,
								selectedTextColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1,
								indicatorColor = com.khsuiti.knowhow.presentation.common.ui.theme.color2,
								unselectedIconColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1.copy(alpha = 0.6f),
								unselectedTextColor = com.khsuiti.knowhow.presentation.common.ui.theme.color1.copy(alpha = 0.6f)
							)
						)
					}
				}
			}
		) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = BottomNavItem.TutorServices.route,
				modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
				enterTransition = { slideInHorizontally(tween(300, easing = EaseOutQuad)) { it } + fadeIn(tween(300)) },
				exitTransition = { slideOutHorizontally(tween(250, easing = EaseInQuad)) { -it / 3 } + fadeOut(tween(250)) },
				popEnterTransition = { slideInHorizontally(tween(300, easing = EaseOutQuad)) { -it } + fadeIn(tween(300)) },
				popExitTransition = { slideOutHorizontally(tween(250, easing = EaseInQuad)) { it / 3 } + fadeOut(tween(250)) }
			) {
				composable(BottomNavItem.TutorServices.route) {
					TutorServicesScreen(viewModel = tutorServicesViewModel)
				}

				composable(BottomNavItem.TutorSchedule.route) {
					TutorScheduleScreen(viewModel = tutorScheduleViewModel)
				}

				composable(BottomNavItem.Profile.route) {
					ProfileScreen(
						viewModel = profileViewModel,
						onNavigateToSettings = { navController.navigate("settings") },
						onNavigateToAbout = { navController.navigate("about") },
						onLogoutSuccess = {
							profileViewModel.clearState()
							tutorServicesViewModel.clearState()
							tutorScheduleViewModel.clearState()
						}
					)
				}

				composable("settings") {
					SettingsScreen(
						viewModel = settingsViewModel,
						onNavigateBack = { navController.popBackStack() }
					)
				}

				composable("about") {
					AboutScreen(
						onNavigateBack = { navController.popBackStack() }
					)
				}
			}
		}
	}
}

@Composable
fun LessonsScreen() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Icon(
				imageVector = Icons.Rounded.CalendarMonth,
				contentDescription = null,
				modifier = Modifier.size(64.dp),
				tint = Color(0xFF1E90FF)
			)
			Spacer(modifier = Modifier.height(16.dp))
			Text(
				text = stringResource(R.string.lessons_title),
				style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = stringResource(R.string.no_lessons),
				style = MaterialTheme.typography.bodyMedium,
				color = Color.Gray
			)
		}
	}
}

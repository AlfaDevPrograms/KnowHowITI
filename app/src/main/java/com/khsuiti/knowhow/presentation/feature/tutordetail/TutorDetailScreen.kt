package com.khsuiti.knowhow.presentation.feature.tutordetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khsuiti.knowhow.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TutorDetailScreen(
	viewModel: TutorDetailViewModel,
	modifier: Modifier = Modifier,
	onNavigateBack: () -> Unit = {}
) {
	val state by viewModel.state.collectAsState()
	val tutor = state.tutor
	val service = state.service
	val context = LocalContext.current
	val bookingSuccessMessage = stringResource(R.string.booking_success)
	val shareMessage = stringResource(R.string.share)

	state.error?.let { err ->
		LaunchedEffect(err) {
			Toast.makeText(context, err, Toast.LENGTH_LONG).show()
		}
	}

	LaunchedEffect(state.bookingSuccess) {
		if (state.bookingSuccess) {
			Toast.makeText(context, bookingSuccessMessage, Toast.LENGTH_SHORT).show()
			onNavigateBack()
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

			// Header Bar
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(onClick = onNavigateBack) {
					Icon(
						imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
						contentDescription = "Back",
						tint = MaterialTheme.colorScheme.onBackground
					)
				}

				Text(
					text = stringResource(R.string.tutor_profile_title),
					style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.onBackground
				)

				IconButton(onClick = {
					Toast.makeText(context, shareMessage, Toast.LENGTH_SHORT).show()
				}) {
					Icon(
						imageVector = Icons.Rounded.Share,
						contentDescription = "Share",
						tint = MaterialTheme.colorScheme.onBackground
					)
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			if (tutor != null) {
				// Tutor Info Card
				Card(
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
					elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column(
						modifier = Modifier.padding(20.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Box {
							AsyncImage(
								model = tutor.photoURL,
								contentDescription = null,
								modifier = Modifier
									.size(80.dp)
									.clip(CircleShape),
								contentScale = ContentScale.Crop,
								error = painterResource(id = R.drawable.logo)
							)
							Box(
								modifier = Modifier
									.size(14.dp)
									.clip(CircleShape)
									.background(Color(0xFF00C853))
									.align(Alignment.BottomEnd)
							)
						}

						Spacer(modifier = Modifier.height(12.dp))

						Row(verticalAlignment = Alignment.CenterVertically) {
							Text(
								text = "Tutor ${tutor.user.firstName} ${tutor.user.lastName}",
								style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
								color = MaterialTheme.colorScheme.onSurface
							)
							if (tutor.isVerified) {
								Spacer(modifier = Modifier.width(6.dp))
								Icon(
									imageVector = Icons.Rounded.CheckCircle,
									contentDescription = "Verified",
									tint = MaterialTheme.colorScheme.secondary,
									modifier = Modifier.size(20.dp)
								)
							}
						}

						Text(
							text = "Subject Tutor & Consultant",
							style = MaterialTheme.typography.bodyMedium,
							color = Color.Gray
						)

						Spacer(modifier = Modifier.height(6.dp))

						Row(verticalAlignment = Alignment.CenterVertically) {
							repeat(5) {
								Icon(
									imageVector = Icons.Rounded.Star,
									contentDescription = null,
									tint = Color(0xFFFFC107),
									modifier = Modifier.size(16.dp)
								)
							}
							Spacer(modifier = Modifier.width(6.dp))
							Text(
								text = "${tutor.averageRating ?: 4.9}",
								style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
							)
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = "(${tutor.reviewCount} reviews)",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.Gray
							)
						}

						Spacer(modifier = Modifier.height(20.dp))
						HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
						Spacer(modifier = Modifier.height(16.dp))

						// Stats Row
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceAround
						) {
							Column(horizontalAlignment = Alignment.CenterHorizontally) {
								Text(text = stringResource(R.string.rate), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
								Spacer(modifier = Modifier.height(4.dp))
								Text(text = "$${service?.priceInDollars ?: "39.30"}/hr", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
							}

							Column(horizontalAlignment = Alignment.CenterHorizontally) {
								Text(text = stringResource(R.string.experience), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
								Spacer(modifier = Modifier.height(4.dp))
								Text(text = "${tutor.experienceYear}+ Years", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
							}

							Column(horizontalAlignment = Alignment.CenterHorizontally) {
								Text(text = stringResource(R.string.students_helped), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
								Spacer(modifier = Modifier.height(4.dp))
								Text(text = "450+ Helped", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
							}
						}
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// About Card
				Card(
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
					elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column(modifier = Modifier.padding(20.dp)) {
						Text(
							text = stringResource(R.string.about_and_expertise),
							style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
							color = MaterialTheme.colorScheme.onSurface
						)

						Spacer(modifier = Modifier.height(12.dp))

						FlowRow(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							val tags = service?.description?.split(",")?.map { it.trim() }
								?: listOf("Mathematics", "Physics", "Calculus", "SAT Math")
							tags.forEach { tag ->
								Surface(
									shape = RoundedCornerShape(12.dp),
									color = MaterialTheme.colorScheme.surfaceVariant
								) {
									Text(
										text = tag,
										style = MaterialTheme.typography.labelMedium,
										color = MaterialTheme.colorScheme.secondary,
										modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
									)
								}
							}
						}

						Spacer(modifier = Modifier.height(12.dp))

						Text(
							text = tutor.bio ?: "",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Date & Time Selection Card
				Card(
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
					elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Column(modifier = Modifier.padding(20.dp)) {
						Text(
							text = stringResource(R.string.select_date_time),
							style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
							color = MaterialTheme.colorScheme.onSurface
						)

						Spacer(modifier = Modifier.height(12.dp))

						// Days Chips Row
						if (state.availableDays.isEmpty()) {
							Text(
								text = "Нет доступных дат",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.Gray
							)
						} else {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								state.availableDays.forEach { (dayName, dayNum) ->
									val dateKey = "$dayName $dayNum"
									val isSelected = dateKey == state.selectedDay
									Box(
										modifier = Modifier
											.size(width = 54.dp, height = 64.dp)
											.clip(RoundedCornerShape(16.dp))
											.background(if (isSelected) MaterialTheme.colorScheme.secondary else Color(0xFFF1F5F9))
											.clickable { viewModel.handleIntent(TutorDetailIntent.SelectDate(dateKey)) },
										contentAlignment = Alignment.Center
									) {
										Column(horizontalAlignment = Alignment.CenterHorizontally) {
											Text(
												text = dayName,
												style = MaterialTheme.typography.labelMedium,
												color = if (isSelected) Color.White else Color.Gray
											)
											Spacer(modifier = Modifier.height(2.dp))
											Text(
												text = dayNum,
												style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
												color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
											)
										}
									}
								}
							}
						}

						Spacer(modifier = Modifier.height(16.dp))

						// Time Slots Row filtered by selectedDay
						val timeFormat = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
						val selectedDaySchedules = state.availableSchedules.filter { schedule ->
							parseScheduleDateTime(schedule.startTime)?.let { dt ->
								val dayFormat = java.time.format.DateTimeFormatter.ofPattern("E", java.util.Locale.forLanguageTag("ru"))
								val dayNumFormat = java.time.format.DateTimeFormatter.ofPattern("d")
								val dayName = dt.format(dayFormat).replaceFirstChar { it.uppercase() }
								val dayNum = dt.format(dayNumFormat)
								"$dayName $dayNum" == state.selectedDay
							} == true
						}

						if (selectedDaySchedules.isEmpty()) {
							Text(
								text = "Нет доступных слотов на выбранную дату",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.Gray
							)
						} else {
							FlowRow(
								horizontalArrangement = Arrangement.spacedBy(10.dp),
								verticalArrangement = Arrangement.spacedBy(10.dp)
							) {
								selectedDaySchedules.forEach { schedule ->
									val dt = parseScheduleDateTime(schedule.startTime)
									val timeText = dt?.format(timeFormat) ?: schedule.startTime
									val isSelected = schedule.idSchedule == state.selectedScheduleId
									Box(
										modifier = Modifier
											.clip(RoundedCornerShape(16.dp))
											.background(if (isSelected) MaterialTheme.colorScheme.secondary else Color(0xFFF1F5F9))
											.clickable {
												viewModel.handleIntent(TutorDetailIntent.SelectScheduleSlot(schedule.idSchedule, timeText))
											}
											.padding(horizontal = 16.dp, vertical = 10.dp)
									) {
										Text(
											text = timeText,
											style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
											color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
										)
									}
								}
							}
						}

						Spacer(modifier = Modifier.height(16.dp))

						// Session Summary Box
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(16.dp))
								.background(MaterialTheme.colorScheme.surfaceVariant)
								.padding(16.dp)
						) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Column {
									Text(
										text = stringResource(R.string.selected_session),
										style = MaterialTheme.typography.labelSmall,
										color = Color.Gray
									)
									Spacer(modifier = Modifier.height(2.dp))
									Text(
										text = "1 Hour (${state.selectedDay} • ${state.selectedTimeSlot})",
										style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
										color = MaterialTheme.colorScheme.secondary
									)
								}
								Text(
									text = "$${service?.priceInDollars ?: "39.30"}",
									style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.secondary
								)
							}
						}
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Recent Review Card
				if (state.reviews.isNotEmpty()) {
					val review = state.reviews.first()
					Card(
						shape = RoundedCornerShape(24.dp),
						colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
						elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
						modifier = Modifier.fillMaxWidth()
					) {
						Column(modifier = Modifier.padding(20.dp)) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween,
								verticalAlignment = Alignment.CenterVertically
							) {
								Text(
									text = stringResource(R.string.recent_review),
									style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.onSurface
								)
								Text(
									text = stringResource(R.string.view_all),
									style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.secondary
								)
							}

							Spacer(modifier = Modifier.height(12.dp))

							Row(verticalAlignment = Alignment.CenterVertically) {
								AsyncImage(
									model = "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
									contentDescription = null,
									modifier = Modifier
										.size(40.dp)
										.clip(CircleShape),
									contentScale = ContentScale.Crop,
									error = painterResource(id = R.drawable.logo)
								)
								Spacer(modifier = Modifier.width(12.dp))
								Column {
									Text(
										text = "${review.student.firstName} ${review.student.lastName}",
										style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
									)
									Text(
										text = "Calculus Prep",
										style = MaterialTheme.typography.labelSmall,
										color = Color.Gray
									)
								}
							}

							Spacer(modifier = Modifier.height(8.dp))

							Text(
								text = "\"${review.comment}\"",
								style = MaterialTheme.typography.bodySmall,
								color = Color.Gray
							)
						}
					}
				}

				Spacer(modifier = Modifier.height(24.dp))

				// Bottom Actions
				val canBook = state.availableSchedules.isNotEmpty() && !state.selectedScheduleId.isNullOrBlank()
				Button(
					onClick = {
						viewModel.handleIntent(TutorDetailIntent.ConfirmBooking)
					},
					enabled = canBook,
					shape = RoundedCornerShape(16.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFF00A86B),
						disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
					),
					modifier = Modifier
						.fillMaxWidth()
						.height(52.dp)
				) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White)
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							text = stringResource(R.string.confirm_booking),
							fontWeight = FontWeight.Bold,
							fontSize = 16.sp,
							color = Color.White
						)
					}
				}

				Spacer(modifier = Modifier.height(12.dp))

				Button(
					onClick = onNavigateBack,
					shape = RoundedCornerShape(16.dp),
					colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
					modifier = Modifier
						.fillMaxWidth()
						.height(52.dp)
				) {
					Text(
						text = stringResource(R.string.cancel_booking),
						fontWeight = FontWeight.Bold,
						fontSize = 16.sp,
						color = Color.White
					)
				}

				Spacer(modifier = Modifier.height(24.dp))
			}
		}
	}
}

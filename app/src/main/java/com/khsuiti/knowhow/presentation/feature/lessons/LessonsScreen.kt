package com.khsuiti.knowhow.presentation.feature.lessons

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.khsuiti.knowhow.R
import com.khsuiti.knowhow.presentation.common.ui.theme.color1
import com.khsuiti.knowhow.responsesData.BookingResponse

@Composable
fun LessonsScreen(
	viewModel: LessonsViewModel,
	modifier: Modifier = Modifier
) {
	val state by viewModel.state.collectAsState()

	val systemDark = isSystemInDarkTheme()
	val grayTextColor = if (systemDark) Color.Gray else color1

	LaunchedEffect(Unit) {
		viewModel.loadBookings()
	}

	Scaffold(
		containerColor = Color.Transparent
	) { innerPadding ->
		Column(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(horizontal = 16.dp)
		) {
			Spacer(modifier = Modifier.height(12.dp))

			Text(
				text = stringResource(R.string.lessons_title),
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(16.dp))

			if (state.isLoading && state.bookings.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
				}
			} else if (state.bookings.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Icon(
							imageVector = Icons.Rounded.CalendarMonth,
							contentDescription = null,
							modifier = Modifier.size(64.dp),
							tint = MaterialTheme.colorScheme.secondary
						)
						Spacer(modifier = Modifier.height(12.dp))
						Text(
							text = stringResource(R.string.no_lessons),
							style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
							color = grayTextColor
						)
					}
				}
			} else {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(16.dp),
					modifier = Modifier.fillMaxSize()
				) {
					items(state.bookings) { booking ->
						BookingCardTile(
							booking = booking,
							grayTextColor = grayTextColor
						)
					}
				}
			}
		}
	}
}

@Composable
fun BookingCardTile(
	booking: BookingResponse,
	grayTextColor: Color
) {
	val service = booking.service
	val tutor = service.tutor
	val schedule = booking.schedule

	Card(
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box {
					AsyncImage(
						model = tutor.photoURL ?: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2",
						contentDescription = null,
						modifier = Modifier
							.size(56.dp)
							.clip(CircleShape),
						contentScale = ContentScale.Crop,
						error = painterResource(id = R.drawable.logo)
					)
				}

				Spacer(modifier = Modifier.width(12.dp))

				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = service.subjectName,
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = "Преподаватель: ${tutor.user.firstName} ${tutor.user.lastName}",
						style = MaterialTheme.typography.bodyMedium,
						color = grayTextColor
					)
				}

				// Status Badge
				Surface(
					shape = RoundedCornerShape(12.dp),
					color = if (booking.isClose) Color.LightGray.copy(alpha = 0.3f) else Color(0xFFE8F5E9)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
					) {
						Icon(
							imageVector = if (booking.isClose) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
							contentDescription = null,
							tint = if (booking.isClose) Color.Gray else Color(0xFF00C853),
							modifier = Modifier.size(14.dp)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = if (booking.isClose) "Завершено" else "Предстоит",
							style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
							color = if (booking.isClose) Color.Gray else Color(0xFF00C853)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			// Time & Details Row
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
					.padding(12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						imageVector = Icons.Rounded.Schedule,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.secondary,
						modifier = Modifier.size(18.dp)
					)
					Spacer(modifier = Modifier.width(6.dp))
					Text(
						text = schedule.startTime.ifBlank { booking.dateBooking },
						style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
						color = MaterialTheme.colorScheme.onSurface
					)
				}

				Text(
					text = "$${service.priceInDollars} • ${service.lessonDurationMinutes} мин",
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.secondary
				)
			}
		}
	}
}

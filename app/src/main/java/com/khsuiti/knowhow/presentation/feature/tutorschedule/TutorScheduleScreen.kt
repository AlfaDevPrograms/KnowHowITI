package com.khsuiti.knowhow.presentation.feature.tutorschedule

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khsuiti.knowhow.data.local.formatScheduleTime
import com.khsuiti.knowhow.presentation.common.ui.theme.color1
import com.khsuiti.knowhow.responsesData.BookingResponse
import com.khsuiti.knowhow.responsesData.ScheduleResponse
import com.khsuiti.knowhow.responsesData.ServiceResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun TutorScheduleScreen(
	viewModel: TutorScheduleViewModel,
	modifier: Modifier = Modifier
) {
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current
	var showAddDialog by remember { mutableStateOf(false) }
	var scheduleToEdit by remember { mutableStateOf<ScheduleResponse?>(null) }

	state.error?.let { err ->
		LaunchedEffect(err) {
			Toast.makeText(context, err, Toast.LENGTH_LONG).show()
		}
	}

	val systemDark = isSystemInDarkTheme()
	val grayTextColor = if (systemDark) Color.Gray else color1

	LaunchedEffect(Unit) {
		viewModel.loadSchedule()
	}

	Scaffold(
		containerColor = Color.Transparent,
		floatingActionButton = {
			FloatingActionButton(
				onClick = { showAddDialog = true },
				containerColor = MaterialTheme.colorScheme.secondary,
				contentColor = Color.White
			) {
				Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Schedule Slot")
			}
		}
	) { innerPadding ->
		Column(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(horizontal = 16.dp)
		) {
			Spacer(modifier = Modifier.height(12.dp))

			Text(
				text = "Занятия и расписание",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(16.dp))

			if (state.isLoading && state.bookings.isEmpty() && state.schedules.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
				}
			} else if (state.bookings.isEmpty() && state.schedules.isEmpty()) {
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
						Spacer(modifier = Modifier.height(8.dp))
						Text(text = "Расписание и бронирования пока пусты", color = grayTextColor)
						Spacer(modifier = Modifier.height(16.dp))
						Button(onClick = { showAddDialog = true }) {
							Text("Добавить слот")
						}
					}
				}
			} else {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.fillMaxSize()
				) {
					// 1. Student Bookings
					if (state.bookings.isNotEmpty()) {
						item {
							Text(
								text = "Забронированные уроки учеников",
								style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
								color = MaterialTheme.colorScheme.secondary,
								modifier = Modifier.padding(vertical = 4.dp)
							)
						}
						items(state.bookings) { booking ->
							TutorBookingTile(
								booking = booking,
								grayTextColor = grayTextColor,
								onCloseBooking = {
									runCatching { UUID.fromString(booking.idBooking) }.getOrNull()?.let {
										viewModel.closeBooking(it)
									}
								},
								onDeleteBooking = {
									runCatching { UUID.fromString(booking.idBooking) }.getOrNull()?.let {
										viewModel.deleteBooking(it)
									}
								}
							)
						}
					}

					// 2. Free / Active Schedule Slots
					if (state.schedules.isNotEmpty()) {
						item {
							Text(
								text = "Слоты времени",
								style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
								color = MaterialTheme.colorScheme.onSurface,
								modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
							)
						}
						items(state.schedules) { schedule ->
							TutorScheduleSlotTile(
								schedule = schedule,
								grayTextColor = grayTextColor,
								onEdit = { scheduleToEdit = schedule },
								onDelete = {
									runCatching { UUID.fromString(schedule.idSchedule) }.getOrNull()?.let {
										viewModel.deleteScheduleSlot(it)
									}
								}
							)
						}
					}
				}
			}
		}

		if (showAddDialog) {
			AddScheduleDialog(
				title = "Добавить слот в расписание",
				services = state.services,
				initialServiceId = state.services.firstOrNull()?.idService ?: "",
				initialTime = "2026-10-10T10:00:00Z",
				onDismiss = { showAddDialog = false },
				onAdd = { serviceId, timeIso ->
					viewModel.addScheduleSlot(serviceId, timeIso)
					showAddDialog = false
				}
			)
		}

		scheduleToEdit?.let { s ->
			AddScheduleDialog(
				title = "Редактировать слот",
				services = state.services,
				initialServiceId = s.idService,
				initialTime = s.startTime,
				onDismiss = { scheduleToEdit = null },
				onAdd = { serviceId, timeIso ->
					runCatching { UUID.fromString(s.idSchedule) }.getOrNull()?.let { scheduleUuid ->
						viewModel.updateScheduleSlot(scheduleUuid, serviceId, timeIso)
					}
					scheduleToEdit = null
				}
			)
		}
	}
}

@Composable
fun TutorBookingTile(
	booking: BookingResponse,
	grayTextColor: Color,
	onCloseBooking: () -> Unit,
	onDeleteBooking: () -> Unit
) {
	val service = booking.service
	val student = booking.student
	val schedule = booking.schedule

	Card(
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = Icons.Rounded.Person,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.secondary,
					modifier = Modifier.size(32.dp)
				)

				Spacer(modifier = Modifier.width(12.dp))

				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = "Ученик: ${student.firstName} ${student.lastName}",
						style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = "Дисциплина: ${service.subjectName}",
						style = MaterialTheme.typography.bodyMedium,
						color = grayTextColor
					)
				}

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
							text = if (booking.isClose) "Завершено" else "Забронировано",
							style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
							color = if (booking.isClose) Color.Gray else Color(0xFF00C853)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
					.padding(12.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = formatScheduleTime(schedule.startTime.ifBlank { booking.dateBooking }),
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
					color = MaterialTheme.colorScheme.onSurface
				)

				Text(
					text = "$${service.priceInDollars} • ${service.lessonDurationMinutes} мин",
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
					color = MaterialTheme.colorScheme.secondary
				)
			}

			Spacer(modifier = Modifier.height(12.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			) {
				if (!booking.isClose) {
					Button(
						onClick = onCloseBooking,
						colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
						shape = RoundedCornerShape(12.dp),
						modifier = Modifier.padding(end = 8.dp)
					) {
						Text("Завершить урок", color = Color.White, fontWeight = FontWeight.Bold)
					}
				}

				TextButton(onClick = onDeleteBooking) {
					Text("Отменить бронь", color = Color(0xFFE53935))
				}
			}
		}
	}
}

@Composable
fun TutorScheduleSlotTile(
	schedule: ScheduleResponse,
	grayTextColor: Color,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = "Слот: ${formatScheduleTime(schedule.startTime)}",
						style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)
					if (schedule.endTime.isNotBlank()) {
						Text(
							text = "Окончание: ${formatScheduleTime(schedule.endTime)}",
							style = MaterialTheme.typography.bodySmall,
							color = grayTextColor
						)
					}
					Text(
						text = if (schedule.isBooked) "Занято" else "Свободно",
						color = if (schedule.isBooked) Color(0xFFE53935) else Color(0xFF00C853),
						fontWeight = FontWeight.Bold,
						style = MaterialTheme.typography.labelSmall
					)
				}

				Row {
					IconButton(onClick = onEdit) {
						Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit Slot", tint = MaterialTheme.colorScheme.secondary)
					}
					IconButton(onClick = onDelete) {
						Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Slot", tint = Color(0xFFE53935))
					}
				}
			}
		}
	}
}

@Composable
fun AddScheduleDialog(
	title: String = "Добавить слот в расписание",
	services: List<ServiceResponse>,
	initialServiceId: String = "",
	initialTime: String = "2026-10-10T10:00:00Z",
	onDismiss: () -> Unit,
	onAdd: (UUID, String) -> Unit
) {
	var selectedServiceId by remember { mutableStateOf(initialServiceId.ifBlank { services.firstOrNull()?.idService ?: "" }) }
	
	val initialLocalDateTime = remember(initialTime) {
		runCatching { LocalDateTime.parse(initialTime.removeSuffix("Z")) }
			.getOrDefault(LocalDateTime.now())
	}
	var selectedDate by remember { mutableStateOf(initialLocalDateTime.toLocalDate()) }
	var selectedTime by remember { mutableStateOf(initialLocalDateTime.toLocalTime()) }
	
	val context = LocalContext.current
	val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
	val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
				Text("Выберите услугу:", fontWeight = FontWeight.Bold)
				LazyColumn(modifier = Modifier.height(110.dp)) {
					items(services) { service ->
						Surface(
							shape = RoundedCornerShape(8.dp),
							color = if (service.idService == selectedServiceId) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else Color.Transparent,
							modifier = Modifier
								.fillMaxWidth()
								.clickable { selectedServiceId = service.idService }
						) {
							Text(
								text = service.subjectName,
								fontWeight = if (service.idService == selectedServiceId) FontWeight.Bold else FontWeight.Normal,
								modifier = Modifier.padding(8.dp)
							)
						}
					}
				}

				Spacer(modifier = Modifier.height(4.dp))
				Text("Дата и время:", fontWeight = FontWeight.Bold)

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					// Date Button
					OutlinedButton(
						onClick = {
							android.app.DatePickerDialog(
								context,
								{ _, year, month, dayOfMonth ->
									selectedDate = java.time.LocalDate.of(year, month + 1, dayOfMonth)
								},
								selectedDate.year,
								selectedDate.monthValue - 1,
								selectedDate.dayOfMonth
							).show()
						},
						modifier = Modifier.weight(1f)
					) {
						Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
						Spacer(modifier = Modifier.width(4.dp))
						Text(text = selectedDate.format(dateFormatter), fontSize = 13.sp)
					}

					// Time Button
					OutlinedButton(
						onClick = {
							android.app.TimePickerDialog(
								context,
								{ _, hourOfDay, minute ->
									selectedTime = java.time.LocalTime.of(hourOfDay, minute)
								},
								selectedTime.hour,
								selectedTime.minute,
								true
							).show()
						},
						modifier = Modifier.weight(1f)
					) {
						Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
						Spacer(modifier = Modifier.width(4.dp))
						Text(text = selectedTime.format(timeFormatter), fontSize = 13.sp)
					}
				}
			}
		},
		confirmButton = {
			Button(
				onClick = {
					val uuid = runCatching { UUID.fromString(selectedServiceId) }.getOrNull() ?: UUID.randomUUID()
					val dateTime = LocalDateTime.of(selectedDate, selectedTime)
					val timeText = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
					onAdd(uuid, timeText)
				},
				colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
			) {
				Text("Сохранить")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Отмена")
			}
		}
	)
}

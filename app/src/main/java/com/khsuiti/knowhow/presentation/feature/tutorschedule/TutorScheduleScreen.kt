package com.khsuiti.knowhow.presentation.feature.tutorschedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun TutorScheduleScreen(
	viewModel: TutorScheduleViewModel,
	modifier: Modifier = Modifier
) {
	val state by viewModel.state.collectAsState()
	var showAddDialog by remember { mutableStateOf(false) }

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
				.padding(16.dp)
		) {
			Text(
				text = "Мое расписание",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(16.dp))

			if (state.schedules.isEmpty()) {
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
						Text(text = "Расписание пока пусто", color = Color.Gray)
						Spacer(modifier = Modifier.height(16.dp))
						Button(onClick = { showAddDialog = true }) {
							Text("Добавить слот")
						}
					}
				}
			} else {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					items(state.schedules) { schedule ->
						Card(
							shape = RoundedCornerShape(16.dp),
							colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
							modifier = Modifier.fillMaxWidth()
						) {
							Column(modifier = Modifier.padding(16.dp)) {
								Text(
									text = "Начало: ${schedule.startTime}",
									style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
									color = MaterialTheme.colorScheme.onSurface
								)
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = "Конец: ${schedule.endTime}",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.Gray
								)
								Spacer(modifier = Modifier.height(8.dp))
								Text(
									text = if (schedule.isBooked) "Забронировано" else "Свободно",
									color = if (schedule.isBooked) Color(0xFFE53935) else Color(0xFF00C853),
									fontWeight = FontWeight.Bold
								)
							}
						}
					}
				}
			}
		}

		if (showAddDialog) {
			AddScheduleDialog(
				services = state.services,
				onDismiss = { showAddDialog = false },
				onAdd = { serviceId, timeIso ->
					viewModel.addScheduleSlot(serviceId, timeIso)
					showAddDialog = false
				}
			)
		}
	}
}

@Composable
fun AddScheduleDialog(
	services: List<com.khsuiti.knowhow.responsesData.ServiceResponse>,
	onDismiss: () -> Unit,
	onAdd: (UUID, String) -> Unit
) {
	var selectedServiceId by remember { mutableStateOf(services.firstOrNull()?.idService ?: "") }
	var timeText by remember { mutableStateOf("2026-10-05T10:00:00Z") }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Добавить слот в расписание") },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text("Услуга: ${services.find { it.idService == selectedServiceId }?.subjectName ?: "Выберите услугу"}")
				LazyColumn(modifier = Modifier.height(100.dp)) {
					items(services) { service ->
						TextButton(onClick = { selectedServiceId = service.idService }) {
							Text(
								text = service.subjectName,
								fontWeight = if (service.idService == selectedServiceId) FontWeight.Bold else FontWeight.Normal
							)
						}
					}
				}

				OutlinedTextField(
					value = timeText,
					onValueChange = { timeText = it },
					label = { Text("Дата и время (ISO 8601)") },
					singleLine = true
				)
			}
		},
		confirmButton = {
			Button(
				onClick = {
					val uuid = runCatching { UUID.fromString(selectedServiceId) }.getOrNull() ?: UUID.randomUUID()
					onAdd(uuid, timeText)
				},
				colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
			) {
				Text("Добавить")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Отмена")
			}
		}
	)
}

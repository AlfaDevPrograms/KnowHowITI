package com.khsuiti.knowhow.presentation.feature.tutorservices

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khsuiti.knowhow.responsesData.ServiceResponse
import com.khsuiti.knowhow.responsesData.SubjectResponse
import java.util.UUID

@Composable
fun TutorServicesScreen(
	viewModel: TutorServicesViewModel,
	modifier: Modifier = Modifier
) {
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current
	var showCreateDialog by remember { mutableStateOf(false) }
	var serviceToEdit by remember { mutableStateOf<ServiceResponse?>(null) }

	state.error?.let { err ->
		LaunchedEffect(err) {
			Toast.makeText(context, err, Toast.LENGTH_LONG).show()
		}
	}

	Scaffold(
		containerColor = Color.Transparent,
		floatingActionButton = {
			FloatingActionButton(
				onClick = { showCreateDialog = true },
				containerColor = MaterialTheme.colorScheme.secondary,
				contentColor = Color.White
			) {
				Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Service")
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
				text = "Мои дисциплины и услуги",
				style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(16.dp))

			if (state.services.isEmpty()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Icon(
							imageVector = Icons.Rounded.School,
							contentDescription = null,
							modifier = Modifier.size(64.dp),
							tint = MaterialTheme.colorScheme.secondary
						)
						Spacer(modifier = Modifier.height(8.dp))
						Text(text = "У вас пока нет созданных услуг", color = Color.Gray)
						Spacer(modifier = Modifier.height(16.dp))
						Button(onClick = { showCreateDialog = true }) {
							Text("Создать услугу")
						}
					}
				}
			} else {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					items(state.services) { service ->
						Card(
							shape = RoundedCornerShape(16.dp),
							colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
							modifier = Modifier.fillMaxWidth()
						) {
							Column(modifier = Modifier.padding(16.dp)) {
								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										text = service.subjectName,
										style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
										color = MaterialTheme.colorScheme.onSurface
									)
									Row {
										IconButton(onClick = { serviceToEdit = service }) {
											Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
										}
									}
								}
								Spacer(modifier = Modifier.height(4.dp))
								Text(
									text = service.description ?: "Индивидуальные занятия",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.Gray
								)
								Spacer(modifier = Modifier.height(8.dp))
								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween,
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(text = "${service.lessonDurationMinutes} мин", fontWeight = FontWeight.SemiBold)
									Text(text = "$${service.priceInDollars}/час", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
									TextButton(onClick = {
										val uuid = runCatching { UUID.fromString(service.idService) }.getOrNull()
										if (uuid != null) {
											viewModel.toggleActive(uuid, service.isActive)
										}
									}) {
										Text(
											text = if (service.isActive) "Активен" else "Выключен",
											color = if (service.isActive) Color(0xFF00C853) else Color(0xFFE53935),
											fontWeight = FontWeight.Bold
										)
									}
								}
							}
						}
					}
				}
			}
		}

		if (showCreateDialog) {
			ServiceDialog(
				title = "Создать новую услугу",
				subjects = state.subjects,
				initialSubjectId = state.subjects.firstOrNull()?.idSubject ?: "",
				initialDuration = "60",
				initialPrice = "40.0",
				initialDesc = "",
				onDismiss = { showCreateDialog = false },
				onSave = { subjectId, duration, price, desc ->
					viewModel.createService(subjectId, duration, price, desc)
					showCreateDialog = false
				}
			)
		}

		serviceToEdit?.let { s ->
			ServiceDialog(
				title = "Редактировать услугу",
				subjects = state.subjects,
				initialSubjectId = s.idSubject,
				initialDuration = s.lessonDurationMinutes.toString(),
				initialPrice = s.priceInDollars.toString(),
				initialDesc = s.description ?: "",
				onDismiss = { serviceToEdit = null },
				onSave = { subjectId, duration, price, desc ->
					val uuid = runCatching { UUID.fromString(s.idService) }.getOrNull()
					if (uuid != null) {
						viewModel.updateService(uuid, subjectId, duration, price, desc)
					}
					serviceToEdit = null
				}
			)
		}
	}
}

@Composable
fun ServiceDialog(
	title: String,
	subjects: List<SubjectResponse>,
	initialSubjectId: String,
	initialDuration: String,
	initialPrice: String,
	initialDesc: String,
	onDismiss: () -> Unit,
	onSave: (UUID, Int, Double, String) -> Unit
) {
	var selectedSubjectId by remember { mutableStateOf(initialSubjectId.ifBlank { subjects.firstOrNull()?.idSubject ?: "" }) }
	var durationText by remember { mutableStateOf(initialDuration) }
	var priceText by remember { mutableStateOf(initialPrice) }
	var descText by remember { mutableStateOf(initialDesc) }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(title) },
		text = {
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text("Предмет: ${subjects.find { it.idSubject == selectedSubjectId }?.name ?: "Выберите предмет"}")
				LazyColumn(modifier = Modifier.height(100.dp)) {
					items(subjects) { subject ->
						TextButton(onClick = { selectedSubjectId = subject.idSubject }) {
							Text(
								text = subject.name,
								fontWeight = if (subject.idSubject == selectedSubjectId) FontWeight.Bold else FontWeight.Normal
							)
						}
					}
				}

				OutlinedTextField(
					value = durationText,
					onValueChange = { durationText = it },
					label = { Text("Длительность (мин)") },
					singleLine = true
				)

				OutlinedTextField(
					value = priceText,
					onValueChange = { priceText = it },
					label = { Text("Цена ($)") },
					singleLine = true
				)

				OutlinedTextField(
					value = descText,
					onValueChange = { descText = it },
					label = { Text("Описание курса") },
					singleLine = true
				)
			}
		},
		confirmButton = {
			Button(
				onClick = {
					val uuid = runCatching { UUID.fromString(selectedSubjectId) }.getOrNull() ?: UUID.randomUUID()
					val dur = durationText.toIntOrNull() ?: 60
					val pr = priceText.toDoubleOrNull() ?: 40.0
					onSave(uuid, dur, pr, descText)
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

package com.khsuiti.knowhow.presentation.feature.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khsuiti.knowhow.R

@Composable
fun AuthScreen(
	viewModel: AuthViewModel,
	onAuthSuccess: () -> Unit,
	modifier: Modifier = Modifier
) {
	val state by viewModel.state.collectAsState()
	val context = LocalContext.current
	val focusManager = LocalFocusManager.current

	LaunchedEffect(state.isSuccess) {
		if (state.isSuccess) {
			onAuthSuccess()
			viewModel.handleIntent(AuthIntent.ResetAuth)
		}
	}

	state.error?.let { err ->
		LaunchedEffect(err) {
			Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
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
				.padding(horizontal = 24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Spacer(modifier = Modifier.height(24.dp))

			// App Logo
			Image(
				painter = painterResource(id = R.drawable.logo),
				contentDescription = null,
				modifier = Modifier
					.size(96.dp)
					.clip(RoundedCornerShape(20.dp)),
				contentScale = ContentScale.Crop
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.height(24.dp))

			// Auth Mode Segmented Selector (Войти / Зарегистрироваться)
			Card(
				shape = RoundedCornerShape(20.dp),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
				modifier = Modifier.fillMaxWidth()
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(4.dp)
				) {
					// Button 1: Войти
					Box(
						modifier = Modifier
							.weight(1f)
							.height(44.dp)
							.clip(RoundedCornerShape(16.dp))
							.background(
								if (state.isLoginMode) MaterialTheme.colorScheme.secondary else Color.Transparent
							)
							.clickable { viewModel.handleIntent(AuthIntent.ToggleAuthMode(true)) },
						contentAlignment = Alignment.Center
					) {
						Text(
							text = stringResource(R.string.login_button),
							fontWeight = FontWeight.Bold,
							color = if (state.isLoginMode) Color.White else MaterialTheme.colorScheme.onSurface
						)
					}

					// Button 2: Зарегистрироваться
					Box(
						modifier = Modifier
							.weight(1f)
							.height(44.dp)
							.clip(RoundedCornerShape(16.dp))
							.background(
								if (!state.isLoginMode) MaterialTheme.colorScheme.secondary else Color.Transparent
							)
							.clickable { viewModel.handleIntent(AuthIntent.ToggleAuthMode(false)) },
						contentAlignment = Alignment.Center
					) {
						Text(
							text = stringResource(R.string.register_button),
							fontWeight = FontWeight.Bold,
							fontSize = 13.sp,
							color = if (!state.isLoginMode) Color.White else MaterialTheme.colorScheme.onSurface
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			// Form Card
			Card(
				shape = RoundedCornerShape(24.dp),
				colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
				elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier.padding(20.dp)
				) {
					Text(
						text = if (state.isLoginMode)
							stringResource(R.string.auth_title_login)
						else
							stringResource(R.string.auth_title_register),
						style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
						color = MaterialTheme.colorScheme.onSurface
					)

					Spacer(modifier = Modifier.height(16.dp))

					if (!state.isLoginMode) {
						// Role selection (Student vs Tutor)
						Text(
							text = "Тип аккаунта:",
							style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
							color = MaterialTheme.colorScheme.onSurface
						)
						Spacer(modifier = Modifier.height(8.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.spacedBy(8.dp)
						) {
							// Student button
							Box(
								modifier = Modifier
									.weight(1f)
									.height(40.dp)
									.clip(RoundedCornerShape(12.dp))
									.background(if (!state.isTutor) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
									.clickable { viewModel.handleIntent(AuthIntent.ToggleAccountType(false)) },
								contentAlignment = Alignment.Center
							) {
								Text(
									text = "Ученик",
									fontWeight = FontWeight.Bold,
									color = if (!state.isTutor) Color.White else MaterialTheme.colorScheme.onSurface
								)
							}

							// Tutor button
							Box(
								modifier = Modifier
									.weight(1f)
									.height(40.dp)
									.clip(RoundedCornerShape(12.dp))
									.background(if (state.isTutor) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
									.clickable { viewModel.handleIntent(AuthIntent.ToggleAccountType(true)) },
								contentAlignment = Alignment.Center
							) {
								Text(
									text = "Репетитор",
									fontWeight = FontWeight.Bold,
									color = if (state.isTutor) Color.White else MaterialTheme.colorScheme.onSurface
								)
							}
						}

						Spacer(modifier = Modifier.height(16.dp))
					}

					// Login or Email Field
					OutlinedTextField(
						value = state.loginOrEmail,
						onValueChange = { viewModel.handleIntent(AuthIntent.LoginQueryChanged(it)) },
						label = {
							Text(
								text = if (state.isLoginMode)
									stringResource(R.string.login_or_email_label)
								else
									stringResource(R.string.login_label)
							)
						},
						leadingIcon = {
							Icon(
								imageVector = Icons.Rounded.Person,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.secondary
							)
						},
						singleLine = true,
						shape = RoundedCornerShape(16.dp),
						colors = OutlinedTextFieldDefaults.colors(
							focusedBorderColor = MaterialTheme.colorScheme.secondary,
							unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
						),
						modifier = Modifier.fillMaxWidth()
					)

					if (!state.isLoginMode) {
						Spacer(modifier = Modifier.height(12.dp))

						// Email Field
						OutlinedTextField(
							value = state.email,
							onValueChange = { viewModel.handleIntent(AuthIntent.EmailChanged(it)) },
							label = { Text(text = stringResource(R.string.email_label)) },
							leadingIcon = {
								Icon(
									imageVector = Icons.Rounded.Email,
									contentDescription = null,
									tint = MaterialTheme.colorScheme.secondary
								)
							},
							singleLine = true,
							shape = RoundedCornerShape(16.dp),
							colors = OutlinedTextFieldDefaults.colors(
								focusedBorderColor = MaterialTheme.colorScheme.secondary,
								unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
							),
							modifier = Modifier.fillMaxWidth()
						)

						Spacer(modifier = Modifier.height(12.dp))

						Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
							// First Name
							OutlinedTextField(
								value = state.firstName,
								onValueChange = { viewModel.handleIntent(AuthIntent.FirstNameChanged(it)) },
								label = { Text(text = stringResource(R.string.first_name_label)) },
								singleLine = true,
								shape = RoundedCornerShape(16.dp),
								colors = OutlinedTextFieldDefaults.colors(
									focusedBorderColor = MaterialTheme.colorScheme.secondary,
									unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
								),
								modifier = Modifier.weight(1f)
							)

							// Last Name
							OutlinedTextField(
								value = state.lastName,
								onValueChange = { viewModel.handleIntent(AuthIntent.LastNameChanged(it)) },
								label = { Text(text = stringResource(R.string.last_name_label)) },
								singleLine = true,
								shape = RoundedCornerShape(16.dp),
								colors = OutlinedTextFieldDefaults.colors(
									focusedBorderColor = MaterialTheme.colorScheme.secondary,
									unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
								),
								modifier = Modifier.weight(1f)
							)
						}

						if (state.isTutor) {
							Spacer(modifier = Modifier.height(12.dp))
							OutlinedTextField(
								value = state.experienceYears.toString(),
								onValueChange = {
									val exp = it.toIntOrNull() ?: 0
									viewModel.handleIntent(AuthIntent.ExperienceChanged(exp))
								},
								label = { Text(text = "Опыт (лет)") },
								leadingIcon = {
									Icon(imageVector = Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
								},
								keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
								singleLine = true,
								shape = RoundedCornerShape(16.dp),
								colors = OutlinedTextFieldDefaults.colors(
									focusedBorderColor = MaterialTheme.colorScheme.secondary,
									unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
								),
								modifier = Modifier.fillMaxWidth()
							)

							Spacer(modifier = Modifier.height(12.dp))
							OutlinedTextField(
								value = state.bio,
								onValueChange = { viewModel.handleIntent(AuthIntent.BioChanged(it)) },
								label = { Text(text = "О себе / Биография") },
								leadingIcon = {
									Icon(imageVector = Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
								},
								maxLines = 3,
								shape = RoundedCornerShape(16.dp),
								colors = OutlinedTextFieldDefaults.colors(
									focusedBorderColor = MaterialTheme.colorScheme.secondary,
									unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
								),
								modifier = Modifier.fillMaxWidth()
							)
						}
					}

					Spacer(modifier = Modifier.height(12.dp))

					// Password Field
					OutlinedTextField(
						value = state.password,
						onValueChange = { viewModel.handleIntent(AuthIntent.PasswordChanged(it)) },
						label = { Text(text = stringResource(R.string.password_label)) },
						leadingIcon = {
							Icon(
								imageVector = Icons.Rounded.Lock,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.secondary
							)
						},
						visualTransformation = PasswordVisualTransformation(),
						singleLine = true,
						keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
						keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
						shape = RoundedCornerShape(16.dp),
						colors = OutlinedTextFieldDefaults.colors(
							focusedBorderColor = MaterialTheme.colorScheme.secondary,
							unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
						),
						modifier = Modifier.fillMaxWidth()
					)

					if (state.isLoginMode) {
						Spacer(modifier = Modifier.height(12.dp))
						Row(
							verticalAlignment = Alignment.CenterVertically,
							modifier = Modifier.fillMaxWidth()
						) {
							Text(
								text = stringResource(R.string.remember_me),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface,
								modifier = Modifier.weight(1f)
							)
							Switch(
								checked = state.rememberMe,
								onCheckedChange = { viewModel.handleIntent(AuthIntent.ToggleRememberMe(it)) },
								colors = SwitchDefaults.colors(
									checkedThumbColor = Color.White,
									checkedTrackColor = MaterialTheme.colorScheme.secondary
								)
							)
						}
					}

					Spacer(modifier = Modifier.height(20.dp))

					// Action Submit Button
					Button(
						onClick = {
							focusManager.clearFocus()
							viewModel.handleIntent(AuthIntent.SubmitAuth)
						},
						enabled = !state.isLoading,
						shape = RoundedCornerShape(16.dp),
						colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
						modifier = Modifier
							.fillMaxWidth()
							.height(50.dp)
					) {
						if (state.isLoading) {
							CircularProgressIndicator(
								color = Color.White,
								modifier = Modifier.size(24.dp)
							)
						} else {
							Text(
								text = if (state.isLoginMode)
									stringResource(R.string.login_button)
								else
									stringResource(R.string.register_button),
								fontWeight = FontWeight.Bold,
								fontSize = 16.sp,
								color = Color.White
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(20.dp))

			// Toggle Prompt
			Text(
				text = if (state.isLoginMode)
					stringResource(R.string.no_account_prompt)
				else
					stringResource(R.string.already_have_account),
				style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.secondary,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.clickable {
						viewModel.handleIntent(AuthIntent.ToggleAuthMode(!state.isLoginMode))
					}
					.padding(8.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

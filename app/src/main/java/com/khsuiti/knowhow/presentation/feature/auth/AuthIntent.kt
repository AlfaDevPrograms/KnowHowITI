package com.khsuiti.knowhow.presentation.feature.auth

sealed interface AuthIntent {
	data class LoginQueryChanged(val login: String) : AuthIntent
	data class EmailChanged(val email: String) : AuthIntent
	data class PasswordChanged(val pass: String) : AuthIntent
	data class FirstNameChanged(val name: String) : AuthIntent
	data class LastNameChanged(val name: String) : AuthIntent
	data class ToggleAuthMode(val isLoginMode: Boolean) : AuthIntent
	data class ToggleAccountType(val isTutor: Boolean) : AuthIntent
	data class BioChanged(val bio: String) : AuthIntent
	data class ExperienceChanged(val exp: Int) : AuthIntent
	data class ToggleRememberMe(val remember: Boolean) : AuthIntent
	data object SubmitAuth : AuthIntent
	data object ResetAuth : AuthIntent
}

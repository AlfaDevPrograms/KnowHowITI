package com.khsuiti.knowhow.presentation.feature.auth

data class AuthState(
	val isLoginMode: Boolean = true,
	val loginOrEmail: String = "",
	val email: String = "",
	val password: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val isTutor: Boolean = false,
	val bio: String = "",
	val experienceYears: Int = 2,
	val rememberMe: Boolean = true,
	val isLoading: Boolean = false,
	val error: String? = null,
	val isSuccess: Boolean = false
)

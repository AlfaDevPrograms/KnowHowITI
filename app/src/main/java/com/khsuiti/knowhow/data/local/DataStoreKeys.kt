package com.khsuiti.knowhow.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreKeys {
	val BACKGROUND_IMAGE_URI = stringPreferencesKey("background_image_uri")
	val DIMMING_LEVEL = stringPreferencesKey("dimming_level")
	val BACKGROUND_ANIMATION_ENABLED = booleanPreferencesKey("background_animation_enabled")
	val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
	val USER_CONSENT = booleanPreferencesKey("user_consent")
	val VIEW = stringPreferencesKey("view")
	val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
	val ACCESS_TOKEN = stringPreferencesKey("access_token")
	val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
	val IS_TUTOR = booleanPreferencesKey("is_tutor")
	val ACTIVE_SERVICE_IDS = stringSetPreferencesKey("active_service_ids")
}

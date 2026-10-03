package com.khsuiti.knowhow.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.khsuiti.knowhow.data.local.DataStoreKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

class PicturesRepository @Inject constructor(
	private val dataStore: DataStore<Preferences>,
	@param:ApplicationContext private val context: Context
) {
	private val backgroungImageFile: File
		get() = File(context.filesDir, "background_image.jpg")
	
	val backgroundImageUri: Flow<Uri?> = dataStore.data
		.map { prefs ->
			prefs[DataStoreKeys.BACKGROUND_IMAGE_URI]?.toUri()
		}
		.distinctUntilChanged()
	
	suspend fun saveBackgroungImageUri(pickedUri: Uri) {
		try {
			
			val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				val source = ImageDecoder.createSource(context.contentResolver, pickedUri)
				ImageDecoder.decodeBitmap(source)
			} else {
				@Suppress("DEPRECATION")
				MediaStore.Images.Media.getBitmap(context.contentResolver, pickedUri)
			}
			
			
			context.openFileOutput("background_image.jpg", Context.MODE_PRIVATE).use { stream ->
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
			}
			
			
			val localUri = backgroungImageFile.toUri().toString()
			dataStore.edit { prefs ->
				prefs[DataStoreKeys.BACKGROUND_IMAGE_URI] = localUri
			}
		} catch (e: IOException) {
			
			throw e
		}
	}
	
	suspend fun deleteBackgroundImage() {
		backgroungImageFile.delete()
		dataStore.edit { prefs ->
			prefs.remove(DataStoreKeys.BACKGROUND_IMAGE_URI)
		}
	}
}
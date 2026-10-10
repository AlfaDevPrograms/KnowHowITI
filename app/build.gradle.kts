plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.compose)
}


android {
	namespace = "com.khsuiti.knowhow"
	compileSdk {
		version = release(37) {
			minorApiLevel = 1
		}
	}
	defaultConfig {
		applicationId = "com.khsuiti.knowhow"
		minSdk = 26
		targetSdk = 37
		versionCode = 19
		versionName = "1.0.1-release"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			ndk {
				debugSymbolLevel = "FULL"
			}
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
}

dependencies {
	implementation(libs.accompanist.permissions)
	implementation(libs.androidx.material3)
	implementation(libs.coil.compose)
	implementation(libs.div)
	implementation(libs.compose)
	implementation (libs.androidx.compose.runtime)
	implementation (libs.jetbrains.kotlinx.coroutines.core)
	implementation (libs.androidx.room.runtime)
	ksp (libs.androidx.room.compiler)
	implementation (libs.androidx.room.ktx)
	implementation(libs.datetime.wheel.picker)
	implementation(libs.kotlinx.datetime)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.core.splashscreen)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.material.icons.extended)
	implementation(libs.androidx.hilt.navigation.compose)
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.material3)
}
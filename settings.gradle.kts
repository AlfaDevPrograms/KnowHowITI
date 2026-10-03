pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		google()
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
		maven {  url = uri("https://developer.huawei.com/repo/")}
		
		// IronSource
		maven { url = uri("https://android-sdk.is.com/") }
		
		// Pangle
		maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
		
		// Tapjoy
		maven { url = uri("https://sdk.tapjoy.com/") }
		
		// Mintegral
		maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
		
		// Chartboost
		maven { url = uri("https://cboost.jfrog.io/artifactory/chartboost-ads/") }
		
		// AppNext
		maven { url = uri("https://dl.appnext.com/") }
	}
}
rootProject.name = "KnowHow"
include(":app")
 
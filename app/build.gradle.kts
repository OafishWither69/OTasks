import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.baseline)
}

fun encodeVersion(version: String): Int {
    val match = Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-alpha\\.([1-9][0-9]*))?").matchEntire(version)
    requireNotNull(match) { "Invalid otasks.versionName" }
    val (major, minor, patch) =
        match.groupValues
            .drop(1)
            .take(3)
            .map(String::toInt)
    val alpha = match.groupValues[4].takeIf(String::isNotEmpty)?.toInt()
    require(major in 0..199 && minor in 0..99 && patch in 0..999 && (alpha == null || alpha in 1..98))
    return major * 10000000 + minor * 100000 + patch * 100 + (alpha ?: 99)
}
val appVersion = providers.gradleProperty("otasks.versionName").get()
val signingPath = providers.environmentVariable("RELEASE_KEYSTORE_PATH").orNull
android {
    namespace = "com.otasks.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.otasks.app"
        minSdk = 31
        targetSdk = 37
        versionCode = encodeVersion(appVersion)
        versionName = appVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        if (signingPath != null) {
            create("release") {
                storeFile = file(signingPath)
                storePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
            }
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "OTasks Debug")
            isPseudoLocalesEnabled = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (signingPath != null) signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
    lint {
        abortOnError = true
        error += setOf("HardcodedText", "NewApi", "InlinedApi", "MissingPermission", "UnspecifiedImmutableFlag")
    }
}
kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }
room { schemaDirectory("$projectDir/schemas") }
dependencies {
    implementation(project(":domain"))
    implementation(libs.core)
    implementation(libs.activity)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.icons)
    implementation(libs.adaptive)
    implementation(libs.navigation)
    implementation(libs.serialization)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.profileinstaller)
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
    testImplementation(libs.room.testing)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.compose.test)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    androidTestImplementation(libs.test.ext)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.uiautomator)
    baselineProfile(project(":baselineprofile"))
}
tasks.register("checkVersioning") {
    doLast {
        val phases =
            (1..9).flatMap { phase -> listOf("0.$phase.0-alpha.1", "0.$phase.0-alpha.98", "0.$phase.0") } +
                listOf("1.0.0-alpha.1", "1.0.0", "1.1.0-alpha.1", "1.1.0-alpha.2", "1.1.0-alpha.3", "1.1.0", "1.2.0-alpha.1", "1.2.0")
        check(phases.map(::encodeVersion).zipWithNext().all { (before, after) -> before < after })
        check(encodeVersion("1.0.10") < encodeVersion("1.1.0"))
        check(encodeVersion("1.0.999") < encodeVersion("1.1.0-alpha.1"))
        check(encodeVersion("1.0.0-alpha.98") < encodeVersion("1.0.0"))
        check(encodeVersion("0.3.0-alpha.2") == 300002)
        listOf("01.0.0", "1.100.0", "1.0.1000", "1.0.0-alpha.99").forEach {
            check(runCatching { encodeVersion(it) }.isFailure)
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst { check(signingPath != null) { "Release signing is required; configure RELEASE_KEYSTORE_PATH and credentials." } }
}

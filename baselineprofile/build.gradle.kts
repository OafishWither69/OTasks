plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baseline)
}
android {
    namespace = "com.otasks.baselineprofile"
    compileSdk = 37
    targetProjectPath = ":app"
    defaultConfig {
        minSdk = 31
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
baselineProfile { useConnectedDevices = true }
dependencies {
    implementation(libs.benchmark)
    implementation(libs.test.ext)
    implementation(libs.test.runner)
    implementation(libs.uiautomator)
}

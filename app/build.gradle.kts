plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "moye.sine.task"
    compileSdk = 34

    defaultConfig {
        applicationId = "moye.sine.task"
        minSdk = 19
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 26
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
        resConfigs("zh")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.github.JessYanCoding:AndroidAutoSize:v1.2.1")
}
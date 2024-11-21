plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.forkid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.forkid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    //implementation("com.github.Stericson:RootTools:master")

    implementation("androidx.appcompat:appcompat:1.6.1") {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
    implementation("com.google.android.material:material:1.9.0") {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5") {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.jeremstyke.purgecoremobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jeremstyke.purgecoremobile"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.0.9"
    }

    signingConfigs {
        create("release") {
            // No commercial release certificate yet, same situation as the
            // Windows app: this keystore isn't a verified commercial
            // identity, it exists so every release is signed with the same
            // key, which Android requires for a new version to install as
            // an update over the previous one rather than needing an
            // uninstall first. Committed to the repo deliberately, it's not
            // standing in for a real production certificate.
            storeFile = file("../keystore/purgecore-mobile.keystore")
            storePassword = "PurgeCoreMobile2026"
            keyAlias = "purgecoremobile"
            keyPassword = "PurgeCoreMobile2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // AdMob, per the roadmap decision to monetize this app with rewarded/
    // banner ads rather than any paywall.
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // Coil, for loading actual photo thumbnails in the duplicates list
    // rather than a plain text summary.
    implementation("io.coil-kt:coil-compose:2.6.0")
}

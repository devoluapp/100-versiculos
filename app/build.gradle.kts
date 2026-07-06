import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// IDs de teste oficiais do Google (https://developers.google.com/admob/android/test-ads)
val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestBannerId = "ca-app-pub-3940256099942544/9214589741"
val admobTestInterstitialId = "ca-app-pub-3940256099942544/1033173712"
val admobTestRewardedId = "ca-app-pub-3940256099942544/5224354917"

// IDs reais de produção: definidos em local.properties (não versionado), fora do repositório.
// Ex.: admob.appId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}
val releaseAdmobAppId = localProperties.getProperty("admob.appId", admobTestAppId)
val releaseAdmobBannerId = localProperties.getProperty("admob.bannerId", admobTestBannerId)
val releaseAdmobInterstitialId = localProperties.getProperty("admob.interstitialId", admobTestInterstitialId)
val releaseAdmobRewardedId = localProperties.getProperty("admob.rewardedId", admobTestRewardedId)

android {
    namespace = "blog.robertotavares.cemversiculos"
    compileSdk = 36

    defaultConfig {
        applicationId = "blog.robertotavares.cemversiculos"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "ADMOB_APP_ID", "\"$admobTestAppId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$admobTestBannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$admobTestInterstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", "\"$admobTestRewardedId\"")
            manifestPlaceholders["admobAppId"] = admobTestAppId
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            buildConfigField("String", "ADMOB_APP_ID", "\"$releaseAdmobAppId\"")
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$releaseAdmobBannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"$releaseAdmobInterstitialId\"")
            buildConfigField("String", "ADMOB_REWARDED_AD_UNIT_ID", "\"$releaseAdmobRewardedId\"")
            manifestPlaceholders["admobAppId"] = releaseAdmobAppId
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
    // Nota: core-ktx 1.19.0 / lifecycle 2.11.0 / activity-compose 1.13.0+ e hilt-navigation-compose/hilt-work
    // 1.4.0 já compilam contra a próxima compileSdk (37) e exigem AGP 9.1+; ficamos na última
    // versão estável compatível com compileSdk 36 / AGP 8.13.2.
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Precisa ser declarado explicitamente: o BOM atual não traz mais os ícones como
    // dependência transitiva do material3 (Icons.Default.*/Icons.AutoMirrored.Filled.*).
    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))

    // Ads
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Room
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt (pinned em 2.58: ver comentário no build.gradle.kts raiz sobre requisito de AGP 9 a partir do 2.59)
    implementation("com.google.dagger:hilt-android:2.58")
    ksp("com.google.dagger:hilt-android-compiler:2.58")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.8")

    // System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // Home screen widget (Glance) + atualização em background
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.hilt:hilt-work:1.3.0")
    ksp("androidx.hilt:hilt-compiler:1.3.0")

    //Firebase
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")

    // In-App Review
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

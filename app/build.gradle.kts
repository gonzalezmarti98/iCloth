plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //plugin de google
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.martigonzalez.project_icloth"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.martigonzalez.project_icloth"
        minSdk = 23
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
    }

}

dependencies {
    // --- LIBRERÍAS DE FIREBASE ---
    // La BOM (Bill of Materials) gestiona que todas las versiones de Firebase sean compatibles.
    // Se declara UNA SOLA VEZ.
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Librerías específicas de Firebase que necesitas:
    implementation("com.google.firebase:firebase-auth-ktx")      // Para autenticación de usuarios
    implementation("com.google.firebase:firebase-storage-ktx")    // Para guardar archivos (imágenes)
    implementation("com.google.firebase:firebase-firestore-ktx")  // Para la base de datos NoSQL

    // --- LIBRERÍAS DE ANDROIDX ---
    // Extensiones de Kotlin para Activity (necesaria para algunas funcionalidades modernas)
    implementation("androidx.activity:activity-ktx:1.11.0")

    // Dependencias estándar de AndroidX (generadas por el proyecto)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- LIBRERÍAS DE TEST ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

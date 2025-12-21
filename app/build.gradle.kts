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
    //librerias de firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    //añade las extensiones de Kotlin como by viewModels()).
    //Simplifica el código para instancia Activities.
    implementation("androidx.activity:activity-ktx:1.11.0")

    // La BOM (Bill of Materials) de Firebase se declara UNA SOLA VEZ.
    // Gestiona las versiones de todas las librerías de Firebase para que sean compatibles.
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Librerías de Firebase que necesitas
    implementation("com.google.firebase:firebase-auth-ktx")
    // Para subir imágenes
    implementation("com.google.firebase:firebase-storage-ktx")

    // Librería para usar 'by viewModels()' y otras extensiones de Kotlin para Activity
    implementation("androidx.activity:activity-ktx:1.11.0")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
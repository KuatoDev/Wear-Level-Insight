plugins {
    id("com.android.application")
    id("kotlin-android")
}

dependencies {
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.14.0-alpha09")
    implementation("com.github.topjohnwu.libsu:core:6.0.0")
    implementation("com.github.topjohnwu.libsu:service:6.0.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.10.0-alpha02")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("io.coil-kt:coil:2.7.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}

android {
    namespace = "id.vern.wearlevelinsight"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "id.vern.wearlevelinsight"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "4.0.1"
        vectorDrawables.useSupportLibrary = true

        val libs = project.configurations.getByName("implementation").dependencies
            .mapNotNull { dep ->
                if (dep.group != null && dep.name != "unspecified") {
                    "${dep.group}:${dep.name}:${dep.version}"
                } else null
            }
            .sorted()
            .distinct()
            .joinToString(separator = "\\n")
        
        buildConfigField("String", "LIBS_LIST", "\"$libs\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        disable.addAll(listOf("TypographyFractions", "TypographyQuotes"))
        enable.addAll(listOf("RtlHardcoded", "RtlCompat", "RtlEnabled", "UnusedResources"))
        checkOnly.addAll(listOf("NewApi", "InlinedApi"))
        quiet = false
        abortOnError = false
        xmlReport = true
        htmlReport = false
        checkDependencies = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_KEY_STORE_PATH") ?: "KuatoDev.jks")
            storePassword = System.getenv("KEYSTORE") 
                ?: project.findProperty("storePassword") as String?
                ?: "defaultPassword"

            keyAlias = System.getenv("ALIAS") 
                ?: project.findProperty("alias") as String?
                ?: "keyAlias"

            keyPassword = System.getenv("PASSWORD") 
                ?: project.findProperty("keyPassword") as String?
                ?: "defaultPassword"

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

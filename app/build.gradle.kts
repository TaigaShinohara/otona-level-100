import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Persistent signing key in the project's .local/ (never the default debug
// keystore, which is regenerated when the build machine is replaced).
val signingPropsFile = rootProject.file(".local/signing.properties")
val signingProps = Properties().apply {
    if (signingPropsFile.exists()) signingPropsFile.inputStream().use { load(it) }
}
val hasPersistentSigning = signingPropsFile.exists() &&
    rootProject.file(".local/otona.keystore").exists()

android {
    namespace = "com.taigashinohara.otonalevel100"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.taigashinohara.otonalevel100"
        minSdk = 26
        targetSdk = 35
        versionCode = 13
        versionName = "0.9.0"
    }

    signingConfigs {
        if (hasPersistentSigning) {
            create("persistent") {
                storeFile = rootProject.file(".local/otona.keystore")
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            if (hasPersistentSigning) signingConfig = signingConfigs.getByName("persistent")
        }
        release {
            isMinifyEnabled = false
            if (hasPersistentSigning) signingConfig = signingConfigs.getByName("persistent")
        }
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
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

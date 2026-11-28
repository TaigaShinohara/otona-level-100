import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Pre-delivery UI verification standard (AGENTS.md, 2026-11-23):
    // Roborazzi screenshot tests + Robolectric screen tests on the JVM.
    id("io.github.takahirom.roborazzi") version "1.35.0"
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
        versionCode = 20
        versionName = "0.16.0"
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
    testOptions {
        unitTests {
            // Robolectric needs the merged manifest/resources on the test classpath.
            isIncludeAndroidResources = true
            all {
                // Roborazzi: record screenshots instead of verifying against
                // goldens (the PNGs are the pre-delivery eyeball evidence).
                it.systemProperty("roborazzi.test.record", "true")
                // On the build VM, Java cannot download Robolectric's
                // android-all at runtime (TLS is intercepted), so use the
                // pre-fetched copy under .local/ when it exists.
                val roboDeps = rootProject.file(".local/robolectric-deps")
                if (roboDeps.exists()) {
                    it.systemProperty("robolectric.offline", "true")
                    it.systemProperty("robolectric.dependency.dir", roboDeps.absolutePath)
                }
            }
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

// On the build VM the Gradle test executor cannot report results (its
// loopback channel is intercepted), so tests are executed with JUnitCore
// directly — see run_tests.sh. This task dumps the unit-test runtime
// classpath for that runner.
tasks.register("writeTestClasspath") {
    val outFile = layout.buildDirectory.file("test-classpath.txt")
    doLast {
        val cp = configurations.getByName("debugUnitTestRuntimeClasspath").files
        outFile.get().asFile.writeText(cp.joinToString(":") { it.absolutePath })
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.35.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:1.35.0")
}

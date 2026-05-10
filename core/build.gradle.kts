import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.buildConfig)
}

kotlin {
    android {
        namespace = "com.davanok.dvnkdnd.core"
        minSdk = libs.versions.android.minSdk.get().toInt()
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jvm()

    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.bundles.supabase)
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.cio)

            implementation(libs.androidx.paging.common)

            implementation(libs.bundles.settings)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
//            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.app.dirs)
//            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
//            implementation(libs.ktor.client.darwin)
        }
        webMain.dependencies {
//            implementation(libs.ktor.client.cio)
        }
    }

    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "Core"
                    isStatic = true
                }
            }
        }
}

buildConfig {
    packageName = "com.davanok.dvnkquizz.core"

    val properties = Properties()
    rootProject.file("config.properties").inputStream().use {
        properties.load(it)
    }

    listOf(
        "SUPABASE_URL",
        "SUPABASE_KEY"
    ).forEach { key ->
        buildConfigField(key, properties[key]!!.toString())
    }

    buildConfigField("APP_NAME", project.rootProject.name)

    buildConfigField("APP_ID", "com.davanok.dvnkquizz")

    buildConfigField("LOG_LEVEL", "WARN")

    buildConfigField("APP_VERSION", libs.versions.project.version.get())
}

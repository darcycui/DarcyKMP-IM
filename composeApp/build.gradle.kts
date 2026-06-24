import dev.icerock.gradle.MRVisibility
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.moco.resources)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}
room3 {
    schemaDirectory("$projectDir/schemas")
}

// config moko resources plugin
multiplatformResources {
    resourcesPackage.set("org.example.library") // required
    resourcesClassName.set("SharedRes") // optional, default MR
    resourcesVisibility.set(MRVisibility.Internal) // optional, default Public
    iosBaseLocalizationRegion.set("en") // optional, default "en"
    iosMinimalDeploymentTarget.set("11.0") // optional, default "9.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            // 忽略expect-actual class警告
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    jvm("desktop")

    listOf(iosArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DarcyKMP"
            isStatic = true
        }
    }

    js {
        browser()
        binaries.executable()
    }

    // moko resources 0.25.2支持wasm
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            // moko resources
            api(libs.moko.resources)
            // moko resources for compose multiplatform
            api(libs.moko.resources.compose)
            // moko resources test
            // api(libs.moko.resources.test)
            // ktor network
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio) // CIO 引擎
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.encoding)
            // kotlinx serialization
            api(libs.kotlinx.serialization.core)
            api(libs.kotlinx.serialization.json)
            // websocket
            implementation(libs.ktor.client.websockets)
            // kotlin coroutine
            implementation(libs.kotlinx.coroutines.core)
            // navigation3
            implementation(libs.org.androidx.navigation.compose)
            // log napier
            implementation(libs.napier)
            // io/File kotlinx-io-core
            implementation(libs.kotlinx.io.core)
            // 引用本地发布的库
//            implementation(libs.kmp.library.demo)
//            implementation(libs.darcy.kmp.storage)
            // multiplatform-settings key-value storage
            implementation(libs.com.russhwolf.multiplatform.settings)
            // fileKit file picker
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            // coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.coil.network.core)
            // md5/hash in KMP
            // define the BOM and its version
            implementation(project.dependencies.platform(libs.hash.bom))
            // define MD5 artifacts without version
            implementation(libs.hash.md)
            implementation(libs.hash.sha1)
            implementation(libs.hash.sha2)
            // database room
            implementation(libs.androidx.room.runtime)
            // krossbow STOMP client for KMP
            api(libs.krossbow.stomp.core)
//            api(libs.krossbow.websocket.builtin)
            api(libs.krossbow.websocket.ktor)
            // crypto
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)
            // UUID

        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // ktor network
            // kotlin coroutine
            implementation(libs.kotlinx.coroutines.android)
            // Room
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.room.runtime.android)
            // Room SQLite Wrapper (可选)
//            implementation(libs.androidx.room.sqlite.wrapper)
            // SLF4J simple implementation for Napier
            implementation(libs.slf4j.simple)
            // Bouncy Castle for Android
            implementation(libs.cryptography.provider.bouncycastle)
            implementation(libs.bouncycastle.bcprov)
            implementation(libs.bouncycastle.bcpkix)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            // pick file
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            // SLF4J simple implementation for Napier
            implementation(libs.slf4j.simple)
            // Room
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.room.runtime.jvm)
        }
        iosMain.dependencies {
            // Room
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.room.runtime.iosarm64)
        }
        jsMain.dependencies {
            //Room
            implementation(libs.androidx.room.runtime.js)
            // SQLite WASM — WebWorkerSQLiteDriver 的 Worker 脚本依赖 OPFS 持久化
            // Ktor Js 引擎 (浏览器 fetch API)
            implementation(libs.ktor.client.js)
        }
        wasmJsMain.dependencies {
            //Room
            implementation(libs.androidx.room.runtime.wasm.js)
            // SQLite WASM — WebWorkerSQLiteDriver 的 Worker 脚本依赖 OPFS 持久化
            // Ktor Js 引擎 (浏览器 fetch API)
            implementation(libs.ktor.client.js)
        }
        webMain.dependencies {
            // 添加 Web 平台的 SQLite 驱动
            implementation(libs.androidx.sqlite.web)
            implementation(project((":sqlJsWorker")))
        }
        // 添加单元测试
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.darcy.kmpdemo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.darcy.kmpdemo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
//    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
    // Room3 Web端通过OPFS(Original Private File System)实现持久化
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspJs", libs.androidx.room.compiler)
    add("kspWasmJs", libs.androidx.room.compiler)
    // Add any other platform target you use in your project, for example kspDesktop
}

compose.desktop {
    application {
        mainClass = "com.darcy.kmpdemo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "com.darcy.kmpdemo"
            packageVersion = "1.0.0"
            windows {
                // 应用名称
                packageName = "PCUtil"
                // 图标配置
                iconFile.set(project.file("src/desktopMain/resources/distribute/house_128.ico"))  // Windows
                shortcut = true
            }
            buildTypes {
                release {
                    proguard {
                        isEnabled = false
                        obfuscate = false
                        optimize = false
                    }
                }
            }
        }
    }
}

/*
 * Copyright 2019 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    kotlin("android")
}

val versionMajor = 5
val versionMinor = 1
val versionPatch = 0

android {
    compileSdkVersion(Android.compileSdkVersion)

    defaultConfig {
        applicationId = "com.sbgapps.scoreit"
        versionCode = versionMajor * 100 + versionMinor * 10 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        vectorDrawables.useSupportLibrary = true
        minSdkVersion(Android.minSdkVersion)
        targetSdkVersion(Android.targetSdkVersion)
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.properties["scoreItStoreFile"] as String)
            storePassword = project.properties["scoreItStorePassword"] as String
            keyAlias = project.properties["scoreItKeyAlias"] as String
            keyPassword = project.properties["scoreItKeyPassword"] as String
        }
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    viewBinding { isEnabled = true }

    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    packagingOptions {
        exclude("**/*.kotlin_module")
        exclude("**/*.version")
        exclude("**/kotlin/**")
        exclude("**/*.txt")
        exclude("**/*.xml")
        exclude("**/*.properties")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":cache"))

    implementation(kotlin("stdlib", Build.Versions.kotlin))
    implementation(AndroidX.appCompat)
    implementation(AndroidX.coreKtx)
    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.recyclerView)
    implementation(AndroidX.navFragment)
    implementation(AndroidX.navUi)
    implementation(Libs.material)
    implementation(Libs.koinAndroidX)
    implementation(Libs.uniflowAndroidX)
    implementation(Libs.timber)
    implementation(Libs.jsr310)
}

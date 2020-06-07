plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
//    id("com.github.triplet.play") version "2.0.0-rc2"
}

kapt.useBuildCache = true

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")

    lintOptions {
        isAbortOnError = true
        isExplainIssues = true
        isIgnoreWarnings = true
        textReport = true
        textOutput("stdout")
        // Should try to remove last two here
        disable("MissingTranslation", "AppCompatCustomView", "InvalidPackage")
        // I really want some to show as errors
        error("InlinedApi", "StringEscaping")
    }

    defaultConfig {
        applicationId = "com.nononsenseapps.feeder"
        minSdkVersion(18)
        targetSdkVersion(29)
        versionCode = 75
        versionName = "1.9.5"
        multiDexEnabled = true

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Export Room schemas
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mutableMapOf(
                        "room.schemaLocation" to "${projectDir.resolve("schemas")}",
                        "room.incremental" to "true"
                )
            }
        }
    }

    sourceSets {
        // To test Room we need to include the schema dir in resources
        named("androidTest") {
            assets.srcDirs(projectDir.resolve("schemas"))
        }
    }

    if (rootProject.hasProperty("STORE_FILE")) {
        val STORE_FILE: String by rootProject
        val STORE_PASSWORD: String by rootProject
        val KEY_ALIAS: String by rootProject
        val KEY_PASSWORD: String by rootProject
        signingConfigs {
            register("release") {
                storeFile = File(STORE_FILE)
                storePassword = STORE_PASSWORD
                keyAlias = KEY_ALIAS
                keyPassword = KEY_PASSWORD
            }
        }
    }

    buildTypes {
        named("debug").configure {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            isPseudoLocalesEnabled = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        named("release").configure {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            if (project.hasProperty("STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        register("play") {
            applicationIdSuffix = ".play"
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            if (project.hasProperty("STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val room_version: String by rootProject
    val multi_dex_version: String by rootProject
    val workmanager_version: String by rootProject
    val androidx_core_version: String by rootProject
    val constraintlayout_version: String by rootProject
    val recyclerview_version: String by rootProject
    val legacy_support_version: String by rootProject
    val appcompat_version: String by rootProject
    val preference_version: String by rootProject
    val material_version: String by rootProject
    val nav_version: String by rootProject
    val paging_version: String by rootProject
    val threetenabp_version: String by rootProject
    val jsoup_version: String by rootProject
    val rome_version: String by rootProject
    val okhttp_version: String by rootProject
    val coroutines_version: String by rootProject
    val kodein_version: String by rootProject
    val mockito_version: String by rootProject
    val mockk_version: String by rootProject
    val threetentest_version: String by rootProject
    val test_runner_version: String by rootProject
    val test_rules_version: String by rootProject
    val test_ext_junit_version: String by rootProject
    val espresso_version: String by rootProject
    val uiautomator_version: String by rootProject
    val lifecycle_version: String by rootProject

    kapt("androidx.room:room-compiler:$room_version")

    // Needed pre SDK21
    implementation("com.android.support:multidex:$multi_dex_version")

    implementation("androidx.room:room-ktx:$room_version")

    implementation("androidx.work:work-runtime-ktx:$workmanager_version")

    implementation("androidx.core:core-ktx:$androidx_core_version")
    implementation("androidx.constraintlayout:constraintlayout:$constraintlayout_version")
    implementation("androidx.recyclerview:recyclerview:$recyclerview_version")
    implementation("androidx.legacy:legacy-support-v4:$legacy_support_version")
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    implementation("androidx.preference:preference:$preference_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")

    // To support SDK18
    implementation("com.nononsenseapps:filepicker:4.1.0")
    // Better times
    implementation("com.jakewharton.threetenabp:threetenabp:$threetenabp_version")
    // HTML parsing
    implementation("org.jsoup:jsoup:$jsoup_version")
    implementation("org.ccil.cowan.tagsoup:tagsoup:1.2.1")
    // RSS
    implementation("com.rometools:rome:$rome_version")
    implementation("com.rometools:rome-modules:$rome_version")
    // JSONFeed
    implementation(project(":jsonfeed-parser"))
    // For better fetching
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
    // Image loading
    implementation("com.github.bumptech.glide:glide:3.7.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:1.4.0@aar")

    implementation(kotlin("stdlib"))
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    // For doing coroutines on UI thread
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    // Dependency injection
    implementation("org.kodein.di:kodein-di-generic-jvm:$kodein_version")
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodein_version")
    // tests
    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    testImplementation("junit:junit:4.13")
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp_version")

    // Needed for unit testing timezone stuff
    testImplementation("org.threeten:threetenbp:$threetentest_version")

    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
    androidTestImplementation("io.mockk:mockk-android:1.8.10.kotlin13")
    androidTestImplementation("junit:junit:4.13")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:$okhttp_version")

    androidTestImplementation("androidx.test:core:$androidx_core_version")
    androidTestImplementation("androidx.test:runner:$test_runner_version")
    androidTestImplementation("androidx.test:rules:$test_rules_version")
    androidTestImplementation("androidx.test.ext:junit:$test_ext_junit_version")
    androidTestImplementation("androidx.recyclerview:recyclerview:$recyclerview_version")
    androidTestImplementation("androidx.legacy:legacy-support-v4:$legacy_support_version")
    androidTestImplementation("androidx.appcompat:appcompat:$appcompat_version")
    androidTestImplementation("com.google.android.material:material:$material_version")
    androidTestImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:$espresso_version")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:$uiautomator_version")
}


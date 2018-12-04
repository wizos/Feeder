plugins {
    id("kotlin")
    id("java-library")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    api("com.squareup.okhttp3:okhttp:3.10.0")
    api("com.squareup.moshi:moshi:1.5.0")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Deps.kotlin_version}")
    testImplementation("junit:junit:4.12")
}

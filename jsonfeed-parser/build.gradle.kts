plugins {
    id("kotlin")
    id("java-library")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    api("com.squareup.okhttp3:okhttp:${Deps.okhttp_version}")
    api("com.squareup.moshi:moshi:${Deps.moshi_version}")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlin_version}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Deps.kotlin_version}")
    testImplementation("junit:junit:${Deps.junit_version}")
}

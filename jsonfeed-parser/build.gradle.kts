plugins {
    kotlin("jvm")
    `java-library`

}

dependencies {
    val okhttp_version: String by rootProject
    val moshi_version: String by rootProject

    implementation(kotlin("stdlib"))
    api("com.squareup.okhttp3:okhttp:$okhttp_version")
    api("com.squareup.moshi:moshi:$moshi_version")

    // tests
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13")
}

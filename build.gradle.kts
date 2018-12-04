// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    jcenter()
    mavenCentral()
    google()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:${Deps.gradle_build_version}")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlin_version}")

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    jcenter()
    mavenCentral()
  }

  tasks.withType(JavaCompile::class.java) {
    options.isIncremental = true
    options.encoding = "UTF-8"
  }
}

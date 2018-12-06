buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Deps.gradle_build_version}")
        classpath(kotlin("gradle-plugin", version = Deps.kotlin_version))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.isIncremental = true
        options.encoding = "UTF-8"
    }
}

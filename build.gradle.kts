buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        val props =
                File("gradle.properties").inputStream().use { file ->
                    java.util.Properties().apply {
                        load(file)
                    }
                }

        val gradle_build_version: String by props
        val kotlin_version: String by props
        classpath("com.android.tools.build:gradle:$gradle_build_version")
        classpath(kotlin("gradle-plugin", version = kotlin_version))
        //classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

//    tasks.withType(JavaCompile) {
//        options.incremental = true
//        options.encoding = 'UTF-8'
//    }
}

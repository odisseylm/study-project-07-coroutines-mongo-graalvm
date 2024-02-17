
plugins {
    `kotlin-dsl` // kotlin("jvm") is not needed there
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

// plugins.forEach { println("plugin: $it") }

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-exec:1.3")

    implementation("org.graalvm.buildtools:native-gradle-plugin:0.10.0") // TODO: use compileOnly
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api")
}


gradlePlugin {
    plugins {
        create("graalvm.native-image-plugin-fix") {
            id = "com.mvv.gradle.graalvm.native-image-plugin-fix"
            implementationClass = "com.mvv.gradle.graalvm.FixOfNativeImagePlugin"
        }
    }
}
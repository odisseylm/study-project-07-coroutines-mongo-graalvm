
plugins {
    //`kotlin-dsl`
    //idea
    //kotlin("jvm") version "1.9.22"
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

plugins.forEach { println("plugin: $it") }

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-exec:1.3")

    //compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")     //:1.9.22")
    //compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api") //:1.9.22")
    compileOnly("org.graalvm.buildtools:native-gradle-plugin:0.10.0")

    //implementation("org.apache.commons:gradleKotlin:1.3")
    //compileOnly(`kotlin-dsl`)
    //compileOnly("org.gradle:gradle-kotlin-dsl")
    //compileOnly("org.gradle:gradle-kotlin-dsl-extension")
    //compileOnly("org.jetbrains.kotlin:gradle-kotlin-dsl")
    //compileOnly("org.jetbrains.kotlin:gradle-kotlin-dsl-extension")

    //compileOnly("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.3.0")
    //compileOnly("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.3.0")

    //compileOnly("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.2.1")
    //compileOnly("org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin:4.1.1")


    //classpath  ("org.gradle.kotlin:gradle-kotlin-dsl-plugins:4.3.0")
    //compileOnly("org.gradle.kotlin:org.gradle.kotlin.kotlin-dsl:4.2.1")
    //compileOnly("org.gradle.kotlin:org.gradle.kotlin.kotlin-dsl.precompiled-script-plugins:4.2.1")

    //compileOnly("org.jetbrains.kotlin:kotlin-dsl") //:1.9.22")
    //compileOnly("org.gradle.api:gradle-kotlin-dsl") //:1.9.22")
    //compileOnly("org.gradle:gradle-core:*")
}

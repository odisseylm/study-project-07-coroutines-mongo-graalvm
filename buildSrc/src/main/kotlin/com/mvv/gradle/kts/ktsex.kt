package com.mvv.gradle.kts

import org.gradle.api.Action
import org.gradle.jvm.toolchain.JavaToolchainService


// Thank you very much gradle developers, that we need to copy paste this stuff in every project!!!
// God, bless you!!!

val org.gradle.api.Project.`javaToolchains`: org.gradle.jvm.toolchain.JavaToolchainService get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("javaToolchains") as org.gradle.jvm.toolchain.JavaToolchainService
fun org.gradle.api.Project.`javaToolchains`(configure: Action<JavaToolchainService>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("javaToolchains", configure)

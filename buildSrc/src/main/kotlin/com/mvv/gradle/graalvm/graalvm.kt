package com.mvv.gradle.graalvm

import java.io.ByteArrayOutputStream
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean

import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaCompiler

import com.mvv.gradle.kts.javaToolchains


fun Project.getGraalJavaLauncher(javaVersion: Int): Provider<JavaLauncher> {
    val project: Project = this

    val javaLauncherProvider: Provider<JavaLauncher> = project.javaToolchains.launcherFor {
        it.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        //vendor = JvmVendorSpec.GRAAL_VM // Does not work
        JvmVendorSpec.matching("GraalVM") // or "GraalVM Community"
    }

    val executablePath = javaLauncherProvider.get().executablePath
    val isGraalVM = executablePath.toString().contains("graal", ignoreCase = true)

    if (!isGraalVM) throw IllegalStateException("Seems JDK [$executablePath] is not GraalVM.")

    return javaLauncherProvider
}

fun Project.getGraalJavaCompiler(javaVersion: Int): Provider<JavaCompiler> {
    val javaCompiler = project.javaToolchains.compilerFor {
        it.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    return javaCompiler
}

fun Project.fixUninitializedGraalVMNoJavaLaunchers(graalVmJdkVersion: Int) {
    val project: Project = this

    val javaLauncherProvider = project.getGraalJavaLauncher(graalVmJdkVersion)
    val javaLauncher = javaLauncherProvider.get()

    println("## JavaLauncher ${javaLauncher.executablePath}")

    val fixCandidates: List<Pair<String, Action<*>>> = tasks.flatMap { task ->
        task.actions.mapNotNull { taskAction ->
            try {
                val unwrappedAction = taskAction.javaClass.getDeclaredField("action").also { it.trySetAccessible() }.get(taskAction)
                val ao = unwrappedAction as org.graalvm.buildtools.gradle.tasks.actions.MergeAgentFilesAction
                Pair(task.name, ao)
            }
            catch (ignore: Exception) { null }
        }
    }

    // fixing uninitialized MergeAgentFilesAction.noLauncherProperty
    val fixedTaskNames: List<String> = fixCandidates.mapNotNull { taskNameAndAction ->

        // fixing
        val action = taskNameAndAction.second

        @Suppress("UNCHECKED_CAST")
        val launchProp = action.javaClass.getDeclaredField("noLauncherProperty")
            .also { it.trySetAccessible() }
            .get(action) as Property<JavaLauncher>

        val toFix = !launchProp.isPresent
        if (toFix) {
            println("## Setting/fixing noLauncherProperty for ${taskNameAndAction.first}")
            launchProp.set(javaLauncherProvider)
        }

        launchProp.get() // to validate

        if (toFix) taskNameAndAction.first else null
    }

    println("## fixedTasks $fixedTaskNames")
}

package com.mvv.gradle.graalvm

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLauncher


fun Project.fixUninitializedGraalVMNoJavaLaunchers(graalVmJdkVersion: Int) =
    fixUninitializedGraalVMNoJavaLaunchers(getGraalJavaLauncher(graalVmJdkVersion))

fun Project.fixUninitializedGraalVMNoJavaLaunchers(javaLauncherProvider: Provider<JavaLauncher>) {
    val javaLauncher = javaLauncherProvider.get()

    println("## JavaLauncher ${javaLauncher.executablePath}")

    val fixCandidates: List<Pair<String, Action<*>>> = tasks.flatMap { task ->
        task.actions.mapNotNull { taskAction ->
            try {
                val unwrappedAction = taskAction.javaClass.getDeclaredField("action").also { it.trySetAccessible() }.get(taskAction)
                val ao = unwrappedAction as Action<*> // org.graalvm.buildtools.gradle.tasks.actions.MergeAgentFilesAction
                Pair(task.name, ao)
            }
            catch (ignore: Exception) { null }
        }
    }

    // fixing uninitialized MergeAgentFilesAction.noLauncherProperty
    val fixedTaskNames: List<String> = fixCandidates.mapNotNull { taskNameAndAction ->

        // fixing
        val action = taskNameAndAction.second

        try {
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

        } catch (ex: Throwable) {
            //println("Error of fixing noLaunch")
            null
        }
    }

    println("## fixedTasks $fixedTaskNames")
}


package com.mvv.gradle.util

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle


fun Project.isLaunchedByIDE(): Boolean = this.gradle.isLaunchedByIDE()

fun Gradle.isLaunchedByIDE(): Boolean {
    val isLaunchedByIdea = sysProp("idea.active") == "true"
            && isSysPropNotBlank("idea.version")
    return isLaunchedByIdea
}


fun Project.hasTestRequest(): Boolean = this.gradle.hasTestRequest()

fun Gradle.hasTestRequest(): Boolean {
    val gradle: org.gradle.api.invocation.Gradle = this

    val taskNamesOnly = gradle.startParameter.taskRequests.mapNotNull { if (it.args.isEmpty()) null else it.args[0] }
    // Add there your non-trivial integration task names if you need it.

    val hasTestTask = taskNamesOnly.containsOneOf(":test", "test") ||
            taskNamesOnly.containsOneOf(":test", "Test")
    return hasTestTask
}


fun Project.isDebugging(): Boolean = this.gradle.isDebugging()

fun Gradle.isDebugging(): Boolean {
    val gradle: org.gradle.api.invocation.Gradle = this

    // jdk.debug = release
    // idea.debugger.dispatch.addr = 127.0.0.1
    // idea.debugger.dispatch.port = 39533
    //
    val isIdeaDebug = isSysPropNotBlank("idea.debugger.dispatch.addr")
            && isSysPropNotBlank("idea.debugger.dispatch.port")

    val allArgs = gradle.startParameter.taskRequests.flatMap { it.args }
    val cmdLineHasDebugParam1 = allArgs.contains("--debug-jvm")

    val isStartedByGradle = sysProp("org.gradle.appname") in listOf("gradle", "gradlew")
    val cmdLineHasDebugParam2 = isStartedByGradle && ProcessHandle.current()
        .parent()
        .flatMap { it.info().arguments().map { args -> args.contains("--debug-jvm") } }
        .orElse(false)

    return isIdeaDebug || cmdLineHasDebugParam1 || cmdLineHasDebugParam2
}

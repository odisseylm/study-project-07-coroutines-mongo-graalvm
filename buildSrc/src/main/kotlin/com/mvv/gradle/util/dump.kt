package com.mvv.gradle.util

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



fun Project.dumpSystem() = this.gradle.dumpSystem()

fun Gradle.dumpSystem() {
    val gradle: Gradle = this

    println("\n\n--------------------------------------------------------------------------------\n\n")

    println("## gradle.startParameter.taskNames: ${gradle.startParameter.taskNames}")
    println("## gradle.startParameter.taskRequests: ${gradle.startParameter.taskRequests.map { it.args }}\n")

    println("Gradle script params:")
    println(" sys prop java.home: ${sysProp("java.home")}")
    println(" internal java.home: ${org.gradle.internal.jvm.Jvm.current().javaHome}")
    //println(" property java.home: ${project.properties["java.home"]}")
    //println(" org.gradle.java.home: ${project.properties["org.gradle.java.home"]}")
    println(" env JAVA_HOME: ${System.getenv("JAVA_HOME")}")
    println(" env GRAALVM_HOME: ${System.getenv("GRAALVM_HOME")}")
    println("")

    val runtimeBean: RuntimeMXBean = ManagementFactory.getRuntimeMXBean()
    println("Current inputJvmArguments: \n${runtimeBean.inputArguments.joinToString("\n") { "  $it" }}")

    val sysPropsAsText = System.getProperties().entries
        .sortedBy { it.key as String }
        .joinToString("\n") { "  ${it.key} = ${it.value}" }
    println("System props: \n$sysPropsAsText")

    println("\nIs launched by Idea : ${isLaunchedByIdea()}")

    println("\n---------------------------- Current process ----------------------------\n\n")
    val currentProcess = ProcessHandle.current()
    dumpProcess(currentProcess)

    println("\n---------------------------- Parent process ----------------------------\n\n")
    currentProcess.parent().ifPresent { dumpProcess(it) }

    println("\n---------------------------- Parent-parent process ----------------------------\n\n")
    currentProcess.parent().flatMap { it.parent() }.ifPresent { dumpProcess(it) }

    //Thread.sleep(60_000)
    println("\n\n------------------------------------------------------------------------\n\n")
}

fun dumpProcess(process: ProcessHandle) {

    println("Process ID: ${process.pid()}")
    println("Sub-processes count: ${process.children().count()}")

    val processInfo = process.info()
    println("Process command: ${processInfo.command().orElse("")}")

    val cmd: String = processInfo.commandLine().orElse("")
    println("Process command line: $cmd")

    val cmd2: String = getProcessCommandLine(process.pid())
    println("\nProcess command line: $cmd2")

    val cmdArgs: Array<String> = processInfo.arguments().orElse(emptyArray())
    val cmdArgsAsText = cmdArgs.joinToString("\n") { "  $it" }
    println("\nProcess args: $cmdArgsAsText")

    //Thread.sleep(60_000)
    //println("\n\n------------------------------------------------------------------------\n\n")
}

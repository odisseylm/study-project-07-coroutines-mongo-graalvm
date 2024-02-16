package com.mvv.gradle.graalvm

import com.mvv.gradle.kts.javaToolchains
import com.mvv.gradle.util.getField
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry
import org.gradle.jvm.toolchain.*
import org.gradle.jvm.toolchain.internal.*
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentMap



fun Provider<JavaLauncher>.isGraalVM(): Boolean =
    try {
        val executablePath = this.get().getMetadata().installationPath
        executablePath.toString().contains("graal", ignoreCase = true)
    }
    catch (ex: NoToolchainAvailableException) { false }


fun Project.getGraalJavaLauncher(requiredJdkJavaVersion: Int): Provider<JavaLauncher> {
    val project: Project = this

    val javaToolchains = project.javaToolchains
    val javaLangVersion = JavaLanguageVersion.of(requiredJdkJavaVersion)

    var javaLauncherProvider: Provider<JavaLauncher> = javaToolchains.launcherFor {
        languageVersion.set(javaLangVersion)
        vendor.set(JvmVendorSpec.matching("graal"))
    }
    if (javaLauncherProvider.isGraalVM()) return javaLauncherProvider

    javaLauncherProvider = javaToolchains.launcherFor { languageVersion.set(javaLangVersion) }
    if (javaLauncherProvider.isGraalVM()) return javaLauncherProvider


    // remove non-graal jdk temporary
    val removed = javaToolchains.removeToolchainInstallationLocations(javaLauncherProvider)
    javaToolchains.cleanCachedToolchainMatching()

    javaLauncherProvider = javaToolchains.launcherFor { languageVersion.set(javaLangVersion) }

    // We need to cache it, otherwise it will be recalculated again and returns non-graal jvm.
    val javaLauncher = try {
        javaLauncherProvider.get() // it can throw exception
    } finally {
        // restore original behavior
        javaToolchains.restoreToolchain(removed)
        javaToolchains.cleanCachedToolchainMatching()
    }

    javaLauncherProvider = project.providers.provider { javaLauncher }

    //val defaultJavaLauncherProviderSearchResult = javaToolchains.launcherFor { languageVersion.set(javaLangVersion) }
    //val defaultJavaLauncherExecPath = defaultJavaLauncherProviderSearchResult.get().executablePath
    //println("## defaultJavaLauncherExecPath: $defaultJavaLauncherExecPath")
    //javaToolchains.cleanCachedToolchainMatching()

    if (!javaLauncherProvider.isGraalVM())
        throw IllegalStateException("Seems JDK [${javaLauncher.metadata.installationPath}] is not GraalVM.")

    return javaLauncherProvider
}

fun Project.getGraalJavaCompiler(javaVersion: Int): Provider<JavaCompiler> {
    // TODO: impl
    val javaCompiler = project.javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    return javaCompiler
}


private fun JavaToolchainService.getToolchainsInstallationLocations(): MutableCollection<InstallationLocation> {
    val tcImpl = this as DefaultJavaToolchainService
    val queryService = tcImpl.getField<JavaToolchainQueryService>("queryService")
    val instRegistry = queryService.getField<JavaInstallationRegistry>("registry")
    val installations = instRegistry.getField<Any>("installations")
    val locations = installations.getField<MutableCollection<InstallationLocation>>("locations")
    return locations
}

private fun JavaToolchainService.removeToolchainInstallationLocations(javaLauncherProvider: Provider<JavaLauncher>): List<InstallationLocation> {
    val javaToolchains = this

    val locations = javaToolchains.getToolchainsInstallationLocations()
    val toRemoveJavaLauncherHomeFile: File = javaLauncherProvider.get().metadata.installationPath.asFile

    val locationsToRemove = locations.filter { it.location == toRemoveJavaLauncherHomeFile }
    locations.removeAll(locationsToRemove)

    return locationsToRemove
}

private fun JavaToolchainService.cleanCachedToolchainMatching() {
    val javaToolchains = this

    val tcImpl = javaToolchains as DefaultJavaToolchainService
    val queryService = tcImpl.getField<JavaToolchainQueryService>("queryService")

    val matchingToolchains = queryService.getField<ConcurrentMap<JavaToolchainSpecInternal.Key, Any>>("matchingToolchains")
    matchingToolchains.clear()
}

/*
private fun JavaToolchainService.removeCachedToolchainFromMatching_(javaLauncherProvider: Provider<JavaLauncher>) {
    val javaToolchains = this

    val toRemoveJavaLauncherHomePath: Path = javaLauncherProvider.get()
        .metadata.installationPath.asFile.toPath()

    val tcImpl = javaToolchains as DefaultJavaToolchainService
    val queryService = tcImpl.getField<JavaToolchainQueryService>("queryService")

    val matchingToolchains = queryService.getField<ConcurrentMap<JavaToolchainSpecInternal.Key, Any>>("matchingToolchains")
    val keys: List<Map.Entry<JavaToolchainSpecInternal.Key, Any>> = matchingToolchains.entries.toList() // debuggable copy
    val matchingsToRemove = keys
        .filter { (it.value as JavaToolchain).metadata.javaHome == toRemoveJavaLauncherHomePath }
        .map { it.key }
    matchingsToRemove.forEach { matchingToolchains.remove(it) }
}
*/

private fun JavaToolchainService.restoreToolchain(installations: List<InstallationLocation>) {
    val javaToolchains = this
    val locations = javaToolchains.getToolchainsInstallationLocations()
    locations.addAll(installations)
}


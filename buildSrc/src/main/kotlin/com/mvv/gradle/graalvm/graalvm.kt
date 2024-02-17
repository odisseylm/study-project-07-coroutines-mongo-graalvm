package com.mvv.gradle.graalvm

import com.mvv.gradle.kts.javaToolchains
import com.mvv.gradle.util.getField
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry
import org.gradle.jvm.toolchain.*
import org.gradle.jvm.toolchain.internal.*
import java.io.File
import java.util.NoSuchElementException
import java.util.concurrent.ConcurrentMap


fun isGraalVM(javaHomeProvider: ()->File): Boolean =
    try {
        val javaHome: File = javaHomeProvider()
        javaHome.toString().contains("graal", ignoreCase = true)
    }
    catch (ex: NoSuchElementException) { false }
    catch (ex: NoToolchainAvailableException) { false }


private fun isGraalVMm(javaHomeProvider: ()->JavaInstallationMetadata): Boolean =
    isGraalVM { javaHomeProvider().installationPath.asFile }


fun File.isGraalVM(): Boolean  = isGraalVM { this }
fun Provider<JavaLauncher>.isGraalVM():  Boolean = isGraalVMm { this.get().getMetadata() }
fun Provider<JavaCompiler>.isGraalVMC(): Boolean = isGraalVMm { this.get().getMetadata() }


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
    val removed = javaToolchains.removeToolchainInstallationLocations {
        javaLauncherProvider.get().metadata.installationPath.asFile }
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

fun Project.getGraalJavaCompiler(requiredJdkJavaVersion: Int): Provider<JavaCompiler> {
    val project: Project = this

    val javaToolchains = project.javaToolchains
    val javaLangVersion = JavaLanguageVersion.of(requiredJdkJavaVersion)

    var javaCompilerProvider: Provider<JavaCompiler> = javaToolchains.compilerFor {
        languageVersion.set(javaLangVersion)
        vendor.set(JvmVendorSpec.matching("graal"))
    }
    if (javaCompilerProvider.isGraalVMC()) return javaCompilerProvider

    javaCompilerProvider = javaToolchains.compilerFor { languageVersion.set(javaLangVersion) }
    if (javaCompilerProvider.isGraalVMC()) return javaCompilerProvider


    // remove non-graal jdk temporary
    val removed = javaToolchains.removeToolchainInstallationLocations {
        javaCompilerProvider.get().metadata.installationPath.asFile }
    javaToolchains.cleanCachedToolchainMatching()

    javaCompilerProvider = javaToolchains.compilerFor { languageVersion.set(javaLangVersion) }

    // We need to cache it, otherwise it will be recalculated again and returns non-graal jvm.
    val javaLauncher = try {
        javaCompilerProvider.get() // it can throw exception
    } finally {
        // restore original behavior
        javaToolchains.restoreToolchain(removed)
        javaToolchains.cleanCachedToolchainMatching()
    }

    javaCompilerProvider = project.providers.provider { javaLauncher }

    //val defaultJavaLauncherProviderSearchResult = javaToolchains.launcherFor { languageVersion.set(javaLangVersion) }
    //val defaultJavaLauncherExecPath = defaultJavaLauncherProviderSearchResult.get().executablePath
    //println("## defaultJavaLauncherExecPath: $defaultJavaLauncherExecPath")
    //javaToolchains.cleanCachedToolchainMatching()

    if (!javaCompilerProvider.isGraalVMC())
        throw IllegalStateException("Seems JDK [${javaLauncher.metadata.installationPath}] is not GraalVM.")

    return javaCompilerProvider
}


private fun JavaToolchainService.getToolchainsInstallationLocations(): MutableCollection<InstallationLocation> {
    val tcImpl = this as DefaultJavaToolchainService
    val queryService = tcImpl.getField<JavaToolchainQueryService>("queryService")
    val instRegistry = queryService.getField<JavaInstallationRegistry>("registry")
    val installations = instRegistry.getField<Any>("installations")
    val locations = installations.getField<MutableCollection<InstallationLocation>>("locations")
    return locations
}


private fun JavaToolchainService.removeToolchainInstallationLocations(
    toRemoveJavaLauncherHomeFileProvider: ()->File): List<InstallationLocation> {

    val javaToolchains = this

    return try {
        val toRemoveJavaLauncherHomeFile = toRemoveJavaLauncherHomeFileProvider()
        val locations = javaToolchains.getToolchainsInstallationLocations()
        val locationsToRemove = locations.filter { it.location == toRemoveJavaLauncherHomeFile }
        locations.removeAll(locationsToRemove)

        locationsToRemove
    }
    catch (ex: NoSuchElementException) { emptyList() }
    catch (ex: NoToolchainAvailableException) { emptyList() }
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


package com.mvv.gradle.graalvm

import com.mvv.gradle.util.hasTestRequest
import com.mvv.gradle.util.isDebugging
import com.mvv.gradle.util.isLaunchedByIDE
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.kotlin.dsl.*
import javax.inject.Inject


enum class DisableCondition {
    None, // Do not use None if it is put into set.
    UnderIDE,
    // Means direct call of tests (not case of running gradle 'build' task).
    OnTests,
    OnDebug,
}


enum class SetMode {
    None,
    // Sets only if property is not set yet.
    Init,
    // Always set.
    AlwaysSet,
    ;

    val toSet: Boolean get() = this != None
}

interface GraalVMExtensionFix {
    // default 'true'
    val useGraalVMToolchain: Property<SetMode>
    // no default value
    val jdkVersion: Property<JavaLanguageVersion>
    // default value UnderIDE
    val disableProcessAot: SetProperty<DisableCondition>
    // default UnderDebug (to avoid JVMTI call failed with JVMTI_ERROR_NOT_AVAILABLE)
    val disableAgent: SetProperty<DisableCondition>
}

fun org.gradle.api.Project.graalvmNativeFix(configure: Action<GraalVMExtensionFix>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("graalvmNativeFix", configure)


class FixOfNativeImagePlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var ext: GraalVMExtensionFix

    override fun apply(project: Project) {
        this.project = project

        // Seems it does not work.
        // ext = project.extensions.create<GraalVMExtensionFix>("graalvmNativeFix")

        this.ext = project.extensions.create(
            GraalVMExtensionFix::class.java,
            "graalvmNativeFix",
            DefaultGraalVMExtensionFix::class.java,
            project,
        )

        project.afterEvaluate {
            fixGraalVMConfiguration()
        }
    }

    fun fixGraalVMConfiguration() { with(project) {

        val jdkVersion: Provider<JavaLanguageVersion> by lazy { ext.jdkVersion }
        val useGraalVMToolchain: Provider<SetMode> by lazy { ext.useGraalVMToolchain.orElse(SetMode.None) }

        val graalJavaLauncher: Provider<JavaLauncher> by lazy {
            getGraalJavaLauncher(jdkVersion.get().asInt()).also {
                println("## graalJavaLauncher: ${it.get().executablePath}") }
        }

        val graalJavaCompiler: Provider<JavaCompiler> by lazy {
            getGraalJavaCompiler(jdkVersion.get().asInt()).also {
                println("## graalJavaCompiler: ${it.get().executablePath}") }
        }

        // Seems setting graalJavaCompiler/graalJavaLauncher for kotlin/java compiler is optional.
        //
        tasks.withType<JavaCompile>().configureEach {
            javaCompiler.setEx(useGraalVMToolchain, graalJavaCompiler)
        }
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain>().configureEach {
            // I do not know how to distingush there SetMode.Init and SetMode.AlwaysSet.
            if (useGraalVMToolchain.get().toSet) {
                kotlinJavaToolchain.toolchain.use(graalJavaLauncher)
                //kotlinJavaToolchain.jdk.use(
                //    graalJavaLauncher.get().metadata.installationPath.asFile, // Put a path to your JDK
                //    JavaVersion.toVersion(javaJdkVersion), // For example, JavaVersion.17
                //)
            }
        }

        // It includes also "processAot", "processTestAot", "BootRun"
        tasks.withType<JavaExec>().configureEach {
            javaLauncher.setEx(useGraalVMToolchain, graalJavaLauncher)
        }

        val toDisableProcessTestAot: Boolean by lazy {
            toDisable(ext.disableProcessAot.orElse(setOf(DisableCondition.UnderIDE)).get())
        }
        val toDisableAgent: Boolean by lazy {
            // Workaround for testing and/or for test debugging if you have error
            //   JVMTI call failed with JVMTI_ERROR_NOT_AVAILABLE
            toDisable(ext.disableAgent.orElse(setOf(DisableCondition.OnDebug)).get())
        }

        tasks.named<Task>("processTestAot") {
            // I do not know how to distingush there SetMode.Init and SetMode.AlwaysSet.
            if (useGraalVMToolchain.get().toSet) {
                fixUninitializedGraalVMNoJavaLaunchers(graalJavaLauncher)
                // need to do some fixes a bit later due to later registered GraalVM actions
                doLast { fixUninitializedGraalVMNoJavaLaunchers(graalJavaLauncher) }
            }

            onlyIf { !toDisableProcessTestAot }
        }

        tasks.withType<Test> {
            javaLauncher.setEx(useGraalVMToolchain, graalJavaLauncher)
        }

        //extensions.the<GraalVMExtension>()
        //extensions.configure<GraalVMExtension> {
        graalvmNative {
            agent {
                enabled = !toDisableAgent // !isDebugging()
            }
            binaries {
                named("test") {
                    javaLauncher.setEx(useGraalVMToolchain, graalJavaLauncher)
                }
                named("main") {
                    javaLauncher.setEx(useGraalVMToolchain, graalJavaLauncher)
                }
            }
        }
    } }
}


fun Project.toDisable(disableCondition: DisableCondition): Boolean =
    when (disableCondition) {
        DisableCondition.None     -> false
        DisableCondition.UnderIDE -> project.isLaunchedByIDE()
        DisableCondition.OnTests  -> project.hasTestRequest()
        DisableCondition.OnDebug  -> project.isDebugging()
    }

fun Project.toDisable(disableCondition: Iterable<DisableCondition>): Boolean =
    disableCondition.any { toDisable(it) }


public abstract class DefaultGraalVMExtensionFix
    @Inject constructor (private val project: Project) : GraalVMExtensionFix {

    override val useGraalVMToolchain = project.objects.property(SetMode::class.java)
    override val jdkVersion = project.objects.property(JavaLanguageVersion::class.java)
    override val disableProcessAot = project.objects.setProperty(DisableCondition::class.java)
    override val disableAgent = project.objects.setProperty(DisableCondition::class.java)
}

fun org.gradle.api.Project.graalvmNative(configure: Action<GraalVMExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("graalvmNative", configure)


private fun <T> Property<T>.setEx(setMode: SetMode, value: T): Unit {
    when (setMode) {
        SetMode.None -> { }
        SetMode.Init -> if (!this.isPresent) this.set(value)
        SetMode.AlwaysSet -> this.set(value)
    }
}

private fun <T> Property<T>.setEx(setMode: Provider<SetMode>, value: Provider<T>): Unit {
    when (setMode.get()) {
        SetMode.None -> { }
        SetMode.Init -> if (!this.isPresent) this.set(value)
        SetMode.AlwaysSet -> this.set(value)
    }
}

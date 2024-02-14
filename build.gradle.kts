//import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
//import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
//import org.jetbrains.kotlin.gradle.targets.js.npm.includedRange
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.aot.ProcessTestAot

repositories {
	mavenCentral()
	//gradlePluginPortal()
}

println("gradle sys prop java.home: ${System.getProperty("java.home")}")
println("gradle internal java.home: ${org.gradle.internal.jvm.Jvm.current().javaHome}")
println("gradle property java.home: ${project.properties["java.home"]}")
println("gradle org.gradle.java.home: ${project.properties["org.gradle.java.home"]}")
println("gradle enc JAVA_HOME: ${System.getenv("JAVA_HOME")}")
println("gradle enc GRAALVM_HOME: ${System.getenv("GRAALVM_HOME")}")
println("")

val cmd = ProcessHandle.current().info().commandLine().orElse(null)
println("gradle command line: $cmd}")


private val javaHomeInternal = System.getProperty("java.home") ?: ""
/*
if (javaHomeInternal.contains("graal", ignoreCase = true)) {
	//System.getenv()["GRAALVM_HOME"] = javaHomeInternal

	//project.providers.environmentVariable("JAVA_HOME", javaHomeInternal)
	//ProviderFactory.
	//project.gradle.provideDelegate()
	//project.provider()
	//val javaHomeInternalProvider: Provider<String> = project.provider { javaHomeInternal }
	//providers.environmentVariable()

}
*/

/*

// Generates scripts (for *nix and windows) which allow you to build your project with Gradle, without having to install Gradle.
// See https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/wrapper/Wrapper.html
tasks.named<Wrapper>("wrapper") {
}

tasks.named<Test>("test") {
	doFirst {
		// it is used only during 'fork'
		environment("JAVA_HOME", javaHomeInternal)
		environment("GRAALVM_HOME", javaHomeInternal)

		//javaLauncher.set(JavaLauncher2(javaHomeInternal))
		//javaLauncher = JavaLauncher2(javaHomeInternal)

		println("### doFirst")
		println("## 2: " + project.providers.environmentVariable("JAVA_HOME").getOrElse("No Java home"))
	}
}
*/


// Mainly used to avoid warnings in Idea
val springBootVer = "3.2.2"
val kotlinVer = "1.9.22"

plugins {
	// For using gradle.local.properties.
	// See https://github.com/open-jumpco/local-properties-plugin
	// id("io.jumpco.open.gradle.local-properties") version "1.0.1"

	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"

	id("org.graalvm.buildtools.native") version "0.10.0" // "0.9.28"

	idea
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
}

group = "com.mvv"
version = "0.0.1-SNAPSHOT"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVer")

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb:$springBootVer")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive:$springBootVer")

	implementation("org.springframework.boot:spring-boot-starter-security:$springBootVer")
	implementation("org.springframework.boot:spring-boot-starter-web:$springBootVer")
	implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVer")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

	implementation("commons-io:commons-io:2.15.1")

	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVer")
	implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVer")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose:$springBootVer")

	val junitVersion = "5.9.2"
	testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
	testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")

	testImplementation("org.assertj:assertj-core:3.24.2")
	testImplementation("org.testcontainers:junit-jupiter:1.17.6")

	testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVer")
	testImplementation("org.springframework.boot:spring-boot-testcontainers:$springBootVer")
	testImplementation("io.projectreactor:reactor-test:3.5.4")
	testImplementation("org.springframework.security:spring-security-test:6.0.2")
	testImplementation("org.testcontainers:mongodb:1.19.5")
}

dependencyManagement {
	//imports {
	//	mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	//}
	dependencies {
		// because 2.8.0 has vulnerability
		dependency("com.jayway.jsonpath:json-path:[2.9.0,)")
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17

	// toolchain {
	//	languageVersion = JavaLanguageVersion.of(21)
	//	vendor = JvmVendorSpec.GRAAL_VM
	//	implementation = JvmImplementation.VENDOR_SPECIFIC
	// }

}


java {
	//toolchain {
	//	languageVersion = JavaLanguageVersion.of(21)
	//	vendor = JvmVendorSpec.matching("Oracle Corporation")
	//}
}
//tasks.withType<JavaCompile> {
//	options.compilerArgs.addAll(arrayOf("--release", "12"))
//}


idea {
	module {
		isDownloadJavadoc = true
		isDownloadSources = true
	}
}


springBoot {
	mainClass = "com.mvv.demo2.Demo2ApplicationKt"
}


graalvmNative {

	// There are cases where you might want to disable native testing support
	// testSupport = false

	// see https://docs.gradle.org/current/userguide/toolchains.html
	//toolchainDetection = true

	metadataRepository {
		// enabled = true
		// version = "0.1.0"
		// uri(file("metadata-repository"))

		// Exclude this library from automatic metadata
		// repository search
		// excludedModules.add("com.company:some-library")

		// Force the version of the metadata for a particular library
		// moduleToConfigVersion.put("com.company:some-library", "3")

		// To include metadata repository inside your jar you can link to the task using the jar DSL from directive:
		// tasks.named<Jar>("jar") {
		//	from(collectReachabilityMetadata)
		// }
	}

	// See https://graalvm.github.io/native-build-tools/0.9.28/gradle-plugin.html
	// See https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
	// See https://github.com/graalvm/native-build-tools/tree/master/samples
	//
	agent {
		enabled = true // Enables the agent

		defaultMode = "standard" // Default agent mode if one isn't specified using `-Pagent=mode_name`
		// Modes:
		//  * Standard - Collects metadata without conditions. This is recommended if you are building an executable.
		//  * Conditional - Collects metadata with conditions. This is recommended if you are creating conditional metadata for a library intended for further use.
		//  * Direct - For advanced users only. This mode allows directly controlling the command line passed to the agent.


		trackReflectionMetadata = true

		// callerFilterFiles.from("filter.json")
		// accessFilterFiles.from("filter.json")
		// builtinCallerFilter = true
		// builtinHeuristicFilter = true
		// enableExperimentalPredefinedClasses = false
		// enableExperimentalUnsafeAllocationTracing = false

		// Copies metadata collected from tasks into the specified directories.
		metadataCopy {
			inputTaskNames.add("test") // Tasks previously executed with the agent attached.
			//outputDirectories.add("src/main/resources/META-INF/native-image")
			mergeWithExisting = true // Instead of copying, merge with existing metadata in the output directories.
		}

		// By default, if `-Pagent` is specified, all tasks that extend JavaForkOptions are instrumented.
        // This can be limited to only specific tasks that match this predicate.
		// tasksToInstrumentPredicate = { it -> true }
	}

	//useArgFile = true

	binaries {

		named("test") {
			verbose = true
			richOutput = true  // Determines if native-image building should be done with rich output
			quickBuild = true // Determines if image is being built in quick build mode (alternatively use GRAALVM_QUICK_BUILD environment variable, or add --native-quick-build to the CLI)
			debug = true // Determines if debug info should be generated, defaults to false (alternatively add --debug-native to the CLI)

			// Seems now it does not work
			javaLauncher = getGraalJavaLauncher()
		}

		named("main") {

			// will be used by the native-image builder process
			//
			verbose = true
			richOutput = true  // Determines if native-image building should be done with rich output

			quickBuild = false // Determines if image is being built in quick build mode (alternatively use GRAALVM_QUICK_BUILD environment variable, or add --native-quick-build to the CLI)

			debug = true // Determines if debug info should be generated, defaults to false (alternatively add --debug-native to the CLI)

			// mainClass =
			// fallback = false   // Sets the fallback mode of native-image, defaults to false

			// Seems now it does not work
			javaLauncher = getGraalJavaLauncher()

			// toolchainDetection = false
			// javaLauncher = javaToolchains.launcherFor {
			//	languageVersion = JavaLanguageVersion.of(21)
			//	//vendor = JvmVendorSpec.matching("GraalVM Community")
			//	vendor = JvmVendorSpec.matching("Oracle Corporation")
			// }
			//
			// requiredVersion = "22.3" // The minimal GraalVM version, can be `MAJOR`, `MAJOR.MINOR` or `MAJOR.MINOR.PATCH`

			// If set to true, this will build a fat jar of the image classpath
			// instead of passing each jar individually to the classpath.
			// This option can be used in case the classpath is too long and that
			// invoking native image fails, which can happen on Windows.
			// Defaults to true for Windows, and false otherwise.
			//
			// useFatJar = true
			//
			// Or you can use yours alternative fatJar
			// tasks.named<BuildNativeImageTask>("nativeCompile") {
			//	classpathJar = myFatJar.flatMap { it.archiveFile }
			// }


			// buildArgs.add("-H:ReflectionConfigurationFiles=${projectDir}/build/native/agent-output/test/reflect-config.json")

			// Adds a native image configuration file directory, containing files like reflection configuration
			// configurationFileDirectories.from(file("src/my-config"))
			configurationFileDirectories.from(file("${projectDir}/build/native/agent-output/test"))
			// configurationFileDirectories.from(file("/home/vmelnykov/projects/study-project-07-coroutines-mongo-graalvm/build/native/agent-output/processAot/resource-config.json"))

			// Excludes configuration that matches one of given regexes from JAR of dependency with said coordinates.
			// excludeConfig.put("org.example.test:artifact:version", listOf("^/META-INF/native-image/.*", "^/config/.*"))
			// excludeConfig.put(file("path/to/artifact.jar"), listOf("^/META-INF/native-image/.*", "^/config/.*"))

			// sharedLibrary  // Gets the value which determines if shared library is being built.
			// buildArgs
			// systemProperties
			// environmentVariables
			// classpath
			// jvmArgs

			// Advanced options
			// buildArgs.add("--link-at-build-time") // Passes '--link-at-build-time' to the native image builder options. This can be used to pass parameters which are not directly supported by this extension
			// jvmArgs.add("flag") // Passes 'flag' directly to the JVM running the native image builder

			// Runtime options
			// runtimeArgs.add("--help") // Passes '--help' to built image, during "nativeRun" task

			// When set to true, the compiled binaries will be generated with PGO instrumentation support.
			// pgoInstrument = true
			// pgoProfilesDirectory =
		}

		binaries.all {
			buildArgs.add("--verbose")
		}

		// For native tests
		// '-O0' - sets the minimal optimizations (for tests, as I understand)
		named("test") {
			buildArgs.addAll("--verbose", "-O0")
		}
	}

	// Use it ONLY if
	// You do NOT actually want to run your tests in native mode (by some reason).
	// testSupport = false

	// registerTestBinary("integTest") {
	//	usingSourceSet(sourceSets.getByName("integTest"))
	//	forTestTask(tasks.named<Test>("integTest"))
	// }
	//
	// In this case the plugin will then automatically create the following tasks:
	// 'nativeIntegTestCompile', to compile a native image using the integTest source set
	// 'nativeIntegTest', to execute the tests in native mode

}

tasks.named<ProcessTestAot>("processTestAot") {
	fixUninitializedGraalVMNoJavaLaunchers()
	// need to do some fixes a bit later due to later registered GraalVM actions
	doLast { fixUninitializedGraalVMNoJavaLaunchers() }
}

fun getGraalJavaLauncher(): Provider<JavaLauncher> {

	val javaLauncherProvider: Provider<JavaLauncher> = javaToolchains.launcherFor {
		languageVersion = JavaLanguageVersion.of(21)
		//vendor = JvmVendorSpec.GRAAL_VM // Does not work
		JvmVendorSpec.matching("GraalVM") // or "GraalVM Community"
	}

	val executablePath = javaLauncherProvider.get().executablePath
	val isGraalVM = executablePath.toString().contains("graal", ignoreCase = true)

	if (!isGraalVM) throw IllegalStateException("Seems JDK [$executablePath] is not GraalVM.")

	return javaLauncherProvider
}
fun getGraalJavaCompiler(): Provider<JavaCompiler> {
	val javaCompiler = javaToolchains.compilerFor {
		languageVersion = JavaLanguageVersion.of(21)
	}
	return javaCompiler
}

fun fixUninitializedGraalVMNoJavaLaunchers() {

	val javaLauncherProvider = getGraalJavaLauncher()
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

// Tasks:
//   init
// Build tasks:
//   collectReachabilityMetadata, nativeCompile, nativeRun, nativeTestCompile
//   compileAotJava, compileAotKotlin, compileAotTestJava, compileAotTestKotlin
//   nativeCompileClasspathJar, nativeTestCompileClasspathJar, nativeBuild, nativeTestBuild
//   processAot, processAotResources, processAotTestResources, processTestAot,
//
// Test & Run tasks:
//   bootRun, bootTestRun, test, nativeTest


tasks.withType<JavaCompile>().configureEach {
	javaCompiler = getGraalJavaCompiler()
}

/*
kotlinExtension.jvmToolchain {
	// JavaToolchainSpec
	languageVersion = JavaLanguageVersion.of(17)
	// Optional: vendor.set(JvmVendorSpec.[VENDOR])
}
kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	// Or shorter:
	//jvmToolchain(<MAJOR_JDK_VERSION>)
	// For example:
	jvmToolchain(21)
}


// See https://kotlinlang.org/docs/gradle-configure-project.html#associate-compiler-tasks
tasks.withType<UsesKotlinJavaToolchain>().configureEach {
    kotlinJavaToolchain.jdk.use(
        "/path/to/local/jdk", // Put a path to your JDK
        JavaVersion.<LOCAL_JDK_VERSION> // For example, JavaVersion.17
    )
}

or

val service = project.extensions.getByType<JavaToolchainService>()
val customLauncher = service.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(<MAJOR_JDK_VERSION>))
}
project.tasks.withType<UsesKotlinJavaToolchain>().configureEach {
    kotlinJavaToolchain.toolchain.use(customLauncher)
}
*/
tasks.withType<KotlinCompile> {

	//jvmToolchain {
	//	(this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
	//}

	//compilerOptions {
	//	apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
	//	languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
	//	progressiveMode = true
	//}

	kotlinOptions {
		//languageVersion = ""
		//apiVersion = ""

		//options.languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
		//options.apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
		//options.progressiveMode = true

		//verbose = true
		//allWarningsAsErrors = true

		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {

	// To avoid caching (and using --rerun) (one of):
	//  systemProperty "random.testing.seed", new Random().nextInt()
	//  inputs.property "integration.date", LocalDate.now()
	//  test.outputs.upToDateWhen {false}


	//setIncludes(listOf(
	//	//"com.mvv.demo2.GraalVMTest.forGraalVM",
	//	"com.mvv.demo2.GraalVMTest*",
	//	//"com.mvv.demo2.GraalVMTest*",
	//	//"*",
	//))

	filter {
		//includeTestsMatching("*UiCheck")
		//includeTestsMatching("*GraalVMTest*")
		includeTestsMatching("com.mvv.demo2.GraalVMTest.forGraalVM")
	}

	// See https://docs.gradle.org/current/userguide/java_testing.html

	useJUnitPlatform()

	//jvmArgs(patchArgs)
	//println("jvmArgs $jvmArgs")
	//println("allJvmArgs $allJvmArgs")
	//println("classpath $classpath")
	//println("bootstrapClasspath $bootstrapClasspath")
	//println("debugOptions $debugOptions")
	//println("executable $executable")

	// forkEvery = 100

	// Parallel tests:
	//
	// gradle.properties
	//    org.gradle.parallel=true
	//
	// gradle <task> --parallel
	//
	//maxParallelForks = Runtime.getRuntime().availableProcessors().intdiv(2) ?: 1
	//maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
	//maxParallelForks = Runtime.getRuntime().availableProcessors()
	//maxParallelForks = 1

	// gradle <task> --daemon
	//
	// gradle.properties
	//    org.gradle.daemon=true

	// debug = false // Or you can use command line args '--debug-jvm' or '--no-debug-jvm'
	//
	// debugOptions {
	//	suspend = false / true
	//  enabled = true // ??
	//  // host.set("localhost")
	//  port = 4455
	//  server = true
	// }

	// Fail the 'test' task on the first test failure
	// failFast = true

	// Skip an actual test execution
	// dryRun = true

	// Show standard out and standard error of the test JVM(s) on the console
	testLogging.showStandardStreams = true

	// testLogging.showExceptions = true/false
	// testLogging.showCauses = true/false
	// testLogging.showStackTraces = true/false
	// testLogging.exceptionFormat = true/false
	// testLogging.stackTraceFilters = true/false
	// testLogging.showExceptions = true/false
	// testLogging.showExceptions = true/false
	// testLogging.showExceptions = true/false


	// reports.html.required = false
	//
	// testLogging {
	//	debug
	// }

	reports {
		junitXml.apply {
			isOutputPerTestCase = true // defaults to false
			// mergeReruns = true // defaults to false
		}
	}

	// Listen to standard out and standard error of the test JVM(s).
	//
	// addTestOutputListener { testDescriptor, outputEvent ->
	//	  //if (event.destination == TestOutputEvent.Destination.StdErr) {
	//	  //    logger.error("Test: " + descriptor + ", error: " + event.message)
	//	  //}
	//
	//	  println("jvmArgs $jvmArgs")
	//	  println("allJvmArgs $allJvmArgs")
	//	  println("classpath $classpath")
	// }

	// Use addTestListener for listen 'beforeSuite', 'afterSuite', 'beforeTest', 'afterTest'.

	// systemProperties.put("key1", "value1")
	// systemProperty("key1", "value1")

	// Explicitly include or exclude tests
	// include("org/foo/**")
	// exclude("org/boo/**")

	// Set heap size for the test JVM(s)
	// minHeapSize = "128m"
	// maxHeapSize = "512m"

	// Set JVM arguments for the test JVM(s)
	// jvmArgs = listOf("-XX:MaxPermSize=256m")
	// jvmArgs(listOf("-XX:MaxPermSize=256m"))
	// jvmArgs(arrayOf("-XX:MaxPermSize=256m"))
	// jvmArgs("-XX:MaxPermSize=256m")
}


/*

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter("test")

    testLogging {
        events("passed")
    }
}

tasks.check { dependsOn(integrationTest) }

*/



/*
// TODO: remove or polish this.
//
// spring-boot configuration
// https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/

tasks.named<BootBuildImage>("bootBuildImage") {
	builder.set("mine/java-cnb-builder")
	runImage.set("mine/java-cnb-run")


	environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17"))
	environment.set(mapOf("HTTP_PROXY" to "http://proxy.example.com",
		"HTTPS_PROXY" to "https://proxy.example.com"))
	environment.set(mapOf(
		"BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
		"BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:+HeapDumpOnOutOfMemoryError"
	))

	imageName.set("example.com/library/${project.name}")

	buildpacks.set(listOf("file:///path/to/example-buildpack.tgz", "urn:cnb:builder:paketo-buildpacks/java"))
}

tasks.named<BootBuildImage>("bootBuildImage") {
	imageName.set("docker.example.com/library/${project.name}")
	publish.set(true)
	docker {
		publishRegistry {
			username.set("user")
			password.set("secret")
		}
	}
}
tasks.named<BootBuildImage>("bootBuildImage") {
	docker {
		host.set("tcp://192.168.99.100:2376")
		tlsVerify.set(true)
		certPath.set("/home/user/.minikube/certs")
	}
}
tasks.named<BootBuildImage>("bootBuildImage") {
	docker {
		host.set("unix:///run/user/1000/podman/podman.sock")
		bindHostToBuilder.set(true)
	}
}
tasks.named<BootBuildImage>("bootBuildImage") {
	docker {
		builderRegistry {
			username.set("user")
			password.set("secret")
			url.set("https://docker.example.com/v1/")
			email.set("user@example.com")
		}
	}
}
tasks.named<BootBuildImage>("bootBuildImage") {
	docker {
		builderRegistry {
			token.set("9cbaf023786cd7...")
		}
	}
}
publishing {
	publications {
		create<MavenPublication>("bootJava") {
			artifact(tasks.named("bootJar"))
		}
	}
	repositories {
		maven {
			url = uri("https://repo.example.com")
		}
	}
}

tasks.named<BootRun>("bootRun") {
	optimizedLaunch.set(false)
}

tasks.named<BootRun>("bootRun") {
	systemProperty("com.example.property", findProperty("example") ?: "default")
}
*/



/*

// spring-boot logging

java -jar target/spring-boot-logging-0.0.1-SNAPSHOT.jar --trace

-Dlogging.level.org.springframework=TRACE
-Dlogging.level.com.baeldung=TRACE

mvn spring-boot:run
-Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

*/

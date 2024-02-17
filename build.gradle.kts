import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.mvv.gradle.graalvm.fixUninitializedGraalVMNoJavaLaunchers
import com.mvv.gradle.graalvm.getGraalJavaCompiler
import com.mvv.gradle.graalvm.getGraalJavaLauncher

import com.mvv.gradle.util.isDebugging
import com.mvv.gradle.util.isLaunchedByIdea

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain
import org.springframework.boot.gradle.tasks.aot.ProcessAot
import org.springframework.boot.gradle.tasks.aot.ProcessTestAot
import org.springframework.boot.gradle.tasks.run.BootRun
import java.util.EnumSet


repositories {
	mavenCentral()
	//gradlePluginPortal()
}

buildscript {
	dependencies {
	}
}


// Mainly used to avoid warnings in Idea
val springBootVer = "3.2.2"
val kotlinVer = "1.9.22"

plugins {
	//`kotlin-dsl` // ??? It throws strange logger's conflict error.

	// For using gradle.local.properties.
	// See https://github.com/open-jumpco/local-properties-plugin
	// id("io.jumpco.open.gradle.local-properties") version "1.0.1"

	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"

	id("org.graalvm.buildtools.native") version "0.10.0" // "0.9.28"

	id("com.adarshr.test-logger") version "4.0.0" apply false

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

	implementation("org.apache.commons:commons-lang3:3.12.0")
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

	//configurations.runtime.resolvedConfiguration.resolvedArtifacts.each { ResolvedArtifact ra ->
	//	ModuleVersionIdentifier id = ra.moduleVersion.id
	//			runtimeSources "${id.group}:${id.name}:${id.version}:sources"
	//}
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

//dumpSystem()


val javaJdkVersion = 21
val javaTargetVersion = 17
val javaSourceCompatibilityVersion = javaTargetVersion

java {
	//sourceCompatibility = JavaVersion.VERSION_17
	sourceCompatibility = JavaVersion.toVersion(javaSourceCompatibilityVersion)

	// toolchain {
	//	languageVersion = JavaLanguageVersion.of(21)
	//	vendor = JvmVendorSpec.GRAAL_VM
	//	implementation = JvmImplementation.VENDOR_SPECIFIC
	// }

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

val graalJavaCompiler = getGraalJavaCompiler(javaJdkVersion)
println("## graalJavaCompiler: ${graalJavaCompiler.get().executablePath}")

val graalJavaLauncher = getGraalJavaLauncher(javaJdkVersion)
println("## graalJavaLauncher: ${graalJavaLauncher.get().executablePath}")

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
		// Enables the agent
		//enabled = true

		// Workarounds for testing and/or for test debugging if you have error
		//   JVMTI call failed with JVMTI_ERROR_NOT_AVAILABLE
		//
		//enabled = !isLaunchedByIdea()
		//enabled = !hasTestRequest()
		enabled = !isDebugging() // TODO: try to solve JVMTI_ERROR_NOT_AVAILABLE in other way

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

			// Seems now it is not really used...
			javaLauncher = graalJavaLauncher
		}

		named("main") {

			// will be used by the native-image builder process
			//
			verbose = true
			richOutput = true  // Determines if native-image building should be done with rich output

			quickBuild = false // Determines if image is being built in quick build mode (alternatively use GRAALVM_QUICK_BUILD environment variable, or add --native-quick-build to the CLI)

			debug = true // Determines if debug info should be generated, defaults to false (alternatively add --debug-native to the CLI)
			// native-image -g -H:+SourceLevelDebug -H:-DeleteLocalSymbols Hello
			// buildArgs.add("-H:+SourceLevelDebug")
			// buildArgs.add("-H:-DeleteLocalSymbols")

			// mainClass =
			// fallback = false   // Sets the fallback mode of native-image, defaults to false

			// Seems now it is not really used...
			javaLauncher = graalJavaLauncher

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

tasks.named<ProcessAot>("processAot") {
	javaLauncher = graalJavaLauncher
	//fixUninitializedGraalVMNoJavaLaunchers(javaJdkVersion)
}

tasks.named<ProcessTestAot>("processTestAot") {
	javaLauncher = graalJavaLauncher

	fixUninitializedGraalVMNoJavaLaunchers(javaJdkVersion)
	// need to do some fixes a bit later due to later registered GraalVM actions
	doLast { fixUninitializedGraalVMNoJavaLaunchers(javaJdkVersion) }

	onlyIf { !isLaunchedByIdea() }
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

// Seems setting graalJavaCompiler/graalJavaLauncher for kotlin/java compiler is optional.

tasks.withType<JavaCompile>().configureEach {
	javaCompiler = graalJavaCompiler
}

// See https://kotlinlang.org/docs/gradle-configure-project.html#associate-compiler-tasks
tasks.withType<UsesKotlinJavaToolchain>().configureEach {
	kotlinJavaToolchain.toolchain.use(graalJavaLauncher)
	//kotlinJavaToolchain.jdk.use(
	//    graalJavaLauncher.get().metadata.installationPath.asFile, // Put a path to your JDK
	//    JavaVersion.toVersion(javaJdkVersion), // For example, JavaVersion.17
	//)
}

tasks.withType<KotlinCompile> {

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

tasks.withType<BootRun> {
	javaLauncher = graalJavaLauncher
	jvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:32323")
}


// For experiments to test what is better :-)
val useAdarshrTestLoggerPlugin = true

if (useAdarshrTestLoggerPlugin) apply(plugin = "com.adarshr.test-logger")

tasks.withType<Test> {

	javaLauncher = graalJavaLauncher

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
		//includeTestsMatching("com.mvv.demo2.GraalVMTest.forGraalVM")
	}

	// See https://docs.gradle.org/current/userguide/java_testing.html

	useJUnitPlatform()

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

	// reports.html.required = false
	//
	// testLogging {
	//	debug
	// }

	testLogging {

		if (useAdarshrTestLoggerPlugin) {
			showStandardStreams = false
			events = EnumSet.noneOf(TestLogEvent::class.java)
		}
		else {

			//events = TestLogEvent.values().toSet()
			//events = setOf(TestLogEvent.STARTED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
			events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED)

			// There is no standard gradle 'showFailedStandardStreams'
			// showStandardStreams = true
			showStandardStreams = false

			exceptionFormat = TestExceptionFormat.FULL
			showExceptions = true
			showCauses = true
			showStackTraces = true

			// ??? debug, info, warn
			//debug {
			//	events = setOf(TestLogEvent.STARTED, TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT)
			//	exceptionFormat = TestExceptionFormat.FULL
			//}
			//info.events = debug.events
			//info.exceptionFormat = debug.exceptionFormat

			//afterTest(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
			//	val output = "Class name: ${desc.className}, Test name: ${desc.name},  (Test status: ${result.resultType})"
			//	println( '\n' + output)
			//}))

			afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
				if (desc.parent != null) { // will match the outermost suite
					//val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
					val testSrc = if (desc.className == null) " ALL" else " ${desc.displayName}"
					val output = "Results${testSrc}: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
					val startItem = "|  "
					val endItem = "  |"
					val repeatLength = startItem.length + output.length + endItem.length
					println("\n" + ("-".repeat(repeatLength)) + '\n' + startItem + output + endItem + '\n' + ("-".repeat(repeatLength)))
				}
			}))
		}
	}

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


// For "com.adarshr.test-logger"
// See https://github.com/radarsh/gradle-test-logger-plugin
//
if (useAdarshrTestLoggerPlugin)
configure<TestLoggerExtension> { // or use => testlogger { }
	theme = ThemeType.STANDARD  // Passed/Failed is shown as text PASSED/FAILED
	//theme = ThemeType.MOCHA   // Passed/Failed is shown as tick
	//showExceptions = true
	//showStackTraces = true
	//showFullStackTraces = false
	//showCauses = true
	//slowThreshold = 2000
	//showSummary = true
	//showSimpleNames = false
	//showPassed = true
	//showSkipped = true
	//showFailed = true
	//showOnlySlow = false
	//showStandardStreams = false
	val showStream = false
	showStandardStreams        = showStream
	showPassedStandardStreams  = showStream
	showSkippedStandardStreams = showStream
	showFailedStandardStreams  = true
	//logLevel = LogLevel.LIFECYCLE
}

/*
task copyJavadocsAndSources {
	inputs.files configurations.runtime
			outputs.dir "${buildDir}/download"
	doLast {
		def componentIds = configurations.runtime.incoming.resolutionResult.allDependencies.collect { it.selected.id }
		ArtifactResolutionResult result = dependencies.createArtifactResolutionQuery()
			.forComponents(componentIds)
			.withArtifacts(JvmLibrary, SourcesArtifact, JavadocArtifact)
			.execute()
		def sourceArtifacts = []
		result.resolvedComponents.each { ComponentArtifactsResult component ->
			Set<ArtifactResult> sources = component.getArtifacts(SourcesArtifact)
			println "Found ${sources.size()} sources for ${component.id}"
			sources.each { ArtifactResult ar ->
				if (ar instanceof ResolvedArtifactResult) {
					sourceArtifacts << ar.file
				}
			}
		}

		copy {
			from sourceArtifacts
					into "${buildDir}/download"
		}
	}
}
*/


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

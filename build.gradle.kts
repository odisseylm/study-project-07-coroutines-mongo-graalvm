import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	id("org.graalvm.buildtools.native") version "0.9.28"

	idea
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
}

group = "com.mvv"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

idea {
	module {
		setDownloadJavadoc(true)
		setDownloadSources(true)
		//downloadJavadoc = true
		//downloadSources = true
	}
}

springBoot {
	mainClass = "com.mvv.demo2.Demo2ApplicationKt"
}

/*
graalvmNative {
	binaries.all {
		//buildArgs.add("-H:ReflectionConfigurationFiles=/some-path/reflect-configs.json")
		buildArgs.add("-H:ReflectionConfigurationFiles=reflect-configs.json")
		//buildArgs.add("--initialize-at-build-time=org.apache.commons.logging.LogFactory")
		//Read more on https://tech-stack.com/blog/using-graalvm-in-a-real-world-scenario-techstacks-experience/
		resources.autodetect()
	}
}
//Read more on https://tech-stack.com/blog/using-graalvm-in-a-real-world-scenario-techstacks-experience/
*/


repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("commons-io:commons-io:2.15.1")

	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// deprecated
	// implementation("org.jetbrains.kotlinx:spring-kotlin-coroutine")
	//implementation("org.jetbrains.kotlinx:spring-webmvc-kotlin-coroutine")
	//implementation("org.jetbrains.kotlinx:spring-webflux-kotlin-coroutine")
	//implementation("org.jetbrains.kotlinx:spring-data-mongodb-kotlin-coroutine")

	//implementation("spring-boot-autoconfigure-kotlin-coroutine")

	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
	testImplementation("org.junit.jupiter:junit-jupiter-engine")
	testImplementation("org.junit.jupiter:junit-jupiter")

	testImplementation("org.assertj:assertj-core")
	testImplementation("org.testcontainers:junit-jupiter")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:mongodb:1.19.5")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {

	useJUnitPlatform()

	//jvmArgs(patchArgs)
	//println("jvmArgs $jvmArgs")
	//println("allJvmArgs $allJvmArgs")
	//println("classpath $classpath")
	//println("bootstrapClasspath $bootstrapClasspath")
	//println("debugOptions $debugOptions")
	//println("executable $executable")

	debug = true
	testLogging.showStandardStreams = true

	//testLogging.setShowExceptions(boolean var1);
	//testLogging.setShowCauses(boolean var1);
	//testLogging.setShowStackTraces(boolean var1);
	//testLogging.setExceptionFormat(boolean var1);
	//testLogging.setStackTraceFilters(boolean var1);
	//testLogging.setShowExceptions(boolean var1);
	//testLogging.setShowExceptions(boolean var1);
	//testLogging.setShowExceptions(boolean var1);


	//reports.html.required = false
	//
	//testLogging {
	//	debug
	//}

	reports {
		junitXml.apply {
			isOutputPerTestCase = true // defaults to false
			//mergeReruns = true // defaults to false
		}
	}

	// listen to standard out and standard error of the test JVM(s)
	//onOutput {
	//	//logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message )
	//}

	//onOutput { descriptor: TestDescriptor, event:  TestOutputEvent ->
	//onOutput { descriptor, event ->
	//}

	/*
	onOutput { descriptor, event: org.gradle.api.tasks.testing.logging.TestLogEvent ->
	//onOutput { descriptor: Any, event: Any ->
	//onOutput { descriptor ->
		//if (event.destination == TestOutputEvent.Destination.StdErr) {
		//	logger.error("Test: " + descriptor + ", error: " + event.message)
		//}

		println("jvmArgs $jvmArgs")
		println("allJvmArgs $allJvmArgs")
		println("classpath $classpath")
	}
	*/
	//addTestOutputListener { testDescriptor, outputEvent -> }
}


//onOutput { descriptor, event ->
//	logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message )
//}

/*
debugOptions {
    enabled = true
    //host.set("localhost")
    port = 4455
    server = true
    suspend = true
}

// set a system property for the test JVM(s)
systemProperty 'some.prop', 'value'

// explicitly include or exclude tests
include 'org/foo/**'
exclude 'org/boo/**'

// show standard out and standard error of the test JVM(s) on the console
testLogging.showStandardStreams = true

// set heap size for the test JVM(s)
minHeapSize = "128m"
maxHeapSize = "512m"

// set JVM arguments for the test JVM(s)
jvmArgs '-XX:MaxPermSize=256m'

// listen to events in the test execution lifecycle
beforeTest { descriptor ->
 logger.lifecycle("Running test: " + descriptor)
}

// fail the 'test' task on the first test failure
failFast = true

// skip an actual test execution
dryRun = true

// listen to standard out and standard error of the test JVM(s)
onOutput { descriptor, event ->
 logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message )
}
*/
}

/*

gradle <someTestTask> --debug-jvm

val integrationTest = task<Test>("integrationTest") {
description = "Runs integration tests."
group = "verification"

testClassesDirs = sourceSets["intTest"].output.classesDirs
classpath = sourceSets["intTest"].runtimeClasspath
shouldRunAfter("test")

useJUnitPlatform()

testLogging {
    events("passed")
}
}

tasks.check { dependsOn(integrationTest) }

 */
// https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/

tasks.named<BootBuildImage>("bootBuildImage") {
builder.set("mine/java-cnb-builder")
runImage.set("mine/java-cnb-run")
}
tasks.named<BootBuildImage>("bootBuildImage") {
environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17"))
}
tasks.named<BootBuildImage>("bootBuildImage") {
environment.set(mapOf("HTTP_PROXY" to "http://proxy.example.com",
    "HTTPS_PROXY" to "https://proxy.example.com"))
}
tasks.named<BootBuildImage>("bootBuildImage") {
environment.set(mapOf(
    "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
    "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-XX:+HeapDumpOnOutOfMemoryError"
))
}
tasks.named<BootBuildImage>("bootBuildImage") {
imageName.set("example.com/library/${project.name}")
}
tasks.named<BootBuildImage>("bootBuildImage") {
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
mainClass.set("com.example.ExampleApplication")
}
springBoot {
mainClass.set("com.example.ExampleApplication")
}

tasks.named<BootRun>("bootRun") {
optimizedLaunch.set(false)
}
application {
mainClass.set("com.example.ExampleApplication")
}

tasks.named<BootRun>("bootRun") {
systemProperty("com.example.property", findProperty("example") ?: "default")
}


java -jar target/spring-boot-logging-0.0.1-SNAPSHOT.jar --trace

-Dlogging.level.org.springframework=TRACE
-Dlogging.level.com.baeldung=TRACE

mvn spring-boot:run
-Dspring-boot.run.arguments=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE

./gradlew bootRun -Pargs=--logging.level.org.springframework=TRACE,--logging.level.com.baeldung=TRACE



application.properties
 logging.level.root=WARN
 logging.level.com.baeldung=TRACE

*/

package com.mvv.demo2

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory
import kotlin.reflect.KClass


private val log: Logger = LoggerFactory.getLogger(BuildToolHelper::class.java)


enum class BuildToolType {
    MAVEN,
    GRADLE
}

class BuildToolHelper {

    companion object {

        @JvmStatic
        fun determineBuildToolType(projectDir: Path): BuildToolType {
            val isMavenProject = Files.exists(projectDir.resolve("pom.xml"))
            val isGradleProject = Files.exists(projectDir.resolve("build.gradle"))
                    || Files.exists(projectDir.resolve("build.gradle.kts"))

            if (isMavenProject && isGradleProject) {
                throw IllegalStateException("Seems project $projectDir is maven or gradle?")
            }

            return when {
                isMavenProject -> BuildToolType.MAVEN
                isGradleProject -> BuildToolType.GRADLE
                else -> throw IllegalStateException("Cannot grasp project build directory of $projectDir.")
            }
        }

        @JvmStatic
        fun getBuildDirectory(projectDirectory: Path, buildToolType: BuildToolType? = null): Path {
            val realBuildToolType = buildToolType ?: determineBuildToolType(projectDirectory)
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            return when (realBuildToolType) {
                BuildToolType.MAVEN -> projectDirectory.resolve("target")
                BuildToolType.GRADLE -> projectDirectory.resolve("build")
                else -> throw IllegalStateException("Unsupported build tool type $buildToolType.")
            }
        }

        @JvmStatic @JvmOverloads
        fun getProjectDirectory(someProjectClass: Class<*>,
                                @Suppress("UNUSED_PARAMETER")
                                buildToolType: BuildToolType? = null): Path {

            val resourceUrl = someProjectClass.getResource("/" + someProjectClass.name.replace('.', '/') + ".class")!!
            val someProjectClassFile = FileUtils.toFile(resourceUrl)

            val packageCount = someProjectClass.name.split('.').size

            var compileClassesDir = someProjectClassFile
            for (i in 1..packageCount) {
                compileClassesDir = compileClassesDir.parentFile
            }

            val compileClassesDirStr = compileClassesDir.toString().replace('\\', '/')
            val projectDir = when {
                // /home/vmelnykov/projects/study-project-07-coroutines-mongo-graalvm/build/classes/kotlin/test
                compileClassesDirStr.endsWithOneOf(
                    "/build/classes/kotlin/test", "/build/classes/kotlin/main",
                    "/build/classes/java/test",   "/build/classes/java/main")
                    -> compileClassesDir.parentFile.parentFile.parentFile.parentFile
                // for maven
                // /home/vmelnykov/projects/study-project-01/account-soa/target/classes
                // /home/vmelnykov/projects/study-project-01/account-soa/target/test-classes
                else -> compileClassesDir.parentFile.parentFile
            }


            return projectDir.toPath()
        }

        @JvmStatic @JvmOverloads
        fun getProjectDirectory(someProjectClass: KClass<*>, buildToolType: BuildToolType? = null): Path =
            getProjectDirectory(someProjectClass.java, buildToolType)

        @JvmStatic
        fun findProjectArtifactId(projectDir: Path, buildToolType: BuildToolType? = null): String? {
            val realBuildToolType = buildToolType ?: determineBuildToolType(projectDir)

            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            return when (realBuildToolType) {
                BuildToolType.MAVEN -> findMavenProjectArtifactId(projectDir)
                BuildToolType.GRADLE -> findGradleProjectName(projectDir)
                else -> throw IllegalStateException("Build tool type $realBuildToolType.")
            }
        }

        @JvmStatic
        fun getProjectArtifactId(projectDir: Path, buildToolType: BuildToolType? = null): String {
            return findProjectArtifactId(projectDir, buildToolType)
                ?: throw IllegalStateException("Impossible to find project name of project $projectDir.")
        }

        @JvmStatic
        fun isLaunchedByIde(): Boolean {
            return !isLaunchedByBuildTool()
        }

        @JvmStatic
        fun isLaunchedByBuildTool(): Boolean {
            return isLaunchedByMaven() || isLaunchedByGradle()
        }

        private fun isLaunchedByGradle(): Boolean {

            //java.class.path = /home/vmelnykov/.gradle/caches/7.4.2/workerMain/gradle-worker.jar:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/classes/java/test:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/classes/kotlin/test:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/resources/test:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/classes/java/main:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/classes/kotlin/main:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/resources/main:/home/vmelnykov/projects/study-project-02-gradle-grpc/grpc-shared/build/libs/grpc-shared-1.0-SNAPSHOT.jar:/home/vmelnykov/.m2/repository/com/mvv/bank/bank-shared/1.0-SNAPSHOT/bank-shared-1.0-SNAPSHOT.jar:/home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa-api/build/libs/auth-soa-api-1.0-SNAPSHOT.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.grpc/grpc-kotlin-stub/1.2.1/95c107ed783bcc5acfc01afc44fbed764b0d017e/grpc-kotlin-stub-1.2.1.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8/1.6.1/365e34d5f89c5cd4b3288f09f783befa21545358/kotlinx-coroutines-jdk8-1.6.1.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm/1.6.1/97fd74ccf54a863d221956ffcd21835e168e2aaa/kotlinx-coroutines-core-jvm-1.6.1.jar:/home/vmelnykov/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk8/1.6.21/kotlin-stdlib-jdk8-1.6.21.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/module/jackson-module-kotlin/2.13.3/jackson-module-kotlin-2.13.3.jar:/home/vmelnykov/.m2/repository/org/jetbrains/kotlin/kotlin-reflect/1.6.21/kotlin-reflect-1.6.21.jar:/home/vmelnykov/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/jakarta.inject/jakarta.inject-api/2.0.1/4c28afe1991a941d7702fe1362c365f0a8641d1e/jakarta.inject-api-2.0.1.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.7.0/spring-boot-starter-web-2.7.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-webflux/2.7.0/c3c005593c93d9bdbe74e9a2a0fb4896cad19b27/spring-boot-starter-webflux-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.7.0/spring-boot-starter-json-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-actuator/2.7.0/spring-boot-starter-actuator-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-actuator-autoconfigure/2.7.0/spring-boot-actuator-autoconfigure-2.7.0.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-jaxrs2/2.2.0/swagger-jaxrs2-2.2.0.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-integration/2.2.0/swagger-integration-2.2.0.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-core/2.2.0/swagger-core-2.2.0.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.13.3/jackson-datatype-jsr310-2.13.3.jar:/home/vmelnykov/.m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:/home/vmelnykov/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/home/vmelnykov/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-validation/2.7.0/spring-boot-starter-validation-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-security/2.7.0/spring-boot-starter-security-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-thymeleaf/2.7.0/spring-boot-starter-thymeleaf-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-test/2.7.0/spring-boot-starter-test-2.7.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-spring-boot-starter/2.13.1.RELEASE/6bdbf3dd5e4f85e76f3c5b2eae12c7ddde9ca6fa/grpc-spring-boot-starter-2.13.1.RELEASE.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-server-spring-boot-starter/2.13.1.RELEASE/3b36ca84dee1cf7942dcbdeefc9e47eae540968e/grpc-server-spring-boot-starter-2.13.1.RELEASE.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-server-spring-boot-autoconfigure/2.13.1.RELEASE/5509ce14f0b7b8fbd36597c61ae6ee752a020607/grpc-server-spring-boot-autoconfigure-2.13.1.RELEASE.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-client-spring-boot-starter/2.13.1.RELEASE/a8fd99af46c5458bee911311705b74f2d8078c77/grpc-client-spring-boot-starter-2.13.1.RELEASE.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-client-spring-boot-autoconfigure/2.13.1.RELEASE/4f171da6a548977e06dc81c2e440792b2f27e5f7/grpc-client-spring-boot-autoconfigure-2.13.1.RELEASE.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/net.devh/grpc-common-spring-boot/2.13.1.RELEASE/9a7cc7770afdeaf361d82ff03e25a901b7b48e51/grpc-common-spring-boot-2.13.1.RELEASE.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter/2.7.0/spring-boot-starter-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-test-autoconfigure/2.7.0/spring-boot-test-autoconfigure-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.7.0/spring-boot-autoconfigure-2.7.0.jar:/home/vmelnykov/.m2/repository/org/mapstruct/mapstruct/1.4.2.Final/mapstruct-1.4.2.Final.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-netty-shaded/1.46.0/grpc-netty-shaded-1.46.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.grpc/grpc-grpclb/1.46.0/89efab5177d839d37f7e70b657949e39b1c43cbf/grpc-grpclb-1.46.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.grpc/grpc-rls/1.46.0/9b1069151f5f3ceaab8a9a99785bd164187fb070/grpc-rls-1.46.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.api.grpc/grpc-google-common-protos/2.8.3/acd72a8751586b54adf8f13d7c2be49872fb4361/grpc-google-common-protos-2.8.3.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-services/1.46.0/grpc-services-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-protobuf/1.46.0/grpc-protobuf-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-stub/1.46.0/grpc-stub-1.46.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protobuf-java-util/3.20.0/ee4496b296418283cbe7ae784984347fc4717a9a/protobuf-java-util-3.20.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.api.grpc/proto-google-common-protos/2.8.3/4e0925604cd8d6ee796bd398280d46ed604c07b/proto-google-common-protos-2.8.3.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protobuf-java/3.20.0/3c72ddaaab7ffafe789e4f732c1fd614eb798bf4/protobuf-java-3.20.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.googlecode.protobuf-java-format/protobuf-java-format/1.4/b8163b6940102c1808814471476f5293dfb419df/protobuf-java-format-1.4.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.grpc/grpc-okhttp/1.46.0/2d997a3c707982a76a494af31cece8c16ee0ff31/grpc-okhttp-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-core/1.46.0/grpc-core-1.46.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.grpc/grpc-auth/1.46.0/b30a2b44a2ebfabc09326af921e1667d34948310/grpc-auth-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-protobuf-lite/1.46.0/grpc-protobuf-lite-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-api/1.46.0/grpc-api-1.46.0.jar:/home/vmelnykov/.m2/repository/io/grpc/grpc-context/1.46.0/grpc-context-1.46.0.jar:/home/vmelnykov/.m2/repository/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-test-junit5/1.6.21/ed8b4f0709ee0509c5fc6ac90ab60bc54c6ce60e/kotlin-test-junit5-1.6.21.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-test/1.6.21/1e9d81b013e31b22da67464948869fc56c652527/kotlin-test-1.6.21.jar:/home/vmelnykov/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-jdk7/1.6.21/kotlin-stdlib-jdk7-1.6.21.jar:/home/vmelnykov/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.6.21/kotlin-stdlib-1.6.21.jar:/home/vmelnykov/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-common/1.6.21/kotlin-stdlib-common-1.6.21.jar:/home/vmelnykov/.m2/repository/org/junit/jupiter/junit-jupiter/5.8.2/junit-jupiter-5.8.2.jar:/home/vmelnykov/.m2/repository/org/assertj/assertj-core/3.22.0/assertj-core-3.22.0.jar:/home/vmelnykov/.m2/repository/org/mockito/kotlin/mockito-kotlin/4.0.0/mockito-kotlin-4.0.0.jar:/home/vmelnykov/.m2/repository/org/springframework/security/spring-security-test/5.7.1/spring-security-test-5.7.1.jar:/home/vmelnykov/.m2/repository/com/h2database/h2/2.1.212/h2-2.1.212.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.13.3/jackson-datatype-jdk8-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.13.3/jackson-module-parameter-names-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/jaxrs/jackson-jaxrs-json-provider/2.13.3/jackson-jaxrs-json-provider-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/jaxrs/jackson-jaxrs-base/2.13.3/jackson-jaxrs-base-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/module/jackson-module-jaxb-annotations/2.13.3/jackson-module-jaxb-annotations-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/dataformat/jackson-dataformat-yaml/2.13.3/jackson-dataformat-yaml-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.13.3/jackson-databind-2.13.3.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-models/2.2.0/swagger-models-2.2.0.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.13.3/jackson-annotations-2.13.3.jar:/home/vmelnykov/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.13.3/jackson-core-2.13.3.jar:/home/vmelnykov/.m2/repository/org/junit/jupiter/junit-jupiter-params/5.8.2/junit-jupiter-params-5.8.2.jar:/home/vmelnykov/.m2/repository/org/junit/jupiter/junit-jupiter-engine/5.8.2/junit-jupiter-engine-5.8.2.jar:/home/vmelnykov/.m2/repository/org/mockito/mockito-junit-jupiter/4.5.1/mockito-junit-jupiter-4.5.1.jar:/home/vmelnykov/.m2/repository/org/junit/jupiter/junit-jupiter-api/5.8.2/junit-jupiter-api-5.8.2.jar:/home/vmelnykov/.m2/repository/org/junit/platform/junit-platform-engine/1.8.2/junit-platform-engine-1.8.2.jar:/home/vmelnykov/.m2/repository/org/junit/platform/junit-platform-commons/1.8.2/junit-platform-commons-1.8.2.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.7.0/spring-boot-starter-logging-2.7.0.jar:/home/vmelnykov/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.17.2/log4j-to-slf4j-2.17.2.jar:/home/vmelnykov/.m2/repository/org/apache/logging/log4j/log4j-api/2.17.2/log4j-api-2.17.2.jar:/home/vmelnykov/.m2/repository/io/micrometer/micrometer-core/1.9.0/micrometer-core-1.9.0.jar:/home/vmelnykov/.m2/repository/org/mockito/mockito-core/4.5.1/mockito-core-4.5.1.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-reactor-netty/2.7.0/52097e25830b3a20f55268a29f326f94406ada4/spring-boot-starter-reactor-netty-2.7.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.projectreactor.netty/reactor-netty-http/1.0.19/bcb2d93714306e8d1235e16cc953ac2bf88ac93c/reactor-netty-http-1.0.19.jar:/home/vmelnykov/.m2/repository/io/netty/netty-codec-http2/4.1.77.Final/netty-codec-http2-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.projectreactor.netty/reactor-netty-core/1.0.19/adb58ba62d297b56d6b7915a50f048eddcfc81a6/reactor-netty-core-1.0.19.jar:/home/vmelnykov/.m2/repository/io/netty/netty-handler-proxy/4.1.77.Final/netty-handler-proxy-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-codec-http/4.1.77.Final/netty-codec-http-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-resolver-dns-native-macos/4.1.77.Final/ba23bed7fd221158b5064096f9f8e286b190250c/netty-resolver-dns-native-macos-4.1.77.Final-osx-x86_64.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-resolver-dns-classes-macos/4.1.77.Final/60a6b7a3d81982bcf98db89c20b04f870d2d5ea0/netty-resolver-dns-classes-macos-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-resolver-dns/4.1.77.Final/aad506ab6804e2720771634e2de2a065fa678126/netty-resolver-dns-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-native-epoll/4.1.77.Final/8d10e9e138dac52172dd83229bdc89197100c723/netty-transport-native-epoll-4.1.77.Final-linux-x86_64.jar:/home/vmelnykov/.m2/repository/io/netty/netty-handler/4.1.77.Final/netty-handler-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-codec-dns/4.1.77.Final/a0a9bc85703efbab626fb8642e08e221b59dc604/netty-codec-dns-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-codec-socks/4.1.77.Final/netty-codec-socks-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-codec/4.1.77.Final/netty-codec-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-classes-epoll/4.1.77.Final/dd70dbccbcf98382223a59044f3c08d8e9920cad/netty-transport-classes-epoll-4.1.77.Final.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/io.netty/netty-transport-native-unix-common/4.1.77.Final/c95d53486414b3270d08057957c5da8e0c37e4eb/netty-transport-native-unix-common-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-transport/4.1.77.Final/netty-transport-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-buffer/4.1.77.Final/netty-buffer-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-resolver/4.1.77.Final/netty-resolver-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/io/netty/netty-common/4.1.77.Final/netty-common-4.1.77.Final.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-webflux/5.3.20/spring-webflux-5.3.20.jar:/home/vmelnykov/.m2/repository/io/projectreactor/reactor-core/3.4.18/reactor-core-3.4.18.jar:/home/vmelnykov/.m2/repository/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-webmvc/5.3.20/spring-webmvc-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/security/spring-security-config/5.7.1/spring-security-config-5.7.1.jar:/home/vmelnykov/.m2/repository/org/springframework/security/spring-security-web/5.7.1/spring-security-web-5.7.1.jar:/home/vmelnykov/.m2/repository/org/springframework/security/spring-security-core/5.7.1/spring-security-core-5.7.1.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-test/2.7.0/spring-boot-test-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-actuator/2.7.0/spring-boot-actuator-2.7.0.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot/2.7.0/spring-boot-2.7.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-swagger-ui/3.0.0/springfox-swagger-ui-3.0.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-spring-webmvc/3.0.0/springfox-spring-webmvc-3.0.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-spring-web/3.0.0/springfox-spring-web-3.0.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-schema/3.0.0/springfox-schema-3.0.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-spi/3.0.0/springfox-spi-3.0.0.jar:/home/vmelnykov/.m2/repository/io/springfox/springfox-core/3.0.0/springfox-core-3.0.0.jar:/home/vmelnykov/.m2/repository/org/springframework/plugin/spring-plugin-metadata/2.0.0.RELEASE/spring-plugin-metadata-2.0.0.RELEASE.jar:/home/vmelnykov/.m2/repository/org/springframework/plugin/spring-plugin-core/2.0.0.RELEASE/spring-plugin-core-2.0.0.RELEASE.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-context/5.3.20/spring-context-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-aop/5.3.20/spring-aop-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-web/5.3.20/spring-web-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-beans/5.3.20/spring-beans-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-test/5.3.20/spring-test-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-expression/5.3.20/spring-expression-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-core/5.3.20/spring-core-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/spring-jcl/5.3.20/spring-jcl-5.3.20.jar:/home/vmelnykov/.m2/repository/org/springframework/security/spring-security-crypto/5.7.1/spring-security-crypto-5.7.1.jar:/home/vmelnykov/.m2/repository/net/bytebuddy/byte-buddy/1.12.10/byte-buddy-1.12.10.jar:/home/vmelnykov/.m2/repository/net/bytebuddy/byte-buddy-agent/1.12.10/byte-buddy-agent-1.12.10.jar:/home/vmelnykov/.m2/repository/org/hibernate/validator/hibernate-validator/6.2.3.Final/hibernate-validator-6.2.3.Final.jar:/home/vmelnykov/.m2/repository/com/fasterxml/classmate/1.5.1/classmate-1.5.1.jar:/home/vmelnykov/.m2/repository/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:/home/vmelnykov/.m2/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar:/home/vmelnykov/.m2/repository/org/glassfish/jaxb/jaxb-runtime/2.3.6/jaxb-runtime-2.3.6.jar:/home/vmelnykov/.m2/repository/org/glassfish/jaxb/txw2/2.3.6/txw2-2.3.6.jar:/home/vmelnykov/.m2/repository/org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar:/home/vmelnykov/.m2/repository/org/apache/httpcomponents/httpcore/4.4.15/httpcore-4.4.15.jar:/home/vmelnykov/.m2/repository/com/sun/activation/jakarta.activation/1.2.2/jakarta.activation-1.2.2.jar:/home/vmelnykov/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/2.3.3/jakarta.xml.bind-api-2.3.3.jar:/home/vmelnykov/.m2/repository/jakarta/activation/jakarta.activation-api/1.2.2/jakarta.activation-api-1.2.2.jar:/home/vmelnykov/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.7.0/spring-boot-starter-tomcat-2.7.0.jar:/home/vmelnykov/.m2/repository/jakarta/annotation/jakarta.annotation-api/1.3.5/jakarta.annotation-api-1.3.5.jar:/home/vmelnykov/.m2/repository/jakarta/validation/jakarta.validation-api/2.0.2/jakarta.validation-api-2.0.2.jar:/home/vmelnykov/.m2/repository/javax/xml/bind/jaxb-api/2.4.0-b180830.0359/jaxb-api-2.4.0-b180830.0359.jar:/home/vmelnykov/.m2/repository/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/javax.annotation/javax.annotation-api/1.3.2/934c04d3cfef185a8008e7bf34331b79730a9d43/javax.annotation-api-1.3.2.jar:/home/vmelnykov/.m2/repository/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar:/home/vmelnykov/.m2/repository/com/jayway/jsonpath/json-path/2.7.0/json-path-2.7.0.jar:/home/vmelnykov/.m2/repository/net/minidev/json-smart/2.4.8/json-smart-2.4.8.jar:/home/vmelnykov/.m2/repository/org/skyscreamer/jsonassert/1.5.0/jsonassert-1.5.0.jar:/home/vmelnykov/.m2/repository/ch/qos/logback/logback-classic/1.2.11/logback-classic-1.2.11.jar:/home/vmelnykov/.m2/repository/ch/qos/logback/logback-core/1.2.11/logback-core-1.2.11.jar:/home/vmelnykov/.m2/repository/org/slf4j/jul-to-slf4j/1.7.36/jul-to-slf4j-1.7.36.jar:/home/vmelnykov/.m2/repository/org/thymeleaf/thymeleaf-spring5/3.0.15.RELEASE/thymeleaf-spring5-3.0.15.RELEASE.jar:/home/vmelnykov/.m2/repository/org/thymeleaf/extras/thymeleaf-extras-java8time/3.0.4.RELEASE/thymeleaf-extras-java8time-3.0.4.RELEASE.jar:/home/vmelnykov/.m2/repository/org/thymeleaf/thymeleaf/3.0.15.RELEASE/thymeleaf-3.0.15.RELEASE.jar:/home/vmelnykov/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar:/home/vmelnykov/.m2/repository/org/yaml/snakeyaml/1.30/snakeyaml-1.30.jar:/home/vmelnykov/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.63/tomcat-embed-websocket-9.0.63.jar:/home/vmelnykov/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.63/tomcat-embed-core-9.0.63.jar:/home/vmelnykov/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.63/tomcat-embed-el-9.0.63.jar:/home/vmelnykov/.m2/repository/org/xmlunit/xmlunit-core/2.9.0/xmlunit-core-2.9.0.jar:/home/vmelnykov/.m2/repository/com/google/guava/guava/31.0.1-android/guava-31.0.1-android.jar:/home/vmelnykov/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-jaxrs2-servlet-initializer/2.2.0/swagger-jaxrs2-servlet-initializer-2.2.0.jar:/home/vmelnykov/.m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:/home/vmelnykov/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:/home/vmelnykov/.m2/repository/com/google/errorprone/error_prone_annotations/2.10.0/error_prone_annotations-2.10.0.jar:/home/vmelnykov/.m2/repository/io/perfmark/perfmark-api/0.25.0/perfmark-api-0.25.0.jar:/home/vmelnykov/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:/home/vmelnykov/.m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:/home/vmelnykov/.m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.19/animal-sniffer-annotations-1.19.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.auth/google-auth-library-credentials/1.4.0/50f543f1da68956b4dec792c64e2f8a2f1dcf376/google-auth-library-credentials-1.4.0.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/1.17.5/34336f82f14dde1c0752fd5f0546dbf3c3225aba/okio-1.17.5.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/com.google.auto.value/auto-value-annotations/1.9/25a0fcef915f663679fcdb447541c5d86a9be4ba/auto-value-annotations-1.9.jar:/home/vmelnykov/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:/home/vmelnykov/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:/home/vmelnykov/.m2/repository/org/checkerframework/checker-qual/3.12.0/checker-qual-3.12.0.jar:/home/vmelnykov/.m2/repository/org/checkerframework/checker-compat-qual/2.5.5/checker-compat-qual-2.5.5.jar:/home/vmelnykov/.m2/repository/org/jetbrains/annotations/13.0/annotations-13.0.jar:/home/vmelnykov/.m2/repository/io/github/classgraph/classgraph/4.8.138/classgraph-4.8.138.jar:/home/vmelnykov/.gradle/caches/modules-2/files-2.1/ognl/ognl/3.1.26/922d3d922b8aa40146d842114c184c8b403d2f4f/ognl-3.1.26.jar:/home/vmelnykov/.m2/repository/org/javassist/javassist/3.25.0-GA/javassist-3.25.0-GA.jar:/home/vmelnykov/.m2/repository/io/swagger/core/v3/swagger-annotations/2.2.0/swagger-annotations-2.2.0.jar:/home/vmelnykov/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/vmelnykov/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/home/vmelnykov/.m2/repository/com/sun/istack/istack-commons-runtime/3.0.12/istack-commons-runtime-3.0.12.jar:/home/vmelnykov/.m2/repository/org/hdrhistogram/HdrHistogram/2.1.12/HdrHistogram-2.1.12.jar:/home/vmelnykov/.m2/repository/org/latencyutils/LatencyUtils/2.0.3/LatencyUtils-2.0.3.jar:/home/vmelnykov/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/home/vmelnykov/.m2/repository/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar:/home/vmelnykov/.m2/repository/org/objenesis/objenesis/3.2/objenesis-3.2.jar:/home/vmelnykov/.m2/repository/com/vaadin/external/google/android-json/0.0.20131108.vaadin1/android-json-0.0.20131108.vaadin1.jar:/home/vmelnykov/.m2/repository/org/attoparser/attoparser/2.0.5.RELEASE/attoparser-2.0.5.RELEASE.jar:/home/vmelnykov/.m2/repository/org/unbescape/unbescape/1.1.6.RELEASE/unbescape-1.1.6.RELEASE.jar:/home/vmelnykov/.m2/repository/net/minidev/accessors-smart/2.4.8/accessors-smart-2.4.8.jar:/home/vmelnykov/.m2/repository/org/ow2/asm/asm/9.1/asm-9.1.jar
            //org.gradle.internal.worker.tmpdir = /home/vmelnykov/projects/study-project-02-gradle-grpc/auth-soa/build/tmp/test/work
            //org.gradle.native = false
            //org.gradle.test.worker = 1

            val gradleSysProps = listOf("org.gradle.internal.worker.tmpdir", "org.gradle.native", "org.gradle.test.worker")
            @Suppress("UnnecessaryVariable")
            val hasGradleSysProp = gradleSysProps.any { System.getProperty(it) != null }

            return hasGradleSysProp
        }

        /*
        @JvmStatic
        fun isLaunchedByIdea(): Boolean {
            // sun.java.command = com.intellij.rt.junit.JUnitStarter -ideVersion5 -junit5 com.mvv.bank.account.soa.conversion.TempTest
        }
        */

        @JvmStatic
        fun isLaunchedByMaven(): Boolean {
            val mavenSysProps = listOf("surefire.real.class.path", "surefire.test.class.path")
            val hasMavenSysProp = mavenSysProps.any { System.getProperty(it) != null }

            val mavenEnvVars = listOf("MAVEN_CMD_LINE_ARGS", "MAVEN_PROJECTBASEDIR")
            val hasMavenEnvVars = mavenEnvVars.any { System.getenv(it) != null }

            return hasMavenSysProp || hasMavenEnvVars
        }
    }
}


private fun findMavenProjectArtifactId(projectDir: Path): String? {

    val pomXml = projectDir.resolve("pom.xml")

    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc: Document = builder.parse(pomXml.toFile())

    val xPath: XPath = XPathFactory.newInstance().newXPath()
    return xPath.compile("/project/artifactId").evaluate(doc)?.toString()
}

private fun findGradleProjectName(projectDir: Path): String? {

    val possibleBuildGradleFiles = listOf(
        projectDir.resolve("build.gradle"),
        projectDir.resolve("build.gradle.kts"),
    )

    val buildGradleFiles = possibleBuildGradleFiles.filter { Files.exists(it) }
    // Now we use build.gradle only for validation on its presence.
    // It would be extremely difficult/impossible to find customized value of
    // projectName/finalName/archiveName/mavenArtifactId by any possibly dynamic way
    // (for example by 'jar { baseName "customizedProjectName" }' or other ways)
    if (buildGradleFiles.size != 1) {
        log.warn("Only one build.gradle is not found (found build.gradle files: $buildGradleFiles).")
        return null
    }

    val defaultGradleProjectName = projectDir.fileName.toString()

    val possibleSettingGradleFiles = listOf(
        projectDir.resolve("settings.gradle"),
        projectDir.resolve("settings.gradle.kts"),
    )

    val settingGradleFiles = possibleSettingGradleFiles.filter { Files.exists(it) }

    val customizedProjectName = settingGradleFiles.flatMap { Files.readAllLines(it) }
        .filter { line -> line.trim().startsWith("rootProject.name") }
        .take(1)
        .map { parseRootProjectName(it) }
        .firstOrNull()

    return customizedProjectName ?: defaultGradleProjectName
}

private fun parseRootProjectName(line: String): String {
    return line.substringAfter("=")
        .trim()
        .removeSurrounding("\'")
        .removeSurrounding("\"")

}

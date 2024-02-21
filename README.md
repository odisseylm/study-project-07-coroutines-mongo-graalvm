# study-project-07-coroutines-mongo-graalvm





Build
 - `./gradlew :clean :build`
 - `./gradlew :clean :build -Dorg.gradle.java.home=/software/graalvm-jdk-21.0.2+13.1` << !!! java.home is passes as SYSTEM property !!!
 - `./gradlew :clean :build -Dorg.gradle.java.home=$GRAALVM_HOME`
 - `./gradlew :clean :build -Porg.gradle.java.installations.paths=/software/graalvm-jdk-21.0.2+13.1` << !!! toolchains is passes as GRADLE property !!!
 - `./gradlew :clean :build -Porg.gradle.java.installations.paths=$GRAALVM_HOME`
 - `./gradlew :clean :test`
 - `./gradlew :clean :test :compileNative`
 - `./gradlew :nativeTest`
 - `./gradlew :clean :compileKotlin`
 - `./gradlew :test --rerun`  << forcing tests to run
 - `./gradlew :build -x test` << skip tests
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM"`
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM" -x processTestAot`
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM" --debug-jvm` << Error:  JDWP unable to get necessary JVMTI capabilities
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM" -Dorg.gradle.debug=true --no-daemon` << Debug gradle script
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM" --rerun`
 - `./gradlew :test --tests "com.mvv.demo2.GraalVMTest.forGraalVM" --rerun --debug-jvm --no-daemon --no-rebuild` << Debug application/test
 - `./gradlew :test --continuous`
 - `./gradlew :test --stacktrace -Dorg.gradle.debug=true --no-daemon`
 - `./gradlew dependencies`
 - `./gradlew -q javaToolchains` << show toolchains
 - `./gradlew buildEnvironment`
 - `mvn dependency:sources`
   - `mvn dependency:sources dependency:resolve -Dclassifier=javadoc`


Useful commands
 - `./gradlew tasks --all`


Tasks
 - bootBuildImage


Test

 - http://localhost:8080/test
   - Credentials
     - User 'user' and random password from console
     - 'user'/'user' in tests



Docs

 - GraalVM
   - https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
   - https://graalvm.github.io/native-build-tools/0.9.28/gradle-plugin.html
   - https://graalvm.github.io/native-build-tools/latest/index.html
   - https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/pdf/spring-boot-gradle-plugin-reference.pdf

 - Gradle
   - Tests & Integration tests
     - https://docs.gradle.org/current/userguide/java_testing.html

 - Coroutines
   - https://docs.spring.io/spring-data/mongodb/reference/kotlin/coroutines.html

 - Tests
   - Use `@DisabledInAotMode` for tests with mocks


Custom toolchain locations

 org.gradle.java.installations.fromEnv=JDK8,JRE17
 org.gradle.java.installations.paths=/custom/path/jdk1.8,/shared/jre11


GraalVM


Testing support

 - `./gradlew nativeTest`


Reflection support and running with the native agent

 - `./gradlew -Pagent run`  # Runs on JVM with native-image-agent.
 - `./gradlew -Pagent nativeBuild` # Builds image using configuration acquired by agent.

 For testing
 - `./gradlew -Pagent` test # Runs on JVM with native-image-agent.
 - `./gradlew -Pagent nativeTest` # Builds image using configuration acquired by agent.


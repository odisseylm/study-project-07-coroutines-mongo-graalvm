# study-project-07-coroutines-mongo-graalvm





Build
 - `./gradlew :clean :test`
 - `./gradlew :clean :test :compileNative`
 - `./gradlew :nativeTest`
 - `./gradlew :clean :compileKotlin`
 - `./gradlew :test --rerun`  // forcing tests to run
 - `./gradlew :build -x test` // skip tests
 - `./gradlew :test  --tests "com.mvv.demo2.GraalVMTest.forGraalVM" --rerun`
 - `./gradlew :test --stacktrace -Dorg.gradle.debug=true --no-daemon`


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

 - Gradle
   - Tests & Integration tests
     - https://docs.gradle.org/current/userguide/java_testing.html

 - Tests
   - Use `@DisabledInAotMode` for tests with mocks


GraalVM


Testing support

 - `./gradlew nativeTest`


Reflection support and running with the native agent

 - `./gradlew -Pagent run`  # Runs on JVM with native-image-agent.
 - `./gradlew -Pagent nativeBuild` # Builds image using configuration acquired by agent.

 For testing
 - `./gradlew -Pagent` test # Runs on JVM with native-image-agent.
 - `./gradlew -Pagent nativeTest` # Builds image using configuration acquired by agent.


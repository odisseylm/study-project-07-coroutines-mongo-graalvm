//import org.gradle.jvm.toolchain.internal.DefaultJavaToolchainDownload
//import java.util.Optional

rootProject.name = "demo2"

/*
pluginManagement {
    val helloPluginVersion: String by settings /// TODO: what is it??? Investigate!
    plugins {
        id("com.example.hello") version helloPluginVersion
    }
}
*/

/*
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
    }
    resolutionStrategy {
    }
}
*/


/*
abstract class JavaToolchainResolverImplementation22 : JavaToolchainResolver {
    override fun resolve(request: JavaToolchainRequest): Optional<JavaToolchainDownload> {
        println("## JavaToolchainResolverImplementation#resolve ${request.javaToolchainSpec}")

        // /software/graalvm-jdk-21.0.2+13.1
        return Optional.of(
            DefaultJavaToolchainDownload.fromUri(
                File("/software/graalvm-jdk-21.0.2+13.1").toURI()))

        //return Optional.empty() // custom mapping logic goes here instead
    }
}


abstract class JavaToolchainResolverPlugin : Plugin<Settings> {
    //@get:Inject
    //protected abstract val toolchainResolverRegistry: JavaToolchainResolverRegistry
    //    get
    @Inject
    protected abstract fun getToolchainResolverRegistry(): JavaToolchainResolverRegistry

    override fun apply(settings: Settings) {
        println("## JavaToolchainResolverPlugin#apply")

        settings.plugins.apply("jvm-toolchain-management")
        println("## JavaToolchainResolverPlugin => plugin 'jvm-toolchain-management' is applied.")

        val registry = getToolchainResolverRegistry()
        registry.register(JavaToolchainResolverImplementation22::class.java)
        println("## JavaToolchainResolverPlugin => custom JavaToolchainResolverImplementation is registered.")
    }
}


//apply(plugin = "my-JavaToolchainResolverPlugin")
//apply(plugin = "my-JavaToolchainResolverPlugin")

//apply(Action<ObjectConfigurationAction> {
//    plugin(JavaToolchainResolverPlugin::class.java)
//})

apply<JavaToolchainResolverPlugin>()
//apply(from = "other.gradle.kts")
*/

/*
gradlePlugin {
    plugins {
        create("hello") {
            id = "com.example.hello"
            implementationClass = "com.example.hello.HelloPlugin"
        }
        create("goodbye") {
            id = "com.example.goodbye"
            implementationClass = "com.example.goodbye.GoodbyePlugin"
        }
    }
}
*/

package com.mvv.demo2.app

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectContainerResponse
import com.mvv.demo2.BuildToolHelper.Companion.getProjectDirectory
import com.mvv.demo2.Demo2Application
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.concurrent.TimeUnit


@TestConfiguration(proxyBeanMethods = false)
class TestDemo2Application {

	/*
	@Bean
	fun mongoClientSettings(): MongoClientSettings? {
		return MongoClientSettings.builder()
			.applyToServerSettings {
				it.heartbeatFrequency(30L, TimeUnit.SECONDS)
				it.minHeartbeatFrequency(30L, TimeUnit.SECONDS)
			}
			.build()
	}
	*/

	@Bean
	fun mongoClientSettingsBuilderCustomizer(): MongoClientSettingsBuilderCustomizer {
		return MongoClientSettingsBuilderCustomizer {
			// to minimize heartbeat failures in logs when mongodb container is not fully ready (happens in default 'replica-set' mode)
			it.applyToServerSettings { it.heartbeatFrequency(10, TimeUnit.SECONDS) }
			it.applyToServerSettings { it.minHeartbeatFrequency(10, TimeUnit.SECONDS) }
		}
	}

	@Bean
	@ServiceConnection
	fun mongoDbContainer(): MongoDBContainer {

		val mongoMode = MongoDBStartMode.Standard
		val connectionSettings = MongoDBConnectSettings(
			user = "db1user",
			psw = "db1psw",
			db = "db1",
			userAuthDb = "db1",
		)
		val projectDir = getProjectDirectory(TestDemo2Application::class)

		val mc = MongoDBContainer2(mongoMode, DockerImageName.parse("mongo:latest"), connectionSettings)
			.withFileSystemBind("$projectDir/src/main/resources/mongodb-init", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY)

		@Suppress("KotlinConstantConditions")
		if (mongoMode == MongoDBStartMode.Standard) {
			mc.withEnv("MONGO_INITDB_DATABASE", "test")
				// MONGO_INITDB_ROOT_USERNAME & MONGO_INITDB_ROOT_PASSWORD does not work with enabled replicas (default test-containers behavior)
				.withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
				.withEnv("MONGO_INITDB_ROOT_PASSWORD", "secret")
				.withStartupAttempts(3)

			mc.setWaitStrategy(Wait.forLogMessage("(?i).*waiting for connections.*", 2))
		}

		return mc

		//.waitingFor(Wait.forHttp("/"))
			//.waitingFor(Wait.forListeningPort())
			//.waitingFor(Wait.forHealthcheck())

			// Log messages:
			//   mongod startup complete
			//    demo2 app data are inserted.
			//   mongod shutdown complete
			//   MongoDB init process complete; ready for start up.
			//
			//.waitingFor(Wait.forLogMessage(".*demo2 app data are inserted.*", 1))


			//.waitingFor(HostPortWaitStrategy().withStartupTimeout( Duration.ofSeconds(5)))
			//.waitingFor(TimeoutWaitStrategy(10_000))
			//.withStartupCheckStrategy(IsRunningStartupCheckStrategy22())
			//.withStartupCheckStrategy(IsRunningStartupCheckStrategy().withTimeout(Duration.ofSeconds(20)))

			//.withExposedPorts()
			//.addFixedExposedPort()

		//mc.waitingFor(Wait.forLogMessage(".*demo2 app data are inserted.*", 1))
		//mc.setWaitStrategy(Wait.forLogMessage("(?i).*waiting for connections.*", 1)) // works ok
		//mc.setWaitStrategy(Wait.forLogMessage("(?i).*waiting for connections.*", 2)) // fails
		//mc.setWaitStrategy(Wait.forLogMessage(".*waiting for connections.*", 1)) // also fails ??? Why?

		//mc.setWaitStrategy(LogMessageWaitStrategy2("(?i).*666waiting for connections.*") { it.contains("888waiting for connections", ignoreCase = true) }) // works ok
		//mc.setWaitStrategy(LogMessageWaitStrategy2("(?i).*666waiting for connections.*") { it.contains("waiting for connections", ignoreCase = true) }) // works ok

		//mc.withCommand()
		//mc.withCommand("mongod")
		//mc.commandParts = arrayOf("mongod")
		//mc.withCommand("--replSet", "docker-rs")
		//mc.withCommand("docker-rs")

		//mc.withStartupTimeout(Duration.ofSeconds(10))

		//mc.withStartupAttempts(3)
		//mc.setContainerDef()
		//mc.logConsumers.add( Consumer {  } )

		//mc.setStartupCheckStrategy(IndefiniteWaitOneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(30)))
		//mc.setStartupCheckStrategy(OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(15)))

		//mc.withStartupCheckStrategy(OneShotStartupCheckStrategy().withTimeout(Duration.ofSeconds(20)))
		//mc.withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(20)))

		//mc.setWaitStrategy(TimeoutWaitStrategy(5000L, mc.getWaitStrategy()))
		//mc.setWaitStrategy(TimeoutWaitStrategy(10_000, HostPortWaitStrategy()))
		//mc.setWaitStrategy(HostPortWaitStrategy().withStartupTimeout(Duration.ofSeconds(20)))

		//mc.waitingFor(DockerHealthcheckWaitStrategy())

		/*
		println("host11: ${mc.host}")
		println("containerId: ${mc.containerId}")

		val aaa = mc.portBindings.filter { it.endsWith(":27017") }
		println(aaa)

		val aaa2 = mc.portBindings
		println(aaa2)

		println("mc.isCreated: ${mc.isCreated}")
		println("mc.isRunning: ${mc.isRunning}")
		*/

		//mc.startupAttempts = 50
		//mc.startupCheckStrategy =
		//mc.setWaitStrategy(TimeoutWaitStrategy(5))

		/*
		startupCheckStrategy = {IsRunningStartupCheckStrategy@9273}
 		 timeout = {Duration@9284} "PT30S"

		waitStrategy = {HostPortWaitStrategy@9278}
		 ports = null
		 waitStrategyTarget = null
		 startupTimeout = {Duration@9286} "PT1M"
		 rateLimiter = {ConstantThroughputRateLimiter@9287}
		  timeBetweenInvocations = 1000
		  lastInvocation = 0

		waitStrategy = {LogMessageWaitStrategy@9296}
		 regEx = "(?i).*waiting for connections.*"
		 times = 1
		 waitStrategyTarget = null
		 startupTimeout = {Duration@9314} "PT1M"
		 rateLimiter = {ConstantThroughputRateLimiter@9287}
		  timeBetweenInvocations = 1000
		  lastInvocation = 0
		*/
	}

	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			fromApplication<Demo2Application>()
				.with(TestDemo2Application::class)
				.run(*args)
		}

	}
}


class TimeoutWaitStrategy(private val additionalTimeout: Long, private  val baseWaitStrategy: WaitStrategy? = null) : WaitStrategy {

	override fun waitUntilReady(waitStrategyTarget: WaitStrategyTarget) {
		println("waitUntilReady ...")
		Thread.sleep(additionalTimeout)
		println("waitUntilReady completed after ${additionalTimeout}ms")
		baseWaitStrategy?.waitUntilReady(waitStrategyTarget)
	}

	override fun withStartupTimeout(startupTimeout: Duration): WaitStrategy =
		TimeoutWaitStrategy(additionalTimeout, baseWaitStrategy?.withStartupTimeout(startupTimeout))
}


class IsRunningStartupCheckStrategy22 : IsRunningStartupCheckStrategy() {
	private val startChecking = System.currentTimeMillis()

	//override fun waitUntilStartupSuccessful(container: GenericContainer<*>?): Boolean =
	//	super.waitUntilStartupSuccessful(container)

	//override fun waitUntilStartupSuccessful(dockerClient: DockerClient?, containerId: String?): Boolean {
	//	return super.waitUntilStartupSuccessful(dockerClient, containerId)
	//}

	override fun checkStartupState(dockerClient: DockerClient?, containerId: String?): StartupStatus {
		return if (System.currentTimeMillis() - startChecking < 5000L)
			StartupStatus.NOT_YET_KNOWN
			else super.checkStartupState(dockerClient, containerId)
	}

	override fun getCurrentState(
		dockerClient: DockerClient?,
		containerId: String?
	): InspectContainerResponse.ContainerState {
		return super.getCurrentState(dockerClient, containerId)
	}
}


/*
class MongoDBContainer2(dockerImageName: DockerImageName) : MongoDBContainer(dockerImageName) {
	//private val port = 27018
	private val host = "localhost"
	private val user = "db1user"
	private val psw = "db1psw"
	private val userAuthDb = "db1" // or "admin" (I don't know which approach is better.)
	//private val db = "db1"

	override fun getConnectionString(): String {

		//return String.format("mongodb://%s:%d", host, getMappedPort(MongoDBContainerDef.MONGODB_INTERNAL_PORT))
		//return String.format("mongodb://%s:%d", host, getMappedPort(27017))

		val port = getMappedPort(27017)

		//return "mongodb://$user:$psw@$host:$port/$userAuthDb"
		return "mongodb://$user:$psw@$host:$port/?authSource=$userAuthDb"

		//return super.getConnectionString()
	}

	override fun getReplicaSetUrl(databaseName: String?): String {
		//return super.getReplicaSetUrl(databaseName)
		val port = getMappedPort(27017)

		//return "mongodb://$user:$psw@$host:$port/$db?authSource=$userAuthDb"
		return "mongodb://$user:$psw@$host:$port/$databaseName?authSource=$userAuthDb"
	}

	override fun containerIsStarted(containerInfo: InspectContainerResponse?) {
		super.containerIsStarted(containerInfo)
	}

	override fun containerIsStarted(containerInfo: InspectContainerResponse, reused: Boolean) {
		//super.containerIsStarted(containerInfo, reused)
	}
}
*/

/*
	@Bean
	MongoClientSettings mongoClientSettings() {
		return MongoClientSettings.builder().build();
	}

public class MongoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({MongoClient.class})
    public MongoClient mongo(ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers, MongoClientSettings settings) {
        return (MongoClient)(new MongoClientFactory(builderCustomizers.orderedStream().toList())).createMongoClient(settings);
    }

*/

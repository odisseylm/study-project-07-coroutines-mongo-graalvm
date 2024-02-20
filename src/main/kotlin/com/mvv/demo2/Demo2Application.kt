package com.mvv.demo2

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import com.mongodb.kotlin.client.coroutine.MongoClient as CorMongoClient


@SpringBootApplication
class Demo2Application {

	private val log: Logger = LoggerFactory.getLogger(Demo2Application::class.java)

	init {
		val cmd = ProcessHandle.current().info().commandLine().orElse(null)
		val javaHome = System.getProperty("java.home")

		log.info("### JAVA_HOME: $javaHome")
		log.info("### App is run as \n  $cmd")
	}
}

fun main(args: Array<String>) {
	val log: Logger = LoggerFactory.getLogger(Demo2Application::class.java)
	val cmd = ProcessHandle.current().info().commandLine().orElse(null)
	log.info("### App is run as \n  $cmd")

	// System.setProperty("spring.docker.compose.enabled", "false")
	runApplication<Demo2Application>(*args)
}


@Configuration
class CoroutineMongoDbConfig {

	// Mainly for tests... but probably it can be in prod too (with another conditions)...
	@Bean
	@ConditionalOnClass(name = ["org.testcontainers.containers.MongoDBContainer"])
	fun altCoroutineMongoClient(md: MongoConnectionDetails): CorMongoClient {
		println("## MongoConnectionDetails connection: ${md.connectionString}")
		println("## creating alt CorMongoClient")
		return CorMongoClient.create(md.connectionString)
	}

	@Bean
	@ConditionalOnMissingClass(value = ["org.testcontainers.containers.MongoDBContainer"])
	fun baseCoroutineMongoClient(mongoProperties: MongoProperties): CorMongoClient =
		with (mongoProperties) {
			println("## creating prod CorMongoClient")

			val authSource = if (authenticationDatabase != null) authenticationDatabase else database

			println("## base coroutineMongoClient => ${host}:$port  $username/*** authSource=$authSource")

			CorMongoClient.create(
				MongoClientSettings.builder()
					//.applicationName("demo2")
					.applyConnectionString(ConnectionString("mongodb://$host:$port"))
					.credential(
						MongoCredential.createCredential(
							username,
							authSource,
							password
						)
					)
					.build()
			)
		}
}

/*
@Configuration
class CoroutineMongoDbConfig (
	@Value("\${spring.data.mongodb.host}")
	private val mongodbHost: String,
	@Value("\${spring.data.mongodb.port}")
	private val mongodbPort: Int,
	@Value("\${spring.data.mongodb.database}")
	private val mongodbDatabase: String,
	@Value("\${spring.data.mongodb.username}")
	private val mongodbUsername: String,
	@Value("\${spring.data.mongodb.password}")
	private val mongodbPassword: String,
	@Value("\${spring.data.mongodb.authSource}")
	private val mongodbAuthSource: String,
	) {

	@Bean
	fun coroutineMongoClient(): CrMongoClient =
		CrMongoClient.create(
			MongoClientSettings.builder()
			//.applicationName("demo2")
			.applyConnectionString(ConnectionString("mongodb://$mongodbHost:$mongodbPort"))
			.credential(MongoCredential.createCredential(mongodbUsername, mongodbAuthSource, mongodbPassword.toCharArray()))
			.build())
}
*/

/*
@Configuration
@EnableCoroutine(
	proxyTargetClass = false, mode = AdviceMode.PROXY,
	order = Ordered.LOWEST_PRECEDENCE, schedulerDispatcher = "")
open class MyAppConfiguration {
	//...
	//TODO()
}
*/

/*
@Configuration
internal class ApplicationConfiguration {

	// T O D O: take values from spring props/@Value

	private val port = 27018
	private val host = "localhost"
	private val user = "db1user"
	private val psw = "db1psw"
	private val userAuthDb = "db1" // or "admin" (I don't know which approach is better.)

	private val db = "db1"

	init {
	    println("test 01")
	}


	@Bean
	fun mongoClient(env: Environment): MongoClient {

		val aaa1 = env.get("spring.data.mongodb.host")
		val aaa2 = env.get("spring.data.mongodb.port")
		println("aaa $aaa1:$aaa2")

		// "mongodb://$user:$psw@$host:$port/?authSource=$userAuthDb"

		// Works ok!
		//return MongoClients.create("mongodb://$user:$psw@$host:$port")

		return MongoClients.create(MongoClientSettings.builder()
			//.applicationName("demo2")
			.applyConnectionString(ConnectionString("mongodb://$host:$port"))
			.credential(MongoCredential.createCredential(user, userAuthDb, psw.toCharArray()))
			.build())

		// It does not work with '$db' at the end!
		//   '/database' is the name of the database to login to
	    //   and thus is only relevant if the username:password@ syntax is used.
	    //   If not specified the "admin" database will be used by default.
		// return MongoClients.create("mongodb://$user:$psw@$host:$port/$userAuthDb")
	}

	@Bean
	fun reactiveMongoClient(): RMongoClient {
		//return RMongoClients.create("mongodb://$user:$psw@$host:$port")
		return RMongoClients.create("mongodb://$user:$psw@$host:$port/$userAuthDb")
	}

	@Bean
	fun mongoTemplate(mongoClient: MongoClient): MongoOperations {
		return MongoTemplate(mongoClient, db)
	}

	@Bean
	fun reactiveMongoTemplate(mongoClient: RMongoClient): RMongoOperations {
		return RMongoTemplate(mongoClient, db)
	}

	//private fun aa(): MongoClientSettings {
	//	return MongoClientSettings.builder()
	//		.applicationName("demo2")
	//		.
	//		.build()
	//}
}
*/
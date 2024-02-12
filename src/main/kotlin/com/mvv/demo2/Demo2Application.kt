package com.mvv.demo2

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class Demo2Application {

	private val log: Logger = LoggerFactory.getLogger(Demo2Application::class.java)

	init {
		val cmd = ProcessHandle.current().info().commandLine().orElse(null)
		log.info("### App is run as \n  $cmd")
	    forGraalVM()
	}
}

fun main(args: Array<String>) {
	val log: Logger = LoggerFactory.getLogger(Demo2Application::class.java)
	val cmd = ProcessHandle.current().info().commandLine().orElse(null)
	log.info("### App is run as \n  $cmd")

	// System.setProperty("spring.docker.compose.enabled", "false")
	runApplication<Demo2Application>(*args)
}

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

	// TODO: take values from spring props/@Value

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
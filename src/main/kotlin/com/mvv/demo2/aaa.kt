package com.mvv.demo2

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoCollection as RMongoCollection
import kotlinx.coroutines.delay
import org.bson.Document
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoTemplate as RMongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

import com.mongodb.kotlin.client.coroutine.MongoClient as CorMongoClient
import kotlinx.coroutines.flow.Flow as CorFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import org.springframework.web.bind.annotation.RequestMapping


data class Customer (
    val name: String,
)

data class Banner (
    val aa: String,
)

@RestController()
class WelcomeController {
    @GetMapping("/")
    fun getCustomers(): String = "Welcome!"
}


@RestController()
@RequestMapping("users")
class MyController(
    private val mongoOperations: MongoOperations,
    private val rMongoOperations: ReactiveMongoOperations,
    //private val crMongoOperations: CorMongoOperations,
    private val crMongoClient: CorMongoClient,
) {

    @GetMapping("/customers")
    suspend /*open*/ fun getCustomers(): List<Customer> {
        //...
        //TODO()
        return listOf(Customer("customer1"))
    }

    @GetMapping("/test")
    fun test(): List<Customer> {

        val users: MongoCollection<Document> = mongoOperations.getCollection("users")

        if (mongoOperations is MongoTemplate) {
            val mongoDatabaseFactory = mongoOperations.mongoDatabaseFactory
            val db = mongoDatabaseFactory.mongoDatabase
            println("mongo db: $db")
        }

        val userCount: Long = users.countDocuments()
        println("user count $userCount")

        val allUsersIt1: FindIterable<Document> = users.find()
        val allUsers: List<Document> = allUsersIt1.asIterable().toList()

        return listOf(Customer("all our users count ${allUsers.size}"))
    }

    @GetMapping("/test-rx")
    @Suppress("UNUSED_VARIABLE", "ReactiveStreamsUnusedPublisher")
    fun testRx():
            //Mono<List<Customer>>
            Flux<Customer>
    {

        val users: Mono<RMongoCollection<Document>> = rMongoOperations.getCollection("users")

        if (rMongoOperations is RMongoTemplate) {
            val mongoDatabaseFactory = rMongoOperations.mongoDatabaseFactory
            val db = mongoDatabaseFactory.mongoDatabase
            println("mongo db: $db")
        }

        val exampleOfUsageCount01: Mono<List<Customer>> = users
            .flatMapMany { it.countDocuments() }
            .take(1)
            .single()
            //.doOnEach { userCount -> println("## user count $userCount") } // TODO: It is something strange??? How to use it?
            .doOnNext { userCount -> println("## user count $userCount") }
            .map { allUsersCount -> Customer("all our users count $allUsersCount") }
            .map { listOf(it) }

        val exampleOfUsageCount02: Flux<Customer> = users
            .flatMapMany { it.countDocuments() }
            .take(1)
            //.doOnEach { userCount -> println("## user count $userCount") } // TODO: It is something strange??? How to use it?
            .doOnNext { userCount -> println("## user count $userCount") }
            .map { allUsersCount -> Customer("all our users count $allUsersCount") }
            .map { it }

        val userEntities: Flux<Customer> = users
            .flatMapMany { it.find() }
            .doOnNext { doc -> println("## user $doc ( ${doc["name"]} )") }
            .map { userDoc -> Customer(userDoc.getString("name")) }

        return userEntities
    }

    @GetMapping("/test-coroutine")
    suspend fun testCoroutine(): CorFlow<Customer> {

        val database = crMongoClient.getDatabase("db1")

        val users = database.getCollection<Document>("users")

        val res: CorFlow<Customer> = users.find()
            .onEach { doc -> println("## user $doc ( ${doc["name"]} )") }
            .map { userDoc -> Customer(userDoc.getString("name")) }

        return res
    }

    @GetMapping("/suspend")
    suspend fun suspendingEndpoint(): Banner {
        delay(10)
        return Banner("banner0")
    }

    @GetMapping("/flow")
    fun flowEndpoint() = flow {
        val banner = Banner("fuck")
        delay(10)
        emit(banner)
        delay(10)
        emit(banner)
    }
}

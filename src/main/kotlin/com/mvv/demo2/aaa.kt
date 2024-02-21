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
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
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
    private val rxUserRepository: RxUserRepository,
    private val corUserRepository: CoroutineUserRepository,
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

    @GetMapping("/test-rx2")
    fun testRx2():
            //Mono<List<Customer>>
            Flux<Customer>
    {
        // It works ok.
        //val userEntities = rMongoOperations.getCollection("users")
        //    .flatMapMany { it.find(Filters.eq("name", "user1")) }
        //    .doOnNext { doc -> println("## user $doc ( ${doc["name"]} )") }
        //    .map { userDoc -> Customer(userDoc.getString("name")) }

        val users: Flux<User> = rxUserRepository.findByName("user1")

        val userEntities: Flux<Customer> = users
            .doOnNext { user -> println("## user $user ( ${user._id}/${user.name} )") }
            .map { user -> Customer(user.name) }

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

    @GetMapping("/test-coroutine2")
    suspend fun testCoroutine2(): CorFlow<Customer> {

        val users: CorFlow<User> = this.corUserRepository.findByName("user2")

        val res: CorFlow<Customer> = users
            .onEach { doc -> println("## user $doc ( ${doc._id} / ${doc.name} )") }
            .map { user -> Customer(user.name) }

        return res
    }

    @GetMapping("/test-coroutine3")
    suspend fun testCoroutine3(): List<Customer> {

        val users: List<User> = this.corUserRepository.findAllByName("user2")

        val res: List<Customer> = users
            .onEach { doc -> println("## user $doc ( ${doc._id} / ${doc.name} )") }
            .map { user -> Customer(user.name) }

        return res
    }

    @GetMapping("/test-coroutine4")
    suspend fun testCoroutine4(): List<Customer> {
        val user: User? = this.corUserRepository.findUserByName("user2") // "user2_unknown")
        val res = if (user != null) listOf(Customer(user.name)) else emptyList()
        return res
    }

    @GetMapping("/test-coroutine5")
    suspend fun testCoroutine5(): List<Customer> {
        val user: User = this.corUserRepository.getByName("user2") //"user2_unknown")

        //corUserRepository.findOne(user._id)
        corUserRepository.findById(user._id)
            ?: throw IllegalStateException("findOne does not work.")

        val res = listOf(Customer(user.name))
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


@org.springframework.data.mongodb.core.mapping.Document("users")
data class User (
    @Id
    @Suppress("PropertyName")
    var _id: ObjectId,
    //var _id: String,
    var name: String,
)

@Repository
interface RxUserRepository
    //: ReactiveMongoRepository<User, String> {
    : ReactiveMongoRepository<User, ObjectId> {
    fun findByName(name: String): Flux<User>
}

@Repository
interface CoroutineUserRepository : CoroutineCrudRepository<User, ObjectId> {
    suspend fun getByName(name: String): User {
        val userOrNull = findUserByName(name)
        return userOrNull ?: throw NoSuchElementException("No user with name [$name].")
    }
    // It has warnings, but it works ok.
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun findUserByName(name: String): User?

    fun findByName(name: String): CorFlow<User>

    // ??? Does not work.
    //suspend fun findOne(id: String): User? // Retrieve the data once and synchronously by suspending.
    // ??? Does not work.
    //suspend fun findOne(id: ObjectId): User?

    // It has warnings, but it works ok and for SMALL collections it should be less/more ok (in my opinion).
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun findAllByName(id: String): List<User>
}

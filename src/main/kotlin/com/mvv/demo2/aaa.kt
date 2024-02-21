package com.mvv.demo2

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.apache.commons.lang3.ThreadUtils
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import com.mongodb.kotlin.client.coroutine.MongoClient as CorMongoClient
import com.mongodb.reactivestreams.client.MongoCollection as RMongoCollection
import kotlinx.coroutines.flow.Flow as CorFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate as RMongoTemplate


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

        println("## test-rx 01, thread: $currentThreadName")

        val users: Mono<RMongoCollection<Document>> = rMongoOperations.getCollection("users")

        println("## test-rx 02, thread: $currentThreadName")

        if (rMongoOperations is RMongoTemplate) {
            val mongoDatabaseFactory = rMongoOperations.mongoDatabaseFactory
            val db = mongoDatabaseFactory.mongoDatabase
            println("mongo db: $db")
        }

        val exampleOfUsageCount01: Mono<List<Customer>> = users
            .flatMapMany {
                println("## test-rx 10, thread: $currentThreadName")
                it.countDocuments()
            }
            .take(1)
            .single()
            //.doOnEach { userCount -> println("## user count $userCount") } // TODO: It is something strange??? How to use it?
            .doOnNext { userCount ->
                println("## test-rx 11, thread: $currentThreadName")
                println("## user count $userCount") }
            .map { allUsersCount ->
                println("## test-rx 12, thread: $currentThreadName")
                Customer("all our users count $allUsersCount") }
            .map {
                println("## test-rx 13, thread: $currentThreadName")
                //dumpThreadNames()
                listOf(it) }

        val exampleOfUsageCount02: Flux<Customer> = users
            .flatMapMany { it.countDocuments() }
            .take(1)
            //.doOnEach { userCount -> println("## user count $userCount") } // TODO: It is something strange??? How to use it?
            .doOnNext { userCount -> println("## user count $userCount") }
            .map { allUsersCount -> Customer("all our users count $allUsersCount") }
            .map { it }

        val userEntities: Flux<Customer> = users
            .flatMapMany {
                println("## test-rx 30, thread: $currentThreadName")
                it.find() }
            .doOnNext { doc ->
                println("## test-rx 31, thread: $currentThreadName")
                println("## user $doc ( ${doc["name"]} )") }
            .map { userDoc ->
                println("## test-rx 32, thread: $currentThreadName")
                //dumpThreadNames()
                Customer(userDoc.getString("name")) }

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

        println("## test-rx2 01, thread: $currentThreadName")

        val users: Flux<User> = rxUserRepository.findByName("user1")

        println("## test-rx2 02, thread: $currentThreadName")

        val userEntities: Flux<Customer> = users
            .doOnNext { user ->
                println("## test-rx2 10, thread: $currentThreadName")
                println("## user $user ( ${user._id}/${user.name} )") }
            .map { user ->
                println("## test-rx2 11, thread: $currentThreadName")
                //dumpThreadNames()
                dumpThreadNames()
                Customer(user.name) }

        println("## test-rx2 03, thread: $currentThreadName")

        return userEntities
    }

    @GetMapping("/test-coroutine")
    suspend fun testCoroutine(): CorFlow<Customer> {

        println("## test-coroutine 01, thread: $currentThreadName")

        val database = crMongoClient.getDatabase("db1")

        println("## test-coroutine 02, thread: $currentThreadName")

        val users: com.mongodb.kotlin.client.coroutine.MongoCollection<Document> = database.getCollection<Document>("users")

        println("## test-coroutine 03, thread: $currentThreadName")

        val res: CorFlow<Customer> = users.find()
            .onEach { doc ->
                println("## test-coroutine 10, thread: $currentThreadName")
                println("## user $doc ( ${doc["name"]} )") }
            .map {
                println("## test-coroutine 11, thread: $currentThreadName")
                it }
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
            .map {
                println("## test-coroutine 12, thread: $currentThreadName")
                it }
            .map { userDoc ->
                println("## test-coroutine 13, thread: $currentThreadName")
                //dumpThreadNames()
                Customer(userDoc.getString("name")) }

        println("## test-coroutine 04, thread: $currentThreadName")

        return res
    }

    @GetMapping("/test-coroutine2")
    suspend fun testCoroutine2(): CorFlow<Customer> {

        println("## test-coroutine2 01, thread: $currentThreadName")

        val users: CorFlow<User> = this.corUserRepository.findByName("user2")

        println("## test-coroutine2 02, thread: $currentThreadName")

        val res: CorFlow<Customer> = users
            .onEach { doc ->
                println("## test-coroutine2 10, thread: $currentThreadName")
                println("## user $doc ( ${doc._id} / ${doc.name} )") }
            .map {
                println("## test-coroutine2 11, thread: $currentThreadName")
                it }
            // !!! flowOn affects to preceding ('before') operators! Not next ('after') operators.
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
            .map {
                println("## test-coroutine2 12, thread: $currentThreadName")
                it }
            // !!! flowOn affects to preceding ('before') operators! Not next ('after') operators.
            .flowOn(kotlinx.coroutines.Dispatchers.Default)
            .map {
                println("## test-coroutine2 13, thread: $currentThreadName")
                it }
            // !!! flowOn affects to preceding ('before') operators! Not next ('after') operators.
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
            .map {
                println("## test-coroutine2 14, thread: $currentThreadName")
                it }
            .map { user ->
                println("## test-coroutine2 15, thread: $currentThreadName")
                //dumpThreadNames()
                Customer(user.name) }

        println("## test-coroutine2 03, thread: $currentThreadName")

        /*
        Without flowOn()

        ## test [testCoroutine] started
        ## test-coroutine2 01, thread: http-nio-auto-1-exec-5 @coroutine#1
        ## test-coroutine2 02, thread: http-nio-auto-1-exec-5 @coroutine#1
        ## test-coroutine2 03, thread: http-nio-auto-1-exec-5 @coroutine#1
        ## test-coroutine2 10, thread: nioEventLoopGroup-3-5
        ## user User(_id=000000000000000000000002, name=user2) ( 000000000000000000000002 / user2 )
        ## test-coroutine2 11, thread: nioEventLoopGroup-3-5
        ## test-coroutine2 12, thread: nioEventLoopGroup-3-5
        ## test-coroutine2 13, thread: nioEventLoopGroup-3-5

        With flowOn()
        A bit unexpected behavior??? nioEventLoopGroup-3-5 disappeared??? Why???

        ## test-coroutine2 01, thread: http-nio-auto-1-exec-4 @coroutine#1
        ## test-coroutine2 02, thread: http-nio-auto-1-exec-4 @coroutine#1
        ## test-coroutine2 03, thread: http-nio-auto-1-exec-4 @coroutine#1
        ## test-coroutine2 10, thread: DefaultDispatcher-worker-1 @coroutine#2
        ## user User(_id=000000000000000000000002, name=user2) ( 000000000000000000000002 / user2 )
        ## test-coroutine2 11, thread: DefaultDispatcher-worker-1 @coroutine#2
        ## test-coroutine2 12, thread: DefaultDispatcher-worker-1 @coroutine#2
        ## test-coroutine2 13, thread: DefaultDispatcher-worker-1 @coroutine#2
        */

        return res
    }

    @GetMapping("/test-coroutine3")
    suspend fun testCoroutine3(): List<Customer> {

        println("## test-coroutine3 01, thread: $currentThreadName")

        val users: List<User> = this.corUserRepository.findAllByName("user2")

        println("## test-coroutine3 02, thread: $currentThreadName")

        val res: List<Customer> = users
            .onEach { doc ->
                println("## test-coroutine3 10, thread: $currentThreadName")
                println("## user $doc ( ${doc._id} / ${doc.name} )") }
            .map { user ->
                println("## test-coroutine3 11, thread: $currentThreadName")
                //dumpThreadNames()
                Customer(user.name) }

        println("## test-coroutine3 03, thread: $currentThreadName")

        //dumpThreadNames()

        return res
    }

    @GetMapping("/test-coroutine4")
    suspend fun testCoroutine4(): List<Customer> {

        println("## test-coroutine4 01, thread: $currentThreadName")

        val user: User? = this.corUserRepository.findUserByName("user2") // "user2_unknown")

        println("## test-coroutine4 02, thread: $currentThreadName")
        //dumpThreadNames()

        val res = if (user != null) listOf(Customer(user.name)) else emptyList()
        return res
    }

    @GetMapping("/test-coroutine5")
    suspend fun testCoroutine5(): List<Customer> {

        println("## test-coroutine5 01, thread: $currentThreadName")

        val user: User = this.corUserRepository.getByName("user2") //"user2_unknown")

        println("## test-coroutine5 02, thread: $currentThreadName")

        //corUserRepository.findOne(user._id)
        corUserRepository.findById(user._id)
            ?: throw IllegalStateException("findOne does not work.")

        println("## test-coroutine5 03, thread: $currentThreadName")
        //dumpThreadNames()

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
        val banner = Banner("banner1")
        delay(10)
        emit(banner)
        delay(10)
        emit(banner)
    }
}

private val currentThreadName: String get() = Thread.currentThread().name

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

private fun dumpThreadNames() {
    //val threads: Set<Thread> = Thread.getAllStackTraces().keys
    val threads: Collection<Thread> = ThreadUtils.getAllThreads()
    val asStr = threads
        .sortedBy { it.name }
        .joinToString("\n") { "  ${it.name}" }
    println("## All threads:\n$asStr\n")
}
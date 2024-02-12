package com.mvv.demo2

import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


data class Customer (
    val name: String,
)

data class Banner (
    val aa: String,
)


@RestController()
//@PathVariable("")
/*open*/ class MyController(
    private val mongoOperations: MongoOperations,
) {

    @GetMapping("/customers")
    suspend /*open*/ fun getCustomers(): List<Customer> {
        //...
        //TODO()
        return listOf(Customer("customer1"))
    }

    @GetMapping("/customers2")
    fun getCustomers2(): List<Customer> {

        val users: MongoCollection<Document> = mongoOperations.getCollection("users")

        if (mongoOperations is MongoTemplate) {
            val mongoDatabaseFactory = mongoOperations.mongoDatabaseFactory
            val db = mongoDatabaseFactory.mongoDatabase
            println("mongo db: $db")
        }

        //val userCount: Long = users.countDocuments()
        //println("user count $userCount")

        val allUsersIt1: FindIterable<Document> = users.find()
        val allUsers: List<Document> = allUsersIt1.asIterable().toList()

        // TODO()
        return listOf(Customer("all our users count ${allUsers.size}"))
    }

    @GetMapping("/customers3")
    fun getCustomers3(): String {

        val cls = loadClass1("org.springframework.data.domain.Unpaged")
        val instance = cls.getDeclaredConstructor(Sort::class.java)
            .also { it.trySetAccessible() }
            .newInstance(Sort.by("prop1"))
        requireNotNull(instance)

        return "OK"
    }

    @GetMapping("/customers2-suspended")
    /*suspend*/ /*open*/ fun getCustomers2_suspended(): List<Customer> {

        val users: MongoCollection<Document> = mongoOperations.getCollection("users")
        println("users collection ref is taken.")

        //val userCount: Long = users.countDocuments()
        //println("user count $userCount")

        val allUsersIt1: FindIterable<Document> = users.find()
        val allUsers: List<Document> = allUsersIt1.asIterable().toList()
        //allUsers.map { it.to }

        //...
        //TODO()
        return listOf(Customer("customer1"))
    }

    @GetMapping("/suspend")
    suspend fun suspendingEndpoint(): Banner {
        delay(10)
        return Banner("fuck")
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


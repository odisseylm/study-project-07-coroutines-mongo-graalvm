package com.mvv.demo2.app

import com.github.dockerjava.api.command.InspectContainerResponse
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName


data class MongoDBConnectSettings (
    val port: Int = 27017,
    val host: String = "localhost",
    val user: String,
    val psw: String,
    val db: String,
    val userAuthDb: String? = null, // specified db, current db or "admin"? (I don't know which approach is better)
)



enum class MongoDBStartMode { Standard, ReplicaSet, Sharding }

class MongoDBContainer2(private val mongoDbStartMode: MongoDBStartMode, dockerImageName: DockerImageName, private val connectSettings: MongoDBConnectSettings? = null) : MongoDBContainer(dockerImageName) {

    init {
        when (mongoDbStartMode) {
            MongoDBStartMode.Sharding -> this.withSharding()
            MongoDBStartMode.Standard -> {
                //this.setWaitStrategy(Wait.forLogMessage("(?i).*waiting for connections.*", 2))
                commandParts = arrayOf("mongod")
            }
            MongoDBStartMode.ReplicaSet -> { }
        }

    }

    override fun start() {
        super.start()
    }

    private fun getConnectionStringImpl(database: String?): String {

        val port = getMappedPort(27017)
        val host = "localhost"

        val userAndPswPart = if (connectSettings == null) ""
                             else "${connectSettings.user}:${connectSettings.psw}@"
        val authSourcePart = if (connectSettings == null) ""
                             else "?authSource=${connectSettings.userAuthDb}"
        val databasePart   = when {
            database != null -> database
            (connectSettings != null && connectSettings.db.isNotBlank()) -> connectSettings.db
            else -> ""
        }

        return "mongodb://$userAndPswPart$host:$port/$databasePart$authSourcePart"
    }

    override fun getConnectionString(): String  = getConnectionStringImpl(null)
    override fun getReplicaSetUrl(databaseName: String?): String  = getConnectionStringImpl(databaseName)

    override fun containerIsStarted(containerInfo: InspectContainerResponse?) {
        super.containerIsStarted(containerInfo)
    }

    override fun containerIsStarted(containerInfo: InspectContainerResponse, reused: Boolean) {
        when (mongoDbStartMode) {
            MongoDBStartMode.Standard   -> { }
            MongoDBStartMode.ReplicaSet -> super.containerIsStarted(containerInfo, reused)
            MongoDBStartMode.Sharding   -> super.containerIsStarted(containerInfo, reused)
        }
    }
}

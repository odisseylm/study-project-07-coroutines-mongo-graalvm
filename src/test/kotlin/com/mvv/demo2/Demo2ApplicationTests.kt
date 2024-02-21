package com.mvv.demo2

import com.mvv.demo2.app.TestDemo2Application
import com.mvv.tests.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource


//@Disabled
//@Conditional(SkipAotPhaseUnderIdeaCondition::class)
//@org.springframework.test.context.aot.DisabledInAotMode // T O D O: Use conditional to skip if current task is ProcessTestAot and it launched under Idea
@SpringBootTest(
	classes = [TestDemo2Application::class],
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
//@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application.properties", "classpath:application-test.properties"])
internal class Demo2ApplicationTests {

	//@Autowired
	//private lateinit var servletAppContext: ServletWebServerApplicationContext

	@LocalServerPort
	private var port: Int = -1

	private val username = "test"
	private val password = "test"
	private val credentials = Credentials(username, password)

	@Test
	@Order(Int.MIN_VALUE)
	fun contextLoads() = useAssertJSoftAssertions("contextLoads") {
		val resp = httpGetString("http://localhost:$port/", credentials)

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).containsIgnoringCase("welcome")
	}

	@Test
	fun testSync0() = testSync()

	@Test
	fun testSync() = useAssertJSoftAssertions("testSync") {
		//val port = servletAppContext.webServer.port
		println("port: $port")

		val url = "http://localhost:$port/users/test"

		//val resp0 = httpGetString("http://localhost:$port/test-temp2", credentials)
		//assertThat(resp0.status).isEqualTo(200)

		val resp1 = httpGetString_byStdUrl(url, credentials)
		println("1) result status: ${resp1.status}")
		println("1) result body: ${resp1.content}")


		val resp2 = httpGetString(url, credentials)
		println("2) result status: ${resp2.status}")
		println("2) result body: ${resp2.content}")

		assertThat(resp1.status).isEqualTo(200)
		assertThat(resp1.content).contains("all our users count 2")

		assertThat(resp2.status).isEqualTo(200)
		assertThat(resp2.content).contains("all our users count 2")
	}

	@Test
	fun testRx() = useAssertJSoftAssertions("testRx") {
		val resp = httpGetString("http://localhost:$port/users/test-rx", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user1"},{"name":"user2"}]""")
	}

	@Test
	fun testRx2() = useAssertJSoftAssertions("testRx") {
		val resp = httpGetString("http://localhost:$port/users/test-rx2", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user1"}]""")
	}

	@Test
	fun testCoroutine() = useAssertJSoftAssertions("testCoroutine") {
		val resp = httpGetString("http://localhost:$port/users/test-coroutine", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user1"},{"name":"user2"}]""")
	}

	@Test
	fun testCoroutine2() = useAssertJSoftAssertions("testCoroutine") {
		val resp = httpGetString("http://localhost:$port/users/test-coroutine2", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user2"}]""")
	}

	@Test
	fun testCoroutine3() = useAssertJSoftAssertions("testCoroutine") {
		val resp = httpGetString("http://localhost:$port/users/test-coroutine3", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user2"}]""")
	}

	@Test
	fun testCoroutine4() = useAssertJSoftAssertions("testCoroutine") {
		val resp = httpGetString("http://localhost:$port/users/test-coroutine4", credentials)
		println("result status: ${resp.status}")
		println("result body: ${resp.content}")

		assertThat(resp.status).isEqualTo(200)
		assertThat(resp.content).contains("""[{"name":"user2"}]""")
	}

	@Test
	fun someOtherTest2() {
		println("## Demo2ApplicationTests.someOtherTest2()")
	}

	@Test
	fun test_someOtherTest2() {
		println("## Demo2ApplicationTests.test_someOtherTest2()")
	}


	/*
	class MongoDbProps (
		val host: String,
		val port: Int,
		val db: String,
		val user: String,
		val password: String,
		val userDb: String,
	)

	@Test
	@Disabled
	fun aa() {
		if (true) throw Exception("Fuck!!! It is disabled!!!")

		val port = 27018
		val host = "localhost"
		val user = "db1user"
		val psw = "db1psw"
		val userAuthDb = "db1" // or "admin" (I don't know which approach is better)

		val db = "db1"


		val p = MongoDbProps(host, port, db, user, psw, userAuthDb)

		//System.setProperty("spring.data.mongodb.host", p.host)
		//System.setProperty("spring.data.mongodb.port", p.port.toString())
		//System.setProperty("spring.data.mongodb.database", p.db)
		//System.setProperty("spring.data.mongodb.username", p.user)
		//System.setProperty("spring.data.mongodb.password", p.password)

		//System.setProperty("spring.data.mongodb.uri",
		//	"mongodb://" + p.user + ":" + p.password + "@" + p.host + ":" + p.port + "/" + p.db)

		val app = SpringApplicationBuilder(Demo2Application::class.java)
			.properties(
				"spring.data.mongodb.host=${p.host}",
				"spring.data.mongodb.port=${p.port}",
				"spring.data.mongodb.database=${p.db}",
				"spring.data.mongodb.username=${p.user}",
				"spring.data.mongodb.password=${p.password}",

				"spring.docker.compose.enabled = true",
				"spring.security.user.name = test",
				"spring.security.user.password = test",
			)
			.build()

		app.run()

		//assertInsertSucceeds(app.context());
	}
	*/

}

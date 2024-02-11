package com.mvv.demo2

import com.mvv.demo2.app.TestDemo2Application
import com.mvv.tests.Credentials
import com.mvv.tests.httpGetString
import com.mvv.tests.httpGetString_byStdUrl
import com.mvv.tests.useAssertJSoftAssertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource


@SpringBootTest(
	classes = [TestDemo2Application::class],
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
)
//@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application.properties", "classpath:application-test.properties"])
internal class Demo2ApplicationTests {

	//@Autowired
	//private lateinit var servletAppContext: ServletWebServerApplicationContext

	@LocalServerPort
	private var port: Int = -1

	@Test
	fun contextLoads() {
		//val port = servletAppContext.webServer.port
		println("port: $port")

		val url = "http://localhost:$port/customers2"
		val username = "test"
		val password = "test"
		val credentials = Credentials(username, password)

		val resp1 = httpGetString_byStdUrl(url, credentials)
		println("1) result status: ${resp1.status}")
		println("1) result body: ${resp1.content}")


		val resp2 = httpGetString(url, credentials)
		println("2) result status: ${resp2.status}")
		println("2) result body: ${resp2.content}")

		//SoftAssertions().runTests {
		useAssertJSoftAssertions {

			assertThat(resp1.status).isEqualTo(200)
			assertThat(resp1.content).contains("all our users count 2")

			assertThat(resp2.status).isEqualTo(200)
			assertThat(resp2.content).contains("all our users count 2")
		}
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

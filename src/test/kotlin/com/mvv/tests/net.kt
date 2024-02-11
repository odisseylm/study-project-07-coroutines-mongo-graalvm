package com.mvv.tests

import java.io.IOException
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.nio.charset.Charset
import java.util.*


class HttpResponse<T> (
    val status: Int,
    val content: T,
)

data class Credentials (
    val user: String,
    val password: String,
)


fun httpGetString(url: String, credentials: Credentials? = null): HttpResponse<String> =
    httpGetString_byStdHttpClient(url, credentials)
        .let { HttpResponse(it.statusCode(), it.body()) }
//fun httpGetString(url: String, credentials: Credentials? = null): HttpResponse<String> = httpGetString_byStdUrl(url, credentials)


fun httpGetString_byStdUrl(url: String, credentials: Credentials? = null): HttpResponse<String> {

    val con = URL(url).openConnection() as HttpURLConnection

    if (credentials != null) {
        val userAndPsw = "${credentials.user}:${credentials.password}"
        val basicAuth = "Basic " + String(Base64.getEncoder().encode(userAndPsw.toByteArray()))
        con.setRequestProperty("Authorization", basicAuth)
    }

    // Setting 'accept' to have 401 error (instead of 302 with default URL 'accept')
    // application/json
    // application/vnd.api+json
    // application/json,text/*;q=0.99
    // application/json;q=0.9,text/plain
    con.setRequestProperty("Accept", "application/json")

    // does not make sense for REST testing
    con.instanceFollowRedirects = false

    return try {
        con.connect()

        val bytes = con.inputStream.readAllBytes()

        val charSet = if (con.contentEncoding.isNullOrBlank()) Charsets.UTF_8 else Charset.forName(con.contentEncoding)
        val s = String(bytes, charSet)

        HttpResponse(con.responseCode, s)
    }
    catch (ex: IOException) {
        HttpResponse(con.responseCode, "")
    }
}

fun httpGetString_byStdHttpClient(url: String, credentials: Credentials? = null): java.net.http.HttpResponse<String> {

    val b = HttpClient.newBuilder()

    if (credentials != null)
        b.authenticator(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication =
                PasswordAuthentication(credentials.user, credentials.password.toCharArray())
        })

    val httpClient = b.build()

    val req = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(url))
        .build()

    val response: java.net.http.HttpResponse<String> = httpClient.send(req, java.net.http.HttpResponse.BodyHandlers.ofString())
    return response
}

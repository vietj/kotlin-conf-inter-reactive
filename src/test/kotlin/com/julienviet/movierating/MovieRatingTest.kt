package com.julienviet.movierating

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.WebClientOptions
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MovieRatingTest {

  lateinit var vertx: Vertx
  lateinit var client: WebClient

  @Before
  fun setUp() {
    vertx = Vertx.vertx()
    client = WebClient.create(vertx, WebClientOptions(defaultPort = 8080))
    runBlocking(vertx.dispatcher()) {
      awaitResult<String> { vertx.deployVerticle(App(), it) }
    }
  }

  @After
  fun tearDown() {
    vertx.close()
  }

  @Test
  fun testGetMovie() {
    runBlocking(vertx.dispatcher()) {
      val result = awaitResult<HttpResponse<Buffer>> { client.get("/movie/starwars").send(it) }
      assertEquals(200, result.statusCode())
      assertEquals(result.bodyAsJsonObject(), json {
        obj(
          "id" to "starwars",
          "title" to "Star Wars"
        )
      })
    }
  }

  @Test
  fun testRateMovie() {
    runBlocking(vertx.dispatcher()) {
      val result = awaitResult<HttpResponse<Buffer>> { client
        .post("/rate/starwars")
        .setQueryParam("rating", "5")
        .send(it) }
      assertEquals(201, result.statusCode())
    }
  }

  @Test
  fun testRateNotFoundMovie() {
    runBlocking(vertx.dispatcher()) {
      val result = awaitResult<HttpResponse<Buffer>> { client
        .post("/rate/does-not-exists")
        .setQueryParam("rating", "5")
        .send(it) }
      assertEquals(404, result.statusCode())
    }
  }
}

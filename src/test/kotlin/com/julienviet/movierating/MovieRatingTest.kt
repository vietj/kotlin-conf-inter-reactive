package com.julienviet.movierating

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.web.client.WebClientOptions
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(VertxUnitRunner::class)
class MovieRatingTest {

  lateinit var vertx: Vertx
  lateinit var client: WebClient

  @Before
  fun setUp(ctx: TestContext) {
    vertx = Vertx.vertx()
    vertx.deployVerticle(App(), ctx.asyncAssertSuccess())
    client = WebClient.create(vertx, WebClientOptions(defaultPort = 8080))
  }

  @After
  fun tearDown(ctx: TestContext) {
    vertx.close(ctx.asyncAssertSuccess())
  }

  @Test
  fun testGetMovie(ctx: TestContext) {
    client.get("/movie/starwars")
      .send(ctx.asyncAssertSuccess {
        ctx.assertEquals(200, it.statusCode())
        ctx.assertEquals(json {
          obj(
            "id" to "starwars",
            "title" to "Star Wars"
          )
        }, it.bodyAsJsonObject())
      })
  }

  @Test
  fun testRateMovie() {

    var result: Any? = null

    client.post("/rate/starwars")
      .setQueryParam("rating", "5")
      .send { ar ->
        if (ar.succeeded()) {
          result = ar.result()
        } else {
          result = ar.cause()
        }
      }

    while (result == null) {
      Thread.sleep(10)
    }
    when (result) {
      is HttpResponse<*> -> assertEquals(201, (result as HttpResponse<*>).statusCode())
      is Throwable -> fail((result as Throwable).message)
      else -> throw AssertionError()
    }
  }

  @Test
  fun testRateNotFoundMovie(ctx: TestContext) {
    client.post("/rate/does-not-exists")
      .setQueryParam("rating", "5")
      .send(ctx.asyncAssertSuccess {
        ctx.assertEquals(404, it.statusCode())
      })
  }
}

package com.julienviet.movierating

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.web.client.WebClientOptions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
  fun testRateMovie(ctx: TestContext) {
    client.post("/rateMovie/starwars")
      .setQueryParam("rate", "5")
      .send(ctx.asyncAssertSuccess {
        ctx.assertEquals(201, it.statusCode())
      })
  }

  @Test
  fun testRateNotFoundMovie(ctx: TestContext) {
    client.post("/rateMovie/does-not-exists")
      .setQueryParam("rate", "5")
      .send(ctx.asyncAssertSuccess {
        ctx.assertEquals(404, it.statusCode())
      })
  }
}

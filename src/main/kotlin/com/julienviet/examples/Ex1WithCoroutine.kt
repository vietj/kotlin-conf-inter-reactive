package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.WebClientOptions
import kotlinx.coroutines.experimental.launch

fun main(args : Array<String>) {

  val vertx = Vertx.vertx()
  val client = WebClient.create(vertx, WebClientOptions(defaultPort = 8080))
  val post = client
    .get("/rating/starwars")
    .`as`(BodyCodec.jsonObject())

  launch(vertx.dispatcher()) {

    try {

      val response = awaitResult<HttpResponse<JsonObject>> { handler ->
        post.send(handler)
      }

      if (response.statusCode() == 200) {
        val json = response.body()
        println("Movie rated ${json.getInteger("rating")}")
      }
    } catch (e: Exception) {
      println("Could not connect to movie rating service: ${e.message}")
    }
  }
}

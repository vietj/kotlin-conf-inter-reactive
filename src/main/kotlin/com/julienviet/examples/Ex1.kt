package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.ext.web.client.WebClientOptions

fun main(args : Array<String>) {

  val vertx = Vertx.vertx()
  val client = WebClient.create(vertx, WebClientOptions(defaultPort = 8080))
  val post = client
    .get("/rating/starwars")
    .`as`(BodyCodec.jsonObject())

  post.send {
    if (it.succeeded()) {
      val response = it.result()
      if (response.statusCode() == 200) {
        val json = response.body()
        println("Movie rated ${json.getInteger("rating")}")
      }
    } else {
      println("Could not connect to movie rating service: ${it.cause().message}")
    }
  }
}

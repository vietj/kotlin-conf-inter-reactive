package com.julienviet.examples

import io.vertx.kotlin.ext.web.client.WebClientOptions
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.client.WebClient
import io.vertx.reactivex.ext.web.codec.BodyCodec

fun main(args : Array<String>) {

  val vertx = Vertx.vertx()
  val client = WebClient.create(vertx, WebClientOptions(defaultPort = 8080))
  val post = client
    .get("/rating/starwars")
    .`as`(BodyCodec.jsonObject())
    .rxSend()

  post.subscribe({ response ->
    if (response.statusCode() == 200) {
      val json = response.body()
      println("Movie rated ${json.getInteger("rating")}")
    }
  }, { cause ->
    println("Could not connect to movie rating service: ${cause.message}")
  })
}

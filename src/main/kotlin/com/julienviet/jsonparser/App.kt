package com.julienviet.jsonparser

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.launch

fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer().requestHandler({ request ->
    println("got reuest ${request.method()}")
    if (request.method() == HttpMethod.PUT) {
      launch(vertx.dispatcher()) {
        val ch = request.toChannel(vertx)
        val parser = CoroutineJsonParser({ event ->
          println("Got event $event")
        })
        parser.parse(ch.iterator())
      }
    } else {
      request.response().setStatusCode(500).end()
    }

  }).listen(8080)

}

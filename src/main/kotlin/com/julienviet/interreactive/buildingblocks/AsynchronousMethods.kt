package com.julienviet.interreactive.buildingblocks

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(args: Array<String>) {

  val vertx = Vertx.vertx()
  val server = vertx.createHttpServer().requestHandler { request ->
    request.response().end("Hello world")
  }
  launch(vertx.dispatcher()) {
    try {
      awaitResult<HttpServer> { server.listen(8080, it) }
      println("server started")
    } catch (e: Exception) {
      println("could not start server: ${e.message}")
    }
  }
}

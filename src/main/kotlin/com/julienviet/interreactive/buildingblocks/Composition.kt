package com.julienviet.interreactive.buildingblocks

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(args: Array<String>) {

  val vertx = Vertx.vertx()
  launch(vertx.dispatcher()) {
    val server1 = vertx.createHttpServer().requestHandler { request ->
      request.response().end("Hello world")
    }
    val f1 = Future.future<HttpServer> { server1.listen(8080, it) }
    val server2 = vertx.createHttpServer().requestHandler { request ->
      request.response().end("Hello world")
    }
    val f2 = Future.future<HttpServer> { server2.listen(8081, it) }
    try {
      CompositeFuture.all(f1, f2).await()
      println("servers started")
    } catch (e: Exception) {
      println("could not start servers: ${e.message}")
    }
  }
}

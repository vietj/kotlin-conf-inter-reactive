package com.julienviet.examples

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.WebClientOptions
import kotlinx.coroutines.experimental.launch

fun Ex2() {

  val vertx = Vertx.vertx()

  launch(vertx.dispatcher()) {

    try {

      val result1 = awaitResult<String> { handler ->
        handler.handle(Future.succeededFuture("OK"))
      }
      println("Result 1 $result1")

      val result2 = awaitResult<String> { handler ->
        handler.handle(Future.failedFuture("Ouch"))
      }
      println("Result 2 $result1")

    } catch (e: Exception) {
      println("Ouch ${e.message}")
    }
  }
}

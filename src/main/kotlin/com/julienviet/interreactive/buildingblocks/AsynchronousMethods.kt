package com.julienviet.interreactive.buildingblocks

import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.eventBus().consumer<String>("the-address") { msg ->
    msg.reply("pong")
  }

  launch(vertx.dispatcher()) {
    try {
      val reply = awaitResult<Message<String>> { it -> vertx.eventBus().send("the-address", "ping", it) }
      val pong = reply.body()
    } catch (e: Exception) {
    }
  }
}

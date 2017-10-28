package com.julienviet.interreactive.buildingblocks

import io.vertx.reactivex.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.rx2.await

fun Rx(args: Array<String>) {

  val vertx = Vertx.vertx()
  val bus = vertx.eventBus()

  bus.consumer<String>("the-address") { msg ->
    msg.reply("pong")
  }

  launch(vertx.delegate.dispatcher()) {

    try {
      val reply = bus
        .rxSend<String>("the-address", "ping")
        .await()
      val pong = reply.body()
      println("Got reply $pong")

    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

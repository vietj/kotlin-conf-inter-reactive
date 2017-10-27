package com.julienviet.interreactive.buildingblocks

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

fun main(args: Array<String>) {
  val vertx = Vertx.vertx()
  launch(vertx.dispatcher()) {
    println("Hello")
    awaitEvent<Long> {  }
    println("World")
  }
}

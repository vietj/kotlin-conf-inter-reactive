package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.ReadStream
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.channels.produce

// Channel example
fun ExWithChannel(vertx: Vertx, readStream: ReadStream<JsonObject>) {

  class User(json: JsonObject) {
    val age: Int = 0
  }

  val input = readStream.toChannel(vertx)

  fun getAdults() = produce<User> {
    for (json in input) {
      val user = User(json)
      if (user.age >= 18) {
        send(user)
      }
    }
  }
}

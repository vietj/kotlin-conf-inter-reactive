package com.julienviet.examples

import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.streams.ReadStream
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

// Channel example
fun Ex6WithRx(readStream: ReadStream<JsonObject>) {


  class User(json: JsonObject) {
    val age: Int = 0
  }

  fun getAdults() = readStream.toFlowable()
    .map { json -> User(json) }
    .filter { user -> user.age >= 18}
}

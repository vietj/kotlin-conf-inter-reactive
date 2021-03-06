package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.WriteStream
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.launch

// Channel example
fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer()
    .requestHandler { request ->

    val writeStream: WriteStream<Buffer> = request.response()
    val sendChannel = writeStream.toChannel(vertx)

    launch(vertx.dispatcher()) {

      sendChannel.send(Buffer.buffer("the-item"))

      request.response().end()
    }
  }.listen(8080)
}

package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

// Channel example
fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer().requestHandler { request ->

    val writeStream: WriteStream<Buffer> = request.response()

    val item = Buffer.buffer("the-item")

    fun sendItemAndClose() {
      writeStream.write(item)
      request.response().end()
    }

    if (!writeStream.writeQueueFull()) {
      sendItemAndClose()
    } else {
      writeStream.drainHandler {
        sendItemAndClose()
      }
    }

  }.listen(8080)
}

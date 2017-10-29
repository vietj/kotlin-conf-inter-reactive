package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

// Channel example
fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer().requestHandler { request ->

    val readStream: ReadStream<Buffer> = request
    val receiveChannel: ReceiveChannel<Buffer> = readStream.toChannel(vertx)

    launch(vertx.dispatcher()) {
      try {
        for (buffer in receiveChannel) {
          // Handle each buffer
        }
        request.response().end("OK")
      } catch (e: Exception) {
        request.response().setStatusCode(500).end("${e.message}")
      }
    }

  }.listen(8080)
}

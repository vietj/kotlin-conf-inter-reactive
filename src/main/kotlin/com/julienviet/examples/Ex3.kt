package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// Channel example
fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer().requestHandler { request ->

    val readStream: ReadStream<Buffer> = request

    readStream.handler { buffer ->
      // Handle each buffer
    }

    readStream.exceptionHandler { err ->
      request.response().setStatusCode(500).end("${err.message}")
    }

    readStream.endHandler {
      request.response().end("OK")
    }

  }.listen(8080)



}

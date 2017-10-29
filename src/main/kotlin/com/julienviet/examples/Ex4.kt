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

    fun writeItems(count: Int = 1) {
      if (count <= 10000) {
        writeStream.write(Buffer.buffer("Item-$count"))
        if (writeStream.writeQueueFull()) {
          writeStream.drainHandler { writeItems(count) }
        } else {
          writeItems(count + 1)
        }
      } else {
        request.response().end()
      }
    }

    writeItems()

  }.listen(8080)
}

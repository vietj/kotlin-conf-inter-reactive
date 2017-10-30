package com.julienviet.examples

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch

// Channel example
fun Ex5WithChannel(vertx: Vertx, readStream: ReadStream<Buffer>, writeStream: WriteStream<Buffer>) {

  val input: ReceiveChannel<Buffer> = readStream.toChannel(vertx)
  val output: SendChannel<Buffer> = writeStream.toChannel(vertx)

  launch(vertx.dispatcher()) {
    try {
      while (true) {
        val buffer = input.receiveOrNull()
        if (buffer == null) {
          break;
        }
        output.send(buffer);
      }
    } finally {
      output.close()
    }
  }
}

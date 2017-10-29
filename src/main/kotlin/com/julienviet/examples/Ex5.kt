package com.julienviet.examples

import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.experimental.channels.SendChannel
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// Channel example
fun main(args: Array<String>) {

  val input = ByteArrayInputStream(kotlin.ByteArray(10))
  val output = ByteArrayOutputStream()

  val buffer = ByteArray(256)
  while (true) {
    val amount = input.read(buffer)
    if (amount == -1) {
      break
    }
    output.write(buffer, 0, amount)
  }

  // Transfering data

  // Transforming data



}

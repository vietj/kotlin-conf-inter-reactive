package com.julienviet.interreactive

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import java.util.*

fun ReadStream<Buffer>.toCharStream() : ReadStream<Char> {
  val adapter = Adapter(this)
  handler { buffer ->
    val s = buffer.toString()
    for (i in 0 until s.length) {
      adapter.pending.addLast(s[i])
    }
    adapter.checkPending();
  }
  return adapter
}

private class Adapter(val stream: ReadStream<Buffer>) : ReadStream<Char> {

  var paused = false
  val pending = ArrayDeque<Char>()
  var handler: Handler<Char>? = null

  override fun pause(): ReadStream<Char> {
    if (!paused) {
      stream.pause()
      paused = true
    }
    return this
  }

  override fun resume(): ReadStream<Char> {
    if (paused) {
      paused = false
      stream.resume()
      checkPending()
    }
    return this
  }

  fun checkPending() {
    while (!paused) {
      val c = pending.poll() ?: break
      val h = handler
      if (h != null) {
        h.handle(c)
      }
    }
  }

  override fun handler(handler: Handler<Char>?): ReadStream<Char> {
    this.handler = handler
    return this
  }

  override fun exceptionHandler(handler: Handler<Throwable>?): ReadStream<Char> {
    stream.exceptionHandler(handler)
    return this
  }

  override fun endHandler(handler: Handler<Void>?): ReadStream<Char> {
    stream.endHandler(handler)
    return this;
  }
}

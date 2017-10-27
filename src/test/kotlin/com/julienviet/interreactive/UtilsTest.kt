package com.julienviet.interreactive

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class UtilsTest {

  @Test
  fun testAdapter() {
    val bufferStream = FakeReadStream()
    val charStream = bufferStream.toCharStream()
    val max = ArrayDeque<Char>()
    charStream.handler { c ->
      max.addLast(c)
      if (max.size >= 3) {
        charStream.pause()
      }
    }
    val buff = Buffer.buffer("hello")
    bufferStream.handler!!.handle(buff)
    assertEquals('h', max.poll())
    assertEquals('e', max.poll())
    assertEquals('l', max.poll())
    assertEquals(null, max.poll())
    charStream.resume()
    assertEquals('l', max.poll())
    assertEquals('o', max.poll())
    assertEquals(null, max.poll())
  }

  class FakeReadStream : ReadStream<Buffer> {

    var handler: Handler<Buffer>? = null
    var endHandler: Handler<Void>? = null
    var exceptionHandler: Handler<Throwable>? = null
    var paused = false

    override fun handler(handler: Handler<Buffer>?): ReadStream<Buffer> {
      this.handler = handler
      return this;
    }

    override fun pause(): ReadStream<Buffer> {
      paused = true
      return this;
    }

    override fun resume(): ReadStream<Buffer> {
      paused = false
      return this;
    }

    override fun endHandler(handler: Handler<Void>?): ReadStream<Buffer> {
      endHandler = handler
      return this;
    }

    override fun exceptionHandler(handler: Handler<Throwable>?): ReadStream<Buffer> {
      exceptionHandler = handler
      return this;
    }
  }


}

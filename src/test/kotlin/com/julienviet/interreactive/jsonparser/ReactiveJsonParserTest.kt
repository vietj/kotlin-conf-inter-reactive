package com.julienviet.interreactive.jsonparser

import com.julienviet.interreactive.toCharStream
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.experimental.launch
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.experimental.suspendCoroutine

@RunWith(VertxUnitRunner::class)
class ReactiveJsonParserTest {

  @Test
  fun testFlowControl(ctx: TestContext) {
    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer().requestHandler { request ->
      val channel = request.toCharStream().toChannel(vertx, 1000)
      val stopped = AtomicBoolean()
      val handler : suspend (JsonEvent) -> Unit = { event ->
        if (stopped.compareAndSet(false, true)) {
          suspendCoroutine<JsonEvent> { coroutine ->
            println("Suspending coroutine")
            vertx.setTimer(10000) {
              println("Resuming coroutine")
              coroutine.resume(event)
            }
          }
        }
      }
      println("Starting coroutine")
      launch(vertx.dispatcher()) {
        CoroutineJsonParser(channel.iterator(), handler).parse()
      }
    }
    val latch = ctx.async()
    server.listen(8080, ctx.asyncAssertSuccess { latch.complete() })
    latch.await()
    val client = vertx.createHttpClient()
    val put = client.put(8080, "localhost", "/") { resp ->
    }
    put.setChunked(true)
    put.write("{")
    var count = 0
    var full = false
    val done = ctx.async()
    while (true) {
      if (put.writeQueueFull()) {
        if (!full) {
          full = true
          println("Send buffer is full")
        }
      } else {
        if (full) {
          full = false
          println("Send buffer is drained")
          done.complete()
          break
        }
        for (i in 1..4) {
          put.write("\"member-$count\":0123456789,")
          count++
        }
      }
      Thread.sleep(1)
    }
  }
}

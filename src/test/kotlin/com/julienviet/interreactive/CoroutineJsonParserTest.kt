package com.julienviet.interreactive

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.test.fail

class CoroutineJsonParserTest {

  @Test
  fun testParseCoroutine() {
    assertParse("null")
    failParse("nul")
    assertParse("1234")
    assertParse("{}")
    assertParse("""{"foo":1234}""")
    assertParse("""{"foo":1234,"bar":null}""")
  }

  fun assertParse(s: String) {
    runBlocking {
      val channel = Channel<Char>(1000)
      for (c in s) {
        channel.send(c);
      }
      channel.close()
      CoroutineJsonParser(channel.iterator()).parse()
    }
  }

  fun failParse(s: String) {
    runBlocking {
      val channel = Channel<Char>(1000)
      for (c in s) {
        channel.send(c);
      }
      channel.close()
      try {
        CoroutineJsonParser(channel.iterator()).parse()
        fail();
      } catch (e: IllegalStateException) {
      }
    }
  }

  @Test
  fun testBackPressure() {
    val latch = CountDownLatch(1)
    val channel = Channel<Char>(256)
    launch {
      val handler : suspend (JsonEvent) -> Unit = {
        println("called with " + it)
        suspendCoroutine<JsonEvent> {
          // resume later
        }
      }
      CoroutineJsonParser(channel.iterator(), handler).parse()
    }
    channel.offer('{')
    channel.offer('}')
    channel.close()

    latch.await()
  }

}

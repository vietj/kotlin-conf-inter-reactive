package com.julienviet.interreactive

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import kotlin.test.fail

class CoroutineJsonParserTest {

  @Test
  fun testParseCoroutine() {
    assertCoroutineParse("null")
    failCoroutineParse("nul")
    assertCoroutineParse("{}")
    assertCoroutineParse("""{"foo":null}""")
  }

  fun assertCoroutineParse(s: String) {
    runBlocking {
      val channel = Channel<Char>(1000)
      for (c in s) {
        channel.send(c);
      }
      channel.close()
      CoroutineJsonParser(channel.iterator()).parse()
    }
  }

  fun failCoroutineParse(s: String) {
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

}

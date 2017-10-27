package com.julienviet.interreactive.jsonparser

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.fail
import org.junit.Test

class CoroutineJsonParserTest {

  @Test
  fun testParseCoroutine() {
    assertParse("null")
    failParse("nul")
    assertParse("1234")
    assertParse("{}")
    assertParse("""{"foo":1234}""")
    assertParse("""{"foo":1234,"bar":null}""")
    assertParse("""{"foo":1234}{"foo":4321}""")
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
}

package com.julienviet.jsonparser

import com.julienviet.interreactive.splitToBuffers
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class CoroutineBufferedJsonParserTest {

  @Test
  fun testParse() {

    val partial: List<List<Any?>> = listOf(
      listOf(null),
      listOf("the-string"),
      listOf(true),
      listOf(false),
      listOf(JsonObject()),
      listOf(JsonObject().put("foo", 1234)),
      listOf(JsonObject().put("foo", 1234).putNull("bar")),
      listOf(JsonObject().put("foo", 1234), JsonObject().put("foo", 4321)),
      listOf(JsonArray()),
      listOf(JsonArray().add("foo").add(1234)/*.addNull()*/),
      listOf(JsonArray().add("foo"), JsonArray().add(1234)),
      listOf(JsonObject().put("1", JsonObject().put("2", JsonObject().put("3", 1234))).put("2", JsonArray().add("abc").add(JsonArray().add(1).add(2))))
    )

    for (test in partial) {
      generator(toJSON(test)).forEach {
        assertParse(test, it)
      }
    }

    generator(toJSON(listOf(1234))).forEach {
      assertParse(listOf(1234), it, false)
    }


    failParse("nul")
  }

  fun assertParse(expected: List<Any?>, s: String, partial: Boolean = true) {
    val builder = Builder()
    val parser = CoroutineJsonParser(builder::handle)
    val buffers = splitToBuffers(s)

    val ch = Channel<Buffer>(buffers.size)
    for (buffer in buffers) {
      ch.offer(buffer)
    }
    if (!partial) {
      ch.close()
    }
    launch {
      parser.parse(ch.iterator())
    }
    val time = System.currentTimeMillis()
    while (builder.result().size < expected.size) {
      assertTrue("Timeout parsing ${expected}", (System.currentTimeMillis() - time) < 1000)
      Thread.sleep(10)
    }
    assertEquals(expected, builder.result())
  }

  fun failParse(s: String) {
    try {
      CoroutineJsonParser().parseBlocking(Buffer.buffer(s))
      Assert.fail()
    } catch (e: IllegalStateException) {
    }
  }
}

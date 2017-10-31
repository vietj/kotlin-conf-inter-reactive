package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.splitToBuffers
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.junit.Assert
import org.junit.Test

class CoroutineBufferedJsonParserTest {

  @Test
  fun testParse() {
    val tests: List<List<Any?>> = listOf(
      listOf(null),
      listOf(1234),
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

    for (test in tests) {
      generator(toJSON(test)).forEach {
        Assert.assertEquals(test, assertParse(it))
      }
    }

    failParse("nul")
  }

  fun assertParse(s: String): List<Any?> {
    val builder = Builder()
    val parser = CoroutineJsonParser(builder::handle)
    val buffers = splitToBuffers(s)
    parser.parseBlocking(buffers)
    return builder.result()
  }

  fun failParse(s: String) {
    try {
      CoroutineJsonParser().parseBlocking(Buffer.buffer(s))
      Assert.fail()
    } catch (e: IllegalStateException) {
    }
  }
}

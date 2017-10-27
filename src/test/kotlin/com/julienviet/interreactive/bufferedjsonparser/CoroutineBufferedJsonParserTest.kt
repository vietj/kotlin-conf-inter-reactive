package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import com.julienviet.interreactive.toBufferIterator
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import org.junit.Assert
import org.junit.Test
import java.util.*

class CoroutineBufferedJsonParserTest {

  @Test
  fun testParse() {
    Assert.assertEquals(listOf(null), assertParse("null"))
    failParse("nul")
    Assert.assertEquals(listOf(1234), assertParse("1234"))
    Assert.assertEquals(listOf(JsonObject()), assertParse("{}"))
    Assert.assertEquals(listOf(JsonObject().put("foo", 1234)), assertParse("""{"foo":1234}"""))
    Assert.assertEquals(listOf(JsonObject().put("foo", 1234).putNull("bar")), assertParse("""{"foo":1234,"bar":null}"""))
    Assert.assertEquals(listOf(JsonObject().put("foo", 1234), JsonObject().put("foo", 4321)), assertParse("""{"foo":1234}{"foo":4321}"""))
  }

  fun assertParse(s: String): List<Any?> {
    val result = ArrayList<Any?>()
    val stack = Stack<JsonObject>()
    var name: String? = null
    CoroutineJsonParser({ event ->
      when (event) {
        is JsonEvent.Member -> name = event.name
        is JsonEvent.Value<*> -> {
          if (name == null) {
            result.add(event.value)
          } else {
            stack.peek().put(name, event.value)
          }
          name = null;
        }
        is JsonEvent.StartObject -> {
          val obj = JsonObject()
          if (name == null) {
            result.add(obj)
          } else {
            stack.peek().put(name, obj)
          }
          stack.add(obj)
          name = null
        }
        is JsonEvent.EndObject -> stack.pop()
      }
    }).parseBlocking(Buffer.buffer(s))
    return result
  }

  fun failParse(s: String) {
    try {
      CoroutineJsonParser().parseBlocking(Buffer.buffer(s))
      Assert.fail()
    } catch (e: IllegalStateException) {
    }
  }
}

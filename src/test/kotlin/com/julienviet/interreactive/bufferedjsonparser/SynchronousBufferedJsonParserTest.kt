package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import com.julienviet.interreactive.toBufferIterator
import io.vertx.core.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.*

class SynchronousBufferedJsonParserTest {

  @Test
  fun testParseSynchronous() {
    assertEquals(listOf(null), assertParse("null"))
    failParse("nul")
    assertEquals(listOf(1234), assertParse("1234"))
    assertEquals(listOf(JsonObject()), assertParse("{}"))
    assertEquals(listOf(JsonObject().put("foo", 1234)), assertParse("""{"foo":1234}"""))
    assertEquals(listOf(JsonObject().put("foo", 1234).putNull("bar")), assertParse("""{"foo":1234,"bar":null}"""))
    assertEquals(listOf(JsonObject().put("foo", 1234), JsonObject().put("foo", 4321)), assertParse("""{"foo":1234}{"foo":4321}"""))
  }

  fun assertParse(s: String): List<Any?> {
    val result = ArrayList<Any?>()
    val stack = Stack<JsonObject>()
    var name: String? = null
    SynchronousJsonParser({ event ->
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
    }).parse(toBufferIterator(s))
    return result
  }

  fun failParse(s: String) {
    try {
      SynchronousJsonParser().parse(toBufferIterator(s))
      fail()
    } catch (e: IllegalStateException) {
    }
  }
}
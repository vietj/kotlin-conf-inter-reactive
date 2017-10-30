package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import com.julienviet.interreactive.toBufferIterator
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

class SynchronousBufferedJsonParserTest {

  @Test
  fun testParseSynchronous() {

    val tests: List<List<Any?>> = listOf(
      listOf(null),
      listOf(1234),
      listOf("the-string"),
      listOf(true),
      listOf(false),
      listOf(JsonObject()),
      listOf(JsonObject().put("foo", 1234)),
      listOf(JsonObject().put("foo", 1234).putNull("bar")),
      listOf(JsonObject().put("foo", 1234), JsonObject().put("foo", 4321))
      // listOf(JsonArray())
    )

    for (test in tests) {
      generator(toJSON(test)).forEach {
        assertEquals(test, assertParse(it))
      }
    }

    failParse("nul")
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

fun generator(list: List<String>): List<String> {
  val ret = ArrayList<String>()
  for (index in 0..list.size) {
    val copy = LinkedList(list)
    copy.add(index, " ")
    val sb = StringBuilder()
    for (elt in copy) {
      sb.append(elt)
    }
    ret.add(sb.toString())
  }
  val sb = StringBuilder()
  for (elt in list) {
    sb.append(elt)
  }
  ret.add(sb.toString())
  return ret
}

fun toJSON(obj: Any?): List<String> {
  when (obj) {
    null -> return listOf("null")
    true -> return listOf("true")
    false -> return listOf("false")
    is List<*> -> {
      val list = ArrayList<String>()
      for (e in obj) {
        list.addAll(toJSON(e))
      }
      return list
    }
    is String -> return listOf("\"$obj\"")
    is Number -> return listOf(obj.toString())
    is JsonObject -> {
      val list = ArrayList<String>()
      list.add("{")
      obj.forEachIndexed { index, entry ->
        if (index > 0) {
          list.add(",")
        }
        list.add('"' + entry.key + '"')
        list.add(":")
        list.addAll(toJSON(entry.value))
      }
      list.add("}")
      return list
    }
    is JsonArray -> {
      val list = ArrayList<String>()
      list.add("[")
      obj.forEachIndexed { index, value ->
        if (index > 0) {
          list.add(",")
        }
        list.addAll(toJSON(value))
      }
      list.add("]")
      return list
    }
    else -> throw IllegalStateException()
  }
}


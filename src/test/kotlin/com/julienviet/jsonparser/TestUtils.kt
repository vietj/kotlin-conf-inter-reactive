package com.julienviet.jsonparser

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

class Builder {

  abstract class Ctx {
    abstract fun add(name: String?, value: Any?)
  }
  class ObjCtx(val obj: JsonObject): Ctx() {
    override fun add(name: String?, value: Any?) {
      obj.put(name, value)
    }
  }
  class ArrCtx(val arr: JsonArray): Ctx() {
    override fun add(name: String?, value: Any?) {
      if (value == null) {
        arr.addNull()
      } else {
        arr.add(value)
      }
    }
  }

  private val result = JsonArray()
  private val stack = Stack<Ctx>()
  private var name: String? = null

  init {
    stack.add(ArrCtx(result))
  }

  fun result(): List<Any?> = result.toList()

  fun handle(event: JsonEvent) {
    when (event) {
      is JsonEvent.Member -> name = event.name
      is JsonEvent.Value<*> -> {
        stack.peek().add(name, event.value)
        name = null;
      }
      is JsonEvent.StartObject -> {
        val obj = JsonObject()
        stack.peek().add(name, obj)
        stack.add(ObjCtx(obj))
        name = null
      }
      is JsonEvent.EndObject -> stack.pop()
      is JsonEvent.StartArray -> {
        val arr = JsonArray()
        stack.peek().add(name, arr)
        stack.add(ArrCtx(arr))
        name = null
      }
      is JsonEvent.EndArray -> stack.pop()
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

fun splitToBuffers(s: String): List<Buffer> {
  val list = ArrayList<Buffer>()
  for (index in 0 until s.length step 2) {
    list += Buffer.buffer(s.substring(index, Math.min(index + 2, s.length)))
  }
  return list
}


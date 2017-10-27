package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent

import io.vertx.core.buffer.Buffer

private val NO_CHAR = '\u0000'

class SynchronousJsonParser(val stream: Iterator<Buffer>, val handler : (JsonEvent) -> Unit = {}) {

  var c: Char = NO_CHAR
  var buffer: Buffer? = null
  var index = 0

  fun parse() {
    nextChar()
    while (c != NO_CHAR) {
      parseElement()
    }
  }

  fun parseElement() {
    when (c) {
      'n' -> parseNull()
      't' -> parseTrue()
      'f' -> parseFalse()
      '"' -> parseString()
      '[' -> parseArray()
      '{' -> parseObject()
      in '0'..'9' -> parseNumber()
      '-' -> parseNumber()
      else -> throw IllegalStateException("Unexpected char <$c>")
    }
  }

  fun parseNull() {
    nextChar('n')
    nextChar('u')
    nextChar('l')
    nextChar('l')
    handler(JsonEvent.Value<Unit>(null))
  }

  fun parseObject() {
    handler(JsonEvent.StartObject())
    nextChar('{')
    if (c == '}') {
      nextChar()
    } else {
      while (true) {
        nextChar('"')
        val acc = StringBuilder()
        while (c != '"') {
          acc.append(c)
          nextChar()
        }
        nextChar('"')
        nextChar(':')
        handler(JsonEvent.Member(acc.toString()))
        parseElement()
        if (c != ',') {
          break
        }
        nextChar()
      }
      nextChar('}')
    }
    handler(JsonEvent.EndObject())
  }

  fun parseTrue() {
  }

  fun parseFalse() {
  }

  fun parseString() {
  }

  fun parseArray() {
  }

  fun parseNumber() {
    val acc = StringBuilder()
    while (c in '0'..'9') {
      acc.append(c)
      nextChar()
    }
    handler(JsonEvent.Value(Integer.parseInt(acc.toString())))
  }

  fun nextChar(expected: Char? = null) {
    if (expected != null && c != expected) {
      throw IllegalStateException("Unexpected char $expected")
    }
    while (true) {
      val b = buffer
      if (b == null || index >= b.length()) {
        if (stream.hasNext()) {
          buffer = stream.next()
          index = 0
        } else {
          c = NO_CHAR
          break
        }
      } else {
        c = b.getByte(index++).toChar()
        break
      }
    }
  }
}

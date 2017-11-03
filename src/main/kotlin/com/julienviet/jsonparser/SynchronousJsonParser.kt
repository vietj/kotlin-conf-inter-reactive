package com.julienviet.jsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import io.vertx.core.buffer.Buffer
import java.util.*

class SynchronousJsonParser(val handler : (JsonEvent) -> Unit = {}) {

  var stream: Iterator<Buffer> = Collections.emptyIterator()
  var c: Char = NO_CHAR
  var buffer: Buffer? = null
  var index = 0

  fun parse(i: Iterator<Buffer>) {
    stream = i
    buffer = null
    nextChar()
    while (c != NO_CHAR) {
      skipWhitespace()
      parseElement()
      skipWhitespace()
    }
  }

  private fun parseElement() {
    when (c) {
      'n' -> parseNull()
      't' -> parseTrue()
      'f' -> parseFalse()
      '"' -> parseString()
      '[' -> parseArray()
      '{' -> parseObject()
      in '0'..'9' -> parseNumber()
      '-' -> parseNumber()
      else -> throw IllegalStateException("Unexpected char <${c.toInt()}>")
    }
  }

  private fun parseNull() {
    assertChar('n')
    nextChar()
    assertChar('u')
    nextChar()
    assertChar('l')
    nextChar()
    assertChar('l')
    handler(JsonEvent.Value<Unit>(null))
    nextChar()
  }

  private fun parseObject() {
    assertChar('{')
    handler(JsonEvent.StartObject())
    nextChar()
    skipWhitespace()
    if (c != '}') {
      while (true) {
        assertChar('"')
        nextChar()
        val acc = StringBuilder()
        while (c != '"') {
          acc.append(c)
          nextChar()
        }
        assertChar('"')
        nextChar()
        skipWhitespace()
        assertChar(':')
        handler(JsonEvent.Member(acc.toString()))
        nextChar()
        skipWhitespace()
        parseElement()
        skipWhitespace()
        if (c == '}') {
          break
        } else if (c == ',') {
          nextChar()
          skipWhitespace()
        } else {
          throw IllegalStateException()
        }
      }
    }
    handler(JsonEvent.EndObject())
    nextChar()
  }

  private fun parseArray() {
    assertChar('[')
    handler(JsonEvent.StartArray())
    nextChar()
    skipWhitespace()
    if (c != ']') {
      while (true) {
        parseElement()
        skipWhitespace()
        if (c == ']') {
          break
        } else if (c == ',') {
          nextChar()
          skipWhitespace()
        } else {
          throw IllegalStateException("Unexpected char ${c.toInt()}")
        }
      }
    }
    handler(JsonEvent.EndArray())
    nextChar()
    skipWhitespace()
  }

  private fun parseTrue() {
    assertChar('t')
    nextChar()
    assertChar('r')
    nextChar()
    assertChar('u')
    nextChar()
    assertChar('e')
    handler(JsonEvent.Value(true))
    nextChar()
  }

  private fun parseFalse() {
    assertChar('f')
    nextChar()
    assertChar('a')
    nextChar()
    assertChar('l')
    nextChar()
    assertChar('s')
    nextChar()
    assertChar('e')
    handler(JsonEvent.Value(false))
    nextChar()
  }

  private fun parseString() {
    assertChar('"')
    nextChar()
    val acc = StringBuilder()
    while (c != '"') {
      acc.append(c)
      nextChar()
    }
    assertChar('"')
    handler(JsonEvent.Value(acc.toString()))
    nextChar()
  }

  private fun parseNumber() {
    val acc = StringBuilder()
    while (c in '0'..'9') {
      acc.append(c)
      nextChar()
    }
    handler(JsonEvent.Value(Integer.parseInt(acc.toString())))
  }

  private fun assertChar(expected: Char? = null) {
    if (expected != null && c != expected) {
      throw IllegalStateException("Unexpected char ${c.toInt()}")
    }
  }

  private fun nextChar() {
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

  private fun skipWhitespace() {
    while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
      nextChar()
    }
  }
}

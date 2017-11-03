package com.julienviet.jsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.experimental.channels.ChannelIterator

class CoroutineJsonParser(val handler : suspend (JsonEvent) -> Unit = {}) {

  var stream: ChannelIterator<Buffer> = emptyChannelIterator()
  var c: Char = NO_CHAR
  var buffer: Buffer? = null
  var index = 0

  suspend fun parse(i: ChannelIterator<Buffer>) {
    stream = i
    buffer = null
    nextChar()
    while (c != NO_CHAR) {
      skipWhitespace()
      parseElement()
      skipWhitespace()
    }
  }

  private suspend fun parseElement() {
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

  private suspend fun parseNull() {
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

  private suspend fun parseObject() {
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

  private suspend fun parseArray() {
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

  private suspend fun parseTrue() {
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

  private suspend fun parseFalse() {
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

  private suspend fun parseString() {
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

  private suspend fun parseNumber() {
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

  private suspend fun nextChar() {
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

  private suspend fun skipWhitespace() {
    while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
      nextChar()
    }
  }
}

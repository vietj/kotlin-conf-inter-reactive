package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.experimental.channels.ChannelIterator

class CoroutineJsonParser(val handler : (JsonEvent) -> Unit = {}) {

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
    nextChar('n')
    nextChar('u')
    nextChar('l')
    nextChar('l')
    handler(JsonEvent.Value<Unit>(null))
  }

  private suspend fun parseObject() {
    handler(JsonEvent.StartObject())
    nextChar('{')
    skipWhitespace()
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
        skipWhitespace()
        nextChar(':')
        skipWhitespace()
        handler(JsonEvent.Member(acc.toString()))
        parseElement()
        skipWhitespace()
        if (c == '}') {
          nextChar()
          skipWhitespace()
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
  }

  private suspend fun parseArray() {
    handler(JsonEvent.StartArray())
    nextChar('[')
    skipWhitespace()
    if (c == ']') {
      nextChar()
    } else {
      while (true) {
        parseElement()
        skipWhitespace()
        if (c == ']') {
          nextChar()
          skipWhitespace()
          break
        } else if (c == ',') {
          nextChar()
          skipWhitespace()
        } else {
          throw IllegalStateException()
        }
      }
    }
    handler(JsonEvent.EndArray())
  }

  private suspend fun parseTrue() {
    nextChar('t')
    nextChar('r')
    nextChar('u')
    nextChar('e')
    handler(JsonEvent.Value(true))
  }

  private suspend fun parseFalse() {
    nextChar('f')
    nextChar('a')
    nextChar('l')
    nextChar('s')
    nextChar('e')
    handler(JsonEvent.Value(false))
  }

  private suspend fun parseString() {
    nextChar('"')
    val acc = StringBuilder()
    while (c != '"') {
      acc.append(c)
      nextChar()
    }
    nextChar('"')
    handler(JsonEvent.Value(acc.toString()))
  }

  private suspend fun parseNumber() {
    val acc = StringBuilder()
    while (c in '0'..'9') {
      acc.append(c)
      nextChar()
    }
    handler(JsonEvent.Value(Integer.parseInt(acc.toString())))
  }

  private suspend fun nextChar(expected: Char? = null) {
    if (expected != null && c != expected) {
      throw IllegalStateException("Unexpected char ${c.toInt()}")
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

  private suspend fun skipWhitespace() {
    while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
      nextChar()
    }
  }
}

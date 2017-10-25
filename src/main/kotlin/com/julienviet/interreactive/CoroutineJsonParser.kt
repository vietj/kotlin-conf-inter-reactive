package com.julienviet.interreactive

import kotlinx.coroutines.experimental.channels.ChannelIterator

class CoroutineJsonParser(val stream : ChannelIterator<Char>, val handler : suspend (JsonEvent) -> Unit = {}) {

  var c: Char? = null

  suspend fun parse() {
    nextChar()
    parseElement()
  }

  suspend fun parseElement() {
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

  suspend fun parseNull() {
    nextChar('n')
    nextChar('u')
    nextChar('l')
    nextChar('l')
    handler(JsonEvent.Value<Unit>(null))
  }

  suspend fun parseObject() {
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

  suspend fun parseNumber() {
    val acc = StringBuilder()
    while (c in '0'..'9') {
      acc.append(c)
      nextChar()
    }
    handler(JsonEvent.Value(Integer.parseInt(acc.toString())))
  }

  suspend fun nextChar(expected: Char? = null) {
    if (expected != null && c != expected) {
      throw IllegalStateException("Unexpected char $expected")
    }
    if (stream.hasNext()) {
      c = stream.next()
    } else {
      c = null
    }
  }
}

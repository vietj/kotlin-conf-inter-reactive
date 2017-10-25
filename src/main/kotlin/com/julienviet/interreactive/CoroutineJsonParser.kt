package com.julienviet.interreactive

import kotlinx.coroutines.experimental.channels.ChannelIterator

class CoroutineJsonParser(val stream : ChannelIterator<Char>) {

  var c: Char? = null

  suspend fun parse() {
    c = if (stream.hasNext()) stream.next() else null
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
      else -> throw IllegalStateException()
    }
  }

  suspend fun parseNull() {
    assertChar('n')
    assertChar('u')
    assertChar('l')
    assertChar('l')
  }

  suspend fun parseObject() {
    assertChar('{')
    if (c == '}') {
      assertChar('}')
    } else {
      assertChar('"')
      while (c != '"') {
        when (c) {
          in 'A'..'Z' -> assertChar()
          in 'a'..'z' -> assertChar()
          '"' -> {}
          else -> throw IllegalStateException()
        }
      }
      assertChar('"')
      assertChar(':')
      parseElement()
    }
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
  }

  suspend fun assertChar(expected: Char? = null) {
    if (expected != null && c != expected) {
      throw IllegalStateException()
    }
    if (stream.hasNext()) {
      c = stream.next()
    } else {
      c = null
    }
  }
}

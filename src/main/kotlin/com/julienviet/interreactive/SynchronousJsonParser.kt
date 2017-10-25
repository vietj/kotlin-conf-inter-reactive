package com.julienviet.interreactive

class SynchronousJsonParser(val stream: Iterator<Char>) {

  var c: Char? = null

  fun parse() {
    nextChar()
    parseElement()
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
      else -> throw IllegalStateException()
    }
  }

  fun parseNull() {
    nextChar('n')
    nextChar('u')
    nextChar('l')
    nextChar('l')
  }

  fun parseObject() {
    nextChar('{')
    if (c == '}') {
      nextChar()
    } else {
      while (true) {
        nextChar('"')
        while (c != '"') {
          nextChar()
        }
        nextChar('"')
        nextChar(':')
        parseElement()
        if (c != ',') {
          break
        }
        nextChar()
      }
      nextChar('}')
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
    while (c in '0'..'9') {
      nextChar()
    }
  }

  fun nextChar(expected: Char? = null) {
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

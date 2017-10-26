package com.julienviet.interreactive.jsonparser

class SynchronousJsonParser(val stream: Iterator<Char>, val handler : (JsonEvent) -> Unit = {}) {

  var c: Char? = null

  fun parse() {
    nextChar()
    while (c != null) {
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
    if (stream.hasNext()) {
      c = stream.next()
    } else {
      c = null
    }
  }
}

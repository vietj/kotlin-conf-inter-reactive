package com.julienviet.interreactive.bufferedjsonparser

import com.julienviet.interreactive.jsonparser.JsonEvent
import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.experimental.channels.ChannelIterator
import kotlinx.coroutines.experimental.runBlocking

private val NO_CHAR = '\u0000'

private val EMPTY_ITERATOR: ChannelIterator<Buffer> = object: ChannelIterator<Buffer> {
  suspend override fun hasNext(): Boolean {
    return false;
  }
  suspend override fun next(): Buffer {
    throw IllegalStateException()
  }
}

class CoroutineJsonParser(val handler : (JsonEvent) -> Unit = {}) {

  var stream: ChannelIterator<Buffer> = EMPTY_ITERATOR
  var c: Char = NO_CHAR
  var buffer: Buffer? = null
  var index = 0

  fun parseBlocking(b: Buffer) {
    runBlocking {
      parse(b)
    }
  }

  suspend fun parse(b: Buffer) {
    stream = EMPTY_ITERATOR
    buffer = b
    nextChar()
    while (c != NO_CHAR) {
      parseElement()
    }
  }

  suspend fun parse(i: ChannelIterator<Buffer>) {
    stream = i
    buffer = null
    nextChar()
    while (c != NO_CHAR) {
      parseElement()
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
      else -> throw IllegalStateException("Unexpected char <$c>")
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

  private fun parseTrue() {
  }

  private fun parseFalse() {
  }

  private fun parseString() {
  }

  private fun parseArray() {
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

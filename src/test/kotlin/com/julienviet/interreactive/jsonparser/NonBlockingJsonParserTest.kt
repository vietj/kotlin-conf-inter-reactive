package com.julienviet.interreactive.jsonparser

import com.julienviet.interreactive.jsonparser.NonBlockingJsonParser
import org.junit.Test
import kotlin.test.fail

class NonBlockingJsonParserTest {

  @Test
  fun testParseNonBlocking() {
    assertNonBlockingParse("null")
    failNonBlockingParse("nulp")
    assertNonBlockingParse("{}")
    assertNonBlockingParse("""{"foo":null}""")
    assertNonBlockingParse("""{"foo":null,"bar":null}""")
  }

  fun assertNonBlockingParse(s: String) {
    val parser = NonBlockingJsonParser({})
    for (c in s) {
      parser.handle(c)
    }
  }

  fun failNonBlockingParse(s: String) {
    val parser = NonBlockingJsonParser({})
    try {
      for (c in s) {
        parser.handle(c)
      }
      fail()
    } catch (e: IllegalStateException) {
    }
  }
}

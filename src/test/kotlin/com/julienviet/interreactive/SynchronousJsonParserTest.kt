package com.julienviet.interreactive

import org.junit.Test
import kotlin.test.fail

class SynchronousJsonParserTest {

  @Test
  fun testParseSynchronous() {
    assertParse("null")
    failParse("nul")
    assertParse("1234")
    assertParse("{}")
    assertParse("""{"foo":1234}""")
    assertParse("""{"foo":1234,"bar":null}""")
  }

  fun assertParse(s: String) {
    SynchronousJsonParser(s.iterator()).parse()
  }

  fun failParse(s: String) {
    try {
      SynchronousJsonParser(s.iterator()).parse()
      fail()
    } catch (e: IllegalStateException) {
    }
  }
}

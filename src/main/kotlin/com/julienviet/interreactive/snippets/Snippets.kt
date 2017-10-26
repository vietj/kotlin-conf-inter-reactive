package com.julienviet.interreactive.snippets

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.StringReader
import java.io.StringWriter

var isRunning = false
var bufferedReader = BufferedReader(StringReader("the-string"))
var bufferedWriter = BufferedWriter(StringWriter())

fun f() {

  while (isRunning) {
    val line = bufferedReader.readLine()
    when (line) {
      "ECHO" -> bufferedWriter.write(line)
      // ...
      // Other cases (...)
      // ...
      else -> bufferedWriter.write("Unknown command")
    }
  }

}

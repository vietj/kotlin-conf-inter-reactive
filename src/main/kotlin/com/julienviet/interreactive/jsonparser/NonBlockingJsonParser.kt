package com.julienviet.interreactive.jsonparser

import java.util.*

class NonBlockingJsonParser(val doneHandler: ()->Unit)  {

  enum class Status {
    READ_ANY,
    READ_NULL,
    READ_OBJECT_MEMBER,
    READ_OBJECT_VALUE
  }

  var status: Status = Status.READ_ANY
  var acc = StringBuilder()
  val stack = Stack<Status>()

  fun handle(c: Char) {
    when (status) {
      Status.READ_ANY -> {
        when (c) {
          'n' -> {
            status = Status.READ_NULL
            acc.append('n')
          }
          '{' -> status = Status.READ_OBJECT_MEMBER
          '}' -> status = stack.pop()
          ',' -> status = stack.pop()
          else -> throw IllegalStateException()
        }
      }
      Status.READ_NULL -> {
        acc.append(c)
        if (acc.length == 4) {
          if (acc.toString() == "null") {
            doneHandler()
            acc.setLength(0)
            status = Status.READ_ANY
          } else {
            throw IllegalStateException()
          }
        }
      }
      Status.READ_OBJECT_MEMBER -> {
        if (acc.isEmpty()) {
          when (c) {
            '"' -> acc.append(c)
            '}' -> status = Status.READ_ANY
            else -> throw IllegalStateException()
          }
        } else {
          if (c == ':') {
            val name = acc.toString()
            acc.setLength(0)
            status = Status.READ_ANY
            stack.push(Status.READ_OBJECT_MEMBER)
          } else {
            acc.append(c)
          }
        }
      }
      else -> throw IllegalStateException()
    }
  }
}

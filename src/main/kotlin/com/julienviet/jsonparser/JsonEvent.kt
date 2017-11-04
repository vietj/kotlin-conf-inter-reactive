package com.julienviet.jsonparser

sealed class JsonEvent {

  class StartObject : JsonEvent() {
    override fun toString() = "{"
  }
  class EndObject : JsonEvent() {
    override fun toString() = "}"
  }

  class StartArray : JsonEvent() {
    override fun toString() = "["
  }

  class EndArray : JsonEvent() {
    override fun toString() = "]"
  }

  class Value<T>(val value: T?) : JsonEvent() {
    override fun toString() = "$value"
  }

  class Member(val name: String) : JsonEvent() {
    override fun toString() = "\"" + name + "\":"
  }
}

package com.julienviet.interreactive.jsonparser

sealed class JsonEvent {

  class StartObject : JsonEvent() {}
  class EndObject : JsonEvent() {}

  class StartArray : JsonEvent() {}
  class EndArray : JsonEvent() {}

  class Value<T>(val value: T?) : JsonEvent() {
    override fun toString(): String {
      return "$value"
    }
  }

  class Member(val name: String) : JsonEvent() {
    override fun toString(): String {
      return "\"" + name + "\":"
    }
  }

}

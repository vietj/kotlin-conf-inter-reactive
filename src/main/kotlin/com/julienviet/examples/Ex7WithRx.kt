package com.julienviet.examples

import io.reactivex.Observable
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

// Channel example
fun Ex7WithRx(vertx: Vertx, map: Map<String, String>) {

  val consumer = createKafkaConsumer(vertx, map, String::class, JsonObject::class)
  val stream = consumer.toObservable()

  stream
    .map({ record -> record.value().getInteger("temperature") })
    .buffer(1, TimeUnit.SECONDS)
    .map({ list -> list.sum() })
    .subscribe({ temperature -> println("Current temperature is " + temperature) })
}

class KafkaConsumerRecord {
  fun value(): JsonObject {
    return JsonObject()
  }
}

class ToObs {
  fun toObservable(): Observable<KafkaConsumerRecord> {
    throw UnsupportedOperationException()
  }
}

fun createKafkaConsumer(vertx: Vertx, map: Map<String, String>, key: KClass<String>, value: KClass<JsonObject>): ToObs {
  return ToObs()
}


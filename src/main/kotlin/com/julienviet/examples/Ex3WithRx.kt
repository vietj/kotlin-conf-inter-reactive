package com.julienviet.examples

import io.reactivex.Observable
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.streams.ReadStream

// Channel example
fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  vertx.createHttpServer()
    .requestHandler { request ->

      val readStream: ReadStream<Buffer> = request
      val observable: Observable<Buffer> = readStream.toObservable()

      observable.subscribe(
        { buffer ->
          // Handle each buffer
        },
        { err ->
          request.response().setStatusCode(500).end("${err.message}")
        },
        {
          request.response().end("OK")
        })
    }
    .listen(8080)
}

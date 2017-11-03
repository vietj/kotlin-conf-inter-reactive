package com.julienviet.jsonparser.app

import com.julienviet.interreactive.jsonparser.JsonEvent
import com.julienviet.jsonparser.backup.CoroutineJsonParser
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import kotlinx.coroutines.experimental.channels.produce

import com.julienviet.utils.*
import io.vertx.kotlin.coroutines.toChannel

fun main(args: Array<String>) {

  val vertx = Vertx.vertx()

  val router = Router.router(vertx)

  router {

    put("/docs") { ctx ->

      val request = ctx.request()
      val bufferChannel = request.toChannel(vertx)

      val jsonChannel = produce<JsonEvent> {
        val p = CoroutineJsonParser({ event ->
          send(event)
        })
        try {
          p.parse(bufferChannel.iterator())
          close()
        } catch (e: Exception) {
          close(e)
        }
      }

      try {
        for (event in jsonChannel) {
          println("Got event $event")
        }
        request.response().end()
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }

  vertx.createHttpServer().requestHandler(router::accept).listen(8080)

}

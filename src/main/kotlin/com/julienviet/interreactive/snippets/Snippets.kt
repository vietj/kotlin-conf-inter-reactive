package com.julienviet.interreactive.snippets

import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.StringReader
import java.io.StringWriter
import io.vertx.kotlin.core.json.*

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

fun singleCallback(router: Router, client: SQLClient) {

  router.get("/movie/:id").handler { ctx ->

    val id = ctx.pathParam("id")
    val params = json { array(id) }
    client.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", params) {
      if (it.succeeded()) {
        val result = it.result()
        if (result.rows.size == 1) {
          ctx.response().end(json {
            obj("id" to id, "title" to result.rows[0]["TITLE"]).encode()
          })
        } else {
          ctx.response().setStatusCode(404).end()
        }
      } else {
        ctx.fail(it.cause())
      }
    }
  }
}

fun callbackHell(router: Router, client: SQLClient, ctx: RoutingContext) {
  val movie = ctx.pathParam("id")
  val rating = Integer.parseInt(ctx.queryParam("getRating")[0])
  client.getConnection {
    if (it.succeeded()) {
      val connection = it.result()
      val queryParams = json { array(movie) }
      connection.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", queryParams) {
        if (it.succeeded()) {
          val result = it.result()
          if (result.rows.size == 1) {
            val updateParams = json { array(rating, movie) }
            connection.updateWithParams("INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?", updateParams) {
              if (it.succeeded()) {
                ctx.response().setStatusCode(201).end()
              } else {
                connection.close()
                ctx.fail(it.cause())
              }
            }
          } else {
            connection.close()
            ctx.response().setStatusCode(404).end()
          }
        } else {
          connection.close()
          ctx.fail(it.cause())
        }
      }
    } else {
      ctx.fail(it.cause())
    }
  }
}

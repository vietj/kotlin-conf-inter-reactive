package com.julienviet.interreactive.snippets

import io.reactivex.Single
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.ext.sql.SQLClient
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext

fun singleCallback(router: Router, client: SQLClient) {

  router.get("/movie/:id").handler { ctx ->

    val id = ctx.pathParam("id")
    val query = "SELECT TITLE FROM MOVIE WHERE ID=?"
    val queryParams = json { array(id) }
    val single = client.rxQueryWithParams(query, queryParams)

    single.subscribe(
      { result ->
        if (result.rows.size == 1) {
          ctx.response().end(json {
            obj("id" to id, "title" to result.rows[0]["TITLE"]).encode()
          })
        } else {
          ctx.response().setStatusCode(404).end()
        }
      },
      { ctx.fail(it) })
  }

}

fun callbackHell(router: Router, client: SQLClient, ctx: RoutingContext) {

  val movie = ctx.pathParam("id")
  val rating = ctx.queryParam("getRating")[0]
  val query = "SELECT TITLE FROM MOVIE WHERE ID=?"
  val queryParams = json { array(movie) }
  val update = "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?"
  val updateParams = json { array(rating, movie) }

  val single = client.rxGetConnection().flatMap {
    connection ->
    connection
      .rxQueryWithParams(query, queryParams)
      .flatMap {
        result ->
        if (result.results.size == 1) {
          connection.rxUpdateWithParams(update, updateParams)
        } else {
          Single.error<UpdateResult>(NotFoundException())
        }
      }
      .doAfterTerminate { connection.close() }
  }

  single.subscribe(
    {
      ctx.response().setStatusCode(201).end()
    },
    {
      ctx.fail(it)
    }
  )
}

class NotFoundException : RuntimeException()

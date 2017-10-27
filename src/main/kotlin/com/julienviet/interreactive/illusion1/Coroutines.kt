package com.julienviet.interreactive.illusion1

import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

suspend fun getMovie(client: SQLClient, ctx: RoutingContext) {

  val id = ctx.pathParam("id")
  val query = "SELECT TITLE FROM MOVIE WHERE ID=?"
  val queryParams = json { array(id) }

  try {

    val result = awaitResult<ResultSet> { client.queryWithParams(query, queryParams, it) }

    if (result.rows.size == 1) {
      ctx.response().end(json {
        obj("id" to id, "title" to result.rows[0]["TITLE"]).encode()
      })
    } else {
      ctx.response().setStatusCode(404).end()
    }

  } catch (err: Exception) {
    ctx.fail(err)
  }

}

suspend fun rateMovie1(client: SQLClient, ctx: RoutingContext) {
  val movie = ctx.pathParam("id")
  val rating = Integer.parseInt(ctx.queryParam("getRating")[0])
  val query = "SELECT TITLE FROM MOVIE WHERE ID=?"
  val queryParams = json { array(movie) }
  val update = "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?"
  val updateParams = json { array(rating, movie) }

  val connection = awaitResult<SQLConnection> { client.getConnection(it) }

  try {
    val result = awaitResult<ResultSet> { connection.queryWithParams(query, queryParams, it) }
    if (result.rows.size == 1) {

      awaitResult<UpdateResult> { connection.updateWithParams(update, updateParams, it) }

      ctx.response().setStatusCode(200).end()
    } else {
      ctx.response().setStatusCode(404).end()
    }
  } catch (err: Exception) {
    ctx.fail(err)
  } finally {
    connection.close()
  }
}

suspend fun rateMovie2(client: SQLClient, ctx: RoutingContext) {
  val movie = ctx.pathParam("id")
  val rating = Integer.parseInt(ctx.queryParam("getRating")[0])
  val query = "SELECT TITLE FROM MOVIE WHERE ID=?"
  val queryParams = json { array(movie) }
  val update = "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?"
  val updateParams = json { array(rating, movie) }
  val connection = awaitResult<SQLConnection> { client.getConnection(it) }
  connection.use {
    val result = awaitResult<ResultSet> { connection.queryWithParams(query, queryParams, it) }
    if (result.rows.size == 1) {
      awaitResult<UpdateResult> { connection.updateWithParams(update, updateParams, it) }
      ctx.response().setStatusCode(200).end()
    } else {
      ctx.response().setStatusCode(404).end()
    }
  }
}

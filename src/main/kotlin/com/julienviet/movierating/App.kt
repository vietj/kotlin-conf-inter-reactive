package com.julienviet.movierating

import io.vertx.core.*
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import com.julienviet.utils.*

fun main(args : Array<String>) {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(App()) { ar ->
    if (ar.succeeded()) {
      println("Application started")
    } else {
      println("Could not start application")
      ar.cause().printStackTrace()
    }
  }
}

val DB_INIT = listOf(
  "CREATE TABLE MOVIE (ID VARCHAR(16) PRIMARY KEY, TITLE VARCHAR(256) NOT NULL)",
  "CREATE TABLE RATING (ID INTEGER IDENTITY PRIMARY KEY, value INTEGER, MOVIE_ID VARCHAR(16))",
  "INSERT INTO MOVIE (ID, TITLE) VALUES 'starwars', 'Star Wars'",
  "INSERT INTO MOVIE (ID, TITLE) VALUES 'indianajones', 'Indiana Jones'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 1, 'starwars'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 5, 'starwars'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 9, 'starwars'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 10, 'starwars'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 4, 'indianajones'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 7, 'indianajones'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 3, 'indianajones'",
  "INSERT INTO RATING (VALUE, MOVIE_ID) VALUES 9, 'indianajones'"
)

class App : AbstractVerticle() {

  private lateinit var client : JDBCClient

  override fun start(startFuture: Future<Void>) {

    client = JDBCClient.createNonShared(vertx, json {
      obj(
        "url" to "jdbc:hsqldb:mem:test?shutdown=true",
        "driver_class" to "org.hsqldb.jdbcDriver",
        "max_pool_size-loop" to 30
      )
    })

    // Build Vert.x Web router
    val router = Router.router(vertx)

    router {
      get("/movie/:id") {
        ctx -> getMovie(ctx)
      }
      post("/rate/:id") {
        ctx -> rateMovie(ctx)
      }
      get("/rating/:id") {
        ctx -> getRating(ctx)
      }
    }

    client.getConnection {
      if (it.succeeded()) {
        val connection = it.result()

        // Populate database
        val all = CompositeFuture.all(DB_INIT.map { statement -> Future.future<Void> { connection.execute(statement, it) } })
        all.setHandler {

          if (it.succeeded()) {

            // Start the server
            vertx.createHttpServer()
              .requestHandler(router::accept)
              .listen(config().getInteger("http.port", 8080)) {
                startFuture.handle(it.mapEmpty())
              }
          } else {
            startFuture.fail(it.cause())
          }
        }
      } else {
        startFuture.fail(it.cause())
      }
    }
  }

  override fun stop() {
    client.query("SHUTDOWN") {}
  }

  // Send info about a movie
  private fun getMovie(ctx: RoutingContext) {
    val id = ctx.pathParam("id")
    client.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", json { array(id) }) {
      ar ->
      if (ar.succeeded()) {
        val result = ar.result()
        if (result.rows.size == 1) {
          ctx.response().end(json {
            obj("id" to id, "title" to result.rows[0]["TITLE"]).encodePrettily()
          })
        } else {
          ctx.response().setStatusCode(404).end()
        }
      } else {
        ctx.fail(ar.cause())
      }
    }
  }

  // Rate a movie
  private fun rateMovie(ctx: RoutingContext) {
    val movie = ctx.pathParam("id")
    val rating = Integer.parseInt(ctx.queryParam("rating")[0])
    client.getConnection { ar1 ->
      if (ar1.succeeded()) {
        val connection = ar1.result()
        connection.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", json { array(movie) }) { ar2 ->
          if (ar2.succeeded()) {
            val result = ar2.result()
            if (result.rows.size == 1) {
              connection.updateWithParams("INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?", json { array(rating, movie) }) {
                ar3 ->
                connection.close()
                if (ar3.succeeded()) {
                  ctx.response().setStatusCode(201).end()
                } else {
                  ctx.fail(ar3.cause())
                }
              }
            } else {
              connection.close()
              ctx.response().setStatusCode(404).end()
            }
          } else {
            connection.close()
            ctx.fail(ar2.cause())
          }
        }
      } else {
        ctx.fail(ar1.cause())
      }
    }
  }

  // Get the current rating of a movie
  private fun getRating(ctx: RoutingContext) {
    val id = ctx.pathParam("id")
    client.queryWithParams("SELECT AVG(VALUE) AS VALUE FROM RATING WHERE MOVIE_ID=?", json { array(id) }) { ar ->
      if (ar.succeeded()) {
        val result = ar.result()
        ctx.response().end(json {
          obj("id" to id, "rating" to result.rows[0]["VALUE"]).encodePrettily()
        })
      } else {
        ctx.fail(ar.cause())
      }
    }
  }
}











// Divide an conquer approach
class RateMovie(
  val ctx: RoutingContext,
  val client: SQLClient,
  val movie: String,
  val rating: Int) {

  fun rate() {
    client.getConnection {
      if (it.succeeded()) {
        query(it.result())
      } else {
        ctx.fail(it.cause())
      }
    }
  }

  fun query(connection: SQLConnection) {
    val params = json { array(movie) }
    connection.queryWithParams("SELECT TITLE FROM MOVIE WHERE ID=?", params) {
      if (it.succeeded()) {
        val result = it.result()
        if (result.rows.size == 1) {
          update(connection)
        } else {
          connection.close()
          ctx.response().setStatusCode(404).end()
        }
      } else {
        connection.close()
        ctx.fail(it.cause())
      }
    }
  }

  fun update(connection: SQLConnection) {
    val params = json { array(rating, movie) }
    connection.updateWithParams("INSERT INTO RATING (VALUE, MOVIE_ID) VALUES ?, ?", params) {
      connection.close()
      if (it.succeeded()) {
        ctx.response().setStatusCode(201).end()
      } else {
        ctx.fail(it.cause())
      }
    }
  }
}



package com.julienviet.interreactive.illusion1

import io.vertx.reactivex.ext.sql.SQLClient

fun rxKotlin(client: SQLClient) {

  client.rxGetConnection().flatMap({ conn ->
    val query = conn.rxQuery("some-database-query")

    // Always close the connection
    query.doAfterTerminate { conn.close() }
  }).subscribe(
    { resultSet -> println("the result is ${resultSet}") },
    { cause -> cause.printStackTrace() }
  )

}

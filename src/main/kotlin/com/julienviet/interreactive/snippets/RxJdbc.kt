package com.julienviet.interreactive.snippets

import io.reactivex.Single
import io.vertx.reactivex.ext.sql.SQLClient

fun jdbcWithRx(client: SQLClient) {


  client.rxGetConnection().flatMap({ conn ->

    val resa = conn.rxUpdate("CREATE TABLE test(col VARCHAR(20))")
      .flatMap({ result -> conn.rxUpdate("INSERT INTO test (col) VALUES ('val1')") })
      .flatMap({ result -> conn.rxUpdate("INSERT INTO test (col) VALUES ('val2')") })
      .flatMap({ result -> conn.rxQuery("SELECT * FROM test") })
    resa.doAfterTerminate { conn.close() }

  }).subscribe(
    { resultSet -> println("Results ${resultSet.rows}") },
    { cause -> cause.printStackTrace() }
  )


}

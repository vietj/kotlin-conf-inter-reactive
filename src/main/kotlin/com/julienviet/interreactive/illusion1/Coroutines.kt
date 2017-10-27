package com.julienviet.interreactive.illusion1

import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.ext.sql.SQLConnection

suspend fun suspendingFunction(): String {
  return "abc"
}

suspend fun performQuery(client: SQLClient) {

  val conn = awaitResult<SQLConnection> { client.getConnection(it) }
  try {
    val result = awaitResult<ResultSet> { conn.query("some-database-query", it) }
    print("the result is $result")
  } finally {

    // Always close the connection
    conn.close()
  }
}

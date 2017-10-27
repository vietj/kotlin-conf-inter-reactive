package com.julienviet.interreactive.illusion1

import io.vertx.ext.sql.SQLClient

fun asynchronousClient(client: SQLClient) {

  client.getConnection({ continuation1 ->
    if (continuation1.succeeded()) {
      val conn = continuation1.result()
      conn.query("some-database-query", { continuation2 ->
        if (continuation2.succeeded()) {
          val result = continuation2.result()
          print("The result is $result")
        }

        // Always close the connection
        conn.close()
      })
    } else {

    }
  })


}

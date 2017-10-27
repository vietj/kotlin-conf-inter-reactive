package com.julienviet.interreactive.illusion1

fun synchronousClient() {

  class SQLConnection {
    fun query(sql: String): Object { throw UnsupportedOperationException() }
    fun close() {}
  }
  class SQLClient {
    fun getConnection(): SQLConnection { throw UnsupportedOperationException()  }
  }
  val client = SQLClient()

  val conn = client.getConnection()
  try {
    val result = conn.query("some-database-query")
    print("The result is $result")
  } finally {

    // Always close the connection
    conn.close()
  }


}


package com.julienviet.interreactive.snippets

import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Singles
import io.vertx.core.AsyncResult
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Handler
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.StringReader
import java.io.StringWriter

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

fun singleCallback() {

  asyncMethod(Handler { v ->
    if (v.succeeded()) {
      val result = v.result();
    } else {
      v.cause().printStackTrace()
    }
  })

  val future = Future.future<String> { fut -> asyncMethod(fut) }
  future.setHandler { v ->
    if (v.succeeded()) {
      val result = v.result();
    } else {
      v.cause().printStackTrace()
    }
  }

  val single = rxAsyncMethod()
  single.subscribe(
    { result -> },
    { cause -> cause.printStackTrace() }
  )
}

fun callbackHell() {

  asyncMethod(Handler { ar1 ->
    if (ar1.succeeded()) {
      val result1 = ar1.result()
      anotherAsyncMethod(result1, Handler { ar2 ->
        if (ar2.succeeded()) {
          val result2 = ar2.result()
        } else {
          ar2.cause().printStackTrace()
        }
      })
    } else {
      ar1.cause().printStackTrace()
    }
  })

  val future1 = Future.future<String> { fut -> asyncMethod(fut) }
  val future2 = future1.compose({ result1 -> Future.future<String> { fut -> anotherAsyncMethod(result1, fut) }})
  future2.setHandler { v ->
    if (v.succeeded()) {
      val result2 = future2.result();
    } else {
      v.cause().printStackTrace()
    }
  }

  val single2 = rxAsyncMethod().flatMap({ result2 -> rxAnotherAsyncMethod(result2) })
  single2.subscribe(
    { result2 -> },
    { cause -> cause.printStackTrace() }
  )
}

fun compoundCallbacks() {

  val f1 = Future.future<String> { fut -> asyncMethod(fut) }
  val f2 = Future.future<String> { fut -> anotherAsyncMethod(fut) }
  CompositeFuture.all(f1, f2).setHandler { v ->
    if (v.succeeded()) {
      val result = f1.result() + f2.result()
    } else {
      v.cause().printStackTrace()
    }
  }



  val s1 = rxAsyncMethod()
  val s2 = rxAnotherAsyncMethod()

  // RxKotlin SAM (famous issue) helper
  Singles.zip<String, String, String>(s1, s2, { r1, r2 ->  r1 + r2 })
    .subscribe(
      { result -> },
      { cause -> cause.printStackTrace() }
    )
}

fun asyncMethod(callback: Handler<AsyncResult<String>>) {

}

fun rxAsyncMethod(): Single<String> {
  throw UnsupportedOperationException()
}

fun anotherAsyncMethod(callback: Handler<AsyncResult<String>>) {
}

fun anotherAsyncMethod(param: String, callback: Handler<AsyncResult<String>>) {
}

fun rxAnotherAsyncMethod(): Single<String> {
  throw UnsupportedOperationException()
}

fun rxAnotherAsyncMethod(param: String): Single<String> {
  throw UnsupportedOperationException()
}

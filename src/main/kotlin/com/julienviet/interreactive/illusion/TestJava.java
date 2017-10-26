package com.julienviet.interreactive.illusion;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kotlin.coroutines.VertxCoroutineKt;
import kotlin.Unit;
import kotlin.coroutines.experimental.Continuation;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.experimental.*;

public class TestJava {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    CoroutineDispatcher dispatcher = VertxCoroutineKt.dispatcher(vertx);

    Job job = BuildersKt.launch(dispatcher, CoroutineStart.DEFAULT, (scope, continuation) -> {

      System.out.println(scope.isActive());

      VertxCoroutineKt.awaitEvent(handler -> {
        vertx.setTimer(1000, id -> {
          // handler.handle("the-result");
        });
        return null;
      }, new AbstractCoroutine<String>(scope.getCoroutineContext(), true) {
        @Override
        protected void afterCompletion(Object state, int mode) {
          String result = (String) state;
          System.out.println("Got result " + result);
        }
      });

      return 1;
    });

    System.out.println("job.isActive() = " + job.isActive());


  }
}

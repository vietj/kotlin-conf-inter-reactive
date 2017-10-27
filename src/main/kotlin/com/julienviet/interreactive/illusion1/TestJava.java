package com.julienviet.interreactive.illusion1;

import io.vertx.core.Vertx;
import io.vertx.kotlin.coroutines.VertxCoroutineKt;
import kotlin.coroutines.experimental.intrinsics.IntrinsicsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.experimental.*;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

public class TestJava {

  public static void main(String[] args) throws Exception {

    Field field = IntrinsicsKt.class.getDeclaredField("COROUTINE_SUSPENDED");
    field.setAccessible(true);
    Object COROUTINE_SUSPENDED = field.get(null);

    Vertx vertx = Vertx.vertx();

    CoroutineDispatcher dispatcher = VertxCoroutineKt.dispatcher(vertx);
    CountDownLatch latch = new CountDownLatch(1);

    Job job = BuildersKt.<String>launch(dispatcher, CoroutineStart.DEFAULT, (scope, continuation) -> {

      System.out.println(scope.isActive());

      VertxCoroutineKt.awaitEvent(handler -> {
        vertx.setTimer(1000, id -> {
          handler.handle("the-result");
        });
        return null;
      }, new AbstractCoroutine<String>(scope.getCoroutineContext(), true) {
        @Override
        protected void afterCompletion(Object state, int mode) {
          String result = (String) state;
          System.out.println("Got result " + result);
          continuation.resume(null);
          latch.countDown();
        }
      });

      return COROUTINE_SUSPENDED;
    });

    System.out.println("job.isActive() = " + job.isActive());

    System.out.println("awaiting");
    latch.await();
    System.out.println("awaited");
    System.out.println("job.isActive() = " + job.isActive());


  }
}

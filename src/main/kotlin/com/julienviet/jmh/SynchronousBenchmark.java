package com.julienviet.jmh;

import com.julienviet.interreactive.bufferedjsonparser.HelpersKt;
import com.julienviet.interreactive.bufferedjsonparser.SynchronousJsonParser;
import com.julienviet.interreactive.jsonparser.JsonEvent;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 2)
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1, jvmArgs = {
  "-XX:+UseBiasedLocking",
  "-XX:BiasedLockingStartupDelay=0",
  "-XX:+AggressiveOpts"
})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class SynchronousBenchmark {

  private Buffer buffer;
  private static Function1<JsonEvent, Unit> kotlinConsume = SynchronousBenchmark::consume;
  private static Method parse;

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static Unit consume(final JsonEvent event) {
    return null;
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static void consume(final Map obj) {
  }

  @Param({"10"})
  public int size;

  @Setup
  public void setup() {
    JsonObject obj = new JsonObject();
    for (int i = 0;i < size;i++) {
      obj.put("number" + i, i);
    }
    buffer = obj.toBuffer();
  }

  @Benchmark
  public void jsonParser() throws Exception {
    SynchronousJsonParser parser = new SynchronousJsonParser(kotlinConsume);
    HelpersKt.parse(parser, buffer);
  }

  @Benchmark
  public void jackson() throws Exception {
    consume(Json.decodeValue(buffer, Map.class));
  }
}

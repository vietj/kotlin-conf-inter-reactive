package com.julienviet.jsonparser.jmh;

import com.julienviet.jsonparser.backup.CoroutineJsonParser;
import com.julienviet.jsonparser.HelpersKt;
import com.julienviet.jsonparser.JsonEvent;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import kotlin.Unit;
import kotlin.coroutines.experimental.Continuation;
import kotlin.jvm.functions.Function2;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
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
public class EventDrivenBenchmark {

  private static Function2<JsonEvent, Continuation<? super Unit>, Object> kotlinConsume = EventDrivenBenchmark::consume;
  private static Handler<io.vertx.core.parsetools.JsonEvent> vertxConsume = EventDrivenBenchmark::consume;

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static Unit consume(final JsonEvent event, Continuation<? super Unit> cont) {
    return null;
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static Unit consume(final io.vertx.core.parsetools.JsonEvent event) {
    return null;
  }

  @Param({"1", "10"})
  public int slices;

  @Param({"10", "100", "1000"})
  public int size;

  private List<Buffer> buffers;

  @Setup
  public void setup() {
    JsonObject obj = new JsonObject();
    for (int i = 0;i < size;i++) {
      obj.put("number" + i, i);
    }
    Buffer buffer = obj.toBuffer();
    buffers = new ArrayList<>();
    for (int i = 0; i < slices; i++) {
      int from = (buffer.length() * i) / slices;
      int to = (buffer.length() * (i + 1)) / slices;
      buffers.add(buffer.slice(from, to));
    }
  }

  @Benchmark
  public void jsonParser() throws Exception {
    Function2<JsonEvent, Continuation<? super Unit>, Object> jsonEventContinuationObjectFunction2 = (a, b) -> {
      return null;
    };
    CoroutineJsonParser parser = new CoroutineJsonParser(jsonEventContinuationObjectFunction2);
    HelpersKt.parseBlocking(parser, buffers);
  }

  @Benchmark
  public void jackson() throws Exception {
    JsonParser parser = JsonParser.newParser();
    parser.handler(vertxConsume);
    for (Buffer buffer : buffers) {
      parser.handle(buffer);
    }
  }
}

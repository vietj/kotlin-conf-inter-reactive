package com.julienviet.jsonparser.jmh;

import com.julienviet.jsonparser.CoroutineJsonParser;
import com.julienviet.jsonparser.HelpersKt;
import com.julienviet.interreactive.jsonparser.JsonEvent;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
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

  private static Function1<JsonEvent, Unit> kotlinConsume = EventDrivenBenchmark::consume;
  private static Handler<io.vertx.core.parsetools.JsonEvent> vertxConsume = EventDrivenBenchmark::consume;

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static Unit consume(final JsonEvent event) {
    return null;
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  public static Unit consume(final io.vertx.core.parsetools.JsonEvent event) {
    return null;
  }

  @Param({"1", "10"})
  public int slices;

  @Param({"10"})
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
    CoroutineJsonParser parser = new CoroutineJsonParser(kotlinConsume);
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

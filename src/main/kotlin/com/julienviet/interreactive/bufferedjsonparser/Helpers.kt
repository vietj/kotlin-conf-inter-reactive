package com.julienviet.interreactive.bufferedjsonparser

import io.vertx.core.buffer.Buffer
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ChannelIterator
import kotlinx.coroutines.experimental.launch

val NO_CHAR = '\u0000'

private val EMPTY_ITERATOR: ChannelIterator<Buffer> = object: ChannelIterator<Buffer> {
  suspend override fun hasNext(): Boolean {
    return false;
  }
  suspend override fun next(): Buffer {
    throw IllegalStateException()
  }
}

fun emptyChannelIterator() = EMPTY_ITERATOR

fun CoroutineJsonParser.parseBlocking(buffer: Buffer) {
  val channel = Channel<Buffer>(1)
  if (!channel.offer(buffer)) {
    throw IllegalStateException()
  }
  channel.close()
  var err: Throwable? = null
  launch(Unconfined) {
    try {
      parse(channel.iterator())
    } catch (e: Exception) {
      err = e
    }
  }
  val cause = err
  if (cause != null) {
    throw cause
  }
}

fun SynchronousJsonParser.parse(b: Buffer) {
  parse(listOf(b).iterator())
}



fun CoroutineJsonParser.parseBlocking(buffers: List<Buffer>) {
  val channel = Channel<Buffer>()
  var err: Throwable? = null
  launch(Unconfined) {
    try {
      parse(channel.iterator())
    } catch (e: Exception) {
      err = e
    }
  }
  for (buffer in buffers) {
    if (!channel.offer(buffer)) {
      throw IllegalStateException()
    }
  }
  channel.close()
  val cause = err
  if (cause != null) {
    throw cause
  }
}

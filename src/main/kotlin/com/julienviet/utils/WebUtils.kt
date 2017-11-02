package com.julienviet.utils

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

/**
 * An extension method for simplifying coroutines usage with Vert.x Web routers
 */
fun Route.handler(fn : suspend (RoutingContext) -> Unit): Route {
  return handler { ctx ->
    launch(ctx.vertx().dispatcher()) {
      try {
        fn(ctx)
      } catch(e: Exception) {
        ctx.fail(e)
      }
    }
  }
}

fun Router.get(path: String, handler: suspend (RoutingContext) -> Unit): Route {
  return get(path).handler(handler)
}

fun Router.post(path: String, handler: suspend (RoutingContext) -> Unit): Route {
  return post(path).handler(handler)
}

fun Router.put(path: String, handler: suspend (RoutingContext) -> Unit): Route {
  return put(path).handler(handler)
}

operator fun Router.invoke(body: Router.() -> Unit) {
  body(this)
}

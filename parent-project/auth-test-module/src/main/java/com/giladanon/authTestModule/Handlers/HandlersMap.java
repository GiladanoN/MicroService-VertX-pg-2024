package com.giladanon.authTestModule.Handlers;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A "singlton-like" (single init/create) class managing the verious Handlers.
 */
public class HandlersMap {
  
  private static Map<String,Handler<RoutingContext>> handlers = new HashMap<>();

  public static Handler<RoutingContext> computeHandlerIfMissing(
    String handlerName, Supplier<Handler<RoutingContext>> hadnlerGenerator
  ) {

    // java 'map.compute' api constraint (workaround)
    Function<String, Handler<RoutingContext>> handlerGenWrapper =
      (ignoredInput) -> hadnlerGenerator.get();

    // use compute computeIfAbsent to return existing instance, or create a relevant one instead.
    var result = handlers.computeIfAbsent(handlerName, handlerGenWrapper);

    return result;

  }
}

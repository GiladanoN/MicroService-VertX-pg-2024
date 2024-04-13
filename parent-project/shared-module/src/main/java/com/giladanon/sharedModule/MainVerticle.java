package com.giladanon.sharedModule;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    System.out.println("Strated SharedVerticle");
    startPromise.complete();

  }

}

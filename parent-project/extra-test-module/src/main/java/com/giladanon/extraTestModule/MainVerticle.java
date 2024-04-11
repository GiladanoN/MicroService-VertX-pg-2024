package com.giladanon.extraTestModule;

import io.vertx.core.AbstractVerticle;
// import io.vertx.core.AsyncResult;
// import io.vertx.core.Future;
// import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // this.vertx.fileSystem()
    vertx.eventBus().consumer("ADD_ORDER", (Message<OrderToAdd> message) -> {
      System.out.println("ADD_ORDER consumer was called over the eventbus"); 
      OrderToAdd data = message.body();
      String asString = (data == null ? "NONE" : data.toString());
      System.out.println("data recieved: " + asString);
    })
    // .handler(null)
    .completionHandler( (v) -> {
        System.out.println("Registered consumer ADD_ORDER successfully = " + v.succeeded());
      }
    );

    // final Router router = Router.router(vertx);
    
    // ServerStarter starter =
    //   new ServerStarter(serverInstance, PORT_NUM).setStartedNotifyPromise(startPromise);

    // starter.StartListen(router);

    System.out.println("Strated OrderVerticle");
    startPromise.complete();

  }

}

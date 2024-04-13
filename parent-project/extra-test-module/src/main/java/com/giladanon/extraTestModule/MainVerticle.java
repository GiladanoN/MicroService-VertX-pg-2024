package com.giladanon.extraTestModule;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
// import io.vertx.core.AsyncResult;
// import io.vertx.core.Future;
// import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.giladanon.sharedModule.Common.Codecs.GenericJsonCodec;
import com.giladanon.sharedModule.Common.Exceptions.ValidationError;
import com.giladanon.sharedModule.Common.POJO.Order;
import com.giladanon.sharedModule.Common.POJO.OrderToAdd;
// import com.hazelcast.internal.json.Json;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // vertx.eventBus().registerDefaultCodec(OrderToAdd.class, new OrderToAddCodec());
    vertx.eventBus().registerDefaultCodec(
        OrderToAdd.class, new GenericJsonCodec<OrderToAdd>(OrderToAdd.class)
      );

    // this.vertx.fileSystem()
    vertx.eventBus().consumer("ADD_ORDER", (Message<OrderToAdd> message) ->
    {
      System.out.println("ADD_ORDER consumer was called over the eventbus"); 
      OrderToAdd data = extractData(message);

      OrderAdder adder = new OrderAdder(data, vertx.fileSystem());
      
      try {
        adder.validate();
        // adder.openOrCreateFile();
        // adder.readAndParseOrders();
        // adder.addOrderToList();
        // adder.dumpListBackToFile();
        // Path path = adder.getFullFilePath().toRealPath();
        // String pathStr = (path != null ? path.toString() : "UNCLEAR");
        // System.out.println("writing to file has completed! file: " + pathStr);
        adder.addOrderProcedure()
          .andThen(x ->
            System.out.println("Goodbye from last 'andThen' -- adder.addOrderProcedure()"));
        
        // System.out.println("Called adder.addOrderProcedure()");
      }
      catch (
        ValidationError |   // data recieved found to be invalid.
        IOException e      // something went wrong handling the filesystem (dir / file etc.)
      ) {
        System.out.println("Cannot complete ADD_ORDER request. returning early...");
        message.reply(false);  // TODO- add reply mechanism to requester
        return;        
      }
      
      // printPWD();  // Debug

      // Path path = adder.getFullFilePath();
      // String pathStr = (path != null ? path.toString() : "UNCLEAR");
      // System.out.println("writing to file handler is registered, file: " + pathStr);

    })
    // .handler(null)
    .completionHandler( (v) -> {
        System.out.println("Registered consumer ADD_ORDER successfully = " + v.succeeded());
        System.out.println(); // newline
      }
    );

    

    // final Router router = Router.router(vertx);
    
    // ServerStarter starter =
    //   new ServerStarter(serverInstance, PORT_NUM).setStartedNotifyPromise(startPromise);

    // starter.StartListen(router);

    System.out.println("Strated OrderVerticle");
    startPromise.complete();

  }

  private OrderToAdd extractData(Message<OrderToAdd> message) {
    OrderToAdd data = message.body();
    String asString = (data == null ? "NONE" : data.toString());
    System.out.println("data recieved: " + asString);
    return data;
  }

  private void printPWD() {
    try {
      // vertx.fileSystem().

    String currentPath = new java.io.File(".").getCanonicalPath();
    System.out.println("Current dir:" + currentPath);
   
    String currentDir = System.getProperty("user.dir");
    System.out.println("Current dir using System:" + currentDir);
    } catch (Exception e) {}
  }

}

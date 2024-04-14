package com.giladanon.extraTestModule;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

import java.io.IOException;

import com.giladanon.sharedModule.Common.Codecs.GenericJsonCodec;
import com.giladanon.sharedModule.Common.Exceptions.ConflictOnAdd;
import com.giladanon.sharedModule.Common.Exceptions.ValidationError;
import com.giladanon.sharedModule.Common.POJO.OrderList;
import com.giladanon.sharedModule.Common.POJO.OrderToAdd;
import com.giladanon.sharedModule.Common.POJO.ReplyEB;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // vertx.eventBus().registerDefaultCodec(OrderToAdd.class, new OrderToAddCodec());
    vertx.eventBus().registerDefaultCodec(
        OrderToAdd.class, new GenericJsonCodec<OrderToAdd>(OrderToAdd.class)
      );
    vertx.eventBus().registerDefaultCodec(
        ReplyEB.class, new GenericJsonCodec<ReplyEB>(ReplyEB.class)
      );

    // this.vertx.fileSystem()
    vertx.eventBus().consumer("ADD_ORDER", (Message<OrderToAdd> message) ->
    {
      System.out.println("ADD_ORDER consumer was called over the eventbus"); 
      OrderToAdd data = extractData(message);

      OrderAdder adder = new OrderAdder(data, vertx.fileSystem());
      
      try {
        adder.validate();
        Future<OrderList> f =
        adder.addOrderProcedure()
          .andThen(x ->
            System.out.println("Goodbye from last 'andThen' -- adder.addOrderProcedure()\n"))
          .onFailure(failure -> {
            System.out.println("ADD_ORDER encountered an error, reporting to requesting verticle.");
            replyWithError(message, failure);
          });
      }
      catch (
        ValidationError |   // data recieved found to be invalid.
        IOException e      // something went wrong handling the filesystem (dir / file etc.)
      ) {
        replyWithError(message, e);
        return;        
      }
      // catch (ValidationError e) {   // data recieved found to be invalid.
      //   replyWithError(message, e, 400);
      //   // message.
      // }
      // catch (IOException e) {       // something went wrong handling the filesystem (dir / file etc.)
      //   replyWithError(message, e, 500);
      // }
      
      // printPWD();  // Debug
    })
    // .handler(null)
    .completionHandler( (v) -> {
        System.out.println("Registered consumer ADD_ORDER successfully = " + v.succeeded());
        System.out.println(); // newline
      }
    );

    System.out.println("Strated OrderVerticle");
    startPromise.complete();

  }
  
  private void replyWithError(Message<OrderToAdd> message, Throwable t, int failCode) {
    System.out.println("Cannot complete ADD_ORDER request. returning...");
    System.out.println("Cause type: " + t.getClass().getName());
    message.reply(
      new ReplyEB<>().setCause(t).setStatusCode(failCode).setSuccess(false)
    );
  }
  private void replyWithError(Message<OrderToAdd> message, Throwable t) {
    replyWithError(message, t, mapThrowableToFailCode(t));
  }
  private int mapThrowableToFailCode(Throwable t) {
    if (t instanceof ValidationError) return 400;
    if (t instanceof IOException) return 500;
    if (t instanceof NullPointerException) return 500;
    if (t instanceof ConflictOnAdd) return 409;
    return 500;  // default
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

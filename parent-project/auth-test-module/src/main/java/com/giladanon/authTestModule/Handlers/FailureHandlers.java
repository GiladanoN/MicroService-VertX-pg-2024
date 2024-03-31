package com.giladanon.authTestModule.Handlers;

import io.vertx.ext.web.Router;

import com.giladanon.authTestModule.Server.ResponseSender;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class FailureHandlers {

  public static void registerFailureHandlers(final Router router) {

    if (false)
    router.errorHandler(401, context -> {

      System.out.println("Hello from errorHandler.401 !");

      Throwable t = context.get("Throwable");
      String errMsg = "errorHandler called with Unknown Error. (registerFailureHandlers:17)";

      if (t == null && context.failed()) {
        t = context.failure();
      }

      if (t != null) {
        errMsg = t.getMessage();
        System.out.println("ERROR :: " + errMsg);
        // e.printStackTrace();

        if (context.response().ended() == false) {
          ResponseSender.create(context)
            .setStatusCode(500).send("Internal server error.");
        }
        else {
          System.out.println(
            "WARN :: note that error occured but another handler already sent a response...");
        }
      }

      else {  // no error object found (throwable)
        System.out.println(
          "Strangly, there seems to be no error (" + context.failed() +
            "), but the errorHandler was still called...");
      }
      
      context.next();  // politely finish server handling
    });

    // based on: https://stackoverflow.com/a/54398973
    router.route("/*").failureHandler(failContext -> {
      System.out.println("Hello from failureHandler ('/*') !");
      Throwable failure = failContext.failure();
      String failMsg = (failure != null) ? failure.getMessage() : "Unknown failure on server occured";
      ResponseSender.create(failContext).send(failMsg);
      failContext.next();  // politely finish server handling
    });

  }

}

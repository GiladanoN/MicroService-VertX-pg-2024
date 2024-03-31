package com.giladanon.authTestModule.Handlers;

import java.util.stream.Collectors;

import com.giladanon.authTestModule.Server.ResponseSender;

import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

public class RequestHandlers {
  
  /**
   * Handler for incoming requests, should be registered as the *FIRST* route handler.
   * @return
   */
  public static Handler<RoutingContext> RequestIncoming() {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = context -> {
          
          try {
            HttpServerRequest request = context.request();
            
            if (request == null) {
              System.out.println("This is wierd - Router called without request present...");
              context.next(); return;
            }

            System.out.println("Incoming endpoint request - path: " + request.absoluteURI());
            System.out.println(" > Connection from: " + request.connection().remoteAddress());
            // System.out.println("Connection from: " + context.request().exceptionHandler()); // body read error handler

            String headersStr = request.headers().entries().stream()
                .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(" , "));
            if (headersStr.isEmpty()) headersStr = "[NONE]";
            System.out.println(" > Recieved Headers: " + headersStr);
            
            String cookiesStr = request.cookies().stream()
                .map(Cookie::encode).collect(Collectors.joining(" , "));
            if (cookiesStr.isEmpty()) cookiesStr = "[NONE]";
            System.out.println(" > Recieved Cookies: " + cookiesStr);
            
            System.out.println(" > QueryParams:" + request.query());
            
            User user = context.user();
            System.out.println(" > User Detected: " + (user == null ? "[NONE]" : user.subject()) );
            System.out.println(" > Protocol version: " + request.version().alpnName());
          }
          catch (Exception e) {
            System.out.println("Something went wrong during 'RequestHandlers.RequestIncoming()'.");
            System.out.println("ERROR :: " + e.getMessage());
          }
          finally {
            System.out.println(); // newline
          }

          context.next();
        };

        return newHandler;
      });
    
    return handler;
  }

  /**
   * Handler for BodyEnd (response complete) event, should be registered as the *LAST* route handler.
   * @return
   */
  public static Handler<RoutingContext> RequestEnded() {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = lastContext -> {
          lastContext.addBodyEndHandler(
            (voidArg) -> {
              printFinalInfo(lastContext);
              ensureRequestReallyEnded(lastContext);        
            });
          };

      return newHandler;
    });
    
    return handler;
  }

  public static Handler<RoutingContext> RequestEndedWithFailure() {
    return RequestEnded();
  }

  private static void printFinalInfo(RoutingContext context) {
    
    System.out.println("Last Handler triggered (request ended) - " + context.request().path());
    System.out.println(" > Total bytes written: " + context.response().bytesWritten());
    Session session = context.session();
    System.out.println(" > Context SessionID: " + (session != null ? session.id() : "NO SESSION PRESENT"));
    User user = context.user();
    System.out.println(" > Context User: " + (user != null ? user.subject() : "NO USER ASOC."));
    System.out.println(" > Response StatusCode: " + context.statusCode());
    System.out.println(" > Request failed? " + context.failed());
    Throwable t = context.failure();
    System.out.println(" > Related failure: " + (t != null ? t.getMessage() : "NO THROWABLE FOUND."));
    System.out.println("");  // newline(s)
    
  }

  private static void ensureRequestReallyEnded(RoutingContext context) {
    if (context.response().ended() == false) {
      System.out.println("The code might have an issue - The response isn't actually ended...");
      try { ResponseSender.create(context).send(null); }
      catch (Exception e) {
        System.out.println("Still could not end the response. ERROR :: " + e.getMessage());
      }
    }
  }

}

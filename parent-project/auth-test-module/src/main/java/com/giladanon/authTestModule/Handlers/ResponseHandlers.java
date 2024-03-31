package com.giladanon.authTestModule.Handlers;

import com.giladanon.authTestModule.Server.ResponseSender;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;

public class ResponseHandlers {
  
  public static Handler<RoutingContext> RootPath() {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = context -> {
          String body = "Main path - welcome to orderes app.\n" +
            "please use Login/Logout for authenticatication, (Basic-HTTP auth over POST, using session cookies)\n" +
            "then use GetOrders/AddOrder to manage your orderes. (GET/POST methods respectively)\n";
          
          ResponseSender.create(context).send(body);
          context.next();  // politely finish server handling
        };

        return newHandler;
      });
    
    return handler;
  }


  public static Handler<RoutingContext> LoginResponder(SessionHandler sessionHandler) {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = context ->
        {
          System.out.println("Hello from Login logic!");
          if (context.user() == null) {
            // should not generally be possible if userAuthHandler did it's job...
            context.fail(500, new Throwable("Cannot determine user auth"));
            return;
          }
  
          Session newSession = sessionHandler.newSession(context);
          // newSession.put("AsociatedUser", context.user());
          // instead see "__vertx.userHolder" key auto-populated in debug info
          
          boolean success = !newSession.isDestroyed();
          String body = new StringBuilder()
            .append("logged in? ").append(success)
            .append(" / timeout:").append(SessionHandlers.SessionTimeoutDESC)
            .append(" / sessionID: ").append(newSession.id())
            .toString();
          
          ResponseSender.create(context).send(body);
          context.next();  // politely finish server handling
        };

        return newHandler;
      });
    
    return handler;
  }

}

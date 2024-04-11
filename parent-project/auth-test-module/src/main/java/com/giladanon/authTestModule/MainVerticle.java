package com.giladanon.authTestModule;

import com.giladanon.authTestModule.Handlers.*;
import com.giladanon.authTestModule.Server.ResponseSender;
import com.giladanon.authTestModule.Server.ServerStarter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorizations;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;

public class MainVerticle extends AbstractVerticle {

  final static int PORT_NUM = 8888;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final Router router = Router.router(vertx);
    
    final AuthenticationHandler userAuthHandler = AuthHandlers.UserAuthHandler(vertx);
    final SessionHandler sessionHandler = SessionHandlers.GenerateSessionHandler(vertx);

    router.route().handler(RequestHandlers.RequestIncoming());  // prints debug request info
    // router.route().failureHandler(RequestHandlers.RequestEnded());

    // Entry point to the application, returnes the text message below.
    router.get("/").handler(ResponseHandlers.RootPath());

    router.get("/TestEB").handler(context -> {
      String uri = context.request().absoluteURI();
      System.out.println("Entering endpoint handler, requested: " + uri);
      
      Object message = new Object();
      
      // vertx.eventBus().send("ADD_ORDER", message);
      vertx.eventBus()
        .sender("ADD_ORDER")
        .write(message, res -> {
          System.out.println("Eventbus returned a result for ADD_ORDER, success=" + res.succeeded());
          if (res.failed())
            System.out.println("FAILURE :: " + res.cause().getMessage());
          context.response().end("wasOrderAdded="+res.succeeded());
        });

      System.out.println("Exiting endpoint: " + uri);
    });

    router.post("/Login")
      .handler(userAuthHandler) // auth the user cred's (via BasicHTTPAuth headers)
      .failureHandler(AuthHandlers.UserLoginFailureHandler()) // politely reject user if required
      .handler(sessionHandler)  // add session handler to use in the next handler
      .handler(ResponseHandlers.LoginResponder(sessionHandler));  // create the session & respond

    router.post("/Logout").handler(context -> {
      
      Session session = context.session();
      User user = context.user();

      if (session == null) {
        context.fail(400,
          new Throwable("Bad request: Cannot determine session info"));
        return;
      }
      if (user == null || session.isDestroyed()) {

        String body = "logged in? false / sessionID: " + session.id() +
        " / failure: user already logged out, or session already invalid.";

        ResponseSender.create(context)
          .setStatusCode(410) // gone (resource no longer available)
          .send(body);
  
        context.next();  // politely finish server handling
      }

      // logout the user by invalidating session.
      session.destroy();

      ResponseSender.create(context)
        .setStatusCode(200) // OK
        .send("logged in? false / successful logout: true");

      context.next();  // politely finish server handling
    });
    
    String routeGetOrderes = "/GetOrders";
    router.get(routeGetOrderes).handler(sessionHandler) // protected resource
      .handler(SessionHandlers.Validator(routeGetOrderes))
        .failureHandler(RequestHandlers.RequestEnded())
      .handler(sessionHandler)
      .handler(DebugHandlers.SessionUserInfo(routeGetOrderes))
      .handler(context -> {

        // TODO- replace with EventBus & additional verticle logic per requirement 

        // request.isExpectMultipart()
        // context.response().setChunked(false)

      String body = "protected path - blah blah 789 - id = " + context.session().id();

      ResponseSender.create(context)
        .setStatusCode(200) // OK
        .send(body);

      context.next();  // politely finish server handling
    });

    router.route().handler(RequestHandlers.RequestEnded()); // last handler

    FailureHandlers.registerFailureHandlers(router);
    router.route().failureHandler(RequestHandlers.RequestEnded());  // error handlers

    vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);
    
    HttpServer serverInstance = vertx.createHttpServer();

    ServerStarter starter =
      new ServerStarter(serverInstance, PORT_NUM).setStartedNotifyPromise(startPromise);

    starter.StartListen(router);

  }


}

package com.giladanon.authTestModule;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class zDeadCode {

  private Vertx vertx;
  
  private void hitCountService() {
    
    final Router router = Router.router(vertx);
    
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    router.route().handler(routingContext -> {

      Session session = routingContext.session();

      Integer cnt = session.get("hitcount");
      cnt = (cnt == null ? 0 : cnt) + 1;

      session.put("hitcount", cnt);

      routingContext.response().putHeader("content-type", "text/html")
                               .end("<html><body><h1>Hitcount: " + cnt + "</h1></body></html>");
    });

    vertx.createHttpServer().requestHandler(router).listen(MainVerticle.PORT_NUM);

  }

  private void startSimpleHelloServer(Promise<Void> startPromise, int serverPort) {

    Handler<HttpServerRequest> helloHandler = (req) -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    };

    startServer(startPromise, serverPort, helloHandler);

  }

  private HttpServer startServer(
    Promise<Void> startPromise, int serverPort, Handler<HttpServerRequest> requestsHandler)
  {
    Handler<AsyncResult<HttpServer>> handlerListenStarted = http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port " + serverPort);
      } else {
        startPromise.fail(http.cause());
      }
    };

    return (
      vertx.createHttpServer()
        .requestHandler(requestsHandler)
        .listen(serverPort, handlerListenStarted)
    );
  }

  private void safelyDestroySession(RoutingContext ctx) {
    if (ctx != null && ctx.session() != null) {
      ctx.session().destroy();
    }
  }

  
  //  // private void oldTryCode(Promise<Void> startPromise) {
  //
  //  //   final Router router = Router.router(vertx);
  //
  //  //   System.setProperty("io.vertx.web.router.setup.lenient", "true");
  //    
  //  //   // SessionStore store = LocalSessionStore.create(vertx);
  //  //   SessionStore store = CookieSessionStore.create(vertx, "my_apps_secret");
  //  //   SessionHandler sessionHandler = SessionHandler.create(store).setCookieSecureFlag(false);
  //
  //  //   // router.route().handler(BodyHandler.create());
  //
  //  //   // router.route().handler(sessionHandler);
  //  //   // all incoming connections will be routed through session handler.
  //
  //  //   ensureFileIsCreatedOrExists(Path.of(authFilePath));
  //
  //  //   AuthenticationProvider authProvider =
  //  //     PropertyFileAuthentication.create(vertx, authFilePath);
  //    
  //  //   AuthenticationHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
  //
  //  //   // basicAuthHandler.handle(null);
  //
  //  //   router.route("/private/*").handler(basicAuthHandler).handler(sessionHandler)
  //  //   .handler(context -> {
  //  //     HttpServerRequest request = context.request();
  //  //     System.out.println("ListOfCookies in request: [" +
  //  //         request.cookies().stream().map(c -> c.toString()).collect(Collectors.joining(", ")) + "]"
  //  //     );
  //  //     System.out.println("Detected user: " + context.user().subject());
  //  //     System.out.println("Session count before request: " + store.size());
  //
  //  //     Session session = store.createSession(1000 * 20);
  //  //     // Session session = sessionHandler.newSession(context);
  //  //     System.out.println("Session created successfuly! " + session);
  //
  //  //     String id = session.id();
  //  //     System.out.println("Session ID is fine! " + id);
  //      
  //  //     Cookie sesCookie = Cookie.cookie("SessionID", id);
  //  //     context.response().addCookie(sesCookie);
  //  //     System.out.println("Cookie added successfuly! " + sesCookie);
  //      
  //  //     try {
  //  //       HttpServerResponse response = context.response();
  //  //       System.out.println("Response is fine! " + response);
  //   
  //  //       response.end("your new SessionID is: " + id);
  //  //       System.out.println("Response 'send' is fine! " + response);
  //  //     }
  //  //     catch (Exception e) {
  //  //       System.out.println("ERROR :: " + e.getMessage());
  //  //       // e.printStackTrace();
  //  //     }
  //
  //  //     System.out.println("Done private endpoint logic!");
  //      
  //  //   });
  //
  //  //   router.get("/public/*").handler(context -> {
  //  //     System.out.println("Hello from public endpoint!");
  //  //     Cookie testCookie = Cookie.cookie("textCookieKey", "testCookieData123");
  //  //     System.out.println("TestCookie is fine!" + testCookie);
  //  //     HttpServerResponse response = context.response();
  //  //     System.out.println("Response is fine! " + response);
  //  //     response.addCookie(testCookie);
  //  //     System.out.println("Cookie was added!");
  //  //     response.putHeader("content-type", "text/plain");
  //  //     System.out.println("Header was added!");
  //  //     try {
  //  //       response.send("hello from public!");
  //  //       System.out.println("Response 'send' is fine!");
  //  //       // response.end();
  //  //       // System.out.println("Response 'end' is fine!");
  //
  //  //       response.endHandler(__ -> {
  //  //         response.send("hello from endHandler!");
  //  //         System.out.println(__);
  //  //         System.out.println(__.getClass());
  //  //         response.send("goodbye from endHandler!");
  //  //         return;
  //  //       });
  //
  //  //     }
  //  //     catch (Exception e) {
  //  //       System.out.println("ERROR :: " + e.getMessage());
  //  //       // e.printStackTrace();
  //  //     }
  //
  //      
  //  //   });
  //
  //  //   // router.e
  //
  //  //   // router.route("/public/*").handle(context -> {
  //  //   //   context.request()
  //  //   // });
  //
  //
  //
  //  //   router.route().handler(sessionHandler);
  //
  //
  //
  //  //   startServer(startPromise, PORT_NUM, router);
  //  //   // vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);
  //
  //
  //  //   // startSimpleHelloServer(startPromise, PORT_NUM);
  //  // }

}

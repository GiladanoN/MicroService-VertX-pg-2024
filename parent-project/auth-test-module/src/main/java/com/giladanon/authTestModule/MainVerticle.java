package com.giladanon.authTestModule;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
// import io.vertx.ext.auth.properties;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;

public class MainVerticle extends AbstractVerticle {

  private final int PORT_NUM = 8888;
  // private final Router router = null;// Router.router(vertx);
  final String authFilePath = "./authfile.txt";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final Router router = Router.router(vertx);
    // anotherTryV3();

    if (false) {
      return;
    }

    ensureFileIsCreatedOrExists(Path.of(authFilePath));
    AuthenticationProvider authProvider = PropertyFileAuthentication.create(vertx, authFilePath);

    AuthenticationHandler basicAuthHandler = BasicAuthHandler.create(authProvider);
    
    router.route("/protected/*").handler(basicAuthHandler);

    // Entry point to the application, this will render a custom template.
    router.get("/").handler(ctx -> {
      ctx.response()
        .putHeader("Content-Type", "text/html")
        .end("main path - blah blah 123");
    });

    // The protected resource
    router.get("/protected").handler(ctx -> {
      String unauthorizedCause = null;
      if (ctx.user() == null) {
        unauthorizedCause = "No user session detected";
      }
      else if (ctx.user().expired()) {
        unauthorizedCause = "User session has expired";
      }
      else if (ctx.session() == null) {
        unauthorizedCause = "Session info missing";
      }
      else if (ctx.session().isDestroyed()) {
        unauthorizedCause = "Session already marked as expired";
      }

      if (unauthorizedCause != null) {
        safelyDestroySession(ctx);
        try {
          ctx.put("Throwable", new Throwable(unauthorizedCause));
          String userInfo = "User detected: " + (
            ctx.user() == null ? "NONE" : ctx.user().subject()
            ) + "\n";
          ctx.response()
            .setStatusCode(403)
            .end(userInfo + "Unauthorized - " + unauthorizedCause + "\n"); // 403: Unauthorized
        }
        catch (Exception e) {
          System.out.println("ERROR :: " + e.getMessage());
          // e.printStackTrace();
        }
        return;  // gaurd clause
      }

      ctx.response()
        .putHeader("Content-Type", "text/html")
        .end("protected path - blah blah 789 - id = " + ctx.session().id());

    });

    router.errorHandler(403, context -> {
      Throwable t = context.get("Throwable");
      if (t == null) {
        System.out.println("errorHandler called with Unknown Error. (line 121)");
      }
      System.out.println("ERROR :: " + t.getMessage());
        // e.printStackTrace();
    });

    router.route().failureHandler(failContext -> {
      Throwable failure = failContext.failure();
      String failMsg = (failure != null) ? failure.getMessage() : "Unknown failure on server occured";
      failContext.response().end(failMsg);
    });

    vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);    


    ////////////////////////////////////////////////////////////////////////////////
    // START OVER !
    ////////////////////////////////////////////////////////////////////////////////

    if (true) {
      return;
    }

    oldTryCode(startPromise);

  }

  private void safelyDestroySession(RoutingContext ctx) {
    if (ctx != null && ctx.session() != null) {
      ctx.session().destroy();
    }
  }

  private void oldTryCode(Promise<Void> startPromise) {

    final Router router = Router.router(vertx);

    System.setProperty("io.vertx.web.router.setup.lenient", "true");
    
    // SessionStore store = LocalSessionStore.create(vertx);
    SessionStore store = CookieSessionStore.create(vertx, "my_apps_secret");
    SessionHandler sessionHandler = SessionHandler.create(store).setCookieSecureFlag(false);

    // router.route().handler(BodyHandler.create());

    // router.route().handler(sessionHandler);
    // all incoming connections will be routed through session handler.

    ensureFileIsCreatedOrExists(Path.of(authFilePath));

    AuthenticationProvider authProvider =
      PropertyFileAuthentication.create(vertx, authFilePath);
    
    AuthenticationHandler basicAuthHandler = BasicAuthHandler.create(authProvider);

    // basicAuthHandler.handle(null);

    router.route("/private/*").handler(basicAuthHandler).handler(sessionHandler)
    .handler(context -> {
      HttpServerRequest request = context.request();
      System.out.println("ListOfCookies in request: [" +
          request.cookies().stream().map(c -> c.toString()).collect(Collectors.joining(", ")) + "]"
      );
      System.out.println("Detected user: " + context.user().subject());
      System.out.println("Session count before request: " + store.size());

      Session session = store.createSession(1000 * 20);
      // Session session = sessionHandler.newSession(context);
      System.out.println("Session created successfuly! " + session);

      String id = session.id();
      System.out.println("Session ID is fine! " + id);
      
      Cookie sesCookie = Cookie.cookie("SessionID", id);
      context.response().addCookie(sesCookie);
      System.out.println("Cookie added successfuly! " + sesCookie);
      
      try {
        HttpServerResponse response = context.response();
        System.out.println("Response is fine! " + response);
   
        response.end("your new SessionID is: " + id);
        System.out.println("Response 'send' is fine! " + response);
      }
      catch (Exception e) {
        System.out.println("ERROR :: " + e.getMessage());
        // e.printStackTrace();
      }

      System.out.println("Done private endpoint logic!");
      
    });

    router.get("/public/*").handler(context -> {
      System.out.println("Hello from public endpoint!");
      Cookie testCookie = Cookie.cookie("textCookieKey", "testCookieData123");
      System.out.println("TestCookie is fine!" + testCookie);
      HttpServerResponse response = context.response();
      System.out.println("Response is fine! " + response);
      response.addCookie(testCookie);
      System.out.println("Cookie was added!");
      response.putHeader("content-type", "text/plain");
      System.out.println("Header was added!");
      try {
        response.send("hello from public!");
        System.out.println("Response 'send' is fine!");
        // response.end();
        // System.out.println("Response 'end' is fine!");

        response.endHandler(__ -> {
          response.send("hello from endHandler!");
          System.out.println(__);
          System.out.println(__.getClass());
          response.send("goodbye from endHandler!");
          return;
        });

      }
      catch (Exception e) {
        System.out.println("ERROR :: " + e.getMessage());
        // e.printStackTrace();
      }

      
    });

    // router.e

    // router.route("/public/*").handle(context -> {
    //   context.request()
    // });



    router.route().handler(sessionHandler);



    startServer(startPromise, PORT_NUM, router);
    // vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);


    // startSimpleHelloServer(startPromise, PORT_NUM);
  }

  private void anotherTryV3() {
    
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

    vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);

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

  private boolean ensureFileIsCreatedOrExists(Path requiredPath) {
    try {
      Path created = Files.createFile(requiredPath);
      System.out.println("INFO :: The file '" + created + "' was created successfuly.");
    } catch (IOException e) {
      if (e instanceof FileAlreadyExistsException) {
        System.out.println("INFO :: The file '" + requiredPath + "' seems to already exists.");
      }
      else {
        System.out.println(
          "ERROR :: The file creation failed (" + requiredPath + ") - existing start method early...");
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }

}

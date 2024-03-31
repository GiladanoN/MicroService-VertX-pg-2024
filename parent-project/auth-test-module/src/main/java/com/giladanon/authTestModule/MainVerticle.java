package com.giladanon.authTestModule;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
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
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.Authorizations;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthenticationHandler;
// import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
// import io.vertx.ext.auth.properties;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;

public class MainVerticle extends AbstractVerticle {

  private final int PORT_NUM = 8888;
  final String authFilePath = "./authfile.txt";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    final Router router = Router.router(vertx);
    
    // hitCountService();

    // boolean disableFromLine47 = true;
    // if (disableFromLine47) {
    //   return;
    // }

    AuthenticationHandler userAuthHandler = getUserAuthHandler();
    // AuthenticationHandler sessionAuthHandler = getSessionAuthHandler(userAuthHandler);

    SessionHandler sessionHandler = getSessionHandler();

    // Entry point to the application, returnes the text message below.
    router.get("/").handler(ctx -> {
      ctx.response()
        .putHeader("Content-Type", "text/html")
        .end("Main path - welcome to orderes app.\n" +
             "please use Login/Logout for authenticatication,\n" +
             "then use GetOrders/AddOrder to manage your orderes.\n");
    });

    // router.route("/protected/*").handler(basicAuthHandler);

    router.post("/Login").handler(userAuthHandler).handler(sessionHandler).handler(context -> {
      System.out.println("Hello from Login logic!");
      if (context.user() == null) {
        // should not generally be possible if userAuthHandler did it's job...
        context.fail(500, new Throwable("Cannot determine user auth"));
      }
      Session newSession = sessionHandler.newSession(context);
      newSession.put("AsociatedUser", context.user());

      // newSession.
      
      boolean success = !newSession.isDestroyed();
      context.response()
        .putHeader("Content-Type", "text/html")
        .end("logged in? " + success + " / " + newSession.id());
    });
    
    // router.get("/GetOrders").handler(sessionAuthHandler) // protected resource
    router.get("/GetOrders").handler(sessionHandler) // protected resource
      .handler(context -> {

      System.out.println("Hello from GetOrders logic!");

      Session session = context.session();
      System.out.println("context.session(): " + descSessionInline(session));
      printSessionDataMultiline(session);
      
      User user = context.user();
      System.out.println(descUserInline(user, "context.user()"));

      if (user == null) {
        user = session.get("asociatedUser");
        System.out.println(descUserInline(user, "session.get(\"asociatedUser\")"));
      }
            
      System.out.println(); // newline

      Throwable failure = null;

      if (session == null) {
        failure = new Throwable("Session is NULL, please login first.");
      }
      else if (session.isDestroyed()) {
        failure = new Throwable("Session is DESTROYED, please login first.");
      }

      // if (user == null) {
      //   failure = new Throwable("User is NULL (both), please login first.");
      // }
      // else if (user.expired()) {
      //   failure = new Throwable("User is EXPIRED, please login first.");
      // }
      // else {
      //   context.fail(403, new Throwable("Session invalid, please login first."));
      // }

      if (failure != null) {
        context.fail(403, failure);
        return;
      }

      context.response()
        .end("protected path - blah blah 789 - id = " + context.session().id());

    });

    registerFailureHandlers(router);

    vertx.createHttpServer().requestHandler(router).listen(PORT_NUM);    


    ////////////////////////////////////////////////////////////////////////////////
    // START OVER !
    ////////////////////////////////////////////////////////////////////////////////

    if (true) {
      return;
    }

    oldTryCode(startPromise);

  }

  private void printSessionDataMultiline(Session session) {
    if (session == null) {
      System.out.println("provided session IS-NULL !"); return;
    }
    if (session.data() == null) {
      System.out.println("session.data() IS-NULL !"); return;
    }

    System.out.println("DataMap in session (Size=" + session.data().size() + ") - " + session);
    session.data().forEach((key,obj) -> System.out.println(key+": "+obj));  
  }

  private String descSessionInline(Session ses) {
    if (ses == null) {
      return "SESSION-IS-NULL";
    }
    StringBuilder desc = new StringBuilder();
    desc.append("---- ");
    desc.append("SesID=").append(ses.id()).append(" / ");
    desc.append("IsEmpty=").append(ses.isEmpty()).append(" / ");
    desc.append("IsDestroyed=").append(ses.isDestroyed()).append(" / ");
    desc.append("IsRenewd=").append(ses.isRegenerated()).append(" / ");
    desc.append("OldID=").append(ses.oldId()).append(" / ");
    if (ses.data() != null)
    desc.append("DataSize=").append(ses.data().size()).append(" / ");
    // desc.append("DataKeys=").append(ses.data().keySet().stream()).append(" / ");
    desc.append("Value=").append(ses.value()).append(" -----.");

    return desc.toString();
  }

  private String descUserInline(User user, String objSrc) {

    StringBuilder userDesc = new StringBuilder();
    userDesc.append(objSrc).append(": ");

    if (user == null) {
      userDesc.append("USER-IS-NULL");
      return userDesc.toString();
    }
    
    JsonObject attributes = user.attributes();
    Authorizations authorizations = user.authorizations();

    userDesc.append("userSubject: ").append(user.subject()).append(" / ");
    userDesc.append("userExpired: ").append(user.expired()).append(" / ");

    userDesc.append("user.get(*): ");
    final List<String> expirationProps = List.of("exp", "iat", "nbf");
    descPropListInline(expirationProps, prop -> user.get(prop));

    userDesc.append("ATTRs: ");
    userDesc.append(attributes == null ? "NULL" : attributes.encode()).append(" / ");
    
    userDesc.append("AUTHs: ");
    userDesc.append(authorizations == null ? "NULL" :
      authorizations.getProviderIds().stream().collect(Collectors.joining(",")));
    
    return userDesc.toString();
  }

  public String descPropListInline(final List<String> propsList, Function<String,Object> mapper) {
    return (
      propsList.stream()
        .map(propName -> {
          StringBuilder propDesc = new StringBuilder();
          Object propValue = mapper.apply(propName); // user.get(propName);
          propDesc.append("(").append(propName).append(")=");
          propDesc.append(propValue == null ? "NULL" : propValue.toString());
          return propDesc;
        })
        .collect(Collectors.joining(" , "))
    );

    // userDesc.append("(exp)=").append(.toString()).append(" , ");
    // userDesc.append("(iat)=").append(user.get("iat").toString()).append(" , ");
    // userDesc.append("(nbf)=").append(user.get("nbf").toString()).append(" / ");

  }

  private void registerFailureHandlers(final Router router) {

    router.errorHandler(403, context -> {
      Throwable t = context.get("Throwable");
      if (t == null) {
        System.out.println("errorHandler called with Unknown Error. (line 121)");
      }
      System.out.println("ERROR :: " + t.getMessage());
        // e.printStackTrace();
    });

    // based on: https://stackoverflow.com/a/54398973
    router.route("/*").failureHandler(failContext -> {
      Throwable failure = failContext.failure();
      String failMsg = (failure != null) ? failure.getMessage() : "Unknown failure on server occured";
      failContext.response().end(failMsg);
    });

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

  private AuthenticationHandler getUserAuthHandler() {

    ensureFileIsCreatedOrExists(Path.of(authFilePath));
    AuthenticationProvider authProvider = PropertyFileAuthentication.create(vertx, authFilePath);

    AuthenticationHandler basicAuthHandler = BasicAuthHandler.create(authProvider);

    return basicAuthHandler;
  }

  private AuthenticationHandler getSessionAuthHandler(AuthenticationHandler ...prerequisites) {
    
    ChainAuthHandler chainAuthHandler = ChainAuthHandler.all();
    
    for (var prereq : prerequisites) {
      chainAuthHandler.add(prereq);
    }
    
    chainAuthHandler.add(ctx -> {
      
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

    });

    return chainAuthHandler;
    
  }

  private SessionHandler getSessionHandler() {

    // TODO- check if still required for local
    System.setProperty("io.vertx.web.router.setup.lenient", "true");
    
    SessionStore store = LocalSessionStore.create(vertx);
    
    // SessionStore store1 = CookieSessionStore.create(vertx, "my_apps_secret");
    // For some reason, CookieSessionStore encodes (HMAC sign.) the session id properly, but than,
    // Seems not to decode it properly, or just cannot detect the correct sessionID...

    SessionHandler sessionHandler = SessionHandler.create(store);

    // TODO- check if still required for local
    sessionHandler.setCookieSecureFlag(false);

    // sessionHandler.setLazySession(false);  // might be useful
    sessionHandler.setSessionTimeout(1000 * 20); // 20 sec

    return sessionHandler;

  }

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
      // populate file contents here...
      // TODO- handle this later to allow default user-pass list.
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

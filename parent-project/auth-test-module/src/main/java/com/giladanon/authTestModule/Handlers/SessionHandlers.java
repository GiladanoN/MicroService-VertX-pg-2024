package com.giladanon.authTestModule.Handlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class SessionHandlers {

  final static int SessionTimeoutMS = 1000 * 20;
  final static String SessionTimeoutDESC = "20sec";

  public static SessionHandler GenerateSessionHandler(Vertx vertx) {

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
    sessionHandler.setSessionTimeout(SessionTimeoutMS); // see SessionTimeoutDESC

    return sessionHandler;

  }


  /**
   * Returnes a handler, which fails if session is invalid / user logged out etc.
   * @param route logging param, used to inform which endpoint was being accessed on this middleware.
   * @return
   */
  public static Handler<RoutingContext> Validator(String route) {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = sessionContext -> {
          if (sessionContext.user() != null)
            sessionContext.next();
        
          sessionContext.session().destroy();
          sessionContext.fail(401,
            new Throwable("Session is invalid, please login to use this resource."));

          System.out.println(route + ": protected endpoint has been handled (auth required failure)");
          System.out.println(route + ": -- Request Finished with failure --");
          System.out.println(); // newline

          sessionContext.next();  // politely finish server handling
        };

        return newHandler;
      });
    
    return handler;
  }

}

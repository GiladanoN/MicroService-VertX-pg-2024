package com.giladanon.authTestModule.Handlers;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.giladanon.authTestModule.Server.ResponseSender;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;

public class AuthHandlers {

  private static final String authFilePath = "./authfile.txt";
  
  /**
   * retrieves or creates the User Authentication middleware.
   * @param vertx instance to be passed from caller
   * @return AuthenticationHandler which uses BasicAuthHandler as its impl. & file based password mgmt.
   */
  public static AuthenticationHandler UserAuthHandler(Vertx vertx) {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () -> {
        // computes & keeps the relevant handler in map, unless already present
        AuthUtils.ensureFileIsCreatedOrExists(Path.of(authFilePath));
        AuthenticationProvider authProvider =
          PropertyFileAuthentication.create(vertx, authFilePath);
        return BasicAuthHandler.create(authProvider);
      });
    
    return (AuthenticationHandler) handler;
  }

  /**
   * A Handler which reports the relevant msg to a user when login fails.
   * 
   * Expected to be registered with `failureHandler`, (see first if block, reflects original idea)
   * But will still checks 'context.failed()' first, and calls 'next' in case not in a fail state...
   * 
   * @return The relevant middleware.
   */
  public static Handler<RoutingContext> UserLoginFailureHandler() {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () -> {

        System.out.println("Hello from " + handlerName);

        Handler<RoutingContext> newHandler = (context) -> {
          if (context.failed() == false) {
            // should not happen, research this edge case if hit.
            System.out.println("somehow in failure handler but context does not indecate that...");
            context.next(); return;
          }
  
          String failMsg =
            (context.failure() != null) ? context.failure().getMessage():
            "unknown failure during attempt to use basic-auth";

          String body = new StringBuilder()
            .append("logged in? ").append(false)
            .append(" / failure:").append(failMsg)
            .toString();

          ResponseSender.create(context)
            .setStatusCode(401) // unauthorized
            .send(body);
          context.next();  // politely finish server handling
        };

        return newHandler;

      });
    
    return handler;

  }

  
  static class AuthUtils {

    public static boolean ensureFileIsCreatedOrExists(Path requiredPath) {  //, boolean populateDefaults) {
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
  
}

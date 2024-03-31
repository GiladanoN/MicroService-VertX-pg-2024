package com.giladanon.authTestModule.Handlers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.Authorizations;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

public class DebugHandlers {
    
  public static Handler<RoutingContext> SessionUserInfo(String route) {

    String handlerName = Utils.getCurrentMethodName();

    var handler =
      HandlersMap.computeHandlerIfMissing(handlerName, () ->
      {
        Handler<RoutingContext> newHandler = context -> {

          System.out.println("Hello from DebugInfoHandler! (" + route + ")");
    
          Session session = context.session();
          System.out.println("context.session(): " + DebugUtils.descSessionInline(session));
          DebugUtils.printSessionDataMultiline(session);
          
          User user = context.user();
          System.out.println(DebugUtils.descUserInline(user, "context.user()"));
    
          // irrelevant, instead see "__vertx.userHolder" key...
          // if (user == null) {
          //   user = session.get("asociatedUser");
          //   System.out.println(DebugUtils.descUserInline(user, "session.get(\"asociatedUser\")"));
          // }
          
          System.out.println(); // newline
          
          context.next();
    
          // if (failure != null) {
          //   context.fail(401, failure);
          //   return;
          // }
        
        };

        return newHandler;
      });
    
    return handler;
  }

  class DebugUtils {

    private static void printSessionDataMultiline(Session session) {
      if (session == null) {
        System.out.println("provided session IS-NULL !"); return;
      }
      if (session.data() == null) {
        System.out.println("session.data() IS-NULL !"); return;
      }

      System.out.println("DataMap in session (Size=" + session.data().size() + ") - " + session);
      session.data().forEach((key,obj) -> System.out.println(key+": "+obj));  
    }

    private static String descSessionInline(Session ses) {
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

    private static String descUserInline(User user, String objSrc) {

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

    private static String descPropListInline(final List<String> propsList, Function<String,Object> mapper) {
      return (
        propsList.stream()
          .map(propName -> {
            StringBuilder propDesc = new StringBuilder();
            Object propValue = mapper.apply(propName); // user.get(propName);
            propDesc.append("(").append(propName).append(")=");
            propDesc.append(propValue == null ? "NULL" : propValue.toString());
            return propDesc.toString();
          })
          .collect(Collectors.joining(" , "))
      );

      // userDesc.append("(exp)=").append(.toString()).append(" , ");
      // userDesc.append("(iat)=").append(user.get("iat").toString()).append(" , ");
      // userDesc.append("(nbf)=").append(user.get("nbf").toString()).append(" / ");

    }

  }

}

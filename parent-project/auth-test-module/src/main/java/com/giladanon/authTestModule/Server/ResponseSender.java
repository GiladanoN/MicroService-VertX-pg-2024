package com.giladanon.authTestModule.Server;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class ResponseSender {

  private RoutingContext context;
  private Integer statusCode = null;
  private String contentType = "text/html";
  private boolean skipIfEnded = false;
  
  public ResponseSender(RoutingContext context) {
    this.context = context;
  }

  public static ResponseSender create(RoutingContext context) {
    return new ResponseSender(context);
  }

  public ResponseSender setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }
  
  public ResponseSender setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public void setSkipIfEnded(boolean flag) {
    this.skipIfEnded = flag;
  }

  public void send(String body) {

    if (skipIfEnded && context.response().ended()) {
      System.out.println();
      return;
    }
    
    HttpServerResponse response = context.response();

    if (context == null || response == null) {
      String err = "ERROR :: cannot send response - context or resonse objects are null.";
      System.out.println(err);
      throw new RuntimeException(err);
    }

    if (body == null) body = "END.";
    System.out.println("Sending response: " + body);
    
    // consider checking for the header presence before adding / changing this mechanism...
    response.putHeader("Content-Type", this.contentType);

    if (statusCode != null)
      response.setStatusCode(this.statusCode);
    
    response.end(body);

    System.out.println(
      "'response.end()' triggered & should have completed. (" + response.ended() + ")\n"); // newline
  }

}

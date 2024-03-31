package com.giladanon.authTestModule.Server;

import java.util.Optional;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class ServerStarter {

  final public HttpServer server;
  private int port;
  Optional<Promise<?>> listenStartedPromise = Optional.empty();

  public ServerStarter(HttpServer server, int port) {
    this.server = server;
    this.port = port;
  }

  public ServerStarter setStartedNotifyPromise(Promise<Void> promise) {
    this.listenStartedPromise = Optional.of(promise);
    return this;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public HttpServer getServer() {
    return server;
  };
  
  public void StartListen(Handler<HttpServerRequest> requestsHandler) {

    if (server == null || requestsHandler == null) {
      String err = "ERROR :: cannot start server - server or requestHandler objects are null.";
      System.out.println(err);
      throw new RuntimeException(err);
    }

    Handler<AsyncResult<HttpServer>> handlerListenStarted = http -> {

      System.out.println("Hello from 'ServerStarter.StartListen(...)' !");
      boolean success = http.succeeded();

      System.out.println( 
        "HTTP server "
          + (success ? "*successfuly* started" : "*failed* to start")
          + " on port " + port
      );

      Consumer<Promise> promiseResolution =
        (success) ?
          (Promise::complete) :
          (p -> p.fail(http.cause())
      );
      listenStartedPromise.ifPresent(promiseResolution);
    };

    
    server
      .requestHandler(requestsHandler)
      .listen(port, handlerListenStarted);
    
  }

  // private HttpServer startServer(
  //   Promise<Void> startPromise, int serverPort, Handler<HttpServerRequest> requestsHandler)
  // {
  //   Handler<AsyncResult<HttpServer>> handlerListenStarted = http -> {
  //     if (http.succeeded()) {
  //       startPromise.complete();
  //       System.out.println("HTTP server started on port " + serverPort);
  //     } else {
  //       startPromise.fail(http.cause());
  //     }
  //   };
  //
  //   return (
  //     vertx.createHttpServer()
  //       .requestHandler(requestsHandler)
  //       .listen(serverPort, handlerListenStarted)
  //   );
  // }

}

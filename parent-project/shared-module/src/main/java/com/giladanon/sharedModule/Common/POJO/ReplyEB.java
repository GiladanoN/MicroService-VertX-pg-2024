package com.giladanon.sharedModule.Common.POJO;

public class ReplyEB<D> {

  private boolean success = true;
  private int statusCode = 200;
  private D body;
  private Throwable cause;
  
  public boolean getSuccess() {
    return success;
  }
  public int getStatusCode() {
    return statusCode;
  }
  public D getBody() {
    return body;
  }
  public Throwable getCause() {
    return cause;
  }

  public ReplyEB<D> setSuccess(boolean success) {
    this.success = success;
    return this;
  }
  public ReplyEB<D> setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }
  public ReplyEB<D> setBody(D body) {
    this.body = body;
    return this;
  }
  public ReplyEB<D> setCause(Throwable cause) {
    this.cause = cause;
    return this;
  }

}

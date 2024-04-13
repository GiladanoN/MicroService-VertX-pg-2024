package com.giladanon.sharedModule.Common.Exceptions;

public class ValidationError extends Throwable {
  public ValidationError() {
    super();
  }
  public ValidationError(String message) {
    super(message);
  }
  public ValidationError(Throwable t) {
    super(t);
  }
  public ValidationError(String message, Throwable t) {
    super(message, t);
  }
}

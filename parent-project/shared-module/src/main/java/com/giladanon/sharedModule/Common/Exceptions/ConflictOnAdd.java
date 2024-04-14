package com.giladanon.sharedModule.Common.Exceptions;

public class ConflictOnAdd extends Throwable {
  public ConflictOnAdd() {
    super();
  }
  public ConflictOnAdd(String message) {
    super(message);
  }
  public ConflictOnAdd(Throwable t) {
    super(t);
  }
  public ConflictOnAdd(String message, Throwable t) {
    super(message, t);
  }
}
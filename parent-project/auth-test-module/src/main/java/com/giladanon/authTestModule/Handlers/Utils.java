package com.giladanon.authTestModule.Handlers;

public class Utils {

  public static String getCurrentMethodName() {
    // offset by 1 to arrive at caller method
    StackTraceElement callerFrame = new Throwable().getStackTrace()[1];
    return (callerFrame.getClassName() + "." + callerFrame.getMethodName());
  }
  
}

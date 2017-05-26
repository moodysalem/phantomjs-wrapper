package com.moodysalem.phantomjs.wrapper;


import java.util.stream.Stream;

public class RenderOptionsException extends Exception {

  private static final long serialVersionUID = 1L;
  private static String LINE_SEPARATOR = System.getProperty("line.separator");

  public RenderOptionsException(String[] constraintViolationMessages) {
    super(buildMessage(constraintViolationMessages));
  }

  private static String buildMessage(String[] constraintViolationMessages) {
    StringBuilder messageBuider = new StringBuilder("RenderOption is not valid")
        .append(LINE_SEPARATOR);

    Stream.of(constraintViolationMessages).forEach(message -> {
      messageBuider.append(message).append(LINE_SEPARATOR);
    });

    return messageBuider.toString();
  }
}

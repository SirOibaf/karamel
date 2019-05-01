package se.kth.karamel.core.execution;

public class ExecutionException extends RuntimeException {

  ExecutionException(String msg) {
    super(msg);
  }

  ExecutionException(String msg, Throwable e) {
    super(msg, e);
  }

  ExecutionException(Throwable e) {
    super(e);
  }
}

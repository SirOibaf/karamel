package se.kth.karamel.common.exception;

public class KaramelException extends Exception {

  public KaramelException() {
  }

  public KaramelException(String message) {
    super(message);
  }
  
  public KaramelException(Throwable exception) {
    super(exception);
  }
  
  public KaramelException(String message, Throwable exception) {
    super(message, exception);
  } 
}

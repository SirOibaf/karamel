package se.kth.karamel.common.exception;

public class IpAddressException extends ValidationException {

  public IpAddressException() {
  }

  public IpAddressException(String message) {
    super(message);
  }

  public IpAddressException(Throwable exception) {
    super(exception);
  }

  public IpAddressException(String message, Throwable exception) {
    super(message, exception);
  }
}

package se.kth.karamel.webservice.calls.cluster;

public enum ClusterActions {
  VALIDATE("validate");

  private String value = null;

  ClusterActions(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}

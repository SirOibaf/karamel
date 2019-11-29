package se.kth.karamel.webservice.calls.deployment;

public enum DeploymentActions {
  START("start"),
  STOP("stop"),
  PAUSE("pause");

  private String value = null;

  DeploymentActions(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}

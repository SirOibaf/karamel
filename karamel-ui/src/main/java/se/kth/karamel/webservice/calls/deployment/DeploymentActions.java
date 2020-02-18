package se.kth.karamel.webservice.calls.deployment;

public enum DeploymentActions {
  RESUME("resume"),
  RETRY("retry"),
  SKIP("skip"),
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

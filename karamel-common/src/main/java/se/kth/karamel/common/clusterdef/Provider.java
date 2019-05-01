package se.kth.karamel.common.clusterdef;

import se.kth.karamel.common.exception.ValidationException;

public abstract class Provider {

  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
  
  public abstract Provider cloneMe();
  
  public abstract Provider applyParentScope(Provider parentScopeProvider);
  
  public abstract Provider applyDefaults();
  
  public abstract void validate() throws ValidationException;
}

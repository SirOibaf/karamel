package se.kth.karamel.common.util;

import lombok.Getter;
import lombok.Setter;

public class SudoPassword {

  @Getter @Setter
  private String password;

  public SudoPassword() { }

  public SudoPassword(String password) {
    this.password = password;
  }
}

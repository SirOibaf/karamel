package se.kth.karamel.common.util;

import lombok.Getter;
import lombok.Setter;

public class SSHKeyPair {

  @Getter @Setter
  private String privateKeyPath;

  @Getter @Setter
  private String passphrase;

  public SSHKeyPair() {

  }

  public SSHKeyPair(String privateKeyPath) {
    this.privateKeyPath = privateKeyPath;
  }

  public SSHKeyPair(String privateKeyPath, String passphrase) {
    this.privateKeyPath = privateKeyPath;
    this.passphrase = passphrase;
  }
}

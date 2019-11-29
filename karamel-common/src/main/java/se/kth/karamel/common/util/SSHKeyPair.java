package se.kth.karamel.common.util;

import lombok.Getter;
import lombok.Setter;

public class SSHKeyPair {

  @Getter @Setter
  private String privateKeyPath;

  @Getter @Setter
  private String publicKeyPath;

  @Getter @Setter
  private String passphrase;

  public SSHKeyPair() {

  }

  public SSHKeyPair(String privateKeyPath, String publicKeyPath, String passphrase) {
    this.privateKeyPath = privateKeyPath;
    this.publicKeyPath = publicKeyPath;
    this.passphrase = passphrase;
  }
}

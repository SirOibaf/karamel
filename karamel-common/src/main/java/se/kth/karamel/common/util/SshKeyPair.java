package se.kth.karamel.common.util;

import lombok.Getter;
import lombok.Setter;

public class SshKeyPair {

  @Getter @Setter
  private String privateKeyPath;

  @Getter @Setter
  private String publicKeyPath;

  @Getter @Setter
  private String passphrase;
}

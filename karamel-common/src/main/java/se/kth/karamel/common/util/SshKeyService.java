package se.kth.karamel.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.SshKeysNotfoundException;

public class SshKeyService {

  private static final Logger logger = Logger.getLogger(SshKeyService.class);

  public static boolean checkIfPasswordNeeded(SshKeyPair sshKey) {
    //    http://serverfault.com/questions/52732/find-out-if-a-ssh-private-key-requires-a-password
    // OpenSSH Keys with passwords contain this string
    if (sshKey.getPublicKey().isEmpty() || sshKey.getPrivateKey().isEmpty()) {
      return true;
    }
    return sshKey.getPrivateKey().contains("Proc-Type: 4,ENCRYPTED");
  }  

  public static SshKeyPair loadSshKeys(String pubkeyPath, String prikeyPath, String passphrase)
      throws SshKeysNotfoundException {
    String pubKey = null;
    String priKey = null;
    try {
      BufferedReader r1;
      r1 = new BufferedReader(new FileReader(new File(pubkeyPath)));
      pubKey = r1.readLine();
      r1.close();

      try (Scanner scanner = new Scanner(new File(prikeyPath))) {
        priKey = scanner.useDelimiter("\\A").next();
      }

    } catch (IOException ex) {
      throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'",
          pubkeyPath, prikeyPath), ex);
    }
    if (pubKey != null && priKey != null) {
      SshKeyPair keypair = new SshKeyPair();
      keypair.setPublicKeyPath(pubkeyPath);
      keypair.setPublicKey(pubKey);
      keypair.setPrivateKeyPath(prikeyPath);
      keypair.setPrivateKey(priKey);
      keypair.setPassphrase(passphrase);
      keypair.setNeedsPassword(checkIfPasswordNeeded(keypair));
      return keypair;
    }
    throw new SshKeysNotfoundException(String.format("Unsuccessful to load ssh keys from '%s' and/or '%s'",
        pubkeyPath, prikeyPath));

  }

}

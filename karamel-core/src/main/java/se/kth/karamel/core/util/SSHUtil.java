package se.kth.karamel.core.util;

import se.kth.karamel.common.util.SSHKeyPair;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SSHUtil {

  /**
   * List all available SSHKey Pairs in ~/.ssh dir
   * @return
   */
  public List<SSHKeyPair> getAvailableKeys() throws IOException {
    Path sshPath = Paths.get(System.getenv("HOME"), ".ssh");
    List<SSHKeyPair> keyList = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(sshPath, path -> {
        // Only look for the private keys.
        return !path.toString().endsWith(".pub") && Files.isRegularFile(path);
      })) {
      for (Path path : stream) {
        keyList.add(new SSHKeyPair(path.toString()));
      }
    }

    return keyList;
  }
}

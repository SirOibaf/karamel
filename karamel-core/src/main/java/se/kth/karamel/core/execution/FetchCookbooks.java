package se.kth.karamel.core.execution;

import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.util.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FetchCookbooks extends Task {

  private final static Logger LOGGER = Logger.getLogger(FetchCookbooks.class.getName());

  private Path localCookbooksPath;

  public FetchCookbooks(Settings settings, Path localCookbooksPath) {
    this.settings = settings;
    this.localCookbooksPath = localCookbooksPath;
  }

  /**
   * Recursively download cookbooks file from
   * @throws ExecutionException
   * @throws IOException
   */
  @Override
  void execute() throws ExecutionException, IOException {
    // We recursively SCP the cookbooks files to the target machine
    Files.walk(localCookbooksPath)
      .forEach(path -> {
        try {
          Path relativePath = localCookbooksPath.relativize(path);
          node.scpFile(path.toString(),
              Paths.get(node.getWorkDir(), Constants.REMOTE_COOKBOOKS_DIR_NAME,
                  relativePath.toString()).toString());
        } catch (IOException e) {
          throw new ExecutionException(e);
        }
      });

    LOGGER.log(Level.INFO, "FetchCookbook completed on node: " + node.getHostAddr());
  }
}

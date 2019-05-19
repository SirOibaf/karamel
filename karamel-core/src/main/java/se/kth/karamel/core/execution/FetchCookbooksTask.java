package se.kth.karamel.core.execution;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.util.Settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FetchCookbooksTask extends Task {

  private final static Logger LOGGER = Logger.getLogger(FetchCookbooksTask.class.getName());

  private Path localCookbooksPath;

  public FetchCookbooksTask(int taskId, Node node, Settings settings, Path localCookbooksPath) {
    this.taskId = taskId;
    this.node = node;
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
    Files.walk(localCookbooksPath).forEach(path -> {
        try {
          Path relativePath = localCookbooksPath.relativize(path);
          node.scpFile(path.toString(),
              Paths.get(node.getWorkDir(), Constants.REMOTE_COOKBOOKS_DIR_NAME,
                  relativePath.toString()).toString());
        } catch (IOException e) {
          throw new ExecutionException(e);
        }
      });

    LOGGER.log(Level.INFO, "FetchCookbook completed on node: " + node.getHostname());
  }
}

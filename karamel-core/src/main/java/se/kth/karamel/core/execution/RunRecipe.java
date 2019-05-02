package se.kth.karamel.core.execution;

import se.kth.karamel.common.util.Settings;

import java.io.IOException;

public class RunRecipe extends Task {

  public RunRecipe(Settings settings) {

  }

  /**
   * To run a recipe we need to perform the following actions:
   * - Build the recipe specific databag - which contains IPs (sad) and
   * the attributes contained in the cluster definition - The databag also contains the recipe to run
   * - SCP the databag to the target node
   * - Run the chef-solo command
   * - Potentially retrieve the output and add it to the databag generator
   * for the next executions
   * @throws ExecutionException
   * @throws IOException
   */
  @Override
  void execute() throws ExecutionException, IOException {
    // TODO(Fabio)
  }
}

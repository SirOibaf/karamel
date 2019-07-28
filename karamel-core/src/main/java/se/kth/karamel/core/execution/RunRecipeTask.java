package se.kth.karamel.core.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.io.FileUtils;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.chef.DataBag;
import se.kth.karamel.core.chef.DataBagsFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunRecipeTask extends Task {

  private DataBagsFactory dataBagsFactory;
  private Settings settings;

  private Group group;
  private Recipe recipe;

  public RunRecipeTask(int taskId, Node node, Group group, Recipe recipe,
                       Settings settings, DataBagsFactory dataBagsFactory) {
    this.taskId = taskId;
    this.node = node;
    this.group = group;
    this.recipe = recipe;
    this.settings = settings;
    this.dataBagsFactory = dataBagsFactory;
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
    // 1. Retrieve and materialize databag
    Path dataBagPath = materializeDataBag();

    // 2. Run recipe
    runRecipe(dataBagPath);

    // 3. Fetch output
    fetchRecipeOutput();
  }

  // TODO(Fabio): we can probably avoid to execute this in case of retries
  private Path materializeDataBag() throws IOException {
    DataBag orgDataBag = dataBagsFactory.getGroupDataBag(group);

    // Make a copy of the original hashmap has it's shared between running thread.
    // The new clone will have the same key/values, but it will also contain the recipe to run
    String[] runList = new String[]{recipe.getCanonicalName()};

    DataBag recipeDataBag = (DataBag)orgDataBag.clone();
    recipeDataBag.put("run_list", runList);

    ObjectMapper objectMapper = new ObjectMapper();
    String dataBagStr = objectMapper.writeValueAsString(recipeDataBag);

    // Write the dataBag to a tmp file
    Path dataBagPath = Paths.get(settings.get(Settings.SettingsKeys.WORKING_DIR),
        Constants.KARAMEL_TMP_DIRNAME,
        taskId + Constants.DATABAG_NAME_SEPARATOR + recipe.getCanonicalName() + Constants.DATABAG_NAME_POSTFIX);

    FileUtils.writeStringToFile(dataBagPath.toFile(), dataBagStr);

    // SCP the file to the target machine
    node.scpFileUpload(dataBagPath.toString(),
        Paths.get(node.getWorkDir(), Constants.REMOTE_INSTALL_DIR_NAME).toString());

    return dataBagPath;
  }

  /*
   * This is the bash command that needs to be executed for running a recipe
   * sudo chef-solo
   *   -c /home/vagrant/.karamel/install/solo.rb
   *   -j /home/vagrant/.karamel/install/kagent__default.json 2>&1 | tee kagent::default.log
   */
  private void runRecipe(Path dataBagPath) throws IOException, ExecutionException {
    String soloConfPath = Paths.get(node.getWorkDir(),
        Constants.REMOTE_INSTALL_DIR_NAME, Constants.SOLO_CONF_NAME).toString();

    String soloCommand = Constants.CHEF_BIN_NAME +
        " -c " + soloConfPath +
        " -j " + dataBagPath.toString() +
        " 2>&1 | tee " + recipe.getCanonicalName() + ".log";

    Session.Command cmd = node.execCommand(soloCommand, true);
    if (cmd.getExitStatus() != 0) {
      throw new ExecutionException("Error executing recipe " + recipe.getCanonicalName());
    }
  }

  private void fetchRecipeOutput() throws IOException {
    String localResultsPath =
        Paths.get(System.getenv(Constants.KARAMEL_HOME), Constants.KARAMEL_RESULTS_DIRNAME).toString();
    String remoteResultPath = Paths.get("/tmp",
        recipe.getCanonicalName().replace("::", "__") + Constants.RECIPE_RESULT_POSFIX).toString();
    node.scpFileDownload(localResultsPath, remoteResultPath);

    // TODO(Fabio): parse output and add it to databag
  }
}

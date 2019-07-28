package se.kth.karamel.core.execution;

import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.ChefSoloConf;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.core.node.OSFamily;

import java.io.IOException;
import java.nio.file.Paths;

public class NodeSetupTask extends Task {

  private static Logger LOGGER = Logger.getLogger(NodeSetupTask.class.getName());

  public NodeSetupTask(int taskId, Node node, Settings settings) {
    this.taskId = taskId;
    this.node = node;
    this.settings = settings;
  }

  /**
   * This Task sets up the Node for Karamel installation. The following steps are executed:
   *
   * 1. Make sure working dir exists on target node
   * 2. Determine the linux distribution - check if yum is installed
   * 3. SCP the correct chefdk artifact to the machine
   * 4. Execute the installer
   * 5. Template the solo.rb file in the correct directory
   */
  @Override
  public void execute() throws ExecutionException, IOException {
    Session.Command mkdirCmd = node.execCommand("mkdir -p " + node.getWorkDir(), false);
    if (mkdirCmd.getExitStatus() != 0) {
      throw new ExecutionException("Failed to create working dir on node: " + node.getHostname());
    }

    Session.Command yumCmd = node.execCommand("yum --help", false);

    // TODO(Fabio) this is wrong
    String chefDkLocalPath = Paths.get(System.getenv(Constants.KARAMEL_HOME), "chefdk").toString();
    OSFamily osFamily = null;
    String chefBinName = null;
    if (yumCmd.getExitStatus() == 0) {
      // RedHat family distribution
      LOGGER.log(Level.INFO, "Node: " + node.getHostname() + " - OS Family: RedHat");

      osFamily = OSFamily.REDHAT;
      chefBinName = "chefdk-" + settings.get(Settings.SettingsKeys.CHEFDK_VERSION_KEY)
          + Constants.CHEFDK_BUILDINFO_REDHAT_DEFAULT;
    } else {
      // Ubuntu distribution
      LOGGER.log(Level.INFO, "Node: " + node.getHostname() + " - OS Family: Ubuntu");

      osFamily = OSFamily.UBUNTU;
      chefBinName = "chefdk_" + settings.get(Settings.SettingsKeys.CHEFDK_VERSION_KEY) +
          Constants.CHEFDK_BUILDINFO_UBUNTU_DEFAULT;
    }

    String remoteInstallDir = Paths.get(node.getWorkDir(), Constants.REMOTE_INSTALL_DIR_NAME).toString();
    chefDkLocalPath = Paths.get(chefDkLocalPath, chefBinName).toString();

    node.scpFileUpload(chefDkLocalPath, remoteInstallDir);

    // install ChefDk
    installChefDK(chefBinName, osFamily, remoteInstallDir);

    // templateSoloConf
    templateSoloConf(settings, remoteInstallDir);

    LOGGER.log(Level.INFO, "NodeSetup completed on node: " + node.getHostname());
  }

  private void installChefDK(String chefBinName, OSFamily osFamily, String remoteInstallDir) throws IOException {
    String installCmd = "";
    switch (osFamily) {
      case REDHAT:
        installCmd = "yum install -y " + Paths.get(remoteInstallDir, chefBinName).toString();
        break;
      case UBUNTU:
        installCmd = "apt-get install " + Paths.get(remoteInstallDir, chefBinName).toString();
    }

    node.execCommand(installCmd, true);
  }

  private void templateSoloConf(Settings settings, String remoteInstallDir) throws IOException {
    node.scpFileUpload(ChefSoloConf.getConfLocalPath(settings, node.getUser()), remoteInstallDir);
  }
}

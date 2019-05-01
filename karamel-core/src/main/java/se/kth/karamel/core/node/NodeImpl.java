package se.kth.karamel.core.node;

import lombok.Getter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.xfer.FileSystemFile;
import se.kth.karamel.common.util.Constants;
import se.kth.karamel.core.ClusterContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeImpl implements Node {

  private final static Logger LOGGER = Logger.getLogger(NodeImpl.class.getName());

  @Getter
  private String hostAddr;

  @Getter
  private String workDir;

  @Getter
  private String user;

  private ClusterContext clusterContext;

  public NodeImpl(String hostAddr, String user, ClusterContext clusterContext) {
    this.hostAddr = hostAddr;
    this.user = user;
    this.clusterContext = clusterContext;
    this.workDir = Paths.get("/home", user, Constants.REMOTE_WORKING_DIR_NAME).toString();
  }

  /**
   * Execute a command on a machine
   * @param command
   * @param requiresRoot
   * @return
   * @throws IOException
   */
  @Override
  public Session.Command execCommand(String command, boolean requiresRoot) throws IOException {
    SSHClient sshClient = getSSHClient();
    Session session = null;
    try {
      session = sshClient.startSession();
      // Allocate the default PTY otherwise SUDO will complain
      session.allocateDefaultPTY();

      if (requiresRoot) {
        command = "sudo " + command;
      }

      Session.Command cmd = session.exec(command);
      if (requiresRoot) {
        // Send the SUDO password on the output stream and flush the channel - \n is required here
        cmd.getOutputStream().write((clusterContext.getSudoPassword() + "\n").getBytes());
        cmd.getOutputStream().flush();
      }

      // Wait for the command to finish.
      cmd.join();

      LOGGER.log(Level.FINE, "Command " + command + " executed on node: " + hostAddr);

      return cmd;
    } finally {
      if (session != null) {
        session.close();
      }
      sshClient.disconnect();
    }
  }

  @Override
  public void scpFile(String localFilePath, String targetPath) throws IOException {
    SSHClient sshClient = getSSHClient();
    try {
      sshClient.newSCPFileTransfer().upload(new FileSystemFile(localFilePath), targetPath);
      LOGGER.log(Level.FINE, "File " + localFilePath + " copied to node: " + hostAddr);
    } finally {
      sshClient.disconnect();
    }
  }

  // TODO(Fabio): does it make sense to recycle sshclient? Can we do it? Should we do it?
  private SSHClient getSSHClient() throws IOException {
    SSHClient sshClient = new SSHClient();
    sshClient.loadKnownHosts();
    // TODO(Fabio): Make it configurable and not limited to SSHKey
    sshClient.loadKeys(clusterContext.getSshKeyPair().getPrivateKeyPath());
    sshClient.connect(hostAddr);
    sshClient.authPublickey(user);

    return sshClient;
  }
}
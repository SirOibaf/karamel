package se.kth.karamel.core.node;

import net.schmizz.sshj.connection.channel.direct.Session;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.core.ClusterContext;

import java.io.IOException;

public class NodeNoop extends NodeImpl {

  public NodeNoop(int nodeId, String hostname, String privateIP, String publicIP,
                  String user, Group group, ClusterContext clusterContext) {
    super(nodeId, hostname, privateIP, publicIP, user, group, clusterContext);
  }

  public NodeNoop(int nodeId) {
    super(nodeId, "", "", "", "", null, null);
  }

  @Override
  public Session.Command execCommand(String command, boolean requiresRoot) throws IOException {
    // Do nothing
    return null;
  }

  @Override
  public void scpFileUpload(String localFilePath, String targetPath) throws IOException {
    // Do nothing
  }

  @Override
  public void scpFileDownload(String localFilePath, String targetPath) throws IOException {
    // Do nothing
  }
}
package se.kth.karamel.webservice.calls.deployment;

import lombok.Getter;
import lombok.Setter;
import se.kth.karamel.common.node.Node;

import java.util.List;

public class NodeStatusDTO {

  @Getter @Setter
  private int nodeId;
  @Getter @Setter
  private String privateIP;
  @Getter @Setter
  private String publicIP;
  @Getter @Setter
  private String hostname;
  @Getter @Setter
  private List<TaskStatusDTO> taskStatus;

  public NodeStatusDTO(Node node, List<TaskStatusDTO> taskStatus) {
    this.nodeId = node.getNodeId();
    this.privateIP = node.getPrivateIP();
    this.publicIP = node.getPublicIP();
    this.hostname = node.getHostname();
    this.taskStatus = taskStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NodeStatusDTO that = (NodeStatusDTO) o;

    if (nodeId != that.nodeId) return false;
    if (privateIP != null ? !privateIP.equals(that.privateIP) : that.privateIP != null) return false;
    if (publicIP != null ? !publicIP.equals(that.publicIP) : that.publicIP != null) return false;
    return hostname != null ? hostname.equals(that.hostname) : that.hostname == null;
  }

  @Override
  public int hashCode() {
    int result = nodeId;
    result = 31 * result + (privateIP != null ? privateIP.hashCode() : 0);
    result = 31 * result + (publicIP != null ? publicIP.hashCode() : 0);
    result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
    return result;
  }
}

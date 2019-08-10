package se.kth.karamel.core.provisioner.jcloud.baremetal;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.node.NodeNoop;
import se.kth.karamel.core.provisioner.Provisioner;

import java.util.ArrayList;
import java.util.List;

public class NoopProvisioner implements Provisioner  {

  @Override
  public void cleanup(Cluster definition, Group group) throws KaramelException {
    //No-op here.
  }

  @Override
  public int provisionGroup(ClusterContext clusterContext,
                            Cluster cluster, Group group, int currentNodeId) throws KaramelException {
    //No-op here. Just create the node objects with the IPs
    List<Node> nodes = new ArrayList<>();
    try {
      group.getProvider().merge(cluster.getProvider());
      for (String IP : group.getNoop().getIps()) {
        nodes.add(new NodeNoop(currentNodeId, IP, IP, IP, group.getNoop().getUsername(),
            clusterContext));
        currentNodeId++;
      }
    } catch (Exception e) {
      throw new KaramelException("Error provisioning group: " + group.getName(), e);
    }

    group.getNoop().setNodes(nodes);

    return nodes.size();
  }
}

package se.kth.karamel.core.provisioner.jcloud.google;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.provisioner.Provisioner;

import java.util.List;

public class GCEProvisioner implements Provisioner {
  @Override
  public void cleanup(Cluster definition, Group group) throws KaramelException {

  }

  @Override
  public List<Node> provisionGroup(ClusterContext clusterContext, Cluster definition, Group group) throws KaramelException {
    return null;
  }
}

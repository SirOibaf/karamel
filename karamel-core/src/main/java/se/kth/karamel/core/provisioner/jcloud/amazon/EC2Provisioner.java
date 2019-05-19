package se.kth.karamel.core.provisioner.jcloud.amazon;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.provisioner.Provisioner;

public class EC2Provisioner implements Provisioner {

  @Override
  public void cleanup(Cluster definition, Group group) throws KaramelException {
  }

  @Override
  public int provisionGroup(ClusterContext clusterContext,
                            Cluster definition, Group group, int currentNodeId)
      throws KaramelException {
    return 0;
  }
}

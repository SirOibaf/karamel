package se.kth.karamel.core.provisioner.jcloud;

import se.kth.karamel.common.clusterdef.Baremetal;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.GCE;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.core.ClusterContext;
import se.kth.karamel.core.provisioner.Provisioner;
import se.kth.karamel.core.provisioner.jcloud.amazon.EC2Provisioner;
import se.kth.karamel.core.provisioner.jcloud.baremetal.BaremetalProvisioner;
import se.kth.karamel.core.provisioner.jcloud.google.GCEProvisioner;

public class JCloudProvisioner implements Provisioner {
  @Override
  public void cleanup(Cluster definition, Group group) throws KaramelException {
    getProvisionerImpl(group).cleanup(definition, group);
  }

  @Override
  public int provisionGroup(ClusterContext clusterContext, Cluster definition, Group group, int currentNodeId)
      throws KaramelException {
    return getProvisionerImpl(group).provisionGroup(clusterContext, definition, group, currentNodeId);
  }

  private Provisioner getProvisionerImpl(Group group) {
    if (group.getProvider() instanceof Baremetal) {
      return new BaremetalProvisioner();
    } else if (group.getProvider() instanceof GCE) {
      return new GCEProvisioner();
    } else {
      return new EC2Provisioner();
    }
  }
}

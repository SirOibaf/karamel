package se.kth.karamel.core.provisioner;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.core.ClusterContext;


public interface Provisioner {

  /**
   * It cleans up all the groups have this type of provider
   * @param definition
   * @param group
   * @throws KaramelException
   */
  void cleanup(Cluster definition, Group group) throws KaramelException;

  /**
   *
   * @param definition
   * @param group
   * @throws KaramelException
   */
  int provisionGroup(ClusterContext clusterContext, Cluster definition,
                     Group group, int currentNodeId) throws KaramelException;

}

package se.kth.karamel.core.chef;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;

import java.util.HashMap;
import java.util.Map;

public class DataBagsFactory {

  private Map<Group, DataBag> dataBagMap = null;

  /**
   * A DataBag is a map that contains the following information:
   *  - Cluster-wide attributes
   *  - Group-wide attributes
   *  - IPs
   *  - Recipe generated json
   */
  public DataBagsFactory(Cluster cluster) {
    dataBagMap = new HashMap<>();

    for (Group group : cluster.getGroups()) {
      dataBagMap.put(group, buildGroupDatabag(cluster, group));
    }
  }

  public DataBag getGroupDataBag(Group group) {
    return dataBagMap.get(group);
  }

  private DataBag buildGroupDatabag(Cluster cluster, Group group) {
    // First we add the cluster wide attributes
    DataBag dataBag = new DataBag(cluster.getAttributes());
    // After we merge (in case of collision they get overwritten) the group attributes
    dataBag.merge(group.getAttributes());
    // Finally we add the IP addr bag

    // TODO(Fabio) see how we can handle the cloud providers
    return dataBag;
  }
}

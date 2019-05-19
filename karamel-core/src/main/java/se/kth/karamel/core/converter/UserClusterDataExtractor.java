package se.kth.karamel.core.converter;

import org.apache.log4j.Logger;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.EC2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.Cluster;

public class UserClusterDataExtractor {

  private static final Logger logger = Logger.getLogger(UserClusterDataExtractor.class);

  public static int totalMachines(Cluster cluster) {
    int total = 0;
    for (Group g : cluster.getGroups()) {
      total += g.getSize();
    }
    return total;
  }

  public static Group findGroup(Cluster cluster, String groupName) {
    for (Group group : cluster.getGroups()) {
      if (group.getName().equals(groupName)) {
        return group;
      }
    }
    return null;
  }
}

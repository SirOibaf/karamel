package se.kth.karamel.core.converter;

import org.apache.log4j.Logger;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Ec2;
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

  // TODO(Fabio): Do we really need to "applyDefaults" an clone a gazzillion time the group provider?
  public static Provider getGroupProvider(Cluster cluster, Group group) {
    Provider groupScopeProvider = group.getProvider();
    Provider clusterScopeProvider = cluster.getProvider();
    Provider provider = null;

    if (groupScopeProvider == null && clusterScopeProvider == null) {
      provider = Ec2.makeDefault();
    } else if (groupScopeProvider == null) {
      provider = clusterScopeProvider.cloneMe().applyDefaults();
    } else {
      provider = groupScopeProvider.applyParentScope(clusterScopeProvider).applyDefaults();
    }
    return provider;
  }
}

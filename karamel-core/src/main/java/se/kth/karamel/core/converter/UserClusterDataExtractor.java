package se.kth.karamel.core.converter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.core.running.model.ClusterRuntime;
import se.kth.karamel.core.running.model.GroupRuntime;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;

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

  public static GroupRuntime findGroup(ClusterRuntime clusterEntity, String groupName) {
    for (GroupRuntime g : clusterEntity.getGroups()) {
      if (g.getName().equals(groupName)) {
        return g;
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

  public static String makeVendorPath(String sshUser, Map<String, Cookbook> rootCookbooks) throws KaramelException {
    Set<String> paths = new HashSet<>();
    for (Map.Entry<String, Cookbook> cookbook : rootCookbooks.entrySet()) {
      paths.add(Settings.REMOTE_COOKBOOK_VENDOR_PATH(sshUser, cookbook.getKey()));
    }
    Object[] arr = paths.toArray();
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        buffer.append("\n");
      }
      buffer.append("\"");
      buffer.append(arr[i]);
      buffer.append("\"");
      if (i < paths.size() - 1) {
        buffer.append(",");
      }
    }
    return buffer.toString();
  }

}

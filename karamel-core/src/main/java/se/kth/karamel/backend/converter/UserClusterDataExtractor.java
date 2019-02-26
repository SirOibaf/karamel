package se.kth.karamel.backend.converter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
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
    for (Group g : cluster.getGroups().values()) {
      total += g.getSize();
    }
    return total;
  }

  public static Group findGroup(Cluster cluster, String groupName) {
    for (Map.Entry<String, Group> group : cluster.getGroups().entrySet()) {
      if (group.getKey().equals(groupName)) {
        return group.getValue();
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

  public static Provider getGroupProvider(Cluster cluster, String groupName) {
    Group group = findGroup(cluster, groupName);
    Provider groupScopeProvider = group.getProvider();
    Provider clusterScopeProvider = cluster.getProvider();
    Provider provider = null;
    if (groupScopeProvider == null && clusterScopeProvider == null) {
      provider = Ec2.makeDefault();
    } else if (groupScopeProvider == null && clusterScopeProvider != null) {
      provider = (Provider) clusterScopeProvider.cloneMe();
      provider = provider.applyDefaults();
    } else if (groupScopeProvider != null && clusterScopeProvider != null) {
      provider = groupScopeProvider.applyParentScope(clusterScopeProvider);
      provider = provider.applyDefaults();
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

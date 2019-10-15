package se.kth.karamel.core.chef;

import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.clusterdef.Group;
import se.kth.karamel.common.clusterdef.Recipe;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // Generate IPs
    DataBag ipsDataBag = buildIPsDataBag(cluster);

    for (Group group : cluster.getGroups()) {
      dataBagMap.put(group, buildGroupDatabag(cluster, group, ipsDataBag));
    }
  }

  public DataBag getGroupDataBag(Group group) {
    return dataBagMap.get(group);
  }

  // Multiple threads can be updating at the same time.
  public synchronized void updateDataBags(Cluster cluster, DataBag dataBag) {
    for (Group group : cluster.getGroups()) {
      dataBagMap.get(group).merge(dataBag);
    }
  }

  private DataBag buildGroupDatabag(Cluster cluster, Group group, DataBag ipsDataBag) {
    // First we add the cluster wide attributes
    DataBag dataBag = new DataBag(cluster.getAttributes());

    // After we merge (in case of collision they get overwritten) the group attributes
    dataBag.merge(group.getAttributes());
    dataBag.merge(ipsDataBag);

    return dataBag;
  }

  // This is an helper method to be compatible with the old Karamel.
  // For each recipe we provide the public/private IPs of the nodes on which the recipe runs
  private DataBag buildIPsDataBag(Cluster cluster) {
    DataBag ipDataBag = new DataBag();

    cluster.getGroups().forEach(
      group -> group.getRecipes().forEach(
        recipe -> addRecipeIPs(ipDataBag, recipe, group.getProvider().getNodes())
    ));

    return ipDataBag;
  }

  private void addRecipeIPs(DataBag dataBag, Recipe recipe, List<Node> nodeList) {
    String[] recipeSplits = recipe.getCanonicalName().split(Constants.COOKBOOK_DELIMITER);

    Map<String, Object> recipeDatabag = (Map<String, Object>)dataBag.get(recipeSplits[0]);
    if (recipeDatabag == null) {
      recipeDatabag = new HashMap<>();
      dataBag.put(recipeSplits[0], recipeDatabag);
    }

    Map<String, Object> serviceDatabag = (Map)recipeDatabag.get(recipeSplits[1]);
    if (serviceDatabag == null) {
      serviceDatabag = new HashMap<>();
      recipeDatabag.put(recipeSplits[1], serviceDatabag);
    }

    // Add private ips in the service map
    List<String> privateIPs = nodeList.stream().map(Node::getPrivateIP).collect(Collectors.toList());
    List<String> existingPrivateIPs =
      (List<String>) serviceDatabag.getOrDefault(Constants.PRIVATE_IPS, new ArrayList<>());
    existingPrivateIPs.addAll(privateIPs);
    serviceDatabag.put(Constants.PRIVATE_IPS, existingPrivateIPs);

    // Add public ips in the service map
    List<String> publicIPs = nodeList.stream().map(Node::getPublicIP).collect(Collectors.toList());
    List<String> existingPublicIPs =
      (List<String>) serviceDatabag.getOrDefault(Constants.PUBLIC_IPS, new ArrayList<>());
    existingPublicIPs.addAll(publicIPs);
    serviceDatabag.put(Constants.PUBLIC_IPS, existingPublicIPs);
  }
}

package se.kth.karamel.backend.converter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.backend.running.model.ClusterRuntime;
import se.kth.karamel.backend.running.model.GroupRuntime;
import se.kth.karamel.backend.running.model.MachineRuntime;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.json.JsonRecipe;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.clusterdef.Ec2;
import se.kth.karamel.common.clusterdef.Provider;
import se.kth.karamel.common.clusterdef.json.JsonCluster;
import se.kth.karamel.common.clusterdef.json.JsonGroup;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.cookbookmeta.MetadataRb;
import se.kth.karamel.common.cookbookmeta.Recipe;

public class UserClusterDataExtractor {

  private static final Logger logger = Logger.getLogger(UserClusterDataExtractor.class);

  private static final CookbookCache cookbookCache = ClusterDefinitionService.CACHE;

  public static String clusterLinks(JsonCluster cluster, ClusterRuntime clusterEntity) throws KaramelException {
    StringBuilder builder = new StringBuilder();
    for (JsonGroup jg : cluster.getGroups()) {

      for (JsonRecipe rec : jg.getRecipes()) {
        String cbid = rec.getCookbook().getCookbookName();
        KaramelizedCookbook cb = cookbookCache.get(cbid);
        MetadataRb metadataRb = cb.getMetadataRb();
        List<Recipe> recipes = metadataRb.getRecipes();
        for (Recipe recipe : recipes) {
          if (recipe.getCanonicalName().equalsIgnoreCase(rec.getCanonicalName())) {
            Set<String> links = recipe.getLinks();
            for (String link : links) {
              if (link.contains(Settings.METADATA_INCOMMENT_HOST_KEY)) {
                if (clusterEntity != null) {
                  GroupRuntime ge = findGroup(clusterEntity, jg.getName());
                  if (ge != null) {
                    List<MachineRuntime> machines = ge.getMachines();
                    if (machines != null) {
                      for (MachineRuntime me : ge.getMachines()) {
                        String l = link.replaceAll(Settings.METADATA_INCOMMENT_HOST_KEY, me.getPublicIp());
                        builder.append(l).append("\n");
                      }
                    }
                  }
                }
              } else {
                builder.append(link).append("\n");
              }

            }

          }
        }
      }
    }
    return builder.toString();
  }

  public static int totalMachines(JsonCluster cluster) {
    int total = 0;
    for (JsonGroup g : cluster.getGroups()) {
      total += g.getSize();
    }
    return total;
  }

  public static JsonGroup findGroup(JsonCluster cluster, String groupName) {
    for (JsonGroup g : cluster.getGroups()) {
      if (g.getName().equals(groupName)) {
        return g;
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

  public static Provider getGroupProvider(JsonCluster cluster, String groupName) {
    JsonGroup group = findGroup(cluster, groupName);
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

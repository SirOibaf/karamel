package se.kth.karamel.common.clusterdef.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.clusterdef.Cookbook;
import se.kth.karamel.common.clusterdef.Scope;
import se.kth.karamel.common.clusterdef.yaml.YamlCluster;
import se.kth.karamel.common.clusterdef.yaml.YamlGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.kth.karamel.common.cookbookmeta.Attribute;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;

public class JsonCluster extends Scope {

  private String name;
  private Map<String, Cookbook> rootCookbooks = new HashMap<>();
  private List<JsonGroup> groups = new ArrayList<>();

  @JsonIgnore
  private List<KaramelizedCookbook> cookbooks = new ArrayList<>();

  public JsonCluster() {
  }

  public JsonCluster(YamlCluster cluster) throws KaramelException {
    super(cluster);
    name = cluster.getName();
    rootCookbooks = cluster.getCookbooks();
    attributes = cluster.flattenAttrs();
    Set<Attribute> validAttrs = new HashSet<>();

    cookbooks.addAll(CookbookCache.getInstance().loadAllKaramelizedCookbooks(cluster));

    // Filtering invalid(not defined in metadata.rb) attributes from yaml model
    // Get all the valid attributes, also for transient dependency
    for (KaramelizedCookbook kcb : cookbooks) {
      validAttrs.addAll(kcb.getMetadataRb().getAttributes());
    }

    // TODO(Fabio): I think that this map should be <String, Attribute>. But I don't want to see
    // what happen if I change it.
    Map<String, Object> invalidAttrs = new HashMap<>();

    for (String usedAttr: attributes.keySet()) {
      if (!validAttrs.contains(new Attribute(usedAttr))) {
        invalidAttrs.put(usedAttr, attributes.get(usedAttr));
      }
    }

    if (!invalidAttrs.isEmpty()) {
      throw new KaramelException(String.format("Invalid attributes, all used attributes must be defined in metadata.rb "
          + "files: %s", invalidAttrs.keySet().toString()));
    }

    Set<Map.Entry<String, YamlGroup>> entrySet = cluster.getGroups().entrySet();
    for (Map.Entry<String, YamlGroup> entry : entrySet) {
      groups.add(new JsonGroup(entry.getValue(), entry.getKey(), cookbooks));
    }

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<JsonGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<JsonGroup> groups) {
    this.groups = groups;
  }

  public Map<String, Cookbook> getRootCookbooks() {
    return rootCookbooks;
  }

  public void setRootCookbooks(Map<String, Cookbook> rootCookbooks) {
    this.rootCookbooks = rootCookbooks;
  }
}

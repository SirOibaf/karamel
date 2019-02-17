package se.kth.karamel.common.clusterdef;

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
import se.kth.karamel.common.exception.ValidationException;

public class Cluster extends Scope {

  private String name;
  private Map<String, Cookbook> rootCookbooks = new HashMap<>();
  private List<Group> groups = new ArrayList<>();

  public Cluster() { }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public Map<String, Cookbook> getRootCookbooks() {
    return rootCookbooks;
  }

  public void setRootCookbooks(Map<String, Cookbook> rootCookbooks) {
    this.rootCookbooks = rootCookbooks;
  }

  @Override
  public void validate() throws ValidationException, KaramelException {
    super.validate();

    List<KaramelizedCookbook> cookbooks = CookbookCache.getInstance().loadAllKaramelizedCookbooks(rootCookbooks);

    // Validate cluster-wide attributes
    attributes = cluster.flattenAttrs();
    Set<Attribute> validAttrs = new HashSet<>();

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
      throw new ValidationException(String.format("Invalid attributes, all used attributes must be defined " +
          "in metadata.rb files: %s", invalidAttrs.keySet().toString()));
    }

    // Validate Groups
    for (Group g : groups) {
      g.validate();
    }
  }
}

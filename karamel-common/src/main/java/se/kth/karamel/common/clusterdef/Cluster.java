package se.kth.karamel.common.clusterdef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.util.AttributesValidator;

public class Cluster extends Scope {

  private String name;
  private Map<String, Cookbook> cookbooks = new HashMap<>();
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

  public Map<String, Cookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(Map<String, Cookbook> cookbooks) {
    this.cookbooks = cookbooks;
  }

  @Override
  public void validate() throws ValidationException, KaramelException {
    super.validate();

    // Validate cookbooks
    for (Cookbook cookbook : cookbooks.values()) {
      cookbook.validate();
    }

    // Validate Attributes
    (new AttributesValidator()).validateAttributes(attributes);

    // Validate Groups
    // TODO(Fabio) validate that there are not more than 2 groups with the same name
    for (Group g : groups) {
      g.validate();
    }
  }
}

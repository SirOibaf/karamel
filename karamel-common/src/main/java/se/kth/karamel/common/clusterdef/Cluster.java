package se.kth.karamel.common.clusterdef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.AttributesValidator;

public class Cluster extends Scope {

  private String name;
  private Map<String, Cookbook> cookbooks = new HashMap<>();
  private ArrayList<Group> groups = new ArrayList<>();

  public Cluster() { }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList<Group> getGroups() {
    return groups;
  }

  public void setGroups(ArrayList<Group> groups) {
    this.groups = groups;
  }

  public Map<String, Cookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(Map<String, Cookbook> cookbooks) {
    this.cookbooks = cookbooks;
  }

  @Override
  public void validate() throws KaramelException, InterruptedException {
    super.validate();

    // Validate cookbooks
    for (Cookbook cookbook : cookbooks.values()) {
      cookbook.validate();
    }

    // Clone and vendor cookbooks to validate attributes
    try {
      CookbookCache.getInstance().loadKaramelizedCookbooks(cookbooks);
    } catch (IOException e) {
      throw new KaramelException(e);
    }

    // Validate Attributes
    AttributesValidator.validateAttributes(attributes);

    // Validate Groups
    // TODO(Fabio) validate that there are not more than 2 groups with the same name
    for (Group g : groups) {
      g.validate();
    }
  }
}

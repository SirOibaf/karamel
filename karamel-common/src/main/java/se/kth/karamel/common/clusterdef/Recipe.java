package se.kth.karamel.common.clusterdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.Settings;

public class Recipe implements Comparable<Recipe>{

  @JsonIgnore
  private KaramelizedCookbook cookbook;

  private String name;

  public Recipe(KaramelizedCookbook cookbook, String name) {
    this.cookbook = cookbook;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public KaramelizedCookbook getCookbook() {
    return cookbook;
  }

  public void setCookbook(KaramelizedCookbook cookbook) {
    this.cookbook = cookbook;
  }

  @JsonIgnore
  public String getCanonicalName() {
    return cookbook.getCookbookName() + Settings.COOKBOOK_DELIMITER + name;
  }

  @Override
  public int compareTo(Recipe o) {
    return name.compareTo(o.name);
  }

}

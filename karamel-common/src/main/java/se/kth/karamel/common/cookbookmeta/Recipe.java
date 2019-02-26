package se.kth.karamel.common.cookbookmeta;

import se.kth.karamel.common.util.Settings;

public class Recipe {

  private String name;
  private String description;

  public Recipe() {
  }

  public Recipe(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getCanonicalName() {
    return Settings.RECIPE_CANONICAL_NAME(name);
  }
}

package se.kth.karamel.common.clusterdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.util.Settings;

public class Recipe implements Comparable<Recipe>{

  @JsonIgnore
  private KaramelizedCookbook cookbook;

  private String name;

  public Recipe() {}

  public Recipe(String name) {
    this.name = name;
  }

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
    if (name.contains(Settings.COOKBOOK_DELIMITER)) {
      return name;
    }

    return cookbook.getCookbookName() + Settings.COOKBOOK_DELIMITER + Settings.DEFAULT_RECIPE;
  }

  @Override
  public int compareTo(Recipe o) {
    return this.getCanonicalName().compareTo(o.getCanonicalName());
  }

  public void validate() throws KaramelException {
    CookbookCache cache = CookbookCache.getInstance();
    String recipeName = name;

    if (!name.contains(Settings.COOKBOOK_DELIMITER)) {
      recipeName = recipeName + Settings.COOKBOOK_DELIMITER +  Settings.DEFAULT_RECIPE;
      cookbook = cache.get(name);
    } else {
      cookbook = cache.get(name.split(Settings.COOKBOOK_DELIMITER)[0]);
    }

    for (String rName : cookbook.getMetadataRb().getRecipes().keySet()) {
      if (recipeName.equals(rName)) {
        return;
      }
    }

    throw new ValidationException("Recipe: " + getCanonicalName() + " does not exists");
  }
}

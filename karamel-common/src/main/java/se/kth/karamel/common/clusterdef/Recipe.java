package se.kth.karamel.common.clusterdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.util.Constants;

import java.io.IOException;

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
    if (name.contains(Constants.COOKBOOK_DELIMITER)) {
      return name;
    }

    return cookbook.getCookbookName() + Constants.COOKBOOK_DELIMITER + Constants.DEFAULT_RECIPE;
  }

  @Override
  public int compareTo(Recipe o) {
    return this.getCanonicalName().compareTo(o.getCanonicalName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Recipe recipe = (Recipe) o;

    return getCanonicalName().equals(recipe.getCanonicalName());
  }

  @Override
  public int hashCode() {
    return getCanonicalName().hashCode();
  }

  public void validate() throws IOException, KaramelException {
    CookbookCache cache = CookbookCache.getInstance();
    String recipeName = name;

    if (!name.contains(Constants.COOKBOOK_DELIMITER)) {
      recipeName = recipeName + Constants.COOKBOOK_DELIMITER +  Constants.DEFAULT_RECIPE;
      cookbook = cache.get(name);
    } else {
      cookbook = cache.get(name.split(Constants.COOKBOOK_DELIMITER)[0]);
    }

    for (String rName : cookbook.getMetadataRb().getRecipes().keySet()) {
      if (recipeName.equals(rName)) {
        return;
      }
    }

    throw new ValidationException("Recipe: " + getCanonicalName() + " does not exists");
  }
}

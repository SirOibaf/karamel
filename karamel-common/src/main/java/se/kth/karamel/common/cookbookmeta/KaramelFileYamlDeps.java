package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;
import se.kth.karamel.common.util.Settings;

public class KaramelFileYamlDeps {

  private String recipe;
  private List<String> local = null;
  private List<String> global = null;

  public String getRecipeCanonicalName() {
    return Settings.RECIPE_CANONICAL_NAME(recipe);
  }

  public String getRecipe() {
    return recipe;
  }

  public void setRecipe(String recipe) {
    this.recipe = recipe;
  }

  public List<String> getGlobal() {
    return global;
  }

  public void setGlobal(List<String> global) {
    this.global = new ArrayList<>();
    if (global != null) {
      for (String gl : global) {
        this.global.add(Settings.RECIPE_CANONICAL_NAME(gl));
      }
    }
  }

  public List<String> getLocal() {
    return local;
  }

  public void setLocal(List<String> local) {
    this.local = new ArrayList<>();
    if (local != null) {
      for (String loc : local) {
        this.local.add(Settings.RECIPE_CANONICAL_NAME(loc));
      }
    }
  }

}

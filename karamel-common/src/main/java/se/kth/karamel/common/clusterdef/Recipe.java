package se.kth.karamel.common.clusterdef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.CookbookCache;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.Settings;

public class Recipe implements Comparable<Recipe>{

  @JsonIgnore
  private KaramelizedCookbook cookbook;

  private String name;

  public Recipe() {

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
    return cookbook.getCookbookName() + Settings.COOKBOOK_DELIMITER + name;
  }

  @Override
  public int compareTo(Recipe o) {
    return this.getCanonicalName().compareTo(o.getCanonicalName());
  }

  public void validate() throws KaramelException {
    CookbookCache cache = CookbookCache.getInstance();
    String cookbookName = name;
    if (name.contains(Settings.COOKBOOK_DELIMITER)) {
      cookbookName = name.split(Settings.COOKBOOK_DELIMITER)[0];
    }

    cookbook = cache.get(cookbookName);
  }
}

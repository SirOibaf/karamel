package se.kth.karamel.common.clusterdef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.AttributesValidator;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

public class Group extends Scope {

  private String name;
  private int size;
  private List<Recipe> recipes = new ArrayList<>();

  public Group() {
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<Recipe> getRecipes() {
    return recipes;
  }

  public void setRecipes(List<Recipe> recipes) {
    this.recipes = recipes;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void validate() throws KaramelException {
    super.validate();

    if (recipes == null || recipes.isEmpty()) {
      throw new ValidationException("The group does not contain any recipe");
    }

    // Validate recipe name
    for (Recipe r : recipes) {
      r.validate();
    }

    // Validate duplicated recipes in the cluster definition
    if (recipes.stream().map(Recipe::getCanonicalName).collect(Collectors.toSet()).size() !=
        recipes.size()) {
      throw new ValidationException("Duplicated recipes found in group");
    }

    // Validate number of IPs in the Baremetal case
    if (getProvider() instanceof Baremetal) {
      int ipSize = getBaremetal().getIps().size();
      if (ipSize != size) {
        throw new ValidationException(
            String.format("Number of ip addresses is not equal to the group size %d != %d", ipSize, size));
      }
    }

    // Validate Group attributes
    AttributesValidator.validateAttributes(attributes);
  }

  @JsonIgnore
  public List<KaramelizedCookbook> getKaramelizedCookbooks() {
    return recipes.stream().map(Recipe::getCookbook).collect(Collectors.toList());
  }
}

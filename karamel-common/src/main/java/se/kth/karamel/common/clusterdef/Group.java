package se.kth.karamel.common.clusterdef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.util.AttributesValidator;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

public class Group extends Scope {

  private String name;
  private int size;

  private List<Recipe> recipes = new ArrayList<>();

  public Group() {
  }

  public String getName() {
    return name;
  }

  public final void setName(String name) throws ValidationException {
    if (!name.matches(Settings.AWS_GEOUPNAME_PATTERN)) {
      throw new ValidationException("Group name '%s' must start with letter/number and just lowercase ASCII letters, "
          + "numbers and dashes are accepted in the name.");
    }
    this.name = name;
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

  @Override
  public void validate() throws KaramelException {
    super.validate();

    // Validate duplicated recipes in the cluster definition
    if (recipes.stream().map(Recipe::getCanonicalName).collect(Collectors.toSet()).size() !=
        recipes.size()) {
      throw new ValidationException("Duplicated recipes found in group: " + name);
    }

    // Validate recipe name
    for (Recipe r : recipes) {
      r.validate();
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
    (new AttributesValidator()).validateAttributes(attributes);
  }

  @JsonIgnore
  public List<KaramelizedCookbook> getKaramelizedCookbooks() {
    return recipes.stream().map(Recipe::getCookbook).collect(Collectors.toList());
  }
}
